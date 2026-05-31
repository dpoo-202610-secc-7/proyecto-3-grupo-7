package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelSugerencias extends JPanel {

    private SistemasDulcesDados sistema;
    private Administrador admin;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelSugerencias(SistemasDulcesDados sistema, Administrador admin) {
        this.sistema = sistema;
        this.admin   = admin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Nombre Propuesto", "Categoría", "Fecha/Hora", "Estado", "Empleado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAprobar  = new JButton("Aprobar");
        JButton btnRechazar = new JButton("Rechazar");
        JButton btnRefresh  = new JButton("Actualizar");
        panelBotones.add(btnAprobar);
        panelBotones.add(btnRechazar);
        panelBotones.add(btnRefresh);
        add(panelBotones, BorderLayout.SOUTH);

        btnAprobar.addActionListener(e  -> cambiarEstado(true));
        btnRechazar.addActionListener(e -> cambiarEstado(false));
        btnRefresh.addActionListener(e  -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<SugerenciaPlatillo> sugerencias = sistema.getSugerencias();
        if (sugerencias == null) return;
        for (SugerenciaPlatillo s : sugerencias) {
            String empleado = s.getEmpleado() != null ? s.getEmpleado().getNombre() : "—";
            modeloTabla.addRow(new Object[]{
                s.getNombrePropuesto(),
                s.getCategoriaPropuesta(),
                s.getFechaHora(),
                s.getEstado(),
                empleado
            });
        }
    }

    private void cambiarEstado(boolean aprobar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una sugerencia primero.");
            return;
        }
        SugerenciaPlatillo sug = sistema.getSugerencias().get(fila);
        if (!sug.estaPendiente()) {
            JOptionPane.showMessageDialog(this, "Solo se pueden gestionar sugerencias PENDIENTES.");
            return;
        }
        if (aprobar) {
            sug.aprobar();
            sug.setRevisadaPor(admin);
        } else {
            sug.rechazar();
            sug.setRevisadaPor(admin);
        }
        cargarTabla();
    }
}