package io.github.moremcmeta.propertiesreaderplugin.fabric;

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataParserPlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.propertiesreaderplugin.ModConstants;

/**
 * Implementation of the properties reader plugin on Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class PropertiesReaderPluginFabric implements MoreMcmetaMetadataParserPlugin {
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
