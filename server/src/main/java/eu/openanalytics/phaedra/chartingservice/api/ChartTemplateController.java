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
package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.dto.ChartTemplateDTO;
import eu.openanalytics.phaedra.chartingservice.service.ChartTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChartTemplateController {

    private final ChartTemplateService chartTemplateService;

    public ChartTemplateController(ChartTemplateService chartTemplateService) {
        this.chartTemplateService = chartTemplateService;
    }

    //Post chart template
    @PostMapping(value = "/chart-template", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartTemplateDTO> createChartTemplate(@RequestBody ChartTemplateDTO chartTemplateDTO) {
        ChartTemplateDTO response = chartTemplateService.createChartTemplate(chartTemplateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    //Put chart template
    @PutMapping(value = "/chart-template", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateChartTemplate(@RequestBody ChartTemplateDTO chartTemplateDTO) {
        chartTemplateService.updateChartTemplate(chartTemplateDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    //Delete chart template
    @DeleteMapping(value="/chart-template/{chartTemplateId}")
    public ResponseEntity<Void> deleteChartTemplate(@PathVariable long chartTemplateId) {
        chartTemplateService.deleteChartTemplate(chartTemplateId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    //Get all chart templates
    @GetMapping(value = "/chart-template", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<ChartTemplateDTO>> getAllChartTemplates() {
        Iterable<ChartTemplateDTO> response = chartTemplateService.getAllChartTemplates();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    //Get chart template by id
    @GetMapping(value="/chart-template/{chartTemplateId}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartTemplateDTO> getChartTemplate(@PathVariable Long chartTemplateId) {
        ChartTemplateDTO response = chartTemplateService.getChartTemplateById(chartTemplateId);
        if (response != null)
            return new ResponseEntity<>(response, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
