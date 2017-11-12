package domain;

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

    /**
     * Порядок ансамбля.
     */
    protected int order;

    public Ensemble(TimeSeries timeSeries) {
        super(timeSeries, 0);
        models = new ArrayList<Model>();
    }

    /**
     * Добавить модель.
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
     * @param model модель.
     */
    public void removeModel(Model model) {
        models.remove(model);
    }

    /**
     * Обучить ансамблевую модель.
     */
    public void fit() {
        for (Model model : models) {
            if (!model.isFit()) {
                model.fit();
            }
        }
        fitMetaAlgorithm();
    }

    /**
     * Обучить метаалгоритм ансамблевой модели.
     */
    protected abstract void fitMetaAlgorithm();

    /**
     * Предсказать по обученному метаалгоритму ансамблевой модели.
     * @param labels прогнозы моделей низжшего уровня.
     * @return прогноз.
     */
    //protected abstract double forecast(double[] labels);

    /**
     * Сформировать список прогнозов моделей низшего уровня.
     * @return список прогнозов моделей низшего уровня.
     */
    private List<double[]> calculateInputs() {
        List<double[]> inputs = new ArrayList<double[]>();
        for (int t = order + 1; t < timeSeries.getSize(); ++t) {
            double[] labels = calculateLabels(t);
            inputs.add(labels);
        }
        return inputs;
    }

    /**
     * Рассчитать прогнозы моделей низшего уровня.
     * @param t метка времени предсказываемого значения.
     * @return прогнозы моделей низшего уровня.
     */
    private double[] calculateLabels(int t) {
        double[] labels = new double[models.size()];
        int j = -1;
        for (Model model : models) {
            labels[++j] = model.forecast(t);
        }
        return labels;
    }
}
