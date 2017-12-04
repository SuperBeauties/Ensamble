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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteEnsemble {
    private final TimeSeries timeSeries;
    private final List<Ensemble> weighted;
    private final List<Ensemble> neural;
    private final List<String> description;
    private final Writer writer;
    private final List<Model> allModels;
    private int index = 0;

    public WriteEnsemble(TimeSeries timeSeries, List<Ensemble> weighted, List<Ensemble> neural, List<Model> allModels) {
        this.timeSeries = timeSeries;
        this.weighted = weighted;
        this.neural = neural;
        this.allModels = allModels;
        this.description = new ArrayList<>();
        this.writer = new Writer();
    }

    /**
     * Запись результатов моделирования в файлы.
     *
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws IOException                    при записи в файл.
     */
    public void write() throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException, IOException {
        write(allModels);
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
     * @throws IOException                    при записи в файл.
     */
    private void write(List<Ensemble> ensembles, String type) throws InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException, IOException {
        for (Ensemble ensemble : ensembles) {
            write(ensemble);
            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append("Тип ансамбля: " + type + ". Включает модели: ");
            for (Model model : ensemble.getModels()) {
                descriptionBuilder.append(getModelDescription(model));
            }
            description.add(descriptionBuilder.toString());
        }
    }

    /**
     * Запись результатов для автомной модели.
     *
     * @param models список моделей.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws IOException                    при записи в файл.
     */
    private void write(List<Model> models) throws ForecastNotFitedModelException, TimeSeriesSizeException, InvalidTemporaryValueException, IOException {
        for (Model model : models) {
            write(model);
            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append("Тип ансамбля: автономная модель.  Включает модели: ");
            descriptionBuilder.append(getModelDescription(model));
            description.add(descriptionBuilder.toString());
        }
    }

    /**
     * Запись результатов модели.
     *
     * @param model модель.
     * @throws InvalidTemporaryValueException некорректна метка времени предсказываемого значения.
     * @throws ForecastNotFitedModelException модель не была обучена.
     * @throws TimeSeriesSizeException        некорректная длина временных рядов.
     * @throws IOException                    при записи в файл.
     */
    private void write(Model model) throws IOException, InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        TimeSeries timeSeriesCalc = new TimeSeries();
        for (int i = model.getOrder() + 1; i <= timeSeries.getSize(); ++i) {
            timeSeriesCalc.add(i, model.forecast(i));
        }
        timeSeriesCalc.denormalize(timeSeries.getMaxValue());

        String name = String.valueOf(++index);
        writer.writeTs(timeSeriesCalc, model.getOrder(), name);

        double mapeTrain = model.getTrainMape();
        double mapeTest = model.getTestMape();
        double sMape = Quality.sMape(mapeTrain, mapeTest);
        writer.writeParams(mapeTrain, mapeTest, sMape, name);
    }

    /**
     * Получить описание для модели.
     *
     * @param model модель.
     * @return писание.
     */
    private StringBuilder getModelDescription(Model model) {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (model instanceof Arima) {
            descriptionBuilder.append("Модель Arima (порядок ");
        } else if (model instanceof Neural) {
            descriptionBuilder.append("Нейросетевая модель (порядок ");
        } else if (model instanceof Fuzzy) {
            descriptionBuilder.append("Нечеткая модель (порядок ");
        }
        descriptionBuilder.append(model.getOrder() + "); ");
        return descriptionBuilder;
    }
}
