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
import com.google.common.collect.ImmutableSet;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests the {@link PropertiesMetadataParser}.
 * @author soir20
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class PropertiesMetadataParserTest {
    private static final PropertiesMetadataParser PARSER = new PropertiesMetadataParser();
    private static final PropertiesMetadataView DUMMY_EMISSIVE_VIEW = new PropertiesMetadataView(ImmutableMap.of(
            "overlay", new PropertiesMetadataView.Value(ImmutableMap.of(
                    "texture", new PropertiesMetadataView.Value("dummy_e.png"),
                    "emissive", new PropertiesMetadataView.Value("true")
            ))
    ));
    private static final PropertiesMetadataView DUMMY_OTHER_VIEW = new PropertiesMetadataView(ImmutableMap.of(
            "other", new PropertiesMetadataView.Value(ImmutableMap.of(
                    "abcd", new PropertiesMetadataView.Value("efgh")
            ))
    ));
    private static final PropertiesMetadataView DUMMY_ANIMATION_VIEW_1 = new PropertiesMetadataView(ImmutableMap.of(
            "animation", new PropertiesMetadataView.Value(ImmutableMap.of(
                    "parts", new PropertiesMetadataView.Value(ImmutableMap.of(
                            "0", new PropertiesMetadataView.Value(ImmutableMap.of(
                                    "width", new PropertiesMetadataView.Value("0")
                            ))
                    ))
            ))
    ));
    private static final PropertiesMetadataView DUMMY_ANIMATION_VIEW_2 = new PropertiesMetadataView(ImmutableMap.of(
            "animation", new PropertiesMetadataView.Value(ImmutableMap.of(
                    "parts", new PropertiesMetadataView.Value(ImmutableMap.of(
                            "0", new PropertiesMetadataView.Value(ImmutableMap.of(
                                    "width", new PropertiesMetadataView.Value("1")
                            ))
                    ))
            ))
    ));
    private static final PropertiesMetadataView DUMMY_ANIMATION_VIEW_3 = new PropertiesMetadataView(ImmutableMap.of(
            "animation", new PropertiesMetadataView.Value(ImmutableMap.of(
                    "parts", new PropertiesMetadataView.Value(ImmutableMap.of(
                            "0", new PropertiesMetadataView.Value(ImmutableMap.of(
                                    "width", new PropertiesMetadataView.Value("2")
                            ))
                    ))
            ))
    ));

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void combine_None_EmptyView() throws InvalidMetadataException {
        MetadataView view = PARSER.combine(new ResourceLocation("dummy.png"), ImmutableMap.of());
        assertEquals(0, view.size());
    }

    @Test
    public void combine_NoAnimationsNoConflict_CombinedByMetadataLocation() throws InvalidMetadataException {
        MetadataView view = PARSER.combine(new ResourceLocation("dummy.png"), ImmutableMap.of(
                new ResourceLocation("other.png.properties"), DUMMY_OTHER_VIEW,
                new ResourceLocation("dummy.png.properties"), DUMMY_EMISSIVE_VIEW
        ));

        assertEquals(2, view.size());
        assertEquals(ImmutableList.of("overlay", "other"), ImmutableList.copyOf(view.keys()));

        MetadataView emissiveSection = view.subView("overlay").get();
        assertTrue(emissiveSection.booleanValue("emissive").get());
        assertEquals("dummy_e.png", emissiveSection.stringValue("texture").get());

        assertEquals("efgh", view.subView("other").get().stringValue("abcd").get());
    }

    @Test
    public void combine_OneAnimationNoConflict_CombinedByMetadataLocation() throws InvalidMetadataException {
        MetadataView view = PARSER.combine(new ResourceLocation("dummy.png"), ImmutableMap.of(
                new ResourceLocation("other.png.properties"), DUMMY_OTHER_VIEW,
                new ResourceLocation("dummy.png.properties"), DUMMY_EMISSIVE_VIEW,
                new ResourceLocation("z1.png.properties"), DUMMY_ANIMATION_VIEW_1
        ));

        assertEquals(3, view.size());
        assertEquals(ImmutableList.of("animation", "overlay", "other"), ImmutableList.copyOf(view.keys()));

        MetadataView emissiveSection = view.subView("overlay").get();
        assertTrue(emissiveSection.booleanValue("emissive").get());
        assertEquals("dummy_e.png", emissiveSection.stringValue("texture").get());

        assertEquals("efgh", view.subView("other").get().stringValue("abcd").get());

        assertEquals(
                0,
                (int) view.subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
                        .integerValue("width").get()
        );
    }

    @Test
    public void combine_MultipleAnimationsNoConflict_CombinedByMetadataLocation() throws InvalidMetadataException {
        MetadataView view = PARSER.combine(new ResourceLocation("dummy.png"), ImmutableMap.of(
                new ResourceLocation("other.png.properties"), DUMMY_OTHER_VIEW,
                new ResourceLocation("dummy.png.properties"), DUMMY_EMISSIVE_VIEW,
                new ResourceLocation("z1.png.properties"), DUMMY_ANIMATION_VIEW_1,
                new ResourceLocation("z3.png.properties"), DUMMY_ANIMATION_VIEW_3,
                new ResourceLocation("z2.png.properties"), DUMMY_ANIMATION_VIEW_2
        ));

        assertEquals(3, view.size());
        assertEquals(ImmutableList.of("animation", "overlay", "other"), ImmutableList.copyOf(view.keys()));

        MetadataView emissiveSection = view.subView("overlay").get();
        assertTrue(emissiveSection.booleanValue("emissive").get());
        assertEquals("dummy_e.png", emissiveSection.stringValue("texture").get());

        assertEquals("efgh", view.subView("other").get().stringValue("abcd").get());

        assertEquals(
                0,
                (int) view.subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
                        .integerValue("width").get()
        );
        assertEquals(
                2,
                (int) view.subView("animation").get()
                        .subView("parts").get()
                        .subView(1).get()
                        .integerValue("width").get()
        );
        assertEquals(
                1,
                (int) view.subView("animation").get()
                        .subView("parts").get()
                        .subView(2).get()
                        .integerValue("width").get()
        );
    }

    @Test
    public void combine_ConflictingSections_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.combine(new ResourceLocation("dummy.png"), ImmutableMap.of(
                new ResourceLocation("other.png.properties"), DUMMY_OTHER_VIEW,
                new ResourceLocation("dummy.png.properties"), DUMMY_EMISSIVE_VIEW,
                new ResourceLocation("z1.png.properties"), DUMMY_ANIMATION_VIEW_1,
                new ResourceLocation("z2.png.properties"), DUMMY_ANIMATION_VIEW_2,
                new ResourceLocation("dummy2.png.properties"), DUMMY_EMISSIVE_VIEW
        ));
    }

    @Test
    public void parse_BadPropertiesFile_InvalidMetadataException() throws InvalidMetadataException, IOException {
        InputStream badStream = IOUtils.toInputStream("dummy", StandardCharsets.UTF_8);
        badStream.close();

        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                badStream,
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png"),
                                new ResourceLocation("textures/optifine/eyes_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png"),
                                new ResourceLocation("textures/entity/bee_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png"),
                                new ResourceLocation("textures/test_e.png")
                        )
                ))
        );
    }

    @Test
    public void parse_UnknownFile_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/other.properties"),
                makePropertiesStream(
                        "bad.config=_e"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("optifine/other.properties")
                        )
                ))
        );
    }

    @Test
    public void parse_EmptyEmissiveConfig_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                makePropertiesStream(
                        ""
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png"),
                                new ResourceLocation("textures/optifine/eyes_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png"),
                                new ResourceLocation("textures/entity/bee_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png"),
                                new ResourceLocation("textures/test_e.png")
                        )
                ))
        );
    }

    @Test
    public void parse_BadEmissiveConfig_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                makePropertiesStream(
                        "bad.config=_e"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png"),
                                new ResourceLocation("textures/optifine/eyes_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png"),
                                new ResourceLocation("textures/entity/bee_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png"),
                                new ResourceLocation("textures/test_e.png")
                        )
                ))
        );
    }

    @Test
    public void parse_NoEmissiveTextures_NoneParsed() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                makePropertiesStream(
                        "suffix.emissive=_e"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png")
                        )
                ))
        );

        assertEquals(0, views.size());
    }

    @Test
    public void parse_HasEmissiveTextures_AllParsed() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                makePropertiesStream(
                        "suffix.emissive=_e"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png"),
                                new ResourceLocation("textures/optifine/eyes_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png"),
                                new ResourceLocation("textures/entity/bee_e.png"),
                                new ResourceLocation("textures/entity/dolphin_e")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png"),
                                new ResourceLocation("textures/test_e.png")
                        )
                ))
        );

        assertEquals(4, views.size());

        MetadataView view1 = views.get(new ResourceLocation("textures/optifine/eyes.png"))
                        .subView("overlay").get();
        MetadataView view2 = views.get(new ResourceLocation("moremcmeta", "textures/dummy.png"))
                .subView("overlay").get();
        MetadataView view3 = views.get(new ResourceLocation("textures/test.png"))
                .subView("overlay").get();
        MetadataView view4 = views.get(new ResourceLocation("textures/entity/bee.png"))
                .subView("overlay").get();

        assertTrue(view1.booleanValue("emissive").get());
        assertTrue(view2.booleanValue("emissive").get());
        assertTrue(view3.booleanValue("emissive").get());
        assertTrue(view4.booleanValue("emissive").get());

        assertEquals(
                new ResourceLocation("textures/optifine/eyes_e.png"),
                new ResourceLocation(view1.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                new ResourceLocation(view2.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("textures/test_e.png"),
                new ResourceLocation(view3.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("textures/entity/bee_e.png"),
                new ResourceLocation(view4.stringValue("texture").get())
        );
    }

    @Test
    public void parse_HasEmissiveTexturesAndWhitespace_AllParsed() throws InvalidMetadataException {
        Map<ResourceLocation, MetadataView> views = PARSER.parse(
                new ResourceLocation("optifine/emissive.properties"),
                makePropertiesStream(
                        "suffix.emissive = \t_e"
                ),
                new MockResourceRepository(ImmutableList.of(
                        ImmutableSet.of(
                                new ResourceLocation("textures/optifine/eyes.png"),
                                new ResourceLocation("textures/optifine/eyes_e.png")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("textures/test.png"),
                                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                                new ResourceLocation("optifine/emissive.properties"),
                                new ResourceLocation("textures/entity/witch.png"),
                                new ResourceLocation("textures/entity/witch_f.png"),
                                new ResourceLocation("textures/entity/bee_e.png"),
                                new ResourceLocation("textures/entity/dolphin_e")
                        ),
                        ImmutableSet.of(
                                new ResourceLocation("moremcmeta", "textures/dummy.png"),
                                new ResourceLocation("textures/test_e.png")
                        )
                ))
        );

        assertEquals(4, views.size());

        MetadataView view1 = views.get(new ResourceLocation("textures/optifine/eyes.png"))
                .subView("overlay").get();
        MetadataView view2 = views.get(new ResourceLocation("moremcmeta", "textures/dummy.png"))
                .subView("overlay").get();
        MetadataView view3 = views.get(new ResourceLocation("textures/test.png"))
                .subView("overlay").get();
        MetadataView view4 = views.get(new ResourceLocation("textures/entity/bee.png"))
                .subView("overlay").get();

        assertTrue(view1.booleanValue("emissive").get());
        assertTrue(view2.booleanValue("emissive").get());
        assertTrue(view3.booleanValue("emissive").get());
        assertTrue(view4.booleanValue("emissive").get());

        assertEquals(
                new ResourceLocation("textures/optifine/eyes_e.png"),
                new ResourceLocation(view1.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("moremcmeta", "textures/dummy_e.png"),
                new ResourceLocation(view2.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("textures/test_e.png"),
                new ResourceLocation(view3.stringValue("texture").get())
        );
        assertEquals(
                new ResourceLocation("textures/entity/bee_e.png"),
                new ResourceLocation(view4.stringValue("texture").get())
        );
    }

    @Test
    public void parse_UsesAllAnimationProperties_AllParsed() throws InvalidMetadataException {
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
                .subView("animation").get()
                .subView("parts").get()
                .subView(0).get();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").get());
        assertEquals(20, (int) animationView.integerValue("y").get());
        assertEquals(10, (int) animationView.integerValue("width").get());
        assertEquals(30, (int) animationView.integerValue("height").get());
        assertEquals(5, (int) animationView.integerValue("skip").get());
        assertTrue(animationView.booleanValue("interpolate").get());
        assertTrue(animationView.booleanValue("smoothAlpha").get());

        MetadataView framesView = animationView.subView("frames").get();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).get()
                        .integerValue("index").get()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).get()
                        .integerValue("index").get()
        );
        assertEquals(
                1,
                (int) framesView.subView(3).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).get()
                        .integerValue("index").get()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).get()
                        .integerValue("time").get()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).get()
                        .integerValue("time").get()
        );
        assertEquals(
                15,
                (int) framesView.subView(2).get()
                        .integerValue("time").get()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).get()
                        .integerValue("time").get()
        );
        assertEquals(
                20,
                (int) framesView.subView(4).get()
                        .integerValue("time").get()
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
                .subView("animation").get()
                .subView("parts").get()
                .subView(0).get();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").get());
        assertEquals(20, (int) animationView.integerValue("y").get());
        assertEquals(10, (int) animationView.integerValue("width").get());
        assertEquals(30, (int) animationView.integerValue("height").get());

        MetadataView framesView = animationView.subView("frames").get();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).get()
                        .integerValue("index").get()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).get()
                        .integerValue("index").get()
        );
        assertEquals(
                1,
                (int) framesView.subView(3).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).get()
                        .integerValue("index").get()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).get()
                        .integerValue("time").get()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).get()
                        .integerValue("time").get()
        );
        assertEquals(
                15,
                (int) framesView.subView(2).get()
                        .integerValue("time").get()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).get()
                        .integerValue("time").get()
        );
        assertEquals(
                20,
                (int) framesView.subView(4).get()
                        .integerValue("time").get()
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
                .subView("animation").get()
                .subView("parts").get()
                .subView(0).get();

        assertEquals(
                ImmutableSet.of(new ResourceLocation("textures/entity/creeper.png")),
                views.keySet()
        );
        assertTrue(animationView.byteStreamValue("texture").isPresent());
        assertEquals(0, (int) animationView.integerValue("x").get());
        assertEquals(20, (int) animationView.integerValue("y").get());
        assertEquals(10, (int) animationView.integerValue("width").get());
        assertEquals(30, (int) animationView.integerValue("height").get());

        MetadataView framesView = animationView.subView("frames").get();
        assertEquals(5, framesView.size());

        assertEquals(
                0,
                (int) framesView.subView(0).get()
                        .integerValue("index").get()
        );
        assertEquals(
                3,
                (int) framesView.subView(1).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(2).get()
                        .integerValue("index").get()
        );
        assertEquals(
                3,
                (int) framesView.subView(3).get()
                        .integerValue("index").get()
        );
        assertEquals(
                2,
                (int) framesView.subView(4).get()
                        .integerValue("index").get()
        );

        assertEquals(
                5,
                (int) framesView.subView(0).get()
                        .integerValue("time").get()
        );
        assertEquals(
                10,
                (int) framesView.subView(1).get()
                        .integerValue("time").get()
        );
        assertFalse(
                framesView.subView(2).get()
                        .integerValue("time").isPresent()
        );
        assertEquals(
                5,
                (int) framesView.subView(3).get()
                        .integerValue("time").get()
        );
        assertFalse(
                framesView.subView(4).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
                        .subView("animation").get()
                        .subView("parts").get()
                        .subView(0).get()
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
    public void parse_EmptyAnimationConfig_InvalidMetadataException() throws InvalidMetadataException {
        expectedException.expect(InvalidMetadataException.class);
        PARSER.parse(
                new ResourceLocation("optifine/anim/creepereyes.properties"),
                makePropertiesStream(
                        ""
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