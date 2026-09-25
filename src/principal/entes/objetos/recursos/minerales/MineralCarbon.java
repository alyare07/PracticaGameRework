package principal.entes.objetos.recursos.minerales;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;

public class MineralCarbon extends MineralCosechable {

	private static final long serialVersionUID = 1L;
	public static final double DURABILIDAD_CARBON = 80.0;
	public static final int SPRITE_INDEX = 4;

	public MineralCarbon(final int x, final int y) {
		super(x, y, DURABILIDAD_CARBON, SPRITE_INDEX);
	}

	public MineralCarbon(final int x, final int y, final double durabilidad) {
		super(x, y, DURABILIDAD_CARBON, SPRITE_INDEX);
		this.durabilidad = Math.max(0.0, Math.min(this.durabilidadMaxima, durabilidad));
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}
		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (LADO / 2);

		// Suelta 4 de carbón para combustible
		this.mundo.meterEntidad(new RecursoMaterial(dropX, dropY, 4, RecursoMaterial.COD_CARBON));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 18);
	}

	@Override
	public Objeto copiar() {
		return new MineralCarbon(this.getPosicionXInt(), this.getPosicionYInt(), this.durabilidad);
	}
}