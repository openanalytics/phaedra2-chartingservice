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
package eu.openanalytics.phaedra.chartingservice.service;

import eu.openanalytics.phaedra.chartingservice.dto.SettingDTO;
import eu.openanalytics.phaedra.chartingservice.enumeration.SettingType;
import eu.openanalytics.phaedra.chartingservice.support.Containers;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class SettingServiceTest {
    @Autowired
    private SettingService settingService;

    @Container
    private static JdbcDatabaseContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("registry.openanalytics.eu/library/postgres:13-alpine")
            .asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE))
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
        assertThat(settingService).isNotNull();
    }

    @Test
    public void createSetting() {
        SettingDTO setting = new SettingDTO();
        setting.setChartTemplateId(1000L);
        setting.setName("size");
        setting.setValue("1000");
        setting.setSettingType(SettingType.AXIS);
        SettingDTO settingDTO = settingService.createSetting(setting);
        assertThat(settingDTO.getId()).isNotNull();
    }

    @Test
    public void createSettings() {
        List<SettingDTO> settings = new ArrayList<>();
        SettingDTO setting = new SettingDTO();
        setting.setName("size");
        setting.setValue("1000");
        settings.add(setting);
        SettingDTO setting2 = new SettingDTO();
        setting2.setName("size");
        setting2.setValue("1000");
        settings.add(setting2);
        settings = settingService.createSettings(settings, 1000L, SettingType.AXIS);
        assertThat(settings.get(0).getId()).isNotNull();
        assertThat(settings.get(1).getId()).isNotNull();

        List<SettingDTO> settingsFromDb = settingService.getSettingsByChartTemplateId(1000L, SettingType.AXIS);
        assertThat(settingsFromDb.size()).isEqualTo(4);
    }

    @Test
    public void createSettingsChartTemplateIdNotFound() {
        List<SettingDTO> settings = new ArrayList<>();
        SettingDTO setting = new SettingDTO();
        setting.setName("size");
        setting.setValue("1000");
        settings.add(setting);
        // If charttemplate id is not found, an illegal argument exception is thrown
        assertThatThrownBy(() -> settingService.createSettings(settings, 1111L, SettingType.AXIS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updateSetting() {
        SettingDTO setting = settingService.getSettingById(1000L);
        assertThat(setting.getValue()).isEqualTo("10");
        setting.setValue("20");
        settingService.updateSetting(setting);
        SettingDTO settingFromDb = settingService.getSettingById(1000L);
        assertThat(settingFromDb.getValue()).isEqualTo("20");
    }

    @Test
    public void updateSettings() {
        SettingDTO setting = settingService.getSettingById(1000L);
        assertThat(setting.getValue()).isEqualTo("10");
        setting.setValue("20");
        SettingDTO setting2 = settingService.getSettingById(2000L);
        assertThat(setting2.getValue()).isEqualTo("20");
        setting2.setValue("30");
        List<SettingDTO> settings = new ArrayList<>();
        settings.add(setting);
        settings.add(setting2);
        settingService.updateSettings(settings, 1000L);
        SettingDTO settingFromDb = settingService.getSettingById(1000L);
        assertThat(settingFromDb.getValue()).isEqualTo("20");
        SettingDTO setting2FromDb = settingService.getSettingById(2000L);
        assertThat(setting2FromDb.getValue()).isEqualTo("30");
    }

    @Test
    public void getSettingsByChartTemplateId() {
        List<SettingDTO> settings = settingService.getSettingsByChartTemplateId(1000L, SettingType.AXIS);
        assertThat(settings.size()).isEqualTo(2);
        List<SettingDTO> settings2 = settingService.getSettingsByChartTemplateId(1000L, SettingType.CHART);
        assertThat(settings2.size()).isEqualTo(2);
        List<SettingDTO> settings3 = settingService.getSettingsByChartTemplateId(1001L, SettingType.AXIS);
        assertThat(settings3.size()).isEqualTo(0);
    }

    @Test
    public void deleteSettingsByChartTemplateId() {
        settingService.deleteSettingsByChartTemplateId(1000L);
        List<SettingDTO> settings = settingService.getSettingsByChartTemplateId(1000L, SettingType.AXIS);
        assertThat(settings.size()).isEqualTo(0);
    }

}
