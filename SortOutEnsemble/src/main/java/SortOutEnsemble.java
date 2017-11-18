import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SortOutEnsemble {
    private final TimeSeries timeSeries;
    private final int orderFuzzy;
    private final int orderNeural;
    private final int orderArima;

    public SortOutEnsemble(TimeSeries timeSeries, int orderArima, int orderNeural, int orderFuzzy) {
        this.timeSeries = timeSeries;
        this.orderFuzzy = orderFuzzy;
        this.orderNeural = orderNeural;
        this.orderArima = orderArima;
    }

    public void sortOut() throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        List<Model> modelsArima = fitedModelsList(Models.ARIMA, orderArima);
        List<Model> modelsNeural = fitedModelsList(Models.NEURAL, orderNeural);
        List<Model> modelsFuzzy = fitedModelsList(Models.FUZZY, orderFuzzy);


    }

    private List<Model> fitedModelsList(Models model, int order) throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        List<Model> models = new ArrayList<Model>(order);
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
