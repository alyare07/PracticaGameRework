package principal.igu.textos;

import java.awt.Graphics2D;
import java.util.Random;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de textos flotantes de combate (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8)
 */
public class GestorTextosFlotantes {

	private static final int CAPACIDAD_MAXIMA = 64;

	private final TextoFlotante[] pool;
	private final TextoFlotante[] activos;
	private int cantidadActivos;
	private int punteroCircular;
	private final Random random;

	public GestorTextosFlotantes() {
		this.pool = new TextoFlotante[CAPACIDAD_MAXIMA];
		this.activos = new TextoFlotante[CAPACIDAD_MAXIMA];
		this.cantidadActivos = 0;
		this.punteroCircular = 0;
		this.random = new Random();

		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i] = new TextoFlotante();
		}
	}

	// =========================================================================
	// === MÉTODOS DE DISPARO RÁPIDO (API PÚBLICA)
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

		final TextoFlotante tf = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_MAXIMA;

		final boolean yaEstabaEnActivos = tf.isActivo();

		tf.activar(texto, x, y, tipo, dispersion);

		if (!yaEstabaEnActivos && (this.cantidadActivos < CAPACIDAD_MAXIMA)) {
			this.activos[this.cantidadActivos] = tf;
			this.cantidadActivos++;
		}
	}

	// =========================================================================
	// === CICLO DE VIDA (GAME LOOP)
	// =========================================================================

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivos) {
			final TextoFlotante tf = this.activos[i];
			tf.actualizar(dt);

			if (tf.isActivo()) {
				i++;
			} else {
				this.activos[i] = this.activos[this.cantidadActivos - 1];
				this.activos[this.cantidadActivos - 1] = null;
				this.cantidadActivos--;
			}
		}
	}

	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.activos[i].pintar(g);
		}
	}

	public void limpiar() {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.activos[i] = null;
		}
		this.cantidadActivos = 0;
		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i].actualizar(100.0);
		}
	}

	public int getCantidadActivos() {
		return this.cantidadActivos;
	}
}