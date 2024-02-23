/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
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
import eu.openanalytics.phaedra.chartingservice.model.ChartTemplate;
import eu.openanalytics.phaedra.chartingservice.repository.ChartTemplateRepository;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChartTemplateService {
    private final ModelMapper modelMapper = new ModelMapper();

    private final ChartTemplateRepository chartTemplateRepository;
    private final SettingService settingService;

    public ChartTemplateService(ChartTemplateRepository chartTemplateRepository, SettingService settingService) {
        this.chartTemplateRepository = chartTemplateRepository;
        this.settingService = settingService;

        Configuration builderConfiguration = modelMapper.getConfiguration().copy()
                .setDestinationNameTransformer(NameTransformers.builder())
                .setDestinationNamingConvention(NamingConventions.builder());
        modelMapper.createTypeMap(ChartTemplate.class, ChartTemplateDTO.class, builderConfiguration)
                .setPropertyCondition(Conditions.isNotNull());
    }

    public ChartTemplateDTO createChartTemplate(ChartTemplateDTO chartTemplateDTO) {
        //Hard copy setting lists
        List<SettingDTO> settingDTOAxis = new ArrayList<>();
        List<SettingDTO> settingDTOChart = new ArrayList<>();
        if (chartTemplateDTO.getAxisSettings() != null) {
            settingDTOAxis.addAll(chartTemplateDTO.getAxisSettings());
        }
        if (chartTemplateDTO.getChartSettings() != null) {
            settingDTOChart.addAll(chartTemplateDTO.getChartSettings());
        }
        //Save chart template
        ChartTemplate chartTemplate = modelMapper.map(chartTemplateDTO, ChartTemplate.class);
        chartTemplate = chartTemplateRepository.save(chartTemplate);
        //Save settings
        settingService.createSettings(settingDTOAxis, chartTemplate.getId(), SettingType.AXIS);
        settingService.createSettings(settingDTOChart, chartTemplate.getId(), SettingType.CHART);
        //Return chart template
        chartTemplateDTO = mapToChartTemplateDTO(chartTemplate);
        return chartTemplateDTO;
    }

    public ChartTemplateDTO updateChartTemplate(ChartTemplateDTO chartTemplateDTO) {
        ChartTemplate chartTemplate = modelMapper.map(chartTemplateDTO, ChartTemplate.class);

        //Update settings
        updateSettings(chartTemplateDTO);
        chartTemplate = chartTemplateRepository.save(chartTemplate);
        chartTemplateDTO = mapToChartTemplateDTO(chartTemplate);
        return chartTemplateDTO;
    }

    public void deleteChartTemplate(Long id) {
        settingService.deleteSettingsByChartTemplateId(id);
        chartTemplateRepository.deleteById(id);
    }

    public ChartTemplateDTO getChartTemplateById(Long id) {
        ChartTemplate chartTemplate = chartTemplateRepository.findById(id).orElse(null);
        if (chartTemplate == null) {
            return null;
        }
        ChartTemplateDTO chartTemplateDTO = mapToChartTemplateDTO(chartTemplate);
        return chartTemplateDTO;
    }

    public List<ChartTemplateDTO> getAllChartTemplates() {
        List<ChartTemplate> chartTemplates = (List<ChartTemplate>) chartTemplateRepository.findAll();
        List<ChartTemplateDTO> chartTemplateDTOs = mapToChartTemplateDTOs(chartTemplates);
        return chartTemplateDTOs;
    }

    private ChartTemplateDTO mapToChartTemplateDTO(ChartTemplate chartTemplate) {
        ChartTemplateDTO chartTemplateDTO = new ChartTemplateDTO();
        chartTemplateDTO.setId(chartTemplate.getId());
        chartTemplateDTO.setType(chartTemplate.getType());
        chartTemplateDTO.setAxisX(chartTemplate.getAxisX());
        chartTemplateDTO.setAxisY(chartTemplate.getAxisY());
        chartTemplateDTO.setGroupBy(chartTemplate.getGroupBy());
        chartTemplateDTO.setFilter(chartTemplate.getFilter());
        chartTemplateDTO.setAxisSettings(settingService.getSettingsByChartTemplateId(chartTemplate.getId(), SettingType.AXIS));
        chartTemplateDTO.setChartSettings(settingService.getSettingsByChartTemplateId(chartTemplate.getId(), SettingType.CHART));
        return chartTemplateDTO;
    }

    private List<ChartTemplateDTO> mapToChartTemplateDTOs(List<ChartTemplate> chartTemplates) {
        List<ChartTemplateDTO> chartTemplateDTOs = new ArrayList<>();
        for (ChartTemplate chartTemplate : chartTemplates) {
            chartTemplateDTOs.add(mapToChartTemplateDTO(chartTemplate));
        }
        return chartTemplateDTOs;
    }

    private void updateSettings(ChartTemplateDTO chartTemplateDTO) {
        //Get existing settings
        List<SettingDTO> settingDTOAxis = settingService.getSettingsByChartTemplateId(chartTemplateDTO.getId(), SettingType.AXIS);
        List<SettingDTO> settingDTOChart = settingService.getSettingsByChartTemplateId(chartTemplateDTO.getId(), SettingType.CHART);
        //Get settings that are not in chartTemplateDTO
        List<SettingDTO> settingDTOAxisToDelete = new ArrayList<>(settingDTOAxis);
        List<SettingDTO> settingDTOChartToDelete = new ArrayList<>(settingDTOChart);
        if (chartTemplateDTO.getAxisSettings() != null) {
            //Remove settings that only have the same id
            List<SettingDTO> settingDTOsToKeepAxis = new ArrayList<>(chartTemplateDTO.getAxisSettings());
            removeSettingsById(settingDTOAxisToDelete, settingDTOsToKeepAxis);
        }
        if (chartTemplateDTO.getChartSettings() != null) {
            //Remove settings that only have the same id
            List<SettingDTO> settingDTOsToKeepChart = new ArrayList<>(chartTemplateDTO.getChartSettings());
            removeSettingsById(settingDTOChartToDelete, settingDTOsToKeepChart);
        }
        //Delete settings that are not in chartTemplateDTO
        settingService.deleteSettings(settingDTOAxisToDelete);
        settingService.deleteSettings(settingDTOChartToDelete);

        //Update settings
        settingService.updateSettings(chartTemplateDTO.getAxisSettings(), chartTemplateDTO.getId());
    }

    private void removeSettingsById(List<SettingDTO> settingDTOs, List<SettingDTO> settingDTOsToKeep) {
        List<SettingDTO> settingDTOsToRemove = new ArrayList<>();
        for (SettingDTO settingDTO : settingDTOsToKeep) {
            for (SettingDTO settingDTO1 : settingDTOs) {
                if (settingDTO.getId() != null && settingDTO.getId().equals(settingDTO1.getId())) {
                    settingDTOsToRemove.add(settingDTO1);
                }
            }
        }
        settingDTOs.removeAll(settingDTOsToRemove);
    }
}
