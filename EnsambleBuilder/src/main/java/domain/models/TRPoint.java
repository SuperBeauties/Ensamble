package domain.models;

import java.math.BigDecimal;

public class TRPoint {
    /**
     * Позиция.
     */
    private double X;
    /**
     * Значение.
     */
    private double Y;

    /**
     * Получить позицию.
     * @return позиция.
     */
    public double getX() {
        return X;
    }

    /**
     * Получить значение.
     * @return значение.
     */
    public double getY() {
        return Y;
    }

    /**
     * Установить позицию.
     * @param x позиция.
     */
    public void setX(double x) {
        X = x;
    }

    /**
     * Установить значение.
     * @param y значение.
     */
    public void setY(double y) {
        Y = y;
    }
}
