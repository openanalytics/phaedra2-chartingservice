/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SettingRepository extends CrudRepository<Setting, Long> {
    List<Setting> findByChartTemplateId(Long chartTemplateId);

    List<Setting> findByChartTemplateIdAndSettingType(Long chartTemplateId, SettingType settingType);

    //Delete all settings for a chart template
    @Modifying
    @Query("DELETE FROM hca_setting WHERE chart_template_id = :chartTemplateId")
    void deleteByChartTemplateId(Long chartTemplateId);
}
