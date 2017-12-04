package domain.models.ensemble;

import domain.Ensemble;
import domain.Model;
import domain.TimeSeries;
import domain.exceptions.ForecastNotFitedModelException;
import domain.exceptions.InvalidTemporaryValueException;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
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

import java.util.List;

public class NeuralEnsemble extends Ensemble {
    private static final int SEED = 12345;
    private static final int ITERATIONS = 5;
    private static final int N_EPOCHS = 1;
    private static final int BATCH_SIZE = 100;
    private static final double LEARNING_RATE = 0.01;
    private static final int NUM_OUTPUTS = 1;

    private MultiLayerNetwork net;

    public NeuralEnsemble(TimeSeries timeSeries, int testPercent) {
        super(timeSeries, testPercent);
    }

    protected void fitMetaAlgorithm() throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        final MultiLayerConfiguration conf = getDeepDenseLayerNetworkConfiguration();
        net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        final DataSetIterator iterator = getTrainingData();
        for (int i = 0; i < N_EPOCHS; i++) {
            iterator.reset();
            net.fit(iterator);
        }
        setFit();
    }

    public double forecast(int t) throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        EnableForForecasting(t);
        INDArray x = Nd4j.create(1, models.size());
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            x.putScalar(new int[]{0, i}, model.forecast(t));
        }
        INDArray res = net.output(x, false);
        return res.getDouble(0);
    }

    /**
     * Создание конфигурации нейронной сети.
     *
     * @return конфигурация нейронной сети.
     */
    private MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration() {
        final int numHiddenNodes1 = 7;
        final int numHiddenNodes2 = 5;
        return new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .iterations(ITERATIONS)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(LEARNING_RATE)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.9))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(models.size()).nOut(numHiddenNodes1).activation(Activation.HARDTANH).build())
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes1).nOut(numHiddenNodes2).activation(Activation.HARDTANH).build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.SIGMOID).nIn(numHiddenNodes2).nOut(NUM_OUTPUTS).build())
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
    private DataSetIterator getTrainingData() throws InvalidTemporaryValueException, ForecastNotFitedModelException {
        int size = timeSeriesTrain.getSize() - order;
        INDArray x = Nd4j.create(size, models.size());
        INDArray y = Nd4j.create(size, NUM_OUTPUTS);
        int j = 0;
        for (int t = order + 1; t <= timeSeriesTrain.getSize(); ++t) {
            for (int i = 0; i < models.size(); i++) {
                Model model = models.get(i);
                x.putScalar(new int[]{j, i}, model.forecast(t));
            }
            y.putScalar(j, timeSeriesTrain.getTimeValue(t));
            ++j;
        }
        return new ListDataSetIterator(new DataSet(x, y).asList(), BATCH_SIZE);
    }
}
