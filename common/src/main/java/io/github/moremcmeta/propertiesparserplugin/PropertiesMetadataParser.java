/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.propertiesparserplugin;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.metadata.CombinedMetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ResourceRepository;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Reads metadata from .properties files.
 * @author soir20
 */
public final class PropertiesMetadataParser implements MetadataParser {
    private static final ResourceLocation EMISSIVE_CONFIG = new ResourceLocation("optifine/emissive.properties");
    private static final String ANIMATION_PATH_START = "optifine/anim/";
    private static final String NAMESPACE_SEP = ":";
    private static final String ANIMATION_SECTION = "animation";
    private static final String PARTS_KEY = "parts";
    private static final String OVERLAY_SECTION = "overlay";

    @Override
    public Map<ResourceLocation, MetadataView> parse(ResourceLocation metadataLocation, InputStream metadataStream,
                                                    ResourceRepository repository)
            throws InvalidMetadataException {
        Properties props = new Properties();
        try {
            props.load(metadataStream);
        } catch (IOException err) {
            throw new InvalidMetadataException(
                    String.format("Unable to load properties file %s: %s", metadataLocation, err.getMessage())
            );
        }

        Map<String, PropertiesMetadataView.Value> metadata = new HashMap<>();
        putAll(metadata, props);

        if (metadataLocation.equals(EMISSIVE_CONFIG)) {
            return readEmissiveFile(props, repository::list);
        }

        if (metadataLocation.getPath().startsWith(ANIMATION_PATH_START)) {
            return readAnimationFile(metadata, props, metadataLocation, repository);
        }

        throw new InvalidMetadataException(String.format("Support is not yet implemented for the OptiFine properties " +
                "file %s. If you're looking to implement a plugin that uses this file, feel free to submit a PR!",
                metadataLocation));
    }

    @Override
    public MetadataView combine(ResourceLocation textureLocation,
                                Map<? extends ResourceLocation, ? extends MetadataView> metadataByLocation)
            throws InvalidMetadataException {
        Set<String> sections = new HashSet<>();
        for (MetadataView view : metadataByLocation.values()) {
            for (String section : view.keys()) {
                if (!section.equals(ANIMATION_SECTION) && !sections.add(section)) {
                    throw new InvalidMetadataException("Conflicting key " + section + " provided by two metadata files");
                }
            }
        }

        // Combine all animations together into one view
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        List<PropertiesMetadataView.Value> animations = metadataByLocation.values().stream()
                .filter((view) -> view.subView(ANIMATION_SECTION).isPresent())
                .map((view) -> view.subView(ANIMATION_SECTION).get())
                .filter((view) -> view.subView(PARTS_KEY).isPresent())
                .map((view) -> view.subView(PARTS_KEY).get())
                .filter((view) -> view instanceof PropertiesMetadataView)
                .map((view) -> (PropertiesMetadataView) view)
                .flatMap((view) -> IntStream.range(0, view.size()).mapToObj(view::rawSubView))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        ImmutableMap<String, PropertiesMetadataView.Value> combinedAnimations = ImmutableMap.copyOf(
                IntStream.range(0, animations.size())
                        .mapToObj((index) -> Pair.of(String.valueOf(index), animations.get(index)))
                        .collect(Collectors.toMap(
                                Pair::getFirst,
                                Pair::getSecond
                        ))
        );

        ImmutableMap<String, PropertiesMetadataView.Value> animationSection;
        if (combinedAnimations.size() > 0) {
            animationSection = ImmutableMap.of(
                    ANIMATION_SECTION, new PropertiesMetadataView.Value(ImmutableMap.of(
                            PARTS_KEY, new PropertiesMetadataView.Value(combinedAnimations)
                    ))
            );
        } else {
            animationSection = ImmutableMap.of();
        }

        MetadataView combinedAnimationView = new PropertiesMetadataView(animationSection);

        // Include other views to avoid losing non-animation sections
        List<MetadataView> allViews = metadataByLocation.keySet().stream()
                .sorted()
                .map(metadataByLocation::get)
                .collect(Collectors.toList());
        allViews.add(0, combinedAnimationView);

        return new CombinedMetadataView(allViews);
    }

    /**
     * Reads metadata from an emissive textures file.
     * @param props                 all read properties
     * @param resourceSearcher      searches for resources that exist in any currently-applied resource pack
     * @return all metadata from an emissive textures files
     */
    private static Map<ResourceLocation, MetadataView> readEmissiveFile(
            Properties props, Function<Predicate<String>, Set<? extends ResourceLocation>> resourceSearcher
    ) throws InvalidMetadataException {
        String emissiveSuffix = require(props, "suffix.emissive") + ".png";

        Function<ResourceLocation, MetadataView> textureToView = (overlayLocation) -> new PropertiesMetadataView(
                ImmutableMap.of(
                        OVERLAY_SECTION,
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
                        textureToView
                ));
    }

