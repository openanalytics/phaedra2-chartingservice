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
package eu.openanalytics.phaedra.chartingservice.service;

import eu.openanalytics.phaedra.chartingservice.dto.ChartDataDTO;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.dto.ChartTupleDTO;
import eu.openanalytics.phaedra.plateservice.dto.PlateMeasurementDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.FeatureDTO;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.PlateUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChartDataService {

    private final ResultDataServiceClient resultDataServiceClient;
    private final PlateServiceClient plateServiceClient;
    private final ProtocolServiceClient protocolServiceClient;

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
            if (measurementDTO.getActive()==true) {
                return measurementDTO.getId();
            }
        }
        throw new ChartDataException("No active measurement found for plate with id " + plateId);
    }

    private ResultSetDTO getLatestResultSet(Long plateId, Long measurementId) throws ChartDataException {
        try {
            return resultDataServiceClient.getLatestResultSet(plateId, measurementId);
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
            chartTupleDTOs.add(new ChartTupleDTO("CompoundId", String.valueOf(well.getCompoundId())));
            chartTupleDTOs.add(new ChartTupleDTO("WellSubstance", well.getWellSubstance().getName()));
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

}
