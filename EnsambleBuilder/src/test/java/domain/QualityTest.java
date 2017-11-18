package domain;

import junit.framework.AssertionFailedError;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;


public class QualityTest {
    @Test
    public void mapeTest() throws Exception {
        TimeSeries timeSeriesFact = new TimeSeries();
        timeSeriesFact.addTimeValue(1.0);
        timeSeriesFact.addTimeValue(2.0);
        timeSeriesFact.addTimeValue(1.3);
        timeSeriesFact.addTimeValue(1.0);

        TimeSeries timeSeriesCalc = new TimeSeries();
        timeSeriesCalc.addTimeValue(1.1);
        timeSeriesCalc.addTimeValue(2.2);
        timeSeriesCalc.addTimeValue(1.1);
        timeSeriesCalc.addTimeValue(1.0);

        double mape = Quality.mape(timeSeriesFact, timeSeriesCalc);
        Assert.assertTrue(Precision.equals(mape, 0.0884615, 0.001));
    }

    @Test
    public void sMapeTest() throws Exception {
        double smape = Quality.sMape(0.1, 0.2);
        Assert.assertTrue(Precision.equals(smape, 0.33333, 0.001));
    }

    @Test
    public void percentErrorTest() throws Exception {
        double error = Quality.percentError(0.1, 0.2);
        Assert.assertTrue(Precision.equals(error, 1));
    }

}