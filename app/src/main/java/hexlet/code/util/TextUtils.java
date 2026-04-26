package hexlet.code.util;

public class TextUtils {
    public static String shorten(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= 200) {
            return value;
        }
        return value.substring(0, 200) + "...";
    }
}
