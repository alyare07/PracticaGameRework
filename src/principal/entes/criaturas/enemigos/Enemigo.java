package principal.entes.criaturas.enemigos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
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
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta para todos los enemigos del juego. Implementa la IA agresiva y
 * pasiva (patrulla aleatoria y persecución por Dijkstra/A*).
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
	protected boolean atacando;
	protected double ataque = 25;
	protected boolean realizandoAtaque;
	protected Rectangle rangoAtaqueMele;

	protected static final int ACCION_ESPERAR = 1;
	protected static final int ACCION_MOVER = 2;
	protected boolean enAccion;
	protected int accion;
	protected int tiempoAccionEsperaMs;

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
		this.meterEstado(Estado.ESTANDAR);
		this.mundo = mundo;

		this.destinoX = (int) x;
		this.destinoY = (int) y;
	}

	@Override
	public void actualizar() {
		this.curar();

		if (Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
			this.actualizarAtaque();
			if (!this.estaEstadoAtacando()) {
				this.tomarAccion();
			} else if (this.enAccion) {
				this.enAccion = false;
				this.recorridoA = null;
				this.nodoADestino = null;
			}
		}

		if (Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())
				&& Constantes.RATON.presionadoClickDerUnicaAct()) {
			this.curar(Constantes.JUGADOR.getDamage());
		}

		this.atrasDeComplemento = (this.mundo != null)
				&& this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
	}

	protected void actualizarAtaque() {
		if (this.realizandoAtaque
				&& this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			this.enAccion = false;

			final Rectangle rangoMele = this.rangoAtaqueMele;
			this.rangoAtaqueMele = null;

			if ((rangoMele != null) && (this.mundo != null)) {
				this.mundo.crearProyectil(new GolpeMeleContraJugador(this.ataque, false, this.mundo, rangoMele.x,
						rangoMele.y, rangoMele.width, rangoMele.height, this.direccion, this));
			}

			this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
			this.realizandoAtaque = false;
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		if (this.atacando) {
			this.meterEstado(Estado.ATACANDO);

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				if (this.rangoAtaqueMele != null) {
					if (this.GT_ATAQUE_INICIAL_COOLDOWN
							.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
						if (!this.realizandoAtaque) {
							this.realizandoAtaque = true;
							final Direccion dAtaque = this.getDireccionAtaqueMele();
							if (dAtaque != null) {
								this.direccion = dAtaque;
							} else {
								this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
							}
						}
					}
					return;
				}
				this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			} else if (!this.GE_FUERA_DE_RANGO.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango())) {
				this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
			} else {
				this.atacando = false;
				this.pendienteADijkstra = false;
				this.mundo.getDijkstra().reducirEntidadesPendientes();
			}
		} else {
			if (!this.estaEstadoEstandar()) {
				this.setEstadoEstandar();
			}

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				this.atacando = true;
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}
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

		// Selección aleatoria limpia sin redefinir semillas constantemente
		this.accion = ALEATORIO.nextBoolean() ? ACCION_ESPERAR : ACCION_MOVER;
		this.enAccion = true;

		if (this.accion == ACCION_ESPERAR) {
			this.recorridoA = null;
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
		if (!this.estaEstadoEstandar()) {
			this.setEstadoEstandar();
		}
	}

	protected void generarTiempoDeEspera() {
		final int minMs = 1500;
		final int maxMs = 10000;
		this.tiempoAccionEsperaMs = ALEATORIO.nextInt((maxMs - minMs) + 1) + minMs;
		this.GT_ESPERA.establecerReferenciaTiempoActual();
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
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

	protected void cambiarDestinoAlAzar() {
		if (this.mundo == null) {
			return;
		}

		boolean destinoFactible = false;
		final int desplazamiento = this.mundo.getTerreno().ladoTile() * 3;

		final int minX = this.getPosicionXInt() - desplazamiento;
		final int maxX = this.getPosicionXInt() + desplazamiento;
		final int minY = this.getPosicionYInt() - desplazamiento;
		final int maxY = this.getPosicionYInt() + desplazamiento;

		int intentos = 0;

		while (!destinoFactible && (intentos < 20)) {
			intentos++;

			this.destinoX = ALEATORIO.nextInt((maxX - minX) + 1) + minX;
			this.destinoY = ALEATORIO.nextInt((maxY - minY) + 1) + minY;

			final NodoA nodoDestino = this.aEstrella.getNodoRef(this.destinoX, this.destinoY);

			if ((nodoDestino != null) && !this.mundo.colisionaConZonaUObjetoSolido(nodoDestino.getAreaEnMundo())) {
				this.recorridoA = this.aEstrella.getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(),
						this.destinoX, this.destinoY);
				if ((this.recorridoA != null) && !this.recorridoA.isEmpty()) {
					destinoFactible = true;
				}
			}
		}

		if (destinoFactible && this.recorridoA.hasNext()) {
			this.nodoADestino = this.recorridoA.getNext();
		}
	}

	protected void moverLugarRandom() {
		if ((this.recorridoA == null) || this.recorridoA.isEmpty()) {
			this.enAccion = false;
			return;
		}

		if ((this.nodoADestino != null)
				&& this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())) {
			if (this.recorridoA.hasNext()) {
				this.nodoADestino = this.recorridoA.getNext();
			}
		}

		final NodoA ultimoNodo = this.recorridoA.getLast();
		final boolean llegoAlFinal = (this.nodoADestino == ultimoNodo)
				&& (this.getPosicionXInt() == ultimoNodo.getAreaEnMundo().x)
				&& (this.getPosicionYInt() == ultimoNodo.getAreaEnMundo().y);

		if (llegoAlFinal) {
			this.enAccion = false;
		} else {
			this.moverANodoADestino();
			if (!this.estaEstadoCaminando()) {
				this.setEstadoCaminando();
			}
		}
	}

	protected NodoD moverEnAtaque(final DijkstraRework d, final Terreno terreno) {
		if ((this.ant != null) && (this.ant.distancia == 0)) {
			for (final Rectangle r : this.rangosAtaqueMele()) {
				if (r.intersects(Constantes.JUGADOR.getArea())) {
					this.rangoAtaqueMele = r;
					return null;
				}
			}
		}

		this.rangoAtaqueMele = null;

		if (!this.pendienteADijkstra) {
			this.pendienteADijkstra = true;
			d.aumentarEntidadesPendientes();
		}

		final NodoD n = d.getNodoCercano((int) this.x, (int) this.y);
		if (this.ant != n) {
			this.ant = n;
		}
		if (n == null) {
			return null;
		}

		final Point posNodo = new Point(n.AREA.x, n.AREA.y);

		if (this.y < posNodo.y) {
			this.modificarPosicionY(this.velocidad);
			if ((posNodo.y - this.y) <= 0.25) {
				this.y = posNodo.y;
			}
		} else if (this.y > posNodo.y) {
			this.modificarPosicionY(-this.velocidad);
			if ((this.y - posNodo.y) <= 0.25) {
				this.y = posNodo.y;
			}
		}

		if (this.x < posNodo.x) {
			this.modificarPosicionX(this.velocidad);
			if ((posNodo.x - this.x) <= 0.25) {
				this.x = posNodo.x;
			}
		} else if (this.x > posNodo.x) {
			this.modificarPosicionX(-this.velocidad);
			if ((this.x - posNodo.x) <= 0.25) {
				this.x = posNodo.x;
			}
		}

		return n;
	}

	public Ellipse2D getAreaDeteccionLogica() {
		return new Ellipse2D.Double((this.x - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0),
				(this.y - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
	}

	protected Rectangle[] rangosAtaqueMele() {
		return new Rectangle[] { this.rangoAtaqueMeleOeste(), this.rangoAtaqueMeleEste(), this.rangoAtaqueMeleNorte(),
				this.rangoAtaqueMeleSur() };
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
		return new Rectangle((int) this.getXRangoAtaqueMele(),
				(int) (this.getYRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()),
				(int) this.getGrosorRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele());
	}

	protected Rectangle rangoAtaqueMeleSur() {
		return new Rectangle((int) this.getXRangoAtaqueMele(), (int) this.getYRangoAtaqueMele(),
				(int) this.getGrosorRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele());
	}

	protected Rectangle rangoAtaqueMeleEste() {
		return new Rectangle((int) this.getXRangoAtaqueMele(), (int) this.getYRangoAtaqueMele(),
				(int) this.getAlcanceRangoAtaqueMele(), (int) this.getGrosorRangoAtaqueMele());
	}

	protected Rectangle rangoAtaqueMeleOeste() {
		return new Rectangle((int) (this.getXRangoAtaqueMele() - this.getAlcanceRangoAtaqueMele()),
				(int) this.getYRangoAtaqueMele(), (int) this.getAlcanceRangoAtaqueMele(),
				(int) this.getGrosorRangoAtaqueMele());
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
			this.atacando = true;
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
		this.eliminado = true;
	}
}