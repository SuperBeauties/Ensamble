import domain.Ensemble;
import domain.Model;
import domain.Quality;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.TimeSeriesSizeException;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;

import java.util.ArrayList;
import java.util.List;

public class WriteEnsemble {
    private final TimeSeries timeSeries;
    private final List<Ensemble> weighted;
    private final List<Ensemble> neural;
    private final List<String> description;
    private final Writer writer;
    private int index = 0;

    public WriteEnsemble(TimeSeries timeSeries, List<Ensemble> weighted, List<Ensemble> neural) {
        this.timeSeries = timeSeries;
        this.weighted = weighted;
        this.neural = neural;
        this.description = new ArrayList<>();
        this.writer = new Writer();
    }

    /**
     * Запись результатов моделирования в файлы.
     *
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    public void write() throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        write(weighted, "средневзвешенный");
        write(neural, "нейросетевой");
        writer.writeDescription(description);
    }

    /**
     * Запись результатов для списка ансамблей.
     *
     * @param ensembles список ансамблей.
     * @param type      типа ансамблей в списке.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     */
    private void write(List<Ensemble> ensembles, String type) throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        for (Ensemble ensemble : ensembles) {
            TimeSeries timeSeriesCalc = new TimeSeries();
            for (int i = ensemble.getOrder() + 1; i <= timeSeries.getSize(); ++i) {
                timeSeriesCalc.add(i, ensemble.forecast(i));
            }
            writer.writeTs(timeSeriesCalc, String.valueOf(++index));

            double mapeTrain = ensemble.getTrainMape();
            double mapeTest = ensemble.getTestMape();
            double sMape = Quality.sMape(mapeTrain, mapeTest);
            writer.writeParams(mapeTrain, mapeTest, sMape);

            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append("Тип ансамбля: " + type + ". Включает модели: ");
            for (Model model : ensemble.getModels()) {
                if (model instanceof Arima) {
                    descriptionBuilder.append("Модель Arima (порядок ");
                } else if (model instanceof Neural) {
                    descriptionBuilder.append("Неросетевая модель (порядок ");
                } else if (model instanceof Fuzzy) {
                    descriptionBuilder.append("Нечеткая модель (порядок ");
                }
                descriptionBuilder.append(model.getOrder() + "); ");
            }
            description.add(descriptionBuilder.toString());
        }
    }
}
