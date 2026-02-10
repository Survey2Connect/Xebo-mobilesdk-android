# These rules are applied to the app that consumes this library

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

-keepattributes JavascriptInterface
