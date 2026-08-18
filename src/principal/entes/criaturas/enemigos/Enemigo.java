package principal.entes.criaturas.enemigos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.proyectil.filtro.GolpeMeleContraJugador;
import principal.ia.aEstrella.NodoA;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta para todos los enemigos del juego. Implementa la IA agresiva y
 * pasiva basada en máquina de estados y navegación Dijkstra/A*.
 */
public abstract class Enemigo extends Criatura {

	protected boolean pendienteADijkstra;
	protected NodoD ant;

	protected final GestorTiempo GE_FUERA_DE_RANGO;
	protected final GestorTiempo GT_ATAQUE_INICIAL_COOLDOWN;
	protected final GestorTiempo GT_CARGA_ATAQUE;
	protected final GestorTiempo GT_RETOMAR_ATAQUE;

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
	protected final Rectangle AREA_RANGO_ATAQUE_MELE_AUXILIAR = new Rectangle();
	protected final Rectangle[] LISTA_AREA_RANGO_ATAQUE_MELE_AUXILIAR = new Rectangle[4];

	public Enemigo(final double x, final double y, final int ancho, final int alto, final double vida,
			final double vidaMaxima, final Mundo mundo) {
		super(x, y, ancho, alto, vida, vidaMaxima);
		this.areaDeteccionAlto = 150;
		this.areaDeteccionAncho = 150;

		this.GE_FUERA_DE_RANGO = new GestorTiempo();
		this.GT_ATAQUE_INICIAL_COOLDOWN = new GestorTiempo();
		this.GT_CARGA_ATAQUE = new GestorTiempo();
		this.GT_RETOMAR_ATAQUE = new GestorTiempo();

		this.velocidad = 0.25;
		this.setEstadoUnico(Estado.ESTANDAR);
		this.mundo = mundo;

		this.destinoX = (int) x;
		this.destinoY = (int) y;
	}

	@Override
	public void actualizar() {
		this.curar();
		if (Globales.TECLADO.TECLA_DIJKSTRA.presionado()) {

			// 1. Evaluación de detección del jugador o reacción a ataques recibidos
			final boolean jugadorDetectado = this.getAreaDeteccionLogica()
					.intersects(Globales.JUGADOR.getRectangulo());
			final boolean bajoAtaque = this.recibiendoAtaque();

			if (jugadorDetectado || bajoAtaque) {
				if (!this.tieneEstado(Estado.PERSIGUIENDO) && !this.tieneEstado(Estado.ATACANDO)) {
					this.meterEstado(Estado.PERSIGUIENDO);
					this.removerEstado(Estado.ESTANDAR);
					this.enAccion = false; // Cancela patrulla pasiva
					this.recorridoA.clear();
				}
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}

		// 2. Transición de decisiones: Combate/Persecución vs Patrulla Pasiva
		if (Globales.TECLADO.TECLA_DIJKSTRA.presionado()
				&& (this.tieneEstado(Estado.PERSIGUIENDO) || this.tieneEstado(Estado.ATACANDO))) {
			this.actualizarAtaque();
		} else {
			this.tomarAccion();
		}

		// Curación/Daño en modo prueba mediante click derecho
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())
				&& Globales.RATON.presionadoClickDerUnicaAct()) {
			this.curar(Globales.JUGADOR.getDamage());
		}

