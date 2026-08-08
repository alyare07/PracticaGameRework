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
import principal.utilidades.Sonidos;

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
	// COD PRUEBA 23

	protected static final int ACCION_ESPERAR = 1;
	protected static final int ACCION_MOVER = 2;
	protected boolean enAccion;
	protected int accion;
	protected int tiempoAccionEsperaMs;

	// FIN COD PRUEBA 23

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
		// COD PRUEBA 23
		this.destinoX = (int) x;
		this.destinoY = (int) y;
		// FIN COD PRUEBA 23
	}

	@Override
	public void actualizar() {
		this.curar();
		if (Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
			this.actualizarAtaque();
			if (!this.ESTADO.containsKey(Estado.ATACANDO)) {
				this.tomarAccion();
			} else if (this.enAccion) {
				this.enAccion = false;
				this.recorridoA = null;
				this.nodoADestino = null;
			}
		}
		if (Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if (Constantes.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Constantes.JUGADOR.getDamage());
			}
		}
		if (this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea())) {
			if (!this.atrasDeComplemento) {
				this.atrasDeComplemento = true;
			}
		} else if (this.atrasDeComplemento) {
			this.atrasDeComplemento = false;
		}

	}

	protected void actualizarAtaque() {
		if (this.realizandoAtaque
				&& this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			if (this.enAccion) {
				this.enAccion = false;
			}
			// Ataque del enemigo al jugador
			final Rectangle rangoMele = this.rangoAtaqueMele;
			this.rangoAtaqueMele = null;
			this.mundo.crearProyectil(new GolpeMeleContraJugador(this.ataque, false, this.mundo, rangoMele.x,
					rangoMele.y, rangoMele.width, rangoMele.height, this.direccion, this));
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

						// realiza la carga del ataque
						if (!this.realizandoAtaque) {
							this.realizandoAtaque = true;
							final Direccion dAtaque = this.getDireccionAtaqueMele();
							if (dAtaque == Direccion.NORTE) {
								this.direccion = Direccion.NORTE;
//				this.actualPerfil = this.hoja.getSprite(5);
							} else if (dAtaque == Direccion.SUR) {
								this.direccion = Direccion.SUR;
//				this.actualPerfil = this.hoja.getSprite(3);
							} else if (dAtaque == Direccion.ESTE) {
								this.direccion = Direccion.ESTE;
//				this.actualPerfil = this.hoja.getSprite(1);
							} else if (dAtaque == Direccion.OESTE) {
								this.direccion = Direccion.OESTE;
//				this.actualPerfil = this.hoja.getSpriteInvertidoHorizontal(1);
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
			if (!this.ESTADO.containsKey(Estado.ESTANDAR)) {
//		this.actualPerfil = this.hoja.getSprite(0);
				this.setEstadoEstandar();
			}

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				this.atacando = true;
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}
	}

	// COD PRUEBA 23

	protected void tomarAccion() {
		if (this.enAccion) {
			switch (this.accion) {
			case ACCION_ESPERAR:
				this.esperar();
				break;
			case ACCION_MOVER:
				this.moverLugarRandom();
				break;
			}
			return;
		}

		this.accion = ALEATORIO.nextInt((2 - 1) + 1) + 1;
		ALEATORIO.setSeed(System.currentTimeMillis());
		this.enAccion = true;
		this.accion = ACCION_MOVER;
		if (this.enAccion) {
			switch (this.accion) {
			case ACCION_ESPERAR:
				this.recorridoA = null;
				this.generarTiempoDeEspera();
				this.esperar();
				break;
			case ACCION_MOVER:
				this.cambiarDestinoAlAzar();
				this.moverLugarRandom();
				break;
			}
		}

	}

	protected void esperar() {
		if (this.GT_ESPERA.transcurrioMiliSegundos(this.tiempoAccionEsperaMs)) {
			this.enAccion = false;
		}
		if (!this.ESTADO.containsKey(Estado.ESTANDAR)) {
//	    this.actualPerfil = this.hoja.getSprite(0);
			this.setEstadoEstandar();
		}
	}

	protected void generarTiempoDeEspera() {
		final int minMs = 1500;
		final int maxMs = 10000;
		this.tiempoAccionEsperaMs = ALEATORIO.nextInt((maxMs - minMs) + 1) + minMs;
		this.GT_ESPERA.establecerReferenciaTiempoActual();
	}

	// FIN COD PRUEBA 23

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.x - (this.areaDeteccionAncho / 2)) + (this.ANCHO / 2)),
							(int) ((this.y - (this.areaDeteccionAlto / 2)) + (this.ALTO / 2)),
							(int) this.areaDeteccionAncho, (int) this.areaDeteccionAlto),
					Color.red);
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle((int) ((this.x - (this.areaDeteccionAncho / 8)) + (this.ANCHO / 2)),
							(int) ((this.y - (this.areaDeteccionAlto / 8)) + (this.ALTO / 2)),
							(int) (this.areaDeteccionAncho / 4), (int) (this.areaDeteccionAlto / 4)),
					Color.orange);
		}

	}

	public boolean recibiendoAtaque() {
		return !this.GT_ATACADO.transcurrioMiliSegundos(this.getTiempoMsEsperaAtacado());
	}

	protected void curar() {
		if (this.vida >= this.vidaMaxima) {
			return;
		}
		if ((!this.recibiendoAtaque())
				&& (this.GT_CURACION.transcurrioMiliSegundos(this.getTiempoMsEsperaRegenVida()))) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	protected void cambiarDestinoAlAzar() {
		boolean destinoFactible = false;
		final int desplazamiento = this.mundo.getTerreno().ladoTile() * 3;

		final int minX = this.getPosicionXInt() - desplazamiento;
		final int maxX = this.getPosicionXInt() + desplazamiento;
		final int minY = this.getPosicionYInt() - desplazamiento;
		final int maxY = this.getPosicionYInt() + desplazamiento;

		NodoA nodoDestino = null;
		int intentos = 0; // Prevenir un bucle infinito si está encerrado

		while (!destinoFactible && (intentos < 20)) {
			intentos++;

			// Generar coordenadas aleatorias sin reiniciar la semilla (Seed)
			this.destinoX = ALEATORIO.nextInt((maxX - minX) + 1) + minX;
			this.destinoY = ALEATORIO.nextInt((maxY - minY) + 1) + minY;

			nodoDestino = this.aEstrella.getNodoRef(this.destinoX, this.destinoY);

			if ((nodoDestino != null) && !this.mundo.colisionaConZonaUObjetoSolido(nodoDestino.getAreaEnMundo())) {
				this.recorridoA = this.aEstrella.getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(),
						this.destinoX, this.destinoY);

				if (!this.recorridoA.isEmpty()) {
					destinoFactible = true;
				}
			}
		}

		if (destinoFactible && this.recorridoA.hasNext()) {
			this.nodoADestino = this.recorridoA.getNext();
		}
	}

	protected void moverLugarRandom() {
		// 1. Control de seguridad: Si no hay recorrido o la lista está vacía, cancelar
		// acción
		if ((this.recorridoA == null) || this.recorridoA.isEmpty()) {
			this.enAccion = false;
			return;
		}

		// 2. Comprobar si la criatura ya alcanzó el nodo objetivo actual
		if ((this.nodoADestino != null)
				&& this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())) {
			if (this.recorridoA.hasNext()) {
				this.nodoADestino = this.recorridoA.getNext();
			}
		}

		// 3. Verificar si ya llegó al FINAL del recorrido completo
		final NodoA ultimoNodo = this.recorridoA.getLast();
		final boolean llegoAlFinal = (this.nodoADestino == ultimoNodo)
				&& (this.getPosicionXInt() == ultimoNodo.getAreaEnMundo().x)
				&& (this.getPosicionYInt() == ultimoNodo.getAreaEnMundo().y);

		if (llegoAlFinal) {
			this.enAccion = false;
		} else {
			this.moverANodoADestino();
			if (!this.ESTADO.containsKey(Estado.CAMINANDO)) {
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
		if (this.rangoAtaqueMele != null) {
			this.rangoAtaqueMele = null;
		}

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
//			y += velocidad;
			this.modificarPosicionY(this.velocidad);
			if ((posNodo.y - this.y) <= 0.25) {
				this.y = posNodo.y;
			}
		} else if (this.y > posNodo.y) {
//				y -= velocidad;
			this.modificarPosicionY(-this.velocidad);
			if ((this.y - posNodo.y) <= 0.25) {
				this.y = posNodo.y;
			}
		}

		if (this.x < posNodo.x) {
//		x += velocidad;
			this.modificarPosicionX(this.velocidad);
			if ((posNodo.x - this.x) <= 0.25) {
				this.x = posNodo.x;
			}
		} else if (this.x > posNodo.x) {
//			x -= velocidad;
			this.modificarPosicionX(-this.velocidad);
			if ((this.x - posNodo.x) <= 0.25) {
				this.x = posNodo.x;
			}
		}
		return n;
	}

	public Ellipse2D getAreaDeteccionLogica() {
		return new Ellipse2D.Double((this.x - (this.areaDeteccionAncho / 2)) + (this.ANCHO / 2),
				(this.y - (this.areaDeteccionAlto / 2)) + (this.ALTO / 2), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
	}

	protected Rectangle[] rangosAtaqueMele() {
		final Rectangle[] rangos = { this.rangoAtaqueMeleOeste(), this.rangoAtaqueMeleEste(),
				this.rangoAtaqueMeleNorte(), this.rangoAtaqueMeleSur() };
		return rangos;
	}

	protected Direccion getDireccionAtaqueMele() {
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
		final double xRango = this.getXRangoAtaqueMele();
		final double yRango = this.getYRangoAtaqueMele();
		final double alcanceRango = this.getAlcanceRangoAtaqueMele();
		final double anchoRango = this.getGrosorRangoAtaqueMele();

		return new Rectangle((int) xRango, (int) (yRango - alcanceRango), (int) anchoRango, (int) alcanceRango);
	}

	protected Rectangle rangoAtaqueMeleSur() {
		final double xRango = this.getXRangoAtaqueMele();
		final double yRango = this.getYRangoAtaqueMele();
		final double alcanceRango = this.getAlcanceRangoAtaqueMele();
		final double anchoRango = this.getGrosorRangoAtaqueMele();

		return new Rectangle((int) xRango, (int) yRango, (int) anchoRango, (int) alcanceRango);
	}

	protected Rectangle rangoAtaqueMeleEste() {
		final double xRango = this.getXRangoAtaqueMele();
		final double yRango = this.getYRangoAtaqueMele();
		final double alcanceRango = this.getAlcanceRangoAtaqueMele();
		final double anchoRango = this.getGrosorRangoAtaqueMele();

		return new Rectangle((int) xRango, (int) yRango, (int) alcanceRango, (int) anchoRango);
	}

	protected Rectangle rangoAtaqueMeleOeste() {
		final double xRango = this.getXRangoAtaqueMele();
		final double yRango = this.getYRangoAtaqueMele();
		final double alcanceRango = this.getAlcanceRangoAtaqueMele();
		final double anchoRango = this.getGrosorRangoAtaqueMele();
		return new Rectangle((int) (xRango - alcanceRango), (int) yRango, (int) alcanceRango, (int) anchoRango);
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
			// prueba de atacar cuando es atacado
			this.atacando = true;
			this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
		}
		super.recibirAtaque(damage, causante);
	}

