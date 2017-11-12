package domain.models.single;

import domain.Model;
import domain.TimeSeries;

public class Fuzzy extends Model {
    public Fuzzy(TimeSeries timeSeries, int order) {
        super(timeSeries, order);
    }

    public void fit() {

    }

    public double forecast(int t) {
        return 0;
    }
}
