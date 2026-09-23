package principal.igu.textos;

import java.awt.Graphics2D;
import java.util.Random;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Gestor centralizado de textos flotantes del mundo y notificaciones fijas de
 * pantalla (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Dual Pool Architecture)
 */
public class GestorTextosFlotantes {

	private static final int CAPACIDAD_MUNDO = 64;
	private static final int CAPACIDAD_FIJOS = 32;

	// === 1. POOL DE TEXTOS DEL MUNDO (COMBATE / RECURSOS) ===
	private final TextoFlotante[] poolMundo;
	private final TextoFlotante[] activosMundo;
	private int cantidadActivosMundo;
	private int punteroCircularMundo;

	// === 2. POOL DE TEXTOS FIJOS DE PANTALLA (HUD / SISTEMA / MENÚS) ===
	private final TextoFlotante[] poolFijos;
	private final TextoFlotante[] activosFijos;
	private int cantidadActivosFijos;
	private int punteroCircularFijos;

	private final Random random;

	public GestorTextosFlotantes() {
		this.random = new Random();

		// Inicialización del pool de mundo
		this.poolMundo = new TextoFlotante[CAPACIDAD_MUNDO];
		this.activosMundo = new TextoFlotante[CAPACIDAD_MUNDO];
		this.cantidadActivosMundo = 0;
		this.punteroCircularMundo = 0;
		for (int i = 0; i < CAPACIDAD_MUNDO; i++) {
			this.poolMundo[i] = new TextoFlotante();
		}

		// Inicialización del pool de textos fijos
		this.poolFijos = new TextoFlotante[CAPACIDAD_FIJOS];
		this.activosFijos = new TextoFlotante[CAPACIDAD_FIJOS];
		this.cantidadActivosFijos = 0;
		this.punteroCircularFijos = 0;
		for (int i = 0; i < CAPACIDAD_FIJOS; i++) {
			this.poolFijos[i] = new TextoFlotante();
		}
	}

	// =========================================================================
	// === A. TEXTOS FLOTANTES DEL MUNDO (REFERENCIADOS A CÁMARA)
	// =========================================================================

	public void agregarDanio(final int danio, final double x, final double y, final boolean critico) {
		final TipoTextoFlotante tipo = critico ? TipoTextoFlotante.CRITICO : TipoTextoFlotante.DANIO_NORMAL;
		final String texto = (critico ? "¡" + danio + "!" : String.valueOf(danio));
		this.agregarTexto(texto, x, y, tipo);
	}

	public void agregarCuracion(final int curacion, final double x, final double y) {
		this.agregarTexto("+" + curacion, x, y, TipoTextoFlotante.CURACION);
	}

	public void agregarTexto(final String texto, final double x, final double y, final TipoTextoFlotante tipo) {
		if ((texto == null) || texto.isEmpty()) {
			return;
		}

		final double dispersion = (this.random.nextDouble() * 2.0) - 1.0;
		final TextoFlotante tf = this.poolMundo[this.punteroCircularMundo];
		this.punteroCircularMundo = (this.punteroCircularMundo + 1) % CAPACIDAD_MUNDO;

		final boolean yaEstaba = tf.isActivo();
		tf.activar(texto, x, y, tipo, dispersion);

		if (!yaEstaba && (this.cantidadActivosMundo < CAPACIDAD_MUNDO)) {
			this.activosMundo[this.cantidadActivosMundo] = tf;
			this.cantidadActivosMundo++;
		}
	}

	// =========================================================================
	// === B. TEXTOS FIJOS DE PANTALLA (COORDENADAS FIJAS 640x360)
	// =========================================================================

	public void agregarTextoFijo(final String texto, final double xPantalla, final double yPantalla,
			final TipoTextoFlotante tipo) {
		if ((texto == null) || texto.isEmpty()) {
			return;
		}

		final double dispersion = (this.random.nextDouble() * 0.8) - 0.4;
		final TextoFlotante tf = this.poolFijos[this.punteroCircularFijos];
		this.punteroCircularFijos = (this.punteroCircularFijos + 1) % CAPACIDAD_FIJOS;

		final boolean yaEstaba = tf.isActivo();
		tf.activar(texto, xPantalla, yPantalla, tipo, dispersion);

		if (!yaEstaba && (this.cantidadActivosFijos < CAPACIDAD_FIJOS)) {
			this.activosFijos[this.cantidadActivosFijos] = tf;
			this.cantidadActivosFijos++;
		}
	}

	public void agregarTextoFijo(final String texto, final double xPantalla, final double yPantalla) {
		this.agregarTextoFijo(texto, xPantalla, yPantalla, TipoTextoFlotante.AVISO_SISTEMA);
	}

	/**
	 * Emite una notificación en la parte superior central de la pantalla fija.
	 */
	public void emitirAvisoPantalla(final String texto, final TipoTextoFlotante tipo) {
		this.agregarTextoFijo(texto, Constantes.CENTROX - 40, 50, tipo);
	}

	// =========================================================================
	// === C. CICLO DE VIDA (GAME LOOP)
	// =========================================================================

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivosMundo) {
			final TextoFlotante tf = this.activosMundo[i];
			tf.actualizar(dt);

			if (tf.isActivo()) {
				i++;
			} else {
				this.activosMundo[i] = this.activosMundo[this.cantidadActivosMundo - 1];
				this.activosMundo[this.cantidadActivosMundo - 1] = null;
				this.cantidadActivosMundo--;
			}
		}
	}

	public void actualizarFijos() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivosFijos) {
			final TextoFlotante tf = this.activosFijos[i];
			tf.actualizar(dt);

			if (tf.isActivo()) {
				i++;
			} else {
				this.activosFijos[i] = this.activosFijos[this.cantidadActivosFijos - 1];
				this.activosFijos[this.cantidadActivosFijos - 1] = null;
				this.cantidadActivosFijos--;
			}
		}
	}

	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivosMundo; i++) {
			this.activosMundo[i].pintar(g);
		}
	}

	public void pintarFijos(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivosFijos; i++) {
			this.activosFijos[i].pintarFijo(g);
		}
	}

	public void limpiar() {
		for (int i = 0; i < this.cantidadActivosMundo; i++) {
			this.activosMundo[i] = null;
		}
		this.cantidadActivosMundo = 0;

		for (int i = 0; i < this.cantidadActivosFijos; i++) {
			this.activosFijos[i] = null;
		}
		this.cantidadActivosFijos = 0;
	}

	public int getCantidadActivos() {
		return this.cantidadActivosMundo + this.cantidadActivosFijos;
	}
}