//    public static Criatura crearDesdeJSON(final JSONObject json, final Mundo mundo) {
//	Criatura criatura = null;
//	try {
//	    final double x = Double.parseDouble(json.get("x").toString());
//	    final double y = Double.parseDouble(json.get("y").toString());
//	    final int ancho = Integer.parseInt(json.get("w").toString());
//	    final int alto = Integer.parseInt(json.get("h").toString());
//	    final double vida = Double.parseDouble(json.get("hp").toString());
//	    final double vidaMax = Double.parseDouble(json.get("maxhp").toString());
//
//	    criatura = new Enemigo(x, y, ancho, alto, vidaMax, vidaMax, mundo);
//	    criatura.establecerVida(vida);
//	} catch (final Exception e) {
//	}
//	return criatura;
//    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public JSONObject exportarParaJSON() {
//	final JSONObject json = new JSONObject();
//	json.put("x", this.getPosicionX());
//	json.put("y", this.getPosicionY());
//	json.put("w", this.ANCHO);
//	json.put("h", this.ALTO);
//	json.put("hp", this.vida);
//	json.put("maxhp", this.vidaMaxima);
//	return json;
//    }

//    @Override
//    public String exportarTipoCriatura() {
//	return "Enemigo";
//    }

	protected abstract int getTiempoMsEsperaRegenVida();

	protected abstract int getTiempoMsEsperaAtacado();

	protected abstract int getTiempoMsBusquedaFueraRango();

	/**
	 * Tiempo de espera desde que quiere atacar hasta que lanza el ataque
	 * 
	 * @return El tiempo en ms
	 */
	protected abstract int getTiempoMsEsperaAtaqueInicial();

	/**
	 * Tiempo de espera desde que termino el ataque anterio hasta que realice otro.
	 * 
	 * @return El tiempo en ms
	 */
	protected abstract int getTiempoMsEsperaRetomarAtaque();

	@Override
	public void eliminar() {
		Sonidos.SONIDO_DEAD_CRIATURE.reproducir();
		this.eliminado = true;
	}

}