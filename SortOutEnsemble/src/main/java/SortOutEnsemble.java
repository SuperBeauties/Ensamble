import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.*;
import domain.models.ensemble.NeuralEnsemble;
import domain.models.ensemble.WeightedAverageEnsemble;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
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
    /**
     * Горизонт прогноза.
     */
    private final int forecastCount;
    /**
     * Является ли временной ряд стационарным.
     */
    private final boolean isStationary;

    private final static int TRAIN_PERSENT = 70;
    private final static int TEST_PERSENT = 20;

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
            boolean needWightedEnsemble,
            int forecastCount,
            boolean isStationary) {

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
        this.forecastCount = forecastCount;
        this.isStationary = isStationary;
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
    public void sortOut(List<Model> allModels, List<Ensemble> weighted, List<Ensemble> neural) throws InvalidTemporaryValueException, ForecastNotFitedModelException, NoEqualsTimeSeriesException, TimeSeriesSizeException, IOException, InvalidOrderException {
        List<Model> empty = new ArrayList<>(0);
        System.out.println("Обучение автономных моделей");
        List<Model> modelsArima = (!needArima) ? empty : fitedModels(Models.ARIMA, orderArima);
        List<Model> modelsNeural = (!needNeural) ? empty : fitedModels(Models.NEURAL, orderNeural);
        List<Model> modelsFuzzy = (!needFuzzy) ? empty : fitedModels(Models.FUZZY, orderFuzzy);

        allModels.addAll(modelsArima);
        allModels.addAll(modelsNeural);
        allModels.addAll(modelsFuzzy);

        System.out.println("Создание ансамблей");
        createEnsembleLists(allModels, weighted, neural);
        System.out.println("Фильтрация средневзвешенных ансамблей");
        selectEnsembles(weighted);
        System.out.println("Фильтрация нейросетевых ансамблей");
        selectEnsembles(neural);
        System.out.println("Фильтрация автономных моделей");
        selectModels(allModels);
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
    private List<Model> fitedModels(Models model, int order) throws InvalidTemporaryValueException, ForecastNotFitedModelException, IOException, InvalidOrderException {
        List<Model> models = new ArrayList<>(order);
        int i = (model != Models.ARIMA) ? 1 : 0;
        for (; i <= order; ++i) {
            if (model == Models.ARIMA) {
                for (int q = 0; q <= order; ++q) {
                    if (i == 0 && q == 0) {
                        continue;
                    }
                    Model creation = createModel(model, i, (isStationary) ? 0 : 2, q);
                    creation.fit();
                    models.add(creation);
                }
            } else {
                Model creation = createModel(model, i, 0, 0);
                creation.fit();
                models.add(creation);
            }
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
    private Model createModel(Models model, int order, int diff, int errOrder) throws InvalidOrderException {
        switch (model) {
            case ARIMA: {
                return new Arima(timeSeries, order, forecastCount, TRAIN_PERSENT, TEST_PERSENT, diff, errOrder);
            }
            case NEURAL: {
                return new Neural(timeSeries, order, forecastCount, TRAIN_PERSENT, TEST_PERSENT);
            }
            case FUZZY: {
                return new Fuzzy(timeSeries, order, forecastCount, TRAIN_PERSENT, TEST_PERSENT);
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
    private void createEnsembleLists(List<Model> allModels, List<Ensemble> weighted, List<Ensemble> neural) throws NoEqualsTimeSeriesException, InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException, IOException, InvalidOrderException {
        List<String> tableSortOut = tableSortOut(allModels.size());
        for (String rowSortOut : tableSortOut) {
            Ensemble ensembleWeighted = new WeightedAverageEnsemble(timeSeries, forecastCount, TRAIN_PERSENT, TEST_PERSENT);
            Ensemble ensembleNeural = new NeuralEnsemble(timeSeries, forecastCount, TRAIN_PERSENT, TEST_PERSENT);
            createEnsemble(rowSortOut, allModels, ensembleWeighted, ensembleNeural);
            if (ensembleWeighted.getModels().size() > 1 && ensembleNeural.getModels().size() > 1) {
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
        long tableSize = (int) Math.pow(2, size);
        List<String> tableSortOut = new ArrayList<>();
        for (long i = 1; i <= tableSize - 1; ++i) {
            String rowSortOut = Long.toBinaryString(i);
            for (int k = 0; k < size; ++k) {
                rowSortOut = "0" + rowSortOut;
            }
            tableSortOut.add(rowSortOut.substring(rowSortOut.length() - size));
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
        for (int i = 0; i < row.length(); ++i) {
            if (row.charAt(i) == '1') {
                Model model = models.get(i);
                //if (!model.isOverFited(overFitedBorder)) {
                weighted.addModel(model);
                neural.addModel(model);
                //}
            }
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
            if (isIncorrect(ensemble)) {
                ensembleIterator.remove();
            }
        }
    }

    /**
     * Выбор моделей, отвечающих требованиям качества.
     *
     * @param models список ансамблей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    private void selectModels(List<Model> models) throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        Iterator<Model> modelIterator = models.iterator();
        while (modelIterator.hasNext()) {
            Model model = modelIterator.next();
            if (isIncorrect(model)) {
                modelIterator.remove();
            }
        }
    }

    /**
     * Проверка модели на корректность.
     *
     * @param model модель.
     * @return некорректность.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    private boolean isIncorrect(Model model) throws TimeSeriesSizeException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        return model.getTestMape() > qualityBorder || model.isOverFited(overFitedBorder);
    }

    enum Models {
        ARIMA,
        NEURAL,
        FUZZY
    }
}
