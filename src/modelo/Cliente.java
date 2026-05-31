package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario 
{
    private int puntosFidelidad;
    private List<JuegoMesa> juegosFavoritos;
    private double bonoDescuentoGanado;
    private double premioMonetarioPendiente;

    public Cliente(String documentoIdentidad, String nombre, String correoElectronico, String login, String password)
    {
        super(documentoIdentidad, nombre, correoElectronico, login, password);
        this.puntosFidelidad = 0;
        this.juegosFavoritos = new ArrayList<JuegoMesa>();
        this.bonoDescuentoGanado = 0;
        this.premioMonetarioPendiente = 0;
    }

    public ReservaMesa crearReserva(Mesa mesa, String fechaHora, int numeroPersonas, boolean hayNinosMenores5, boolean hayMenoresEdad)
    {
        return new ReservaMesa(fechaHora, numeroPersonas, hayNinosMenores5, hayMenoresEdad, this, mesa);
    }

    public Prestamo solicitarPrestamo(Mesa mesa, List<CopiaJuegoPrestamo> copias)
    {
        return new Prestamo(null, null, false, this, mesa);
    }

    public void devolverPrestamo(Prestamo prestamo)
    {
        if (prestamo != null)
        {
            prestamo.finalizarPrestamo();
        }
    }

    public Venta comprarProductos(List<ItemVenta> items, double propina)
    {
        Venta venta = new Venta(null, TipoVenta.CAFETERIA, 0, propina, this, null);

        if (items != null)
        {
            for (ItemVenta item : items)
            {
                venta.agregarItem(item);
            }
        }

        return venta;
    }

    public void agregarJuegoFavorito(JuegoMesa juego)
    {
        if (juego != null && !juegosFavoritos.contains(juego))
        {
            juegosFavoritos.add(juego);
        }
    }

    public void eliminarJuegoFavorito(JuegoMesa juego)
    {
        if (juego != null)
        {
            juegosFavoritos.remove(juego);
        }
    }

    public List<JuegoMesa> consultarJuegosFavoritos()
    {
        return juegosFavoritos;
    }

    public boolean tieneJuegoFavorito(JuegoMesa juego)
    {
        return juegosFavoritos.contains(juego);
    }

    public void recibirBonoDescuento(double porcentaje)
    {
        if (porcentaje > 0)
        {
            bonoDescuentoGanado = porcentaje;
        }
    }

    public boolean tieneBonoActivo()
    {
        return bonoDescuentoGanado > 0;
    }

    public double usarBonoDescuento()
    {
        double bono = bonoDescuentoGanado;
        bonoDescuentoGanado = 0;
        return bono;
    }

    public boolean aplicarBonoAVenta(Venta venta)
    {
        if (venta == null || !tieneBonoActivo())
        {
            return false;
        }

        double porcentaje = usarBonoDescuento();
        venta.aplicarDescuentoPorcentaje(porcentaje);
        return true;
    }

    public void aplicarCodigoDescuento(String codigo, Venta venta)
    {
        if (codigo == null || venta == null)
        {
            return;
        }

        if (codigo.equalsIgnoreCase("BONO_TORNEO"))
        {
            aplicarBonoAVenta(venta);
        }
    }

    public void acumularPuntos(double valorCompra)
    {
        if (valorCompra > 0)
        {
            puntosFidelidad += (int) valorCompra;
        }
    }

    public void usarPuntosFidelidad(int puntos)
    {
        if (puntos > 0 && puntos <= puntosFidelidad)
        {
            puntosFidelidad -= puntos;
        }
    }

    public void registrarPremioMonetario(double monto)
    {
        if (monto > 0)
        {
            premioMonetarioPendiente += monto;
        }
    }

    public List<Venta> consultarHistorialCompras()
    {
        return new ArrayList<Venta>();
    }

    public int getPuntosFidelidad()
    {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad)
    {
        this.puntosFidelidad = puntosFidelidad;
    }

    public double getBonoDescuentoGanado()
    {
        return bonoDescuentoGanado;
    }

    public void setBonoDescuentoGanado(double bonoDescuentoGanado)
    {
        this.bonoDescuentoGanado = bonoDescuentoGanado;
    }

    public double getPremioMonetarioPendiente()
    {
        return premioMonetarioPendiente;
    }

    public void setPremioMonetarioPendiente(double premioMonetarioPendiente)
    {
        this.premioMonetarioPendiente = premioMonetarioPendiente;
    }

    public String convertirAArchivo()
    {
        return "CLIENTE;" 
                + getDocumentoIdentidad() + ";" 
                + getNombre() + ";" 
                + getCorreoElectronico() + ";" 
                + getLogin() + ";" 
                + getPassword() + ";" 
                + puntosFidelidad + ";" 
                + bonoDescuentoGanado + ";" 
                + premioMonetarioPendiente;
    }
}