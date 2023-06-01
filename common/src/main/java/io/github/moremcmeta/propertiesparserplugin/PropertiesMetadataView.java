package io.github.moremcmeta.propertiesparserplugin;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.NegativeKeyIndexException;

import java.io.InputStream;
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
        if (isNotString(key)) {
            return Optional.empty();
        }

        return Optional.of(PROPERTIES.get(key).STRING);
    }

    @Override
    public Optional<String> stringValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        return Optional.of(VALUES_BY_INDEX.get(index).STRING);
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        if (isNotString(key)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(PROPERTIES.get(key).STRING));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(VALUES_BY_INDEX.get(index).STRING));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> longValue(String key) {
        if (isNotString(key)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(PROPERTIES.get(key).STRING));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> longValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(VALUES_BY_INDEX.get(index).STRING));
        } catch (NumberFormatException err) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Float> floatValue(String key) {
        if (isNotString(key)) {
            return Optional.empty();
        }

        try {
            float value = Float.parseFloat(PROPERTIES.get(key).STRING);
            if (Float.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Float> floatValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        try {
            float value = Float.parseFloat(VALUES_BY_INDEX.get(index).STRING);
            if (Float.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        if (isNotString(key)) {
            return Optional.empty();
        }

        try {
            double value = Double.parseDouble(PROPERTIES.get(key).STRING);
            if (Double.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        try {
            double value = Double.parseDouble(VALUES_BY_INDEX.get(index).STRING);
            if (Double.isFinite(value)) {
                return Optional.of(value);
            }
        } catch (NumberFormatException ignored) {}

        return Optional.empty();
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        if (isNotString(key)) {
            return Optional.empty();
        }

        return Optional.of("true".equalsIgnoreCase(PROPERTIES.get(key).STRING));
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        if (isNotString(index)) {
            return Optional.empty();
        }

        return Optional.of("true".equalsIgnoreCase(VALUES_BY_INDEX.get(index).STRING));
    }

    @Override
    public Optional<InputStream> byteStreamValue(String key) {
        if (!hasKey(key) || PROPERTIES.get(key).TYPE != ValueType.BYTE_STREAM) {
            return Optional.empty();
        }

        return Optional.of(PROPERTIES.get(key).BYTE_STREAM);
    }

    @Override
    public Optional<InputStream> byteStreamValue(int index) {
        if (!hasKey(index) || VALUES_BY_INDEX.get(index).TYPE != ValueType.BYTE_STREAM) {
            return Optional.empty();
        }

        return Optional.of(VALUES_BY_INDEX.get(index).BYTE_STREAM);
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        if (!hasKey(key)) {
            return Optional.empty();
        }

        Value value = PROPERTIES.get(key);
        if (value.TYPE != ValueType.SUB_VIEW) {
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
        if (value.TYPE != ValueType.SUB_VIEW) {
            return Optional.empty();
        }

        return Optional.of(new PropertiesMetadataView(value.SUB_VIEW));
    }

    /**
     * Gets the map underlying the sub view, if present.
     * @param key       key of the sub view to retrieve
     * @return map underlying the sub view wrapped in a {@link Value}, if present
     */
    public Optional<Value> rawSubView(String key) {
        if (!hasKey(key)) {
            return Optional.empty();
        }

        Value value = PROPERTIES.get(key);
        if (value.TYPE != ValueType.SUB_VIEW) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    /**
     * Checks if a value is either not present or not a string.
     * @param key       key associated with the value
     * @return true if the value is not present or not a string, false otherwise
     */
    private boolean isNotString(String key) {
        return !hasKey(key) || PROPERTIES.get(key).TYPE != ValueType.STRING;
    }

    /**
     * Checks if a value is either not present or not a string.
     * @param index       index of the value
     * @return true if the value is not present or not a string, false otherwise
     */
    private boolean isNotString(int index) {
        return !hasKey(index) || VALUES_BY_INDEX.get(index).TYPE != ValueType.STRING;
    }

    /**
     * Holds either a property/string or a sub view for the {@link PropertiesMetadataView}.
     * Enforces valid inputs to the view at compile-time.
     * @author soir20
     */
    public static final class Value {
        private final ValueType TYPE;
        private final String STRING;
        private final InputStream BYTE_STREAM;
        private final ImmutableMap<String, Value> SUB_VIEW;

        /**
         * Creates a new wrapper with a string.
         * @param value      string value to store
         */
        public Value(String value) {
            STRING = requireNonNull(value, "Property cannot be null");
            BYTE_STREAM = null;
            SUB_VIEW = null;
            TYPE = ValueType.STRING;
        }

        /**
         * Creates a new wrapper with a sub view.
         * @param byteStream    byte stream to store
         */
        public Value(InputStream byteStream) {
            STRING = null;
            BYTE_STREAM = requireNonNull(byteStream, "Byte stream cannot be null");
            SUB_VIEW = null;
            TYPE = ValueType.BYTE_STREAM;
        }

        /**
         * Creates a new wrapper with a sub view.
         * @param subView       sub view to store
         */
        public Value(ImmutableMap<String, Value> subView) {
            STRING = null;
            BYTE_STREAM = null;
            SUB_VIEW = requireNonNull(subView, "Sub view cannot be null");
            TYPE = ValueType.SUB_VIEW;
        }

    }

    private enum ValueType {
        STRING,
        BYTE_STREAM,
        SUB_VIEW
    }

}
