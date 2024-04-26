package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.enumeration.AxisFieldType;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.service.ChartDataService;
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

        return chartDataService.getScatterPlot(plateId, protocolId, xFieldName, xFieldType, yFieldName, yFieldType, groupBy);
    }

    @GetMapping("/box")
    public Map<String, ChartData> getBoxPlotData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                                 @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType,
                                                 @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        return chartDataService.getBoxPlot(plateId, protocolId, yFieldName, yFieldType, groupBy);
    }

    @GetMapping("/histogram")
    public Map<String, ChartData> getHistogramData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                                   @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                                   @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        return chartDataService.getHistogramPlot(plateId, protocolId, xFieldName, xFieldType, groupBy);
    }
}
