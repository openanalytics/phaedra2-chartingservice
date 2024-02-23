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
package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.dto.ChartDataDTO;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.service.ChartDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChartDataController {

    private final ChartDataService chartDataService;

    public ChartDataController(ChartDataService chartDataService) {
        this.chartDataService = chartDataService;
    }

    //Get chart data for given plate ids and type
    @GetMapping(value = "/chartdata/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChartDataDTO>> getChartData(@PathVariable String type, @RequestParam List<Long> plateIds) throws ChartDataException {
        List<ChartDataDTO> chartData = chartDataService.getChartDataByPlateIds(plateIds, type);
        if (chartData != null) {
            return new ResponseEntity<>(chartData, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
