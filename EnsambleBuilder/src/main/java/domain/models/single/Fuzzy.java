package domain.models.single;

import domain.Model;
import domain.TimeSeries;

public class Fuzzy extends Model {
    public Fuzzy(TimeSeries timeSeries, int order, int testPercent) {
        super(timeSeries, order, testPercent);
    }

    public void fit() {

    }

    public double forecast(int t) {
        return 0;
    }
}
