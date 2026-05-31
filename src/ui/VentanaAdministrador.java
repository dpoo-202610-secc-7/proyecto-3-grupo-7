package ui;

import modelo.*;
import javax.swing.*;
import java.awt.*;

public class VentanaAdministrador extends JFrame {

    private SistemasDulcesDados sistema;
    private Administrador admin;

    public VentanaAdministrador(SistemasDulcesDados sistema, Administrador admin) {
        this.sistema = sistema;
        this.admin = admin;
        setTitle("Dulces & Dados — Administrador: " + admin.getNombre());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
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
        tabs.addTab("Usuarios", new PanelUsuarios(sistema));
        tabs.addTab("Inventario", new PanelInventario(sistema));;
        tabs.addTab("Turnos",      new JLabel("Panel Turnos — próximamente"));
        tabs.addTab("Ventas",      new JLabel("Panel Ventas — próximamente"));
        tabs.addTab("Sugerencias", new JLabel("Panel Sugerencias — próximamente"));
        add(tabs, BorderLayout.CENTER);
    }
}