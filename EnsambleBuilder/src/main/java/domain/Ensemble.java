package domain;

import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.NoEqualsTimeSeriesException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class Ensemble extends Model {


    /**
     * Список моделей составляющих ансамбль.
     */
    protected List<Model> models;

    public Ensemble(TimeSeries timeSeries, int forecastCount, int trainPercent, int testPercent) throws InvalidOrderException {
        super(timeSeries, 1, forecastCount, trainPercent, testPercent);
        models = new ArrayList<Model>();
    }

    /**
     * Получить список моделей ансамбля.
     *
     * @return список моделей ансамбля.
     */
    public List<Model> getModels() {
        return models;
    }

    /**
     * Добавить модель.
     *
     * @param model модель.
     * @throws NoEqualsTimeSeriesException разные временные ряды в моделях.
     */
    public void addModel(Model model) throws NoEqualsTimeSeriesException {
        if (!timeSeries.equals(model.getTimeSeries())) {
            throw new NoEqualsTimeSeriesException();
        }

        order = (order < model.getOrder()) ? model.getOrder() : order;
        models.add(model);
    }

    /**
     * Удалить модель.
     *
     * @param model модель.
     */
    public void removeModel(Model model) {
        models.remove(model);
    }

    /**
     * Обучить ансамблевую модель.
     */
    public void fit() throws InvalidTemporaryValueException, ForecastNotFitedModelException, IOException {
        for (Model model : models) {
            if (!model.isFit()) {
                model.fit();
            }
        }
        fitMetaAlgorithm();
        setFit();
    }

    /**
     * Обучить метаалгоритм ансамблевой модели.
     */
    protected abstract void fitMetaAlgorithm() throws InvalidTemporaryValueException, ForecastNotFitedModelException;

}
