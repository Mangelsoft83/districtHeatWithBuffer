package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;

public class DataPlotter extends JFrame {

    public DataPlotter(String title, String XaxisLabel, String YaxisLabel, XYSeriesCollection dataset) {
        super(title);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                XaxisLabel,
                YaxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the chart
        chart.setBackgroundPaint(Color.white);

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

