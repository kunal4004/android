-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.**
-dontwarn okio.**

-keepattributes exceptions,innerclasses,signature,deprecated,sourcefile,linenumbertable,*annotation*,enclosingmethod

-keepclassmembers class * {
    @retrofit.** *;
    @android.** *;
    @za.co.wigroup.appserver.lib.annotations.optional *;
    @java.lang.annotation.** *;
}

-keep class retrofit.** {
    *;
}
-keep class com.squareup.okhttp.** {
    *;
}
-keep class okio.** {
    *;
}

-keep class android.** {
    *;
}

-keep class org.apache.http.**

-keep interface org.apache.http.**

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault