package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelSugerencias extends JPanel {

    private SistemasDulcesDados sistema;
    private Administrador admin;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelSugerencias(SistemasDulcesDados sistema, Administrador admin) {
        this.sistema = sistema;
        this.admin = admin;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarTabla();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel(
            new String[] {"Nombre propuesto", "Categoría", "Fecha/Hora", "Estado", "Empleado", "Revisada por"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnAprobar = new JButton("Aprobar");
        JButton btnRechazar = new JButton("Rechazar");
        JButton btnActualizar = new JButton("Actualizar");

        panelBotones.add(btnAprobar);
        panelBotones.add(btnRechazar);
        panelBotones.add(btnActualizar);

        add(panelBotones, BorderLayout.SOUTH);

        btnAprobar.addActionListener(e -> cambiarEstado(true));
        btnRechazar.addActionListener(e -> cambiarEstado(false));
        btnActualizar.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);

        for (SugerenciaPlatillo sugerencia : sistema.getSugerencias()) {
            String nombreEmpleado = "—";
            String nombreAdmin = "—";

            if (sugerencia.getEmpleado() != null) {
                nombreEmpleado = sugerencia.getEmpleado().getNombre();
            }

            if (sugerencia.getRevisadaPor() != null) {
                nombreAdmin = sugerencia.getRevisadaPor().getNombre();
            }

            modeloTabla.addRow(new Object[] {
                sugerencia.getNombrePropuesto(),
                sugerencia.getCategoriaPropuesta(),
                sugerencia.getFechaHora(),
                sugerencia.getEstado(),
                nombreEmpleado,
                nombreAdmin
            });
        }
    }

    private void cambiarEstado(boolean aprobar) {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una sugerencia primero.");
            return;
        }

        SugerenciaPlatillo sugerencia = sistema.getSugerencias().get(fila);

        if (!sugerencia.estaPendiente()) {
            JOptionPane.showMessageDialog(this, "Esta sugerencia ya fue revisada.");
            return;
        }

        if (aprobar) {
            sugerencia.aprobar();
            sugerencia.setRevisadaPor(admin);
            JOptionPane.showMessageDialog(this, "Sugerencia aprobada.");
        } else {
            sugerencia.rechazar();
            sugerencia.setRevisadaPor(admin);
            JOptionPane.showMessageDialog(this, "Sugerencia rechazada.");
        }

        sistema.guardarDatos();

        cargarTabla();
    }
}