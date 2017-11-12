package domain.models;

import domain.TimeSeries;
import domain.exceptions.TimeSeriesSizeException;
import org.jetbrains.annotations.Contract;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class Quality {
    public static double mape(TimeSeries timeSeriesFact, TimeSeries timeSeriesCalc) throws TimeSeriesSizeException {
        int size = timeSeriesCalc.getSize();
        if (size == 0 || size != timeSeriesFact.getSize()) {
            throw new TimeSeriesSizeException();
        }

        double sum = 0;
        for (Map.Entry<Integer, Double> value : timeSeriesCalc.getTimeSeries().entrySet()) {
            sum += percentError(timeSeriesFact.getTimeValue(value.getKey()), value.getValue());
        }
        return sum / size;
    }

    @Contract(pure = true)
    public static double sMape(double mapeTrain, double mapeTest) {
        return Math.abs(mapeTrain - mapeTest) / (mapeTrain + mapeTest);
    }

    @Contract(pure = true)
    public static double percentError(double valueFact, double valueCalc) {
        return Math.abs(valueFact - valueCalc) / valueFact;
    }
}
