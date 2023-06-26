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

import eu.openanalytics.phaedra.chartingservice.model.ChartTemplate;
import eu.openanalytics.phaedra.chartingservice.support.Containers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
//@Sql({"/jdbc/initial.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChartTemplateRepositoryTest {

    @Autowired
    private ChartTemplateRepository chartTemplateRepository;
    @Container
    private static JdbcDatabaseContainer postgresSQLContaioner = new PostgreSQLContainer(DockerImageName.parse("registry.openanalytics.eu/library/postgres:13-alpine")
            .asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE))
            .withDatabaseName("phaedra2")
            .withUrlParam("currentSchema", "charting")
            .withPassword("phaedra2")
            .withUsername("phaedra2");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    public void contextLoads() {
        assertThat(chartTemplateRepository).isNotNull();
    }

    @Test
    public void getAllChartTemplates() {
        ChartTemplate chartTemplate = new ChartTemplate();
        chartTemplate.setType("scatter");
        chartTemplate.setAxisY("Feature value");
        chartTemplate.setAxisX("Volume (mL)");
        chartTemplateRepository.save(chartTemplate);

        List<ChartTemplate> allChartTemplates = (List<ChartTemplate>) chartTemplateRepository.findAll();
        assertThat(allChartTemplates).isNotEmpty();
    }

    @Test
    public void createChartTemplate() {
        ChartTemplate chartTemplate = new ChartTemplate();
        chartTemplate.setType("scatter");
        chartTemplate.setAxisY("Feature value");
        chartTemplate.setAxisX("Volume (mL)");

        ChartTemplate result = chartTemplateRepository.save(chartTemplate);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getType()).isEqualTo(chartTemplate.getType());
        assertThat(result.getAxisX()).isEqualTo(chartTemplate.getAxisX());
        assertThat(result.getAxisY()).isEqualTo(chartTemplate.getAxisY());
    }
}
