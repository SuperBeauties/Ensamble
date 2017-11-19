import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.NoEqualsTimeSeriesException;
import domain.models.ensemble.NeuralEnsemble;
import domain.models.ensemble.WeightedAverageEnsemble;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class SortOutEnsemble {
    private final TimeSeries timeSeries;
    private final int orderFuzzy;
    private final int orderNeural;
    private final int orderArima;
    private final double qualityBorder;
    private final double overLearnedBorder;

    public SortOutEnsemble(TimeSeries timeSeries, int orderArima, int orderNeural, int orderFuzzy, double qualityBorder, double overLearnedBorder) {
        this.timeSeries = timeSeries;
        this.orderFuzzy = orderFuzzy;
        this.orderNeural = orderNeural;
        this.orderArima = orderArima;
        this.qualityBorder = qualityBorder;
        this.overLearnedBorder = overLearnedBorder;
    }

    public void sortOut() throws InvalidTemporaryValueException, ForecastNotFitedModelException, NoEqualsTimeSeriesException {
        List<Model> modelsArima = fitedModelsList(Models.ARIMA, orderArima);
        List<Model> modelsNeural = fitedModelsList(Models.NEURAL, orderNeural);
        List<Model> modelsFuzzy = fitedModelsList(Models.FUZZY, orderFuzzy);

        List<Model> allModels = new ArrayList<>();
        allModels.addAll(modelsArima);
        allModels.addAll(modelsNeural);
        allModels.addAll(modelsFuzzy);

        List<Ensemble> ensemblesWeighted = new ArrayList<>();
        List<Ensemble> ensemblesNeural = new ArrayList<>();
        createEnsembleLists(allModels, ensemblesWeighted, ensemblesNeural);


    }

    private void selectEnsembles(List<Ensemble> ensembles) {

    }

    private void createEnsembleLists(List<Model> allModels, List<Ensemble> weighted, List<Ensemble> neural) throws NoEqualsTimeSeriesException, InvalidTemporaryValueException, ForecastNotFitedModelException {
        List<String> tableSortOut = tableSortOut(allModels.size());
        for (String rowSortOut : tableSortOut) {
            Ensemble ensembleWeighted = new WeightedAverageEnsemble(timeSeries);
            Ensemble ensembleNeural = new NeuralEnsemble(timeSeries);
            createEnsemble(rowSortOut, allModels, ensembleWeighted, ensembleNeural);
            if (ensembleWeighted != null && ensembleNeural != null) {
                ensembleWeighted.fit();
                ensembleNeural.fit();

                weighted.add(ensembleWeighted);
                neural.add(ensembleNeural);
            }
        }
    }

    @Nullable
    private void createEnsemble(String row, List<Model> models, Ensemble weighted, Ensemble neural) throws NoEqualsTimeSeriesException {
        int size = models.size();
        int modelsCount = 0;
        for (int i = 0; i < row.length(); ++i) {
            if (row.charAt(i) == '1') {
                Model model = models.get(size - i - 1);
                weighted.addModel(model);
                neural.addModel(model);
                ++modelsCount;
            }
        }
        if (modelsCount <= 1) {
            weighted = null;
            neural = null;
        }
    }

    private void isOverLearnedModel() {

    }

    private List<String> tableSortOut(int size) {
        size = (int) Math.pow(2, size);
        List<String> tableSortOut = new ArrayList<>(size);
        for (int i = 1; i <= size; ++i) {
            String rowSortOut = Integer.toBinaryString(i);
            tableSortOut.add(rowSortOut);
        }
        return tableSortOut;
    }

    private List<Model> fitedModelsList(Models model, int order) throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        List<Model> models = new ArrayList<>(order);
        for (int i = 1; i <= order; ++i) {
            Model creation = createModel(model, i);
            creation.fit();
            models.add(creation);
        }
        return models;
    }

    @NotNull
    private Model createModel(Models model, int order) {
        switch (model) {
            case ARIMA: {
                return new Arima(timeSeries, order);
            }
            case NEURAL: {
                return new Neural(timeSeries, order);
            }
            case FUZZY: {
                return new Fuzzy(timeSeries, order);
            }
        }
        return null;
    }

    enum Models {
        ARIMA,
        NEURAL,
        FUZZY
    }
}
