package com.creatorsclub.core.storage.manager

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class StorageManager(private val context: Context) {

    /**
     * Directory for internal working projects.
     */
    fun getProjectsDir(): File {
        val dir = File(context.getExternalFilesDir(null), "projects")
        dir.mkdirs()
        return dir
    }

    /**
     * Directory for temporary render cache and frame buffers.
     */
    fun getCacheDir(): File {
        val dir = File(context.cacheDir, "kreavilo_temp")
        dir.mkdirs()
        return dir
    }

    /**
     * Clears transient temporary cache.
     */
    fun clearCache(): Long {
        val cache = getCacheDir()
        val size = calculateDirectorySize(cache)
        cache.deleteRecursively()
        cache.mkdirs()
        return size
    }

    /**
     * Exports a completed image to user's public gallery (DCIM/Pictures) via MediaStore.
     */
    fun exportImageToGallery(
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 95
    ): Result<Uri> = runCatching {
        val extension = when (format) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "jpg"
        }
        val mimeType = when (format) {
            Bitmap.CompressFormat.PNG -> "image/png"
            Bitmap.CompressFormat.WEBP -> "image/webp"
            else -> "image/jpeg"
        }

        val fullFilename = if (filename.endsWith(".$extension")) filename else "$filename.$extension"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fullFilename)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Kreavilo")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(format, quality, outputStream)
        } ?: throw IllegalStateException("Failed to write bitmap to MediaStore stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        uri
    }

    private fun calculateDirectorySize(directory: File): Long {
        var length = 0L
        directory.listFiles()?.forEach { file ->
            length += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return length
    }

    /**
     * Directory for creator-imported assets (fonts, audio, stickers, banners).
     */
    fun getCreatorHubDir(subfolder: String): File {
        val dir = File(context.filesDir, "creator_hub/$subfolder")
        dir.mkdirs()
        return dir
    }

    fun importAssetFromUri(sourceUri: Uri, subfolder: String, destinationFileName: String): Result<File> = runCatching {
        val destDir = getCreatorHubDir(subfolder)
        val destFile = File(destDir, destinationFileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open input stream for URI: $sourceUri")
        destFile
    }

    fun listImportedAssets(subfolder: String): List<File> {
        val dir = getCreatorHubDir(subfolder)
        return dir.listFiles()?.filter { it.isFile }?.toList() ?: emptyList()
    }

    fun deleteImportedAsset(subfolder: String, fileName: String): Boolean {
        val file = File(getCreatorHubDir(subfolder), fileName)
        return if (file.exists()) file.delete() else false
    }
}
