import domain.TimeSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Reader {
    private final static String DIR = "input\\";
    private final static String TS_FILE_NAME = "ts.csv";
    private final static String PARAMS_FILE_NAME = "params.csv";
    private final static String MODEL_FILE_NAME = "model.csv";

    /**
     * Чтение временного ряда.
     *
     * @return временной ряд.
     * @throws IOException возникает при чтении файла.
     */
    public TimeSeries readTimeSeries() throws IOException {
        File file = new File(DIR, TS_FILE_NAME);
        String inputString = read(file);
        String[] values = inputString.split(";");
        TimeSeries timeSeries = new TimeSeries();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        for (String value : values) {
            value = value.replaceAll(",", ".");
            timeSeries.addTimeValue(Double.parseDouble(value));
        }
        return timeSeries;
    }

    /**
     * Чтение параметров.
     *
     * @return параметры.
     * @throws IOException возникает при чтении файла.
     */
    public String[] readParams() throws IOException {
        File file = new File(DIR, PARAMS_FILE_NAME);
        String inputString = read(file);
        return inputString.split(";");
    }

    /**
     * Чтение описания ансамбля.
     *
     * @return описание ансамбля.
     * @throws IOException при чтении файла.
     */
    public String readDescription() throws IOException {
        File file = new File(DIR, MODEL_FILE_NAME);
        return read(file);
    }

    /**
     * Чтение строки из файла.
     *
     * @param file файл.
     * @return строка.
     * @throws IOException возникает при чтении файла.
     */
    private String read(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String s;
        String inputString = "";
        while ((s = br.readLine()) != null) {
            inputString += s;
        }
        return inputString;
    }
}
