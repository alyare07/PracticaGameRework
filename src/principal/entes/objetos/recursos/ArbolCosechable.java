package principal.entes.objetos.recursos;

import java.awt.image.BufferedImage;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

public class ArbolCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	private boolean esTocon = false;
	private final int codTexturaArbol;

	public ArbolCosechable(final int x, final int y, final int codTexturaArbol) {
		super(x, y, 100.0, TipoHerramienta.HACHA);
		this.codTexturaArbol = codTexturaArbol;
	}

	@Override
	public void destruir(final Ente causante) {
		if (!this.esTocon) {
			this.soltarBotin();
			this.esTocon = true;
			this.durabilidadMaxima = 40.0;
			this.durabilidad = this.durabilidadMaxima;
			this.activarShake();
		} else {
			this.soltarBotin();
			super.destruir(causante);
		}
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);

		// Drop de Madera real
		final int cantidadMadera = this.esTocon ? 2 : 5;
		this.mundo.meterEntidad(RecursoMaterial.crearMadera(dropX, dropY, cantidadMadera));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 18);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 6);
	}

	@Override
	public BufferedImage getTextura() {
		return Textura.getTextura(this.codTexturaArbol);
	}

	@Override
	public int getAncho() {
		return 32;
	}

	@Override
	public int getAlto() {
		return 32;
	}

	@Override
	public Objeto copiar() {
		return new ArbolCosechable(this.getPosicionXInt(), this.getPosicionYInt(), this.codTexturaArbol);
	}

	public boolean isEsTocon() {
		return this.esTocon;
	}
}