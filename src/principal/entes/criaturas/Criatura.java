package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.facciones.GestorFacciones;
import principal.ia.aEstrella.NodoA;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Base abstracta para todas las criaturas con soporte de Atributos RPG (Zero-GC
 * / O(1)).
 * 
 * @version 4.0 (Vanilla Java 8)
 */
public abstract class Criatura extends Ente {

	public enum Direccion {
		NORTE("N"), SUR("S"), ESTE("E"), OESTE("W");

		private final String DESCRIPCION;

		private Direccion(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}

	public enum Estado {
		ESTANDAR("Estandar"), CAMINANDO("Caminando"), CORRIENDO("Corriendo"), ATACANDO("Atacando"),
		ARROJANDO("Arrojando"), PERSIGUIENDO("Persiguiendo");

		private final String DESCRIPCION;

		private Estado(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}

	private static final Color COLOR_FONDO_BARRA = Color.BLACK;
	private static final Color COLOR_BARRA_LAG = new Color(255, 205, 40);
	private static final Color COLOR_BARRA_VIDA = new Color(235, 30, 30);

	// =========================================================================
	// === 1. ATRIBUTOS RPG FUNDAMENTALES (ZERO-GC)
	// =========================================================================
	protected int fuerzaBase = 10;
	protected int agilidadBase = 10;
	protected int inteligenciaBase = 10;

	// =========================================================================
	// === 2. FACCIONES Y RELACIONES DIPLOMÁTICAS
	// =========================================================================
	protected int faccionBit = GestorFacciones.FACCION_NEUTRAL;
	protected int mascaraHostilidad = 0;

	// =========================================================================
	// === 3. CINEMÁTICA Y WAYPOINTS
	// =========================================================================
	protected double velActualX = 0.0;
	protected double velActualY = 0.0;
	protected double agilidadGiro = 0.25;
	protected static final double RADIO_ANTICIPACION_ESQUINA = 12.0;
	protected static final double RADIO_LLEGADA_WAYPOINT = 4.0;

	private final Set<Estado> estados = EnumSet.noneOf(Estado.class);
	protected final int ANCHO;
	protected final int ALTO;
	protected double velocidad = 1.0;
	private double x;
	private double y;
	protected boolean modoDios = false;

	// =========================================================================
	// === 4. VIDA, HIT-FLASH Y TIEMPOS
	// =========================================================================
	protected double vida;
	protected double vidaLag;
	protected double vidaMaxima;
	protected double velocidadEstandar = 0.5;

	protected final GestorTiempo GT_ESPERA;
	protected final GestorTiempo GT_ATACADO;
	protected final GestorTiempo GT_CURACION;

	protected static final int TIEMPO_MS_FLASH_DANIO = 65;
	protected final GestorTiempo GT_FLASH_DANIO;

	protected double vidaRegen;
	protected Direccion direccion;
	protected boolean atrasDeComplemento;

	protected int margenXInicialSprite;
	protected int margenYInicialSprite;
	protected int margenXFinalSprite;
	protected int margenYFinalSprite;

	protected final ArrayDeque<NodoA> recorridoA;
	protected NodoA nodoADestino;
	protected int destinoX;
	protected int destinoY;

	protected static final Random ALEATORIO = new Random();

	public Criatura(final double x, final double y, final int ancho, final int alto) {
		this(x, y, ancho, alto, 100.0, 100.0, 0.5);
	}

	public Criatura(final double x, final double y, final int ancho, final int alto, final double vida,
			final double vidaMaxima) {
		this(x, y, ancho, alto, vida, vidaMaxima, 0.5);
	}

	public Criatura(final double x, final double y, final int ancho, final int alto, final double velocidad) {
		this(x, y, ancho, alto, 100.0, 100.0, velocidad);
	}

