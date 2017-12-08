package domain;

import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.TimeSeriesSizeException;
import org.jetbrains.annotations.Contract;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public abstract class Model {
    /**
     * Временной ряд.
     */
    protected final TimeSeries timeSeries;

    /**
     * Временной ряд для обучения.
     */
    protected TimeSeries timeSeriesTrain;

    /**
     * Временной ряд для тестирования.
     */
    protected TimeSeries timeSeriesTest;

    /**
     * Горизонт прогноза.
     */
    protected int forecastCount;

    /**
     * Прогноз.
     */
    protected double[] forecast;

    /**
     * Порядок модели.
     */
    protected int order;

    /**
     * Флаг была ли модель обучена.
     */
    private boolean isFit;


    public Model(TimeSeries timeSeries, int order, int forecastCount, int trainPercent, int testPercent) throws InvalidOrderException {
        if (order >= timeSeries.getSize()) {
            throw new InvalidOrderException();
        }

        this.timeSeries = timeSeries;
        this.order = order;
        this.forecastCount = forecastCount;

        this.timeSeriesTrain = new TimeSeries();
        this.timeSeriesTest = new TimeSeries();
        partTimeSeries(trainPercent, testPercent);
    }

    /**
     * Получить mape для обучающей выборки.
     *
     * @return mape.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     */
    public double getTrainMape() throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        return mape(timeSeriesTrain, 0);
    }

    /**
     * Получить mape для обучающей выборки.
     *
     * @return mape.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     */
    public double getTestMape() throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        return mape(timeSeriesTest, timeSeriesTrain.getSize());
    }

    /**
     * Получить прогноз.
     *
     * @return прогноз.
     */
    public double[] getForecast() {
        return forecast;
    }

    /**
     * Была ли модель переобучена.
     *
     * @param border граница переобученности модели.
     * @return была ли модель переобучена.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    public boolean isOverFited(double border) throws ForecastNotFitedModelException, InvalidTemporaryValueException, TimeSeriesSizeException {
        if (!isFit()) {
            throw new ForecastNotFitedModelException();
        }
        double mapeTrain = getTrainMape();
        double mapeTest = getTestMape();
        double sMape = Quality.sMape(mapeTrain, mapeTest);
        return sMape > border;
    }

    /**
     * Обучить модель.
     */
    public abstract void fit() throws InvalidTemporaryValueException, ForecastNotFitedModelException, IOException;

    /**
     * Предсказать по обученной модели.
     *
     * @param t метка времени предсказываемого значения.
     * @return прогноз.
     */
    public abstract double forecast(int t) throws InvalidTemporaryValueException, ForecastNotFitedModelException;

    /**
     * Получить временной ряд, с которым работает модель.
     *
     * @return временной ряд.
     */
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    /**
     * Узнать обучена ли уже модель или нет.
     *
     * @return результат.
     */
    public boolean isFit() {
        return isFit;
    }

    /**
     * Проставить флаг обученности на модель.
     */
    public void setFit() {
        isFit = true;
    }

    /**
     * Получить порядок модели.
     *
     * @return порядок модели.
     */
    public int getOrder() {
        return order;
    }

    /**
     * Получить обучающий временной ряд.
     *
     * @return обучающий временной ряд.
     */
    public TimeSeries getTimeSeriesTrain() {
        return timeSeriesTrain;
    }

    /**
     * Получить тестовый временной ряд.
     *
     * @return тестовый временной ряд.
     */
    public TimeSeries getTimeSeriesTest() {
        return timeSeriesTest;
    }

    /**
     * Выброс исключений в случае невозможности сделать предсказание.
     *
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     */
    protected void EnableForForecasting(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        if (!isFit()) {
            throw new ForecastNotFitedModelException();
        }

        if (t > timeSeries.getSize() + 1) {
            throw new InvalidTemporaryValueException();
        }
    }

    /**
     * Поделить временной ряд.
     */
    private void partTimeSeries(int trainPercent, int testPercent) {
        List<Object> keys = timeSeries.getTimeSeries().keySet().stream().collect(Collectors.toList());
        int size = keys.size();
        int trainSize = partSize(size, trainPercent);
        int testSize = partSize(size, testPercent);
        int end = trainSize + testSize;
        for (Object key : keys.subList(0, trainSize)) {
            this.timeSeriesTrain.addTimeValue(timeSeries.getTimeValue((int) key));
        }
        for (Object key : keys.subList(trainSize, end)) {
            this.timeSeriesTest.addTimeValue(timeSeries.getTimeValue((int) key));
        }
    }

    /**
     * Расчитать длину обучающего временного ряда.
     *
     * @param size    длина временного ряда.
     * @param percent процент тестовой выборки.
     * @return размер обучающей выборки.
     */
    @Contract(pure = true)
    private int partSize(int size, int percent) {
        return (int) (size * ((double) percent / 100));
    }

    /**
     * Рассчитать mape для модели.
     *
     * @param timeSeries временной ряд.
     * @return mape.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    private double mape(TimeSeries timeSeries, int k) throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        TimeSeries timeSeriesFact = new TimeSeries();
        TimeSeries timeSeriesCalc = new TimeSeries();
        for (int i = 1; i <= timeSeries.getSize(); ++i) {
            timeSeriesFact.add(i, timeSeries.getTimeValue(i));
            timeSeriesCalc.add(i, forecast(i + k));
        }
        return Quality.mape(timeSeriesFact, timeSeriesCalc);
    }
}
