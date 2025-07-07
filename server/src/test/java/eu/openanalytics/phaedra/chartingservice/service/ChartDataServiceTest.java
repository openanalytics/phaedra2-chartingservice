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
package eu.openanalytics.phaedra.chartingservice.service;

import eu.openanalytics.phaedra.chartingservice.dto.ChartDataDTO;
import eu.openanalytics.phaedra.chartingservice.dto.ChartTupleDTO;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.UnresolvableObjectException;
import eu.openanalytics.phaedra.plateservice.dto.PlateMeasurementDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellSubstanceDTO;
import eu.openanalytics.phaedra.plateservice.enumeration.WellStatus;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.FeatureDTO;
import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Disabled
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
public class ChartDataServiceTest {

    private <T> T mockUnimplemented(Class<T> clazz) {
        return mock(clazz, invocation -> {
            throw new IllegalStateException(String.format("[%s:%s] must be stubbed with arguments [%s]!", invocation.getMock().getClass().getSimpleName(), invocation.getMethod().getName(), Arrays.toString(invocation.getArguments())));
        });
    }

    private ResultDataServiceClient resultDataServiceClient;
    private PlateServiceClient plateServiceClient;
    private ProtocolServiceClient protocolServiceClient;
    private ChartDataService chartDataService;

    @BeforeEach
    public void before() throws UnresolvableObjectException, ProtocolUnresolvableException, ResultSetUnresolvableException, ResultDataUnresolvableException {
        resultDataServiceClient = mockUnimplemented(ResultDataServiceClient.class);
        plateServiceClient = mockUnimplemented(PlateServiceClient.class);
        protocolServiceClient = mockUnimplemented(ProtocolServiceClient.class);
        chartDataService = new ChartDataService(resultDataServiceClient, plateServiceClient, protocolServiceClient);

        //Mocks
        List<FeatureDTO> featureDTOS = new ArrayList<>();
        featureDTOS.add(FeatureDTO.builder().id(1L).protocolId(1L).name("Feature 1").build());
        featureDTOS.add(FeatureDTO.builder().id(2L).protocolId(1L).name("Feature 2").build());
        doReturn(featureDTOS).when(protocolServiceClient).getFeaturesOfProtocol(anyLong());

        List<PlateMeasurementDTO> plateMeasurements = new ArrayList<>();
        plateMeasurements.add(PlateMeasurementDTO.builder().id(1L).plateId(1L).measurementId(1L).active(Boolean.TRUE).build());
        plateMeasurements.add(PlateMeasurementDTO.builder().id(2L).plateId(1L).measurementId(2L).active(Boolean.FALSE).build());
        doReturn(plateMeasurements).when(plateServiceClient).getPlateMeasurements(1L);

        List<WellDTO> wellDTOS = new ArrayList<>();
        WellSubstanceDTO fillerSubstanceDTO = new WellSubstanceDTO();
        fillerSubstanceDTO.setName("Filler");
        wellDTOS.add(WellDTO.builder().id(1L).plateId(1L).row(1).column(1).wellType("Sample").status(WellStatus.ACCEPTED).wellSubstance(fillerSubstanceDTO).build());
        wellDTOS.add(WellDTO.builder().id(2L).plateId(1L).row(1).column(2).wellType("Sample").status(WellStatus.ACCEPTED).wellSubstance(fillerSubstanceDTO).build());
        wellDTOS.add(WellDTO.builder().id(3L).plateId(1L).row(2).column(1).wellType("Sample").status(WellStatus.ACCEPTED).wellSubstance(fillerSubstanceDTO).build());
        wellDTOS.add(WellDTO.builder().id(4L).plateId(1L).row(2).column(2).wellType("Sample").status(WellStatus.ACCEPTED).wellSubstance(fillerSubstanceDTO).build());
        doReturn(wellDTOS).when(plateServiceClient).getWells(1L);

        doReturn(ResultSetDTO.builder().id(1L).plateId(1L).measId(1L).protocolId(1L).build()).when(resultDataServiceClient).getLatestResultSetByPlateIdAndMeasId(1L, 1L);
        List<ResultDataDTO> resultDataDTOS = new ArrayList<>();
        resultDataDTOS.add(ResultDataDTO.builder().id(1L).featureId(1L).resultSetId(1L).values(new float[]{1.11f, 2.22f, 3.33f, 4.44f}).build());
        resultDataDTOS.add(ResultDataDTO.builder().id(2L).featureId(2L).resultSetId(1L).values(new float[]{5.55f, 6.66f, 7.77f, 8.88f}).build());
        doReturn(resultDataDTOS).when(resultDataServiceClient).getResultData(1L);

    }

    @Test
    public void simpleTest() throws ChartDataException{
        List<ChartDataDTO> chartDataDTOS = chartDataService.getChartDataByPlateId(1L, "line");
        assertThat(chartDataDTOS).isNotNull();
        assertThat(chartDataDTOS.size()).isEqualTo(4);
        ChartDataDTO chartDataDTOWell1 = chartDataDTOS.get(0);
        assertThat(chartDataDTOWell1.getWellId()).isEqualTo(1L);
        //Values of the first well
        List<ChartTupleDTO> chartTupleDTOS = chartDataDTOWell1.getValues();
        assertThat(chartTupleDTOS).isNotNull();
        assertThat(chartTupleDTOS.size()).isEqualTo(10);
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellId")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("PlateId")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Row")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Column")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellType")).findFirst().get().getValue()).isEqualTo("Sample");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellStatus")).findFirst().get().getValue()).isEqualTo("ACCEPTED");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("CompoundId")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellSubstance")).findFirst().get().getValue()).isEqualTo("Filler");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Feature 1")).findFirst().get().getValue()).isEqualTo("1.11");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Feature 2")).findFirst().get().getValue()).isEqualTo("5.55");

        //Values of last well
        ChartDataDTO chartDataDTOWell4 = chartDataDTOS.get(3);
        assertThat(chartDataDTOWell4.getWellId()).isEqualTo(4L);
        chartTupleDTOS = chartDataDTOWell4.getValues();
        assertThat(chartTupleDTOS).isNotNull();
        assertThat(chartTupleDTOS.size()).isEqualTo(10);
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellId")).findFirst().get().getValue()).isEqualTo("4");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("PlateId")).findFirst().get().getValue()).isEqualTo("1");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Row")).findFirst().get().getValue()).isEqualTo("2");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Column")).findFirst().get().getValue()).isEqualTo("2");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellType")).findFirst().get().getValue()).isEqualTo("Sample");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellStatus")).findFirst().get().getValue()).isEqualTo("ACCEPTED");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("CompoundId")).findFirst().get().getValue()).isEqualTo("4");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("WellSubstance")).findFirst().get().getValue()).isEqualTo("Filler");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Feature 1")).findFirst().get().getValue()).isEqualTo("4.44");
        assertThat(chartTupleDTOS.stream().filter(chartTupleDTO -> chartTupleDTO.getName().equals("Feature 2")).findFirst().get().getValue()).isEqualTo("8.88");
    }
}
