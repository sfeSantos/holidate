package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void coreIsANamedModuleThatOnlyRequiresJavaBase() throws URISyntaxException {
        var descriptor = coreDescriptor();

        assertEquals("io.github.sfesantos.holidays", descriptor.name());
        assertEquals(Set.of("java.base"), requiredModules(descriptor));
    }

    @Test
    void internalPackagesAreNotExported() throws URISyntaxException {
        var descriptor = coreDescriptor();

        assertTrue(descriptor.exports()
                .stream()
                .noneMatch(e -> e.source().contains(".internal")));
    }

    private static ModuleDescriptor coreDescriptor() throws URISyntaxException {
        var classes = Path.of(HolidayCalendar.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());

        return ModuleFinder.of(classes)
                .findAll()
                .iterator()
                .next()
                .descriptor();
    }

    private static Set<String> requiredModules(ModuleDescriptor descriptor) {
        return descriptor.requires()
                .stream()
                .map(ModuleDescriptor.Requires::name)
                .collect(Collectors.toSet());
    }
}
