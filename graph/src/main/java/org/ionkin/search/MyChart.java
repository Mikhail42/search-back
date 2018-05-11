package org.ionkin.search;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import static java.lang.Math.log;

public class MyChart extends ApplicationFrame {
    public MyChart(int[] ys, int step) throws Exception {
        super("Frequence");

        final XYSeries xySeries = new XYSeries("First");
        for (int i = 1000; i < ys.length; i += step) {
            xySeries.add(log(ys.length - i), 0.98 * log(ys[i]));
        }
        final XYSeriesCollection dataset = new XYSeriesCollection(xySeries);

        JFreeChart chart = ChartFactory.createScatterPlot("Frequence", // Title
                "ln(№)", // x-axis Label
                "ln(frequence)", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                false, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1024, 1024));
        setContentPane(chartPanel);
    }
}