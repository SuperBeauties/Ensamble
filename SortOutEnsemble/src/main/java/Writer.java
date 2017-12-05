import domain.Ensemble;
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
    private final static String dir = "output\\";
    private final static String tsFileName = "ts%n%.csv";
    private final static String paramsFileName = "params%n%.csv";
    private final static String descriptionFileName = "description.csv";

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
        Path path = Paths.get(dir, tsFileName.replace("%n%", name));
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
        Path path = Paths.get(dir, paramsFileName.replace("%n%", name));
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
}
