package domain.models.single;

import com.workday.insights.timeseries.arima.ArimaSolver;
import com.workday.insights.timeseries.arima.struct.ArimaModel;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ArimaTest {
    @Test
    public void ShouldForecastByArima() {
        final int p = 2;
        final int d = 1;
        final int q = 0;
        final int P = 0;
        final int D = 0;
        final int Q = 0;
        final int m = 0;
        final ArimaParams paramsForecast = new ArimaParams(p, d, q, P, D, Q, m);
        final ArimaParams paramsXValidation = new ArimaParams(p, d, q, P, D, Q, m);
        double[] data = new double[] {576, 559, 732, 836, 861, 580, 816, 679, 1001, 652, 483, 768, 555};
        // estimate ARIMA model parameters for forecasting
        final ArimaModel fittedModel = ArimaSolver.estimateARIMA(
                paramsForecast, data, data.length, data.length + 1);
        System.out.println(Arrays.toString(fittedModel.getParams().getCurrentARCoefficients()));
        System.out.println(Arrays.toString(fittedModel.getParams().getCurrentMACoefficients()));
        System.out.println(Arrays.toString(fittedModel.forecast(5).getForecast()));
        System.out.println(fittedModel.getParams().forecastOnePointARMA(new double[] {576, 559}, new double[0], 2) * -1);
    }
}