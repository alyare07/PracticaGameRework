package principal.entes.objetos.recursos;

import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

public class RocaCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	private final int codTexturaRoca;

	public RocaCosechable(final int x, final int y, final int codTexturaRoca) {
		super(x, y, 120.0, TipoHerramienta.PICO);
		this.codTexturaRoca = codTexturaRoca;
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);

		// Drop de Piedra real
		this.mundo.meterEntidad(RecursoMaterial.crearPiedra(dropX, dropY, 4));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 20);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 8);
	}

	@Override
	public BufferedImage getTextura() {
		return Textura.getTextura(this.codTexturaRoca);
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
		return new RocaCosechable(this.getPosicionXInt(), this.getPosicionYInt(), this.codTexturaRoca);
	}
}