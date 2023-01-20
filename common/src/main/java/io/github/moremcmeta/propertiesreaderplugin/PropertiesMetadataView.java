package io.github.moremcmeta.propertiesreaderplugin;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * {@link MetadataView} implementation with an underlying .properties format.
 * @author soir20
 */
public class PropertiesMetadataView implements MetadataView {
    private final Map<String, Value> PROPERTIES;
    private final List<Value> VALUES_BY_INDEX;

    /**
     * Creates a new metadata view with the given properties at the root.
     * @param root              the root properties object
     */
    public PropertiesMetadataView(ImmutableMap<String, Value> root) {
        PROPERTIES = requireNonNull(root, "Properties root cannot be null");
        VALUES_BY_INDEX = new ArrayList<>(PROPERTIES.values());
    }

    @Override
    public int size() {
        return PROPERTIES.size();
    }

    @Override
    public Iterable<String> keys() {
        return PROPERTIES.keySet();
    }

    @Override
    public boolean hasKey(String key) {
        return PROPERTIES.containsKey(key);
    }

    @Override
    public boolean hasKey(int index) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        return index < PROPERTIES.size();
    }

    @Override
    public Optional<String> stringValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        return Optional.of(PROPERTIES.get(key).PROPERTY);
    }

    @Override
    public Optional<String> stringValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        return Optional.of(VALUES_BY_INDEX.get(index).PROPERTY);
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(PROPERTIES.get(key).PROPERTY));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(VALUES_BY_INDEX.get(index).PROPERTY));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> longValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(PROPERTIES.get(key).PROPERTY));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> longValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(VALUES_BY_INDEX.get(index).PROPERTY));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Float> floatValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        try {
            float value = Float.parseFloat(PROPERTIES.get(key).PROPERTY);
            if (Float.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Float> floatValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        try {
            float value = Float.parseFloat(VALUES_BY_INDEX.get(index).PROPERTY);
            if (Float.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        try {
            double value = Double.parseDouble(PROPERTIES.get(key).PROPERTY);
            if (Double.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        try {
            double value = Double.parseDouble(VALUES_BY_INDEX.get(index).PROPERTY);
            if (Double.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        if (isNotProperty(key)) {
            return Optional.empty();
        }

        return Optional.of("true".equalsIgnoreCase(PROPERTIES.get(key).PROPERTY));
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        if (isNotProperty(index)) {
            return Optional.empty();
        }

        return Optional.of("true".equalsIgnoreCase(VALUES_BY_INDEX.get(index).PROPERTY));
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        if (!hasKey(key)) {
            return Optional.empty();
        }

        Value value = PROPERTIES.get(key);
        if (value.IS_PROPERTY) {
            return Optional.empty();
        }

        return Optional.of(new PropertiesMetadataView(value.SUB_VIEW));
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        Value value = VALUES_BY_INDEX.get(index);
        if (value.IS_PROPERTY) {
            return Optional.empty();
        }

        return Optional.of(new PropertiesMetadataView(value.SUB_VIEW));
    }

    /**
     * Checks if a value is either not present or not a property (string).
     * @param key       key associated with the value
     * @return true if the value is not present or not a property, false otherwise
     */
    private boolean isNotProperty(String key) {
        return !hasKey(key) || !PROPERTIES.get(key).IS_PROPERTY;
    }

    /**
     * Checks if a value is either not present or not a property (string).
     * @param index       index of the value
     * @return true if the value is not present or not a property, false otherwise
     */
    private boolean isNotProperty(int index) {
        return !hasKey(index) || !VALUES_BY_INDEX.get(index).IS_PROPERTY;
    }

    /**
     * Holds either a property/string or a sub view for the {@link PropertiesMetadataView}.
     * Enforces valid inputs to the view at compile-time.
     * @author soir20
     */
    public static final class Value {
        private final boolean IS_PROPERTY;
        private final String PROPERTY;
        private final ImmutableMap<String, Value> SUB_VIEW;

        /**
         * Creates a new wrapper with a string property.
         * @param property      property to store
         */
        public Value(String property) {
            PROPERTY = requireNonNull(property, "Property cannot be null");
            SUB_VIEW = null;
            IS_PROPERTY = true;
        }

        /**
         * Creates a new wrapper with a sub view.
         * @param subView       sub view to store
         */
        public Value(ImmutableMap<String, Value> subView) {
            PROPERTY = null;
            SUB_VIEW = requireNonNull(subView, "Sub view cannot be null");
            IS_PROPERTY = false;
        }

    }

}
