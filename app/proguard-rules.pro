-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.**
-dontwarn okio.**

-keepattributes exceptions,innerclasses,signature,deprecated,sourcefile,linenumbertable,*annotation*,enclosingmethod

-keepclassmembers class * {
    @retrofit.** *;
    @android.** *;
    @za.co.wigroup.appserver.lib.annotations.optional *;
    @java.lang.annotation.** *;
    long producerIndex;
    long consumerIndex;
}

-keep class com.shockwave.**

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

-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.*
-keepattributes InnerClasses

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.* { *; }
-dontwarn org.bouncycastle.*

-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }

-dontwarn javax.xml.bind.DatatypeConverter
-dontwarn io.jsonwebtoken.impl.Base64Codec

-keepnames class com.fasterxml.jackson.** { * ; }
-keepnames interface com.fasterxml.jackson.** { *; }

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

#GSON
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

-keep class com.facebook.all.All

-ignorewarnings

-keep class * {
    public private *;
}

# fresco start------------------------
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}
-keep class com.facebook.animated.gif.** {
    *;
}

-keep class com.facebook.** {
    *;
}
-dontwarn okio.**
-dontwarn javax.annotation.**
# Keep native methods
-dontwarn com.android.volley.toolbox.**
# fresco end ------------------------

-keep class androidx.core.app.CoreComponentFactory { *; }
-keep public class com.google.android.material.bottomnavigation.BottomNavigationView { *; }
-keep public class com.google.android.material.bottomnavigation.BottomNavigationMenuView { *; }
-keep public class com.google.android.material.bottomnavigation.BottomNavigationPresenter { *; }
-keep public class com.google.android.material.bottomnavigation.BottomNavigationItemView { *; }


-keep public class  za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension {
    public <fields>;
}