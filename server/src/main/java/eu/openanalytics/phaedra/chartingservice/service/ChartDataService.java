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

import eu.openanalytics.phaedra.chartingservice.dto.ChartDataDTO;
import eu.openanalytics.phaedra.chartingservice.dto.ChartTupleDTO;
import eu.openanalytics.phaedra.chartingservice.enumeration.AxisFieldType;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.model.FeatureStatData;
import eu.openanalytics.phaedra.chartingservice.model.TrendChartData;
import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.PlateUnresolvableException;
import eu.openanalytics.phaedra.plateservice.dto.PlateDTO;
import eu.openanalytics.phaedra.plateservice.dto.PlateMeasurementDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.plateservice.enumeration.CalculationStatus;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.FeatureUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.FeatureDTO;
import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultFeatureStatUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChartDataService {

    private final ResultDataServiceClient resultDataServiceClient;
    private final PlateServiceClient plateServiceClient;
    private final ProtocolServiceClient protocolServiceClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ChartDataService(ResultDataServiceClient resultDataServiceClient, PlateServiceClient plateServiceClient, ProtocolServiceClient protocolServiceClient) {
        this.resultDataServiceClient = resultDataServiceClient;
        this.plateServiceClient = plateServiceClient;
        this.protocolServiceClient = protocolServiceClient;
    }

    public List<ChartDataDTO> getChartDataByPlateIds(List<Long> plateIds, String type) throws ChartDataException {
        List<ChartDataDTO> chartData = new ArrayList<>();
        for (Long plateId : plateIds) {
            chartData.addAll(getChartDataByPlateId(plateId, type));
        }
        return chartData;
    }

    public List<ChartDataDTO> getChartDataByPlateId(Long plateId, String type) throws ChartDataException {
        Long measurementId = getActiveMeasurementIdByPlateId(plateId);
        ResultSetDTO resultSetDTO = getLatestResultSet(plateId, measurementId);

        if (resultSetDTO == null)
            throw new ChartDataException("No result set found for plate " + plateId + " and measurement " + measurementId);

        List<ChartDataDTO> chartTuplesWells = getWellDataByPlateId(plateId, type);
        //Add values from features to chartData
        List<ChartDataDTO> chartTuplesFeatures = getChartDataByResultSet(resultSetDTO, type, chartTuplesWells);
        return chartTuplesFeatures;
    }

    private Long getActiveMeasurementIdByPlateId(Long plateId) throws ChartDataException {
        List<PlateMeasurementDTO> measurementDTOs = new ArrayList<>();
        try {
            measurementDTOs = plateServiceClient.getPlateMeasurements(plateId);
        } catch (PlateUnresolvableException e) {
            throw new ChartDataException("Measurements for plate with id " + plateId + " are not resolvable");
        }
        //Find the active measurement
        for (PlateMeasurementDTO measurementDTO : measurementDTOs) {
            if (measurementDTO.getActive() == true) {
                return measurementDTO.getMeasurementId();
            }
        }
        throw new ChartDataException("No active measurement found for plate with id " + plateId);
    }

    private ResultSetDTO getLatestResultSet(Long plateId, Long measurementId) throws ChartDataException {
        try {
            return resultDataServiceClient.getLatestResultSetByPlateIdAndMeasId(plateId, measurementId);
        } catch (ResultSetUnresolvableException e) {
            throw new ChartDataException("Latest result set for plate with id " + plateId + " and measurement with id " + measurementId + " is not resolvable");
        }
    }

    private List<ChartDataDTO> getChartDataByResultSet(ResultSetDTO resultSetDTO, String type, List<ChartDataDTO> chartTuplesWells) throws ChartDataException {
        List<ResultDataDTO> resultDataDTOS = getResultDataByResultSetId(resultSetDTO);
        List<FeatureDTO> featureDTOS = getFeaturesByProtocolId(resultSetDTO.getProtocolId());
        for (ResultDataDTO resultDataDTO : resultDataDTOS) {
            Integer index = 0;
            String featureName = getFeatureNameById(featureDTOS, resultDataDTO.getFeatureId());
            if (featureName != null) {
                for (ChartDataDTO chartTupleWell : chartTuplesWells) {
                    chartTupleWell.getValues().add(new ChartTupleDTO(featureName, Float.toString(resultDataDTO.getValues()[index])));
                    index++;
                }
            }
        }
        return chartTuplesWells;
    }

    private List<ResultDataDTO> getResultDataByResultSetId(ResultSetDTO resultSetDTO) throws ChartDataException {
        try {
            return resultDataServiceClient.getResultData(resultSetDTO.getId());
        } catch (ResultDataUnresolvableException e) {
            throw new ChartDataException("Result data for result set with id " + resultSetDTO.getId() + " is not resolvable");
        }
    }

    private List<FeatureDTO> getFeaturesByProtocolId(Long protocolId) throws ChartDataException {
        try {
            return protocolServiceClient.getFeaturesOfProtocol(protocolId);
        } catch (ProtocolUnresolvableException e) {
            throw new ChartDataException("Features for protocol with id " + protocolId + " are not resolvable");
        }
    }

    private String getFeatureNameById(List<FeatureDTO> featureDTOS, Long featureId) {
        for (FeatureDTO featureDTO : featureDTOS) {
            if (featureDTO.getId().equals(featureId)) {
                return featureDTO.getName();
            }
        }
        return null;
    }

    private List<ChartDataDTO> getWellDataByPlateId(Long plateId, String type) throws ChartDataException {
        List<WellDTO> wells;
        try {
            wells = plateServiceClient.getWells(plateId);
        } catch (PlateUnresolvableException e) {
            throw new ChartDataException("Wells for plate with id " + plateId + " could not be found");
        }

        List<ChartDataDTO> chartDataDTOS = wells.stream().map(well -> {
            List<ChartTupleDTO> chartTupleDTOs = new ArrayList<>();
            chartTupleDTOs.add(new ChartTupleDTO("WellId", String.valueOf(well.getId())));
            chartTupleDTOs.add(new ChartTupleDTO("PlateId", String.valueOf(well.getPlateId())));
            chartTupleDTOs.add(new ChartTupleDTO("Row", String.valueOf(well.getRow())));
            chartTupleDTOs.add(new ChartTupleDTO("Column", String.valueOf(well.getColumn())));
            chartTupleDTOs.add(new ChartTupleDTO("WellType", well.getWellType()));
            chartTupleDTOs.add(new ChartTupleDTO("WellStatus", well.getStatus().name()));
            if (well.getWellSubstance() != null) {
                chartTupleDTOs.add(
                    new ChartTupleDTO("WellSubstance", well.getWellSubstance().getName()));
            }
            ChartDataDTO chartDataDTO = new ChartDataDTO(well.getId(), chartTupleDTOs);
            return chartDataDTO;
        }).collect(Collectors.toList());

        //Sort the list by wellId
        return sortByWellId(chartDataDTOS);
    }

    private List<ChartDataDTO> sortByWellId(List<ChartDataDTO> chartDataDTOS) {
        //Ascending order
        chartDataDTOS.sort(Comparator.comparing(ChartDataDTO::getWellId));
        return chartDataDTOS;
    }

    public Map<String, ChartData> getScatterPlotData(Long plateId, Long protocolId, String xFieldName, AxisFieldType xFieldType, String yFieldName, AxisFieldType yFieldType, String groupBy) throws ChartDataException {
        List<WellDTO> wells = retrieveWellData(plateId);
        List<String> xValues = getChartData(plateId, protocolId, xFieldName, xFieldType);
        List<String> yValues = getChartData(plateId, protocolId, yFieldName, yFieldType);

        Map<String, ChartData> groupByMap = new HashMap<>();
        IntStream.range(0, wells.size()).forEach(i -> {
            String groupKey = defineGroupKey(wells.get(i), groupBy);
            if (StringUtils.isNotBlank(groupKey)) {
                if (!groupByMap.containsKey(groupKey)) {
                    groupByMap.put(groupKey, ChartData.builder()
                            .mode("markers")
                            .type("scatter")
                            .name(groupKey)
                            .xValues(new ArrayList<>())
                            .yValues(new ArrayList<>())
                            .build());
                }
                groupByMap.get(groupKey).getXValues().add(xValues.get(i));
                groupByMap.get(groupKey).getYValues().add(yValues.get(i));
            }
        });
        return groupByMap;
    }

    public Map<String, ChartData> getBoxPlotData(Long plateId, Long protocolId, String fieldName, AxisFieldType fieldType, String groupBy) throws ChartDataException {
        List<WellDTO> wells = retrieveWellData(plateId);
        List<String> yValues = getChartData(plateId, protocolId, fieldName, fieldType);

        Map<String, ChartData> groupByMap = new HashMap<>();
        IntStream.range(0, wells.size()).forEach(i -> {
            String groupKey = defineGroupKey(wells.get(i), groupBy);
            if (StringUtils.isNotBlank(groupKey)) {
                if (!groupByMap.containsKey(groupKey)) {
                    groupByMap.put(groupKey, ChartData.builder()
                            .type("box")
                            .name(groupKey)
                            .yValues(new ArrayList<>())
                            .build());
                }
                groupByMap.get(groupKey).getYValues().add(yValues.get(i));
            }
        });
        return groupByMap;
    }

    public Map<String, ChartData> getHistogramPlotData(Long plateId, Long protocolId, String fieldName, AxisFieldType fieldType, String groupBy) throws ChartDataException {
        List<WellDTO> wells = retrieveWellData(plateId);
        List<String> xValues = getChartData(plateId, protocolId, fieldName, fieldType);

        Map<String, ChartData> groupByMap = new HashMap<>();
        IntStream.range(0, wells.size()).forEach(i -> {
            String groupKey = defineGroupKey(wells.get(i), groupBy);
            if (StringUtils.isNotBlank(groupKey)) {
                if (!groupByMap.containsKey(groupKey)) {
                    groupByMap.put(groupKey, ChartData.builder()
                            .type("histogram")
                            .xValues(new ArrayList<>())
                            .build());
                }
                groupByMap.get(groupKey).getXValues().add(xValues.get(i));
            }
        });
        return groupByMap;
    }

    public List<String> getChartData(Long plateId, Long protocolId, String fieldName, AxisFieldType fieldType) throws ChartDataException {
        if (AxisFieldType.FEATURE_ID.equals(fieldType)) {
            ResultDataDTO resultData = retrieveResultData(plateId, protocolId, Long.parseLong(fieldName));
            return convertValuesToReadableFormat(resultData.getValues());
        } else if (AxisFieldType.WELL_PROPERTY.equals(fieldType)) {
            List<WellDTO> wellData = retrieveWellData(plateId);
            return convertWellDataToReadableFormat(wellData, fieldName);
        } else {
            throw new ChartDataException("Unknown axis field type!");
        }
    }

    public List<TrendChartData> getTrendChartData(Long experimentId) {
        List<PlateDTO> plates = plateServiceClient.getPlatesByExperiment(experimentId);
        if (CollectionUtils.isEmpty(plates))
            return Collections.emptyList();

        return plates.stream()
            .filter(plate -> CalculationStatus.CALCULATION_OK.equals(plate.getCalculationStatus()))
            .map(this::createTrendChartDataForPlate)
            .collect(Collectors.toList());
    }

    private ResultDataDTO retrieveResultData(Long plateId, Long protocolId, Long featureId) throws ChartDataException {
        try {
            ResultSetDTO resultSet = resultDataServiceClient.getLatestResultSetByPlateIdAndProtocolId(plateId, protocolId);
            return resultDataServiceClient.getResultData(resultSet.getId(), featureId);
        } catch (ResultSetUnresolvableException | ResultDataUnresolvableException e) {
            throw new ChartDataException(e.getMessage());
        }
    }

    private List<WellDTO> retrieveWellData(Long plateId) throws ChartDataException {
        try {
            return plateServiceClient.getWells(plateId);
        } catch (PlateUnresolvableException e) {
            throw new ChartDataException(e.getMessage());
        }
    }

    private List<String> convertValuesToReadableFormat(float[] values) {
        return IntStream.range(0, values.length)
                .mapToObj(i -> String.valueOf(values[i]))
                .collect(Collectors.toList());
    }

    private Map<String, Function<WellDTO, String>> fieldMapper = Map.of(
            "wellId", well -> String.valueOf(well.getId()),
            "row", well -> String.valueOf(well.getRow()),
            "column", well -> String.valueOf(well.getColumn()),
            "wellNr", well -> String.valueOf(well.getWellNr()),
            "wellType", WellDTO::getWellType,
            "wellSubstance", well -> well.getWellSubstance().getName(),
            "wellConcentration", well -> String.valueOf(well.getWellSubstance().getConcentration())
    );

    private List<String> convertWellDataToReadableFormat(List<WellDTO> wellData, String fieldName) throws ChartDataException {
        Function<WellDTO, String> mapper = fieldMapper.get(fieldName);
        if (mapper == null) {
            throw new ChartDataException(String.format("Unknown well property %s!", fieldName));
        }
        return wellData.stream().map(mapper).toList();
    }

    private String defineGroupKey(WellDTO well, String groupBy) {
        switch (groupBy.toLowerCase()) {
            case "welltype":
                return well.getWellType();
            case "substance":
                return well.getWellSubstance() != null ? well.getWellSubstance().getName() : null;
            case "row":
                return well.getRow().toString();
            case "column":
                return well.getColumn().toString();
            case "status":
                return well.getStatus().name();
            case "none":
                return groupBy;
            default:
                throw new IllegalArgumentException("Unsupported groupBy value: " + groupBy);
        }
    }

    private TrendChartData createTrendChartDataForPlate(PlateDTO plateDTO) {
        TrendChartData trendChartData = new TrendChartData();

        try {
            List<ResultFeatureStatDTO> featureStats = resultDataServiceClient.getLatestResultFeatureStatsForPlateId(
                plateDTO.getId());

            if (CollectionUtils.isNotEmpty(featureStats)) {
                List<FeatureStatData> featureStatData = featureStats.stream()
                    .map(this::mapToFeatureStatData)
                    .collect(Collectors.toList());

                trendChartData.setFeatureStats(featureStatData);
            }

        } catch (ResultFeatureStatUnresolvableException e) {
            logger.error(e.getMessage());
        } finally {
            trendChartData.setPlateId(plateDTO.getId());
            trendChartData.setBarcode(plateDTO.getBarcode());
        }

        return trendChartData;
    }

    private FeatureStatData mapToFeatureStatData(ResultFeatureStatDTO fstat) {
        FeatureStatData fStatData = new FeatureStatData();
        fStatData.setFeatureId(fstat.getFeatureId());
        fStatData.setStatName(fstat.getStatisticName());
        fStatData.setStatValue(fstat.getValue());
        fStatData.setWellType(fstat.getWelltype());
        return fStatData;
    }
}
