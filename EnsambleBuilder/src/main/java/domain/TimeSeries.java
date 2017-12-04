package domain;

import org.apache.commons.math3.util.Precision;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class TimeSeries {
    private final Map<Integer, Double> timeSeries;

    private double maxValue = 0;

    public TimeSeries() {
        timeSeries = new HashMap<Integer, Double>();
    }

    /**
     * Добавить значение временного ряда (временная метка = текущая длина + 1).
     *
     * @param value значение.
     */
    public void addTimeValue(double value) {
        maxValue = (value > maxValue) ? value : maxValue;
        timeSeries.put(timeSeries.size() + 1, value);
    }

    /**
     * Добавить значение временного ряда.
     *
     * @param t     временная метка.
     * @param value значение.
     */
    public void add(int t, double value) {
        maxValue = (value > maxValue) ? value : maxValue;
        timeSeries.put(t, value);
    }

    /**
     * Получить временной ряд.
     *
     * @return временной ряд.
     */
    public Map<Integer, Double> getTimeSeries() {
        return timeSeries;
    }

    /**
     * Получить максимальное значение временного ряда.
     *
     * @return максимальное значение временного ряда.
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Удалить значение временного ряда.
     *
     * @param t метка времени удаляемого значения.
     */
    public void removeTimeValue(int t) {
        timeSeries.remove(t);
    }

    /**
     * Получить значение временного ряда.
     *
     * @param t метка времени получаемого значения.
     * @return значение временного ряда.
     */
    public double getTimeValue(int t) {
        return timeSeries.get(t);
    }

    /**
     * Нормализация временного ряда для дальнейшего использования.
     */
    public void normalize() {
        for (Map.Entry<Integer, Double> entry : timeSeries.entrySet()) {
            entry.setValue(entry.getValue() / maxValue);
        }
    }

    /**
     * Денормализация временного ряда.
     */
    public void denormalize() {
        for (Map.Entry<Integer, Double> entry : timeSeries.entrySet()) {
            entry.setValue(entry.getValue() * maxValue);
        }
    }

    /**
     * Денормализация временного ряда с использованием внешнего максимума.
     *
     * @param maxValue максимум.
     */
    public void denormalize(double maxValue) {
        for (Map.Entry<Integer, Double> entry : timeSeries.entrySet()) {
            entry.setValue(entry.getValue() * maxValue);
        }
    }

    /**
     * Получить длину временного ряда.
     *
     * @return длина временного ряда.
     */
    public int getSize() {
        return timeSeries.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        TimeSeries that = (TimeSeries) o;
        if (that == null || that.getSize() != getSize()) return false;

        for (Map.Entry<Integer, Double> value : timeSeries.entrySet()) {
            if (!Precision.equals(value.getValue(), that.getTimeValue(value.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return timeSeries != null ? timeSeries.hashCode() : 0;
    }


}
