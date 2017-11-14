package domain;

import domain.TimeSeries;
import domain.exceptions.TimeSeriesSizeException;
import org.jetbrains.annotations.Contract;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class Quality {
    /**
     * Рассчитать среднюю ошибку аппроксимации.
     * @param timeSeriesFact временной ряд с фактическими значениями.
     * @param timeSeriesCalc временной ряд с расчетными значениями.
     * @return средняя ошибка аппроксимации
     * @throws TimeSeriesSizeException некорректная длина временных рядов.
     */
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

    /**
     * Рассчитать SMape для ошибок аппроксимации.
     * @param mapeTrain mape обучающей выборки.
     * @param mapeTest mape тестовой выборки.
     * @return SMape для ошибок аппроксимации.
     */
    @Contract(pure = true)
    public static double sMape(double mapeTrain, double mapeTest) {
        return Math.abs(mapeTrain - mapeTest) / (mapeTrain + mapeTest);
    }

    /**
     * Рассчитать абсолютную процентную ошибку.
     * @param valueFact фактическое значение.
     * @param valueCalc расчетное значение.
     * @return абсолютная процентная ошибка.
     */
    @Contract(pure = true)
    public static double percentError(double valueFact, double valueCalc) {
        return Math.abs(valueFact - valueCalc) / valueFact;
    }
}
