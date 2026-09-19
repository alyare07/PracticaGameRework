package principal.dialogos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Cuadro de diálogo y opciones con desplazamiento vertical automático y por
 * rueda del ratón (Zero-GC / Viewport Clipping).
 * 
 * @version 2.0 (Vanilla Java 8 - Smart Auto-Scrolling Dialogue Box)
 */
public class CajaDialogo {

	private static final int ANCHO_CAJA = 480;
	private static final int ALTO_CAJA = 82;
	private static final int POS_X = Constantes.CENTROX - (ANCHO_CAJA / 2);
	private static final int POS_Y = Constantes.ALTO_JUEGO - ALTO_CAJA - 10;

	private static final Color COLOR_FONDO = new Color(14, 17, 24, 245);
	private static final Color COLOR_BORDE = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_ORO = new Color(220, 180, 50);
	private static final Color COLOR_TRACK_SCROLL = new Color(25, 30, 40, 220);
	private static final Color COLOR_THUMB_SCROLL = new Color(220, 180, 50, 240);

	private static final int ESPACIADO_LINEA = 13;
	private static final double VELOCIDAD_TYPEWRITER = 0.025; // Segundos por letra

	private MensajeDialogo mensajeActual;
	private int caracteresRevelados = 0;
	private double tiempoAcumuladoLetra = 0.0;
	private boolean textoCompletado = false;

	private int opcionSeleccionada = 0;

	// Control de Scroll
	private int scrollY = 0;
	private int maxScrollY = 0;
	private int altoContenidoTotal = 0;
	private final List<String> lineasTextoEnvuelto = new ArrayList<String>();

	public void mostrarMensaje(final MensajeDialogo mensaje) {
		this.mensajeActual = mensaje;
		this.caracteresRevelados = 0;
		this.tiempoAcumuladoLetra = 0.0;
		this.opcionSeleccionada = 0;
		this.textoCompletado = false;
		this.scrollY = 0;
		this.maxScrollY = 0;
		this.lineasTextoEnvuelto.clear();
	}

	public void actualizar(final double dt) {
		if (this.mensajeActual == null) {
			return;
		}

		final String texto = this.mensajeActual.getTextoCompleto();

		// 1. Efecto máquina de escribir
		if (!this.textoCompletado) {
			this.tiempoAcumuladoLetra += dt;
			while ((this.tiempoAcumuladoLetra >= VELOCIDAD_TYPEWRITER) && (this.caracteresRevelados < texto.length())) {
				this.caracteresRevelados++;
				this.tiempoAcumuladoLetra -= VELOCIDAD_TYPEWRITER;
			}
			if (this.caracteresRevelados >= texto.length()) {
				this.textoCompletado = true;
			}
		}

		// 2. Desplazamiento por rueda del ratón
		if (Globales.RATON != null) {
			final int rueda = Globales.RATON.getRotacionRueda();
			if ((rueda != 0) && (this.maxScrollY > 0)) {
				this.scrollY = Math.max(0, Math.min(this.maxScrollY, this.scrollY + (rueda * ESPACIADO_LINEA)));
			}
		}

		// 3. Control de avance y selección
		final boolean teclaAvanzar = Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER);

		if (teclaAvanzar) {
			if (!this.textoCompletado) {
				this.caracteresRevelados = texto.length();
				this.textoCompletado = true;
			} else if (!this.mensajeActual.tieneOpciones()) {
				Globales.GESTOR_DIALOGOS.siguienteMensaje();
			} else {
				final List<OpcionDialogo> ops = this.mensajeActual.getOpciones();
				if ((this.opcionSeleccionada >= 0) && (this.opcionSeleccionada < ops.size())) {
					ops.get(this.opcionSeleccionada).seleccionar();
				}
				Globales.GESTOR_DIALOGOS.siguienteMensaje();
			}
			GestorSonido.reproducir(IDSonido.SELECT);
			return;
		}

