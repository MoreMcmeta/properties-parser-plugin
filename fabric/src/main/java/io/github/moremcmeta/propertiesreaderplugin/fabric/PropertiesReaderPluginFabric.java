package io.github.moremcmeta.propertiesreaderplugin.fabric;

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataReaderPlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.propertiesreaderplugin.ModConstants;

/**
 * Implementation of the properties reader plugin on Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class PropertiesReaderPluginFabric implements MoreMcmetaMetadataReaderPlugin {
    @Override
    public String extension() {
        return ModConstants.EXTENSION;
    }

    @Override
    public MetadataReader metadataReader() {
        return ModConstants.READER;
    }

    @Override
    public String displayName() {
        return ModConstants.DISPLAY_NAME;
    }
}
