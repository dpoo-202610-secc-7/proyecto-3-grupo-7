package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelVentasAdmin extends JPanel {

    private SistemasDulcesDados sistema;
    private DefaultTableModel modeloTabla;

    public PanelVentasAdmin(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Fecha/Hora", "Tipo", "Comprador", "Mesero", "Total", "Propina"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Actualizar");
        panelBotones.add(btnRefresh);
        add(panelBotones, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Venta> ventas = sistema.getCafe().getVentas();
        if (ventas == null) return;
        for (Venta v : ventas) {
            double total = 0;
            if (v.getItems() != null) {
                for (ItemVenta item : v.getItems()) {
                    total += item.calcularSubtotal();
                }
            }
            String comprador = v.getComprador() != null ? v.getComprador().getNombre() : "—";
            String mesero    = v.getRegistradaPor() != null ? v.getRegistradaPor().getNombre() : "—";
            modeloTabla.addRow(new Object[]{
                v.getFechaHora(),
                v.getTipoVenta(),
                comprador,
                mesero,
                String.format("$%.2f", total),
                String.format("$%.2f", v.getPropina())
            });
        }
    }
}