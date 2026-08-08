package principal.animaciones;

import java.awt.Graphics2D;
import java.util.HashMap;

import principal.entes.criaturas.Criatura.Direccion;

public class AnimacionDireccionada{
    private final HashMap<Direccion, Animacion> ANIMACIONES;

    public AnimacionDireccionada(final Animacion aNorte, final Animacion aSur, final Animacion aEste, final Animacion aOeste) {
	this.ANIMACIONES = new HashMap<Direccion, Animacion>();
	this.ANIMACIONES.put(Direccion.NORTE, aNorte);
	this.ANIMACIONES.put(Direccion.SUR, aSur);
	this.ANIMACIONES.put(Direccion.ESTE, aEste);
	this.ANIMACIONES.put(Direccion.OESTE, aOeste);

    }

    public void pintar(final Graphics2D g, final double x, final double y, final boolean refCamara, final Direccion direccion) {
	this.ANIMACIONES.get(direccion).pintar(g, x, y, refCamara);
    }

    public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refCamara, final float alpha, final Direccion direccion) {
	this.ANIMACIONES.get(direccion).pintarConTransparencia(g, x, y, refCamara, alpha);
    }

    public Animacion getAnimacion(final Direccion direccion) {
	return this.ANIMACIONES.get(direccion);
    }

}
