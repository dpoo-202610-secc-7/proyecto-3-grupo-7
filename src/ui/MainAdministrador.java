package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import modelo.*;
import persistencia.PersistenciaSistema;


public class MainAdministrador {

    public static void main(String[] args) {
        SistemasDulcesDados sistema = new SistemasDulcesDados(
            new Cafe(50), new PersistenciaSistema("datos"));
        sistema.inicializarSistema();

        Scanner scanner = new Scanner(System.in);
        try {
            if (!iniciarSesion(scanner, sistema)) {
                System.out.println("Credenciales inválidas o rol no autorizado.");
                return;
            }

            boolean continuar = true;
            while (continuar) {
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.println("║       MENÚ ADMINISTRADOR             ║");
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  USUARIOS                            ║");
                System.out.println("║  1. Registrar cliente                ║");
                System.out.println("║  2. Registrar empleado               ║");
                System.out.println("║  3. Listar usuarios                  ║");
                System.out.println("║  INVENTARIO                          ║");
                System.out.println("║  4. Ver inventario préstamo          ║");
                System.out.println("║  5. Ver inventario venta             ║");
                System.out.println("║  6. Reabastecer juego venta          ║");
                System.out.println("║  7. Agregar copia préstamo           ║");
                System.out.println("║  8. Reparar copia                    ║");
                System.out.println("║  9. Marcar copia desaparecida        ║");
                System.out.println("║  MENÚ CAFETERÍA                      ║");
                System.out.println("║  10. Agregar bebida al menú          ║");
                System.out.println("║  11. Agregar pastelería al menú      ║");
                System.out.println("║  12. Ver sugerencias pendientes      ║");
                System.out.println("║  13. Aprobar/rechazar sugerencia     ║");
                System.out.println("║  TURNOS                              ║");
                System.out.println("║  14. Asignar turno a empleado        ║");
                System.out.println("║  15. Ver turnos de empleado          ║");
                System.out.println("║  16. Revisar solicitudes de cambio   ║");
                System.out.println("║  VENTAS                              ║");
                System.out.println("║  17. Informe de ventas               ║");
                System.out.println("║  0.  Cerrar sesión                   ║");
                System.out.println("╚══════════════════════════════════════╝");

                int op = leerEntero(scanner, "Seleccione opción: ", 0, 17);
                switch (op) {
                    case 1:  registrarCliente(scanner, sistema);          break;
                    case 2:  registrarEmpleado(scanner, sistema);         break;
                    case 3:  listarUsuarios(sistema);                     break;
                    case 4:  verInventarioPrestamo(sistema);              break;
                    case 5:  verInventarioVenta(sistema);                 break;
                    case 6:  reabastecerVenta(scanner, sistema);          break;
                    case 7:  agregarCopiaPrestamo(scanner, sistema);      break;
                    case 8:  repararCopia(scanner, sistema);              break;
                    case 9:  marcarDesaparecida(scanner, sistema);        break;
                    case 10: agregarBebida(scanner, sistema);             break;
                    case 11: agregarPasteleria(scanner, sistema);         break;
                    case 12: verSugerencias(sistema);                     break;
                    case 13: revisarSugerencia(scanner, sistema);         break;
                    case 14: asignarTurno(scanner, sistema);              break;
                    case 15: verTurnosEmpleado(scanner, sistema);         break;
                    case 16: revisarSolicitudesCambio(scanner, sistema);  break;
                    case 17: informeVentas(scanner, sistema);             break;
                    case 0:  continuar = false;                           break;
                }
            }
        } finally {
            sistema.guardarDatos();
            scanner.close();
            System.out.println("Datos guardados. Hasta luego.");
        }
    }

    // ── Autenticación ─────────────────────────────────────────────────

    private static boolean iniciarSesion(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();
        Usuario u = sistema.autenticarUsuario(login, pass);
        return u instanceof Administrador;
    }

    // ── Usuarios ──────────────────────────────────────────────────────

