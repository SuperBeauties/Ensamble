package domain.models.ensemble;

import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.models.Quality;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class WeightedAverageEnsemble extends Ensemble {
    /**
     * Веса моделей для расчета прогнозного значения.
     */
    Map<Model, Double> weightedModels;

    public WeightedAverageEnsemble(TimeSeries timeSeries) {
        super(timeSeries);
        weightedModels = new HashMap<Model, Double>();
    }

    /**
     * Обучить метаалгоритм ансамблевой модели.
     */
    protected void fitMetaAlgorithm() {
        Map<Model, Double> modelsMapes = new HashMap<Model, Double>();
        double sumMapes = calculateMapeForModels(modelsMapes);
        for (Map.Entry<Model, Double> modelMape : modelsMapes.entrySet()) {
            double weight = calculateWeight(modelMape.getValue(), sumMapes);
            weightedModels.put(modelMape.getKey(), weight);
        }
    }

    /**
     * Предсказать по обученной ансамблевой модели.
     * @param t метка времени предсказываемого значения.
     * @return прогноз.
     */
    public double forecast(int t) {
        double weightedAverage = 0;
        for (Map.Entry<Model, Double> weightedModel : weightedModels.entrySet()) {
            weightedAverage += weightedModel.getKey().forecast(t) * weightedModel.getValue();
        }
        return weightedAverage;
    }

    /**
     * Рассчитать MAPE для всех моделей.
     * @param modelsMapes MAPE для всех моделей.
     * @return сумма MAPE всех моделей.
     */
    private double calculateMapeForModels(Map<Model, Double> modelsMapes) {
        double sumMapes = 0;
        for (Model model : models) {
            double sum = 0;
            for (int t = order + 1; t < timeSeries.getSize(); ++t) {
                sum += Quality.percentError(timeSeries.getTimeValue(t), model.forecast(t));
            }

            double mape = sum / (timeSeries.getSize() - order);
            sumMapes += mape;
            modelsMapes.put(model, mape);
        }

        return sumMapes;
    }

    /**
     * Рассчитать вес модели.
     * @param mape MAPE модели.
     * @param sumMapes сумма MAPE всех моделей.
     * @return вес модели.
     */
    private double calculateWeight(double mape, double sumMapes) {
        return (1 - mape) / (models.size() - sumMapes);
    }
}
