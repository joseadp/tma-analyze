package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eubr.atmosphere.tma.analyze.database.DataManager;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class Main {

    final private static int minutes = 240;

    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();

        calculateScoreNonNormalized(dataManager);
        calculateScoreNormalized(dataManager);
    }

    private static void calculateScoreNonNormalized(DataManager dataManager) {
        System.out.println("dateTime,cpuPod,memoryPod,cpuNode,memoryNode,score");
        for (int i = 0; i < minutes; i++) {
            Date date = new Date(118, 8, 20, 11, 10 + i);
            String strDate = sdf.format(date);
            Score score = dataManager.getData(strDate);
            if (score != null)
                System.out.println(strDate + "," + score.getCsvLine());
        }
    }

    private static void calculateScoreNormalized(DataManager dataManager) {
        /*Date initialDate = new Date(118, 8, 24, 13, 21); //2018-09-24 13:21
        Date finalDate = new Date(118, 8, 24, 13, 21 + 60);*/

        Date initialDate = new Date(118, 8, 20, 13, 04); //2018-09-24 13:21
        Date finalDate = new Date(118, 8, 20, 13, 04 + 60);

        List<Double> valuesCpuPod =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.cpuDescriptionId, Constants.podId);
        normalizeData(valuesCpuPod);

        List<Double> valuesMemoryPod =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.memoryDescriptionId, Constants.podId);
        normalizeData(valuesMemoryPod);

        List<Double> valuesCpuNode =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.cpuDescriptionId, Constants.nodeId);
        normalizeData(valuesCpuNode);

        List<Double> valuesMemoryNode =
                dataManager.getValuesPeriod(sdf.format(initialDate), sdf.format(finalDate),
                Constants.memoryDescriptionId, Constants.nodeId);
        normalizeData(valuesMemoryNode);

        ////////////////////////////////////////////////////////////////////////////////////
        System.out.println("Size CPU-Pod: " + valuesCpuPod.size());
        System.out.println("Size Memory-Pod: " + valuesMemoryPod.size());
        System.out.println("Size CPU-Node: " + valuesCpuNode.size());
        System.out.println("Size Memory-Node: " + valuesMemoryNode.size());
    }

    private static void normalizeData(List<Double> valuesCpuPod) {
        System.out.println(valuesCpuPod);
        Double mean = getArithmeticMean(valuesCpuPod);
        System.out.println("Arithmetic Mean:" + mean);
        Double standardDeviation = getStandardDeviation(valuesCpuPod, mean);
        System.out.println("Standard Deviation:" + standardDeviation);
        System.out.println("Normalized Data:" + getNormalizedData(valuesCpuPod, mean, standardDeviation));
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
