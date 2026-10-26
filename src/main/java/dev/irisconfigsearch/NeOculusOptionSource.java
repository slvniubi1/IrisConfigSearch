package dev.irisconfigsearch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Optional NeOculus bridge that avoids a compile-time dependency on NeOculus.
 *
 * The bridge uses the public API available in NeOculus 1.21.1:
 * Iris.getCurrentPack(), ShaderPack.getShaderPackOptions(), and the parsed
 * OptionSet/OptionValues model. If NeOculus is not installed or no pack is
 * active, this source returns an empty snapshot.
 */
public final class NeOculusOptionSource implements ShaderOptionSource {
    private static final String IRIS_CLASS = "net.irisshaders.iris.Iris";

    @Override
    public List<ShaderOptionEntry> getOptions() {
        final Class<?> iris;
        try {
            iris = Class.forName(IRIS_CLASS);
        } catch (ClassNotFoundException absent) {
            return List.of();
        }

        try {
            Object currentPackResult = invoke(iris, null, "getCurrentPack");
            Object currentPack = unwrapOptional(currentPackResult);
            if (currentPack == null) {
                return List.of();
            }

            Object packOptions = invoke(currentPack.getClass(), currentPack, "getShaderPackOptions");
            Object optionSet = invoke(packOptions.getClass(), packOptions, "getOptionSet");
            Object optionValues = invoke(packOptions.getClass(), packOptions, "getOptionValues");

            List<ShaderOptionEntry> entries = new ArrayList<>();
            appendBooleanOptions(entries, invoke(optionSet.getClass(), optionSet, "getBooleanOptions"),
                    optionValues);
            appendStringOptions(entries, invoke(optionSet.getClass(), optionSet, "getStringOptions"),
                    optionValues);

            entries.sort(Comparator.comparing(ShaderOptionEntry::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ShaderOptionEntry::name));
            return List.copyOf(entries);
        } catch (ReflectiveOperationException | ClassCastException error) {
            throw new IllegalStateException(
                    "Unable to read parsed shader options from this NeOculus version", error);
        }
    }

    private static void appendBooleanOptions(
            List<ShaderOptionEntry> entries, Object rawMap, Object optionValues)
            throws ReflectiveOperationException {
        for (Object raw : ((Map<?, ?>) rawMap).values()) {
            Object option = invoke(raw.getClass(), raw, "getOption");
            String name = (String) invoke(option.getClass(), option, "getName");
            String description = optionalString(invoke(option.getClass(), option, "getComment"));
            boolean defaultValue = (boolean) invoke(option.getClass(), option, "getDefaultValue");
            Object rawCurrent = invoke(optionValues.getClass(), optionValues, "getBooleanValue", name);
            Object currentEnum = rawCurrent;
            boolean currentValue = (boolean) invoke(currentEnum.getClass(), currentEnum, "orElse", defaultValue);

            entries.add(new ShaderOptionEntry(name, description, defaultValue,
                    "NeOculus parsed options", Optional.of(Boolean.toString(currentValue)),
                    "boolean", Optional.of(Boolean.toString(defaultValue))));
        }
    }

    private static void appendStringOptions(
            List<ShaderOptionEntry> entries, Object rawMap, Object optionValues)
            throws ReflectiveOperationException {
        for (Object raw : ((Map<?, ?>) rawMap).values()) {
            Object option = invoke(raw.getClass(), raw, "getOption");
            String name = (String) invoke(option.getClass(), option, "getName");
            String description = optionalString(invoke(option.getClass(), option, "getComment"));
            String defaultValue = (String) invoke(option.getClass(), option, "getDefaultValue");
            Object rawCurrent = invoke(optionValues.getClass(), optionValues, "getStringValue", name);
            String currentValue = optionalString(rawCurrent);
            entries.add(new ShaderOptionEntry(name, description, true,
                    "NeOculus parsed options", Optional.of(currentValue == null ? defaultValue : currentValue),
                    "string", Optional.ofNullable(defaultValue)));
        }
    }

    private static String optionalString(Object value) throws ReflectiveOperationException {
        Object unwrapped = unwrapOptional(value);
        return unwrapped == null ? "" : unwrapped.toString();
    }

    private static Object unwrapOptional(Object value) throws ReflectiveOperationException {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
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
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean compatible = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (arguments[i] == null) {
                    if (parameterTypes[i].isPrimitive()) {
                        compatible = false;
                        break;
                    }
                } else if (!wrapPrimitive(parameterTypes[i]).isInstance(arguments[i])) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                match = method;
                break;
            }
        }
        if (match == null) {
            throw new NoSuchMethodException(owner.getName() + "." + name
                    + " with " + arguments.length + " compatible argument(s)");
        }
        try {
            return match.invoke(target, arguments);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw error;
        }
    }
}
