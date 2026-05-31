package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelTorneos extends JPanel {

    private SistemasDulcesDados sistema;
    private Usuario usuario;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelTorneos(SistemasDulcesDados sistema, Usuario usuario) {
        this.sistema = sistema;
        this.usuario = usuario;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Nombre", "Día", "Juego", "Cupos", "Premio", "Inscrito"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnInscribir = new JButton("Inscribirse");
        JButton btnSalir = new JButton("Desinscribirse");
        JButton btnRefresh = new JButton("Actualizar");

        panelBotones.add(btnInscribir);
        panelBotones.add(btnSalir);
        panelBotones.add(btnRefresh);

        add(panelBotones, BorderLayout.SOUTH);

        btnInscribir.addActionListener(e -> inscribir());
        btnSalir.addActionListener(e -> desinscribir());
        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);

        List<Torneo> torneos = sistema.consultarTorneos();

        if (torneos == null) {
            return;
        }

        for (Torneo torneo : torneos) {
            String juego = torneo.getJuego() != null ? torneo.getJuego().getNombre() : "—";
            boolean inscrito = usuario != null && torneo.usuarioYaInscrito(usuario);

            modeloTabla.addRow(new Object[] {
                torneo.getNombre(),
                torneo.getDia(),
                juego,
                torneo.getTotalCuposDisponibles() + "/" + torneo.getCuposTotales(),
                torneo.obtenerDescripcionPremio(),
                inscrito ? "Sí" : "No"
            });
        }
    }

    private Torneo getTorneoSeleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un torneo primero.");
            return null;
        }

        return sistema.consultarTorneos().get(fila);
    }

    private void inscribir() {
        Torneo torneo = getTorneoSeleccionado();

        if (torneo == null) {
            return;
        }

        String texto = JOptionPane.showInputDialog(this, "Número de cupos (1 a 3):", "1");

        if (texto == null) {
            return;
        }

        try {
            int cupos = Integer.parseInt(texto.trim());

            InscripcionTorneo inscripcion = sistema.inscribirEnTorneo(torneo, cupos);

            if (inscripcion == null) {
                JOptionPane.showMessageDialog(this, "No fue posible inscribirse. Revisa cupos, inscripción previa o turnos del empleado.");
            } else {
                JOptionPane.showMessageDialog(this, "Inscripción realizada correctamente.");
                cargarTabla();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Escribe un número válido.");
        }
    }

    private void desinscribir() {
        Torneo torneo = getTorneoSeleccionado();

        if (torneo == null) {
            return;
        }

        boolean ok = sistema.desinscribirDeTorneo(torneo);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Desinscripción realizada.");
        } else {
            JOptionPane.showMessageDialog(this, "No estabas inscrito en ese torneo.");
        }

        cargarTabla();
    }
}