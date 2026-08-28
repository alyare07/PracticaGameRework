package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

/**
 * Variación del enemigo Bandido enfocado en ataques a distancia utilizando
 * granadas.
 */
public class BandidoGranadero extends Bandido {

	private final Granada granada;
	private final Ellipse2D.Double areaDeteccionLogica = new Ellipse2D.Double();

	public BandidoGranadero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.granada = new GranadaT1(100);
		this.areaDeteccionAncho = 300;
		this.areaDeteccionAlto = 300;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		// Recarga automática para mantener munición infinita en el enemigo
		if (this.granada.getCantidad() <= 1) {
			this.granada.establecerCantidad(100);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.ESTANDAR, this.atrasDeComplemento, true);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.CAMINANDO, this.atrasDeComplemento, true);
		}
	}

	/**
	 * Implementación específica de ataque a distancia. Orienta al enemigo hacia el
	 * jugador y lanza la granada al finalizar la carga.
	 */
	@Override
	protected void actualizarAtaque() {
		// --- FASE 1: Lanzamiento y recuperación del disparo ---
		if (this.realizandoAtaque) {
			if (this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
				this.enAccion = false;

				// Arrojar granada apuntando al centro de la caja de colisión del jugador
				final int targetX = Globales.JUGADOR.getPosicionXInt() + (Globales.JUGADOR.getAncho() / 2);
				final int targetY = Globales.JUGADOR.getPosicionYInt() + (Globales.JUGADOR.getAlto() / 2);

				this.granada.arrojar(targetX, targetY, this.direccion, this.mundo, this, false);

				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
				this.removerEstado(Estado.ARROJANDO);
			}
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		// --- FASE 2: Evaluación de visión y toma de decisiones ---
		final boolean jugadorEnVisión = this.getAreaDeteccionLogica().intersects(Globales.JUGADOR.getRectangulo());
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (jugadorEnVisión || dentroTiempoBusqueda) {
			if (jugadorEnVisión) {
				// El jugador está en rango de tiro (300px) -> Detenerse, apuntar y cargar
				// ataque
				this.meterEstado(Estado.ATACANDO);
				this.meterEstado(Estado.ARROJANDO);
				this.removerEstado(Estado.CAMINANDO);
				this.removerEstado(Estado.PERSIGUIENDO);

				// Orientación visual hacia la posición del jugador
				this.direccion = Globales.FUNCIONES.getDireccionMirando(this.getPosicionXInt(), this.getPosicionYInt(),
						Globales.JUGADOR.getPosicionXInt(), Globales.JUGADOR.getPosicionYInt(), true);

				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
					if (!this.realizandoAtaque) {
						this.realizandoAtaque = true;
						this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
					}
				}
			} else {
				// Perdió línea de visión directa pero sigue en persecución -> Acercarse con
				// Dijkstra
				this.removerEstado(Estado.ATACANDO);
				this.removerEstado(Estado.ARROJANDO);
				this.meterEstado(Estado.PERSIGUIENDO);

				this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
			}
		} else {
			// El jugador escapó y terminó el tiempo de gracia -> Volver a estado pasivo
			this.desactivarModoAgresivo();
		}
	}

	/**
	 * Retorna el área de detección reutilizando una única instancia de Ellipse2D
	 * para prevenir la creación continua de objetos en la memoria Heap.
	 */
	@Override
	public Ellipse2D getAreaDeteccionLogica() {
		this.areaDeteccionLogica.setFrame((this.getPosicionX() - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0),
				(this.getPosicionY() - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
		return this.areaDeteccionLogica;
	}

	// --- Métodos de Contrato Melee (No utilizados por atacantes a distancia) ---

	@Override
	protected double getXRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 0;
	}

	// --- Tiempos de Recarga y Animación ---

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return this.getTiempoMsEsperaRetomarAtaque();
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1350;
	}
}