	private Criatura(final double x, final double y, final int ancho, final int alto, final double vida,
			final double vidaMaxima, final double velocidadEstandar) {
		this.establecerMargenesSprite();
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.x = x;
		this.y = y;
		this.velocidadEstandar = velocidadEstandar;
		this.establecerVelocidadStardar();

		this.vidaMaxima = vidaMaxima;
		this.vida = Math.min(vida, vidaMaxima);
		this.vidaLag = this.vida;

		this.GT_ESPERA = new GestorTiempo();
		this.GT_ATACADO = new GestorTiempo();
		this.GT_CURACION = new GestorTiempo();
		this.GT_FLASH_DANIO = new GestorTiempo();

		this.vidaRegen = 1.0;
		this.direccion = Direccion.ESTE;
		this.recorridoA = new ArrayDeque<NodoA>();

		this.faccionBit = GestorFacciones.FACCION_NEUTRAL;
		this.mascaraHostilidad = GestorFacciones.getMascaraHostilidadPorDefecto(this.faccionBit);
	}

	// =========================================================================
	// === MÉTODOS DE ATRIBUTOS POLIMÓRFICOS
	// =========================================================================

	public int getFuerzaTotal() {
		return this.fuerzaBase;
	}

	public int getAgilidadTotal() {
		return this.agilidadBase;
	}

	public int getInteligenciaTotal() {
		return this.inteligenciaBase;
	}

	public int getFuerzaBase() {
		return this.fuerzaBase;
	}

	public void setFuerzaBase(final int fuerza) {
		this.fuerzaBase = Math.max(1, fuerza);
	}

	public int getAgilidadBase() {
		return this.agilidadBase;
	}

	public void setAgilidadBase(final int agilidad) {
		this.agilidadBase = Math.max(1, agilidad);
	}

	public int getInteligenciaBase() {
		return this.inteligenciaBase;
	}

	public void setInteligenciaBase(final int inteligencia) {
		this.inteligenciaBase = Math.max(1, inteligencia);
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA Y REPULSIÓN DE MANADA
	// =========================================================================

	@Override
	public void actualizar() {
		this.verificarZoneBox();
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
		this.actualizarBarraFantasma(dt);
		this.aplicarFuerzaSeparacion();
	}

	public void actualizarBarraFantasma(final double dt) {
		if (this.vidaLag > this.vida) {
			final double diferencia = this.vidaLag - this.vida;
			final double velocidadDrenado = Math.max(this.vidaMaxima * 0.6, diferencia * 3.0);
			this.vidaLag -= velocidadDrenado * dt;

			if (this.vidaLag < this.vida) {
				this.vidaLag = this.vida;
			}
		} else {
			this.vidaLag = this.vida;
		}
	}

	protected void aplicarFuerzaSeparacion() {
		if (this.zonasOcupadas.isEmpty()) {
			return;
		}

		final double miCentroX = this.x + (this.ANCHO / 2.0);
		final double miCentroY = this.y + (this.ALTO / 2.0);
		final double miRadio = (this.ANCHO + this.ALTO) / 4.0;

		final int cantZonas = this.zonasOcupadas.size();
		for (int z = 0; z < cantZonas; z++) {
			final ZoneBox zb = this.zonasOcupadas.get(z);
			final ArrayList<Criatura> lista = zb.getCriaturas();
			final int totalCriat = lista.size();

			for (int i = 0; i < totalCriat; i++) {
				final Criatura otra = lista.get(i);
				if ((otra == this) || otra.estaEliminado()) {
					continue;
				}

				final double otroCentroX = otra.x + (otra.ANCHO / 2.0);
				final double otroCentroY = otra.y + (otra.ALTO / 2.0);
				final double otroRadio = (otra.ANCHO + otra.ALTO) / 4.0;

				final double dx = miCentroX - otroCentroX;
				final double dy = miCentroY - otroCentroY;
				final double distSq = (dx * dx) + (dy * dy);
				final double radioMin = miRadio + otroRadio;

				if ((distSq < (radioMin * radioMin)) && (distSq > 0.0001)) {
					final double dist = Math.sqrt(distSq);
					final double penetracion = radioMin - dist;
					final double factorEmpuje = (penetracion / radioMin) * 0.25;

					final double empujeX = (dx / dist) * factorEmpuje;
					final double empujeY = (dy / dist) * factorEmpuje;

					this.modificarPosicionX(empujeX);
					this.modificarPosicionY(empujeY);
				}
			}
		}
	}

	// =========================================================================
	// === MÉTODOS DE FACCIONES
	// =========================================================================

	public int getFaccionBit() {
		return this.faccionBit;
	}

	public void setFaccion(final int faccionBit) {
		this.faccionBit = faccionBit;
		this.mascaraHostilidad = GestorFacciones.getMascaraHostilidadPorDefecto(faccionBit);
	}

	public int getMascaraHostilidad() {
		return this.mascaraHostilidad;
	}

	public void setMascaraHostilidad(final int mascaraHostilidad) {
		this.mascaraHostilidad = mascaraHostilidad;
	}

	public boolean esHostilHacia(final Criatura otra) {
		if ((otra == null) || otra.estaEliminado() || (otra == this)) {
			return false;
		}
		return GestorFacciones.esHostil(otra.getFaccionBit(), this.mascaraHostilidad);
	}

	// =========================================================================
	// === HIT-FLASH Y COMBATE
	// =========================================================================

	public boolean estaEnFlashDanio() {
		return !this.GT_FLASH_DANIO.transcurrioMiliSegundos(TIEMPO_MS_FLASH_DANIO);
	}

	public void activarFlashDanio() {
		this.GT_FLASH_DANIO.establecerReferenciaTiempoActual();
	}

	public void recibirAtaque(final double damage, final Ente causante) {
		if (this.modoDios) {
			return;
		}
		this.reducirVida(damage);
		this.activarFlashDanio();

		if (this.mundo != null) {
			final double dirX = (causante != null) ? Math.signum(this.x - causante.getPosicionX()) : 0.0;
			final double dirY = (causante != null) ? Math.signum(this.y - causante.getPosicionY()) : 0.0;

			Globales.GESTOR_PARTICULAS.emitirSangre(this.getCentroX(), this.getCentroY(), dirX, dirY, 15);
			Globales.GESTOR_TEXTOS.agregarDanio((int) damage, this.getPosicionX(), this.getPosicionY(), false);
		}
	}

	public Rectangle getRectangulo() {
		return this.getArea();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarIndicadorVida(g);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.CYAN);
		}

