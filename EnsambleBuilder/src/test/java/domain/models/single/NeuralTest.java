package domain.models.single;

import domain.Model;
import domain.TimeSeries;
import org.junit.Test;

import static org.junit.Assert.*;

public class NeuralTest {
    @Test
    public  void ShouldCreate() {
        TimeSeries timeSeries = new TimeSeries();
        timeSeries.addTimeValue(1.0);
        timeSeries.addTimeValue(2.0);
        timeSeries.addTimeValue(1.3);
        timeSeries.addTimeValue(1.0);
        timeSeries.addTimeValue(1.1);
        timeSeries.addTimeValue(2.2);
        timeSeries.addTimeValue(1.1);
        timeSeries.addTimeValue(1.7);
        timeSeries.addTimeValue(1.5);
        timeSeries.addTimeValue(1.9);
        Model model = new Neural(timeSeries, 3, 0);

    }

}