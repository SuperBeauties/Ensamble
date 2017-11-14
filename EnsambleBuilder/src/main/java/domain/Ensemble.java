package domain;

import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.NoEqualsTimeSeriesException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class Ensemble extends Model {
    /**
     * Список моделей составляющих ансамбль.
     */
    protected List<Model> models;

    public Ensemble(TimeSeries timeSeries) {
        super(timeSeries, 0);
        models = new ArrayList<Model>();
    }

    /**
     * Добавить модель.
     *
     * @param model модель.
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
    public void fit() throws InvalidTemporaryValueException, ForecastNotFitedModelException {
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
