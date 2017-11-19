package domain;

import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.TimeSeriesSizeException;

import javax.annotation.ParametersAreNonnullByDefault;
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
     * Временной ряд для контрольного тестирования.
     */
    protected TimeSeries timeSeriesValidate;

    /**
     * Порядок модели.
     */
    protected int order;

    /**
     * Флаг была ли модель обучена.
     */
    private boolean isFit;


    public Model(TimeSeries timeSeries, int order) {
        this.timeSeries = timeSeries;
        this.order = order;

        this.timeSeriesTrain = new TimeSeries();
        this.timeSeriesTest = new TimeSeries();
        this.timeSeriesValidate = new TimeSeries();
        partTimeSeries();
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
        return mape(timeSeriesTrain);
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
        return mape(timeSeriesTest);
    }

    /**
     * Получить mape для обучающей выборки.
     *
     * @return mape.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     */
    public double getValidateMape() throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        return mape(timeSeriesValidate);
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
    public abstract void fit() throws InvalidTemporaryValueException, ForecastNotFitedModelException;

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
     * Получить валидационный временной ряд.
     *
     * @return валидационный временной ряд.
     */
    public TimeSeries getTimeSeriesValidate() {
        return timeSeriesValidate;
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

        if (t <= order || t > timeSeries.getSize() + 1) {
            throw new InvalidTemporaryValueException();
        }
    }

    /**
     * Поделить временной ряд.
     */
    private void partTimeSeries() {
        List<Object> keys = timeSeries.getTimeSeries().keySet().stream().collect(Collectors.toList());
        int size = keys.size();
        int trainIndex = 0;
        int testIndex = (int) (size * 0.7);
        int validateIndex = (int) (size * 0.9);
        for (Object key : keys.subList(trainIndex, testIndex)) {
            this.timeSeriesTrain.add((int) key, timeSeries.getTimeValue((int) key));
        }
        for (Object key : keys.subList(testIndex, validateIndex)) {
            this.timeSeriesTest.add((int) key, timeSeries.getTimeValue((int) key));
        }
        for (Object key : keys.subList(validateIndex, size - 1)) {
            this.timeSeriesValidate.add((int) key, timeSeries.getTimeValue((int) key));
        }
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
    private double mape(TimeSeries timeSeries) throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        TimeSeries timeSeriesFact = new TimeSeries();
        TimeSeries timeSeriesCalc = new TimeSeries();
        for (int i = order + 1; i <= timeSeries.getSize(); ++i) {
            timeSeriesFact.add(i, timeSeries.getTimeValue(i));
            timeSeriesCalc.add(i, forecast(i));
        }
        return Quality.mape(timeSeriesFact, timeSeriesCalc);
    }
}
