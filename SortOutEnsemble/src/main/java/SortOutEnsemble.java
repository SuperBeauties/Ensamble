import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.NoEqualsTimeSeriesException;
import domain.exceptions.TimeSeriesSizeException;
import domain.models.ensemble.NeuralEnsemble;
import domain.models.ensemble.WeightedAverageEnsemble;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ParametersAreNonnullByDefault
public class SortOutEnsemble {
    /**
     * Временной ряд.
     */
    private final TimeSeries timeSeries;
    /**
     * Порядок нечеткой модели.
     */
    private final int orderFuzzy;
    /**
     * Порядок модели нейронной сети.
     */
    private final int orderNeural;
    /**
     * Порядок авторегрессионной модели.
     */
    private final int orderArima;
    /**
     * Порог качества модели.
     */
    private final double qualityBorder;
    /**
     * Порог переобученности модели.
     */
    private final double overFitedBorder;
    /**
     * Необходимо ли добавлять в ансамбль авторегрессионную модель.
     */
    private final boolean needArima;
    /**
     * Необходимо ли добавлять в ансамбль нейронную модель.
     */
    private final boolean needNeural;
    /**
     * Необходимо ли добавлять в ансамбль нечеткую модель.
     */
    private final boolean needFuzzy;
    /**
     * Необходимо ли строить нейронный ансамбль.
     */
    private final boolean needNeuralEnsemble;
    /**
     * Необходимо ли строить средневзвешенный ансамбль.
     */
    private final boolean needWeightedEnsemble;

    private final static int TEST_PERSENT = 22;

    public SortOutEnsemble(
            TimeSeries timeSeries,
            boolean needArima,
            boolean needNeural,
            boolean needFuzzy,
            double qualityBorder,
            double overFitedBorder,
            int orderArima,
            int orderNeural,
            int orderFuzzy,
            boolean needNeuralEnsemble,
            boolean needWightedEnsemble) {

        this.timeSeries = timeSeries;
        this.orderArima = orderArima;
        this.orderNeural = orderNeural;
        this.orderFuzzy = orderFuzzy;
        this.qualityBorder = qualityBorder;
        this.overFitedBorder = overFitedBorder;
        this.needArima = needArima;
        this.needNeural = needNeural;
        this.needFuzzy = needFuzzy;
        this.needNeuralEnsemble = needNeuralEnsemble;
        this.needWeightedEnsemble = needWightedEnsemble;
    }

    /**
     * Перебор моделей для создания ансамблей.
     *
     * @param weighted список средневзвешенных ансамблей.
     * @param neural   список нейронных ансамблей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws NoEqualsTimeSeriesException    разные временные ряды в моделях.
     */
    public void sortOut(List<Ensemble> weighted, List<Ensemble> neural) throws InvalidTemporaryValueException, ForecastNotFitedModelException, NoEqualsTimeSeriesException, TimeSeriesSizeException {
        List<Model> empty = new ArrayList<>(0);
        List<Model> modelsArima = (!needArima) ? empty : fitedModels(Models.ARIMA, orderArima);
        List<Model> modelsNeural = (!needNeural) ? empty : fitedModels(Models.NEURAL, orderNeural);
        List<Model> modelsFuzzy = (!needFuzzy) ? empty : fitedModels(Models.FUZZY, orderFuzzy);

        List<Model> allModels = new ArrayList<>();
        allModels.addAll(modelsArima);
        allModels.addAll(modelsNeural);
        allModels.addAll(modelsFuzzy);

        createEnsembleLists(allModels, weighted, neural);
        selectEnsembles(weighted);
        selectEnsembles(neural);
    }

