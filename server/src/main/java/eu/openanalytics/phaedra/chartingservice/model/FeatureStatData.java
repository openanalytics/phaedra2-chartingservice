package eu.openanalytics.phaedra.chartingservice.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FeatureStatData {
  private Long featureId;
  private String featureName;
  private String statName;
  private Float statValue;
  private String wellType;
}
