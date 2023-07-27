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
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
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
            if ("welltype".equalsIgnoreCase(groupBy)) {
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
            } else if ("row".equalsIgnoreCase(groupBy)) {
                String row = wells.get(i).getRow().toString();
                if (!groupByMap.containsKey(row)) {
                    groupByMap.put(row, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(row)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(row).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(row).getYValue().add(yResultData.getValues()[i]);
            } else if ("column".equalsIgnoreCase(groupBy)) {
                String column = wells.get(i).getColumn().toString();
                if (!groupByMap.containsKey(column)) {
                    groupByMap.put(column, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(column)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(column).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(column).getYValue().add(yResultData.getValues()[i]);
            } else if ("status".equalsIgnoreCase(groupBy)) {
                String wellStatus = wells.get(i).getStatus().name();
                if (!groupByMap.containsKey(wellStatus)) {
                    groupByMap.put(wellStatus, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(wellStatus)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(wellStatus).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(wellStatus).getYValue().add(yResultData.getValues()[i]);
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

    @QueryMapping
    public Chart barPlot(@Argument long plateId, @Argument long xFeatureId, @Argument long yFeatureId, @Argument String groupBy) throws ResultSetUnresolvableException, ResultDataUnresolvableException, PlateUnresolvableException, FeatureUnresolvableException {
        ResultSetDTO latestResultSet = resultDataServiceClient.getLatestResultSet(plateId);

        ResultDataDTO xResultData = resultDataServiceClient.getResultData(latestResultSet.getId(), xFeatureId);
        ResultDataDTO yResultData = resultDataServiceClient.getResultData(latestResultSet.getId(), yFeatureId);

        List<WellDTO> wells = plateServiceClient.getWells(plateId);

        Map<String, ChartData> groupByMap = new HashMap<>();

        IntStream.range(0, wells.size()).forEach(i -> {
            if ("welltype".equalsIgnoreCase(groupBy)) {
                String wellType = wells.get(i).getWellType();
                if (!groupByMap.containsKey(wellType)) {
                    groupByMap.put(wellType, ChartData.builder()
                            .mode("markers")
                            .type("bar")
                            .name(wellType)
                            .xValue(new ArrayList<>())
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(wellType).getXValue().add(xResultData.getValues()[i]);
                groupByMap.get(wellType).getYValue().add(yResultData.getValues()[i]);
            } else if ("substance".equalsIgnoreCase(groupBy)) {
                String substanceName = wells.get(i).getWellSubstance().getName();
                if (!groupByMap.containsKey(substanceName)) {
                    groupByMap.put(substanceName, ChartData.builder()
                            .mode("markers")
                            .type("bar")
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
                            .type("bar")
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
                .chartTitle(String.format("Plate Bar Plot"))
                .xAxisLabel(xFeature.getName())
                .yAxisLabel(yFeature.getName())
                .build());

        return chart;
    }

    @QueryMapping
    public Chart boxPlot(@Argument long plateId, @Argument long featureId) throws ResultSetUnresolvableException, ResultDataUnresolvableException, PlateUnresolvableException, FeatureUnresolvableException {
        ResultSetDTO latestResultSet = resultDataServiceClient.getLatestResultSet(plateId);
        ResultDataDTO resultData = resultDataServiceClient.getResultData(latestResultSet.getId(), featureId);

        List<Float> yValues = new ArrayList<>();
        IntStream.range(0, resultData.getValues().length).forEach(i -> {
            yValues.add(Float.valueOf(resultData.getValues()[i]));
        });
        ChartData chartData = ChartData.builder().type("box").yValue(yValues).build();

        FeatureDTO feature = protocolServiceClient.getFeature(featureId);
        ChartLayout chartLayout = ChartLayout.builder()
                .chartTitle(String.format("Box plot"))
                .yAxisLabel(feature.getName())
                .build();

        Chart chart = new Chart();
        chart.setData(ArrayUtils.toArray(chartData));
        chart.setLayout(chartLayout);

        return chart;
    }

    @QueryMapping
    public Chart boxPlotWithGrouping(@Argument long plateId, @Argument long featureId, @Argument String groupBy) throws ResultSetUnresolvableException, ResultDataUnresolvableException, PlateUnresolvableException, FeatureUnresolvableException {
        ResultSetDTO latestResultSet = resultDataServiceClient.getLatestResultSet(plateId);
        ResultDataDTO resultData = resultDataServiceClient.getResultData(latestResultSet.getId(), featureId);
        var wells = plateServiceClient.getWells(plateId);
        FeatureDTO feature = protocolServiceClient.getFeature(featureId);

        Map<String, ChartData> groupByMap = new HashMap<>();
        IntStream.range(0, wells.size()).forEach(i -> {
            if ("welltype".equalsIgnoreCase(groupBy)) {
                String wellType = wells.get(i).getWellType();
                if (!groupByMap.containsKey(wellType)) {
                    groupByMap.put(wellType, ChartData.builder()
                            .type("box")
                            .name(wellType)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(wellType).getYValue().add(resultData.getValues()[i]);
            } else if ("substance".equalsIgnoreCase(groupBy)) {
                String substanceName = wells.get(i).getWellSubstance().getName();
                if (!groupByMap.containsKey(substanceName)) {
                    groupByMap.put(substanceName, ChartData.builder()
                            .type("box")
                            .name(substanceName)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(substanceName).getYValue().add(resultData.getValues()[i]);
            } else if ("row".equalsIgnoreCase(groupBy)) {
                String row = wells.get(i).getRow().toString();
                if (!groupByMap.containsKey(row)) {
                    groupByMap.put(row, ChartData.builder()
                            .type("box")
                            .name(row)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(row).getYValue().add(resultData.getValues()[i]);
            } else if ("column".equalsIgnoreCase(groupBy)) {
                String column = wells.get(i).getColumn().toString();
                if (!groupByMap.containsKey(column)) {
                    groupByMap.put(column, ChartData.builder()
                            .type("box")
                            .name(column)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(column).getYValue().add(resultData.getValues()[i]);
            } else if ("status".equalsIgnoreCase(groupBy)) {
                String wellStatus = wells.get(i).getStatus().name();
                if (!groupByMap.containsKey(wellStatus)) {
                    groupByMap.put(wellStatus, ChartData.builder()
                            .type("box")
                            .name(wellStatus)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(wellStatus).getYValue().add(resultData.getValues()[i]);
            } else {
                if (!groupByMap.containsKey(groupBy)) {
                    groupByMap.put(groupBy, ChartData.builder()
                            .type("box")
                            .name(groupBy)
                            .yValue(new ArrayList<>())
                            .build());
                }
                groupByMap.get(groupBy).getYValue().add(resultData.getValues()[i]);
            }
        });

        Chart chart = new Chart();
        chart.setData(groupByMap.values().toArray(ChartData[]::new));
        chart.setLayout(ChartLayout.builder()
                .chartTitle(String.format("Box Plot"))
                .yAxisLabel(feature.getName())
                .build());

        return chart;
    }
}
