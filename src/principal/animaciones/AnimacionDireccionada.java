package principal.animaciones;

import java.awt.Graphics2D;
import java.util.HashMap;

import principal.entes.criaturas.Criatura.Direccion;

/**
 * Agrupa y gestiona las animaciones de una criatura en las 4 orientaciones
 * cardinales.
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class AnimacionDireccionada {

	private final HashMap<Direccion, Animacion> ANIMACIONES;

	public AnimacionDireccionada(final Animacion aNorte, final Animacion aSur, final Animacion aEste,
			final Animacion aOeste) {
		this.ANIMACIONES = new HashMap<Direccion, Animacion>();
		this.ANIMACIONES.put(Direccion.NORTE, aNorte);
		this.ANIMACIONES.put(Direccion.SUR, aSur);
		this.ANIMACIONES.put(Direccion.ESTE, aEste);
		this.ANIMACIONES.put(Direccion.OESTE, aOeste);
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final Direccion direccion) {
		this.pintar(g, x, y, refCamara, direccion, false);
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final Direccion direccion, final boolean flash) {
		final Animacion anim = this.ANIMACIONES.get(direccion);
		if (anim != null) {
			anim.pintar(g, x, y, refCamara, flash);
		}
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final float alpha, final Direccion direccion) {
		this.pintarConTransparencia(g, x, y, refCamara, alpha, direccion, false);
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final float alpha, final Direccion direccion, final boolean flash) {
		final Animacion anim = this.ANIMACIONES.get(direccion);
		if (anim != null) {
			anim.pintarConTransparencia(g, x, y, refCamara, alpha, flash);
		}
	}

	public Animacion getAnimacion(final Direccion direccion) {
		return this.ANIMACIONES.get(direccion);
	}
}