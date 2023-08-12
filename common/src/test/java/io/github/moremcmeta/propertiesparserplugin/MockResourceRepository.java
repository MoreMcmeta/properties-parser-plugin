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

import io.github.moremcmeta.moremcmeta.api.client.metadata.ResourceRepository;
import io.github.moremcmeta.moremcmeta.api.client.metadata.RootResourceName;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A mock {@link ResourceRepository} for testing.
 * @author soir20
 */
public final class MockResourceRepository implements ResourceRepository {
    private final List<Set<ResourceLocation>> PACKS;
    private final InputStream DUMMY_STREAM;
    private final boolean BAD_PACKS;

    public MockResourceRepository(List<Set<ResourceLocation>> packs) {
        this(packs, false, new ByteArrayInputStream("dummy".getBytes()));
    }

    public MockResourceRepository(List<Set<ResourceLocation>> packs, InputStream dummyStream) {
        this(packs, false, dummyStream);
    }

    public MockResourceRepository(List<Set<ResourceLocation>> packs, boolean badPacks) {
        this(packs, badPacks, new ByteArrayInputStream("dummy".getBytes()));
    }

    @Override
    public Optional<Pack> highestPackWith(ResourceLocation location) {
        return PACKS.stream().filter((pack) -> pack.contains(location))
                .findFirst()
                .map(this::makeMockPack);
    }

    @Override
    public Optional<Pack> highestPackWith(ResourceLocation location, ResourceLocation floor) {
        for (Set<ResourceLocation> pack : PACKS) {
            if (pack.contains(location)) {
                return Optional.of(makeMockPack(pack));
            }

            if (pack.contains(floor)) {
                break;
            }
        }

        return Optional.empty();
    }

    @Override
    public Set<? extends ResourceLocation> list(Predicate<String> fileFilter) {
        return PACKS.stream()
                .flatMap(Set::stream)
                .filter((location) -> fileFilter.test(location.getPath()))
                .collect(Collectors.toSet());
    }

    private MockResourceRepository(List<Set<ResourceLocation>> packs, boolean badPacks, InputStream dummyStream) {
        PACKS = packs;
        DUMMY_STREAM = dummyStream;
        BAD_PACKS = badPacks;
    }

    private Pack makeMockPack(Set<ResourceLocation> pack) {
        return new Pack() {
            @Override
            public Optional<InputStream> resource(ResourceLocation location) {
                if (pack.contains(location) && !BAD_PACKS) {
                    return Optional.of(DUMMY_STREAM);
                }

                return Optional.empty();
            }

            @Override
            public ResourceLocation locateRootResource(RootResourceName rootResourceName) {
                return new ResourceLocation("root/" + rootResourceName.toString());
            }
        };
    }
}
