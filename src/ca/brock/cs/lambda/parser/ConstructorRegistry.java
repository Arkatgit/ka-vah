package ca.brock.cs.lambda.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructorRegistry {
    private static final Map<String, List<String>> constructorsByType = new HashMap<>();

    public static void registerType(String typeName, List<String> constructorNames) {
        constructorsByType.put(typeName, new ArrayList<>(constructorNames));
    }

    public static List<String> getConstructors(String typeName) {
        return constructorsByType.getOrDefault(typeName, Collections.emptyList());
    }

    public static void clear() {
        constructorsByType.clear();
    }
}