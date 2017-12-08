package domain.models.single;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import domain.models.Stub;
import domain.models.TRPoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fuzzy extends Model {
    private Stub stub;

    public Fuzzy(TimeSeries timeSeries, int order, int forecastCount, int trainPercent, int testPercent) throws InvalidOrderException {
        super(timeSeries, order, forecastCount, trainPercent, testPercent);
    }

    public void fit() throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://salx.pw/api/FModel/S");
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");

        StringEntity input = getTrainingData();
        System.out.println(input);

        post.setEntity(input);
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        String output = "";
        while ((line = rd.readLine()) != null) {
            output += line;
        }

        Gson gson = new GsonBuilder().create();
        stub = gson.fromJson(output, Stub.class);
        predict();
        setFit();
    }

    public double forecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        EnableForForecasting(t);
        return stub.getROW().get(t - 1).getY();
    }

    /**
     * Расчет прогноза заданной длины.
     */
    private void predict() {
        Gson gson = new GsonBuilder().create();
        System.out.println(gson.toJson(stub));
        forecast = new double[forecastCount];
        for(int i = 0; i < forecastCount; ++i) {
            forecast[i] = stub.getROW().get(timeSeries.getSize() + i - 1).getY();
        }
        System.out.println(Arrays.toString(forecast));
    }

    /**
     * Создание обучающей выборки.
     *
     * @return обучающая выборка.
     */
    @NotNull
    private StringEntity getTrainingData() throws UnsupportedEncodingException {
        Stub stub = new Stub();
        stub.setName("нечеткий");
        stub.setOrder(order);
        stub.setActualCount(timeSeries.getSize() - timeSeriesTrain.getSize());
        stub.setForecastCount(forecastCount);

        List<TRPoint> collection = new ArrayList<>();
        for (int i = 1; i <= timeSeries.getSize(); ++i) {
            TRPoint point = new TRPoint();
            point.setX(i);
            point.setY(timeSeries.getTimeValue(i));
            collection.add(point);
        }

        stub.setROW(collection);

        Gson gson = new GsonBuilder().create();
        return new StringEntity(gson.toJson(stub));
    }
}
