package domain.models.single;

import com.workday.insights.timeseries.arima.struct.ArimaModel;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ArimaTest {
    @Test
    public void ShouldPredict() throws InvalidOrderException, IOException, ForecastNotFitedModelException, InvalidTemporaryValueException {
        TimeSeries timeSeries = new TimeSeries();
        timeSeries.addTimeValue(0.1);
        timeSeries.addTimeValue(0.1);
        timeSeries.addTimeValue(0.2);
        timeSeries.addTimeValue(0.3);
        timeSeries.addTimeValue(0.3);
        timeSeries.addTimeValue(0.3);
        timeSeries.addTimeValue(0.4);
        timeSeries.addTimeValue(0.4);
        timeSeries.addTimeValue(0.4);
        timeSeries.addTimeValue(0.4);
        timeSeries.addTimeValue(0.5);
        timeSeries.addTimeValue(0.5);
        timeSeries.addTimeValue(0.5);
        timeSeries.addTimeValue(0.5);
        timeSeries.addTimeValue(0.6);
        timeSeries.addTimeValue(0.6);
        timeSeries.addTimeValue(0.7);
        timeSeries.addTimeValue(0.8);
        timeSeries.addTimeValue(0.8);
        timeSeries.addTimeValue(0.8);
        timeSeries.addTimeValue(0.6);
        timeSeries.addTimeValue(0.9);
        timeSeries.addTimeValue(0.7);
        Model arima = new Arima(timeSeries, 1, 9, 90, 10);
        arima.fit();
    }

}