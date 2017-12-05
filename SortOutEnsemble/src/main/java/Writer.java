import domain.TimeSeries;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Writer {
    private final static String DIR = "output\\";
    private final static String TS_FILE_NAME = "ts%n%.csv";
    private final static String PARAMS_FILE_NAME = "params%n%.csv";
    private final static String EXCEPTION_FILE_NAME = "exception.csv";

    private static final Charset CHARSET = Charset.forName("windows-1251");
    private static final CSVFormat CSV_WRITER_FORMAT = CSVFormat.EXCEL.withDelimiter(';');

    /**
     * Запись результирующих временных рядов.
     *
     * @param timeSeries временной ряд.
     * @param name       название ряда.
     * @throws IOException при записи в файл.
     */
    public void writeTs(TimeSeries timeSeries, int order, String name) throws IOException {
        Path path = Paths.get(DIR, TS_FILE_NAME.replace("%n%", name));
        BufferedWriter writer = Files.newBufferedWriter(path, CHARSET);
        CSVPrinter printer = new CSVPrinter(writer, CSV_WRITER_FORMAT);

        for (int i = order + 1; i <= timeSeries.getSize() + order; ++i) {
            double value = timeSeries.getTimeValue(i);
            List<String> record = new ArrayList<>();
            record.add(String.valueOf(i));
            record.add(String.valueOf(value).replace(".", ","));
            printer.printRecord(record);
        }
        writer.flush();
        writer.close();
    }

    /**
     * Запись параметров качества.
     *
     * @param mapeTrain MAPE обучающей выборки.
     * @param mapeTest  MAPE тестовой выборки.
     * @param sMape     SMAPE.
     * @param name      название ряда.
     * @throws IOException при записи в файл.
     */
    public void writeParams(double mapeTrain, double mapeTest, double sMape, String description, String name) throws IOException {
        Path path = Paths.get(DIR, PARAMS_FILE_NAME.replace("%n%", name));
        BufferedWriter writer = Files.newBufferedWriter(path, CHARSET);
        CSVPrinter printer = new CSVPrinter(writer, CSV_WRITER_FORMAT);

        List<String> record = new ArrayList<>();
        record.add(String.valueOf(mapeTrain).replace(".", ","));
        printer.printRecord(record);

        record = new ArrayList<>();
        record.add(String.valueOf(mapeTest).replace(".", ","));
        printer.printRecord(record);

        record = new ArrayList<>();
        record.add(String.valueOf(sMape).replace(".", ","));
        printer.printRecord(record);

        record = new ArrayList<>();
        record.add(description);
        printer.printRecord(record);

        writer.flush();
        writer.close();
    }

    /**
     * Запись исключения в файл.
     *
     * @param description описание.
     * @param stackTrace  трасировка.
     */
    public void writeException(String description, String stackTrace) throws IOException {
        Path path = Paths.get(DIR, EXCEPTION_FILE_NAME);
        BufferedWriter writer = Files.newBufferedWriter(path, CHARSET);
        CSVPrinter printer = new CSVPrinter(writer, CSV_WRITER_FORMAT);

        List<String> record = new ArrayList<>();
        record.add(description);
        printer.printRecord(record);

        record = new ArrayList<>();
        record.add(stackTrace);
        printer.printRecord(record);

        writer.flush();
        writer.close();
    }
}
