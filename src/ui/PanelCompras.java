package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelCompras extends JPanel {

    private SistemasDulcesDados sistema;
    private Usuario usuario;
    private DefaultTableModel modeloCatalogo;
    private DefaultTableModel modeloCarrito;
    private JTable tablaCatalogo;
    private JTable tablaCarrito;
    private JLabel lblTotal;
    private List<ItemVenta> carrito;

    public PanelCompras(SistemasDulcesDados sistema, Usuario usuario) {
        this.sistema = sistema;
        this.usuario = usuario;
        this.carrito = new ArrayList<ItemVenta>();

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        cargarCatalogo();
        cargarCarrito();
    }

    private void initUI() {
        modeloCatalogo = new DefaultTableModel(
            new String[] {"Tipo", "Nombre", "Precio", "Disponible", "Stock"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaCatalogo = new JTable(modeloCatalogo);
        tablaCatalogo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modeloCarrito = new DefaultTableModel(
            new String[] {"Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0
        ) {
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tablaCatalogo),
            new JScrollPane(tablaCarrito)
        );

        split.setResizeWeight(0.55);

        add(split, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnAgregar = new JButton("Agregar al carrito");
        JButton btnQuitar = new JButton("Quitar item");
        JButton btnComprar = new JButton("Registrar compra");
        JButton btnRefresh = new JButton("Actualizar catálogo");

        lblTotal = new JLabel("Subtotal carrito: $0.00");

        botones.add(btnAgregar);
        botones.add(btnQuitar);
        botones.add(btnComprar);
        botones.add(btnRefresh);
        botones.add(lblTotal);

        add(botones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnQuitar.addActionListener(e -> quitarItem());
        btnComprar.addActionListener(e -> registrarCompra());
        btnRefresh.addActionListener(e -> cargarCatalogo());
    }

    private void cargarCatalogo() {
        modeloCatalogo.setRowCount(0);

        if (sistema == null || sistema.getCafe() == null) {
            return;
        }

        for (ProductoMenu producto : sistema.getCafe().getMenu()) {
            modeloCatalogo.addRow(new Object[] {
                tipoMenu(producto),
                producto.getNombre(),
                formato(producto.getPrecio()),
                producto.isDisponible() ? "Sí" : "No",
                "—"
            });
        }

        for (JuegoVenta juego : sistema.getCafe().getInventarioVenta()) {
            modeloCatalogo.addRow(new Object[] {
                "Juego venta",
                juego.getNombre(),
                formato(juego.getPrecio()),
                juego.isDisponible() ? "Sí" : "No",
                juego.getStockDisponible()
            });
        }
    }

    private String tipoMenu(ProductoMenu producto) {
        if (producto instanceof Bebida) {
            return "Bebida";
        }

        if (producto instanceof Pasteleria) {
            return "Pastelería";
        }

        return "Menú";
    }

    private ProductoVendible productoCatalogoEn(int fila) {
        int cantidadMenu = sistema.getCafe().getMenu().size();

        if (fila < cantidadMenu) {
            return sistema.getCafe().getMenu().get(fila);
        }

        return sistema.getCafe().getInventarioVenta().get(fila - cantidadMenu);
    }

    private void agregarAlCarrito() {
        int fila = tablaCatalogo.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.");
            return;
        }

        ProductoVendible producto = productoCatalogoEn(fila);

        if (!producto.isDisponible()) {
            JOptionPane.showMessageDialog(this, "El producto no está disponible.");
            return;
        }

        String texto = JOptionPane.showInputDialog(this, "Cantidad:", "1");

        if (texto == null) {
            return;
        }

        try {
            int cantidad = Integer.parseInt(texto.trim());

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.");
                return;
            }

            if (producto instanceof JuegoVenta) {
                JuegoVenta juego = (JuegoVenta) producto;

                if (!juego.hayStock(cantidad)) {
                    JOptionPane.showMessageDialog(this, "No hay stock suficiente.");
                    return;
                }
            }

            if (producto instanceof Bebida && usuario instanceof Cliente) {
                Mesa mesa = buscarMesaActivaCliente();

                if (mesa != null && !mesa.puedeRecibirBebida((Bebida) producto, sistema.getCafe())) {
                    JOptionPane.showMessageDialog(this, "La bebida no puede despacharse a la mesa por las reglas del negocio.");
                    return;
                }
            }

            carrito.add(new ItemVenta(cantidad, producto.getPrecio(), producto));

            cargarCarrito();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe una cantidad válida.");
        }
    }

    private void cargarCarrito() {
        modeloCarrito.setRowCount(0);

        double total = 0;

        for (ItemVenta item : carrito) {
            modeloCarrito.addRow(new Object[] {
                item.getProducto().getNombre(),
                item.getCantidad(),
                formato(item.getPrecioUnitario()),
                formato(item.calcularSubtotal())
            });

            total += item.calcularSubtotal();
        }

        lblTotal.setText("Subtotal carrito: " + formato(total));
    }

    private void quitarItem() {
        int fila = tablaCarrito.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un item del carrito.");
            return;
        }

        carrito.remove(fila);

        cargarCarrito();
    }

    private void registrarCompra() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        String texto = JOptionPane.showInputDialog(this, "Propina:", "0");

        if (texto == null) {
            return;
        }

        try {
            double propina = Double.parseDouble(texto.trim());

            if (propina < 0) {
                JOptionPane.showMessageDialog(this, "La propina no puede ser negativa.");
                return;
            }

            TipoVenta tipoVenta = calcularTipoVenta();

            Venta venta = new Venta(fechaActual(), tipoVenta, 0, propina, usuario, buscarMeseroRegistrador());

            for (ItemVenta item : carrito) {
                venta.agregarItem(item);
            }

            if (usuario instanceof Empleado) {
                venta.aplicarDescuentoPorcentaje(((Empleado) usuario).obtenerDescuentoEmpleado());
            }

            if (usuario instanceof Cliente) {
                ((Cliente) usuario).acumularPuntos(venta.calcularSubtotal());
            }

            for (ItemVenta item : carrito) {
                if (item.getProducto() instanceof JuegoVenta) {
                    ((JuegoVenta) item.getProducto()).reducirStock(item.getCantidad());
                }

                if (item.getProducto() instanceof Bebida && usuario instanceof Cliente) {
                    Mesa mesa = buscarMesaActivaCliente();

                    if (mesa != null) {
                        mesa.registrarBebidaCaliente((Bebida) item.getProducto());
                    }
                }
            }

            sistema.getCafe().registrarVenta(venta);

            sistema.guardarDatos();

            carrito.clear();

            cargarCatalogo();
            cargarCarrito();

            JOptionPane.showMessageDialog(this, "Compra registrada. Total: " + formato(venta.calcularTotal()));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe una propina válida.");
        }
    }

    private TipoVenta calcularTipoVenta() {
        boolean soloJuegos = true;

        for (ItemVenta item : carrito) {
            if (!(item.getProducto() instanceof JuegoVenta)) {
                soloJuegos = false;
            }
        }

        if (soloJuegos) {
            return TipoVenta.TIENDA_JUEGOS;
        }

        return TipoVenta.CAFETERIA;
    }

    private Mesero buscarMeseroRegistrador() {
        for (Empleado empleado : sistema.getCafe().getEmpleados()) {
            if (empleado instanceof Mesero) {
                return (Mesero) empleado;
            }
        }

        return null;
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

    private String fechaActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String formato(double valor) {
        return String.format("$%.2f", valor);
    }
}