//package ca.brock.cs.lambda.parser;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ConstructorRegistry {
//    private static final Map<String, List<String>> constructorsByType = new HashMap<>();
//
//    public static void registerType(String typeName, List<String> constructorNames) {
//        constructorsByType.put(typeName, new ArrayList<>(constructorNames));
//    }
//
//    public static List<String> getConstructors(String typeName) {
//        return constructorsByType.getOrDefault(typeName, Collections.emptyList());
//    }
//
//    public static void clear() {
//        constructorsByType.clear();
//    }
//}

package ca.brock.cs.lambda.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConstructorRegistry {
    public static final class ConstructorInfo {
        private final String typeName;
        private final String constructorName;
        private final int index;
        private final int arity;
        private final List<String> allConstructorsInOrder;

        public ConstructorInfo(
            String typeName,
            String constructorName,
            int index,
            int arity,
            List<String> allConstructorsInOrder
        ) {
            this.typeName = typeName;
            this.constructorName = constructorName;
            this.index = index;
            this.arity = arity;
            this.allConstructorsInOrder = List.copyOf(allConstructorsInOrder);
        }

        public String getTypeName() {
            return typeName;
        }

        public String getConstructorName() {
            return constructorName;
        }

        public int getIndex() {
            return index;
        }

        public int getArity() {
            return arity;
        }

        public int getConstructorCount() {
            return allConstructorsInOrder.size();
        }

        public List<String> getAllConstructorsInOrder() {
            return allConstructorsInOrder;
        }
    }

    public static final class ConstructorDataView {
        private final String name;
        private final int arity;

        public ConstructorDataView(String name, int arity) {
            this.name = name;
            this.arity = arity;
        }

        public String name() {
            return name;
        }

        public int arity() {
            return arity;
        }
    }
    private static final Map<String, ConstructorInfo> byConstructor = new HashMap<>();
    private static final Map<String, List<ConstructorInfo>> byType = new HashMap<>();

    private ConstructorRegistry() {}

    public static void registerType(String typeName, List<ConstructorDataView> constructors) {
        List<String> names = constructors.stream().map(ConstructorDataView::name).collect(Collectors.toList());
        List<ConstructorInfo> infos = new ArrayList<>();

        for (int i = 0; i < constructors.size(); i++) {
            ConstructorDataView cd = constructors.get(i);
            ConstructorInfo info = new ConstructorInfo(typeName, cd.name(), i, cd.arity(), names);
            infos.add(info);
            byConstructor.put(cd.name(), info);
        }

        byType.put(typeName, infos);
    }

    public static ConstructorInfo getConstructorInfo(String constructorName) {
        ConstructorInfo info = byConstructor.get(constructorName);
        if (info == null) {
            throw new IllegalArgumentException("Unknown constructor: " + constructorName);
        }
        return info;
    }

    public static List<String> getConstructors(String typeName) {
        List<ConstructorInfo> infos = byType.getOrDefault(typeName, Collections.emptyList());
        List<String> names = new ArrayList<>(infos.size());
        for (ConstructorInfo info : infos) {
            names.add(info.getConstructorName());
        }
        return names;
    }

    public static List<ConstructorInfo> getConstructorsForType(String typeName) {
        return byType.getOrDefault(typeName, Collections.emptyList());
    }

    public static void clear() {
        byConstructor.clear();
        byType.clear();
    }
}