    /**
     * Созадание списка обученных моделей.
     *
     * @param model тип модели.
     * @param order порядок модели.
     * @return список обученных моделей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     */
    private List<Model> fitedModels(Models model, int order) throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        List<Model> models = new ArrayList<>(order);
        for (int i = 1; i <= order; ++i) {
            Model creation = createModel(model, i);
            creation.fit();
            models.add(creation);
        }
        return models;
    }

    /**
     * Создание модели.
     *
     * @param model тип модели.
     * @param order порядок модели.
     * @return модель.
     */
    @NotNull
    private Model createModel(Models model, int order) {
        switch (model) {
            case ARIMA: {
                return new Arima(timeSeries, order, TEST_PERSENT);
            }
            case NEURAL: {
                return new Neural(timeSeries, order, TEST_PERSENT);
            }
            case FUZZY: {
                return new Fuzzy(timeSeries, order, TEST_PERSENT);
            }
        }
        return null;
    }

    /**
     * Создание списка ансамблей.
     *
     * @param allModels список моделей.
     * @param weighted  список средневзвешенных ансамблей.
     * @param neural    список нейронных ансамблей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws NoEqualsTimeSeriesException    разные временные ряды в моделях.
     */
    private void createEnsembleLists(List<Model> allModels, List<Ensemble> weighted, List<Ensemble> neural) throws NoEqualsTimeSeriesException, InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        List<String> tableSortOut = tableSortOut(allModels.size());
        for (String rowSortOut : tableSortOut) {
            Ensemble ensembleWeighted = new WeightedAverageEnsemble(timeSeries, TEST_PERSENT);
            Ensemble ensembleNeural = new NeuralEnsemble(timeSeries, TEST_PERSENT);
            createEnsemble(rowSortOut, allModels, ensembleWeighted, ensembleNeural);
            if (ensembleWeighted != null && ensembleNeural != null) {
                if (needWeightedEnsemble) {
                    ensembleWeighted.fit();
                    weighted.add(ensembleWeighted);
                }
                if (needNeuralEnsemble) {
                    ensembleNeural.fit();
                    neural.add(ensembleNeural);
                }
            }
        }
    }

    /**
     * Создать таблицу перебора моделей.
     *
     * @param size количество моделей.
     * @return таблица перебора моделей
     */
    private List<String> tableSortOut(int size) {
        size = (int) Math.pow(2, size);
        List<String> tableSortOut = new ArrayList<>(size);
        for (int i = 1; i <= size; ++i) {
            String rowSortOut = Integer.toBinaryString(i);
            tableSortOut.add(rowSortOut);
        }
        return tableSortOut;
    }

    /**
     * Создание ансамбля.
     *
     * @param row      строка таблицы перебора моделей.
     * @param models   список моделей.
     * @param weighted список средневзвешенных ансамблей.
     * @param neural   список нейронных ансамблей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws NoEqualsTimeSeriesException    разные временные ряды в моделях.
     */
    @Nullable
    private void createEnsemble(String row, List<Model> models, Ensemble weighted, Ensemble neural) throws NoEqualsTimeSeriesException, TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        int size = models.size();
        int modelsCount = 0;
        for (int i = 0; i < row.length(); ++i) {
            if (row.charAt(i) == '1') {
                Model model = models.get(size - i - 1);
                if (!model.isOverFited(overFitedBorder)) {
                    weighted.addModel(model);
                    neural.addModel(model);
                    ++modelsCount;
                }
            }
        }
        if (modelsCount <= 1) {
            weighted = null;
            neural = null;
        }
    }

    /**
     * Выбор ансамблей, отвечающих требованиям качества.
     *
     * @param ensembles список ансамблей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    private void selectEnsembles(List<Ensemble> ensembles) throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        Iterator<Ensemble> ensembleIterator = ensembles.iterator();
        while (ensembleIterator.hasNext()) {
            Ensemble ensemble = ensembleIterator.next();
            if (ensemble.getTestMape() > qualityBorder) {
                ensembleIterator.remove();
            }
        }
    }

    enum Models {
        ARIMA,
        NEURAL,
        FUZZY
    }
}
