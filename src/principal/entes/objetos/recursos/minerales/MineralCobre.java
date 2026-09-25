package principal.entes.objetos.recursos.minerales;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;

public class MineralCobre extends MineralCosechable {

	private static final long serialVersionUID = 1L;
	public static final double DURABILIDAD_COBRE = 120.0;
	public static final int SPRITE_INDEX = 1;

	public MineralCobre(final int x, final int y) {
		super(x, y, DURABILIDAD_COBRE, SPRITE_INDEX);
	}

	public MineralCobre(final int x, final int y, final double durabilidad) {
		super(x, y, DURABILIDAD_COBRE, SPRITE_INDEX);
		this.durabilidad = Math.max(0.0, Math.min(this.durabilidadMaxima, durabilidad));
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}
		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (LADO / 2);

		// Suelta 3 de cobre y 1 de piedra residual
		this.mundo.meterEntidad(new RecursoMaterial(dropX, dropY, 3, RecursoMaterial.COD_COBRE));
		this.mundo.meterEntidad(RecursoMaterial.crearPiedra(dropX + 2, dropY, 1));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 18);
	}

	@Override
	public Objeto copiar() {
		return new MineralCobre(this.getPosicionXInt(), this.getPosicionYInt(), this.durabilidad);
	}
}