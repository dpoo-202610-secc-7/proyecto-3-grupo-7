package ui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import modelo.Cafe;
import modelo.SistemasDulcesDados;
import modelo.Usuario;

class PruebaUsuariosAutenticacion {

    @Test
    void testRegistrarCliente() {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), null);

        boolean resultado = sistema.registrarCliente(
                "1001",
                "Juan Perez",
                "juan@gmail.com",
                "juan",
                "123"
        );

        assertTrue(resultado);
        assertNotNull(sistema.buscarUsuarioPorLogin("juan"));
    }

    @Test
    void testLoginCorrecto() {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), null);

        sistema.registrarCliente("1002", "Maria Lopez", "maria@gmail.com", "maria", "456");

        Usuario usuario = sistema.iniciarSesion("maria", "456");

        assertNotNull(usuario);
        assertEquals("maria", usuario.getLogin());
    }

    @Test
    void testLoginIncorrecto() {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), null);

        sistema.registrarCliente("1003", "Carlos Ruiz", "carlos@gmail.com", "carlos", "789");

        Usuario usuario = sistema.iniciarSesion("carlos", "000");

        assertNull(usuario);
    }

    @Test
    void testValidarCredenciales() {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), null);

        sistema.registrarCliente("1004", "Laura Gomez", "laura@gmail.com", "laura", "abc");

        assertTrue(sistema.validarCredenciales("laura", "abc"));
        assertFalse(sistema.validarCredenciales("laura", "mal"));
    }

    @Test
    void testCerrarSesion() {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), null);

        sistema.registrarCliente("1005", "Pedro Torres", "pedro@gmail.com", "pedro", "111");

        sistema.iniciarSesion("pedro", "111");

        assertTrue(sistema.haySesionIniciada());

        sistema.cerrarSesion();

        assertFalse(sistema.haySesionIniciada());
    }
}