package principal.igu.textos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

/**
 * Unidad individual de texto flotante reutilizable en memoria (Zero-GC).
 * <p>
 * <b>Física y Cinemática:</b><br>
 * Simula una trayectoria balística en parábola:
 * <ul>
 * <li><b>Salto Inicial (Impulso):</b> Salta hacia arriba con velocidad vertical
 * negativa (-Vy) y una ligera dispersión horizontal aleatoria (Vx).</li>
 * <li><b>Gravedad:</b> Una fuerza de aceleración hacia abajo (+G) frena el
 * salto y lo hace descender suavemente.</li>
 * <li><b>Sombra de Contraste Pixel-Art:</b> Dibuja una sombra negra de 1 px
 * alrededor del texto para garantizar que sea 100% legible sobre cualquier
 * fondo (pasto, agua, lava o nieve).</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class TextoFlotante {

	// =========================================================================
	// === 1. CONSTANTES FÍSICAS
	// =========================================================================

	/** Aceleración de gravedad hacia abajo en píxeles por segundo al cuadrado. */
	private static final double GRAVEDAD = 180.0;

	// =========================================================================
	// === 2. ESTADO Y POSICIÓN EN EL MUNDO CONTINUO
	// =========================================================================

	private boolean activo;
	private String texto;
	private double posX;
	private double posY;
	private double velX;
	private double velY;

	private double duracionSegundos;
	private double tiempoTranscurrido;
	private Color colorTexto;
	private Font fuente;
	private boolean esCritico;

	public TextoFlotante() {
		this.activo = false;
		this.texto = "";
		this.colorTexto = Color.WHITE;
		this.fuente = new Font(Font.SANS_SERIF, Font.BOLD, (int) Constantes.TAMANO_FUENTE);
	}

	// =========================================================================
	// === ACTIVACIÓN Y REUTILIZACIÓN (CERO 'NEW' EN HEAP)
	// =========================================================================

	/**
	 * Configura y dispara el texto flotante con impulso balístico.
	 *
	 * @param texto       Mensaje o número a mostrar (ej: "-35", "¡CRÍTICO!").
	 * @param x           Coordenada X inicial en píxeles del mundo (sobre la cabeza
	 *                    del ente).
	 * @param y           Coordenada Y inicial en píxeles del mundo.
	 * @param tipo        Estilo visual y preset del texto.
	 * @param dispersionX Desvío horizontal aleatorio (-1.0 a +1.0).
	 */
	public void activar(final String texto, final double x, final double y, final TipoTextoFlotante tipo,
			final double dispersionX) {
		this.texto = texto;
		this.posX = x;
		this.posY = y;
		this.colorTexto = tipo.getColor();
		this.duracionSegundos = tipo.getDuracionMs() / 1000.0;
		this.tiempoTranscurrido = 0.0;
		this.esCritico = tipo.isCritico();

		// Asignación de fuente según sea crítico (más grande) o normal
		final int estilo = this.esCritico ? Font.BOLD : Font.PLAIN;
		this.fuente = new Font(Font.SANS_SERIF, estilo, (int) tipo.getTamanoFuente());

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: VELOCIDADES INICIALES DEL SALTO
		 * --------------------------------------------------------------------- - Los
		 * críticos saltan más alto (-110 px/s) para mayor dramatismo. - Los golpes
		 * normales saltan con velocidad moderada (-75 px/s). - velX dispersa el número
		 * hacia la izquierda o derecha para que si ocurren múltiples golpes seguidos,
		 * los números no se tapen entre sí.
		 * =====================================================================
		 */
		this.velY = this.esCritico ? -110.0 : -75.0;
		this.velX = dispersionX * (this.esCritico ? 35.0 : 20.0);

		this.activo = true;
	}

	// =========================================================================
	// === ACTUALIZACIÓN FÍSICA (GAME LOOP A 60 APS)
	// =========================================================================

	/**
	 * Avanza la trayectoria balística aplicando gravedad y monitorea el tiempo de
	 * vida.
	 *
	 * @param dt Delta de tiempo en segundos (1.0 / 60.0).
	 */
	public void actualizar(final double dt) {
		if (!this.activo) {
			return;
		}

		this.tiempoTranscurrido += dt;

		// Si el tiempo de vida expiró, desactivamos la partícula
		if (this.tiempoTranscurrido >= this.duracionSegundos) {
			this.activo = false;
			return;
		}

		// 1. Integración de Euler: Posición += Velocidad * dt
		this.posX += this.velX * dt;
		this.posY += this.velY * dt;

		// 2. Gravedad: La velocidad hacia abajo aumenta con el tiempo
		this.velY += GRAVEDAD * dt;

		// 3. Fricción del aire horizontal: frena suavemente la deriva lateral
		this.velX *= (1.0 - (0.8 * dt));
	}

	// =========================================================================
	// === RENDERIZADO EN CAPA DE MUNDO (CON SOMBRA DE ALTO CONTRASTE)
	// =========================================================================

	/**
	 * Dibuja el texto flotante de daño con sombra de contraste proyectado con la
	 * cámara.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.activo || this.texto.isEmpty()) {
			return;
		}

		final Font fuentePrevia = g.getFont();
		g.setFont(this.fuente);

		final int renderX = (int) Math.round(this.posX);
		final int renderY = (int) Math.round(this.posY);

		// =========================================================================
		// DIBUJO DE TEXTO CON SOMBRA DE ALTO CONTRASTE REFERENCIADO A CÁMARA
		// =========================================================================
		DibujoDebug.dibujarStringConSombraRefCamara(g, this.texto, renderX, renderY, this.colorTexto, Color.BLACK);

		g.setFont(fuentePrevia);
	}

	public boolean isActivo() {
		return this.activo;
	}
}