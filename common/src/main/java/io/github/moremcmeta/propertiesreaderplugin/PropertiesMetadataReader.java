package io.github.moremcmeta.propertiesreaderplugin;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Reads metadata from .properties files.
 * @author soir20
 */
public class PropertiesMetadataReader implements MetadataReader {
    private static final ResourceLocation EMISSIVE_CONFIG = new ResourceLocation("optifine/emissive.properties");
    private static final String ANIMATION_PATH_START = "optifine/anim/";
    private static final String NAMESPACE_SEP = ":";
    private static final String PATH_SEP = "/";
    private static final String ASSETS_DIR = "/assets";
    private static final String OPTIFINE_HOME = ASSETS_DIR + "/minecraft/optifine/";

    @Override
    public Map<ResourceLocation, MetadataView> read(ResourceLocation metadataLocation, InputStream metadataStream,
                                                    Function<Predicate<String>, Set<ResourceLocation>> resourceSearcher)
            throws InvalidMetadataException {
        Properties props = new Properties();
        try {
            props.load(metadataStream);
        } catch (IOException err) {
            throw new InvalidMetadataException(
                    String.format("Unable to load properties file %s: %s", metadataLocation, err.getMessage())
            );
        }

        if (metadataLocation.equals(EMISSIVE_CONFIG)) {
            return readEmissiveFile(props, resourceSearcher);
        }

        if (metadataLocation.getPath().startsWith(ANIMATION_PATH_START)) {
            return readAnimationFile(props, metadataLocation);
        }

        throw new InvalidMetadataException(String.format("Support is not yet implemented for the OptiFine properties " +
                "file %s. If you're looking to implement a plugin that uses this file, feel free to submit a PR!",
                metadataLocation));
    }

    /**
     * Reads metadata from an emissive textures file.
     * @param props                 all read properties
     * @param resourceSearcher      searches for resources that exist in any currently-applied resource pack
     * @return all metadata from an emissive textures files
     */
    private static Map<ResourceLocation, MetadataView> readEmissiveFile(
            Properties props, Function<Predicate<String>, Set<ResourceLocation>> resourceSearcher
    ) {
        String emissiveSuffix = props.getOrDefault("suffix.emissive", "_e") + ".png";

        Function<ResourceLocation, MetadataView> textureToView = (overlayLocation) -> new PropertiesMetadataView(
                ImmutableMap.of(
                        "overlay",
                        new PropertiesMetadataView.Value(
                                ImmutableMap.of(
                                        "texture",
                                        new PropertiesMetadataView.Value(overlayLocation.toString()),
                                        "emissive",
                                        new PropertiesMetadataView.Value("true")
                                )
                        )
                )
        );

        return resourceSearcher.apply((fileName) -> fileName.endsWith(emissiveSuffix))
                .stream()
                .collect(Collectors.toMap(
                        (overlayLocation) -> new ResourceLocation(
                                overlayLocation.getNamespace(),
                                overlayLocation.getPath().replace(emissiveSuffix, ".png")
                        ),
                        (overlayLocation) -> textureToView.apply(toSpriteName(overlayLocation))
                ));
    }

    /**
     * Reads metadata from an animation file.
     * @param props                 all read properties
     * @param metadataLocation      location of the animation file
     * @return all metadata from an animation files
     */
    private static Map<ResourceLocation, MetadataView> readAnimationFile(Properties props,
                                                                         ResourceLocation metadataLocation)
            throws InvalidMetadataException {
        ResourceLocation from = convertToLocation(require(props, "from"), metadataLocation);
        ImmutableMap.Builder<String, PropertiesMetadataView.Value> builder = new ImmutableMap.Builder<>();

        putIfValPresent(builder, props, "to", "base", (path) -> PropertiesMetadataReader.expandPath(
                path,
                metadataLocation
        ));
        putIfValPresent(builder, props, "x", "x", Function.identity());
        putIfValPresent(builder, props, "y", "y", Function.identity());
        putIfValPresent(builder, props, "w", "width", Function.identity());
        putIfValPresent(builder, props, "h", "height", Function.identity());
        putIfValPresent(builder, props, "interpolate", "interpolate", Function.identity());
        putIfValPresent(builder, props, "skip", "skip", Function.identity());
        putIfValPresent(builder, props, "duration", "frameTime", Function.identity());
        buildFrameList(props).ifPresent((value) -> builder.put("frames", value));

        return ImmutableMap.of(
                from,
                new PropertiesMetadataView(builder.build())
        );
    }

