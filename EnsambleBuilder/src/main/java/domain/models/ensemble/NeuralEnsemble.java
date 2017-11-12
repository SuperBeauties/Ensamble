package domain.models.ensemble;

import domain.Ensemble;
import domain.TimeSeries;

import java.util.List;

public class NeuralEnsemble extends Ensemble {
    public NeuralEnsemble(TimeSeries timeSeries) {
        super(timeSeries);
    }

    protected void fitMetaAlgorithm() {

    }

    public double forecast(int t) {
        return 0;
    }
}
