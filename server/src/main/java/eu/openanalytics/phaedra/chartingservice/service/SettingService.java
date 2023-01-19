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
import eu.openanalytics.phaedra.chartingservice.model.Setting;
import eu.openanalytics.phaedra.chartingservice.repository.ChartTemplateRepository;
import eu.openanalytics.phaedra.chartingservice.repository.SettingRepository;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingService {
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private final SettingRepository settingRepository;
    private final ChartTemplateRepository chartTemplateRepository;

    public SettingService(SettingRepository settingRepository, ChartTemplateRepository chartTemplateRepository) {
        this.settingRepository = settingRepository;
        this.chartTemplateRepository = chartTemplateRepository;

        Configuration builderConfiguration = modelMapper.getConfiguration().copy()
                .setDestinationNameTransformer(NameTransformers.builder())
                .setDestinationNamingConvention(NamingConventions.builder());
        modelMapper.createTypeMap(Setting.class, SettingDTO.SettingDTOBuilder.class, builderConfiguration)
                .setPropertyCondition(Conditions.isNotNull());

    }

    public SettingDTO createSetting(SettingDTO settingDTO) {
        if (!doesChartTemplateExist(settingDTO.getChartTemplateId())) {
            throw new IllegalArgumentException("Chart template does not exist");
        }
        Setting newSetting = modelMapper.map(settingDTO, Setting.class);
        newSetting = settingRepository.save(newSetting);
        return mapToSettingDTO(newSetting);
    }

    public List<SettingDTO> createSettings(List<SettingDTO> settingDTOs, Long chartTemplateId, SettingType settingType) {
        if (!doesChartTemplateExist(chartTemplateId)) {
            throw new IllegalArgumentException("Chart template does not exist");
        }
        if (settingDTOs == null || settingDTOs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Setting> settings = new ArrayList<>();
        for (SettingDTO settingDTO : settingDTOs) {
            settingDTO.setChartTemplateId(chartTemplateId);
            settingDTO.setSettingType(settingType);
            Setting newSetting = modelMapper.map(settingDTO, Setting.class);
            settings.add(newSetting);
        }
        settings = (List<Setting>) settingRepository.saveAll(settings);
        return mapToSettingDTOs(settings);
    }

    public SettingDTO updateSetting(SettingDTO settingDTO) {
        Setting setting = modelMapper.map(settingDTO, Setting.class);
        setting = settingRepository.save(setting);
        return mapToSettingDTO(setting);
    }

    public List<SettingDTO> updateSettings(List<SettingDTO> settingDTOs, Long chartTemplateId) {
        settingDTOs.forEach(settingDTO -> settingDTO.setChartTemplateId(chartTemplateId));
        List<Setting> settings = new ArrayList<>();
        for (SettingDTO settingDTO : settingDTOs) {
            Setting setting = modelMapper.map(settingDTO, Setting.class);
            settings.add(setting);
        }
        settings = (List<Setting>) settingRepository.saveAll(settings);
        return mapToSettingDTOs(settings);
    }

    public void deleteSettingsByChartTemplateId(Long id) {
        settingRepository.deleteByChartTemplateId(id);
    }

    public void deleteSettings(List<SettingDTO> settingDTOs) {
        for (SettingDTO settingDTO : settingDTOs) {
            settingRepository.deleteById(settingDTO.getId());
        }
    }

    public SettingDTO getSettingById(Long id) {
        Setting setting = settingRepository.findById(id).orElse(null);
        return modelMapper.map(setting, SettingDTO.class);
    }

    public List<SettingDTO> getSettingsByChartTemplateId(Long chartTemplateId, SettingType settingType) {
        List<Setting> settings = settingRepository.findByChartTemplateIdAndSettingType(chartTemplateId, settingType);
        return mapToSettingDTOs(settings);
    }

    private SettingDTO mapToSettingDTO(Setting setting) {
        var builder = modelMapper.map(setting, SettingDTO.SettingDTOBuilder.class);
        return builder.build();
    }

    private List<SettingDTO> mapToSettingDTOs(List<Setting> settings) {
        List<SettingDTO> settingDTOs = new ArrayList<>();
        for (Setting setting : settings) {
            settingDTOs.add(mapToSettingDTO(setting));
        }
        return settingDTOs;
    }

    private boolean doesChartTemplateExist(Long id) {
        return chartTemplateRepository.existsById(id);
    }

}
