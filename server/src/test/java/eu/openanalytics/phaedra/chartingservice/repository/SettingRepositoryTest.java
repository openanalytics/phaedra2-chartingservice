/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.chartingservice.repository;

import eu.openanalytics.phaedra.chartingservice.enumeration.SettingType;
import eu.openanalytics.phaedra.chartingservice.model.Setting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import support.Containers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class SettingRepositoryTest {
    @Autowired
    private SettingRepository settingRepository;

    @Container
    private static JdbcDatabaseContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/postgres:13-alpine").asCompatibleSubstituteFor("postgres:13-alpine"))
            .withDatabaseName("phaedra2")
            .withUrlParam("currentSchema","plates")
            .withPassword("inmemory")
            .withUsername("inmemory");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    public void contextLoads() {
        assertThat(settingRepository).isNotNull();
    }

    @Test
    public void getSetting() {
        Optional<Setting> setting = settingRepository.findById(1000L);
        assertThat(setting.isPresent()).isTrue();
        assertThat(setting.get().getId()).isEqualTo(1000L);
        assertThat(setting.get().getChartTemplateId()).isEqualTo(1000L);
        assertThat(setting.get().getName()).isEqualTo("size");
        assertThat(setting.get().getValue()).isEqualTo("10");
    }

    @Test
    public void getSettingsByChartTemplateId() {
        assertThat(settingRepository.findByChartTemplateId(1000L).size()).isEqualTo(4);
    }

    @Test
    public void getSettingsByChartTemplateIdAndSettingType() {
        assertThat(settingRepository.findByChartTemplateIdAndSettingType(1000L, SettingType.AXIS).size()).isEqualTo(2);
        assertThat(settingRepository.findByChartTemplateIdAndSettingType(1000L, SettingType.CHART).size()).isEqualTo(2);
    }
}
