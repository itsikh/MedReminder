# R8 / ProGuard rules.
#
# Release builds set isMinifyEnabled = true, so anything reached only by reflection has to
# be kept explicitly. Room, Hilt and Compose ship their own consumer rules; the entries
# below cover what this app does that R8 cannot see.

# ── Gson ────────────────────────────────────────────────────────────────────────────────
# Gson maps JSON keys onto field *names* by reflection. Without these, R8 renames the
# fields of the backup model classes and restoring a backup silently yields nulls.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses, EnclosingMethod

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Classes serialized or deserialized by Gson: keep their field names intact.
-keepclassmembers class com.itsikh.medreminder.backup.BackupContent { <fields>; }
-keepclassmembers class com.itsikh.medreminder.backup.BackupData { <fields>; }
-keepclassmembers class com.itsikh.medreminder.backup.SettingsData { <fields>; }

# Gson's reflective no-arg construction of Kotlin classes.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Room entities and enums ─────────────────────────────────────────────────────────────
# LogStatus round-trips through valueOf() in the TypeConverter, which is name-based.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.itsikh.medreminder.data.model.** { *; }

# ── OkHttp / Okio ───────────────────────────────────────────────────────────────────────
# Both reference optional platform APIs that are absent on Android.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Keep line numbers in crash reports ──────────────────────────────────────────────────
# CrashAutoReporter files stack traces to GitHub; without this they are unreadable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
