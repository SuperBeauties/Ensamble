package domain.models.single;

import com.workday.insights.timeseries.arima.ArimaSolver;
import com.workday.insights.timeseries.arima.struct.ArimaModel;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Arima extends Model {
    private ArimaModel fittedModel;
    private TimeSeries timeSeriesErrors;
    private final int d;
    private final int q;
    private static final int P = 0;
    private static final int D = 0;
    private static final int Q = 0;
    private static final int m = 0;

    public Arima(TimeSeries timeSeries, int order, int forecastCount, int trainPercent, int testPercent, int d, int q) throws InvalidOrderException {
        super(timeSeries, order, forecastCount, trainPercent, testPercent);
        this.d = d;
        this.q = q;
        timeSeriesErrors = new TimeSeries();
        for (int i = 0; i < order; ++i) {
            timeSeriesErrors.addTimeValue(0);
        }
    }

    public void fit() {
        double[] data = getTrainingData();
        ArimaParams paramsForecast = new ArimaParams(order, d, q, P, D, Q, m);
        fittedModel = ArimaSolver.estimateARIMA(paramsForecast, data, data.length, data.length + 1);
        predict();
        setFit();

        double[] errors = new double[q];
        for (int i = 0; i < q; ++i) {
            errors[i] = 0;
        }
        for (int i = order + 1; i <= timeSeries.getSize(); ++i) {
            double forecast = fittedModel.getParams().forecastOnePointARMA(getDataForForecast(i), errors, order) * (-1);
            double value = timeSeries.getTimeValue(i) - forecast;
            timeSeriesErrors.addTimeValue(value);
        }
    }

    public double forecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        EnableForForecasting(t);
        double[] values = getDataForForecast(t);
        double[] errors = getErrorsForForecast(t);
        return fittedModel.getParams().forecastOnePointARMA(values, errors, order) * (-1);
    }

    /**
     * Сформировать данные для прогноза.
     *
     * @param t временная метка прогноза.
     * @return данные для прогноза.
     */
    private double[] getDataForForecast(int t) {
        double[] values = new double[order];
        for (int i = 0; i < order; i++) {
            values[i] = timeSeries.getTimeValue(t + i - order);
        }
        return values;
    }

    /**
     * Сформировать данные об ошибках прогноза.
     *
     * @param t временная метка прогноза.
     * @return ошибки прогноза.
     */
    private double[] getErrorsForForecast(int t) {
        double[] values = new double[order];
        for (int i = 0; i < order; i++) {
            values[i] = timeSeriesErrors.getTimeValue(t + i - order);
        }
        return values;
    }

    /**
     * Расчет прогноза заданной длины.
     */
    private void predict() {
        forecast = fittedModel.forecast(forecastCount).getForecast();
        System.out.println(Arrays.toString(forecast));
    }

    /**
     * Создание обучающей выборки.
     *
     * @return обучающая выборка.
     */
    @NotNull
    private double[] getTrainingData() {
        int size = timeSeriesTrain.getSize();
        double[] trainingData = new double[size];
        for (int i = 0; i < size; ++i) {
            trainingData[i] = timeSeriesTrain.getTimeValue(i + 1);
        }
        return trainingData;
    }
}
