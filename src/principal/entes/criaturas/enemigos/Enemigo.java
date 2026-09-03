package principal.entes.criaturas.enemigos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.facciones.GestorFacciones;
import principal.ia.aEstrella.NodoA;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.iluminacion.CalculadorSigilo;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta para todos los enemigos del juego.
 * <p>
 * <b>CARACTERÍSTICAS DEL MOTOR DE IA (v3.6):</b>
 * <ul>
 * <li><b>Control de Estados Robusto:</b> Previene el desenganche y la
 * congelación del enemigo en combate.</li>
 * <li><b>Targeting Multiobjetivo:</b> Selecciona dinámicamente cualquier
 * {@link Criatura} hostil o al jugador.</li>
 * <li><b>Sensorium Fotorreactivo:</b> Integración con {@link CalculadorSigilo}
 * para detección por luz y distancias.</li>
 * <li><b>Navegación Híbrida:</b> Conmuta automáticamente entre el flujo masivo
 * {@link DijkstraRework} y cálculo táctico individual con
 * {@link principal.ia.aEstrella.AEstrella}.</li>
 * <li><b>Zero-GC Sensory Geometry:</b> Reutilización de primitivas para
 * comprobaciones espaciales en caliente.</li>
 * </ul>
 * </p>
 * 
 * @version 3.6 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class Enemigo extends Criatura {

	/**
	 * Objetivo vivo actual hacia el que se orientan la persecución y los ataques.
	 */
	protected Criatura objetivoActual;

	protected boolean pendienteADijkstra;
	protected NodoD ant;

	protected final GestorTiempo GE_FUERA_DE_RANGO;
	protected final GestorTiempo GT_ATAQUE_INICIAL_COOLDOWN;
	protected final GestorTiempo GT_CARGA_ATAQUE;
	protected final GestorTiempo GT_RETOMAR_ATAQUE;
	protected final GestorTiempo GT_ACTUALIZACION_A_ESTRELLA;

	protected double areaDeteccionAncho;
	protected double areaDeteccionAlto;
	protected double ataque = 25;
	protected boolean realizandoAtaque;
	protected Rectangle rangoAtaqueMele;

	protected static final int ACCION_ESPERAR = 1;
	protected static final int ACCION_MOVER = 2;
	protected boolean enAccion;
	protected int accion;
	protected int tiempoAccionEsperaMs;

	// Búferes geométricos pre-asignados para Zero-GC
	private final Ellipse2D.Double AREA_DETECCION_AUXILIAR = new Ellipse2D.Double();
	protected final Rectangle AREA_RANGO_ATAQUE_MELE_AUXILIAR_NORTE = new Rectangle();
	protected final Rectangle AREA_RANGO_ATAQUE_MELE_AUXILIAR_SUR = new Rectangle();
	protected final Rectangle AREA_RANGO_ATAQUE_MELE_AUXILIAR_ESTE = new Rectangle();
	protected final Rectangle AREA_RANGO_ATAQUE_MELE_AUXILIAR_OESTE = new Rectangle();
	protected final Rectangle[] LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR = new Rectangle[4];

	public Enemigo(final double x, final double y, final int ancho, final int alto, final double vida,
			final double vidaMaxima, final Mundo mundo) {
		super(x, y, ancho, alto, vida, vidaMaxima);

		this.setFaccion(GestorFacciones.FACCION_MONSTRUOS);

		this.areaDeteccionAlto = 150;
		this.areaDeteccionAncho = 150;

		this.GE_FUERA_DE_RANGO = new GestorTiempo();
		this.GT_ATAQUE_INICIAL_COOLDOWN = new GestorTiempo();
		this.GT_CARGA_ATAQUE = new GestorTiempo();
		this.GT_RETOMAR_ATAQUE = new GestorTiempo();
		this.GT_ACTUALIZACION_A_ESTRELLA = new GestorTiempo();

		this.velocidad = 0.25;
		this.setEstadoUnico(Estado.ESTANDAR);
		this.mundo = mundo;

		this.destinoX = (int) x;
		this.destinoY = (int) y;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.curar();

		// 1. Pipeline Sensorial y Adquisición de Blancos
		this.actualizarPercepcionYObjetivo();

		// 2. Transición de Decisiones: Combate/Persecución vs Patrulla Pasiva
		if (this.objetivoActual != null) {
			this.actualizarAtaque();
		} else {
			this.tomarAccion();
		}

		// Curación o daño en modo prueba mediante clic secundario
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())
				&& Globales.RATON.presionadoClickDerUnicaAct()) {
			this.curar(Globales.JUGADOR.getDamage());
		}

		this.atrasDeComplemento = (this.mundo != null)
				&& this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
	}

	// =========================================================================
	// === SENSORIUM Y SELECCIÓN DE OBJETIVOS (ZERO-GC / O(1))
	// =========================================================================

	protected void actualizarPercepcionYObjetivo() {
		// Validar si el objetivo actual sigue con vida
		if (this.objetivoActual != null) {
			if (this.objetivoActual.estaEliminado()) {
				this.desactivarModoAgresivo();
				return;
			}

			final double rangoVision = this.areaDeteccionAncho / 2.0;
			final boolean detectable = CalculadorSigilo.puedeDetectar(this, this.objetivoActual, rangoVision);
			final boolean bajoAtaque = this.recibiendoAtaque();

			if (detectable || bajoAtaque) {
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}

			// Garantizar que si no está atacando, siempre permanezca en persecución
			if (!this.tieneEstado(Estado.ATACANDO) && !this.tieneEstado(Estado.PERSIGUIENDO)) {
				this.meterEstado(Estado.PERSIGUIENDO);
				this.removerEstado(Estado.ESTANDAR);
			}
			return;
		}

		// Escanear al Jugador prioritariamente si es hostil
		if (this.esHostilHacia(Globales.JUGADOR) && !Globales.JUGADOR.estaEliminado()) {
			final double rangoVision = this.areaDeteccionAncho / 2.0;
			if (CalculadorSigilo.puedeDetectar(this, Globales.JUGADOR, rangoVision) || this.recibiendoAtaque()) {
				this.fijarObjetivo(Globales.JUGADOR);
				return;
			}
		}

		// Escaneo en celdas espaciales locales para detectar otras criaturas enemigas
		if (!this.zonasOcupadas.isEmpty()) {
			Criatura blancoMasCercano = null;
			double menorDistSq = Double.MAX_VALUE;
			final double rangoVision = this.areaDeteccionAncho / 2.0;
			final double rangoVisionSq = rangoVision * rangoVision;
			final double miCentroX = this.getCentroX();
			final double miCentroY = this.getCentroY();

			final int cantZonas = this.zonasOcupadas.size();
			for (int z = 0; z < cantZonas; z++) {
				final ZoneBox zb = this.zonasOcupadas.get(z);
				final ArrayList<Criatura> lista = zb.getCriaturas();
				final int totalCriat = lista.size();

				for (int i = 0; i < totalCriat; i++) {
					final Criatura candidata = lista.get(i);
					if ((candidata == this) || candidata.estaEliminado() || !this.esHostilHacia(candidata)) {
						continue;
					}

					final double dx = miCentroX - candidata.getCentroX();
					final double dy = miCentroY - candidata.getCentroY();
					final double distSq = (dx * dx) + (dy * dy);

					// PODA TEMPRANA: Si ya encontramos a alguien más cerca, o si está fuera del
					// cono visual,
					// descartamos de inmediato sin evaluar sigilo ni luces
					if ((distSq >= menorDistSq) || (distSq > rangoVisionSq)) {
						continue;
					}

					// Solo si es el más cercano hasta ahora evaluamos iluminación
					if (CalculadorSigilo.puedeDetectar(distSq, candidata, rangoVision)) {
						menorDistSq = distSq;
						blancoMasCercano = candidata;
					}
				}
			}

			if (blancoMasCercano != null) {
				this.fijarObjetivo(blancoMasCercano);
			}
		}
	}

	public void fijarObjetivo(final Criatura objetivo) {
		if (objetivo == null) {
			return;
		}
		this.objetivoActual = objetivo;
		this.meterEstado(Estado.PERSIGUIENDO);
		this.removerEstado(Estado.ESTANDAR);
		this.enAccion = false;
		this.recorridoA.clear();
		this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
	}

	// =========================================================================
	// === MÁQUINA DE COMBATE Y APROXIMACIÓN
	// =========================================================================

	protected void actualizarAtaque() {
		if (this.objetivoActual == null) {
			this.desactivarModoAgresivo();
			return;
		}

		// --- FASE 1: Ejecución y recuperación del golpe cargado ---
		if (this.realizandoAtaque) {
			if (this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
				this.enAccion = false;

				final Rectangle rangoMele = this.rangoAtaqueMele;
				this.rangoAtaqueMele = null;

				if ((rangoMele != null) && (this.mundo != null)) {
					if (rangoMele.intersects(this.objetivoActual.getArea())) {
						this.objetivoActual.recibirAtaque(this.ataque, this);
					}
				}

				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
				this.meterEstado(Estado.PERSIGUIENDO); // Reenganche de persecución
			}
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		// --- FASE 2: Evaluación de rangos y movimiento hacia el objetivo ---
		final double rangoVision = this.areaDeteccionAncho / 2.0;
		final boolean objetivoVisible = CalculadorSigilo.puedeDetectar(this, this.objetivoActual, rangoVision);
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (objetivoVisible || dentroTiempoBusqueda) {
			this.rangoAtaqueMele = this.obtenerRangoAtaqueMeleValido();

			if (this.rangoAtaqueMele != null) {
				// En rango Melee -> Iniciar secuencia de golpe
				this.meterEstado(Estado.ATACANDO);
				this.removerEstado(Estado.CAMINANDO);
				this.removerEstado(Estado.PERSIGUIENDO);

				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
					this.realizandoAtaque = true;
					final Direccion dAtaque = this.getDireccionAtaqueMele();
					if (dAtaque != null) {
						this.direccion = dAtaque;
					} else {
						this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
					}
				}
			} else {
				// Fuera de rango Melee -> Aproximación táctica híbrida
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
			}
		} else {
			this.desactivarModoAgresivo();
		}
	}

	protected NodoD moverEnAtaque(final DijkstraRework d, final Terreno terreno) {
		if (d == null) {
			return null;
		}

		if (!this.pendienteADijkstra) {
			this.pendienteADijkstra = true;
			d.aumentarEntidadesPendientes();
		}

		// POR (Anclaje exacto en los pies para evitar solape de 2 px):
		final double pieX = this.getPosicionX() + (this.ANCHO / 2.0);
		final double pieY = (this.getPosicionY() + this.ALTO) - 3.0; // Centro vertical de la caja de pies

		final NodoD n = d.getNodoCercano((int) pieX, (int) pieY);

		if (this.ant != n) {
			this.ant = n;
		}
		if (n == null) {
			this.velActualX *= 0.8;
			this.velActualY *= 0.8;
			return null;
		}

		final int readBuf = d.getBufferLecturaIndex();

		final double offsetManadaX = ((this.hashCode() % 9) - 4.0) * 0.3; // Offset atenuado para no empujar a paredes
		final double offsetManadaY = (((this.hashCode() / 9) % 9) - 4.0) * 0.3;

		// El objetivo es alinear los pies del enemigo con el centro del nodo
		double targetX = n.getXMundo() + (n.getAncho() / 2.0) + offsetManadaX;
		double targetY = n.getYMundo() + (n.getAlto() / 2.0) + offsetManadaY;

		final double distAlNodoActual = Math.hypot(targetX - pieX, targetY - pieY);
		final NodoD siguienteNodo = n.getNodoProcedente(readBuf);

		if ((siguienteNodo != null) && (distAlNodoActual < Criatura.RADIO_ANTICIPACION_ESQUINA)) {
			final double sigX = siguienteNodo.getXMundo() + (siguienteNodo.getAncho() / 2.0) + offsetManadaX;
			final double sigY = siguienteNodo.getYMundo() + (siguienteNodo.getAlto() / 2.0) + offsetManadaY;

			final double t = 1.0 - (distAlNodoActual / Criatura.RADIO_ANTICIPACION_ESQUINA);
			targetX = targetX + ((sigX - targetX) * t);
			targetY = targetY + ((sigY - targetY) * t);
		}

		final double diffX = targetX - pieX;
		final double diffY = targetY - pieY;
		final double distanciaTotal = Math.hypot(diffX, diffY);

		if (distanciaTotal > 0.001) {
			final double dirDeseadaX = (diffX / distanciaTotal) * this.velocidad;
			final double dirDeseadaY = (diffY / distanciaTotal) * this.velocidad;

			this.velActualX += (dirDeseadaX - this.velActualX) * this.agilidadGiro;
			this.velActualY += (dirDeseadaY - this.velActualY) * this.agilidadGiro;

			if (Math.abs(this.velActualX) > 0.001) {
				if ((this.mundo != null) && !this.mundo
						.colisionaConZonaUObjetoSolido(this.getAreaColisionMovimiento(this.velActualX, 0.0))) {
					this.modificarPosicionX(this.velActualX);
				} else {
					this.velActualX = 0.0;
				}
			}
			if (Math.abs(this.velActualY) > 0.001) {
				if ((this.mundo != null) && !this.mundo
						.colisionaConZonaUObjetoSolido(this.getAreaColisionMovimiento(0.0, this.velActualY))) {
					this.modificarPosicionY(this.velActualY);
				} else {
					this.velActualY = 0.0;
				}
			}

			if (Math.abs(this.velActualX) > Math.abs(this.velActualY)) {
				this.direccion = (this.velActualX > 0) ? Direccion.ESTE : Direccion.OESTE;
			} else if (Math.abs(this.velActualY) > 0.01) {
				this.direccion = (this.velActualY > 0) ? Direccion.SUR : Direccion.NORTE;
			}

			this.setEstadoCaminando();
		} else {
			this.velActualX *= 0.5;
			this.velActualY *= 0.5;
		}

		return n;
	}

	protected void desactivarModoAgresivo() {
		this.objetivoActual = null;
		this.removerEstado(Estado.ATACANDO);
		this.removerEstado(Estado.PERSIGUIENDO);
		this.setEstadoEstandar();

		if (this.pendienteADijkstra && (this.mundo != null) && (this.mundo.getDijkstra() != null)) {
			this.pendienteADijkstra = false;
			this.mundo.getDijkstra().reducirEntidadesPendientes();
		}
	}

	protected Rectangle obtenerRangoAtaqueMeleValido() {
		if (this.objetivoActual == null) {
			return null;
		}
		for (final Rectangle r : this.rangosAtaqueMele()) {
			if ((r != null) && r.intersects(this.objetivoActual.getArea())) {
				return r;
			}
		}
		return null;
	}

	// =========================================================================
	// === PATRULLA PASIVA
	// =========================================================================

	protected void tomarAccion() {
		if (this.enAccion) {
			if (this.accion == ACCION_ESPERAR) {
				this.esperar();
			} else if (this.accion == ACCION_MOVER) {
				this.moverLugarRandom();
			}
			return;
		}

		this.accion = ALEATORIO.nextBoolean() ? ACCION_ESPERAR : ACCION_MOVER;
		this.enAccion = true;

		if (this.accion == ACCION_ESPERAR) {
			this.reiniciarRecorridoAEstrella();
			this.generarTiempoDeEspera();
			this.esperar();
		} else {
			this.cambiarDestinoAlAzar();
			this.moverLugarRandom();
		}
	}

	protected void esperar() {
		if (this.GT_ESPERA.transcurrioMiliSegundos(this.tiempoAccionEsperaMs)) {
			this.enAccion = false;
		}
		if (!this.tieneEstado(Estado.ESTANDAR)) {
			this.setEstadoEstandar();
		}
	}

	protected void generarTiempoDeEspera() {
		final int minMs = 1500;
		final int maxMs = 10000;
		this.tiempoAccionEsperaMs = ALEATORIO.nextInt((maxMs - minMs) + 1) + minMs;
		this.GT_ESPERA.establecerReferenciaTiempoActual();
	}

	protected void cambiarDestinoAlAzar() {
		if ((this.mundo == null) || (this.getMundo().getAEstrellaX12X20() == null)) {
			return;
		}

		boolean destinoFactible = false;
		final int desplazamiento = this.mundo.getTerreno().ladoTile() * 3;

		final int minX = this.getPosicionXInt() - desplazamiento;
		final int maxX = this.getPosicionXInt() + desplazamiento;
		final int minY = this.getPosicionYInt() - desplazamiento;
		final int maxY = this.getPosicionYInt() + desplazamiento;

		int intentos = 0;
		final Dimension dimNodoA = this.getMundo().getAEstrellaX12X20().getDimensionNodoA();
		final Rectangle areaPrueba = new Rectangle(0, 0, dimNodoA.width, dimNodoA.height);

		while (!destinoFactible && (intentos < 20)) {
			intentos++;

			this.destinoX = ALEATORIO.nextInt((maxX - minX) + 1) + minX;
			this.destinoY = ALEATORIO.nextInt((maxY - minY) + 1) + minY;

			final NodoA nodoDestino = this.getMundo().getAEstrellaX12X20().getNodoRef(this.destinoX, this.destinoY);

			if (nodoDestino != null) {
				areaPrueba.x = nodoDestino.getXNodo() * dimNodoA.width;
				areaPrueba.y = nodoDestino.getYNodo() * dimNodoA.height;

				if (!this.mundo.colisionaConZonaUObjetoSolido(areaPrueba)) {
					this.getMundo().getAEstrellaX12X20().getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(),
							this.destinoX, this.destinoY, this.recorridoA);
					if ((this.recorridoA != null) && !this.recorridoA.isEmpty()) {
						destinoFactible = true;
					}
				}
			}
		}

		if (destinoFactible && !this.recorridoA.isEmpty()) {
			this.nodoADestino = this.recorridoA.poll();
		}
	}

	protected void moverLugarRandom() {
		if ((this.recorridoA == null) || this.recorridoA.isEmpty()) {
			this.enAccion = false;
			return;
		}

		if ((this.nodoADestino != null)
				&& this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())) {
			if (this.recorridoA.isEmpty()) {
				this.nodoADestino = this.recorridoA.poll();
			}
		}

		final NodoA ultimoNodo = this.recorridoA.getLast();
		final boolean llegoAlFinal = (this.nodoADestino == ultimoNodo)
				&& (this.getPosicionXInt() == (ultimoNodo.getXNodo()
						* this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width))
				&& (this.getPosicionYInt() == (ultimoNodo.getYNodo()
						* this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height));

		if (llegoAlFinal) {
			this.enAccion = false;
		} else {
			this.moverANodoADestino();
			if (!this.estaEstadoCaminando()) {
				this.setEstadoCaminando();
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
		if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.estadoJuego) {
			Render2D.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.getPosicionX() - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0)),
							(int) ((this.getPosicionY() - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0)),
							(int) this.areaDeteccionAncho, (int) this.areaDeteccionAlto),
					Color.RED);
			Render2D.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.getPosicionX() - (this.areaDeteccionAncho / 8.0)) + (this.ANCHO / 2.0)),
							(int) ((this.getPosicionY() - (this.areaDeteccionAlto / 8.0)) + (this.ALTO / 2.0)),
							(int) (this.areaDeteccionAncho / 4.0), (int) (this.areaDeteccionAlto / 4.0)),
					Color.ORANGE);
		}
	}

	public boolean recibiendoAtaque() {
		return !this.GT_ATACADO.transcurrioMiliSegundos(this.getTiempoMsEsperaAtacado());
	}

	protected void curar() {
		if (this.vida >= this.vidaMaxima) {
			return;
		}

		if (!this.recibiendoAtaque() && this.GT_CURACION.transcurrioMiliSegundos(this.getTiempoMsEsperaRegenVida())) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	public Ellipse2D getAreaDeteccionLogica() {
		this.AREA_DETECCION_AUXILIAR.setFrame(
				(this.getPosicionX() - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0),
				(this.getPosicionY() - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
		return this.AREA_DETECCION_AUXILIAR;
	}

	protected Rectangle[] rangosAtaqueMele() {
		this.LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR[0] = this.rangoAtaqueMeleOeste();
		this.LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR[1] = this.rangoAtaqueMeleEste();
		this.LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR[2] = this.rangoAtaqueMeleNorte();
		this.LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR[3] = this.rangoAtaqueMeleSur();
		return this.LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR;
	}

	protected Direccion getDireccionAtaqueMele() {
		if (this.rangoAtaqueMele == null) {
			return null;
		}

		if (this.rangoAtaqueMele.equals(this.rangoAtaqueMeleNorte())) {
			return Direccion.NORTE;
		}
		if (this.rangoAtaqueMele.equals(this.rangoAtaqueMeleSur())) {
			return Direccion.SUR;
		}
		if (this.rangoAtaqueMele.equals(this.rangoAtaqueMeleOeste())) {
			return Direccion.OESTE;
		}
		if (this.rangoAtaqueMele.equals(this.rangoAtaqueMeleEste())) {
			return Direccion.ESTE;
		}

		return null;
	}

	protected Rectangle rangoAtaqueMeleNorte() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_NORTE.setBounds((int) this.getXRangoAtaqueMele(),
				(int) (this.getYRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()),
				(int) this.getGrosorRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_NORTE;
	}

	protected Rectangle rangoAtaqueMeleSur() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_SUR.setBounds((int) this.getXRangoAtaqueMele(),
				(int) this.getYRangoAtaqueMele(), (int) this.getGrosorRangoAtaqueMele(),
				(int) this.getAlcanceRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_SUR;
	}

	protected Rectangle rangoAtaqueMeleEste() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_ESTE.setBounds((int) this.getXRangoAtaqueMele(),
				(int) this.getYRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele(),
				(int) this.getGrosorRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_ESTE;
	}

	protected Rectangle rangoAtaqueMeleOeste() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_OESTE.setBounds(
				(int) (this.getXRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()), (int) this.getYRangoAtaqueMele(),
				(int) this.getAlcanceRangoAtaqueMele(), (int) this.getGrosorRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR_OESTE;
	}

	protected abstract double getXRangoAtaqueMele();

	protected abstract double getYRangoAtaqueMele();

	protected abstract double getAlcanceRangoAtaqueMele();

	protected abstract double getGrosorRangoAtaqueMele();

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		if (causante instanceof Criatura) {
			this.GT_ATACADO.establecerReferenciaTiempoActual();
			this.fijarObjetivo((Criatura) causante);
		}
		super.recibirAtaque(damage, causante);
	}

	public Criatura getObjetivoActual() {
		return this.objetivoActual;
	}

	protected abstract int getTiempoMsEsperaRegenVida();

	protected abstract int getTiempoMsEsperaAtacado();

	protected abstract int getTiempoMsBusquedaFueraRango();

	protected abstract int getTiempoMsEsperaAtaqueInicial();

	protected abstract int getTiempoMsEsperaRetomarAtaque();

	@Override
	public void eliminar() {
		GestorSonido.reproducir(IDSonido.CRIATURA_MUERTA);
		this.desactivarModoAgresivo();
		super.eliminar();
	}

}