    private static void registrarCliente(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.println("\n-- Registrar Cliente --");
        String doc    = leerTexto(scanner, "Documento: ");
        String nombre = leerTexto(scanner, "Nombre: ");
        String correo = leerTexto(scanner, "Correo: ");
        String login  = leerTexto(scanner, "Login: ");
        String pass   = leerTexto(scanner, "Password: ");
        boolean ok = sistema.registrarCliente(doc, nombre, correo, login, pass);
        System.out.println(ok ? "✅ Cliente registrado." : "❌ No se pudo registrar (login duplicado).");
    }

    private static void registrarEmpleado(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.println("\n-- Registrar Empleado --");
        System.out.println("1. Mesero   2. Cocinero");
        int tipo   = leerEntero(scanner, "Tipo: ", 1, 2);
        String doc    = leerTexto(scanner, "Documento: ");
        String nombre = leerTexto(scanner, "Nombre: ");
        String correo = leerTexto(scanner, "Correo: ");
        String login  = leerTexto(scanner, "Login: ");
        String pass   = leerTexto(scanner, "Password: ");
        String codigo = leerTexto(scanner, "Código empleado: ");
        String cargo  = (tipo == 1) ? "MESERO" : "COCINERO";
        boolean ok = sistema.registrarEmpleadoPorAdministrador(doc, nombre, correo, login, pass, codigo, cargo);
        System.out.println(ok ? "✅ Empleado registrado." : "❌ No se pudo registrar (login duplicado).");
    }

    private static void listarUsuarios(SistemasDulcesDados sistema) {
        List<Usuario> usuarios = sistema.getUsuarios();
        if (usuarios.isEmpty()) { System.out.println("No hay usuarios."); return; }
        System.out.println("\n-- Usuarios registrados --");
        for (Usuario u : usuarios) {
            System.out.printf("  %-20s %-12s login: %s%n",
                u.getNombre(), u.getClass().getSimpleName(), u.getLogin());
        }
    }

    // ── Inventario ────────────────────────────────────────────────────

    private static void verInventarioPrestamo(SistemasDulcesDados sistema) {
        Cafe cafe = sistema.getCafe();
        List<CopiaJuegoPrestamo> copias = cafe.consultarInventarioPrestamo();
        if (copias.isEmpty()) { System.out.println("No hay copias en inventario."); return; }
        System.out.println("\n-- Inventario de Préstamo --");
        int i = 1;
        for (CopiaJuegoPrestamo c : copias) {
            System.out.printf("  %d. %-20s  Estado: %-12s  Disponible: %s%n",
                i++, c.getJuegoMesa().getNombre(), c.getEstado(), c.estaDisponible());
        }
    }

    private static void verInventarioVenta(SistemasDulcesDados sistema) {
        List<JuegoVenta> juegos = sistema.getCafe().getInventarioVenta();
        if (juegos.isEmpty()) { System.out.println("No hay juegos en venta."); return; }
        System.out.println("\n-- Inventario de Venta --");
        int i = 1;
        for (JuegoVenta j : juegos) {
            System.out.printf("  %d. %-20s  Precio: $%.0f  Stock: %d%n",
                i++, j.getNombre(), j.getPrecio(), j.getStockDisponible());
        }
    }

    private static void reabastecerVenta(Scanner scanner, SistemasDulcesDados sistema) {
        List<JuegoVenta> juegos = sistema.getCafe().getInventarioVenta();
        if (juegos.isEmpty()) { System.out.println("No hay juegos en venta."); return; }
        verInventarioVenta(sistema);
        int idx = leerEntero(scanner, "Seleccione juego: ", 1, juegos.size()) - 1;
        int cantidad = leerEntero(scanner, "Cantidad a añadir: ", 1, 9999);
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.reabastecerJuegoVenta(juegos.get(idx), cantidad);
        System.out.println("✅ Stock actualizado. Stock actual: " + juegos.get(idx).getStockDisponible());
    }

