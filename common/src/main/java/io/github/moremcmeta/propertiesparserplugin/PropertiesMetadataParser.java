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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.metadata.CombinedMetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.JsonMetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ResourceRepository;
import io.github.moremcmeta.moremcmeta.api.client.metadata.RootResourceName;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
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
        Pair<Properties, Map<String, PropertiesMetadataView.Value>> initRead = readProperties(metadataStream);
        Properties props = initRead.getFirst();
        Map<String, PropertiesMetadataView.Value> metadata = initRead.getSecond();

        if (metadataLocation.equals(EMISSIVE_CONFIG)) {
            return readEmissiveFile(props, repository);
        }

        if (metadataLocation.getPath().startsWith(ANIMATION_PATH_START)) {
            return readNonRootAnimationFile(metadata, props, metadataLocation, repository);
        }

        throw new InvalidMetadataException(String.format("Support is not yet implemented for the OptiFine properties " +
                "file %s. If you're looking to implement a plugin that uses this file, feel free to submit a PR!",
                metadataLocation), true);
    }

    @Override
    public Map<? extends RootResourceName, ? extends Map<? extends RootResourceName, ? extends MetadataView>> parse(
            ResourceRepository.Pack pack) {
        Map<RootResourceName, MetadataView> anims = new HashMap<>();
        int index = 0;

        while (true) {
            RootResourceName imageName = new RootResourceName(String.format("pack_anim%s.png", index));
            RootResourceName animName = new RootResourceName(String.format("pack_anim%s.properties", index));
            ResourceLocation animLocation = pack.locateRootResource(animName);
            Optional<InputStream> animStream = pack.resource(animLocation);

            if (animStream.isEmpty()) {
                break;
            }

            Pair<Properties, Map<String, PropertiesMetadataView.Value>> initRead;
            try {
                initRead = readProperties(animStream.get());
            } catch (InvalidMetadataException err) {
                LogManager.getLogger().error("Bad root animation file {}: {}", animName, err);
                break;
            }

            Properties props = initRead.getFirst();
            Map<String, PropertiesMetadataView.Value> metadata = initRead.getSecond();

            ResourceLocation imageLocation = pack.locateRootResource(imageName);
            pack.resource(imageLocation).ifPresent((imageStream) -> metadata.put(
                    "texture",
                    new PropertiesMetadataView.Value(imageStream))
            );

            anims.put(animName, readAnimationFile(metadata, props));
            index++;
        }

        return ImmutableMap.of(new RootResourceName("pack.png"), anims);
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
        List<PropertiesMetadataView.Value> animations = metadataByLocation.values().stream()
                .filter((view) -> view.subView(ANIMATION_SECTION).isPresent())
                .map((view) -> view.subView(ANIMATION_SECTION).orElseThrow())
                .filter((view) -> view.subView(PARTS_KEY).isPresent())
                .map((view) -> view.subView(PARTS_KEY).orElseThrow())
                .flatMap((view) -> IntStream.range(0, view.size()).mapToObj(view::subView))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PropertiesMetadataView.Value::new)
                .toList();

        ImmutableMap<String, PropertiesMetadataView.Value> combinedAnimations = ImmutableMap.copyOf(
                IntStream.range(0, animations.size())
                        .mapToObj((index) -> Pair.of(String.valueOf(index), animations.get(index)))
                        .collect(Collectors.toMap(
                                Pair::getFirst,
                                Pair::getSecond
                        ))
        );

        ImmutableMap<String, PropertiesMetadataView.Value> animationSection;
        if (!combinedAnimations.isEmpty()) {
            animationSection = ImmutableMap.of(
                    ANIMATION_SECTION, new PropertiesMetadataView.Value(new PropertiesMetadataView(
                            ImmutableMap.of(
                                    PARTS_KEY,
                                    new PropertiesMetadataView.Value(
                                            new PropertiesMetadataView(combinedAnimations)
                                    )
                            )
                    ))
            );
        } else {
            animationSection = ImmutableMap.of();
        }

        MetadataView combinedAnimationView = new PropertiesMetadataView(animationSection);

        // Include other views to avoid losing non-animation sections
        List<? extends MetadataView> sortedViews = metadataByLocation.keySet().stream()
                .sorted()
                .map(metadataByLocation::get)
                .toList();
        List<MetadataView> allViews = new ArrayList<>(sortedViews);
        allViews.add(0, combinedAnimationView);

        return new CombinedMetadataView(allViews);
    }

    /**
     * Reads all properties and adds them directly to the metadata.
     * @param metadataStream        metadata stream to read properties from
     * @return read properties and initial metadata
     * @throws InvalidMetadataException if the properties could not be read from the stream
     */
    private static Pair<Properties, Map<String, PropertiesMetadataView.Value>> readProperties(InputStream metadataStream)
            throws InvalidMetadataException {
        Properties props = new Properties();
        try {
            props.load(metadataStream);
        } catch (IOException err) {
            throw new InvalidMetadataException(
                    String.format("Unable to load properties file: %s", err.getMessage())
            );
        }

        Map<String, PropertiesMetadataView.Value> metadata = new HashMap<>();
        putAll(metadata, props);

        return Pair.of(props, metadata);
    }

    /**
     * Reads metadata from an emissive textures file.
     * @param props                 all read properties
     * @param repository            resource repository to search in
     * @return all metadata from an emissive textures files
     */
    private static Map<ResourceLocation, MetadataView> readEmissiveFile(Properties props, ResourceRepository repository)
            throws InvalidMetadataException {
        String emissiveSuffix = require(props, "suffix.emissive") + ".png";

        Function<ResourceLocation, MetadataView> overlayToView = (overlayLocation) -> new PropertiesMetadataView(
                ImmutableMap.of(
                        OVERLAY_SECTION,
                        new PropertiesMetadataView.Value(new PropertiesMetadataView(
                                ImmutableMap.of(
                                        "texture",
                                        new PropertiesMetadataView.Value(overlayLocation.toString()),
                                        "emissive",
                                        new PropertiesMetadataView.Value("true")
                                )
                        )
                        )
                )
        );

        return repository.list((fileName) -> fileName.endsWith(emissiveSuffix))
                .stream()
                .collect(Collectors.toMap(
                        (overlayLocation) -> textureFromOverlay(overlayLocation, emissiveSuffix),
                        (overlayLocation) -> addDefaultMetadata(
                                textureFromOverlay(overlayLocation, emissiveSuffix),
                                overlayToView.apply(overlayLocation),
                                repository
                        )
                ));
    }

    /**
     * Converts an emissive overlay location to the texture's location.
     * @param overlayLocation       overlay location to convert
     * @param emissiveSuffix        suffix of the overlay
     * @return location of texture underneath the overlay
     */
    private static ResourceLocation textureFromOverlay(ResourceLocation overlayLocation, String emissiveSuffix) {
        return new ResourceLocation(
                overlayLocation.getNamespace(),
                overlayLocation.getPath().replace(emissiveSuffix, ".png")
        );
    }

    /**
     * Adds metadata from Minecraft's default .mcmeta files if present.
     * @param textureLocation       location of the texture whose metadata is being processed
     * @param currentView           current metadata for the texture
     * @param repository            resource repository to search in
     * @return given metadata with default metadata added, if any
     */
    private static MetadataView addDefaultMetadata(ResourceLocation textureLocation, MetadataView currentView,
                                                   ResourceRepository repository) {
        ResourceLocation metadataLocation = new ResourceLocation(
                textureLocation.getNamespace(),
                textureLocation.getPath() + ".mcmeta"
        );

        // Add default metadata if it exists
        Optional<ResourceRepository.Pack> packOptional = repository.highestPackWith(metadataLocation, textureLocation);
        if (packOptional.isPresent()) {
            ResourceRepository.Pack pack = packOptional.get();
            InputStream metadataStream = pack.resource(metadataLocation).orElseThrow();

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new InputStreamReader(metadataStream, StandardCharsets.UTF_8));
                JsonObject metadataObject = GsonHelper.parse(bufferedReader);

                /* Parsed "animation" metadata will be under the "animation" section directly, not "animation" and
                   then the "parts" sub view. This means that the default animation will be ignored during
                   combination if there are .properties animations. */
                MetadataView defaultView = new JsonMetadataView(metadataObject, String::compareTo);
                return new CombinedMetadataView(ImmutableList.of(currentView, defaultView));

            } catch (JsonParseException parseError) {

                // Ignore invalid default metadata
                return currentView;

            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }

        return currentView;
    }

    /**
     * Reads metadata from an animation file that is not at the root of a resource pack.
     * @param metadata              key-to-property map pre-filled with all properties in the file
     * @param props                 all read properties
     * @param metadataLocation      location of the animation file
     * @return all metadata from the animation file
     */
    private static Map<ResourceLocation, MetadataView> readNonRootAnimationFile(
            Map<String, PropertiesMetadataView.Value> metadata, Properties props,
            ResourceLocation metadataLocation, ResourceRepository repository) throws InvalidMetadataException {
        ResourceLocation to = convertToLocation(require(props, "to"), metadataLocation);

        if (props.containsKey("from")) {
            ResourceLocation from = convertToLocation(props.getProperty("from"), metadataLocation);
            InputStream fromStream = findTextureStream(from, repository);
            metadata.put("texture", new PropertiesMetadataView.Value(fromStream));
        }

        return ImmutableMap.of(
                to,
                readAnimationFile(metadata, props)
        );
    }

    /**
     * Reads metadata common to both root and non-root animations from a file.
     * @param metadata              key-to-property map pre-filled with all properties in the file
     * @param props                 all read properties
     * @return all metadata from the animation file
     */
    private static MetadataView readAnimationFile(Map<String, PropertiesMetadataView.Value> metadata, Properties props) {
        putIfValPresent(metadata, props, "w", "width", Function.identity());
        putIfValPresent(metadata, props, "h", "height", Function.identity());
        putIfValPresent(metadata, props, "duration", "frameTime", Function.identity());
        buildFrameList(props).ifPresent((value) -> metadata.put("frames", value));

        return new PropertiesMetadataView(
                ImmutableMap.of(
                        "animation",
                        new PropertiesMetadataView.Value(new PropertiesMetadataView(
                                ImmutableMap.of(
                                        PARTS_KEY,
                                        new PropertiesMetadataView.Value(new PropertiesMetadataView(
                                                ImmutableMap.of(
                                                        "0",
                                                        new PropertiesMetadataView.Value(new PropertiesMetadataView(
                                                                ImmutableMap.copyOf(metadata)
                                                        ))
                                                )
                                        ))
                                )
                        ))
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
        if (packWithFromTexture.isEmpty()) {
            throw new InvalidMetadataException("Unable to find texture " + location);
        }

        Optional<InputStream> textureOptional = packWithFromTexture.get().resource(location);
        if (textureOptional.isEmpty()) {
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
        if (maxDefinedTick.isEmpty()) {
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

            builder.put(
                    String.valueOf(index),
                    new PropertiesMetadataView.Value(new PropertiesMetadataView(frame.build()))
            );
        }

        return Optional.of(new PropertiesMetadataView.Value(
                new PropertiesMetadataView(builder.build())
        ));
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
