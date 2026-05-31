package ui;

import modelo.SistemasDulcesDados;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class VentanaGraficas extends JPanel {

    private SistemasDulcesDados sistema;

    public VentanaGraficas(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout());
        inicializar();
    }

    private void inicializar() {
        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab("Copias préstamo vs ventas", new GraficaPastel(sistema));
        pestanas.addTab("Ventas últimos 5 días", new GraficaBarras(sistema));
        pestanas.addTab("Reservas por semana", new GraficaLineas(sistema));

        add(pestanas, BorderLayout.CENTER);
    }
}
