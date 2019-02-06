package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.utils.Score;
import eubr.atmosphere.tma.analyze.database.DataManager;
import eubr.atmosphere.tma.analyze.utils.Constants;
import eubr.atmosphere.tma.analyze.utils.KubernetesManager;

public class Main {

    /** OBSERVATION_WINDOW: window that the readings will be used to calculate the score (in minutes) */
    private static int OBSERVATION_WINDOW = 1;
    private static int OBSERVATION_WINDOW_SECONDS = 30;

    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");

    private static KafkaManager kafkaManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();
        kafkaManager = new KafkaManager();

        while (true) {
        	Calendar initialDate = Calendar.getInstance();
            initialDate.add(Calendar.SECOND, -OBSERVATION_WINDOW_SECONDS);
            Calendar finalDate = Calendar.getInstance();

            System.out.println("dateTime,cpuPod,memoryPod,cpuNode,memoryNode,score,type");
            //calculateScoreNormalized(dataManager, initialDate, finalDate);
            calculateScoreNonNormalized(dataManager, initialDate);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the score without normalizing the data in advance.
     * It assumes that the value is already the mean of the last minute.
     * @param dataManager object used to manipulate the database
     * @param initialDate initial date of the search
     */
    private static void calculateScoreNonNormalized(DataManager dataManager, Calendar initialDate) {
        String strDate = sdf.format(initialDate.getTime());
        Score score = dataManager.getData(strDate);
        if (score != null && score.isValid()) {
            score.setTimestamp(initialDate.getTimeInMillis());
            score.setPodCount(KubernetesManager.getReplicas("wildfly"));
            System.out.println(strDate + "," + score.getCsvLine() + ",singleReading");
            try {
                kafkaManager.addItemKafka(score);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private static void calculateScoreNormalized(DataManager dataManager, Calendar initialDate, Calendar finalDate) {
        Calendar currentInitial = (Calendar) initialDate.clone();
        Calendar currentFinal = (Calendar) finalDate.clone();

        currentInitial.add(Calendar.MINUTE, -OBSERVATION_WINDOW);
        currentFinal.add(Calendar.MINUTE, -OBSERVATION_WINDOW);

        for (int i = 0; i < OBSERVATION_WINDOW; i++) {
            String strInitialDate = sdf.format(currentInitial.getTime());
            String strFinalDate = sdf.format(currentFinal.getTime());

            List<Double> valuesCpuPod =
                    dataManager.getValuesPeriod(strInitialDate, strFinalDate,
                    Constants.cpuDescriptionId, Constants.podId);
            List<Double> valuesCpuPodNormalized = normalizeData(valuesCpuPod);

            List<Double> valuesMemoryPod =
                    dataManager.getValuesPeriod(strInitialDate, strFinalDate,
                    Constants.memoryDescriptionId, Constants.podId);
            List<Double> valuesMemoryPodNormalized = normalizeData(valuesMemoryPod);

            List<Double> valuesCpuNode =
                    dataManager.getValuesPeriod(strInitialDate, strFinalDate,
                    Constants.cpuDescriptionId, Constants.nodeId);
            List<Double> valuesCpuNodeNormalized = normalizeData(valuesCpuNode);

            List<Double> valuesMemoryNode =
                    dataManager.getValuesPeriod(strInitialDate, strFinalDate,
                    Constants.memoryDescriptionId, Constants.nodeId);
            List<Double> valuesMemoryNodeNormalized = normalizeData(valuesMemoryNode);

            ////////////////////////////////////////////////////////////////////////////////////
            LOGGER.debug("Size CPU-Pod: " + valuesCpuPod.size());
            LOGGER.debug("Size Memory-Pod: " + valuesMemoryPod.size());
            LOGGER.debug("Size CPU-Node: " + valuesCpuNode.size());
            LOGGER.debug("Size Memory-Node: " + valuesMemoryNode.size());

            ////////////////////////////////////////////////////////////////////////////////////
            Score scoreNonNormalized = new Score();
            scoreNonNormalized.setCpuPod(getArithmeticMean(valuesCpuPod));
            scoreNonNormalized.setMemoryPod(getArithmeticMean(valuesMemoryPod));
            scoreNonNormalized.setCpuNode(getArithmeticMean(valuesCpuNode));
            scoreNonNormalized.setMemoryNode(getArithmeticMean(valuesMemoryNode));
            System.out.println(strFinalDate + "," + scoreNonNormalized.getCsvLine() + ",timeSeriesNonNormalized");

            ////////////////////////////////////////////////////////////////////////////////////
            Score scoreNormalized = new Score();
            scoreNormalized.setCpuPod(getArithmeticMean(valuesCpuPodNormalized));
            scoreNormalized.setMemoryPod(getArithmeticMean(valuesMemoryPodNormalized));
            scoreNormalized.setCpuNode(getArithmeticMean(valuesCpuNodeNormalized));
            scoreNormalized.setMemoryNode(getArithmeticMean(valuesMemoryNodeNormalized));
            System.out.println(strFinalDate + "," + scoreNormalized.getCsvLine() + ",timeSeriesNormalized");

            currentInitial.add(Calendar.MINUTE, 1);
            currentFinal.add(Calendar.MINUTE, 1);
        }
    }

    private static List<Double> normalizeData(List<Double> values) {
        LOGGER.debug(values.toString());
        Double mean = getArithmeticMean(values);
        LOGGER.debug("Arithmetic Mean:" + mean);
        Double standardDeviation = getStandardDeviation(values, mean);
        LOGGER.debug("Standard Deviation:" + standardDeviation);
        List<Double> normalizedData = getNormalizedData(values, mean, standardDeviation);
        LOGGER.debug("Normalized Data:" + normalizedData);
        return normalizedData;
    }

    public static Double getArithmeticMean(List<Double> values) {
        Double sum = 0.0;
        int length = values.size();
        for (int i = 0; i < length; i++) {
            sum += values.get(i);
        }
        return sum / length;
    }

    public static double getStandardDeviation(List<Double> values, Double mean) {
        double standardDeviation = 0.0;
        int length = values.size();
        for (int i = 0; i < length; i++) {
            standardDeviation += Math.pow(values.get(i) - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    private static List<Double> getNormalizedData(List<Double> values,
            Double mean, Double standardDeviation) {
        List<Double> result = new ArrayList<Double>();

        int length = values.size();
        for (int i = 0; i < length; i++) {
            standardDeviation += Math.pow(values.get(i) - mean, 2);
            result.add((values.get(i) - mean) / standardDeviation);
        }
        return result;
    }
}
