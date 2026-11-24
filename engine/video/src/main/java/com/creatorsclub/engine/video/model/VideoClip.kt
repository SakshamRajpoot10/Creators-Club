package com.creatorsclub.engine.video.model

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
enum class VideoTransition(val displayName: String) {
    NONE("Cut (Direct)"),
    CROSSFADE("Crossfade"),
    DISSOLVE("Dissolve"),
    SLIDE_LEFT("Slide Left"),
    WIPE("Wipe"),
    ZOOM_IN("Zoom In")
}

data class VideoClip(
    val id: String,
    val uri: Uri,
    val name: String,
    val startMs: Long = 0L,
    val endMs: Long,
    val durationMs: Long,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val transition: VideoTransition = VideoTransition.NONE
) {
    val trimmedDurationMs: Long get() = (endMs - startMs).coerceAtLeast(0L)
    val effectiveDurationMs: Long get() = ((endMs - startMs).coerceAtLeast(0L) / speed.coerceAtLeast(0.1f)).toLong()
}
