package domain.models.single;

import com.workday.insights.matrix.InsightsVector;
import com.workday.insights.timeseries.arima.ArimaSolver;
import com.workday.insights.timeseries.arima.struct.ArimaModel;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.BackShift;
import com.workday.insights.timeseries.timeseriesutil.Integrator;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Arima extends Model {
    private ArimaModel fittedModel;
    private final int d;
    private final int q;
    private static final int P = 0;
    private static final int D = 0;
    private static final int Q = 0;
    private static final int m = 0;
    private TimeSeries resultTimeSeries;
    private double[] data_stationary;
    private ArimaParams paramsForecast;

    private final BackShift _opAR;
    private final BackShift _opMA;
    private double mean_stationary;

    public Arima(TimeSeries timeSeries, int order, int forecastCount, int trainPercent, int testPercent, int diff, int errOrder) throws InvalidOrderException {
        super(timeSeries, order, forecastCount, trainPercent, testPercent);
        this.d = diff;
        this.q = errOrder;

        this._opAR = getNewOperatorAR();
        this._opMA = getNewOperatorMA();
        _opAR.initializeParams(false);
        _opMA.initializeParams(false);

        resultTimeSeries = new TimeSeries();

        setDifferentiateTimeSeries();
    }

    public void fit() throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        double[] data = getTrainingData(timeSeriesTrain);
        paramsForecast = new ArimaParams(order, d, q, P, D, Q, m);
        fittedModel = ArimaSolver.estimateARIMA(paramsForecast, data, data.length, data.length + 1);
        setParamsFromVector(fittedModel.getParams().getParamsIntoVector());
        setFit();
        forecast();
        predict();
    }

    public double forecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        EnableForForecasting(t);
        return resultTimeSeries.getTimeValue(t);
    }

    private void forecast() throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        final double[] forecast_stationary = new double[timeSeries.getSize()];
        for (int t = 1; t <= timeSeries.getSize(); ++t) {
            forecast_stationary[t - 1] = forecastValue(t);
        }
        //=========== UN-CENTERING =================
        Integrator.shift(forecast_stationary, mean_stationary);
        //==========================================

        //===========================================
        // INTEGRATE
        double[] forecast_merged = integrate(fittedModel.getParams(), forecast_stationary, false,
                false);

        for (int t = 0; t < forecast_merged.length; ++t) {
            resultTimeSeries.add(t + 1, forecast_merged[t]);
        }
    }

    /**
     * Прогнозирование значения.
     *
     * @param t метка времени прогнозируемого значения.
     * @return значение.
     */
    private double forecastValue(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        double[] values = getDataForForecast(t, order);
        double[] errors = getErrorsForForecast(t);
        //double[] valuesP = fittedModel.getParams().getCurrentARCoefficients();
        //double[] errorsP = fittedModel.getParams().getCurrentMACoefficients();
        final double estimateAR = _opAR.getLinearCombinationFrom(values, order);
        final double estimateMA = _opMA.getLinearCombinationFrom(errors, q);
        final double forecastValue = estimateAR + estimateMA;
        return Math.abs(forecastValue);
    }

    /**
     * Интегрирование временного ряда.
     */
    private void setDifferentiateTimeSeries() {
        double[] data = getTrainingData(timeSeries);
        final double[] data_train = new double[timeSeries.getSize()];
        System.arraycopy(data, 0, data_train, 0, timeSeries.getSize());

        //=======================================
        // DIFFERENTIATE
        final boolean hasSeasonalI = false;
        final boolean hasNonSeasonalI = false;
        data_stationary = differentiate(paramsForecast, data_train, hasSeasonalI,
                hasNonSeasonalI);  // currently un-centered
        // END OF DIFFERENTIATE
        //==========================================

        //=========== CENTERING ====================
        mean_stationary = Integrator.computeMean(data_stationary);
        Integrator.shift(data_stationary, (-1) * mean_stationary);
    }

    /**
     * Получить порядок интегрирования.
     *
     * @return порядок интегрирования.
     */
    public int getDiff() {
        return d;
    }

    /**
     * Получить порядок скользящего среднего.
     *
     * @return порядок скользящего среднего.
     */
    public int getErrOrder() {
        return q;
    }

