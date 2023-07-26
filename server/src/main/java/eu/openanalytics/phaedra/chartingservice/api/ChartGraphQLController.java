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

import eu.openanalytics.phaedra.chartingservice.model.Chart;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.model.ChartLayout;
import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.PlateUnresolvableException;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.FeatureUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.FeatureDTO;
import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
public class ChartGraphQLController {

    private final ProtocolServiceClient protocolServiceClient;
    private final ResultDataServiceClient resultDataServiceClient;
    private final PlateServiceClient plateServiceClient;

    public ChartGraphQLController(ProtocolServiceClient protocolServiceClient, ResultDataServiceClient resultDataServiceClient, PlateServiceClient plateServiceClient) {
        this.protocolServiceClient = protocolServiceClient;
        this.resultDataServiceClient = resultDataServiceClient;
        this.plateServiceClient = plateServiceClient;
    }

    @QueryMapping
    public Chart scatterPlot(@Argument long plateId, @Argument long xFeatureId, @Argument long yFeatureId, @Argument String groupBy) throws ResultSetUnresolvableException, ResultDataUnresolvableException, PlateUnresolvableException, FeatureUnresolvableException {
        ResultSetDTO latestResultSet = resultDataServiceClient.getLatestResultSet(plateId);

        ResultDataDTO xResultData = resultDataServiceClient.getResultData(latestResultSet.getId(), xFeatureId);
        ResultDataDTO yResultData = resultDataServiceClient.getResultData(latestResultSet.getId(), yFeatureId);

        List<WellDTO> wells = plateServiceClient.getWells(plateId);

        Map<String, ChartData> groupByMap = new HashMap<>();

        IntStream.range(0, wells.size()).forEach(i -> {
            if (groupBy.equalsIgnoreCase("welltype")) {
                String wellType = wells.get(i).getWellType();
                if (!groupByMap.containsKey(wellType)) {
                    groupByMap.put(wellType, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(wellType)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(wellType).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(wellType).getYValue().add(yResultData.getValues()[i]);
            } else if (groupBy.equalsIgnoreCase("substance")) {
                String substanceName = wells.get(i).getWellSubstance().getName();
                if (!groupByMap.containsKey(substanceName)) {
                    groupByMap.put(substanceName, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(substanceName)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(substanceName).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(substanceName).getYValue().add(yResultData.getValues()[i]);
            } else {
                if (!groupByMap.containsKey(groupBy)) {
                    groupByMap.put(groupBy, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(groupBy)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(groupBy).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(groupBy).getYValue().add(yResultData.getValues()[i]);
            }
        });

        FeatureDTO xFeature = protocolServiceClient.getFeature(xFeatureId);
        FeatureDTO yFeature = protocolServiceClient.getFeature(yFeatureId);

        Chart chart = new Chart();
        chart.setData(groupByMap.values().toArray(ChartData[]::new));
        chart.setLayout(ChartLayout.builder()
                .chartTitle(String.format("Plate Scatter Plot"))
                .xAxisLabel(xFeature.getName())
                .yAxisLabel(yFeature.getName())
                .build());

        return chart;
    }
}
