package domain;

import domain.exceptions.InvalidOrderException;
import domain.models.single.Fuzzy;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModelTest {

    @Test
    public void ShouldCreateModelAndPartTimeSeries() throws InvalidOrderException {
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
        Model model = new Fuzzy(timeSeries, 3, 20);
        TimeSeries timeSeriesTrain = model.getTimeSeriesTrain();
        TimeSeries timeSeriesTest = model.getTimeSeriesTest();

        Assert.assertEquals(timeSeriesTrain.getSize(), 8);
        Assert.assertEquals(timeSeriesTest.getSize(), 2);

        Assert.assertTrue(Precision.equals(timeSeriesTrain.getTimeValue(1), 1.0));
        Assert.assertTrue(Precision.equals(timeSeriesTrain.getTimeValue(8), 1.7));

        Assert.assertTrue(Precision.equals(timeSeriesTest.getTimeValue(1), 1.5));
        Assert.assertTrue(Precision.equals(timeSeriesTest.getTimeValue(2), 1.9));
    }

}