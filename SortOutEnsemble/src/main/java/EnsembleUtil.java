import domain.Ensemble;
import domain.Model;
import domain.Quality;
import domain.TimeSeries;
import domain.exceptions.*;
import domain.models.ensemble.NeuralEnsemble;
import domain.models.ensemble.WeightedAverageEnsemble;
import domain.models.single.Arima;
import domain.models.single.Fuzzy;
import domain.models.single.Neural;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnsembleUtil {
    private final TimeSeries timeSeries;
    private final List<Ensemble> weighted;
    private final List<Ensemble> neural;
    private final Writer writer;
    private final Reader reader;
    private final List<Model> allModels;
    private final int trainPercent;
    private final int testPercent;
    private final int forecastCount;
    private int index;

    public EnsembleUtil(TimeSeries timeSeries, List<Ensemble> weighted, List<Ensemble> neural, List<Model> allModels, int start) {
        this.timeSeries = timeSeries;
        this.weighted = weighted;
        this.neural = neural;
        this.allModels = allModels;
        index = start;
        forecastCount = 0;
        testPercent = 0;
        trainPercent = 0;
        writer = new Writer();
        reader = new Reader();
    }

    public EnsembleUtil(TimeSeries timeSeries, int forecastCount, int trainPercent, int testPercent) {
        this.timeSeries = timeSeries;
        this.testPercent = testPercent;
        this.trainPercent = trainPercent;
        this.forecastCount = forecastCount;
        index = 0;
        weighted = new ArrayList<>();
        neural = new ArrayList<>();
        allModels = new ArrayList<>();
        writer = new Writer();
        reader = new Reader();
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
        write(weighted, "WA_Ensemble");
        write(neural, "ANN_ENSEMBLE");
    }

    /**
     * Чтение ансамбля.
     *
     * @return ансабль.
     * @throws IOException при чтении файла.
     */
    public Model read() throws IOException, InvalidDescriptionException, NoEqualsTimeSeriesException, InvalidOrderException {
        String description = reader.readDescription();
        description = description.replace("\"", "");
        description = description.replace(".", "");
        description = description.replace("(", "");
        description = description.replace(")", "");

        Ensemble ensemble;
        if (description.contains("WA_Ensemble")) {
            description = description.replace("WA_Ensemble", "");
            ensemble = new WeightedAverageEnsemble(timeSeries, forecastCount, trainPercent, testPercent);
        } else if (description.contains("ANN_ENSEMBLE")) {
            description = description.replace("ANN_ENSEMBLE", "");
            ensemble = new NeuralEnsemble(timeSeries, forecastCount, trainPercent, testPercent);
        } else {
            description = description.replace(";", "");
            Model model = getModelFromDescription(description);
            return model;
        }
        description = description.trim();
        String[] modelDescriptions = description.split(";");
        for (String modelDescription : modelDescriptions) {
            Model model = getModelFromDescription(modelDescription);
            ensemble.addModel(model);
        }
        return ensemble;
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
            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append(type + "(");
            for (Model model : ensemble.getModels()) {
                descriptionBuilder.append(getModelDescription(model));
            }
            descriptionBuilder.append(")");
            write(ensemble, descriptionBuilder.toString());
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
            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append(getModelDescription(model));
            write(model, descriptionBuilder.toString());
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
    private void write(Model model, String description) throws IOException, InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
        TimeSeries timeSeriesCalc = new TimeSeries();
        int i;
        for (i = 1; i <= timeSeries.getSize(); ++i) {
            timeSeriesCalc.add(i, model.forecast(i));
        }
        for (double forecast : model.getForecast()) {
            timeSeriesCalc.add(i, forecast);
            ++i;
        }
        timeSeriesCalc.denormalize(timeSeries.getMaxValue());

        String name = String.valueOf(++index);
        writer.writeTs(timeSeriesCalc, model.getOrder(), name);

        double mapeTrain = model.getTrainMape();
        double mapeTest = model.getTestMape();
        double sMape = Quality.sMape(mapeTrain, mapeTest);

        writer.writeParams(mapeTrain, mapeTest, sMape, description, name);
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
            descriptionBuilder.append("Arima(");
            descriptionBuilder.append(model.getOrder() + ",");
            descriptionBuilder.append(((((Arima) model).getDiff() == 0) ? 0 : 2) + ",");
            descriptionBuilder.append(((Arima) model).getErrOrder() + "); ");
            return descriptionBuilder;
        } else if (model instanceof Neural) {
            descriptionBuilder.append("ANN(");
        } else if (model instanceof Fuzzy) {
            descriptionBuilder.append("S(");
        }
        descriptionBuilder.append(model.getOrder() + "); ");
        return descriptionBuilder;
    }

    /**
     * Создать модель на основе текстового описания.
     *
     * @param modelDescription описание модели.
     * @return модль.
     * @throws InvalidDescriptionException некорректное описание.
     */
    private Model getModelFromDescription(String modelDescription) throws InvalidDescriptionException, InvalidOrderException {
        Model model;
        modelDescription = modelDescription.replace(")", "");
        modelDescription = modelDescription.replace("(", "");
        modelDescription = modelDescription.replace(" ", "");
        if (modelDescription.contains("Arima")) {
            modelDescription = modelDescription.replace("Arima", "");
            String[] orders = modelDescription.split(",");
            int order = Integer.parseInt(orders[0].replaceAll("[^0-9]", ""));
            int diff = Integer.parseInt(orders[1].replaceAll("[^0-9]", ""));
            int errOrder = Integer.parseInt(orders[2].replaceAll("[^0-9]", ""));
            model = new Arima(timeSeries, order, forecastCount, trainPercent, testPercent, (diff == 0) ? 0 : 1, errOrder);
        } else if (modelDescription.contains("S")) {
            modelDescription = modelDescription.replace("S", "");
            int order = Integer.parseInt(modelDescription.replaceAll("[^0-9]", ""));
            model = new Fuzzy(timeSeries, order, forecastCount, trainPercent, testPercent);
        } else if (modelDescription.contains("ANN")) {
            modelDescription = modelDescription.replace("ANN", "");
            int order = Integer.parseInt(modelDescription.replaceAll("[^0-9]", ""));
            model = new Neural(timeSeries, order, forecastCount, trainPercent, testPercent);
        } else {
            throw new InvalidDescriptionException();
        }
        return model;
    }
}