		this.atrasDeComplemento = (this.mundo != null)
				&& this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
	}

	/**
	 * Gestiona las fases de ataque cuerpo a cuerpo, persecución por Dijkstra y
	 * abandono del combate según la máquina de estados.
	 */
	protected void actualizarAtaque() {
		// --- FASE 1: Ejecución y recuperación del golpe cargado ---
		if (this.realizandoAtaque) {
			if (this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
				this.enAccion = false;

				final Rectangle rangoMele = this.rangoAtaqueMele;
				this.rangoAtaqueMele = null;

				if ((rangoMele != null) && (this.mundo != null)) {
					this.mundo.crearProyectil(new GolpeMeleContraJugador(this.ataque, false, this.mundo, rangoMele.x,
							rangoMele.y, rangoMele.width, rangoMele.height, this.direccion, this));
				}

				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
			}
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		// --- FASE 2: Evaluación de rangos y movimiento ---
		final boolean jugadorEnRangoVision = this.getAreaDeteccionLogica()
				.intersects(Globales.JUGADOR.getRectangulo());
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (jugadorEnRangoVision || dentroTiempoBusqueda) {
			// Comprobar si el jugador está dentro de algún rango Melee de ataque
			this.rangoAtaqueMele = this.obtenerRangoAtaqueMeleValido();

			if (this.rangoAtaqueMele != null) {
				// En rango Melee -> Iniciar secuencia de ataque
				this.meterEstado(Estado.ATACANDO);

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
				// Fuera de rango Melee -> Mover hacia el jugador con Dijkstra
				this.removerEstado(Estado.ATACANDO);
				this.meterEstado(Estado.PERSIGUIENDO);
				this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
			}
		} else {
			// El jugador escapó y expiró el tiempo de búsqueda -> Retornar a estado pasivo
			this.desactivarModoAgresivo();
		}
	}

	/**
	 * Desplaza la criatura de forma fluida mediante vectores hacia el nodo más
	 * cercano provisto por Dijkstra.
	 */
	protected NodoD moverEnAtaque(final DijkstraRework d, final Terreno terreno) {
		if (d == null) {
			return null;
		}

		if (!this.pendienteADijkstra) {
			this.pendienteADijkstra = true;
			d.aumentarEntidadesPendientes();
		}

		// 1. Centro actual de la criatura
		final double centroX = this.x + (this.ANCHO / 2.0);
		final double centroY = this.y + (this.ALTO / 2.0);

		// 2. Consultar el nodo Dijkstra correspondiente al centro
		final NodoD n = d.getNodoCercano((int) centroX, (int) centroY);

		if (this.ant != n) {
			this.ant = n;
		}
		if (n == null) {
			return null;
		}

		// 3. Punto objetivo al CENTRO del nodo destino
		final double targetX = n.getXMundo() + (n.getAncho() / 2.0);
		final double targetY = n.getYMundo() + (n.getAlto() / 2.0);

		// 4. Vector de desplazamiento continuo
		final double diffX = targetX - centroX;
		final double diffY = targetY - centroY;
		final double distanciaMundo = Math.hypot(diffX, diffY);

		// 5. Movimiento continuo sin zonas muertas de frenado
		if (distanciaMundo > 0) {
			final double paso = Math.min(this.velocidad, distanciaMundo);
			final double dirX = (diffX / distanciaMundo) * paso;
			final double dirY = (diffY / distanciaMundo) * paso;

			// Desplazamiento en X e Y solo si existe variación real
			if (Math.abs(dirX) > 0.001) {
				this.modificarPosicionX(dirX);
			}
			if (Math.abs(dirY) > 0.001) {
				this.modificarPosicionY(dirY);
			}

			// 6. Asignar la dirección AL FINAL para que 'modificarPosicionY' no la
			// sobreescriba
			if (Math.abs(diffX) > Math.abs(diffY)) {
				this.direccion = (diffX > 0) ? Direccion.ESTE : Direccion.OESTE;
			} else if (Math.abs(diffY) > 0.01) {
				this.direccion = (diffY > 0) ? Direccion.SUR : Direccion.NORTE;
			}

			this.setEstadoCaminando();
		}

		return n;
	}

	/**
	 * Limpia los estados de agresividad y libera la referencia pendiente en
	 * Dijkstra.
	 */
	protected void desactivarModoAgresivo() {
		this.removerEstado(Estado.ATACANDO);
		this.removerEstado(Estado.PERSIGUIENDO);
		this.setEstadoEstandar();

		if (this.pendienteADijkstra && (this.mundo != null) && (this.mundo.getDijkstra() != null)) {
			this.pendienteADijkstra = false;
			this.mundo.getDijkstra().reducirEntidadesPendientes();
		}
	}

	/**
	 * Evalúa los 4 rangos de ataque Melee y retorna el primero que colisione con el
	 * jugador.
	 */
	protected Rectangle obtenerRangoAtaqueMeleValido() {
		for (final Rectangle r : this.rangosAtaqueMele()) {
			if ((r != null) && r.intersects(Globales.JUGADOR.getArea())) {
				return r;
			}
		}
		return null;
	}

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

	/**
	 * Selecciona un destino aleatorio para la patrulla utilizando A*. Reutiliza el
	 * mismo Rectangle para evitar asignaciones continuas en la memoria Heap.
	 */
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

		if ((this.nodoADestino != null) && this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(),
				this.getPosicionYInt(), this.getMundo().getAEstrellaX12X20().getDimensionNodoA())) {
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
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.x - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0)),
							(int) ((this.y - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0)),
							(int) this.areaDeteccionAncho, (int) this.areaDeteccionAlto),
					Color.RED);
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.x - (this.areaDeteccionAncho / 8.0)) + (this.ANCHO / 2.0)),
							(int) ((this.y - (this.areaDeteccionAlto / 8.0)) + (this.ALTO / 2.0)),
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
		return new Ellipse2D.Double((this.x - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0),
				(this.y - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
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
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR.setBounds((int) this.getXRangoAtaqueMele(),
				(int) (this.getYRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()),
				(int) this.getGrosorRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR;
	}

	protected Rectangle rangoAtaqueMeleSur() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR.setBounds((int) this.getXRangoAtaqueMele(),
				(int) this.getYRangoAtaqueMele(), (int) this.getGrosorRangoAtaqueMele(),
				(int) this.getAlcanceRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR;
	}

	protected Rectangle rangoAtaqueMeleEste() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR.setBounds((int) this.getXRangoAtaqueMele(),
				(int) this.getYRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele(),
				(int) this.getGrosorRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR;
	}

	protected Rectangle rangoAtaqueMeleOeste() {
		this.AREA_RANGO_ATAQUE_MELE_AUXILIAR.setBounds(
				(int) (this.getXRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()), (int) this.getYRangoAtaqueMele(),
				(int) this.getAlcanceRangoAtaqueMele(), (int) this.getGrosorRangoAtaqueMele());
		return this.AREA_RANGO_ATAQUE_MELE_AUXILIAR;
	}

	protected abstract double getXRangoAtaqueMele();

	protected abstract double getYRangoAtaqueMele();

	protected abstract double getAlcanceRangoAtaqueMele();

	protected abstract double getGrosorRangoAtaqueMele();

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		this.reducirVida(damage);
		if (causante instanceof Jugador) {
			this.GT_ATACADO.establecerReferenciaTiempoActual();
			this.meterEstado(Estado.PERSIGUIENDO);
			this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
		}
		super.recibirAtaque(damage, causante);
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
		this.eliminado = true;
	}
}