package io.github.sfesantos.holidays.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class JdbcModuleTest {

    @Test
    void requiresOnlyTheCoreAndJavaSql() throws URISyntaxException {
        var classes = Path.of(JdbcDataProvider.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        var descriptor = ModuleFinder.of(classes)
                .findAll()
                .iterator()
                .next()
                .descriptor();
        var required = descriptor.requires()
                .stream()
                .map(ModuleDescriptor.Requires::name)
                .collect(Collectors.toSet());

        assertEquals("io.github.sfesantos.holidays.jdbc", descriptor.name());
        assertEquals(Set.of("java.base", "java.sql", "io.github.sfesantos.holidays"), required);
    }
}
