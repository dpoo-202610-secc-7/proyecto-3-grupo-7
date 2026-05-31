DULCES Y DADOS - PROYECTO3

Descripción:
Este proyecto implementa el sistema de gestión del café Dulces y Dados.
Permite gestionar usuarios, reservas, préstamos de juegos, ventas, turnos, torneos, sugerencias, compras, inventario y persistencia de datos.

El sistema cuenta con una interfaz gráfica desarrollada en Java Swing.
Desde la ventana de login, el usuario ingresa sus credenciales y el sistema abre la ventana correspondiente según su rol.

----------------------------------------
EJECUCIÓN PRINCIPAL DE LA APLICACIÓN
----------------------------------------

La aplicación principal se ejecuta desde:

- ui.AplicacionPrincipal

Esta clase se encarga de:
- Inicializar el sistema.
- Cargar los datos guardados.
- Abrir la ventana de login.
- Redirigir al usuario según su rol.

No se requieren argumentos de entrada.

Al iniciar, el sistema carga automáticamente la información desde src/datos.
Al cerrar la ventana principal, el sistema guarda automáticamente la información mediante guardarDatos().

----------------------------------------
LOGIN Y ROLES
----------------------------------------

El sistema inicia mostrando la ventana:

- ui.VentanaLogin

El usuario debe ingresar login y contraseña.

Si las credenciales son correctas, se abre una ventana según el tipo de usuario:

- Administrador  → ui.VentanaAdministrador
- Cliente        → ui.VentanaCliente
- Empleado       → ui.VentanaEmpleado

Si las credenciales son incorrectas, el sistema muestra un mensaje de error.

Las credenciales se encuentran en el archivo:

src/datos/usuarios.txt

El formato general de los usuarios es:

TIPO;documento;nombre;correo;login;password;...

Ejemplos de credenciales de prueba:

- admin / admin123
- sofia / 1234
- vale / 1234

----------------------------------------
EJECUCIÓN DE LOS PROGRAMAS DE PRUEBA
----------------------------------------

Cada prueba se ejecuta de forma independiente.
No se requieren argumentos de entrada.

- ui.PruebaReservas       → Reservas de mesa
- ui.PruebaPrestamos      → Préstamo y devolución de juegos
- ui.PruebaVentas         → Ventas, impuestos y descuentos
- ui.PruebaTurnos         → Asignación y cambio de turnos
- ui.PruebaAdministrador  → Gestión de inventario
- ui.PruebaAutenticacion  → Login correcto e incorrecto
- ui.PruebasPersistencia  → Guardado y carga de datos

----------------------------------------
PERSISTENCIA
----------------------------------------

Los datos se guardan en archivos dentro de la carpeta configurada en PersistenciaSistema.

Normalmente los archivos de datos se encuentran en:

src/datos

El sistema:
- Carga los datos automáticamente al iniciar.
- Guarda los datos al finalizar o cuando se invoque guardarDatos().
- Guarda usuarios, café, torneos y sugerencias.
- Guarda los cambios hechos desde las ventanas de administrador, cliente y empleado.
- Conserva reservas, préstamos, ventas, compras e inventario según la persistencia del café.

----------------------------------------
PRUEBAS
----------------------------------------

Las pruebas se encuentran en el paquete:

pruebas

Cada una se ejecuta de forma independiente.

Pruebas disponibles:

- PruebaPersistencia
  Verifica guardado y carga de datos.

- PruebaReservas
  Verifica creación, activación y cierre de reservas.

- PruebaPrestamos
  Verifica préstamo de juegos y devolución.

- PruebaVentas
  Verifica cálculo de subtotal, impuestos, descuentos y total.

- PruebaTurnos
  Verifica asignación de turnos y solicitudes de cambio.

- PruebaAdministrador
  Verifica gestión de inventario y juegos.

- PruebaAutenticacion
  Verifica inicio de sesión correcto e incorrecto.

Para ejecutar una prueba:
1. Ir a la clase en el paquete pruebas.
2. Ejecutar el método main.

----------------------------------------
ESTRUCTURA DEL PROYECTO
----------------------------------------

modelo        -> lógica del sistema
persistencia  -> carga y guardado de datos
ui            -> interfaz gráfica Swing y pruebas
datos         -> archivos de persistencia
lib           -> librerías externas

----------------------------------------
MANUAL BÁSICO DE USO
----------------------------------------

Inicio:
La aplicación principal se ejecuta desde:

- ui.AplicacionPrincipal

Al iniciar, el sistema carga automáticamente la información desde src/datos.
Al cerrar la ventana principal, guarda automáticamente los datos.

----------------------------------------
FLUJO ADMINISTRADOR
----------------------------------------

1. Iniciar sesión con credenciales de administrador.
2. Se abre la ventana ui.VentanaAdministrador.
3. Opciones disponibles:
   - Consultar usuarios registrados.
   - Registrar clientes.
   - Eliminar usuarios.
   - Consultar inventario.
   - Aumentar stock de juegos de venta.
   - Reducir stock de juegos de venta.
   - Consultar turnos.
   - Agregar turnos.
   - Consultar historial de ventas.
   - Revisar sugerencias.
   - Aprobar sugerencias.
   - Rechazar sugerencias.
   - Consultar gráficas del sistema.

----------------------------------------
FLUJO CLIENTE
----------------------------------------

1. Iniciar sesión con usuario de tipo cliente.
2. Se abre la ventana ui.VentanaCliente.
3. Opciones disponibles:
   - Consultar torneos.
   - Inscribirse a torneo.
   - Desinscribirse de torneo.
   - Crear reserva de mesa.
   - Consultar reservas propias.
   - Cerrar reserva activa.
   - Solicitar préstamo de juegos.
   - Devolver préstamo activo.
   - Consultar productos y juegos disponibles.
   - Agregar productos al carrito.
   - Registrar compras.
   - Acumular puntos de fidelidad.

----------------------------------------
FLUJO EMPLEADO
----------------------------------------

1. Iniciar sesión con usuario de tipo empleado.
2. Se abre la ventana ui.VentanaEmpleado.
3. Opciones disponibles:
   - Consultar torneos.
   - Inscribirse a torneo.
   - Desinscribirse de torneo.
   - Consultar reservas.
   - Solicitar préstamo de juegos.
   - Devolver préstamo activo.
   - Realizar compras con descuento de empleado.
   - Crear sugerencias de productos o platillos.
   - Consultar sugerencias creadas.

----------------------------------------
VALIDACIÓN DE ENTRADAS
----------------------------------------

Todas las opciones numéricas y campos obligatorios pasan por validaciones para:
- Evitar valores vacíos.
- Controlar rangos de menú.
- Asegurar números válidos.
- Validar credenciales correctas e incorrectas.
- Validar disponibilidad de mesas.
- Validar capacidad del café.
- Validar disponibilidad de copias de juegos.
- Validar máximo de dos juegos por préstamo.
- Validar stock suficiente en compras.
- Validar propina no negativa.
- Validar sugerencias con nombre no vacío.



