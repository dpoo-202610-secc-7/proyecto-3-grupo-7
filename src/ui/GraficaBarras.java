package ui;

import modelo.SistemasDulcesDados;
import modelo.TipoVenta;
import modelo.Venta;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;

public class GraficaBarras extends JPanel {

    private SistemasDulcesDados sistema;

    public GraficaBarras(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout());
        add(crearGrafica(), BorderLayout.CENTER);
    }

    private ChartPanel crearGrafica() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int cafeteria = 0;
        int tiendaJuegos = 0;

        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(4);

        if (sistema != null) {
            List<Venta> ventas = sistema.getVentasPorRango(inicio, fin);

            if (ventas != null) {
                for (Venta venta : ventas) {
                    if (venta.getTipoVenta() == TipoVenta.CAFETERIA) {
                        cafeteria++;
                    } else if (venta.getTipoVenta() == TipoVenta.TIENDA_JUEGOS) {
                        tiendaJuegos++;
                    }
                }
            }
        }

        dataset.addValue(cafeteria, "Ventas", "Cafetería");
        dataset.addValue(tiendaJuegos, "Ventas", "Tienda Juegos");

        JFreeChart chart = ChartFactory.createBarChart(
                "Ventas cafetería/juegos en últimos 5 días",
                "Tipo",
                "Cantidad",
                dataset
        );

        return new ChartPanel(chart);
    }
}