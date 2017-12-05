package domain.models;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public class Stub {
    private String Name;
    private List<TRPoint> ROW;
    private double SMAPE_i;
    private double SMAPE_e;
    private double MSE_i;
    private double MSE_e;
    private int order;
    private int ForecastCount;
    private int ActualCount;
    private String type;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public List<TRPoint> getROW() {
        return ROW;
    }

    public void setROW(List<TRPoint> ROW) {
        this.ROW = ROW;
    }

    public double getSMAPE_i() {
        return SMAPE_i;
    }

    public void setSMAPE_i(double SMAPE_i) {
        this.SMAPE_i = SMAPE_i;
    }

    public double getSMAPE_e() {
        return SMAPE_e;
    }

    public void setSMAPE_e(double SMAPE_e) {
        this.SMAPE_e = SMAPE_e;
    }

    public double getMSE_i() {
        return MSE_i;
    }

    public void setMSE_i(double MSE_i) {
        this.MSE_i = MSE_i;
    }

    public double getMSE_e() {
        return MSE_e;
    }

    public void setMSE_e(double MSE_e) {
        this.MSE_e = MSE_e;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getForecastCount() {
        return ForecastCount;
    }

    public void setForecastCount(int forecastCount) {
        ForecastCount = forecastCount;
    }

    public int getActualCount() {
        return ActualCount;
    }

    public void setActualCount(int actualCount) {
        ActualCount = actualCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
