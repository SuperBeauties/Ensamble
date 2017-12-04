import domain.TimeSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Reader {
    private static String dir = "input\\";
    private static String tsFileName = "ts.csv";
    private static String paramsFileName = "params.csv";

    /**
     * Чтение временного ряда.
     *
     * @return временной ряд.
     * @throws IOException возникает при чтении файла.
     */
    public TimeSeries readTimeSeries() throws IOException {
        File file = new File(dir, tsFileName);
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
        File file = new File(dir, paramsFileName);
        String inputString = read(file);
        return inputString.split(";");
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
