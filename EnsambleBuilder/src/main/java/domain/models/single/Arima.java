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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Arima extends Model {
    private ArimaModel fittedModel;
    private final int p;
    private static final int d = 1;
    private static final int q = 0;
    private static final int P = 0;
    private static final int D = 0;
    private static final int Q = 0;
    private static final int m = 0;

    public Arima(TimeSeries timeSeries, int order, int testPercent) throws InvalidOrderException {
        super(timeSeries, order, testPercent);
        p = order;
    }

    public void fit() {
        double[] data = getTrainingData();
        ArimaParams paramsForecast = new ArimaParams(p, d, q, P, D, Q, m);
        fittedModel = ArimaSolver.estimateARIMA(paramsForecast, data, data.length, data.length + 1);
        setFit();
    }

    public double forecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        EnableForForecasting(t);
        double[] values = new double[order];
        for (int i = 0; i < order; i++) {
            values[i] = timeSeries.getTimeValue(t + i - order);
        }
        return fittedModel.getParams().forecastOnePointARMA(values, new double[0], order) * (-1);
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
