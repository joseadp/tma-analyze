package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eubr.atmosphere.tma.analyze.database.DataManager;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class Main {

    private final static int minutes = 240;

    /** OBSERVATION_WINDOW: window that the readings will be used to calculate the score (in minutes) */
    private static int OBSERVATION_WINDOW = 20;

    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");

    // 2018-09-24 18:00
    private static final int INITIAL_YEAR = 118;
    private static final int INITIAL_MONTH = 8;
    private static final int INITIAL_DAY = 24;
    private static final int INITIAL_HOUR = 18;

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();

        for (int i = 0; i < 60; i++) {
            calculateScoreNonNormalized(dataManager);
            calculateScoreNormalized(dataManager);
            try {
                Thread.sleep(2000);
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
        System.out.println("dateTime,cpuPod,memoryPod,cpuNode,memoryNode,score");
        for (int i = 0; i < minutes; i++) {
            Date date = new Date(INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY, INITIAL_HOUR, 0 + i);
            String strDate = sdf.format(date);
            Score score = dataManager.getData(strDate);
            if (score != null)
                System.out.println(strDate + "," + score.getCsvLine());
        }
    }

    private static void calculateScoreNormalized(DataManager dataManager) {
        /*Date initialDate = new Date(118, 8, 24, 13, 21); //2018-09-24 13:21
        Date finalDate = new Date(118, 8, 24, 13, 21 + 60);*/

        Date initialDate = new Date(INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY,
                INITIAL_HOUR, 0);
        Date finalDate = new Date(INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY,
                INITIAL_HOUR, 0 + OBSERVATION_WINDOW);

        List<Double> valuesCpuPod =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.cpuDescriptionId, Constants.podId);
        List<Double> valuesCpuPodNormalized = normalizeData(valuesCpuPod);

        List<Double> valuesMemoryPod =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.memoryDescriptionId, Constants.podId);
        List<Double> valuesMemoryPodNormalized = normalizeData(valuesMemoryPod);

        List<Double> valuesCpuNode =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.cpuDescriptionId, Constants.nodeId);
        List<Double> valuesCpuNodeNormalized = normalizeData(valuesCpuNode);

        List<Double> valuesMemoryNode =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.memoryDescriptionId, Constants.nodeId);
        List<Double> valuesMemoryNodeNormalized = normalizeData(valuesMemoryNode);

        ////////////////////////////////////////////////////////////////////////////////////
        System.out.println("Size CPU-Pod: " + valuesCpuPod.size());
        System.out.println("Size Memory-Pod: " + valuesMemoryPod.size());
        System.out.println("Size CPU-Node: " + valuesCpuNode.size());
        System.out.println("Size Memory-Node: " + valuesMemoryNode.size());

        ////////////////////////////////////////////////////////////////////////////////////
        Score scoreNonNormalized = new Score();
        scoreNonNormalized.setCpuPod(getArithmeticMean(valuesCpuPod));
        scoreNonNormalized.setMemoryPod(getArithmeticMean(valuesMemoryPod));
        scoreNonNormalized.setCpuNode(getArithmeticMean(valuesCpuNode));
        scoreNonNormalized.setMemoryNode(getArithmeticMean(valuesMemoryNode));
        System.out.println(scoreNonNormalized);
        System.out.println("Score:" + scoreNonNormalized.getScore());
        System.out.println(scoreNonNormalized.getCsvLine());

        ////////////////////////////////////////////////////////////////////////////////////
        Score scoreNormalized = new Score();
        scoreNormalized.setCpuPod(getArithmeticMean(valuesCpuPodNormalized));
        scoreNormalized.setMemoryPod(getArithmeticMean(valuesMemoryPodNormalized));
        scoreNormalized.setCpuNode(getArithmeticMean(valuesCpuNodeNormalized));
        scoreNormalized.setMemoryNode(getArithmeticMean(valuesMemoryNodeNormalized));
        System.out.println(scoreNormalized);
        System.out.println("Score:" + scoreNormalized.getScore());
        System.out.println(scoreNormalized.getCsvLine());
    }

    private static List<Double> normalizeData(List<Double> valuesCpuPod) {
        System.out.println(valuesCpuPod);
        Double mean = getArithmeticMean(valuesCpuPod);
        System.out.println("Arithmetic Mean:" + mean);
        Double standardDeviation = getStandardDeviation(valuesCpuPod, mean);
        System.out.println("Standard Deviation:" + standardDeviation);
        List<Double> normalizedData = getNormalizedData(valuesCpuPod, mean, standardDeviation);
        System.out.println("Normalized Data:" + normalizedData);
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