		// 4. Navegación entre opciones con auto-centrado de scroll
		if (this.textoCompletado && this.mensajeActual.tieneOpciones()) {
			final int totalOps = this.mensajeActual.getOpciones().size();
			final int previo = this.opcionSeleccionada;

			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
					|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
				this.opcionSeleccionada = (this.opcionSeleccionada <= 0) ? totalOps - 1 : this.opcionSeleccionada - 1;
			} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
					|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
				this.opcionSeleccionada = (this.opcionSeleccionada >= (totalOps - 1)) ? 0 : this.opcionSeleccionada + 1;
			}

			if (previo != this.opcionSeleccionada) {
				GestorSonido.reproducir(IDSonido.SELECT_MENU);
				this.ajustarScrollAOpcionSeleccionada();
			}
		}
	}

	/**
	 * Mantiene la opción seleccionada siempre visible dentro de la ventana de
	 * diálogo.
	 */
	private void ajustarScrollAOpcionSeleccionada() {
		final int altoVisible = ALTO_CAJA - 16;
		final int yInicioOpciones = 8 + (this.lineasTextoEnvuelto.size() * ESPACIADO_LINEA) + 4;
		final int yOpcionRelativa = yInicioOpciones + (this.opcionSeleccionada * ESPACIADO_LINEA);

		if ((yOpcionRelativa - this.scrollY) < 8) {
			this.scrollY = Math.max(0, yOpcionRelativa - 8);
		} else if (((yOpcionRelativa + ESPACIADO_LINEA) - this.scrollY) > altoVisible) {
			this.scrollY = Math.min(this.maxScrollY, ((yOpcionRelativa + ESPACIADO_LINEA) - altoVisible) + 4);
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.mensajeActual == null) {
			return;
		}

		final int x = POS_X;
		final int y = POS_Y;
		final int w = ANCHO_CAJA;
		final int h = ALTO_CAJA;

		// 1. Chasis exterior del diálogo
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, Color.BLACK);

		// 2. Placa superior con el nombre del personaje
		final String nombre = this.mensajeActual.getNombreHablante();
		int xTexto = x + 10;
		if ((nombre != null) && !nombre.isEmpty()) {
			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));

			final int anchoNom = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			Render2D.dibujarRectanguloRelleno(g, x + 8, y - 10, anchoNom + 12, 14, COLOR_FONDO);
			Render2D.dibujarRectanguloContorno(g, x + 8, y - 10, anchoNom + 12, 14, COLOR_BORDE_ORO);
			Render2D.dibujarStringConSombra(g, nombre, x + 14, y + 1, this.mensajeActual.getColorNombre(), Color.BLACK);

			g.setFont(fontPrevia);
		}

		// 3. Retrato del interlocutor
		if (this.mensajeActual.getRetrato() != null) {
			Render2D.dibujarRectanguloRelleno(g, x + 8, y + 8, 48, 48, Color.BLACK);
			Render2D.dibujarRectanguloContorno(g, x + 8, y + 8, 48, 48, COLOR_BORDE_ORO);
			Render2D.dibujarImagen(g, this.mensajeActual.getRetrato(), x + 10, y + 10);
			xTexto += 54;
		}

		final int anchoUtilTexto = (x + w) - xTexto - 18;

		// 4. Medición y cálculo de líneas
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		this.envolverTexto(g, this.mensajeActual.getTextoCompleto(), anchoUtilTexto);

		final int cantOpciones = this.mensajeActual.tieneOpciones() ? this.mensajeActual.getOpciones().size() : 0;
		this.altoContenidoTotal = 8 + (this.lineasTextoEnvuelto.size() * ESPACIADO_LINEA)
				+ (cantOpciones > 0 ? (4 + (cantOpciones * ESPACIADO_LINEA)) : 0);

		final int altoVisible = h - 16;
		this.maxScrollY = Math.max(0, this.altoContenidoTotal - altoVisible);

		// 5. Renderizado recortado dentro del cuadro (Clipping)
		final Graphics2D gClip = (Graphics2D) g.create();
		try {
			gClip.setClip(x + 4, y + 6, w - 12, h - 12);

			int yCursor = (y + 18) - this.scrollY;
			int caracteresAcumulados = 0;

			// Dibujar líneas de texto hasta los caracteres revelados por el typewriter
			for (int i = 0; i < this.lineasTextoEnvuelto.size(); i++) {
				final String linea = this.lineasTextoEnvuelto.get(i);
				if (this.caracteresRevelados >= (caracteresAcumulados + linea.length())) {
					Render2D.dibujarStringConSombra(gClip, linea, xTexto, yCursor, Color.WHITE, Color.BLACK);
				} else if (this.caracteresRevelados > caracteresAcumulados) {
					final int parcial = this.caracteresRevelados - caracteresAcumulados;
					final String lineaParcial = linea.substring(0, parcial);
					Render2D.dibujarStringConSombra(gClip, lineaParcial, xTexto, yCursor, Color.WHITE, Color.BLACK);
				}
				caracteresAcumulados += linea.length() + 1;
				yCursor += ESPACIADO_LINEA;
			}

			// Dibujar opciones desplazables
			if (this.textoCompletado && this.mensajeActual.tieneOpciones()) {
				yCursor += 4;
				final List<OpcionDialogo> ops = this.mensajeActual.getOpciones();
				for (int i = 0; i < ops.size(); i++) {
					final boolean seleccionada = (i == this.opcionSeleccionada);
					final String prefijo = seleccionada ? "> " : "  ";
					final Color cTexto = seleccionada ? COLOR_BORDE_ORO : Color.LIGHT_GRAY;

					Render2D.dibujarStringConSombra(gClip, prefijo + ops.get(i).getTexto(), xTexto, yCursor, cTexto,
							Color.BLACK);
					yCursor += ESPACIADO_LINEA;
				}
			} else if (this.textoCompletado && !this.mensajeActual.tieneOpciones()) {
				Render2D.dibujarStringConSombra(gClip, ">", (x + w) - 20, (y + h) - 6 - this.scrollY, COLOR_BORDE_ORO,
						Color.BLACK);
			}

		} finally {
			gClip.dispose();
		}

		// 6. Barra de Scroll lateral si el contenido supera el cuadro
		if (this.maxScrollY > 0) {
			final int trackX = (x + w) - 8;
			final int trackY = y + 8;
			final int trackH = h - 16;
			Render2D.dibujarRectanguloRelleno(g, trackX, trackY, 3, trackH, COLOR_TRACK_SCROLL);

			final double ratio = (double) this.scrollY / this.maxScrollY;
			final int thumbH = Math.max(12, (int) (((double) altoVisible / this.altoContenidoTotal) * trackH));
			final int thumbY = trackY + (int) (ratio * (trackH - thumbH));

			Render2D.dibujarRectanguloRelleno(g, trackX, thumbY, 3, thumbH, COLOR_THUMB_SCROLL);
		}

		g.setFont(fontPrevia);
	}

	private void envolverTexto(final Graphics2D g, final String texto, final int anchoMax) {
		if (!this.lineasTextoEnvuelto.isEmpty()) {
			return;
		}

		final String[] palabras = texto.split(" ");
		final StringBuilder lineaActual = new StringBuilder();

		for (int i = 0; i < palabras.length; i++) {
			final String prueba = lineaActual.length() == 0 ? palabras[i] : lineaActual + " " + palabras[i];
			final int anchoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, prueba);

			if (anchoLinea > anchoMax) {
				this.lineasTextoEnvuelto.add(lineaActual.toString());
				lineaActual.setLength(0);
				lineaActual.append(palabras[i]);
			} else {
				lineaActual.setLength(0);
				lineaActual.append(prueba);
			}
		}

		if (lineaActual.length() > 0) {
			this.lineasTextoEnvuelto.add(lineaActual.toString());
		}
	}
}