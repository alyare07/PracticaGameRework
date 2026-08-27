package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.particulas.Sangre;
import principal.ia.aEstrella.NodoA;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Clase abstracta base para todas las entidades vivas (Jugador, Enemigos,
 * NPCs). Define la gestión de vida, posición, movimiento por Pathfinding (A*) y
 * estados.
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

	/** Velocidades acumuladas continuas para inercia y giros suaves */
	protected double velActualX = 0.0;
	protected double velActualY = 0.0;

	/**
	 * Factor de agilidad de giro (0.1 = giro muy suave y pesado, 0.35 = ágil y
	 * reactivo). Ajustable según el tipo de enemigo (un zombie gira más lento que
	 * un goblin).
	 */
	protected double agilidadGiro = 0.25;
	/** Radio en píxeles para empezar a anticipar la siguiente curva */
	protected static final double RADIO_ANTICIPACION_ESQUINA = 12.0;
	/**
	 * Radio en píxeles para considerar que la criatura ya alcanzó el nodo actual
	 */
	protected static final double RADIO_LLEGADA_WAYPOINT = 4.0;

	// EnumSet prealocado. No genera garbage collection durante el juego.
	private final Set<Estado> estados = EnumSet.noneOf(Estado.class);
	protected final int ANCHO;
	protected final int ALTO;
	protected double velocidad = 1.0;
	protected double x;
	protected double y;
	protected double vida;
	protected double vidaMaxima;
	protected double velocidadEstandar = 0.5;

	protected final GestorTiempo GT_ESPERA; // Temporizador de acción de espera
	protected final GestorTiempo GT_ATACADO; // Tiempo transcurrido desde el último ataque recibido
	protected final GestorTiempo GT_CURACION; // Temporizador de regeneración de vida
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

	// --- CONSTRUCTORES ---

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

	/**
	 * Constructor principal centralizado para evitar duplicación de código.
	 */
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

		this.GT_ESPERA = new GestorTiempo();
		this.GT_ATACADO = new GestorTiempo();
		this.GT_CURACION = new GestorTiempo();
		this.vidaRegen = 1.0;
		this.direccion = Direccion.ESTE;
		this.recorridoA = new ArrayDeque<NodoA>();
	}

	// --- MÉTODOS DE DIBUJO ---

	public Rectangle getRectangulo() {
		return new Rectangle((int) this.x, (int) this.y, this.ANCHO, this.ALTO);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarIndicadorVida(g);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.CYAN);
		}

		// Renderizado del camino A* en modo debug
		if ((Globales.CAMARA.getEntidadEnfocada() == this)
				&& (!this.recorridoA.isEmpty() || (this.nodoADestino != null))) {
			g.setFont(g.getFont().deriveFont(7f));

			final Dimension dimNodo = this.getMundo().getAEstrellaX12X20().getDimensionNodoA();
			final int anchoTile = dimNodo.width;
			final int altoTile = dimNodo.height;

			int pos = 1;

			// Iteración limpia sobre la cola sin consumirla
			for (final NodoA n : this.recorridoA) {
				final int xMundo = n.getXNodo() * anchoTile;
				final int yMundo = n.getYNodo() * altoTile;
				final String txt = String.valueOf(pos);

				DibujoDebug.dibujarRectanguloContornoRefCamara(g, new Rectangle(xMundo, yMundo, anchoTile, altoTile),
						Color.MAGENTA);

				final int xTexto = (xMundo + (anchoTile / 2))
						- (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2);
				final int yTexto = (yMundo + (altoTile / 2))
						+ (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2);

				DibujoDebug.dibujarStringRefCamara(g, txt, xTexto, yTexto, Color.BLACK);
				pos++;
			}

			// Destino inmediato en Amarillo
			if (this.nodoADestino != null) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getXNodo() * anchoTile,
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
			return;
		}
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getPosicionX(),
				this.getPosicionY(), this.ANCHO, this.ALTO)) {
			this.pintarRectanguloBarraVida(g);
			this.pintarValorVida(g);
		}
	}

	private void pintarValorVida(final Graphics2D g) {
		g.setFont(g.getFont().deriveFont(4f));
		final String texto = (int) this.vida + "/" + (int) this.vidaMaxima;
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);

		final int xTexto = this.getPosicionXInt() + ((this.ANCHO - anchoTexto) / 2);
		DibujoDebug.dibujarStringRefCamara(g, texto, xTexto, this.getPosicionYInt() - 6, Color.WHITE);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	private void pintarRectanguloBarraVida(final Graphics2D g) {
		this.getPosicionXInt();
		this.getPosicionYInt();
		// BUSCAR LA FORMA DE DEJAR DE CREAR NUEVOS RECTANGLE EN EL ACT
		final int porcentajeBarraActual = ((int) ((this.vida * 100) / this.vidaMaxima) * this.ANCHO) / 100;

		// Barra negra indicador
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt() - 1, this.getPosicionYInt() - 5,
				this.ANCHO + 2, 4, Color.BLACK);
		// barra vida actual
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt() - 4,
				porcentajeBarraActual, 2, Color.RED);
	}

	protected void moverANodoADestino() {
		// 1. Si no hay nodo activo, extraemos el primero de la ruta
		if (this.nodoADestino == null) {
			this.nodoADestino = this.recorridoA.poll();
			if (this.nodoADestino == null) {
				this.velActualX = 0.0;
				this.velActualY = 0.0;
				return;
			}
		}

		// 2. Centro de masa actual de la criatura
		final double centroX = this.x + (this.ANCHO / 2.0);
		final double centroY = this.y + (this.ALTO / 2.0);

		// 3. Centro de la casilla destino actual
		double targetX = this.nodoADestino.getXMundo() + (this.nodoADestino.getAncho() / 2.0);
		double targetY = this.nodoADestino.getYMundo() + (this.nodoADestino.getAlto() / 2.0);

		double diffX = targetX - centroX;
		double diffY = targetY - centroY;
		double distAlNodo = Math.hypot(diffX, diffY);

		// 4. AVANCE DE WAYPOINT ROBUSTO (Proximidad O Superación de Plano por Producto
		// Punto)
		NodoA siguienteNodo = this.recorridoA.peek();
		boolean avanzarNodo = (distAlNodo <= Math.max(RADIO_LLEGADA_WAYPOINT, this.velocidad));

		if (!avanzarNodo && (siguienteNodo != null)) {
			final double sigX = siguienteNodo.getXMundo() + (siguienteNodo.getAncho() / 2.0);
			final double sigY = siguienteNodo.getYMundo() + (siguienteNodo.getAlto() / 2.0);

			// Vector del segmento: del nodo actual al siguiente nodo
			final double segX = sigX - targetX;
			final double segY = sigY - targetY;

			// Vector de posición: del nodo actual hacia la criatura
			final double posRelX = centroX - targetX;
			final double posRelY = centroY - targetY;

			// Producto Punto (Dot Product): Si es positivo, la criatura ya cruzó el nodo
			// actual hacia el siguiente
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

			// Actualizamos coordenadas con el nuevo nodo extraído
			targetX = this.nodoADestino.getXMundo() + (this.nodoADestino.getAncho() / 2.0);
			targetY = this.nodoADestino.getYMundo() + (this.nodoADestino.getAlto() / 2.0);
			diffX = targetX - centroX;
			diffY = targetY - centroY;
			distAlNodo = Math.hypot(diffX, diffY);
			siguienteNodo = this.recorridoA.peek();
		}

		// 5. LOOKAHEAD (Curvatura suave al aproximarse a la esquina)
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

		// 6. DIRECCIÓN VECTORIAL E INERCIA
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

	// --- GESTIÓN DE VIDA ---

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
		this.vida = Math.max(0, this.vida - puntos);
		if (this.vida <= 0) {
			this.eliminar();
		}
	}

	public void establecerVidaMaxima(final double puntos) {
		this.vidaMaxima = puntos;
		this.vida = puntos;
	}

	public void aumentarVidaMaxima(final double puntos) {
		this.vidaMaxima += puntos;
		this.vida += puntos;
	}

	public void reducirVidaMaxima(final double puntos) {
		this.vidaMaxima = Math.max(50, this.vidaMaxima - puntos);
		this.vida = Math.min(this.vida, this.vidaMaxima);
	}

	public void curar(final double puntos) {
		this.vida = Math.min(this.vidaMaxima, this.vida + puntos);

		// Curación (número verde "+50"):
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
	}

	public void sanar() {
		this.vida = this.vidaMaxima;
	}

	public void calcularRutaAEstrella(final int xObjetivo, final int yObjetivo) {
		if (this.mundo == null) {
			return;
		}

		// A* poblará la cola recorridoA
		this.mundo.getAEstrellaX12X20().getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(), xObjetivo,
				yObjetivo, this.recorridoA);

		// .poll() extrae el primer paso y lo remueve de la cola. Si la cola está vacía,
		// devuelve null.
		this.nodoADestino = this.recorridoA.poll();
	}

	// --- GESTIÓN DE ESTADOS ---

	public String getStringEstados() {
		final StringBuilder sb = new StringBuilder();
		for (final Estado e : this.estados) {
			sb.append(e.toString()).append("  ");
		}
		return sb.toString();
	}

	/**
	 * Verifica si la criatura tiene activo un estado específico. O(1) a nivel de
	 * bitmask.
	 */
	public boolean tieneEstado(final Estado estado) {
		return this.estados.contains(estado);
	}

	/**
	 * Agrega un estado sin duplicar ni generar objetos nuevos.
	 */
	public void meterEstado(final Estado estado) {
		this.estados.add(estado);
	}

	/**
	 * Remueve un estado activo de forma atómica.
	 */
	public void removerEstado(final Estado estado) {
		this.estados.remove(estado);
	}

	/**
	 * Limpia todos los estados y asigna únicamente el estado indicado. Ideal para
	 * transiciones como volver a ESTANDAR o entrar en MUERTO.
	 */
	public void setEstadoUnico(final Estado estado) {
		this.estados.clear();
		this.estados.add(estado);
	}

	/**
	 * Remueve todos los estados activos.
	 */
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

	// --- GETTERS Y SETTERS DE POSICIÓN ---

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

	public void setPosicionX(final int x) {
		this.x = x;
	}

	public void setPosicionY(final int y) {
		this.y = y;
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

	@Override
	public void eliminar() {
		this.eliminado = true;
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
	public void modificarPosicionX(final double desplazamientoX) {
		if (desplazamientoX > 0) {
			this.direccion = Direccion.ESTE;
		} else if (desplazamientoX < 0) {
			this.direccion = Direccion.OESTE;
		}
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		if (desplazamientoY > 0) {
			this.direccion = Direccion.SUR;
		} else if (desplazamientoY < 0) {
			this.direccion = Direccion.NORTE;
		}
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	public void recibirAtaque(final double damage, final Ente causante) {
		if (this.mundo != null) {
			this.mundo.agregarParticula(
					new Sangre(this.getPosicionXInt() + (this.ANCHO / 2), this.getPosicionYInt() + (this.ALTO / 2)));
			// Daño normal (número blanco que salta):
			Globales.GESTOR_TEXTOS.agregarDanio((int) damage, this.getPosicionX(), this.getPosicionY(), false);

			// Daño crítico (número rojo grande con "¡58!" + sacudida de cámara):
//			Globales.GESTOR_TEXTOS.agregarDanio(58, this.getPosicionX(), this.getPosicionY(), true);
//			Globales.CAMARA.aplicarImpactoCritico(100);

			// Curación (número verde "+50"):
//			Globales.GESTOR_TEXTOS.agregarCuracion(50, this.getPosicionX(), this.getPosicionY());

			// Mensaje de estado ("¡FALLO!", "¡BLOQUEO!"):
//			Globales.GESTOR_TEXTOS.agregarTexto("¡FALLO!", this.getPosicionX(), this.getPosicionY(),
//					TipoTextoFlotante.ESTADO);
		}
	}

	protected void reiniciarRecorridoAEstrella() {
		this.recorridoA.clear();
		this.nodoADestino = null;
	}

	protected void setDireccionMirandoCriatura(final Criatura c) {
		this.direccion = Globales.FUNCIONES.getDireccionMirando(this.getPosicionXInt(), this.getPosicionYInt(),
				c.getPosicionXInt(), c.getPosicionYInt());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJsonCriatura() {
		final JSONObject datosCriatura = this.exportarParaJSON();
		final JSONObject criatura = new JSONObject();
		criatura.put("tipo", this.exportarTipoCriatura());
		criatura.put("entiti", datosCriatura);
		return criatura;
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

	public int getCentroX() {
		return this.getPosicionXInt() + (this.ANCHO / 2);
	}

	public int getCentroY() {
		return this.getPosicionYInt() + (this.ALTO / 2);
	}

	// --- MÉTODOS ABSTRACTOS ---
	public abstract void establecerMargenesSprite();

	protected abstract JSONObject exportarParaJSON();

	public abstract String exportarTipoCriatura();
}