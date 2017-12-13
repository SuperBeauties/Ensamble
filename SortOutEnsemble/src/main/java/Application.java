import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.*;
import domain.models.ensemble.NeuralEnsemble;
import domain.models.ensemble.WeightedAverageEnsemble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application {
    public static void main(String[] args) throws IOException {
        Reader reader = new Reader();
        TimeSeries timeSeries = null;
        try {
            timeSeries = reader.readTimeSeries();
        } catch (IOException e) {
            (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
            return;
        }
        timeSeries.normalize();
        String[] params;
        try {
            params = reader.readParams();
        } catch (IOException e) {
            (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
            return;
        }
        if (params[12].equals("Test")) {
            SortOutEnsemble sortOut = new SortOutEnsemble(
                    timeSeries,
                    (params[0].equals("1")) ? true : false,
                    (params[1].equals("1")) ? true : false,
                    (params[2].equals("1")) ? true : false,
                    Double.parseDouble(params[3].replace(",", ".")),
                    Double.parseDouble(params[4].replace(",", ".")),
                    Integer.parseInt(params[5]),
                    Integer.parseInt(params[6]),
                    Integer.parseInt(params[7]),
                    (params[8].equals("1")) ? true : false,
                    (params[9].equals("1")) ? true : false,
                    Integer.parseInt(params[10]),
                    (params[11].equals("1")) ? true : false
                    );

            List<Model> allModels = new ArrayList<>();
            List<Ensemble> weighted = new ArrayList<>();
            List<Ensemble> neural = new ArrayList<>();
            try {
                sortOut.sortOut(allModels, weighted, neural);
            } catch (InvalidTemporaryValueException e) {
                (new Writer()).writeException("Некорректная метка времени предсказываемого значения", Arrays.toString(e.getStackTrace()));
                return;
            } catch (ForecastNotFitedModelException e) {
                (new Writer()).writeException("Предсказание необученной модели", Arrays.toString(e.getStackTrace()));
                return;
            } catch (NoEqualsTimeSeriesException e) {
                (new Writer()).writeException("Не эквивалентные временные ряды среди моделей ансамбля", Arrays.toString(e.getStackTrace()));
                return;
            } catch (TimeSeriesSizeException e) {
                (new Writer()).writeException("Некорректная длина временных рядов", Arrays.toString(e.getStackTrace()));
                return;
            } catch (IOException e) {
                (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            } catch (InvalidOrderException e) {
                (new Writer()).writeException("Некорректный порядок модели", Arrays.toString(e.getStackTrace()));
                return;
            }

            EnsembleUtil writeEnsemble = new EnsembleUtil(timeSeries, weighted, neural, allModels, 0);
            try {
                writeEnsemble.write();
            } catch (InvalidTemporaryValueException e) {
                (new Writer()).writeException("Некорректная метка времени предсказываемого значения", Arrays.toString(e.getStackTrace()));
                return;
            } catch (ForecastNotFitedModelException e) {
                (new Writer()).writeException("Предсказание необученной модели", Arrays.toString(e.getStackTrace()));
                return;
            } catch (TimeSeriesSizeException e) {
                (new Writer()).writeException("Некорректная длина временных рядов", Arrays.toString(e.getStackTrace()));
                return;
            } catch (IOException e) {
                (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            }
        } else {
            EnsembleUtil readEnsemble = new EnsembleUtil(timeSeries, Integer.parseInt(params[10]),90, 10);
            Model model = null;
            try {
                model = readEnsemble.read();
            } catch (IOException e) {
                (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            } catch (InvalidDescriptionException e) {
                (new Writer()).writeException("Не корректное описание модели", Arrays.toString(e.getStackTrace()));
                return;
            } catch (NoEqualsTimeSeriesException e) {
                (new Writer()).writeException("Не эквивалентные временные ряды среди моделей ансамбля", Arrays.toString(e.getStackTrace()));
                return;
            } catch (InvalidOrderException e) {
                (new Writer()).writeException("Некорректный порядок модели", Arrays.toString(e.getStackTrace()));
                return;
            }
            try {
                model.fit();
            } catch (InvalidTemporaryValueException e) {
                (new Writer()).writeException("Некорректная метка времени предсказываемого значения", Arrays.toString(e.getStackTrace()));
                return;
            } catch (ForecastNotFitedModelException e) {
                (new Writer()).writeException("Предсказание необученной модели", Arrays.toString(e.getStackTrace()));
                return;
            } catch (IOException e) {
                (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            }

            List<Model> allModels = new ArrayList<>();
            List<Ensemble> weighted = new ArrayList<>();
            List<Ensemble> neural = new ArrayList<>();

            if(model instanceof WeightedAverageEnsemble) {
                weighted.add((Ensemble) model);
            } else if(model instanceof NeuralEnsemble) {
                neural.add((Ensemble) model);
            } else {
                allModels.add(model);
            }
            EnsembleUtil writeEnsemble = new EnsembleUtil(timeSeries, weighted, neural, allModels, -1);
            try {
                writeEnsemble.write();
            } catch (InvalidTemporaryValueException e) {
                (new Writer()).writeException("Некорректная метка времени предсказываемого значения", Arrays.toString(e.getStackTrace()));
                return;
            } catch (ForecastNotFitedModelException e) {
                (new Writer()).writeException("Предсказание необученной модели", Arrays.toString(e.getStackTrace()));
                return;
            } catch (TimeSeriesSizeException e) {
                (new Writer()).writeException("Некорректная длина временных рядов", Arrays.toString(e.getStackTrace()));
                return;
            } catch (IOException e) {
                (new Writer()).writeException(e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            }
        }
    }
}
