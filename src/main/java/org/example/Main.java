package org.example;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.Calendar;
import java.util.Date;

public class Main {
    static final double bufferSize = 250 * 995; //kg = m3 * density
    static final double supplyT = 72;
    static final double returnT = 50;
    static final double cpWater = 4.21;
    static final int scanBackward = 24;
    static int solveAt = 0;

    static final double maxLevel = 0.9;
    static final double minLevel = 0.1;

    static boolean[] maxLim;
    static boolean[] minLim;

    static double[] flowDH,flowPD,flowPDcorrected,bufferLevel,timeAxis,powerDH;

    public static void main(String[] args) {

        String filePath = "C:\\javatraining\\bufferDesign\\src\\main\\resources\\data.xlsx";
        double[][] data = ExcelToDoubleArray.readExcelFileToDoubleArray(filePath);

        //Reading district heating power
        powerDH = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            powerDH[i] = data[i][1];
        }

        //Calculate district heating flow kg/h
        calculateDHflow(data);

        //Average the data in 24 hour windows
        int avgWindow = 48;
        averageWindow(data, avgWindow);

        //Calculate production using the buffer limits
        calculateOptimalProductionWithBufferSize(data);

        //Calculate production MW
        double[] powerProduction = new double[flowPDcorrected.length];
        for (int j = 0; j < powerProduction.length; j++) {
            powerProduction[j] = flowPDcorrected[j] * (cpWater * (supplyT - returnT)) / 3600000.0; //flow to MW
        }


        //PLOTTING
        //Calculate time axis
        // Create a time series chart
        //TimeSeries series1 = new TimeSeries("Series 1");
        //Calendar cal = Calendar.getInstance();

       // cal.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        // Dummy data for illustration

        timeAxis = new double[data.length];
        for (int i = 0; i < data.length; i++) {
   //        series1.add(new Hour(cal.getTime()),1); // Random value for demonstration
    //        cal.add(Calendar.HOUR_OF_DAY, 1);
            timeAxis[i] = data[i][0];
        }

        for (int i = 0; i < bufferLevel.length; i++) {
            bufferLevel[i] *= 100.0;
            bufferLevel[i] = Math.max(Math.min(bufferLevel[i],100.0),0.0);
        }
        XYSeries graph1 = makeXYSeries(timeAxis, powerDH, "Energy District Heating");
        XYSeries graph2 = makeXYSeries(timeAxis, powerProduction, "Energy Production");
        XYSeries graph3 = makeXYSeries(timeAxis, bufferLevel, "Buffer charge percentage");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(graph1);
        dataset.addSeries(graph2);

        XYSeriesCollection dataset2 = new XYSeriesCollection();
        dataset2.addSeries(graph3);




        if (data != null) {
            SwingUtilities.invokeLater(() -> {

                DataPlotterDouble example = new DataPlotterDouble("Plot District energy heating vs. Time","Time (hours)","Energy (MW)", "", dataset,dataset2);
                example.setSize(800, 400);
                example.setLocationRelativeTo(null);
                example.setVisible(true);
            });
        }
    }

    private static XYSeries makeXYSeries(double[] axisX, double[] axisY,String name) {
        XYSeries series = new XYSeries(name);
        for (int i = 0; i < timeAxis.length; i++) {
            series.add(axisX[i], axisY[i]);
        }

        return series;
    }

    private static void calculateOptimalProductionWithBufferSize(double[][] data) {
        bufferLevel = new double[data.length];
        bufferLevel[0]=0.5;

        maxLim = new boolean[data.length];
        minLim = new boolean[data.length];

        int i = 0;
        while (i < data.length-1){

            calcNextBufferLevel(i);
            //max als level+1 kleiner dan level
            if((bufferLevel[i] > bufferLevel[i+1]) && bufferLevel[i] > maxLevel){
                i = solveProdMax(i-1);
            }else if((bufferLevel[i] < bufferLevel[i+1]) && bufferLevel[i] < minLevel){
                i = solveProdMin(i-1);
            }else{

                if(i>solveAt){
                    System.out.println("solved at index: " + i);
                    solveAt = i;
                }

                i++;
            }

        }
    }

    private static void averageWindow(double[][] data, int avgWindow) {
        flowPDcorrected = new double[data.length]; //kg/h
        flowPD = new double[data.length]; //kg/h
        double avg = 0;
        for (int i = 0; i < flowPD.length; i++) {

            if(i% avgWindow == 0 || i ==0){
                avg = 0;
                for (int j = 0; j < avgWindow; j++) {
                    if(i+j >= data.length) break;
                    double valueMW = data[i+j][1];
                    avg += valueMW / avgWindow;
                }
            }

            flowPDcorrected[i] = flowPD[i] = 3600000.0 * avg / (cpWater * (supplyT - returnT));
        }
    }

    private static void calculateDHflow(double[][] data) {
        flowDH = new double[data.length]; //kg/h
        for (int i = 0; i < flowDH.length; i++) {
            flowDH[i] = 3600000.0 * data[i][1] / (cpWater * (supplyT - returnT));
        }
    }

    private static int solveProdMax(int i) {

        int startIndex;
        for (startIndex = i; startIndex >= i-scanBackward; startIndex--) {
            if(startIndex <= 0) break;

            if(minLim[startIndex])
            {
                startIndex++;
                break;
            }
        }

        double maxFlow = 0;
        for (int j = startIndex; j <= i ; j++) {
            if(minLim[j])continue;
            maxFlow = flowPDcorrected[j] > maxFlow ? flowPDcorrected[j] : maxFlow;
        }

        double newMaxflow = 0.99 * maxFlow;

        for (int j = startIndex; j <= i ; j++) {
            if(minLim[j])continue;
            flowPDcorrected[j] = newMaxflow < flowPDcorrected[j] ? newMaxflow : flowPDcorrected[j];
            maxLim[i] = true;
        }

        return startIndex;

    }

    private static int solveProdMin(int i) {
        int startIndex;
        for (startIndex = i; startIndex >= i-scanBackward; startIndex--) {
            if(startIndex == 0) break;

            if(maxLim[startIndex])
            {
                startIndex++;
                break;
            }
        }

        double minFlow = Double.MAX_VALUE;
        for (int j = startIndex; j <= i ; j++) {
            if(maxLim[j])continue;
            minFlow = flowPDcorrected[j] < minFlow ? flowPDcorrected[j] : minFlow;
        }

        double newMinflow = 1.01 * minFlow;

        for (int j = startIndex; j <= i ; j++) {
            if(maxLim[j])continue;
            flowPDcorrected[j] = newMinflow > flowPDcorrected[j] ? newMinflow : flowPDcorrected[j];
            minLim[i] = true;
        }

        return startIndex;

    }

    private static void calcNextBufferLevel(int i) {
        if(i >= bufferLevel.length -1) return;
        bufferLevel[i +1] = bufferLevel[i] + ((flowPDcorrected[i] - flowDH[i]) / bufferSize); //level for next hour
    }

}