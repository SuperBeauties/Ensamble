package domain;

import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;

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
        this.timeSeriesTrain = new TimeSeries();
        this.timeSeriesTest = new TimeSeries();
        this.timeSeriesValidate = new TimeSeries();
        partTimeSeries(timeSeries);

        this.timeSeries = timeSeries;
        this.order = order;
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

    private void partTimeSeries(TimeSeries timeSeries) {
        List<Object> values = timeSeries.getTimeSeries().values().stream().collect(Collectors.toList());
        int size = values.size();
        int trainIndex = 0;
        int testIndex = (int) (size * 0.7);
        int validateIndex = (int) (size * 0.9);
        for (Object value : values.subList(trainIndex, testIndex)) {
            this.timeSeriesTrain.addTimeValue((Double) value);
        }
        for (Object value : values.subList(testIndex, validateIndex)) {
            this.timeSeriesTest.addTimeValue((Double) value);
        }
        for (Object value : values.subList(validateIndex, size - 1)) {
            this.timeSeriesValidate.addTimeValue((Double) value);
        }
    }
}
