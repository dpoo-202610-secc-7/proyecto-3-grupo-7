package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelSugerenciasEmpleado extends JPanel {

    private SistemasDulcesDados sistema;
    private Empleado empleado;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelSugerenciasEmpleado(SistemasDulcesDados sistema, Empleado empleado) {
        this.sistema = sistema;
        this.empleado = empleado;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarTabla();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel(
            new String[] {"Nombre propuesto", "Categoría", "Fecha/Hora", "Estado", "Empleado"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnCrear = new JButton("Crear sugerencia");
        JButton btnActualizar = new JButton("Actualizar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);

        add(panelBotones, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearSugerencia());
        btnActualizar.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);

        for (SugerenciaPlatillo sugerencia : sistema.getSugerencias()) {
            if (sugerencia.getEmpleado() == empleado) {
                modeloTabla.addRow(new Object[] {
                    sugerencia.getNombrePropuesto(),
                    sugerencia.getCategoriaPropuesta(),
                    sugerencia.getFechaHora(),
                    sugerencia.getEstado(),
                    empleado.getNombre()
                });
            }
        }
    }

    private void crearSugerencia() {
        JTextField txtNombre = new JTextField();
        JComboBox<CategoriaPropuesta> comboCategoria = new JComboBox<CategoriaPropuesta>(CategoriaPropuesta.values());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

        panel.add(new JLabel("Nombre del platillo o bebida:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Categoría:"));
        panel.add(comboCategoria);

        int opcion = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Nueva sugerencia",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe un nombre para la sugerencia.");
            return;
        }

        CategoriaPropuesta categoria = (CategoriaPropuesta) comboCategoria.getSelectedItem();

        SugerenciaPlatillo sugerencia = empleado.crearSugerenciaPlatillo(nombre, categoria);

        sugerencia.setEstado(EstadoSugerencia.PENDIENTE);
        sugerencia.setFechaHora(fechaActual());

        sistema.agregarSugerencia(sugerencia);

        sistema.guardarDatos();

        cargarTabla();

        JOptionPane.showMessageDialog(this, "Sugerencia registrada correctamente.");
    }

    private String fechaActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}