package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelPrestamos extends JPanel {

    private SistemasDulcesDados sistema;
    private Usuario usuario;
    private DefaultTableModel modeloJuegos;
    private DefaultTableModel modeloPrestamos;
    private JTable tablaJuegos;
    private JTable tablaPrestamos;

    public PanelPrestamos(SistemasDulcesDados sistema, Usuario usuario) {
        this.sistema = sistema;
        this.usuario = usuario;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarTablas();
    }

    private void initUI() {
        modeloJuegos = new DefaultTableModel(
            new String[] {"Juego", "Categoría", "Jugadores", "Edad", "Difícil", "Copias disponibles"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaJuegos = new JTable(modeloJuegos);
        tablaJuegos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        modeloPrestamos = new DefaultTableModel(
            new String[] {"Fecha inicio", "Fecha fin", "Usuario", "Mesa", "Juegos", "Activo", "Advertencia"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaPrestamos = new JTable(modeloPrestamos);
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tablaJuegos),
            new JScrollPane(tablaPrestamos)
        );

        split.setResizeWeight(0.48);

        add(split, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnPrestar = new JButton("Prestar seleccionados");
        JButton btnDevolver = new JButton("Devolver préstamo");
        JButton btnRefresh = new JButton("Actualizar");

        botones.add(btnPrestar);
        botones.add(btnDevolver);
        botones.add(btnRefresh);

        add(botones, BorderLayout.SOUTH);

        btnPrestar.addActionListener(e -> crearPrestamo());
        btnDevolver.addActionListener(e -> devolverPrestamo());
        btnRefresh.addActionListener(e -> cargarTablas());
    }

    private void cargarTablas() {
        modeloJuegos.setRowCount(0);

        if (sistema == null || sistema.getCafe() == null) {
            return;
        }

        for (JuegoMesa juego : sistema.getCafe().getCatalogoJuegos()) {
            modeloJuegos.addRow(new Object[] {
                juego.getNombre(),
                juego.getCategoria(),
                juego.getMinJugadores() + "-" + juego.getMaxJugadores(),
                juego.getEdadMinima(),
                juego.isDificil() ? "Sí" : "No",
                juego.obtenerCopiasDisponibles().size()
            });
        }

        modeloPrestamos.setRowCount(0);

        for (Prestamo prestamo : sistema.getCafe().getPrestamos()) {
            if (prestamo.getUsuario() == usuario) {
                String mesa = "—";

                if (prestamo.getMesa() != null) {
                    mesa = String.valueOf(prestamo.getMesa().getNumeroMesa());
                }

                modeloPrestamos.addRow(new Object[] {
                    prestamo.getFechaInicio(),
                    prestamo.getFechaFin(),
                    prestamo.getUsuario() != null ? prestamo.getUsuario().getNombre() : "—",
                    mesa,
                    nombresJuegos(prestamo),
                    prestamo.estaActivo() ? "Sí" : "No",
                    prestamo.isAdvertenciaSinMesero() ? "Sin mesero capacitado" : "—"
                });
            }
        }
    }

    private String nombresJuegos(Prestamo prestamo) {
        StringBuilder sb = new StringBuilder();

        for (DetallePrestamo detalle : prestamo.getDetalles()) {
            if (detalle.getCopiaJuego() != null && detalle.getCopiaJuego().getJuegoMesa() != null) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }

                sb.append(detalle.getCopiaJuego().getJuegoMesa().getNombre());
            }
        }

        return sb.toString();
    }

    private Mesa buscarMesaActivaCliente() {
        if (!(usuario instanceof Cliente)) {
            return null;
        }

        for (Mesa mesa : sistema.getCafe().getMesas()) {
            ReservaMesa reserva = mesa.getReservaActiva();

            if (reserva != null && reserva.getCliente() == usuario) {
                return mesa;
            }
        }

        return null;
    }

    private void crearPrestamo() {
        int[] filas = tablaJuegos.getSelectedRows();

        if (filas.length == 0 || filas.length > 2) {
            JOptionPane.showMessageDialog(this, "Selecciona uno o dos juegos.");
            return;
        }

        Mesa mesa = null;

        if (usuario instanceof Cliente) {
            mesa = buscarMesaActivaCliente();

            if (mesa == null) {
                JOptionPane.showMessageDialog(this, "El cliente debe tener una reserva activa para pedir juegos.");
                return;
            }
        }

        Prestamo prestamo = new Prestamo(fechaActual(), null, false, usuario, mesa);
        List<CopiaJuegoPrestamo> copias = new ArrayList<CopiaJuegoPrestamo>();

        for (int fila : filas) {
            JuegoMesa juego = sistema.getCafe().getCatalogoJuegos().get(fila);

            if (mesa != null && !mesa.puedeRecibirJuego(juego)) {
                JOptionPane.showMessageDialog(this, "La mesa no puede recibir el juego: " + juego.getNombre());
                return;
            }

            List<CopiaJuegoPrestamo> disponibles = juego.obtenerCopiasDisponibles();

            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay copias disponibles de " + juego.getNombre() + ".");
                return;
            }

            copias.add(disponibles.get(0));
        }

        for (CopiaJuegoPrestamo copia : copias) {
            DetallePrestamo detalle = new DetallePrestamo(copia);

            detalle.registrarAsignacion(fechaActual());
            prestamo.agregarDetalle(detalle);
        }

        if (!prestamo.validarMaximoJuegos() || !prestamo.validarDisponibilidadCopias()) {
            JOptionPane.showMessageDialog(this, "El préstamo no cumple las reglas del sistema.");
            return;
        }

        if (!prestamo.validarJuegosDificiles(sistema.getCafe())) {
            prestamo.registrarAdvertenciaSinMesero();
        }

        for (CopiaJuegoPrestamo copia : copias) {
            copia.prestar();
        }

        sistema.getCafe().registrarPrestamo(prestamo);

        sistema.guardarDatos();

        cargarTablas();

        String mensaje = "Préstamo creado correctamente.";

        if (prestamo.isAdvertenciaSinMesero()) {
            mensaje = "Préstamo creado con advertencia: no hay mesero capacitado para un juego difícil.";
        }

        JOptionPane.showMessageDialog(this, mensaje);
    }

    private void devolverPrestamo() {
        int fila = tablaPrestamos.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo.");
            return;
        }

        Prestamo prestamo = prestamoVisibleEn(fila);

        if (prestamo == null || !prestamo.estaActivo()) {
            JOptionPane.showMessageDialog(this, "Ese préstamo no está activo.");
            return;
        }

        prestamo.finalizarPrestamo();

        sistema.guardarDatos();

        cargarTablas();

        JOptionPane.showMessageDialog(this, "Préstamo devuelto correctamente.");
    }

    private Prestamo prestamoVisibleEn(int indiceVisible) {
        int contador = 0;

        for (Prestamo prestamo : sistema.getCafe().getPrestamos()) {
            if (prestamo.getUsuario() == usuario) {
                if (contador == indiceVisible) {
                    return prestamo;
                }

                contador++;
            }
        }

        return null;
    }

    private String fechaActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}