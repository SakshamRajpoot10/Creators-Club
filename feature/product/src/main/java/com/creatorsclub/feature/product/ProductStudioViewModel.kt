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

    fun loadBitmapDirectly(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                originalBitmap = bitmap,
                cutoutBitmap = null,
                hasRemovedBackground = false
            )
        }
    }

    fun loadSampleProduct() {
        val width = 600
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw stylish product bottle / box
        paint.color = Color.parseColor("#405BC7")
        canvas.drawRoundRect(RectF(180f, 160f, 420f, 520f), 24f, 24f, paint)

        paint.color = Color.parseColor("#7664C7")
        canvas.drawRoundRect(RectF(240f, 100f, 360f, 170f), 12f, 12f, paint)

        paint.color = Color.WHITE
        paint.alpha = 180
        canvas.drawRoundRect(RectF(210f, 260f, 390f, 360f), 12f, 12f, paint)

        _uiState.update {
            it.copy(
                originalBitmap = bitmap,
                cutoutBitmap = bitmap,
                hasRemovedBackground = true
            )
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

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessUri = null) }
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

    private suspend fun processCutout(src: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val bgSample = pixels[0]
        val bgR = Color.red(bgSample)
        val bgG = Color.green(bgSample)
        val bgB = Color.blue(bgSample)
        val tolerance = 48

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)

            val diff = kotlin.math.abs(r - bgR) + kotlin.math.abs(g - bgG) + kotlin.math.abs(b - bgB)
            if (diff < tolerance) {
                pixels[i] = Color.TRANSPARENT
            }
        }

        output.setPixels(pixels, 0, w, 0, 0, w, h)
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

        // Draw backdrop
        if (backdrop != StudioBackdrop.TRANSPARENT) {
            val bgPaint = Paint().apply {
                color = Color.parseColor(backdrop.primaryColorHex)
            }
            canvas.drawRect(0f, 0f, outWidth.toFloat(), outHeight.toFloat(), bgPaint)

            // Optional podium base
            if (backdrop == StudioBackdrop.CONCRETE_PEDESTAL) {
                val podiumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E5E7EB")
                }
                val rect = RectF(190f, 750f, 890f, 860f)
                canvas.drawOval(rect, podiumPaint)
            }
        }

        // Draw shadow
        if (shadowAlpha > 0f && backdrop != StudioBackdrop.TRANSPARENT) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (shadowAlpha * 120).roundToInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
            }
            val shadowY = 740f + offsetY * 2.5f
            val shadowRect = RectF(290f, shadowY - 40f, 790f, shadowY + 40f)
            canvas.drawOval(shadowRect, shadowPaint)
        }

        // Draw product centered
        val pw = (product.width * scale * (outWidth / 600f)).toInt()
        val ph = (product.height * scale * (outHeight / 600f)).toInt()
        val left = (outWidth - pw) / 2f
        val top = (outHeight - ph) / 2f + offsetY * 2.5f

        val destRect = RectF(left, top, left + pw, top + ph)
        canvas.drawBitmap(product, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))

        output
    }

    private suspend fun decodeSampledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    }.getOrNull()
}
