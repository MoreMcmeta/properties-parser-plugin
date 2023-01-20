package io.github.moremcmeta.propertiesreaderplugin;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
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

    @Override
    public Map<ResourceLocation, MetadataView> read(ResourceLocation metadataLocation, InputStream metadataStream,
                                                    Function<Predicate<String>, Set<ResourceLocation>> resourceSearcher)
            throws InvalidMetadataException {
        Properties properties = new Properties();
        try {
            properties.load(metadataStream);
        } catch (IOException err) {
            throw new InvalidMetadataException(
                    String.format("Unable to load properties file %s: %s", metadataLocation, err.getMessage())
            );
        }

        if (metadataLocation.equals(EMISSIVE_CONFIG)) {
            return readEmissiveFile(properties, resourceSearcher);
        }

        throw new InvalidMetadataException(String.format("Support is not yet implemented for the OptiFine properties " +
                "file %s. If you're looking to implement a plugin that uses this file, feel free to submit a PR!",
                metadataLocation));
    }

    /**
     * Reads metadata from an emissive textures file.
     * @param properties            all read properties
     * @param resourceSearcher      searches for resources that exist in any currently-applied resource pack
     * @return all metadata from an emissive textures files
     */
    private static Map<ResourceLocation, MetadataView> readEmissiveFile(
            Properties properties, Function<Predicate<String>, Set<ResourceLocation>> resourceSearcher
    ) {
        String emissiveSuffix = properties.getOrDefault("suffix.emissive", "_e") + ".png";

        Function<ResourceLocation, MetadataView> textureToView = (overlayLocation) -> new PropertiesMetadataView(
                ImmutableMap.of(
                        "overlay",
                        new PropertiesMetadataView.PropertyOrSubView(
                                ImmutableMap.of(
                                        "texture",
                                        new PropertiesMetadataView.PropertyOrSubView(overlayLocation.toString()),
                                        "emissive",
                                        new PropertiesMetadataView.PropertyOrSubView("true")
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

}
