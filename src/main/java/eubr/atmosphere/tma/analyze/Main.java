package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.Date;

import eubr.atmosphere.tma.analyze.database.DataManager;

public class Main {

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();

        System.out.println("dateTime,cpuPod,memoryPod,cpuNode,memoryNode,score");
        for (int i = 0; i < 60; i++) {
            Date date = new Date(118, 8, 20, 11, 10 + i);

            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");
            String strDate = sdf.format(date).trim();

            Score score = dataManager.getData(strDate);
            if (score != null)
                System.out.println(strDate + "," + score.getCsvLine());
        }

    }

}
