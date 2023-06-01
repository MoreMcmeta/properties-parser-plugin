package io.github.moremcmeta.propertiesparserplugin;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;

/**
 * Constants for both Fabric and Forge implementations of the plugin.
 * @author soir20
 */
public class ModConstants {
    public static final String MOD_ID = "moremcmeta_properties_parser_plugin";
    public static final MetadataParser PARSER = new PropertiesMetadataParser();
    public static final String EXTENSION = "properties";
}
