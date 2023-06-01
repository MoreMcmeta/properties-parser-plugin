package io.github.moremcmeta.propertiesreaderplugin.forge;

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataParserPlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.forge.api.client.MoreMcmetaClientPlugin;
import io.github.moremcmeta.propertiesreaderplugin.ModConstants;

/**
 * Implementation of the properties reader plugin on Forge.
 * @author soir20
 */
@SuppressWarnings("unused")
@MoreMcmetaClientPlugin
public class PropertiesReaderPluginForge implements MoreMcmetaMetadataParserPlugin {
    @Override
    public String extension() {
        return ModConstants.EXTENSION;
    }

    @Override
    public MetadataParser metadataParser() {
        return ModConstants.PARSER;
    }

    @Override
    public String id() {
        return ModConstants.MOD_ID;
    }
}
