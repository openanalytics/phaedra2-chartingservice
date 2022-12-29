/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
package eu.openanalytics.phaedra.chartingservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.chartingservice.dto.ChartTemplateDTO;
import eu.openanalytics.phaedra.chartingservice.dto.SettingDTO;
import eu.openanalytics.phaedra.chartingservice.enumeration.SettingType;
import eu.openanalytics.phaedra.chartingservice.model.ChartTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import support.Containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChartTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    public void chartTemplatePostTest() throws Exception {
        ChartTemplateDTO newChartTemplate = new ChartTemplateDTO();
        newChartTemplate.setType("test");
        newChartTemplate.setAxisX("x");
        newChartTemplate.setAxisY("y");
        newChartTemplate.setGroupBy("group");
        newChartTemplate.setFilter("filter");
        SettingDTO setting = new SettingDTO();
        setting.setName("axis_setting");
        setting.setValue("axis_value");
        setting.setSettingType(SettingType.AXIS);
        newChartTemplate.setAxisSettings(List.of(setting));
        SettingDTO setting2 = new SettingDTO();
        setting2.setName("chart_setting");
        setting2.setValue("chart_value");
        setting2.setSettingType(SettingType.CHART);
        newChartTemplate.setChartSettings(List.of(setting2));
        //Post
        String requestBody = objectMapper.writeValueAsString(newChartTemplate);
        MvcResult result = this.mockMvc.perform(post("/chart-template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ChartTemplateDTO chartTemplateDTOResult = objectMapper.readValue(result.getResponse().getContentAsString(), ChartTemplateDTO.class);
        assertThat(chartTemplateDTOResult.getId()).isNotNull();
        assertThat(chartTemplateDTOResult.getType()).isEqualTo("test");
        assertThat(chartTemplateDTOResult.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTOResult.getAxisY()).isEqualTo("y");
        assertThat(chartTemplateDTOResult.getGroupBy()).isEqualTo("group");
        assertThat(chartTemplateDTOResult.getFilter()).isEqualTo("filter");
        assertThat(chartTemplateDTOResult.getAxisSettings()).hasSize(1);
        assertThat(chartTemplateDTOResult.getAxisSettings().get(0).getName()).isEqualTo("axis_setting");
        assertThat(chartTemplateDTOResult.getAxisSettings().get(0).getValue()).isEqualTo("axis_value");
        assertThat(chartTemplateDTOResult.getChartSettings()).hasSize(1);
        assertThat(chartTemplateDTOResult.getChartSettings().get(0).getName()).isEqualTo("chart_setting");
        assertThat(chartTemplateDTOResult.getChartSettings().get(0).getValue()).isEqualTo("chart_value");
    }

    @Test
    public void chartTemplatePutAndGetTest() throws Exception {
        //Get chartTemplate with id 1000
        MvcResult result = this.mockMvc.perform(get("/chart-template/1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ChartTemplateDTO chartTemplateDTOResult = objectMapper.readValue(result.getResponse().getContentAsString(), ChartTemplateDTO.class);
        assertThat(chartTemplateDTOResult.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTOResult.getType()).isEqualTo("bar");
        assertThat(chartTemplateDTOResult.getAxisX()).isEqualTo("row");
        assertThat(chartTemplateDTOResult.getAxisY()).isEqualTo("column");
        assertThat(chartTemplateDTOResult.getGroupBy()).isEqualTo("wellType");
        assertThat(chartTemplateDTOResult.getFilter()).isEqualTo("plateId = 2000");
        assertThat(chartTemplateDTOResult.getAxisSettings()).hasSize(2);
        //Update chartTemplate
        chartTemplateDTOResult.setAxisX("x");
        chartTemplateDTOResult.setAxisY("y");
        //Put
        String requestBody = objectMapper.writeValueAsString(chartTemplateDTOResult);
        this.mockMvc.perform(put("/chart-template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
        //Get chartTemplate with id 1000
        result = this.mockMvc.perform(get("/chart-template/1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        chartTemplateDTOResult = objectMapper.readValue(result.getResponse().getContentAsString(), ChartTemplateDTO.class);
        assertThat(chartTemplateDTOResult.getId()).isEqualTo(1000L);
        assertThat(chartTemplateDTOResult.getAxisX()).isEqualTo("x");
        assertThat(chartTemplateDTOResult.getAxisY()).isEqualTo("y");
    }

    @Test
    public void chartTemplateDeleteTest() throws Exception {
        //Get chartTemplate with id 1000
        MvcResult result = this.mockMvc.perform(get("/chart-template/1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        ChartTemplateDTO chartTemplateDTOResult = objectMapper.readValue(result.getResponse().getContentAsString(), ChartTemplateDTO.class);
        assertThat(chartTemplateDTOResult.getId()).isEqualTo(1000L);
        //Delete chartTemplate with id 1000
        this.mockMvc.perform(delete("/chart-template/1000"))
                .andDo(print())
                .andExpect(status().isOk());
        //Get chartTemplate with id 1000
        this.mockMvc.perform(get("/chart-template/1000"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void chartTemplateGetAllTest() throws Exception {
        //Get all chartTemplates
        MvcResult result = this.mockMvc.perform(get("/chart-template"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<ChartTemplateDTO> chartTemplateDTOResult = objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
        assertThat(chartTemplateDTOResult).hasSize(2);
    }
}
