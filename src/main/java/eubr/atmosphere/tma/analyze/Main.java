package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.analyze.database.DataManager;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class Main {

    /** OBSERVATION_WINDOW: window that the readings will be used to calculate the score (in minutes) */
    private static int OBSERVATION_WINDOW = 20;

    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");

    private static Calendar initialDate;
    private static Calendar finalDate;

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();

        for (int i = 0; i < 60; i++) {
            initialDate = Calendar.getInstance();
            initialDate.add(Calendar.MINUTE, -OBSERVATION_WINDOW);
            finalDate = Calendar.getInstance();

            System.out.println("dateTime,cpuPod,memoryPod,cpuNode,memoryNode,score,type");
            calculateScoreNormalized(dataManager);
            calculateScoreNonNormalized(dataManager);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the score without normalizing the data in advance.
     * It assumes that the value is already the mean of the last minute.
     * @param dataManager object used to manipulate the database
     */
    private static void calculateScoreNonNormalized(DataManager dataManager) {
        for (int i = 0; i < OBSERVATION_WINDOW; i++) {
            String strDate = sdf.format(initialDate.getTime());
            Score score = dataManager.getData(strDate);
            if (score != null)
                System.out.println(strDate + "," + score.getCsvLine() + ",singleReading");
            initialDate.add(Calendar.MINUTE, 1);
        }
    }

    private static void calculateScoreNormalized(DataManager dataManager) {

        String strInitialDate = sdf.format(initialDate.getTime());
        String strFinalDate = sdf.format(finalDate.getTime());

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
    }

    private static List<Double> normalizeData(List<Double> valuesCpuPod) {
        LOGGER.debug(valuesCpuPod.toString());
        Double mean = getArithmeticMean(valuesCpuPod);
        LOGGER.debug("Arithmetic Mean:" + mean);
        Double standardDeviation = getStandardDeviation(valuesCpuPod, mean);
        LOGGER.debug("Standard Deviation:" + standardDeviation);
        List<Double> normalizedData = getNormalizedData(valuesCpuPod, mean, standardDeviation);
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
