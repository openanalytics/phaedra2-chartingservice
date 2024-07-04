package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.enumeration.AxisFieldType;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.model.TrendChartData;
import eu.openanalytics.phaedra.chartingservice.service.ChartDataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/charts")
public class ChartController {

    private final ChartDataService chartDataService;

    public ChartController(ChartDataService chartDataService) {
        this.chartDataService = chartDataService;
    }

    @GetMapping("/scatter")
    public Map<String, ChartData> getScatterData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                                 @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                                 @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType,
                                                 @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {

        return chartDataService.getScatterPlotData(plateId, protocolId, xFieldName, xFieldType, yFieldName, yFieldType, groupBy);
    }

    @GetMapping("/box")
    public Map<String, ChartData> getBoxPlotData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                                 @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType,
                                                 @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        return chartDataService.getBoxPlotData(plateId, protocolId, yFieldName, yFieldType, groupBy);
    }

    @GetMapping("/histogram")
    public Map<String, ChartData> getHistogramData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                                   @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                                   @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        return chartDataService.getHistogramPlotData(plateId, protocolId, xFieldName, xFieldType, groupBy);
    }

    @GetMapping("plate_trend")
    public List<TrendChartData> getTrendChartData(@RequestParam Long experimentId) {
        return chartDataService.getTrendChartData(experimentId);
    }
}