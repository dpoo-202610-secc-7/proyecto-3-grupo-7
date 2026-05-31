package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelTurnos extends JPanel {

    private SistemasDulcesDados sistema;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelTurnos(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Empleado", "Cargo", "Día", "Hora Inicio", "Hora Fin"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar Turno");
        JButton btnRefresh = new JButton("Actualizar");
        panelBotones.add(btnAgregar);
        panelBotones.add(btnRefresh);
        add(panelBotones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarTurno());
        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Empleado> empleados = sistema.getCafe().getEmpleados();
        if (empleados == null) return;
        for (Empleado emp : empleados) {
            List<Turno> turnos = emp.getTurnos();
            if (turnos == null || turnos.isEmpty()) {
                modeloTabla.addRow(new Object[]{
                    emp.getNombre(), emp.getTipoUsuario(), "Sin turno", "-", "-"
                });
            } else {
                for (Turno t : turnos) {
                    modeloTabla.addRow(new Object[]{
                        emp.getNombre(),
                        emp.getTipoUsuario(),
                        t.getDia(),
                        t.getHoraInicio(),
                        t.getHoraFin()
                    });
                }
            }
        }
    }

    private void agregarTurno() {
        List<Empleado> empleados = sistema.getCafe().getEmpleados();
        if (empleados == null || empleados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay empleados registrados.");
            return;
        }

        // Selector de empleado
        String[] nombres = empleados.stream()
            .map(e -> e.getNombre() + " (" + e.getTipoUsuario() + ")")
            .toArray(String[]::new);
        String seleccion = (String) JOptionPane.showInputDialog(this,
            "Selecciona empleado:", "Agregar Turno",
            JOptionPane.PLAIN_MESSAGE, null, nombres, nombres[0]);
        if (seleccion == null) return;
        int idx = java.util.Arrays.asList(nombres).indexOf(seleccion);
        Empleado empleado = empleados.get(idx);

        // Selector de día
        DiaSemana[] dias = DiaSemana.values();
        DiaSemana dia = (DiaSemana) JOptionPane.showInputDialog(this,
            "Día:", "Agregar Turno",
            JOptionPane.PLAIN_MESSAGE, null, dias, dias[0]);
        if (dia == null) return;

        JTextField txtInicio = new JTextField("08:00");
        JTextField txtFin    = new JTextField("16:00");
        Object[] campos = {"Hora inicio (HH:mm):", txtInicio, "Hora fin (HH:mm):", txtFin};
        int result = JOptionPane.showConfirmDialog(this, campos, "Horario", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        Turno turno = new Turno(dia, txtInicio.getText().trim(), txtFin.getText().trim());
        empleado.agregarTurno(turno);
        cargarTabla();
        JOptionPane.showMessageDialog(this, "Turno agregado.");
    }
}