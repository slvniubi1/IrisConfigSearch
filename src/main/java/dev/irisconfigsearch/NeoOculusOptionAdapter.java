package dev.irisconfigsearch;

import net.irisshaders.iris.Iris;
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
 * Adapter for NeOculus 1.21.1's shader option model.
 *
 * This adapter deliberately accepts the active ShaderPack as an argument rather
 * than reaching into loader internals to discover the current pack. A client
 * integration can supply the pack once a stable accessor/event is selected.
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
     * Converts the active pack's parsed boolean and string options into entries.
     * Returns an empty list if no shader pack is supplied.
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
            entries.add(new Entry(
                    option.getName(),
                    option.getComment(),
                    String.valueOf(values.getBooleanValue(option.getName())),
                    "boolean"
            ));
        }

        for (MergedStringOption merged : optionSet.getStringOptions().values()) {
            var option = merged.getOption();
            entries.add(new Entry(
                    option.getName(),
                    option.getComment(),
                    values.getStringValue(option.getName()),
                    "string"
            ));
        }

        entries.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
    }
}
