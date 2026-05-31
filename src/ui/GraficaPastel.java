package ui;

import modelo.Prestamo;
import modelo.SistemasDulcesDados;
import modelo.TipoVenta;
import modelo.Venta;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

public class GraficaPastel extends JPanel {

    private SistemasDulcesDados sistema;

    public GraficaPastel(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout());
        add(crearGrafica(), BorderLayout.CENTER);
    }

    private ChartPanel crearGrafica() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        int copiasPrestadas = 0;
        int ventasJuegos = 0;

        if (sistema != null && sistema.getCafe() != null) {
            List<Prestamo> prestamos = sistema.getCafe().getPrestamos();

            if (prestamos != null) {
                for (Prestamo prestamo : prestamos) {
                    if (prestamo.getDetalles() != null) {
                        copiasPrestadas = copiasPrestadas + prestamo.getDetalles().size();
                    }
                }
            }

            List<Venta> ventas = sistema.getCafe().getVentas();

            if (ventas != null) {
                for (Venta venta : ventas) {
                    if (venta.getTipoVenta() == TipoVenta.TIENDA_JUEGOS) {
                        ventasJuegos++;
                    }
                }
            }
        }

        dataset.setValue("Copias prestadas", copiasPrestadas);
        dataset.setValue("Ventas de juegos", ventasJuegos);

        JFreeChart chart = ChartFactory.createPieChart(
                "Copias préstamo vs ventas de juegos",
                dataset,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }
}