    private static void agregarCopiaPrestamo(Scanner scanner, SistemasDulcesDados sistema) {
        List<JuegoMesa> catalogo = sistema.getCafe().getCatalogoJuegos();
        if (catalogo.isEmpty()) { System.out.println("No hay juegos en catálogo."); return; }
        System.out.println("\n-- Catálogo --");
        for (int i = 0; i < catalogo.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, catalogo.get(i).getNombre());
        int idx = leerEntero(scanner, "Seleccione juego: ", 1, catalogo.size()) - 1;
        int cantidad = leerEntero(scanner, "Copias a agregar: ", 1, 50);
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.reabastecerCopiaPrestamo(catalogo.get(idx), cantidad);
        System.out.println("✅ Copias agregadas.");
    }

    private static void repararCopia(Scanner scanner, SistemasDulcesDados sistema) {
        List<CopiaJuegoPrestamo> copias = sistema.getCafe().consultarInventarioPrestamo();
        if (copias.isEmpty()) { System.out.println("No hay copias."); return; }
        System.out.println("\n-- Copias --");
        for (int i = 0; i < copias.size(); i++) {
            CopiaJuegoPrestamo c = copias.get(i);
            System.out.printf("  %d. %-20s Estado: %s%n", i + 1, c.getJuegoMesa().getNombre(), c.getEstado());
        }
        int idx = leerEntero(scanner, "Seleccione copia: ", 1, copias.size()) - 1;
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.repararJuego(copias.get(idx), null);
        System.out.println("✅ Copia marcada como BUENO.");
    }

    private static void marcarDesaparecida(Scanner scanner, SistemasDulcesDados sistema) {
        List<CopiaJuegoPrestamo> copias = sistema.getCafe().consultarInventarioPrestamo();
        if (copias.isEmpty()) { System.out.println("No hay copias."); return; }
        System.out.println("\n-- Copias --");
        for (int i = 0; i < copias.size(); i++) {
            CopiaJuegoPrestamo c = copias.get(i);
            System.out.printf("  %d. %-20s Estado: %s%n", i + 1, c.getJuegoMesa().getNombre(), c.getEstado());
        }
        int idx = leerEntero(scanner, "Seleccione copia: ", 1, copias.size()) - 1;
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.marcarJuegoDesaparecido(copias.get(idx));
        System.out.println("✅ Copia marcada como DESAPARECIDA.");
    }

    // ── Menú Cafetería ────────────────────────────────────────────────

    private static void agregarBebida(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.println("\n-- Nueva Bebida --");
        String nombre = leerTexto(scanner, "Nombre: ");
        double precio = leerDouble(scanner, "Precio: ");
        boolean alcoh = leerBoolean(scanner, "¿Es alcohólica? (s/n): ");
        boolean cal   = leerBoolean(scanner, "¿Es caliente? (s/n): ");
        Bebida bebida = new Bebida(nombre, precio, true, alcoh, cal);
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.agregarProductoMenu(bebida, sistema.getCafe());
        System.out.println("✅ Bebida agregada al menú.");
    }

    private static void agregarPasteleria(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.println("\n-- Nueva Pastelería --");
        String nombre = leerTexto(scanner, "Nombre: ");
        double precio = leerDouble(scanner, "Precio: ");
        Pasteleria pas = new Pasteleria(nombre, precio, true);
        System.out.println("Alérgenos disponibles: MANI, GLUTEN, MARISCOS, LACTEOS, HUEVO");
        System.out.println("Ingrese alérgenos separados por coma (o deje vacío):");
        System.out.print("> ");
        String alerStr = scanner.nextLine().trim();
        if (!alerStr.isEmpty()) {
            for (String al : alerStr.split(",")) {
                try { pas.agregarAlergeno(Alergeno.valueOf(al.trim().toUpperCase())); }
                catch (Exception e) { System.out.println("Alérgeno desconocido ignorado: " + al.trim()); }
            }
        }
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.agregarProductoMenu(pas, sistema.getCafe());
        System.out.println("✅ Pastelería agregada al menú.");
    }

    // ── Sugerencias ───────────────────────────────────────────────────

