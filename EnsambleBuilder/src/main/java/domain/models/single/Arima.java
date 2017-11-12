package domain.models.single;

import domain.Model;
import domain.TimeSeries;

public class Arima extends Model {
    public Arima(TimeSeries timeSeries, int order) {
        super(timeSeries, order);
    }

    public void fit() {

    }

    public double forecast(int t) {
        return 0;
    }
}
