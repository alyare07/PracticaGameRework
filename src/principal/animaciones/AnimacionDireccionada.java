package principal.animaciones;

import java.awt.Graphics2D;

import principal.entes.criaturas.Criatura.Direccion;

public class AnimacionDireccionada {

	private final Animacion[] animaciones = new Animacion[4];

	public AnimacionDireccionada(final Animacion aNorte, final Animacion aSur, final Animacion aEste,
			final Animacion aOeste) {
		this.animaciones[Direccion.NORTE.ordinal()] = aNorte;
		this.animaciones[Direccion.SUR.ordinal()] = aSur;
		this.animaciones[Direccion.ESTE.ordinal()] = aEste;
		this.animaciones[Direccion.OESTE.ordinal()] = aOeste;
	}

	public void actualizar(final Direccion direccion) {
		if (direccion != null) {
			final Animacion anim = this.animaciones[direccion.ordinal()];
			if (anim != null) {
				anim.actualizar();
			}
		}
	}

	public void actualizarTodas() {
		for (int i = 0; i < this.animaciones.length; i++) {
			if (this.animaciones[i] != null) {
				this.animaciones[i].actualizar();
			}
		}
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final Direccion direccion) {
		this.pintar(g, x, y, refCamara, direccion, false);
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final Direccion direccion, final boolean flash) {
		if (direccion != null) {
			final Animacion anim = this.animaciones[direccion.ordinal()];
			if (anim != null) {
				anim.pintar(g, x, y, refCamara, flash);
			}
		}
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final float alpha, final Direccion direccion) {
		this.pintarConTransparencia(g, x, y, refCamara, alpha, direccion, false);
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refCamara,
			final float alpha, final Direccion direccion, final boolean flash) {
		if (direccion != null) {
			final Animacion anim = this.animaciones[direccion.ordinal()];
			if (anim != null) {
				anim.pintarConTransparencia(g, x, y, refCamara, alpha, flash);
			}
		}
	}

	public Animacion getAnimacion(final Direccion direccion) {
		return (direccion != null) ? this.animaciones[direccion.ordinal()] : null;
	}
}