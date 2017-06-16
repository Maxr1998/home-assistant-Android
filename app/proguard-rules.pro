# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/max/Development/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keepclassmembers class io.homeassistant.android.api.icons.MaterialDesignIconsUtils$ManifestItem {
    public java.lang.String *;
}

-keepclassmembers class io.homeassistant.android.api.requests.** {
    protected final *** *;
}

-keep class io.homeassistant.android.api.results.** {
    public *** *;
}

-keep class * extends io.homeassistant.android.view.viewholders.BaseViewHolder {
    public <init>(...);
}

# Kotlin
-dontwarn kotlin.**

# OkHttp
-dontwarn okio.**

# Other warnings
-dontwarn android.content.ServiceConnection$$CC
-dontwarn java.lang.invoke.MethodHandles
-dontwarn java.lang.invoke.MethodHandles$Lookup