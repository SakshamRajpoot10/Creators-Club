package com.creatorsclub.engine.video.exporter

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.creatorsclub.engine.video.model.VideoClip
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object Media3VideoExporter {

    /**
     * Concatenates and exports video clips using hardware-accelerated Media3 Transformer.
     * Clean MP4 export, zero watermarks.
     */
    suspend fun exportClips(
        context: Context,
        clips: List<VideoClip>,
        outputFile: File
    ): Result<File> = suspendCancellableCoroutine { continuation ->
        if (clips.isEmpty()) {
            continuation.resume(Result.failure(IllegalArgumentException("No clips to export")))
            return@suspendCancellableCoroutine
        }

        outputFile.parentFile?.mkdirs()

        val editedMediaItems = clips.map { clip ->
            val clippingConfiguration = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clip.startMs)
                .setEndPositionMs(clip.endMs)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(clip.uri)
                .setClippingConfiguration(clippingConfiguration)
                .build()

            EditedMediaItem.Builder(mediaItem)
                .build()
        }

        val sequence = EditedMediaItemSequence(editedMediaItems)
        val composition = Composition.Builder(listOf(sequence)).build()

        val transformerListener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                continuation.resume(Result.success(outputFile))
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                continuation.resume(Result.failure(exportException))
            }
        }

        val transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()

        transformer.start(composition, outputFile.absolutePath)
    }
}
