package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.enumeration.AxisFieldType;
import eu.openanalytics.phaedra.chartingservice.exception.ChartDataException;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.service.ChartDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/charts")
public class ChartController {

    private final ChartDataService chartDataService;

    public ChartController(ChartDataService chartDataService) {
        this.chartDataService = chartDataService;
    }

    @GetMapping("/scatter")
    public ChartData getScatterData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                    @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                    @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType,
                                    @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        ChartData result = new ChartData();
        result.setXValues(chartDataService.getChartData(plateId, protocolId, xFieldName, xFieldType, groupBy));
        result.setYValues(chartDataService.getChartData(plateId, protocolId, yFieldName, yFieldType, groupBy));
        return result;
    }

    @GetMapping("/box")
    public ChartData getBoxPlotData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                    @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType,
                                    @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        ChartData result = new ChartData();
        result.setYValues(chartDataService.getChartData(plateId, protocolId, yFieldName, yFieldType, groupBy));
        return result;
    }

    @GetMapping("/histogram")
    public ChartData getHistogramData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                    @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                    @RequestParam(defaultValue = "none") String groupBy) throws ChartDataException {
        ChartData result = new ChartData();
        result.setXValues(chartDataService.getChartData(plateId, protocolId, xFieldName, xFieldType, groupBy));
        return result;
    }
}
