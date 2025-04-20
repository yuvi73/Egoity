package com.simego.visualization;

import com.simego.core.SelfModel.MirroringDataPoint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class MirroringVisualizer extends JFrame {
    private final TimeSeries mirroringSeries;
    private final TimeSeries learningRateSeries;
    private final TimeSeries thresholdSeries;
    private final TimeSeries valenceSeries;

    public MirroringVisualizer() {
        super("Mirroring Behavior Visualization");
        
        // Create time series for different metrics
        mirroringSeries = new TimeSeries("Mirroring Value");
        learningRateSeries = new TimeSeries("Learning Rate");
        thresholdSeries = new TimeSeries("Recognition Threshold");
        valenceSeries = new TimeSeries("Memory Valence");

        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(mirroringSeries);
        dataset.addSeries(learningRateSeries);
        dataset.addSeries(thresholdSeries);
        dataset.addSeries(valenceSeries);

        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Mirroring Behavior Over Time",
            "Time",
            "Value",
            dataset,
            true,
            true,
            false
        );

        // Customize plot
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);    // Mirroring
        renderer.setSeriesPaint(1, Color.GREEN);   // Learning Rate
        renderer.setSeriesPaint(2, Color.RED);     // Threshold
        renderer.setSeriesPaint(3, Color.ORANGE);  // Valence
        plot.setRenderer(renderer);

        // Create panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    public void updateData(List<MirroringDataPoint> dataPoints) {
        for (MirroringDataPoint point : dataPoints) {
            Millisecond time = new Millisecond(new Date(point.getTimestamp()));
            mirroringSeries.add(time, point.getUpdateValue());
            learningRateSeries.add(time, point.getLearningRate());
            thresholdSeries.add(time, point.getThreshold());
            valenceSeries.add(time, point.getValence());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MirroringVisualizer visualizer = new MirroringVisualizer();
            visualizer.pack();
            visualizer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            visualizer.setVisible(true);
        });
    }
} 