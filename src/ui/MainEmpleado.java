package ui;

import java.util.List;
import java.util.Scanner;

import modelo.Cafe;
import modelo.Empleado;
import modelo.InscripcionTorneo;
import modelo.SistemasDulcesDados;
import modelo.Torneo;
import modelo.Usuario;
import persistencia.PersistenciaSistema;

public class MainEmpleado
{
    public static void main(String[] args)
    {
        SistemasDulcesDados sistema = new SistemasDulcesDados(new Cafe(50), new PersistenciaSistema("src/datos"));
        sistema.inicializarSistema();

        Scanner scanner = new Scanner(System.in);
        try
        {
            if (!iniciarSesion(scanner, sistema))
            {
                System.out.println("No autorizado.");
                return;
            }

            boolean continuar = true;
            while (continuar)
            {
                System.out.println("\n=== MENÚ EMPLEADO ===");
                System.out.println("1. Consultar torneos disponibles");
                System.out.println("2. Inscribirse a torneo");
                System.out.println("0. Salir");

                int opcion = ValidadorEntradaConsola.leerEnteroEnRango(scanner, "Seleccione opción: ", 0, 2);
                switch (opcion)
                {
                    case 1:
                        listarTorneosDisponibles(sistema);
                        break;
                    case 2:
                        inscribirseTorneo(scanner, sistema);
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        break;
                }
            }
        }
        finally
        {
            sistema.guardarDatos();
            scanner.close();
        }
    }

    private static boolean iniciarSesion(Scanner scanner, SistemasDulcesDados sistema)
    {
        String login = ValidadorEntradaConsola.leerTextoNoVacio(scanner, "Login: ");
        String password = ValidadorEntradaConsola.leerTextoNoVacio(scanner, "Password: ");
        Usuario usuario = sistema.autenticarUsuario(login, password);
        return usuario instanceof Empleado;
    }

    private static void listarTorneosDisponibles(SistemasDulcesDados sistema)
    {
        List<Torneo> torneos = sistema.consultarTorneosDisponibles();
        if (torneos.isEmpty())
        {
            System.out.println("No hay torneos disponibles.");
            return;
        }

        for (int i = 0; i < torneos.size(); i++)
        {
            Torneo torneo = torneos.get(i);
            System.out.println((i + 1) + ". " + torneo.getNombre() + " - " + torneo.getDia());
        }
    }

    private static void inscribirseTorneo(Scanner scanner, SistemasDulcesDados sistema)
    {
        List<Torneo> torneos = sistema.consultarTorneosDisponibles();
        if (torneos.isEmpty())
        {
            System.out.println("No hay torneos para inscribirse.");
            return;
        }

        listarTorneosDisponibles(sistema);
        int indice = ValidadorEntradaConsola.leerEnteroEnRango(scanner, "Elija torneo: ", 1, torneos.size()) - 1;

        InscripcionTorneo inscripcion = sistema.inscribirEnTorneo(torneos.get(indice), 1);
        System.out.println(inscripcion != null
            ? "Inscripción realizada (se aplican reglas de turno automáticamente)."
            : "No fue posible inscribir al empleado.");
    }
}