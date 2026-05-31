package ui;

import modelo.*;
import javax.swing.*;
import java.awt.*;

public class VentanaEmpleado extends JFrame {

    private SistemasDulcesDados sistema;
    private Empleado empleado;

    public VentanaEmpleado(SistemasDulcesDados sistema, Empleado empleado) {
        this.sistema = sistema;
        this.empleado = empleado;

        setTitle("Dulces & Dados — Empleado: " + empleado.getNombre());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);

        initUI();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                sistema.guardarDatos();
                System.exit(0);
            }
        });
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Torneos", new PanelTorneos(sistema, empleado));
        tabs.addTab("Reservas", new PanelReservas(sistema, null));
        tabs.addTab("Préstamos", new PanelPrestamos(sistema, empleado));
        tabs.addTab("Compras", new PanelCompras(sistema, empleado));
        tabs.addTab("Sugerencias", new PanelSugerenciasEmpleado(sistema, empleado));

        add(tabs, BorderLayout.CENTER);
    }
}