//    /**
//     * Рассчитать значение части прогноза.
//     *
//     * @param values значения ряда.
//     * @param coefs  коэффициенты.
//     * @return значение.
//     */
//    @Contract(pure = true)
//    private double calcPart(double[] values, double[] coefs) {
//        if (coefs.length == 0) {
//            return 0.0;
//        }
//        double sum = coefs[0];
//        for (int i = 0; i < values.length; ++i) {
//            sum += values[i] * coefs[i + 1];
//        }
//        return sum;
//    }

    /**
     * Сформировать данные для прогноза.
     *
     * @param t временная метка прогноза.
     * @return данные для прогноза.
     */
    private double[] getDataForForecast(int t, int order) {
        double[] values = new double[order];
        if (order >= t) {
            for (int i = 0; i < order; i++) {
                values[i] = data_stationary[i];
            }
        } else {
            for (int i = 0; i < order; i++) {
                values[i] = data_stationary[t + i - order - 1];
            }
        }
        return values;
    }

    /**
     * Сформировать данные для прогноза.
     *
     * @param t временная метка прогноза.
     * @return данные для прогноза.
     */
    private double[] getErrorsForForecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        double[] values = getDataForForecast(t, q);
        double[] errors = new double[q];
        for (int i = 0; i < q; i++) {
            int _t = t + i - q;
            if (_t < order + 1 || _t < q + 1) {
                errors[i] = 0;
            } else {
                errors[i] = values[i] - forecastValue(_t);
            }
        }
        return errors;
    }

    /**
     * Расчет прогноза заданной длины.
     */
    private void predict() {
        double[] data = getTrainingData(timeSeriesTrain);
        ArimaParams paramsForecast = new ArimaParams(order, d, q, P, D, Q, m);
        forecast = com.workday.insights.timeseries.arima.Arima.forecast_arima(data, forecastCount, paramsForecast).getForecast();
        System.out.println(Arrays.toString(forecast));
    }

    /**
     * Создание обучающей выборки.
     *
     * @return обучающая выборка.
     */
    @NotNull
    private double[] getTrainingData(TimeSeries timeSeries) {
        int size = timeSeries.getSize();
        double[] trainingData = new double[size];
        for (int i = 0; i < size; ++i) {
            trainingData[i] = timeSeries.getTimeValue(i + 1);
        }
        return trainingData;
    }

    /**
     * Differentiate procedures for forecast and estimate ARIMA.
     *
     * @param params          ARIMA parameters
     * @param trainingData    training data
     * @param hasSeasonalI    has seasonal I or not based on the parameter
     * @param hasNonSeasonalI has NonseasonalI or not based on the parameter
     * @return stationary data
     */
    private static double[] differentiate(ArimaParams params, double[] trainingData,
                                          boolean hasSeasonalI, boolean hasNonSeasonalI) {
        double[] dataStationary;  // currently un-centered
        if (hasSeasonalI && hasNonSeasonalI) {
            params.differentiateSeasonal(trainingData);
            params.differentiateNonSeasonal(params.getLastDifferenceSeasonal());
            dataStationary = params.getLastDifferenceNonSeasonal();
        } else if (hasSeasonalI) {
            params.differentiateSeasonal(trainingData);
            dataStationary = params.getLastDifferenceSeasonal();
        } else if (hasNonSeasonalI) {
            params.differentiateNonSeasonal(trainingData);
            dataStationary = params.getLastDifferenceNonSeasonal();
        } else {
            dataStationary = new double[trainingData.length];
            System.arraycopy(trainingData, 0, dataStationary, 0, trainingData.length);
        }

        return dataStationary;
    }

    public BackShift getNewOperatorAR() {
        return mergeSeasonalWithNonSeasonal(order, P, m);
    }

    public BackShift getNewOperatorMA() {
        return mergeSeasonalWithNonSeasonal(q, Q, m);
    }

    private BackShift mergeSeasonalWithNonSeasonal(int nonSeasonalLag, int seasonalLag,
                                                   int seasonalStep) {
        final BackShift nonSeasonal = new BackShift(nonSeasonalLag, true);
        final BackShift seasonal = new BackShift(seasonalLag * seasonalStep, false);
        for (int s = 1; s <= seasonalLag; ++s) {
            seasonal.setIndex(s * seasonalStep, true);
        }
        final BackShift merged = seasonal.apply(nonSeasonal);
        return merged;
    }

    /**
     * Setting parameters from a Insight Vector
     * <p>
     * It is assumed that the input vector has _np + _nq entries first _np entries are AR-parameters
     * and the last _nq entries are MA-parameters
     *
     * @param paramVec a vector of parameters
     */
    public void setParamsFromVector(final InsightsVector paramVec) {
        int index = 0;
        final int[] offsetsAR = getOffsetsAR();
        final int[] offsetsMA = getOffsetsMA();
        for (int pIdx : offsetsAR) {
            _opAR.setParam(pIdx, paramVec.get(index++));
        }
        for (int qIdx : offsetsMA) {
            _opMA.setParam(qIdx, paramVec.get(index++));
        }
    }

    /**
     * Getter for the parameter offsets of AR
     *
     * @return parameter offsets of AR
     */
    public int[] getOffsetsAR() {
        return _opAR.paramOffsets();
    }

    /**
     * Getter for the parameter offsets of MA
     *
     * @return parameter offsets of MA
     */
    public int[] getOffsetsMA() {
        return _opMA.paramOffsets();
    }

    /**
     * Differentiate procedures for forecast and estimate ARIMA.
     *
     * @param params ARIMA parameters
     * @param dataForecastStationary stationary forecast data
     * @param hasSeasonalI has seasonal I or not based on the parameter
     * @param hasNonSeasonalI has NonseasonalI or not based on the parameter
     * @return merged forecast data
     */
    private static double[] integrate(ArimaParams params, double[] dataForecastStationary,
                                      boolean hasSeasonalI, boolean hasNonSeasonalI) {
        double[] forecast_merged;
        if (hasSeasonalI && hasNonSeasonalI) {
            params.integrateSeasonal(dataForecastStationary);
            params.integrateNonSeasonal(params.getLastIntegrateSeasonal());
            forecast_merged = params.getLastIntegrateNonSeasonal();
        } else if (hasSeasonalI) {
            params.integrateSeasonal(dataForecastStationary);
            forecast_merged = params.getLastIntegrateSeasonal();
        } else if (hasNonSeasonalI) {
            params.integrateNonSeasonal(dataForecastStationary);
            forecast_merged = params.getLastIntegrateNonSeasonal();
        } else {
            forecast_merged = new double[dataForecastStationary.length];
            System.arraycopy(dataForecastStationary, 0, forecast_merged, 0,
                    dataForecastStationary.length);
        }

        return forecast_merged;
    }

}
