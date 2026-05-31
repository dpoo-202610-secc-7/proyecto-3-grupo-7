package ui;

import modelo.*;
import javax.swing.*;
import java.awt.*;

public class VentanaLogin extends JFrame {

    private SistemasDulcesDados sistema;
    private JTextField txtLogin;
    private JPasswordField txtPassword;

    public VentanaLogin(SistemasDulcesDados sistema) {
        this.sistema = sistema;
        setTitle("Dulces & Dados — Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; 
        gbc.gridy = 0;
        panel.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        txtLogin = new JTextField(16);
        panel.add(txtLogin, gbc);

        gbc.gridx = 0; 
        gbc.gridy = 1;
        panel.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(16);
        panel.add(txtPassword, gbc);

        JButton btnIngresar = new JButton("Ingresar");
        gbc.gridx = 0; 
        gbc.gridy = 2; 
        gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        btnIngresar.addActionListener(e -> autenticar());
        txtPassword.addActionListener(e -> autenticar());

        add(panel);
    }

    private void autenticar() {
        String login = txtLogin.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario = sistema.autenticarUsuario(login, pass);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            return;
        }

        redirigir(usuario);
    }

    private void redirigir(Usuario usuario) {
        dispose();

        if (usuario instanceof Administrador) {
            new VentanaAdministrador(sistema, (Administrador) usuario).setVisible(true);
        } else if (usuario instanceof Empleado) {
            new VentanaEmpleado(sistema, (Empleado) usuario).setVisible(true);
        } else if (usuario instanceof Cliente) {
            new VentanaCliente(sistema, (Cliente) usuario).setVisible(true);
        }
    }
}