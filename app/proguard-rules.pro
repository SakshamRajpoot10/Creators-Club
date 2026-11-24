# ProGuard & R8 Optimization Rules for Creators Club (Kreavilo Studio)

# 1. Kotlin & Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 2. Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <init>(...);
}
-keepclassmembers class * implements kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
}

# 3. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.creatorsclub.core.database.model.** { *; }
-keep interface com.creatorsclub.core.database.dao.** { *; }

# 4. Hilt & Dagger
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.ComponentManager
-keep class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# 5. AndroidX Media3 (ExoPlayer & Transformer)
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# 6. Compose Runtime & Animation
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# 7. MediaPipe & Computer Vision
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# 8. Project & Engine Models
-keep class com.creatorsclub.core.storage.model.** { *; }
-keep class com.creatorsclub.engine.canvas.model.** { *; }
-keep class com.creatorsclub.engine.image.model.** { *; }
-keep class com.creatorsclub.engine.video.model.** { *; }
-keep class com.creatorsclub.engine.ai.registry.** { *; }
