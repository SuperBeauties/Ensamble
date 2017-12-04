import domain.TimeSeries;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ReaderTest {
    @Test
    public void ShouldReadTS() throws IOException {
        Reader reader = new Reader();
        TimeSeries timeSeries = reader.readTimeSeries();
        System.out.println(timeSeries.getTimeValue(1));
    }

}