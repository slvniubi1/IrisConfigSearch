package dev.irisconfigsearch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Optional NeOculus bridge. Reads parsed options and applies changes through
 * NeOculus' public option queue followed by Iris.reload().
 */
public final class NeOculusOptionSource implements ShaderOptionSource {
    private static final String IRIS_CLASS = "net.irisshaders.iris.Iris";

    @Override
    public List<ShaderOptionEntry> getOptions() {
        try {
            Class<?> iris = Class.forName(IRIS_CLASS);
            Object pack = unwrapOptional(invoke(iris, null, "getCurrentPack"));
            if (pack == null) return List.of();

            Object packOptions = invoke(pack.getClass(), pack, "getShaderPackOptions");
            Object optionSet = invoke(packOptions.getClass(), packOptions, "getOptionSet");
            Object optionValues = invoke(packOptions.getClass(), packOptions, "getOptionValues");

            List<ShaderOptionEntry> entries = new ArrayList<>();
            appendBooleanOptions(entries, invoke(optionSet.getClass(), optionSet, "getBooleanOptions"), optionValues);
            appendStringOptions(entries, invoke(optionSet.getClass(), optionSet, "getStringOptions"), optionValues);
            entries.sort(Comparator.comparing(ShaderOptionEntry::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ShaderOptionEntry::name));
            return List.copyOf(entries);
        } catch (ClassNotFoundException absent) {
            return List.of();
        } catch (ReflectiveOperationException | ClassCastException error) {
            throw new IllegalStateException("Unable to read parsed shader options from this NeOculus version", error);
        }
    }

    /**
     * Cycles a boolean option or advances a string option to its next allowed value.
     * NeOculus persists queued values to the shader-pack config when Iris.reload() runs.
     */
    public String nextValue(ShaderOptionEntry entry, String currentValue) {
        try {
            Class<?> iris = Class.forName(IRIS_CLASS);
            Object pack = unwrapOptional(invoke(iris, null, "getCurrentPack"));
            if (pack == null) throw new IllegalStateException("No active shader pack");
            Object packOptions = invoke(pack.getClass(), pack, "getShaderPackOptions");
            Object optionSet = invoke(packOptions.getClass(), packOptions, "getOptionSet");
            boolean isBoolean = "boolean".equals(entry.type());
            String mapMethod = isBoolean ? "getBooleanOptions" : "getStringOptions";
            Object raw = ((Map<?, ?>) invoke(optionSet.getClass(), optionSet, mapMethod)).get(entry.name());
            if (raw == null) throw new IllegalArgumentException("Unknown shader option: " + entry.name());
            Object option = invoke(raw.getClass(), raw, "getOption");
            if (isBoolean) return Boolean.toString(!Boolean.parseBoolean(currentValue));
            if (!"string".equals(entry.type())) throw new IllegalArgumentException("Read-only option");
            Object allowedRaw = invoke(option.getClass(), option, "getAllowedValues");
            List<String> allowed = new ArrayList<>();
            for (Object value : (Iterable<?>) allowedRaw) allowed.add(value.toString());
            if (allowed.isEmpty()) throw new IllegalStateException("No allowed values for " + entry.name());
            int index = allowed.indexOf(currentValue);
            return allowed.get((index + 1 + allowed.size()) % allowed.size());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Unable to read option from NeOculus.", error);
        }
    }

    public void applyChanges(Map<String, String> changes) {
        if (changes.isEmpty()) return;
        try {
            Class<?> iris = Class.forName(IRIS_CLASS);
            @SuppressWarnings("unchecked")
            Map<String, String> queue = (Map<String, String>) invoke(iris, null, "getShaderPackOptionQueue");
            queue.putAll(changes);
            invoke(iris, null, "reload");
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("NeOculus could not apply staged options.", error);
        }
    }

    private static void appendBooleanOptions(List<ShaderOptionEntry> entries, Object rawMap, Object optionValues)
            throws ReflectiveOperationException {
        for (Object raw : ((Map<?, ?>) rawMap).values()) {
            Object option = invoke(raw.getClass(), raw, "getOption");
            String name = (String) invoke(option.getClass(), option, "getName");
            String description = optionalString(invoke(option.getClass(), option, "getComment"));
            boolean defaultValue = (boolean) invoke(option.getClass(), option, "getDefaultValue");
            Object current = invoke(optionValues.getClass(), optionValues, "getBooleanValue", name);
            boolean currentValue = (boolean) invoke(current.getClass(), current, "orElse", defaultValue);
            entries.add(new ShaderOptionEntry(name, description, defaultValue, "NeOculus parsed options",
                    Optional.of(Boolean.toString(currentValue)), "boolean", Optional.of(Boolean.toString(defaultValue))));
        }
    }

    private static void appendStringOptions(List<ShaderOptionEntry> entries, Object rawMap, Object optionValues)
            throws ReflectiveOperationException {
        for (Object raw : ((Map<?, ?>) rawMap).values()) {
            Object option = invoke(raw.getClass(), raw, "getOption");
            String name = (String) invoke(option.getClass(), option, "getName");
            String description = optionalString(invoke(option.getClass(), option, "getComment"));
            String defaultValue = (String) invoke(option.getClass(), option, "getDefaultValue");
            Object current = unwrapOptional(invoke(optionValues.getClass(), optionValues, "getStringValue", name));
            entries.add(new ShaderOptionEntry(name, description, true, "NeOculus parsed options",
                    Optional.of(current == null ? defaultValue : current.toString()), "string",
                    Optional.of(defaultValue)));
        }
    }

    private static String optionalString(Object value) throws ReflectiveOperationException {
        Object unwrapped = unwrapOptional(value);
        return unwrapped == null ? "" : unwrapped.toString();
    }

    private static Object unwrapOptional(Object value) {
        return value instanceof Optional<?> optional ? optional.orElse(null) : value;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static Object invoke(Class<?> owner, Object target, String name, Object... arguments)
            throws ReflectiveOperationException {
        Method match = null;
        for (Method method : owner.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
            Class<?>[] parameters = method.getParameterTypes();
            boolean compatible = true;
            for (int i = 0; i < parameters.length; i++) {
                if (arguments[i] == null ? parameters[i].isPrimitive()
                        : !wrapPrimitive(parameters[i]).isInstance(arguments[i])) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) { match = method; break; }
        }
        if (match == null) throw new NoSuchMethodException(owner.getName() + "." + name);
        try {
            return match.invoke(target, arguments);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            throw error;
        }
    }
}
