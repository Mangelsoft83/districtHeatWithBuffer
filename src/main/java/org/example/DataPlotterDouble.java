package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class DataPlotterDouble extends JFrame {

    public DataPlotterDouble(String title, String XaxisLabel, String YaxisLabel1, String YaxisLabel2, XYSeriesCollection dataset1, XYSeriesCollection dataset2) {
        super(title);


        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                XaxisLabel,
                YaxisLabel1,
                dataset1,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the chart with a secondary Y-axis (for Series 2)
        XYPlot plot = chart.getXYPlot();

        NumberAxis axis2 = new NumberAxis(YaxisLabel2);
        axis2.setRange(0.0,100.0);
        //axis2.setTickMarksVisible(false);
       // axis2.setTickLabelsVisible(false);

        plot.setRangeAxis(1, axis2);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.setDataset(1, dataset2);

        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesPaint(0,Color.DARK_GRAY);
        renderer2.setSeriesShapesVisible(0, false);

        plot.setRenderer(1, renderer2);

        // Add the chart to a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}

