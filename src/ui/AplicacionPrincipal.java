package ui;

import javax.swing.SwingUtilities;
import modelo.*;
import persistencia.PersistenciaSistema;


public class AplicacionPrincipal {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SistemasDulcesDados sistema = new SistemasDulcesDados(
                new Cafe(50), new PersistenciaSistema("src/datos"));
            sistema.inicializarSistema();
            
            VentanaLogin login = new VentanaLogin(sistema);
            login.setVisible(true);
        });
    }
}
