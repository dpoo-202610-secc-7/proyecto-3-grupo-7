package ui;

import modelo.*;
import javax.swing.*;
import java.awt.*;

public class VentanaCliente extends JFrame {

    private SistemasDulcesDados sistema;
    private Cliente cliente;

    public VentanaCliente(SistemasDulcesDados sistema, Cliente cliente) {
        this.sistema = sistema;
        this.cliente = cliente;

        setTitle("Dulces & Dados — Cliente: " + cliente.getNombre());
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

        tabs.addTab("Torneos", new PanelTorneos(sistema, cliente));
        tabs.addTab("Reservas", new PanelReservas(sistema, cliente));
        tabs.addTab("Préstamos", new PanelPrestamos(sistema, cliente));
        tabs.addTab("Compras", new PanelCompras(sistema, cliente));

        add(tabs, BorderLayout.CENTER);
    }
}