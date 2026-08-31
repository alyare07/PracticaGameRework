package principal.entes.criaturas.enemigos.bandido;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;
import principal.utilidades.Globales;

/**
 * Variación del enemigo Bandido enfocado en combate a distancia con arma de
 * fuego. Dispara únicamente cuando la línea de tiro recta hasta el jugador está
 * 100% libre de obstáculos.
 */
public class BandidoPistolero extends Bandido {

	private final int rangoDisparo = 248;
	private final Pistola pistola;

	// Scratchpad de reuso para verificar línea de tiro (0 GC)
	private final Rectangle rPistola2 = new Rectangle();
	private final Rectangle rPistolaDistancia = new Rectangle();

	public BandidoPistolero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.pistola = new Pistola(ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(100));
		this.areaDeteccionAncho = this.rangoDisparo * 2;
		this.areaDeteccionAlto = this.rangoDisparo * 2;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.pistola.getMunicion().getCantidad() <= 1) {
			this.pistola.getMunicion().restablecer();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola2(this.rangoDisparo),
					Color.red);
		}
	}

	private void pintarSprite(final Graphics2D g) {
		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_ESTANDAR, this.atrasDeComplemento, true);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_CAMINANDO, this.atrasDeComplemento, true);
		}
	}

	/**
	 * Evalúa si existe una línea de tiro recta (Norte, Sur, Este u Oeste)
	 * completamente libre de árboles o paredes sólidas entre el bandido y el
	 * jugador.
	 */
	private boolean tieneLineaDeTiroLimpia(final Jugador jugador) {
		final double diffX = jugador.getCentroX() - this.getCentroX();
		final double diffY = jugador.getCentroY() - this.getCentroY();
		final double distanciaMundo = Math.hypot(diffX, diffY);

		// Fuera del alcance del arma
		if (distanciaMundo > this.rangoDisparo) {
			return false;
		}

		// Comprobar si el jugador está alineado dentro del ancho del sprite
		final boolean alineadoHorizontal = Math.abs(diffY) <= (this.ALTO / 2.0);
		final boolean alineadoVertical = Math.abs(diffX) <= (this.ANCHO / 2.0);

		if (!alineadoHorizontal && !alineadoVertical) {
			return false; // No están alineados en línea recta
		}

		// Determinar la dirección de tiro
		if (alineadoHorizontal) {
			this.direccion = (diffX > 0) ? Direccion.ESTE : Direccion.OESTE;
		} else {
			this.direccion = (diffY > 0) ? Direccion.SUR : Direccion.NORTE;
		}

		// Generar rectángulo de distancia hasta el jugador
		final int dist = (int) (alineadoHorizontal ? Math.abs(diffX) : Math.abs(diffY));
		final Rectangle rLineaFuego = this.getRectanguloInterseccionPistola2(dist);

		// Si intersecta cualquier árbol u objeto sólido, la línea NO está limpia
		return !this.mundo.colisionaConZonaUObjetoSolido(rLineaFuego);
	}

	/**
	 * Lógica de ataque a distancia con arma de fuego.
	 */
	@Override
	protected void actualizarAtaque() {
		// --- FASE 1: Disparo y tiempo de recuperación ---
		if (this.realizandoAtaque) {
			if (this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
				if (this.enAccion) {
					this.enAccion = false;
				}
				this.pistola.disparar(this.getCentroX(), this.getCentroY(), this.direccion, this.mundo, this, true);
				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
			}
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		// --- FASE 2: Detección, disparo o navegación Dijkstra ---
		final Jugador jugador = Globales.JUGADOR;
		final boolean jugadorEnVision = this.getAreaDeteccionLogica().intersects(jugador.getRectangulo());
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (jugadorEnVision || dentroTiempoBusqueda) {
			if (jugadorEnVision && this.tieneLineaDeTiroLimpia(jugador)) {
				// ¡Línea de tiro 100% limpia sin árboles! Detenerse y disparar
				this.meterEstado(Estado.ATACANDO);
				this.removerEstado(Estado.CAMINANDO);
				this.removerEstado(Estado.PERSIGUIENDO);

				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
					if (!this.realizandoAtaque) {
						this.realizandoAtaque = true;
						this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
					}
				}
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();

			} else {
				// Línea bloqueada por árboles o desalineado -> Rodear el bosque usando Dijkstra
				this.removerEstado(Estado.ATACANDO);
				this.meterEstado(Estado.PERSIGUIENDO);
				this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		} else {
			this.desactivarModoAgresivo();
		}
	}

	private Rectangle getRectanguloInterseccionPistola2(final int rango) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		switch (this.direccion) {
		case OESTE:
			this.rPistola2.setBounds(posX - rango, posY, rango, this.ALTO);
			break;
		case NORTE:
			this.rPistola2.setBounds(posX, posY - rango, this.ANCHO, rango);
			break;
		case ESTE:
			this.rPistola2.setBounds(posX + this.ANCHO, posY, rango, this.ALTO);
			break;
		case SUR:
			this.rPistola2.setBounds(posX, posY + this.ALTO, this.ANCHO, rango);
			break;
		}
		return this.rPistola2;
	}

	// --- Métodos de Contrato Melee (No utilizados por pistolero) ---

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
		return this.getTiempoMsEsperaRetomarAtaque();
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1350;
	}
}