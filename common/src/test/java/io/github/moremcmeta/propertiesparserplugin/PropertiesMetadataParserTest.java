package io.github.moremcmeta.propertiesparserplugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests the {@link PropertiesMetadataParser}.
 * @author soir20
 */
public class PropertiesMetadataParserTest {
    private static final PropertiesMetadataParser PARSER = new PropertiesMetadataParser();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parse_UsesAllProperties_AllParsed() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "y=20",
                        "h=30",
                        "x=0",
                        "w=10",
                        "tile.0=0",
                        "tile.4=2",
                        "tile.3=1",
                        "tile.1=3",
                        "tile.2=2",
                        "duration.0=5",
                        "duration.2=15",
                        "duration.1=10",
                        "duration.4=20",
                        "duration.3=5",
                        "skip=5",
                        "interpolate=true",
                        "smoothAlpha=true"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        MetadataView animationView = views.get(new ResourceLocation("textures/entity/creeper.png"))
                .subView("animation").orElseThrow()
                .subView("parts").orElseThrow()
                .subView(0).orElseThrow();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").orElseThrow());
        assertEquals(20, (int) animationView.integerValue("y").orElseThrow());
        assertEquals(10, (int) animationView.integerValue("width").orElseThrow());
        assertEquals(30, (int) animationView.integerValue("height").orElseThrow());
        assertEquals(5, (int) animationView.integerValue("skip").orElseThrow());
        assertTrue(animationView.booleanValue("interpolate").orElseThrow());
        assertTrue(animationView.booleanValue("smoothAlpha").orElseThrow());

        MetadataView framesView = animationView.subView("frames").orElseThrow();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                1,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).orElseThrow()
                        .integerValue("index").orElseThrow()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                15,
                (int) framesView.subView(2).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                20,
                (int) framesView.subView(4).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
    }

    @Test
    public void parse_ExtraProperties_RenamedPropertiesNotOverridden() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "y=20",
                        "h=30",
                        "x=0",
                        "w=10",
                        "width=27",
                        "tile.0=0",
                        "tile.4=2",
                        "tile.3=1",
                        "tile.1=3",
                        "tile.2=2",
                        "duration.0=5",
                        "duration.2=15",
                        "duration.1=10",
                        "duration.4=20",
                        "duration.3=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        MetadataView animationView = views.get(new ResourceLocation("textures/entity/creeper.png"))
                .subView("animation").orElseThrow()
                .subView("parts").orElseThrow()
                .subView(0).orElseThrow();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").orElseThrow());
        assertEquals(20, (int) animationView.integerValue("y").orElseThrow());
        assertEquals(10, (int) animationView.integerValue("width").orElseThrow());
        assertEquals(30, (int) animationView.integerValue("height").orElseThrow());

        MetadataView framesView = animationView.subView("frames").orElseThrow();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                1,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).orElseThrow()
                        .integerValue("index").orElseThrow()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                15,
                (int) framesView.subView(2).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                20,
                (int) framesView.subView(4).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
    }

    @Test
    public void parse_NonConsecutiveFrameKeys_AllParsed() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "y=20",
                        "h=30",
                        "x=0",
                        "w=10",
                        "tile.0=0",
                        "tile.4=2",
                        "tile.1=3",
                        "tile.2=2",
                        "duration.0=5",
                        "duration.1=10",
                        "duration.3=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        MetadataView animationView = views.get(new ResourceLocation("textures/entity/creeper.png"))
                .subView("animation").orElseThrow()
                .subView("parts").orElseThrow()
                .subView(0).orElseThrow();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").orElseThrow());
        assertEquals(20, (int) animationView.integerValue("y").orElseThrow());
        assertEquals(10, (int) animationView.integerValue("width").orElseThrow());
        assertEquals(30, (int) animationView.integerValue("height").orElseThrow());

        MetadataView framesView = animationView.subView("frames").orElseThrow();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                3,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("index").orElseThrow()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).orElseThrow()
                        .integerValue("index").orElseThrow()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertFalse(
                framesView.subView(2).orElseThrow()
                        .integerValue("time").isPresent()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).orElseThrow()
                        .integerValue("time").orElseThrow()
        );
        assertFalse(
                framesView.subView(4).orElseThrow()
                        .integerValue("time").isPresent()
        );
    }

    @Test
    public void parse_OnlyBaseTexture_NoException() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "to=textures/entity/creeper.png"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertEquals(
                1,
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .size()
        );
    }

    @Test
    public void parse_CustomNamespace_ExpandedCorrectly() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=moremcmeta:optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("moremcmeta", "optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_EmptyNamespace_DefaultsToMinecraft() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=:optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_OnlyNamespaceCharacter_DefaultsToEmptyPath() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=:",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation(""),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_HomeCharacter_ExpandedCorrectly() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=~/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_ThisDirectoryReference_ExpandedCorrectly() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=./eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_AnimationTextureInPackBelow_NoException() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("optifine/anim/eyes.png")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_AnimationTextureInPackAbove_NoException() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("optifine/anim/eyes.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertTrue(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .byteStreamValue("texture").isPresent()
        );
    }

    @Test
    public void parse_MissingAnimationTexturePath_NoException() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertFalse(
                views.get(new ResourceLocation("textures/entity/creeper.png"))
                        .subView("animation").orElseThrow()
                        .subView("parts").orElseThrow()
                        .subView(0).orElseThrow()
                        .hasKey("texture")
        );
    }

    @Test
    public void parse_BaseUsesParentReference_NoException() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=../textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );

        assertEquals(
                ImmutableSet.of(new ResourceLocation("../textures/entity/creeper.png")),
                views.keySet()
        );
    }

    @Test
    public void parse_MissingBaseTexture_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );
    }

    @Test
    public void parse_UsesBackslash_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures\\\\entity\\\\creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );
    }

    @Test
    public void parse_BadAnimationTexturePath_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/%eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );
    }

    @Test
    public void parse_MissingAnimationTextureInRepository_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes2.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ))
        );
    }

    @Test
    public void parse_MissingAnimationTextureInPack_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        "from=optifine/anim/eyes.png",
                        "to=textures/entity/creeper.png",
                        "x=0",
                        "y=0",
                        "w=10",
                        "h=10",
                        "tile.0=0",
                        "tile.1=1",
                        "tile.2=2",
                        "tile.3=3",
                        "tile.4=4",
                        "duration.0=5",
                        "duration.1=5",
                        "duration.2=5",
                        "duration.3=5",
                        "duration.4=5"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/entity/creeper.png"),
                                new ResourceLocation("optifine/anim/eyes.png"),
                                new ResourceLocation("optifine/anim/creepereyes.properties")
                        )
                ), true)
        );
    }

    private static InputStream makePropertiesStream(String... lines) {
        String properties = String.join("\n", lines);
        return new ByteArrayInputStream(properties.getBytes());
    }

}