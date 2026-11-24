package com.creatorsclub.feature.product

import android.app.Application
import android.graphics.*
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.storage.manager.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ProductStudioViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)

    private val _uiState = MutableStateFlow(ProductStudioUiState())
    val uiState: StateFlow<ProductStudioUiState> = _uiState.asStateFlow()

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bitmap = decodeSampledBitmap(uri)
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalBitmap = bitmap,
                        cutoutBitmap = null,
                        hasRemovedBackground = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load product image") }
            }
        }
    }

    fun removeBackground() {
        val src = _uiState.value.originalBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cutout = processCutout(src)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cutoutBitmap = cutout,
                    hasRemovedBackground = true
                )
            }
        }
    }

    fun setBackdrop(backdrop: StudioBackdrop) {
        _uiState.update { it.copy(selectedBackdrop = backdrop) }
    }

    fun updateScale(scale: Float) {
        _uiState.update { it.copy(productScale = scale) }
    }

    fun updateOffsetY(offsetY: Float) {
        _uiState.update { it.copy(productOffsetY = offsetY) }
    }

    fun updateShadowIntensity(intensity: Float) {
        _uiState.update { it.copy(shadowIntensity = intensity) }
    }

    fun exportProductImage() {
        val productBmp = _uiState.value.cutoutBitmap ?: _uiState.value.originalBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val rendered = renderFinalProductImage(
                product = productBmp,
                backdrop = state.selectedBackdrop,
                scale = state.productScale,
                offsetY = state.productOffsetY,
                shadowAlpha = state.shadowIntensity
            )

            val format = if (state.selectedBackdrop == StudioBackdrop.TRANSPARENT) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }

            val result = storageManager.exportImageToGallery(
                bitmap = rendered,
                filename = "Kreavilo_Product_${System.currentTimeMillis()}",
                format = format
            )

            result.onSuccess { uri ->
                _uiState.update { it.copy(isLoading = false, exportSuccessUri = uri) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    /**
     * High-speed on-device foreground extraction:
     * Evaluates corner background samples and produces alpha mask for crisp product isolation.
     */
    private suspend fun processCutout(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample corner colors (typical photo background anchors)
        val topLeft = pixels[0]
        val topRight = pixels[width - 1]
        val rAvg = (Color.red(topLeft) + Color.red(topRight)) / 2
        val gAvg = (Color.green(topLeft) + Color.green(topRight)) / 2
        val bAvg = (Color.blue(topLeft) + Color.blue(topRight)) / 2

        val threshold = 40

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = Color.red(c)
            val g = Color.green(c)
            val b = Color.blue(c)

            val diff = kotlin.math.abs(r - rAvg) + kotlin.math.abs(g - gAvg) + kotlin.math.abs(b - bAvg)
            if (diff < threshold) {
                pixels[i] = 0 // Transparent
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }

    private suspend fun renderFinalProductImage(
        product: Bitmap,
        backdrop: StudioBackdrop,
        scale: Float,
        offsetY: Float,
        shadowAlpha: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val outWidth = 1080
        val outHeight = 1080
        val output = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw Backdrop
        if (backdrop != StudioBackdrop.TRANSPARENT) {
            val bgColor = android.graphics.Color.parseColor(backdrop.primaryColorHex)
            canvas.drawColor(bgColor)

            // Draw soft pedestal gradient if pedestal mode
            if (backdrop == StudioBackdrop.CONCRETE_PEDESTAL) {
                val pedestalPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#D1D5DB")
                    isAntiAlias = true
                }
                canvas.drawOval(
                    140f, 750f, 940f, 950f, pedestalPaint
                )
            }
        }

        // 2. Draw Simulated Contact Drop Shadow
        if (shadowAlpha > 0f && backdrop != StudioBackdrop.TRANSPARENT) {
            val shadowPaint = Paint().apply {
                color = android.graphics.Color.argb((shadowAlpha * 255).roundToInt().coerceIn(0, 255), 30, 35, 50)
                isAntiAlias = true
                maskFilter = BlurMaskFilter(45f, BlurMaskFilter.Blur.NORMAL)
            }
            val shadowRect = RectF(
                outWidth * 0.25f,
                outHeight * 0.72f + offsetY,
                outWidth * 0.75f,
                outHeight * 0.82f + offsetY
            )
            canvas.drawOval(shadowRect, shadowPaint)
        }

        // 3. Draw Scaled & Centered Product
        val prodW = (outWidth * 0.7f * scale).roundToInt()
        val prodH = (product.height.toFloat() / product.width.toFloat() * prodW).roundToInt()
        val left = (outWidth - prodW) / 2f
        val top = ((outHeight - prodH) / 2f) + offsetY

        val dstRect = RectF(left, top, left + prodW, top + prodH)
        val productPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(product, null, dstRect, productPaint)

        output
    }

    private suspend fun decodeSampledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

                var sampleSize = 1
                val maxDim = maxOf(options.outWidth, options.outHeight)
                while (maxDim / sampleSize > 2048) {
                    sampleSize *= 2
                }

                context.contentResolver.openInputStream(uri)?.use { second ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeStream(second, null, opts)
                }
            }
        }.getOrNull()
    }
}
