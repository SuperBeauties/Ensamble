package domain.models.single;

import domain.Model;
import domain.TimeSeries;

public class Neural extends Model {
    public Neural(TimeSeries timeSeries, int order) {
        super(timeSeries, order);
    }

    public void fit() {

    }

    public double forecast(int t) {
        return 0;
    }
}
