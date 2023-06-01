package io.github.moremcmeta.propertiesparserplugin.fabric;

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataParserPlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.propertiesparserplugin.ModConstants;

/**
 * Implementation of the properties parser plugin on Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class PropertiesParserPluginFabric implements MoreMcmetaMetadataParserPlugin {
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
