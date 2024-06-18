/**
 * Phaedra II
 * <p>
 * Copyright (C) 2016-2024 Open Analytics
 * <p>
 * ===========================================================================
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * Apache License as published by The Apache Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Apache
 * License for more details.
 * <p>
 * You should have received a copy of the Apache License along with this program.  If not, see
 * <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.chartingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartTupleDTO {

  public static String WELL_ID = "WellId";
  public static String WELL_NR = "WellNr";
  public static String WELL_ROW = "WellRow";
  public static String WELL_COLUMN = "WellColumn";
  public static String WELL_TYPE = "WellType";
  public static String WELL_STATUS = "WellStatus";

  private String name;
  private String value;
}
