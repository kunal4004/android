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
