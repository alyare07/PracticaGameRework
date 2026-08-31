package principal.entes.criaturas.enemigos.bandido;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Variación del enemigo Bandido especializado en combate a distancia con arma
 * de fuego.
 * <p>
 * <b>CARACTERÍSTICAS TÉCNICAS (v3.5):</b>
 * <ul>
 * <li><b>Cargador Táctico y Ventana de Recarga:</b> Gestiona 12 proyectiles en
 * recámara. Al vaciarse, entra en recarga activa durante 1.2 s y se reposiciona
 * tácticamente sin disparar.</li>
 * <li><b>Balística Vectorial 360°:</b> Emisión de proyectiles precisos hacia el
 * centro de masa del blanco.</li>
 * <li><b>Línea de Tiro DDA:</b> Evaluación instantánea de obstáculos antes de
 * abrir fuego.</li>
 * </ul>
 * </p>
 * 
 * @version 3.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public class BandidoPistolero extends Bandido {

	private final int rangoDisparo = 248;
	private final Pistola pistola;

	public BandidoPistolero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.pistola = new Pistola(ListaModelosItem.COD_EQUIPABLE_ARMA);
		this.areaDeteccionAncho = this.rangoDisparo * 2;
		this.areaDeteccionAlto = this.rangoDisparo * 2;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		// Actualiza el temporizador de recarga de la pistola para la IA
		this.pistola.actualizarCicloRecarga(this);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);

		// Línea de tiro de depuración hacia el objetivo
		if (Globales.TECLADO.TECLA_DEBUG.presionado() && (this.objetivoActual != null) && Globales.isEstadoJuego()) {
			final int x1 = this.getCentroX();
			final int y1 = this.getCentroY();
			final int x2 = this.objetivoActual.getCentroX();
			final int y2 = this.objetivoActual.getCentroY();
			final boolean lineaLimpia = this.tieneLineaDeTiroLimpia(this.objetivoActual);

			Render2D.dibujarLineaRefCamara(g, x1, y1, x2, y2, lineaLimpia ? Color.GREEN : Color.RED);
		}
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_ESTANDAR, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_CAMINANDO, this.atrasDeComplemento, true, flash);
		}
	}

	/**
	 * Evalúa mediante Raycasting DDA en 360 grados si existe una línea de tiro
	 * completamente despejada entre el bandido y el objetivo.
	 */
	private boolean tieneLineaDeTiroLimpia(final Criatura objetivo) {
		if ((objetivo == null) || objetivo.estaEliminado() || (this.mundo == null)) {
			return false;
		}

		final double origenX = this.getCentroX();
		final double origenY = this.getCentroY();
		final double destX = objetivo.getCentroX();
		final double destY = objetivo.getCentroY();

		final double dx = destX - origenX;
		final double dy = destY - origenY;
		final double distSq = (dx * dx) + (dy * dy);

		if (distSq > (this.rangoDisparo * this.rangoDisparo)) {
			return false;
		}

		return this.mundo.getTerreno().hayLineaDeVisionLimpia(origenX, origenY, destX, destY);
	}

	/**
	 * Lógica de combate y disparo balístico en 360 grados considerando recarga y
	 * cadencia.
	 */
	@Override
	protected void actualizarAtaque() {
		if (this.objetivoActual == null) {
			this.desactivarModoAgresivo();
			return;
		}

		// Si el arma está en proceso de recarga activa, la IA se reposiciona
		// tácticamente
		if (this.pistola.isRecargando()) {
			this.reposicionarseHaciaObjetivo();
			return;
		}

		// --- FASE 1: Disparo balístico directo al centro del objetivo ---
		if (this.realizandoAtaque) {
			if (this.GT_CARGA_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
				this.enAccion = false;

				final int origenX = this.getCentroX();
				final int origenY = this.getCentroY();
				final int targetX = this.objetivoActual.getCentroX();
				final int targetY = this.objetivoActual.getCentroY();
				final boolean soloJugador = (this.objetivoActual instanceof Jugador);

				this.setDireccionMirandoCriatura(this.objetivoActual);

				// Ejecuta el disparo consumiendo un cartucho de la recámara
				this.pistola.disparar(origenX, origenY, targetX, targetY, this.mundo, this, soloJugador);

				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
				this.meterEstado(Estado.PERSIGUIENDO);
			}
			return;
		}

		// Si aún está en tiempo de enfriamiento (cooldown) tras un disparo
		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			if (!this.tieneLineaDeTiroLimpia(this.objetivoActual)) {
				this.reposicionarseHaciaObjetivo();
			}
			return;
		}

		// --- FASE 2: Detección, inicio de carga o reposicionamiento táctico ---
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (this.tieneLineaDeTiroLimpia(this.objetivoActual)) {
			this.meterEstado(Estado.ATACANDO);
			this.removerEstado(Estado.CAMINANDO);
			this.removerEstado(Estado.PERSIGUIENDO);
			this.setDireccionMirandoCriatura(this.objetivoActual);

			if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
				if (!this.realizandoAtaque) {
					this.realizandoAtaque = true;
					this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
				}
			}
			this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();

		} else if (dentroTiempoBusqueda) {
			this.reposicionarseHaciaObjetivo();
		} else {
			this.desactivarModoAgresivo();
		}
	}

	private void reposicionarseHaciaObjetivo() {
		this.removerEstado(Estado.ATACANDO);
		this.meterEstado(Estado.PERSIGUIENDO);

		if (this.objetivoActual instanceof Jugador) {
			this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
		} else {
			if (this.GT_ACTUALIZACION_A_ESTRELLA.transcurrioMiliSegundos(500)
					|| ((this.nodoADestino == null) && this.recorridoA.isEmpty())) {
				this.calcularRutaAEstrella(this.objetivoActual.getCentroX(), this.objetivoActual.getCentroY());
				this.GT_ACTUALIZACION_A_ESTRELLA.establecerReferenciaTiempoActual();
			}
			this.moverANodoADestino();
		}
		this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
	}

	// --- Métodos de Contrato Melee (No utilizados por atacantes a distancia) ---

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

	@Override
	protected double getXRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return 500;
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1100;
	}
}