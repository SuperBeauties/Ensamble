import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.NoEqualsTimeSeriesException;
import domain.exceptions.TimeSeriesSizeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Application {
    public static void main(String[] args) throws IOException, TimeSeriesSizeException, NoEqualsTimeSeriesException, InvalidTemporaryValueException, ForecastNotFitedModelException {
        Reader reader = new Reader();
        TimeSeries timeSeries = reader.readTimeSeries();
        String[] params = reader.readParams();
        if (params[10].equals("Test")) {
            List<Object> keys = timeSeries.getTimeSeries().keySet().stream().collect(Collectors.toList());
            int trainSize = (int) (keys.size() * 0.9);
            TimeSeries timeSeriesProcess = new TimeSeries();
            for (Object key : keys.subList(0, trainSize)) {
                timeSeriesProcess.addTimeValue(timeSeries.getTimeValue((int) key));
            }
            timeSeriesProcess.normalize();

            SortOutEnsemble sortOut = new SortOutEnsemble(
                    timeSeriesProcess,
                    (params[0].equals("1")) ? true : false,
                    (params[1].equals("1")) ? true : false,
                    (params[2].equals("1")) ? true : false,
                    Double.parseDouble(params[3].replace(",", ".")),
                    Double.parseDouble(params[4].replace(",", ".")),
                    Integer.parseInt(params[5]),
                    Integer.parseInt(params[6]),
                    Integer.parseInt(params[7]),
                    (params[8].equals("1")) ? true : false,
                    (params[9].equals("1")) ? true : false);

            List<Model> allModels = new ArrayList<>();
            List<Ensemble> weighted = new ArrayList<>();
            List<Ensemble> neural = new ArrayList<>();
            sortOut.sortOut(allModels, weighted, neural);

            WriteEnsemble writeEnsemble = new WriteEnsemble(timeSeriesProcess, weighted, neural, allModels);
            writeEnsemble.write();
        }

    }

}