    private static void verSugerencias(SistemasDulcesDados sistema) {
        List<SugerenciaPlatillo> sugerencias = sistema.getSugerencias();
        List<SugerenciaPlatillo> pendientes  = new ArrayList<>();
        for (SugerenciaPlatillo s : sugerencias) {
            if (s.estaPendiente()) pendientes.add(s);
        }
        if (pendientes.isEmpty()) { System.out.println("No hay sugerencias pendientes."); return; }
        System.out.println("\n-- Sugerencias pendientes --");
        for (int i = 0; i < pendientes.size(); i++) {
            SugerenciaPlatillo s = pendientes.get(i);
            System.out.printf("  %d. '%s' (%s) — por: %s%n",
                i + 1, s.getNombrePropuesto(), s.getCategoriaPropuesta(),
                s.getEmpleado() != null ? s.getEmpleado().getNombre() : "?");
        }
    }

    private static void revisarSugerencia(Scanner scanner, SistemasDulcesDados sistema) {
        List<SugerenciaPlatillo> sugerencias = sistema.getSugerencias();
        List<SugerenciaPlatillo> pendientes  = new ArrayList<>();
        for (SugerenciaPlatillo s : sugerencias) {
            if (s.estaPendiente()) pendientes.add(s);
        }
        if (pendientes.isEmpty()) { System.out.println("No hay sugerencias pendientes."); return; }
        verSugerencias(sistema);
        int idx = leerEntero(scanner, "Seleccione sugerencia: ", 1, pendientes.size()) - 1;
        System.out.println("1. Aprobar   2. Rechazar");
        int accion = leerEntero(scanner, "Acción: ", 1, 2);
        Administrador admin = (Administrador) sistema.getSesionActual();
        if (accion == 1) {
            admin.aprobarSugerencia(pendientes.get(idx));
            System.out.println("✅ Sugerencia aprobada.");
        } else {
            admin.rechazarSugerencia(pendientes.get(idx));
            System.out.println("✅ Sugerencia rechazada.");
        }
    }

    // ── Turnos ────────────────────────────────────────────────────────

    private static void asignarTurno(Scanner scanner, SistemasDulcesDados sistema) {
        List<Empleado> empleados = sistema.getCafe().getEmpleados();
        if (empleados.isEmpty()) { System.out.println("No hay empleados."); return; }
        System.out.println("\n-- Empleados --");
        for (int i = 0; i < empleados.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, empleados.get(i).getNombre());
        int idx = leerEntero(scanner, "Seleccione empleado: ", 1, empleados.size()) - 1;

        DiaSemana[] dias = DiaSemana.values();
        System.out.println("Días: ");
        for (int i = 0; i < dias.length; i++) System.out.printf("  %d. %s%n", i + 1, dias[i]);
        int diaIdx = leerEntero(scanner, "Seleccione día: ", 1, dias.length) - 1;

        String horaInicio = leerTexto(scanner, "Hora inicio (HH:MM): ");
        String horaFin    = leerTexto(scanner, "Hora fin (HH:MM): ");

        Turno turno = new Turno(dias[diaIdx], horaInicio, horaFin);
        Administrador admin = (Administrador) sistema.getSesionActual();
        admin.asignarTurno(empleados.get(idx), turno);
        System.out.println("✅ Turno asignado a " + empleados.get(idx).getNombre() + ".");
    }

    private static void verTurnosEmpleado(Scanner scanner, SistemasDulcesDados sistema) {
        List<Empleado> empleados = sistema.getCafe().getEmpleados();
        if (empleados.isEmpty()) { System.out.println("No hay empleados."); return; }
        System.out.println("\n-- Empleados --");
        for (int i = 0; i < empleados.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, empleados.get(i).getNombre());
        int idx = leerEntero(scanner, "Seleccione empleado: ", 1, empleados.size()) - 1;
        Empleado emp = empleados.get(idx);
        List<Turno> turnos = emp.getTurnos();
        if (turnos.isEmpty()) { System.out.println("No tiene turnos asignados."); return; }
        System.out.println("\nTurnos de " + emp.getNombre() + ":");
        for (Turno t : turnos) {
            System.out.printf("  %-10s %s - %s%n", t.getDia(), t.getHoraInicio(), t.getHoraFin());
        }
    }

