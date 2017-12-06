package domain.models.single;

import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidOrderException;
import domain.exceptions.InvalidTemporaryValueException;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;

public class Neural extends Model {
    private static final int SEED = 12345;
    private static final int ITERATIONS = 5;
    private static final int N_EPOCHS = 10;
    private static final int BATCH_SIZE = 100;
    private static final double LEARNING_RATE = 0.01;
    private static final int NUM_OUTPUTS = 1;

    private MultiLayerNetwork net;

    public Neural(TimeSeries timeSeries, int order, int forecastCount, int trainPercent, int testPercent) throws InvalidOrderException {
        super(timeSeries, order, forecastCount, trainPercent, testPercent);
        final MultiLayerConfiguration conf = getDeepDenseLayerNetworkConfiguration();
        net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));
    }

    public void fit() {
        final DataSetIterator iterator = getTrainingData();
        for (int i = 0; i < N_EPOCHS; i++) {
            iterator.reset();
            net.fit(iterator);
        }
        predict();
        setFit();
    }

    public double forecast(int t) throws ForecastNotFitedModelException, InvalidTemporaryValueException {
        EnableForForecasting(t);
        INDArray x = Nd4j.create(1, order);
        for (int i = 0; i < order; i++) {
            x.putScalar(new int[]{0, i}, timeSeries.getTimeValue(t + i - order));
        }
        INDArray res = net.output(x, false);
        return res.getDouble(0);
    }

    /**
     * Расчет прогноза заданной длины.
     */
    private void predict() {
        forecast = new double[forecastCount];
        for (int i = 0; i < forecastCount; ++i) {
            INDArray x = Nd4j.create(1, order);
            for (int j = 0; j < order; j++) {
                int timeValue = (timeSeries.getSize() + 1) + j - order;
                if (timeValue < timeSeries.getSize() + 1) {
                    x.putScalar(new int[]{0, j}, timeSeries.getTimeValue(timeValue));
                } else {
                    x.putScalar(new int[]{0, j}, forecast[i + j - order]);
                }
            }
            forecast[i] = net.output(x, false).getDouble(0);
        }
        System.out.println(Arrays.toString(forecast));
    }

    /**
     * Создание конфигурации нейронной сети.
     *
     * @return конфигурация нейронной сети.
     */
    private MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration() {
        final int numHiddenNodes = 10;
        return new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .iterations(ITERATIONS)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(LEARNING_RATE)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.9))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(order).nOut(numHiddenNodes).activation(Activation.HARDTANH).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.SIGMOID).nIn(numHiddenNodes).nOut(NUM_OUTPUTS).build())
                .pretrain(false)
                .backprop(true)
                .build();
    }

    /**
     * Создание обучающей выборки.
     *
     * @return обучающая выборка.
     */
    @NotNull
    private DataSetIterator getTrainingData() {
        int size = timeSeriesTrain.getSize() - order;
        INDArray x = Nd4j.create(size, order);
        INDArray y = Nd4j.create(size, NUM_OUTPUTS);
        int j = 0;
        for (int t = order + 1; t <= timeSeriesTrain.getSize(); ++t) {
            for (int i = 0; i < order; ++i) {
                x.putScalar(new int[]{j, i}, timeSeriesTrain.getTimeValue(i + j + 1));
            }

            y.putScalar(j, timeSeriesTrain.getTimeValue(t));
            ++j;
        }
        return new ListDataSetIterator(new DataSet(x, y).asList(), BATCH_SIZE);
    }
}
