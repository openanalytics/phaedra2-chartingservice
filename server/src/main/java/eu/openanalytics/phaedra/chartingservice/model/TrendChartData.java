package eu.openanalytics.phaedra.chartingservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TrendChartData {
  private long plateId;
  private String barcode;
  private List<FeatureStatData> featureStats;
}
