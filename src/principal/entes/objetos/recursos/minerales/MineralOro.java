package principal.entes.objetos.recursos.minerales;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;

public class MineralOro extends MineralCosechable {

	private static final long serialVersionUID = 1L;
	public static final double DURABILIDAD_ORO = 160.0;
	public static final int SPRITE_INDEX = 3;

	public MineralOro(final int x, final int y) {
		super(x, y, DURABILIDAD_ORO, SPRITE_INDEX);
	}

	public MineralOro(final int x, final int y, final double durabilidad) {
		super(x, y, DURABILIDAD_ORO, SPRITE_INDEX);
		this.durabilidad = Math.max(0.0, Math.min(this.durabilidadMaxima, durabilidad));
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}
		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (LADO / 2);

		// Suelta 2 de oro y 1 de piedra
		this.mundo.meterEntidad(new RecursoMaterial(dropX, dropY, 2, RecursoMaterial.COD_ORO));
		this.mundo.meterEntidad(RecursoMaterial.crearPiedra(dropX + 2, dropY, 1));
		Globales.GESTOR_PARTICULAS.emitirExplosion(this.getCentroX(), this.getCentroY(), 2);
	}

	@Override
	public Objeto copiar() {
		return new MineralOro(this.getPosicionXInt(), this.getPosicionYInt(), this.durabilidad);
	}
}