package domain.models.single;

import com.workday.insights.timeseries.arima.ArimaSolver;
import com.workday.insights.timeseries.arima.struct.ArimaModel;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import org.junit.Test;

import java.util.Arrays;


public class ArimaTest {
    @Test
    public void ShouldPredict()  {
//        ArimaParams paramsForecast = new ArimaParams(2, 0, 0, 0, 0, 0, 0);
//        double[] data = new double[]{576.229877206145,
//        559.260840956339,
//        732.576319190309,
//        836.994312521585,
//        861.603196854723,
//        580.481933408632,
//        576.229877206145,
//        559.260840956339,
//        732.576319190309,
//        836.994312521585,
//        861.603196854723,
//        580.481933408632};
//        ForecastResult result = ArimaSolver.forecastARIMA(paramsForecast, data, data.length, data.length + 5);
//        System.out.println(Arrays.toString(result.getForecast()));
//        System.out.println(Arrays.toString(result.getForecastLowerConf()));
//        System.out.println(Arrays.toString(result.getForecastUpperConf()));
//        System.out.println(result.getRMSE());
//
//        ArimaModel arimaModel = ArimaSolver.estimateARIMA(paramsForecast, data, data.length, data.length + 1);
//        System.out.println(Arrays.toString(arimaModel.getParams().getCurrentARCoefficients()));
//        System.out.println(Arrays.toString(arimaModel.getParams().getCurrentMACoefficients()));
//        System.out.println(arimaModel.getParams().forecastOnePointARMA(new double[]{576, 559}, new double[0], 2));
        double[] v = new double[3];
        v[0] = 1;
        System.out.println(v[1]);

    }

}