    /**
     * Builds a list of animation frames, if properties for individual frames is present.
     * @param props     all properties read
     * @return list of animation frames or {@link Optional#empty()} if there are no individual frame settings
     */
    private static Optional<PropertiesMetadataView.Value> buildFrameList(Properties props) {
        Optional<Integer> maxDefinedTick = props.stringPropertyNames().stream()
                .filter((propName) -> propName.matches("(duration|tile)\\.\\d+"))
                .map((propName) -> Integer.parseInt(propName.substring(propName.indexOf('.') + 1)))
                .max(Integer::compareTo);
        if (maxDefinedTick.isEmpty()) {
            return Optional.empty();
        }

        ImmutableMap.Builder<String, PropertiesMetadataView.Value> builder = new ImmutableMap.Builder<>();

        for (int index = 0; index <= maxDefinedTick.get(); index++) {
            String durationKey = "duration." + index;
            String tileKey = "tile." + index;

            ImmutableMap.Builder<String, PropertiesMetadataView.Value> frame =
                    new ImmutableMap.Builder<>();

            if (props.containsKey(durationKey)) {
                frame.put(
                        "time",
                        new PropertiesMetadataView.Value(
                                (String) props.get(durationKey)
                        )
                );
            }

            frame.put(
                    "index",
                    new PropertiesMetadataView.Value(
                            (String) props.getOrDefault(tileKey, String.valueOf(index))
                    )
            );

            builder.put(String.valueOf(index), new PropertiesMetadataView.Value(frame.build()));
        }

        return Optional.of(new PropertiesMetadataView.Value(builder.build()));
    }

    /**
     * Converts a standard texture location (with textures/ prefix and .png suffix) to a sprite name.
     * @param textureLocation      the texture location to convert
     * @return the sprite name corresponding to the texture location
     */
    private static ResourceLocation toSpriteName(ResourceLocation textureLocation) {
        final int SUFFIX_LENGTH = ".png".length();
        String originalPath = textureLocation.getPath();
        String spriteName = originalPath.replace("textures/", "");
        if (spriteName.length() >= SUFFIX_LENGTH) {
            spriteName = spriteName.substring(0, spriteName.length() - SUFFIX_LENGTH);
        }
        return new ResourceLocation(textureLocation.getNamespace(), spriteName);
    }

    /**
     * Adds a transformed value to the sub view, if it exists.
     * @param builder           builder to add properties to
     * @param props             all properties read
     * @param sourceKey         key of the property to retrieve
     * @param destinationKey    key of the transformed property that will be added to the builder
     * @param transformer       function to transform the value (only called if the value is non-null)
     */
    private static void putIfValPresent(ImmutableMap.Builder<String, PropertiesMetadataView.Value> builder,
                                        Properties props, String sourceKey, String destinationKey,
                                        Function<String, String> transformer) {
        String value = props.getProperty(sourceKey);
        if (value != null) {
            value = transformer.apply(value);
            builder.put(destinationKey, new PropertiesMetadataView.Value(value));
        }
    }

    /**
     * Expands special characters in OptiFine paths to make a complete path.
     * @param path              path to expand
     * @param metadataLocation  location of the metadata containing the path
     * @return path with special characters expanded
     */
    private static String expandPath(String path, ResourceLocation metadataLocation) {

        // Process namespace
        if (path.contains(NAMESPACE_SEP)) {
            int separatorIndex = path.indexOf(NAMESPACE_SEP);
            String namespace = path.substring(0, separatorIndex);
            path = path.substring(separatorIndex + 1);
            path = ASSETS_DIR + PATH_SEP + namespace + PATH_SEP + path;
        }

        // Process home
        path = path.replaceAll("~", OPTIFINE_HOME);

        // Process ./, ../, and no start symbol
        try {
            Path userPath = Paths.get(path);

            if (path.startsWith("./") || path.startsWith("../")) {
                Path metadataPath = Paths.get(ASSETS_DIR, metadataLocation.getNamespace(), metadataLocation.getPath());
                path = metadataPath.resolve(userPath).normalize().toString();
            } else {
                Path homePath = Paths.get(OPTIFINE_HOME);
                path = homePath.resolve(userPath).normalize().toString();
            }
        } catch (InvalidPathException ignored) {

            // An exception will be raised in the caller when the path is converted to a ResourceLocation
            return path;

        }

        /* At this point, the path has been normalized to /assets/namespace/...
           Now, we need to add the namespace so that it is properly converted to a ResourceLocation. */
        path = path.substring(ASSETS_DIR.length());
        if (path.contains(PATH_SEP)) {
            int separatorIndex = path.indexOf(PATH_SEP);
            String namespace = path.substring(0, separatorIndex);
            path = namespace + NAMESPACE_SEP + path.substring(separatorIndex + 1);
        }

        return path;
    }

    /**
     * Retrieve a value or throw an {@link InvalidMetadataException} if it does not exist.
     * @param properties    all properties read
     * @param key           key of the required property
     * @return value of the property, if it exists
     * @throws InvalidMetadataException if the property is not present (value is null)
     */
    private static String require(Properties properties, String key) throws InvalidMetadataException {
        String property = (String) properties.get(key);

        if (property == null) {
            throw new InvalidMetadataException("Missing required key: " + key);
        }

        return property;
    }

    /**
     * Tries to convert a string to a {@link ResourceLocation}. If the conversion fails, throws an
     * {@link InvalidMetadataException}.
     * @param path              path to convert
     * @param metadataLocation  location of the metadata containing the path
     * @return path as a {@link ResourceLocation}
     * @throws InvalidMetadataException if the path cannot be converted to a valid {@link ResourceLocation}
     */
    private static ResourceLocation convertToLocation(String path, ResourceLocation metadataLocation)
            throws InvalidMetadataException {
        try {
            return new ResourceLocation(expandPath(path, metadataLocation));
        } catch (ResourceLocationException err) {
            throw new InvalidMetadataException(err.getMessage());
        }
    }

}
