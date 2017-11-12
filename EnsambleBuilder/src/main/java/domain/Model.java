package domain;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class Model {
    /**
     * Временной ряд.
     */
    protected final TimeSeries timeSeries;

    /**
     * Порядок модели.
     */
    protected final int order;

    /**
     * Флаг была ли модель обучена.
     */
    private boolean isFit;


    public Model(TimeSeries timeSeries, int order) {
        this.timeSeries = timeSeries;
        this.order = order;
    }

    /**
     * Обучить модель.
     */
    public abstract void fit();

    /**
     * Предсказать по обученной модели.
     * @param t метка времени предсказываемого значения.
     * @return прогноз.
     */
    public abstract double forecast(int t);

    /**
     * Получить временной ряд, с которым работает модель.
     * @return временной ряд.
     */
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    /**
     * Узнать обучена ли уже модель или нет.
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
     * @return порядок модели.
     */
    public int getOrder() {
        return order;
    }
}
