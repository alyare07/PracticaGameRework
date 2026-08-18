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

		} else if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara()
				.intersects(this.getPosicionX(), this.getPosicionY(), this.ANCHO, this.ALTO)) {
			this.pintarRectanguloBarraVida(g);
			this.pintarValorVida(g);
			return;
		}
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara()
				.intersects(this.getPosicionX(), this.getPosicionY(), this.ANCHO, this.ALTO)) {
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
		g.setFont(g.getFont().deriveFont(Globales.CONSTANTES.TAMANO_FUENTE));
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

	// --- MOVIMIENTO Y NAVEGACIÓN ---
	protected void moverANodoADestino() {
		if (this.nodoADestino == null) {
			return;
		}

		final Dimension dimNodo = this.getMundo().getAEstrellaX12X20().getDimensionNodoA();

		final int destX = this.nodoADestino.getXNodo() * dimNodo.width;
		final int destY = this.nodoADestino.getYNodo() * dimNodo.height;

		final int posCurrX = this.getPosicionXInt();
		final int posCurrY = this.getPosicionYInt();

		// Movimiento Vertical
		if (posCurrY < destY) {
			final double dist = destY - posCurrY;
			this.y = (dist < this.velocidad) ? destY : this.y + Math.min(dist, this.velocidad);
			this.direccion = Direccion.SUR;
		} else if (posCurrY > destY) {
			final double dist = posCurrY - destY;
			this.y = (dist < this.velocidad) ? destY : this.y - Math.min(dist, this.velocidad);
			this.direccion = Direccion.NORTE;
		}

		// Movimiento Horizontal
		if (posCurrX < destX) {
			final double dist = destX - posCurrX;
			this.x = (dist < this.velocidad) ? destX : this.x + Math.min(dist, this.velocidad);
			this.direccion = Direccion.ESTE;
		} else if (posCurrX > destX) {
			final double dist = posCurrX - destX;
			this.x = (dist < this.velocidad) ? destX : this.x - Math.min(dist, this.velocidad);
			this.direccion = Direccion.OESTE;
		}

		// Llegada exacta al tile -> Extraer el siguiente nodo de la cola
		if ((posCurrX == destX) && (posCurrY == destY)) {
			// poll() asigna el siguiente nodo o null si ya no quedan más pasos
			this.nodoADestino = this.recorridoA.poll();
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
		return this.tieneEstado(Estado.ATACANDO);
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
		return new Point((int) this.x / Globales.CONSTANTES.LADO_TILE,
				(int) this.y / Globales.CONSTANTES.LADO_TILE);
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