    private static void revisarSolicitudesCambio(Scanner scanner, SistemasDulcesDados sistema) {
        List<SolicitudCambioTurno> pendientes = new ArrayList<>();
        for (Empleado emp : sistema.getCafe().getEmpleados()) {
            for (SolicitudCambioTurno s : emp.getSolicitudesCambioTurno()) {
                if (s.estaPendiente()) pendientes.add(s);
            }
        }
        if (pendientes.isEmpty()) { System.out.println("No hay solicitudes pendientes."); return; }
        System.out.println("\n-- Solicitudes pendientes --");
        for (int i = 0; i < pendientes.size(); i++) {
            SolicitudCambioTurno s = pendientes.get(i);
            System.out.printf("  %d. [%s] %s → solicita %s%n",
                i + 1, s.getTipoSolicitud(),
                s.getSolicitante().getNombre(),
                s.getTurnoPropuesto() != null ? s.getTurnoPropuesto().getDia() : "?");
        }
        int idx = leerEntero(scanner, "Seleccione solicitud: ", 1, pendientes.size()) - 1;
        System.out.println("1. Aprobar   2. Rechazar");
        int accion = leerEntero(scanner, "Acción: ", 1, 2);
        Administrador admin = (Administrador) sistema.getSesionActual();
        if (accion == 1) {
            admin.aprobarSolicitudCambio(pendientes.get(idx));
            System.out.println("✅ Solicitud aprobada.");
        } else {
            admin.rechazarSolicitudCambio(pendientes.get(idx));
            System.out.println("✅ Solicitud rechazada.");
        }
    }

    // ── Ventas ────────────────────────────────────────────────────────

    private static void informeVentas(Scanner scanner, SistemasDulcesDados sistema) {
        System.out.println("\n-- Informe de ventas --");
        System.out.println("Tipo: 1. Cafetería   2. Tienda   3. Todas");
        int tipo = leerEntero(scanner, "Tipo: ", 1, 3);
        TipoVenta tipoVenta = tipo == 1 ? TipoVenta.CAFETERIA : (tipo == 2 ? TipoVenta.TIENDA_JUEGOS : null);

        Administrador admin = (Administrador) sistema.getSesionActual();
        List<Venta> ventas = admin.generarInformeVentas("GENERAL", tipoVenta, sistema.getCafe());

        if (ventas.isEmpty()) { System.out.println("No hay ventas registradas."); return; }

        double total = 0;
        System.out.printf("%-20s %-15s %10s%n", "Comprador", "Tipo", "Total");
        System.out.println("─".repeat(50));
        for (Venta v : ventas) {
            String nombre = v.getComprador() != null ? v.getComprador().getNombre() : "?";
            System.out.printf("%-20s %-15s $%9.0f%n", nombre, v.getTipoVenta(), v.calcularTotal());
            total += v.calcularTotal();
        }
        System.out.println("─".repeat(50));
        System.out.printf("TOTAL GENERAL: $%.0f%n", total);
    }

    // ── Utilidades ────────────────────────────────────────────────────

    private static String leerTexto(Scanner sc, String prompt) {
        String val;
        do {
            System.out.print(prompt);
            val = sc.nextLine().trim();
        } while (val.isEmpty());
        return val;
    }

    private static int leerEntero(Scanner sc, String prompt, int min, int max) {
        int val = min - 1;
        while (val < min || val > max) {
            System.out.print(prompt);
            try { val = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { val = min - 1; }
        }
        return val;
    }

    private static double leerDouble(Scanner sc, String prompt) {
        double val = -1;
        while (val < 0) {
            System.out.print(prompt);
            try { val = Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { val = -1; }
        }
        return val;
    }

    private static boolean leerBoolean(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim().toLowerCase().startsWith("s");
    }
}
