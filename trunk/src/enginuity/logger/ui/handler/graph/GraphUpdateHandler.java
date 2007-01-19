/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.ui.handler.graph;

import java.awt.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;
import static enginuity.logger.ui.handler.graph.SpringUtilities.makeCompactGrid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public final class GraphUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private static final Color RED = new Color(190, 30, 30);
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private final JPanel graphPanel;
    private final Map<EcuData, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuData, ChartPanel>());
    private final Map<EcuData, XYSeries> seriesMap = synchronizedMap(new HashMap<EcuData, XYSeries>());
    private int loggerCount = 0;

    public GraphUpdateHandler(JPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    public void registerData(EcuData ecuData) {
        // add to charts
        final XYSeries series = new XYSeries(ecuData.getName());
        //TODO: Make chart max item count configurable via settings
        series.setMaximumItemCount(200);
        ChartPanel chartPanel = new ChartPanel(createXYLineChart(series, ecuData), false, true, true, true, true);
        graphPanel.add(chartPanel);
        seriesMap.put(ecuData, series);
        chartMap.put(ecuData, chartPanel);
        makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(2);
    }

    public synchronized void handleDataUpdate(EcuData ecuData, double value, long timestamp) {
        // update chart
        XYSeries series = seriesMap.get(ecuData);
        if (series != null) {
            series.add(timestamp/1000.0, value);
        }
    }

    public void deregisterData(EcuData ecuData) {
        // remove from charts
        graphPanel.remove(chartMap.get(ecuData));
        chartMap.remove(ecuData);
        makeCompactGrid(graphPanel, --loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(1);
    }

    public void cleanUp() {
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        if (chartMap.containsKey(updatedEcuData)) {
            seriesMap.get(updatedEcuData).clear();
            JFreeChart chart = chartMap.get(updatedEcuData).getChart();
            chart.getXYPlot().getRangeAxis().setLabel(buildRangeAxisTitle(updatedEcuData));
        }
    }

    private JFreeChart createXYLineChart(XYSeries series, EcuData ecuData) {
        final XYDataset xyDataset = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(ecuData.getName(), "Time (sec)", buildRangeAxisTitle(ecuData), xyDataset,
                        VERTICAL, false, true, false);
        chart.setBackgroundPaint(BLACK);
        chart.getTitle().setPaint(WHITE);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BLACK);
        plot.getRenderer().setPaint(RED);
        plot.getDomainAxis().setLabelPaint(WHITE);
        plot.getRangeAxis().setLabelPaint(WHITE);
        plot.getDomainAxis().setTickLabelPaint(LIGHT_GREY);
        plot.getRangeAxis().setTickLabelPaint(LIGHT_GREY);
        plot.setDomainGridlinePaint(DARK_GREY);
        plot.setRangeGridlinePaint(DARK_GREY);
        plot.setOutlinePaint(DARK_GREY);
        return chart;
    }

    private String buildRangeAxisTitle(EcuData ecuData) {
        return ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ")";
    }

    private void repaintGraphPanel(int parentRepaintLevel) {
        if (loggerCount < parentRepaintLevel) {
            graphPanel.doLayout();
            graphPanel.repaint();
        } else {
            if (loggerCount == 1) {
                graphPanel.doLayout();
            }
            graphPanel.getParent().doLayout();
            graphPanel.getParent().repaint();
        }
    }
}

