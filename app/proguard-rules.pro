# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Program Files (x86)\Android\android-studio\sdk/tools/proguard/proguard-android.txt
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

-target 1.6
-dontobfuscate
-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dump ../bin/class_files.txt
-printseeds ../bin/seeds.txt
-printusage ../bin/unused.txt
-printmapping ../bin/mapping.txt

# The -optimizations option disables some arithmetic simplifications that Dalvik 1.0 and 1.5 can't handle.
-optimizations !code/simplification/arithmetic

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class com.google.inject.Binder
-keep class AnnotationDatabaseImpl

-keepclassmembers class * {
    @com.google.inject.Inject <fields>;
    @com.google.inject.Inject <init>(...);
}
# There's no way to keep all @Observes methods, so use the On*Event convention to identify event handlers
-keepclassmembers class * {
    void *(**On*Event);
}
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keep class roboguice.**
-keepattributes *Annotation*
-keepattributes Signature

 -keep class com.google.inject.** { *; }
 -keep class javax.inject.** { *; }
 -keep class javax.annotation.** { *; }

-keep class com.ecchilon.sadpanda.ExhentaiModule {
    <fields>;
    <methods>;
}
-keep class * extends com.ecchilon.sadpanda.ExhentaiModule {
    <fields>;
    <methods>;
 }

-keep class org.codehaus.jackson.map.ObjectMapper
-keep class com.ecchilon.sadpanda.imageviewer.GestureViewPager

# missing classes we can do without
-dontwarn org.roboguice.**
-dontwarn roboguice.**
-dontwarn com.google.**
-dontwarn org.codehaus.**
-dontwarn com.squareup.**