		if ((Globales.CAMARA.getEntidadEnfocada() == this)
				&& (!this.recorridoA.isEmpty() || (this.nodoADestino != null))) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(7f));

			final Dimension dimNodo = this.getMundo().getAEstrellaX12X20().getDimensionNodoA();
			final int anchoTile = dimNodo.width;
			final int altoTile = dimNodo.height;

			int pos = 1;
			for (final NodoA n : this.recorridoA) {
				final int xMundo = n.getXNodo() * anchoTile;
				final int yMundo = n.getYNodo() * altoTile;
				final String txt = String.valueOf(pos);

				Render2D.dibujarRectanguloContornoRefCamara(g, new Rectangle(xMundo, yMundo, anchoTile, altoTile),
						Color.MAGENTA);

				final int xTexto = (xMundo + (anchoTile / 2))
						- (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2);
				final int yTexto = (yMundo + (altoTile / 2))
						+ (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2);

				Render2D.dibujarStringRefCamara(g, txt, xTexto, yTexto, Color.BLACK);
				pos++;
			}

			if (this.nodoADestino != null) {
				Render2D.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getXNodo() * anchoTile,
						this.nodoADestino.getYNodo() * altoTile, anchoTile, altoTile, Color.YELLOW);
			}
		}
	}

	protected void pintarIndicadorVida(final Graphics2D g) {
		if (this.estaEstadoPersiguiendo() || this.estaEstadoAtacando()) {
			this.pintarRectanguloBarraVida(g);
		} else if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getPosicionX(),
				this.getPosicionY(), this.ANCHO, this.ALTO)) {
			this.pintarRectanguloBarraVida(g);
			this.pintarValorVida(g);
		}
	}

	private void pintarValorVida(final Graphics2D g) {
		g.setFont(Globales.GESTOR_FUENTES.getFuente(4f));
		final String texto = (int) this.vida + "/" + (int) this.vidaMaxima;
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);

		final int xTexto = this.getPosicionXInt() + ((this.ANCHO - anchoTexto) / 2);
		Render2D.dibujarStringRefCamara(g, texto, xTexto, this.getPosicionYInt() - 6, Color.WHITE);
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Constantes.TAMANO_FUENTE));
	}

	private void pintarRectanguloBarraVida(final Graphics2D g) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		final int anchoRojo = (int) Math.round((this.vida * this.ANCHO) / this.vidaMaxima);
		final int anchoAmarillo = (int) Math.round((this.vidaLag * this.ANCHO) / this.vidaMaxima);

		Render2D.dibujarRectanguloRellenoRefCamara(g, posX - 1, posY - 5, this.ANCHO + 2, 4, COLOR_FONDO_BARRA);

		if (anchoAmarillo > 0) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, posX, posY - 4, anchoAmarillo, 2, COLOR_BARRA_LAG);
		}

		if (anchoRojo > 0) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, posX, posY - 4, anchoRojo, 2, COLOR_BARRA_VIDA);
		}
	}

	// =========================================================================
	// === NAVEGACIÓN Y WAYPOINTS A*
	// =========================================================================

	protected void moverANodoADestino() {
		if (this.nodoADestino == null) {
			this.nodoADestino = this.recorridoA.poll();
			if (this.nodoADestino == null) {
				this.velActualX = 0.0;
				this.velActualY = 0.0;
				return;
			}
		}

		final double centroX = this.x + (this.ANCHO / 2.0);
		final double centroY = this.y + (this.ALTO / 2.0);

		double targetX = this.nodoADestino.getXMundo() + (this.nodoADestino.getAncho() / 2.0);
		double targetY = this.nodoADestino.getYMundo() + (this.nodoADestino.getAlto() / 2.0);

		double diffX = targetX - centroX;
		double diffY = targetY - centroY;
		double distAlNodo = Math.hypot(diffX, diffY);

		NodoA siguienteNodo = this.recorridoA.peek();
		boolean avanzarNodo = (distAlNodo <= Math.max(RADIO_LLEGADA_WAYPOINT, this.velocidad));

		if (!avanzarNodo && (siguienteNodo != null)) {
			final double sigX = siguienteNodo.getXMundo() + (siguienteNodo.getAncho() / 2.0);
			final double sigY = siguienteNodo.getYMundo() + (siguienteNodo.getAlto() / 2.0);

			final double segX = sigX - targetX;
			final double segY = sigY - targetY;
			final double posRelX = centroX - targetX;
			final double posRelY = centroY - targetY;

			final double dot = (segX * posRelX) + (segY * posRelY);
			if (dot > 0) {
				avanzarNodo = true;
			}
		}

		if (avanzarNodo) {
			this.nodoADestino = this.recorridoA.poll();
			if (this.nodoADestino == null) {
				this.velActualX = 0.0;
				this.velActualY = 0.0;
				return;
			}

			targetX = this.nodoADestino.getXMundo() + (this.nodoADestino.getAncho() / 2.0);
			targetY = this.nodoADestino.getYMundo() + (this.nodoADestino.getAlto() / 2.0);
			diffX = targetX - centroX;
			diffY = targetY - centroY;
			distAlNodo = Math.hypot(diffX, diffY);
			siguienteNodo = this.recorridoA.peek();
		}

		if ((siguienteNodo != null) && (distAlNodo < RADIO_ANTICIPACION_ESQUINA)) {
			final double sigX = siguienteNodo.getXMundo() + (siguienteNodo.getAncho() / 2.0);
			final double sigY = siguienteNodo.getYMundo() + (siguienteNodo.getAlto() / 2.0);

			final double t = 1.0 - (distAlNodo / RADIO_ANTICIPACION_ESQUINA);
			targetX = targetX + ((sigX - targetX) * t);
			targetY = targetY + ((sigY - targetY) * t);

			diffX = targetX - centroX;
			diffY = targetY - centroY;
			distAlNodo = Math.hypot(diffX, diffY);
		}

		if (distAlNodo > 0.001) {
			final double paso = Math.min(this.velocidad, distAlNodo);
			final double dirDeseadaX = (diffX / distAlNodo) * paso;
			final double dirDeseadaY = (diffY / distAlNodo) * paso;

			this.velActualX += (dirDeseadaX - this.velActualX) * this.agilidadGiro;
			this.velActualY += (dirDeseadaY - this.velActualY) * this.agilidadGiro;

			if (Math.abs(this.velActualX) > 0.001) {
				this.modificarPosicionX(this.velActualX);
			}
			if (Math.abs(this.velActualY) > 0.001) {
				this.modificarPosicionY(this.velActualY);
			}

			if (Math.abs(this.velActualX) > Math.abs(this.velActualY)) {
				this.direccion = (this.velActualX > 0) ? Direccion.ESTE : Direccion.OESTE;
			} else if (Math.abs(this.velActualY) > 0.01) {
				this.direccion = (this.velActualY > 0) ? Direccion.SUR : Direccion.NORTE;
			}

			this.setEstadoCaminando();
		} else {
			this.velActualX = 0.0;
			this.velActualY = 0.0;
		}
	}

	protected void establecerVelocidadStardar() {
		this.velocidad = this.velocidadEstandar;
	}

	public double getVida() {
		return this.vida;
	}

	public double getVidaMaxima() {
		return this.vidaMaxima;
	}

	public boolean vidaCompleta() {
		return Double.compare(this.vida, this.vidaMaxima) == 0;
	}

	public void reducirVida(final double puntos) {
		if (this.modoDios) {
			return;
		}
		this.vida = Math.max(0, this.vida - puntos);
		if (this.vida <= 0) {
			this.eliminar();
		}
	}

	public void establecerVidaMaxima(final double puntos) {
		this.vidaMaxima = puntos;
		this.vida = puntos;
		this.vidaLag = puntos;
	}

	public void aumentarVidaMaxima(final double puntos) {
		this.vidaMaxima += puntos;
		this.vida += puntos;
		this.vidaLag += puntos;
	}

	public void reducirVidaMaxima(final double puntos) {
		this.vidaMaxima = Math.max(10, this.vidaMaxima - puntos);
		this.vida = Math.min(this.vida, this.vidaMaxima);
		this.vidaLag = Math.min(this.vidaLag, this.vidaMaxima);
	}

	public void curar(final double puntos) {
		this.vida = Math.min(this.vidaMaxima, this.vida + puntos);
		this.vidaLag = this.vida;
		Globales.GESTOR_TEXTOS.agregarCuracion((int) puntos, this.getPosicionX(), this.getPosicionY());
	}

	public void establecerVida(final double puntos) {
		if (puntos > this.vidaMaxima) {
			this.vida = this.vidaMaxima;
		} else if (puntos <= 0) {
			this.vida = 0;
			this.eliminar();
		} else {
			this.vida = puntos;
		}
		this.vidaLag = this.vida;
	}

	public void sanar() {
		this.vida = this.vidaMaxima;
		this.vidaLag = this.vidaMaxima;
	}

	public void setModoDios(final boolean modoDios) {
		this.modoDios = modoDios;
		if (this.modoDios) {
			this.sanar();
		}
	}

	public boolean isModoDios() {
		return this.modoDios;
	}

	public boolean conmutarModoDios() {
		this.setModoDios(!this.modoDios);
		return this.modoDios;
	}

	public void calcularRutaAEstrella(final int xObjetivo, final int yObjetivo) {
		if (this.mundo == null) {
			return;
		}
		this.mundo.getAEstrellaX12X20().getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(), xObjetivo,
				yObjetivo, this.recorridoA);
		this.nodoADestino = this.recorridoA.poll();
	}

	public String getStringEstados() {
		final StringBuilder sb = new StringBuilder();
		for (final Estado e : this.estados) {
			sb.append(e.toString()).append(" ");
		}
		return sb.toString();
	}

	public boolean tieneEstado(final Estado estado) {
		return this.estados.contains(estado);
	}

	public void meterEstado(final Estado estado) {
		this.estados.add(estado);
	}

	public void removerEstado(final Estado estado) {
		this.estados.remove(estado);
	}

	public void setEstadoUnico(final Estado estado) {
		this.estados.clear();
		this.estados.add(estado);
	}

	public void limpiarEstados() {
		this.estados.clear();
	}

	protected void setEstadoCaminando() {
		this.removerEstado(Estado.ESTANDAR);
		this.removerEstado(Estado.CORRIENDO);
		this.meterEstado(Estado.CAMINANDO);
	}

	protected void setEstadoCorriendo() {
		this.removerEstado(Estado.ESTANDAR);
		this.removerEstado(Estado.CAMINANDO);
		this.meterEstado(Estado.CORRIENDO);
	}

	protected void setEstadoEstandar() {
		this.setEstadoUnico(Estado.ESTANDAR);
	}

	public boolean estaEstadoCaminando() {
		return this.tieneEstado(Estado.CAMINANDO);
	}

	public boolean estaEstadoAtacando() {
		return this.tieneEstado(Estado.ATACANDO);
	}

	public boolean estaEstadoEstandar() {
		return this.tieneEstado(Estado.ESTANDAR);
	}

	public boolean estaEstadoCorriendo() {
		return this.tieneEstado(Estado.CORRIENDO);
	}

	public boolean estaEstadoPersiguiendo() {
		return this.tieneEstado(Estado.PERSIGUIENDO);
	}

	public void setVelocidadBase(final double velBase) {
		this.velocidadEstandar = velBase;
	}

	public Set<Estado> getEstado() {
		return this.estados;
	}

	public Point getPosicion() {
		return new Point((int) this.x, (int) this.y);
	}

	public Point getPosicionTile() {
		return new Point((int) this.x / Constantes.LADO_TILE, (int) this.y / Constantes.LADO_TILE);
	}

	public Direccion getDireccion() {
		return this.direccion;
	}

	public boolean atrasDeComplemento() {
		return this.atrasDeComplemento;
	}

	public void setPosicionX(final double x) {
		this.x = x;
		this.marcarPosicionModificada();
	}

	public void setPosicionY(final double y) {
		this.y = y;
		this.marcarPosicionModificada();
	}

	protected void setPosicionYSinVerificarZonebox(final double y) {
		this.y = y;
	}

	protected void setPosicionXSinVerificarZonebox(final double x) {
		this.x = x;
	}

	protected int getPosicionXIntDibujado() {
		return (int) this.x - this.margenXInicialSprite;
	}

	protected int getPosicionYIntDibujado() {
		return (int) this.y - this.margenYInicialSprite;
	}

	public int getMargenXSprite() {
		return this.margenXInicialSprite;
	}

	public int getMargenYSprite() {
		return this.margenYInicialSprite;
	}

	public double getVidaLag() {
		return this.vidaLag;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
		this.desvincularDeZonas();
	}

	@Override
	public int getPosicionXInt() {
		return (int) this.x;
	}

	@Override
	public int getPosicionYInt() {
		return (int) this.y;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void setPosicion(final double x, final double y) {
		this.x = x;
		this.y = y;
		this.marcarPosicionModificada();
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		if (desplazamientoX > 0) {
			this.direccion = Direccion.ESTE;
		} else if (desplazamientoX < 0) {
			this.direccion = Direccion.OESTE;
		}
		this.x += desplazamientoX;
		this.marcarPosicionModificada();
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		if (desplazamientoY > 0) {
			this.direccion = Direccion.SUR;
		} else if (desplazamientoY < 0) {
			this.direccion = Direccion.NORTE;
		}
		this.y += desplazamientoY;
		this.marcarPosicionModificada();
	}

	protected void reiniciarRecorridoAEstrella() {
		this.recorridoA.clear();
		this.nodoADestino = null;
	}

	protected void setDireccionMirandoCriatura(final Criatura c) {
		if (c != null) {
			this.direccion = Globales.FUNCIONES.getDireccionMirando(this.getPosicionXInt(), this.getPosicionYInt(),
					c.getPosicionXInt(), c.getPosicionYInt());
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJsonCriatura() {
		final JSONObject datosCriatura = this.exportarParaJSON();
		final JSONObject criatura = new JSONObject();
		criatura.put("tipo", this.exportarTipoCriatura());
		criatura.put("entiti", datosCriatura);
		return criatura;
	}

	public double getVelocidad() {
		return this.velocidad;
	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

	@Override
	public void setMundo(final Mundo mundo) {
		super.setMundo(mundo);
	}

	public abstract void establecerMargenesSprite();

	protected abstract JSONObject exportarParaJSON();

	public abstract String exportarTipoCriatura();
}