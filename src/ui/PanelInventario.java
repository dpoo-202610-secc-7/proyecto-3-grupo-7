package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelInventario extends JPanel {

    private SistemasDulcesDados sistema;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelInventario(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Nombre", "Precio", "Stock", "Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAumentar = new JButton("+ Stock");
        JButton btnReducir  = new JButton("- Stock");
        JButton btnRefresh  = new JButton("Actualizar");
        panelBotones.add(btnAumentar);
        panelBotones.add(btnReducir);
        panelBotones.add(btnRefresh);
        add(panelBotones, BorderLayout.SOUTH);

        btnAumentar.addActionListener(e -> cambiarStock(true));
        btnReducir.addActionListener(e -> cambiarStock(false));
        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<JuegoVenta> inventario = sistema.getCafe().getInventarioVenta();
        if (inventario == null) return;
        for (JuegoVenta j : inventario) {
            modeloTabla.addRow(new Object[]{
                j.getNombre(),
                String.format("$%.2f", j.getPrecio()),
                j.getStockDisponible(),
                j.isDisponible() ? "Sí" : "No"
            });
        }
    }

    private void cambiarStock(boolean aumentar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un juego primero.");
            return;
        }
        List<JuegoVenta> inventario = sistema.getCafe().getInventarioVenta();
        JuegoVenta juego = inventario.get(fila);
        if (aumentar) {
            juego.aumentarStock(1);
        } else {
            if (juego.getStockDisponible() <= 0) {
                JOptionPane.showMessageDialog(this, "Stock ya está en 0.");
                return;
            }
            juego.reducirStock(1);
        }
        cargarTabla();
    }
}