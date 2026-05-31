package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelReservas extends JPanel {

    private SistemasDulcesDados sistema;
    private Cliente cliente;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelReservas(SistemasDulcesDados sistema, Cliente cliente) {
        this.sistema = sistema;
        this.cliente = cliente;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Mesa", "Fecha/Hora", "Personas", "Niños < 5", "Menores", "Estado", "Cliente"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnCrear = new JButton("Crear reserva");
        JButton btnCerrar = new JButton("Cerrar reserva activa");
        JButton btnRefresh = new JButton("Actualizar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnCerrar);
        panelBotones.add(btnRefresh);

        add(panelBotones, BorderLayout.SOUTH);

        btnCrear.setEnabled(cliente != null);

        btnCrear.addActionListener(e -> crearReserva());
        btnCerrar.addActionListener(e -> cerrarReserva());
        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);

        if (sistema == null || sistema.getCafe() == null || sistema.getCafe().getMesas() == null) {
            return;
        }

        for (Mesa mesa : sistema.getCafe().getMesas()) {
            for (ReservaMesa reserva : mesa.getReservas()) {
                if (cliente == null || reserva.getCliente() == cliente) {
                    String nombreCliente = "—";

                    if (reserva.getCliente() != null) {
                        nombreCliente = reserva.getCliente().getNombre();
                    }

                    modeloTabla.addRow(new Object[] {
                        mesa.getNumeroMesa(),
                        reserva.getFechaHora(),
                        reserva.getNumeroPersonas(),
                        reserva.isHayNinosMenores5() ? "Sí" : "No",
                        reserva.isHayMenoresEdad() ? "Sí" : "No",
                        reserva.getEstadoReserva(),
                        nombreCliente
                    });
                }
            }
        }
    }

    private void crearReserva() {
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Solo un cliente puede crear reservas.");
            return;
        }

        JTextField txtPersonas = new JTextField("2");
        JCheckBox chkNinos = new JCheckBox("Hay niños menores de 5 años");
        JCheckBox chkMenores = new JCheckBox("Hay menores de edad");

        JTextField txtFecha = new JTextField(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

        panel.add(new JLabel("Fecha/Hora:"));
        panel.add(txtFecha);
        panel.add(new JLabel("Número de personas:"));
        panel.add(txtPersonas);
        panel.add(chkNinos);
        panel.add(chkMenores);

        int opcion = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Nueva reserva",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int personas = Integer.parseInt(txtPersonas.getText().trim());

            if (personas <= 0) {
                JOptionPane.showMessageDialog(this, "El número de personas debe ser mayor a 0.");
                return;
            }

            String fecha = txtFecha.getText().trim();

            Mesa mesa = sistema.getCafe().buscarMesaDisponible(personas, fecha);

            if (mesa == null) {
                JOptionPane.showMessageDialog(this, "No hay una mesa disponible para esa reserva.");
                return;
            }

            if (!sistema.getCafe().verificarCapacidadDisponible(personas)) {
                JOptionPane.showMessageDialog(this, "No hay capacidad disponible en el café.");
                return;
            }

            ReservaMesa reserva = cliente.crearReserva(
                mesa,
                fecha,
                personas,
                chkNinos.isSelected(),
                chkMenores.isSelected()
            );

            reserva.activar();
            mesa.asignarReserva(reserva);

            sistema.guardarDatos();

            cargarTabla();

            JOptionPane.showMessageDialog(this, "Reserva creada en la mesa " + mesa.getNumeroMesa() + ".");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe un número válido de personas.");
        }
    }

    private void cerrarReserva() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva.");
            return;
        }

        int numeroMesa = (Integer) modeloTabla.getValueAt(fila, 0);

        for (Mesa mesa : sistema.getCafe().getMesas()) {
            if (mesa.getNumeroMesa() == numeroMesa) {
                mesa.cerrarReservaActiva();

                sistema.guardarDatos();

                cargarTabla();

                JOptionPane.showMessageDialog(this, "Reserva activa cerrada.");
                return;
            }
        }
    }
}