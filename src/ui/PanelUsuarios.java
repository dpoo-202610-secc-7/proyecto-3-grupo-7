package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelUsuarios extends JPanel {

    private SistemasDulcesDados sistema;
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    public PanelUsuarios(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        String[] columnas = {"Tipo", "Nombre", "Login", "Correo", "Documento"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar  = new JButton("Agregar Cliente");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefresh  = new JButton("Actualizar");
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefresh);
        add(panelBotones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarCliente());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnRefresh.addActionListener(e -> cargarTabla());
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = sistema.getUsuarios();
        if (usuarios == null) return;
        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{
                u.getTipoUsuario(),
                u.getNombre(),
                u.getLogin(),
                u.getCorreoElectronico(),
                u.getDocumentoIdentidad()
            });
        }
    }

    private void agregarCliente() {
        JTextField txtDoc    = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtLogin  = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        Object[] campos = {
            "Documento:", txtDoc,
            "Nombre:",    txtNombre,
            "Correo:",    txtCorreo,
            "Login:",     txtLogin,
            "Password:",  txtPass
        };

        int result = JOptionPane.showConfirmDialog(this, campos,
            "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        boolean ok = sistema.registrarCliente(
        	    txtDoc.getText().trim(),
        	    txtNombre.getText().trim(),
        	    txtCorreo.getText().trim(),
        	    txtLogin.getText().trim(),
        	    new String(txtPass.getPassword())
        	);

        	JOptionPane.showMessageDialog(this, ok ? "Cliente registrado." : "Error: datos inválidos o login ya existe.");
        cargarTabla();
    }

    private void eliminarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario primero.");
            return;
        }
        String login = (String) modeloTabla.getValueAt(fila, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar usuario '" + login + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = sistema.eliminarUsuarioPorLogin(login);
            JOptionPane.showMessageDialog(this, ok ? "Eliminado." : "No se pudo eliminar.");
            cargarTabla();
        }
    }
}