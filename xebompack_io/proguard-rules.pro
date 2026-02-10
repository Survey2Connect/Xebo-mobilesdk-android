# Proguard rules for XeboSurveyCollectorAndroidSdk

# Keep the public API classes and their members
-keep public class com.example.xebompack.XeboSurveyView {
    public *;
}

-keep public interface com.example.xebompack.XeboSurveyDelegate {
    *;
}

-keep public class com.example.xebompack.XeboSurveyPreloader {
    public *;
}

# Keep common Android View things
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# JS Interface names if used (though not explicitly used in targetContent, good practice)
-keepattributes JavascriptInterface

# Suppress warnings if necessary
-dontwarn com.example.xebompack.**