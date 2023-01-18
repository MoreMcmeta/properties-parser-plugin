package io.github.moremcmeta.propertiesreaderplugin.forge;

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataReaderPlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.forge.api.client.MoreMcmetaClientPlugin;
import io.github.moremcmeta.propertiesreaderplugin.ModConstants;

/**
 * Implementation of the properties reader plugin on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@MoreMcmetaClientPlugin
public class PropertiesReaderPluginForge implements MoreMcmetaMetadataReaderPlugin {
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
