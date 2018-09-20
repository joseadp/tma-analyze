package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.Date;

import eubr.atmosphere.tma.analyze.database.DataManager;

public class Main {

    public static void main(String[] args) {
        DataManager dataManager = new DataManager();

        int cpuDescriptionId = 27;
        int memoryDescriptionId = 28;

        int podId = 8; //kafka
        int nodeId = 11;

        String stringTime = "2018-09-20 11:10";

        for (int i = 0; i < 60; i++) {
            Date date = new Date(118, 8, 20, 11, 10 + i);

            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");
            String strDate = sdf.format(date).trim();
            System.out.println("Nova: " + strDate);

            dataManager.getData(strDate);
        }

    }

}
