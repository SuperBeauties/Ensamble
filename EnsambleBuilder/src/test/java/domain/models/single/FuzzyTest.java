package domain.models.single;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.exceptions.TimeSeriesSizeException;
import domain.models.Stub;
import domain.models.TRPoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FuzzyTest {
    @Test
    public void ShouldFuzzyForecast() throws IOException, InvalidTemporaryValueException, ForecastNotFitedModelException, TimeSeriesSizeException {
//        Stub stub = new Stub();
//        stub.setName("name");
//        stub.setOrder(3);
//        stub.setActualCount(2);
//        stub.setForecastCount(3);
//        stub.setForecastCount(3);
//
//        List<TRPoint> collection = new ArrayList<>();
//        for (double i = 1; i <= 6; ++i) {
//            TRPoint point = new TRPoint();
//            point.setX(i);
//            point.setY(i * 0.9);
//            collection.add(point);
//        }
//
//        stub.setROW(collection);
//
//        Gson gson = new GsonBuilder().create();
//        StringEntity input = new StringEntity(gson.toJson(stub));
//
//        HttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost("http://salx.pw/api/FModel/S");
//        post.setHeader("Accept", "application/json");
//        post.setHeader("Content-type", "application/json");
//        post.setEntity(input);
//        HttpResponse response = client.execute(post);
//        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//        String line = "";
//        while ((line = rd.readLine()) != null) {
//            System.out.println(line);
//        }
//        TimeSeries timeSeries = new TimeSeries();
//        timeSeries.addTimeValue(1.0);
//        timeSeries.addTimeValue(2.0);
//        timeSeries.addTimeValue(1.3);
//        timeSeries.addTimeValue(1.0);
//        timeSeries.addTimeValue(1.1);
//        timeSeries.addTimeValue(2.2);
//        timeSeries.addTimeValue(1.1);
//        timeSeries.addTimeValue(1.7);
//        timeSeries.addTimeValue(1.5);
//        timeSeries.addTimeValue(1.9);
//
//        Model model = new Fuzzy(timeSeries, 2, 50);
//        model.fit();
//        model.getTestMape();
    }


}