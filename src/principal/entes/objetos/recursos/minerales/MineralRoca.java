package principal.entes.objetos.recursos.minerales;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.utilidades.Globales;

public class MineralRoca extends MineralCosechable {

	private static final long serialVersionUID = 1L;
	public static final double DURABILIDAD_ROCA = 100.0;
	public static final int SPRITE_INDEX = 0;

	public MineralRoca(final int x, final int y) {
		super(x, y, DURABILIDAD_ROCA, SPRITE_INDEX);
	}

	public MineralRoca(final int x, final int y, final double durabilidad) {
		super(x, y, DURABILIDAD_ROCA, SPRITE_INDEX);
		this.durabilidad = Math.max(0.0, Math.min(this.durabilidadMaxima, durabilidad));
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}
		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (LADO / 2);

		this.mundo.meterEntidad(RecursoMaterial.crearPiedra(dropX, dropY, 4));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 18);
	}

	@Override
	public Objeto copiar() {
		return new MineralRoca(this.getPosicionXInt(), this.getPosicionYInt(), this.durabilidad);
	}
}