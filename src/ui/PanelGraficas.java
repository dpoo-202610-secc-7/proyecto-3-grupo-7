package ui;

import modelo.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelGraficas extends JPanel {

    private SistemasDulcesDados sistema;

    public PanelGraficas(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JTabbedPane graficas = new JTabbedPane();
        graficas.addTab("Ventas por Tipo", crearGraficaVentas());
        add(graficas, BorderLayout.CENTER);
    }

    private ChartPanel crearGraficaVentas() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Venta> ventas = sistema.getCafe().getVentas();
        int cafeteria = 0, tienda = 0, otro = 0;

        if (ventas != null) {
            for (Venta v : ventas) {
                if (v.getTipoVenta() == TipoVenta.CAFETERIA) cafeteria++;
                else if (v.getTipoVenta() == TipoVenta.TIENDA_JUEGOS) tienda++;
                else otro++;
            }
        }

        dataset.addValue(cafeteria, "Ventas", "Cafetería");
        dataset.addValue(tienda,    "Ventas", "Tienda Juegos");
        if (otro > 0) dataset.addValue(otro, "Ventas", "Otro");

        JFreeChart chart = ChartFactory.createBarChart(
            "Ventas por Tipo",
            "Tipo",
            "Cantidad",
            dataset
        );

        return new ChartPanel(chart);
    }
}