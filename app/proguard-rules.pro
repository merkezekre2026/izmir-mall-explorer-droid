# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.izmir.avmmap.**$$serializer { *; }
-keepclassmembers class com.izmir.avmmap.** { *** Companion; }
-keepclasseswithmembers class com.izmir.avmmap.** { kotlinx.serialization.KSerializer serializer(...); }

# WebView JavaScript Interface
-keepclassmembers class com.izmir.avmmap.presentation.map.WebAppInterface {
    @android.webkit.JavascriptInterface <methods>;
}
