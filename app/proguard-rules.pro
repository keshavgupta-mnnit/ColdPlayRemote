# --- WRIST DJ PRODUCTION PROGUARD RULES ---

# Preserve Line Numbers for Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Data Models (Crucial for JSON serialization/deserialization)
-keep class com.kglabs.wristdj.models.** { *; }

# Keep Compose internal annotations
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Timber
-keep class timber.log.Timber { *; }

# Firebase / Google Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# IR/Audio Utilities (Ensures reflection/dynamic access doesn't break)
-keep class com.kglabs.wristdj.utils.** { *; }

# Support for generic signatures and annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
