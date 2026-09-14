package principal.entes.criaturas.enemigos.bandido;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.Ente;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.ia.arbol.FabricaArbolesIA;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class BandidoPistolero extends Bandido {

	private static final String NOMBRE = "Bandido Pistolero";
	private final Pistola pistola;

	public BandidoPistolero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.pistola = new Pistola(Pistola.COD_PISTOLA);
		this.areaDeteccionAncho = 260.0;
		this.areaDeteccionAlto = 260.0;
		this.configurarFootprint(10, 8, 0);
		this.arbolComportamiento = FabricaArbolesIA.ARBOL_BANDIDO_PISTOLERO;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.pistola.actualizarCicloRecarga(this);
	}

	@Override
	protected int obtenerClaveAnimacionActiva() {
		return this.estaEnMovimientoFisico() ? AnimacionesBandido.PISTOLA_CAMINANDO
				: AnimacionesBandido.PISTOLA_ESTANDAR;
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);

		if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.isEstadoJuego()) {
			final Ente obj = this.blackboard.getObjetivoActual();
			if (obj != null) {
				final boolean lineaLimpia = (this.mundo != null) && this.mundo.hayLineaDeTiroLimpia(this.getCentroX(),
						this.getCentroY(), obj.getCentroX(), obj.getCentroY());

				Render2D.dibujarLineaRefCamara(g, this.getCentroX(), this.getCentroY(), obj.getCentroX(),
						obj.getCentroY(), lineaLimpia ? Color.GREEN : Color.RED);
			}
		}
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (this.estaEnMovimientoFisico()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_CAMINANDO, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_ESTANDAR, this.atrasDeComplemento, true, flash);
		}
	}

	public Pistola getPistola() {
		return this.pistola;
	}

	@Override
	public String exportarSubtipoBandido() {
		return "Pistolero";
	}

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}