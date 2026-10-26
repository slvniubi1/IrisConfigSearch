package dev.irisconfigsearch;

import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.option.MergedBooleanOption;
import net.irisshaders.iris.shaderpack.option.MergedStringOption;
import net.irisshaders.iris.shaderpack.option.OptionSet;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Adapter for the NeOculus 1.21.1 shader option model.
 *
 * The caller supplies the active ShaderPack. Discovering the active pack is
 * intentionally kept separate until the loader accessor is verified.
 */
public final class NeoOculusOptionAdapter {
    private NeoOculusOptionAdapter() {
    }

    public record Entry(String name, String description, String value, String type) {
        public Entry {
            Objects.requireNonNull(name, "name");
            description = description == null ? "" : description;
            value = value == null ? "" : value;
            Objects.requireNonNull(type, "type");
        }
    }

    /**
     * Converts parsed boolean and string options into simple searchable entries.
     * Null packs produce an empty list.
     */
    public static List<Entry> readOptions(ShaderPack pack) {
        if (pack == null) {
            return List.of();
        }

        OptionSet optionSet = pack.getShaderPackOptions().getOptionSet();
        OptionValues values = pack.getShaderPackOptions().getOptionValues();
        List<Entry> entries = new ArrayList<>();

        for (MergedBooleanOption merged : optionSet.getBooleanOptions().values()) {
            var option = merged.getOption();
            boolean defaultValue = option.getDefaultValue();
            entries.add(new Entry(
                    option.getName(),
                    option.getComment().orElse(""),
                    String.valueOf(values.getBooleanValue(option.getName()).orElse(defaultValue)),
                    "boolean"
            ));
        }

        for (MergedStringOption merged : optionSet.getStringOptions().values()) {
            var option = merged.getOption();
            String defaultValue = option.getDefaultValue();
            entries.add(new Entry(
                    option.getName(),
                    option.getComment().orElse(""),
                    values.getStringValue(option.getName()).orElse(defaultValue),
                    "string"
            ));
        }

        entries.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
    }
}
