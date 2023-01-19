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

import eu.openanalytics.phaedra.chartingservice.dto.ChartTemplateDTO;
import eu.openanalytics.phaedra.chartingservice.dto.SettingDTO;
import eu.openanalytics.phaedra.chartingservice.enumeration.SettingType;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChartTemplateServiceTest {

    @Autowired
    private ChartTemplateService chartTemplateService;

    @Container
    private static JdbcDatabaseContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/postgres:13-alpine")
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
        assertThat(chartTemplateService).isNotNull();
    }

    @Test
    public void createChartTemplate() {
        ChartTemplateDTO chartTemplate = new ChartTemplateDTO();
        chartTemplate.setAxisX("x");
        chartTemplate.setAxisY("y");
        chartTemplate.setType("bar");
        chartTemplate.setGroupBy("group");
        chartTemplate.setFilter("filter");
        //Axis settings
        SettingDTO setting = new SettingDTO();
        setting.setName("size");
        setting.setValue("1000");
        setting.setSettingType(SettingType.AXIS);
        List<SettingDTO> axisSettings = new ArrayList<>();
        axisSettings.add(setting);
        chartTemplate.setAxisSettings(axisSettings);
        //Chart settings
        SettingDTO setting2 = new SettingDTO();
        setting2.setName("size");
        setting2.setValue("1000");
        setting2.setSettingType(SettingType.CHART);
        List<SettingDTO> chartSettings = new ArrayList<>();
        chartSettings.add(setting2);
        chartTemplate.setChartSettings(chartSettings);
        //Create chart template
        ChartTemplateDTO chartTemplateDTO = chartTemplateService.createChartTemplate(chartTemplate);
        assertThat(chartTemplateDTO.getId()).isNotNull();
        assertThat(chartTemplateDTO.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTO.getGroupBy()).isEqualTo("group");
        assertThat(chartTemplateDTO.getFilter()).isEqualTo("filter");
        assertThat(chartTemplateDTO.getAxisSettings().size()).isEqualTo(1);
        assertThat(chartTemplateDTO.getAxisSettings().get(0).getName()).isEqualTo("size");
        assertThat(chartTemplateDTO.getAxisSettings().get(0).getValue()).isEqualTo("1000");
        assertThat(chartTemplateDTO.getAxisSettings().get(0).getSettingType()).isEqualTo(SettingType.AXIS);
        assertThat(chartTemplateDTO.getChartSettings().size()).isEqualTo(1);
        assertThat(chartTemplateDTO.getChartSettings().get(0).getName()).isEqualTo("size");
        assertThat(chartTemplateDTO.getChartSettings().get(0).getValue()).isEqualTo("1000");
        assertThat(chartTemplateDTO.getChartSettings().get(0).getSettingType()).isEqualTo(SettingType.CHART);
    }

    @Test
    public void getAndUpdateChartTemplate() {
        //Get chart template
        ChartTemplateDTO chartTemplateDTO = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTO.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTO.getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTO.getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTO.getGroupBy()).isEqualTo("wellType");
        assertThat(chartTemplateDTO.getFilter()).isEqualTo("plateId = 2000");
        assertThat(chartTemplateDTO.getAxisSettings().size()).isEqualTo(2);
        SettingDTO setting = chartTemplateDTO.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null);
        assertThat(setting.getName()).isEqualTo("size");
        //Update chart template
        chartTemplateDTO.setAxisX("x");
        chartTemplateDTO.setAxisY("y");
        chartTemplateDTO.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).setName("size2");
        //Update chart template
        ChartTemplateDTO chartTemplateDTO2 = chartTemplateService.updateChartTemplate(chartTemplateDTO);
        //Assert values
        assertThat(chartTemplateDTO2.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO2.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO2.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).getName()).isEqualTo("size2");
        //Get chart template
        ChartTemplateDTO chartTemplateDTO3 = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO3.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO3.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO3.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).getName()).isEqualTo("size2");
    }

    @Test
    public void updateChartTemplateWithNewSettings() {
        //Get chart template
        ChartTemplateDTO chartTemplateDTO = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTO.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTO.getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTO.getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTO.getGroupBy()).isEqualTo("wellType");
        assertThat(chartTemplateDTO.getFilter()).isEqualTo("plateId = 2000");
        assertThat(chartTemplateDTO.getAxisSettings().size()).isEqualTo(2);
        SettingDTO setting = chartTemplateDTO.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null);
        assertThat(setting.getName()).isEqualTo("size");
        //Update chart template
        chartTemplateDTO.setAxisX("x");
        chartTemplateDTO.setAxisY("y");
        chartTemplateDTO.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).setName("size2");
        //Add new setting
        SettingDTO setting2 = new SettingDTO();
        setting2.setName("size3");
        setting2.setValue("1000");
        setting2.setSettingType(SettingType.AXIS);
        chartTemplateDTO.getAxisSettings().add(setting2);
        //Update chart template
        ChartTemplateDTO chartTemplateDTO2 = chartTemplateService.updateChartTemplate(chartTemplateDTO);
        //Assert values
        assertThat(chartTemplateDTO2.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO2.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO2.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).getName()).isEqualTo("size2");
        assertThat(chartTemplateDTO2.getAxisSettings().stream().filter(s -> s.getId() == 1L).findFirst().orElse(null).getName()).isEqualTo("size3");
        //Get chart template
        ChartTemplateDTO chartTemplateDTO3 = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO3.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO3.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO3.getAxisSettings().stream().filter(s -> s.getId() == 1000L).findFirst().orElse(null).getName()).isEqualTo("size2");
        assertThat(chartTemplateDTO3.getAxisSettings().stream().filter(s -> s.getId() == 1L).findFirst().orElse(null).getName()).isEqualTo("size3");
    }

    @Test
    public void updateChartTemplateDeleteSetting() {
        //Get chart template
        ChartTemplateDTO chartTemplateDTO = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTO.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTO.getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTO.getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTO.getGroupBy()).isEqualTo("wellType");
        assertThat(chartTemplateDTO.getFilter()).isEqualTo("plateId = 2000");
        assertThat(chartTemplateDTO.getAxisSettings().size()).isEqualTo(2);
        //Update chart template
        chartTemplateDTO.setAxisX("x");
        chartTemplateDTO.setAxisY("y");
        //Delete setting
        chartTemplateDTO.getAxisSettings().remove(0);
        assertThat(chartTemplateDTO.getAxisSettings().get(0).getId()).isEqualTo(2000L);
        //Update chart template
        ChartTemplateDTO chartTemplateDTO2 = chartTemplateService.updateChartTemplate(chartTemplateDTO);
        //Assert values
        assertThat(chartTemplateDTO2.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO2.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO2.getAxisSettings().size()).isEqualTo(1);
        assertThat(chartTemplateDTO2.getAxisSettings().get(0).getId()).isEqualTo(2000L);
        //Get chart template
        ChartTemplateDTO chartTemplateDTO3 = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO3.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTO3.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTO3.getAxisSettings().size()).isEqualTo(1);
        assertThat(chartTemplateDTO3.getAxisSettings().get(0).getId()).isEqualTo(2000L);
    }

    @Test
    public void deleteChartTemplate() {
        //Get chart template
        ChartTemplateDTO chartTemplateDTO = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTO.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTO.getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTO.getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTO.getGroupBy()).isEqualTo("wellType");
        assertThat(chartTemplateDTO.getFilter()).isEqualTo("plateId = 2000");
        assertThat(chartTemplateDTO.getAxisSettings().size()).isEqualTo(2);
        //Delete chart template
        chartTemplateService.deleteChartTemplate(1000L);
        //Get chart template
        ChartTemplateDTO chartTemplateDTO2 = chartTemplateService.getChartTemplateById(1000L);
        //Assert values
        assertThat(chartTemplateDTO2).isNull();
    }

    @Test
    public void getAllChartTemplates() {
        //Get all chart templates
        List<ChartTemplateDTO> chartTemplateDTOs = chartTemplateService.getAllChartTemplates();
        //Assert values
        assertThat(chartTemplateDTOs.size()).isEqualTo(2);
        assertThat(chartTemplateDTOs.get(0).getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTOs.get(0).getType()).isEqualTo("bar");
        assertThat(chartTemplateDTOs.get(0).getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTOs.get(0).getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTOs.get(1).getId()).isEqualTo(2000L);
        assertThat(chartTemplateDTOs.get(1).getType()).isEqualTo("bar");
        assertThat(chartTemplateDTOs.get(1).getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTOs.get(1).getAxisY()).isEqualTo("column");
    }

}
