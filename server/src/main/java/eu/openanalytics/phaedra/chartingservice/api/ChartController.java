package eu.openanalytics.phaedra.chartingservice.api;

import eu.openanalytics.phaedra.chartingservice.enumeration.AxisFieldType;
import eu.openanalytics.phaedra.chartingservice.model.ChartData;
import eu.openanalytics.phaedra.chartingservice.service.ChartDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/charts")
public class ChartController {

    @Autowired
    private ChartDataService chartDataService;

    public ChartData getScatterData(@RequestParam Long plateId, @RequestParam(required = false) Long protocolId,
                                    @RequestParam String xFieldName, @RequestParam AxisFieldType xFieldType,
                                    @RequestParam String yFieldName, @RequestParam AxisFieldType yFieldType) {
        ChartData result = new ChartData();
        result.setXValues(chartDataService.getChartData(plateId, protocolId, xFieldName, xFieldType));
        result.setXValues(chartDataService.getChartData(plateId, protocolId, yFieldName, yFieldType));
        return result;
    }
}
