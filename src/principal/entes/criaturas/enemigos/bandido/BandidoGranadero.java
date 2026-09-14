package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.ia.arbol.FabricaArbolesIA;
import principal.mapa.Mundo;
import principal.utilidades.GestorTiempo;

public class BandidoGranadero extends Bandido {

	private static final String NOMBRE = "Bandido Granadero";
	private static final int TIEMPO_MS_LANZAMIENTO = 450;

	private final Granada granada;
	private final GestorTiempo GT_LANZAMIENTO = new GestorTiempo();

	public BandidoGranadero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.granada = new GranadaT1(100);
		this.areaDeteccionAncho = 300.0;
		this.areaDeteccionAlto = 300.0;

		this.configurarFootprint(8, 8, 0);
		this.arbolComportamiento = FabricaArbolesIA.ARBOL_BANDIDO_GRANADERO;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.granada.getCantidad() <= 1) {
			this.granada.establecerCantidad(100);
		}

		// Libera los estados transitorios de lanzamiento para permitir la vuelta a
		// ESTANDAR
		if (this.tieneEstado(Estado.ARROJANDO) && this.GT_LANZAMIENTO.transcurrioMiliSegundos(TIEMPO_MS_LANZAMIENTO)) {
			this.removerEstado(Estado.ARROJANDO);
			this.removerEstado(Estado.ATACANDO);
		}
	}

	public void arrojarGranadaHacia(final int targetX, final int targetY) {
		if ((this.granada != null) && (this.mundo != null)) {
			this.meterEstado(Estado.ATACANDO);
			this.meterEstado(Estado.ARROJANDO);
			this.GT_LANZAMIENTO.establecerReferenciaTiempoActual();
			this.granada.arrojar(targetX, targetY, this.direccion, this.mundo, this);
		}
	}

	@Override
	protected int obtenerClaveAnimacionActiva() {
		return this.estaEnMovimientoFisico() ? AnimacionesBandido.CAMINANDO : AnimacionesBandido.ESTANDAR;
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (this.estaEnMovimientoFisico()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.CAMINANDO, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.ESTANDAR, this.atrasDeComplemento, true, flash);
		}
	}

	@Override
	public String exportarSubtipoBandido() {
		return "Granadero";
	}

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}