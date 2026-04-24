package hexlet.code.util;

import java.util.Map;

public class View {
    public static Map<String, Object> model(String str, Object obj) {
        return Map.of(str, obj);
    }
}