    /**
     * Reads metadata from an animation file.
     * @param metadata              key-to-property map pre-filled with all properties in the file
     * @param props                 all read properties
     * @param metadataLocation      location of the animation file
     * @return all metadata from an animation files
     */
    private static Map<ResourceLocation, MetadataView> readAnimationFile(
            Map<String, PropertiesMetadataView.Value> metadata, Properties props,
            ResourceLocation metadataLocation, ResourceRepository repository) throws InvalidMetadataException {
        ResourceLocation to = convertToLocation(require(props, "to"), metadataLocation);

        if (props.containsKey("from")) {
            ResourceLocation from = convertToLocation(props.getProperty("from"), metadataLocation);
            InputStream fromStream = findTextureStream(from, repository);
            metadata.put("texture", new PropertiesMetadataView.Value(fromStream));
        }

        putIfValPresent(metadata, props, "w", "width", Function.identity());
        putIfValPresent(metadata, props, "h", "height", Function.identity());
        putIfValPresent(metadata, props, "duration", "frameTime", Function.identity());
        buildFrameList(props).ifPresent((value) -> metadata.put("frames", value));

        return ImmutableMap.of(
                to,
                new PropertiesMetadataView(
                        ImmutableMap.of(
                                "animation",
                                new PropertiesMetadataView.Value(ImmutableMap.of(
                                        "parts",
                                        new PropertiesMetadataView.Value(ImmutableMap.of(
                                                "0",
                                                new PropertiesMetadataView.Value(ImmutableMap.copyOf(metadata))
                                        ))
                                ))
                        )
                )
        );
    }

    /**
     * Finds an {@link InputStream} containing image data for a given texture.
     * @param location      location to search for
     * @param repository    resource repository to search in
     * @return input stream for the given texture
     * @throws InvalidMetadataException if the texture is not found
     */
    private static InputStream findTextureStream(ResourceLocation location, ResourceRepository repository)
            throws InvalidMetadataException {
        Optional<ResourceRepository.Pack> packWithFromTexture = repository.highestPackWith(location);
        if (!packWithFromTexture.isPresent()) {
            throw new InvalidMetadataException("Unable to find texture " + location);
        }

        Optional<InputStream> textureOptional = packWithFromTexture.get().resource(location);
        if (!textureOptional.isPresent()) {
            throw new InvalidMetadataException("Unable to find texture that should exist " + location);
        }

        return textureOptional.get();
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
        if (!maxDefinedTick.isPresent()) {
            return Optional.empty();
        }

        ImmutableMap.Builder<String, PropertiesMetadataView.Value> builder = new ImmutableMap.Builder<>();

        for (int index = 0; index <= maxDefinedTick.get(); index++) {
            String durationKey = "duration." + index;
            String tileKey = "tile." + index;

            ImmutableMap.Builder<String, PropertiesMetadataView.Value> frame = new ImmutableMap.Builder<>();

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
     * Adds a transformed value to the sub view, if it exists.
     * @param builder           builder to add properties to
     * @param props             all properties read
     * @param sourceKey         key of the property to retrieve
     * @param destinationKey    key of the transformed property that will be added to the builder
     * @param transformer       function to transform the value (only called if the value is non-null)
     */
    private static void putIfValPresent(Map<String, PropertiesMetadataView.Value> builder,
                                        Properties props, String sourceKey, String destinationKey,
                                        Function<String, String> transformer) {
        String value = props.getProperty(sourceKey);
        if (value != null) {
            value = transformer.apply(value);
            builder.put(destinationKey, new PropertiesMetadataView.Value(value));
        }
    }

    /**
     * Puts all properties in the given metadata.
     * @param metadata  map holding in-progress metadata
     * @param props     all properties to put in the metadata
     */
    private static void putAll(Map<String, PropertiesMetadataView.Value> metadata,
                               Properties props) {
        for (Object key : props.keySet()) {
            metadata.put((String) key, new PropertiesMetadataView.Value((String) props.get(key)));
        }
    }

    /**
     * Gets the parent of a file, assuming the file does not end with a slash.
     * @param path      path to the file
     * @return parent of the file
     */
    private static String parent(String path) {
        int slashIndex = path.lastIndexOf('/');
        if (slashIndex >= 0) {

            // Include the slash if the file has a parent
            return path.substring(0, slashIndex + 1);

        }

        return "";
    }

    /**
     * Expands special characters in OptiFine paths to make a complete path.
     * @param path              path to expand
     * @param metadataLocation  location of the metadata containing the path
     * @return path with special characters expanded
     */
    private static String expandPath(String path, ResourceLocation metadataLocation) {
        String namespace = "minecraft";
        if (path.contains(NAMESPACE_SEP)) {
            int separatorIndex = path.indexOf(NAMESPACE_SEP);
            namespace = path.substring(0, separatorIndex);
            path = path.substring(separatorIndex + 1);
        } else if (path.startsWith("~")) {
            path = path.replace("~", "optifine");
        } else if (path.startsWith("./")) {
            namespace = metadataLocation.getNamespace();
            path = path.replace("./", parent(metadataLocation.getPath()));
        }

        return namespace + NAMESPACE_SEP + path;
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
