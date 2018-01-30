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
-dontwarn org.apache.**
-dontwarn com.facebook.infer.annotation.ReturnsOwnership
-dontwarn com.facebook.infer.annotation.Functional
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

#The proguard exclusions for JJWT & Jackson

-keepnames class com.fasterxml.jackson.databind.** { ; }
-dontwarn com.fasterxml.jackson.databind.*
-keepattributes InnerClasses

-keep class org.bouncycastle.** { ; }
-keepnames class org.bouncycastle.* { ; }
-dontwarn org.bouncycastle.*

-keep class io.jsonwebtoken.** { ; }
-keepnames class io.jsonwebtoken.* { ; }
-keepnames interface io.jsonwebtoken.* { *; }

-dontwarn javax.xml.bind.DatatypeConverter
-dontwarn io.jsonwebtoken.impl.Base64Codec

-keepnames class com.fasterxml.jackson.** { * ; }
-keepnames interface com.fasterxml.jackson.** { *; }

-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    (java.lang.Throwable);
}

