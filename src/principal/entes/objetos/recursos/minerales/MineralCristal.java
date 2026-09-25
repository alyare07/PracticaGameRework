package principal.entes.objetos.recursos.minerales;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;

public class MineralCristal extends MineralCosechable {

	private static final long serialVersionUID = 1L;
	public static final double DURABILIDAD_CRISTAL = 200.0;
	public static final int SPRITE_INDEX = 5;

	public MineralCristal(final int x, final int y) {
		super(x, y, DURABILIDAD_CRISTAL, SPRITE_INDEX);
	}

	public MineralCristal(final int x, final int y, final double durabilidad) {
		super(x, y, DURABILIDAD_CRISTAL, SPRITE_INDEX);
		this.durabilidad = Math.max(0.0, Math.min(this.durabilidadMaxima, durabilidad));
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}
		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (LADO / 2);

		// Suelta 2 cristales arcanos
		this.mundo.meterEntidad(new RecursoMaterial(dropX, dropY, 2, RecursoMaterial.COD_CRISTAL));
		Globales.GESTOR_PARTICULAS.emitirMagia(this.getCentroX(), this.getCentroY(), 25);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirMagia(this.getCentroX(), this.getCentroY(), 4);
	}

	@Override
	public Objeto copiar() {
		return new MineralCristal(this.getPosicionXInt(), this.getPosicionYInt(), this.durabilidad);
	}
}