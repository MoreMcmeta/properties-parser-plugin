package io.github.moremcmeta.propertiesparserplugin;

import io.github.moremcmeta.moremcmeta.api.client.metadata.ResourceRepository;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A mock {@link ResourceRepository} for testing.
 * @author soir20
 */
public class MockResourceRepository implements ResourceRepository {
    private final List<Set<ResourceLocation>> PACKS;
    private final boolean BAD_PACKS;

    public MockResourceRepository(List<Set<ResourceLocation>> packs) {
        this(packs, false);
    }

    public MockResourceRepository(List<Set<ResourceLocation>> packs, boolean badPacks) {
        PACKS = packs;
        BAD_PACKS = badPacks;
    }

    @Override
    public Optional<Pack> highestPackWith(ResourceLocation location) {
        return PACKS.stream().filter((pack) -> pack.contains(location))
                .findFirst()
                .map((pack) -> (l) -> {
                    if (pack.contains(l) && !BAD_PACKS) {
                        return Optional.of(new ByteArrayInputStream("dummy".getBytes()));
                    }

                    return Optional.empty();
                });
    }

    @Override
    public Set<? extends ResourceLocation> list(Predicate<String> fileFilter) {
        return PACKS.stream()
                .flatMap(Set::stream)
                .filter((location) -> fileFilter.test(location.getPath()))
                .collect(Collectors.toSet());
    }
}
