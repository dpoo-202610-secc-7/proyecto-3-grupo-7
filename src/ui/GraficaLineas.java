package ui;

import modelo.ReservaMesa;
import modelo.SistemasDulcesDados;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class GraficaLineas extends JPanel {

    private SistemasDulcesDados sistema;

    public GraficaLineas(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout());
        add(crearGrafica(), BorderLayout.CENTER);
    }

    private ChartPanel crearGrafica() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int lunes = 0;
        int martes = 0;
        int miercoles = 0;
        int jueves = 0;
        int viernes = 0;
        int sabado = 0;
        int domingo = 0;

        if (sistema != null && sistema.getCafe() != null) {
            List<ReservaMesa> reservas = sistema.getCafe().getAllReservas();

            if (reservas != null) {
                for (ReservaMesa reserva : reservas) {
                    String fechaHora = reserva.getFechaHora();

                    if (fechaHora != null && fechaHora.length() >= 10) {
                        try {
                            LocalDate fecha = LocalDate.parse(fechaHora.substring(0, 10));
                            DayOfWeek dia = fecha.getDayOfWeek();

                            if (dia == DayOfWeek.MONDAY) {
                                lunes++;
                            } else if (dia == DayOfWeek.TUESDAY) {
                                martes++;
                            } else if (dia == DayOfWeek.WEDNESDAY) {
                                miercoles++;
                            } else if (dia == DayOfWeek.THURSDAY) {
                                jueves++;
                            } else if (dia == DayOfWeek.FRIDAY) {
                                viernes++;
                            } else if (dia == DayOfWeek.SATURDAY) {
                                sabado++;
                            } else if (dia == DayOfWeek.SUNDAY) {
                                domingo++;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        dataset.addValue(lunes, "Reservas", "Lunes");
        dataset.addValue(martes, "Reservas", "Martes");
        dataset.addValue(miercoles, "Reservas", "Miércoles");
        dataset.addValue(jueves, "Reservas", "Jueves");
        dataset.addValue(viernes, "Reservas", "Viernes");
        dataset.addValue(sabado, "Reservas", "Sábado");
        dataset.addValue(domingo, "Reservas", "Domingo");

        JFreeChart chart = ChartFactory.createLineChart(
                "Reservas por día de la semana",
                "Día",
                "Cantidad",
                dataset
        );

        return new ChartPanel(chart);
    }
}