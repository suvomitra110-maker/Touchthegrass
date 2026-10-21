# Add project specific ProGuard rules here.
# Keep kotlinx.serialization classes for Gemini REST API
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.example.network.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.network.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room database entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Strip verbose debug logs in release builds for security hardening
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
