package principal.dialogos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class CajaDialogo {

	private static final int ANCHO_CAJA = 480;
	private static final int ALTO_CAJA = 74;
	private static final int POS_X = Constantes.CENTROX - (ANCHO_CAJA / 2);
	private static final int POS_Y = Constantes.ALTO_JUEGO - ALTO_CAJA - 12;

	private static final Color COLOR_FONDO = new Color(14, 17, 24, 245);
	private static final Color COLOR_BORDE = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_ORO = new Color(220, 180, 50);

	private MensajeDialogo mensajeActual;
	private int caracteresRevelados = 0;
	private double tiempoAcumuladoLetra = 0.0;
	private static final double VELOCIDAD_TYPEWRITER = 0.025; // Segundos por letra

	private int opcionSeleccionada = 0;
	private boolean textoCompletado = false;

	public void mostrarMensaje(final MensajeDialogo mensaje) {
		this.mensajeActual = mensaje;
		this.caracteresRevelados = 0;
		this.tiempoAcumuladoLetra = 0.0;
		this.opcionSeleccionada = 0;
		this.textoCompletado = false;
	}

	public void actualizar(final double dt) {
		if (this.mensajeActual == null) {
			return;
		}

		final String texto = this.mensajeActual.getTextoCompleto();

		// 1. Efecto máquina de escribir
		if (!this.textoCompletado) {
			this.tiempoAcumuladoLetra += dt;
			while (this.tiempoAcumuladoLetra >= VELOCIDAD_TYPEWRITER && this.caracteresRevelados < texto.length()) {
				this.caracteresRevelados++;
				this.tiempoAcumuladoLetra -= VELOCIDAD_TYPEWRITER;
			}
			if (this.caracteresRevelados >= texto.length()) {
				this.textoCompletado = true;
			}
		}

		// 2. Control de avance / Salto de texto
		final boolean teclaAvanzar = Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER);

		if (teclaAvanzar) {
			if (!this.textoCompletado) {
				// Salta instantáneamente al final del texto
				this.caracteresRevelados = texto.length();
				this.textoCompletado = true;
			} else if (!this.mensajeActual.tieneOpciones()) {
				// Avanza al siguiente mensaje del gestor
				Globales.GESTOR_DIALOGOS.siguienteMensaje();
			} else {
				// Confirma la opción seleccionada
				final List<OpcionDialogo> ops = this.mensajeActual.getOpciones();
				if (this.opcionSeleccionada >= 0 && this.opcionSeleccionada < ops.size()) {
					ops.get(this.opcionSeleccionada).seleccionar();
				}
				Globales.GESTOR_DIALOGOS.siguienteMensaje();
			}
			GestorSonido.reproducir(IDSonido.GOLPE_1);
			return;
		}

		// 3. Navegación entre opciones múltiples (Arriba / Abajo)
		if (this.textoCompletado && this.mensajeActual.tieneOpciones()) {
			final int totalOps = this.mensajeActual.getOpciones().size();
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP) || Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
				this.opcionSeleccionada = (this.opcionSeleccionada <= 0) ? totalOps - 1 : this.opcionSeleccionada - 1;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN) || Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
				this.opcionSeleccionada = (this.opcionSeleccionada >= totalOps - 1) ? 0 : this.opcionSeleccionada + 1;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
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

		// 1. Marco principal de la caja de diálogo
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, Color.BLACK);

		// 2. Nombre del hablante en placa superior
		final String nombre = this.mensajeActual.getNombreHablante();
		int xTexto = x + 10;
		if (nombre != null && !nombre.isEmpty()) {
			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));

			final int anchoNom = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			Render2D.dibujarRectanguloRelleno(g, x + 8, y - 10, anchoNom + 12, 14, COLOR_FONDO);
			Render2D.dibujarRectanguloContorno(g, x + 8, y - 10, anchoNom + 12, 14, COLOR_BORDE_ORO);
			Render2D.dibujarStringConSombra(g, nombre, x + 14, y + 1, this.mensajeActual.getColorNombre(), Color.BLACK);

			g.setFont(fontPrevia);
		}

		// 3. Retrato si existe
		if (this.mensajeActual.getRetrato() != null) {
			Render2D.dibujarRectanguloRelleno(g, x + 8, y + 8, 48, 48, Color.BLACK);
			Render2D.dibujarRectanguloContorno(g, x + 8, y + 8, 48, 48, COLOR_BORDE_ORO);
			Render2D.dibujarImagen(g, this.mensajeActual.getRetrato(), x + 10, y + 10);
			xTexto += 54;
		}

		// 4. Texto revelado progresivamente
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		final String subTexto = this.mensajeActual.getTextoCompleto().substring(0, this.caracteresRevelados);
		this.dibujarTextoConSaltoDeLinea(g, subTexto, xTexto, y + 18, (x + w) - xTexto - 12);

		// 5. Opciones de decisión si ya terminó de escribir
		if (this.textoCompletado && this.mensajeActual.tieneOpciones()) {
			this.pintarOpciones(g, xTexto, y + 42);
		} else if (this.textoCompletado) {
			// Indicador de avance [►]
			Render2D.dibujarStringConSombra(g, ">", (x + w) - 14, (y + h) - 6, COLOR_BORDE_ORO, Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	private void pintarOpciones(final Graphics2D g, final int x, final int y) {
		final List<OpcionDialogo> ops = this.mensajeActual.getOpciones();
		int yOp = y;
		for (int i = 0; i < ops.size(); i++) {
			final boolean seleccionada = (i == this.opcionSeleccionada);
			final String prefijo = seleccionada ? "> " : "  ";
			final Color cTexto = seleccionada ? COLOR_BORDE_ORO : Color.LIGHT_GRAY;

			Render2D.dibujarStringConSombra(g, prefijo + ops.get(i).getTexto(), x, yOp, cTexto, Color.BLACK);
			yOp += 13;
		}
	}

	private void dibujarTextoConSaltoDeLinea(final Graphics2D g, final String texto, final int x, final int y, final int anchoMax) {
		final String[] palabras = texto.split(" ");
		final StringBuilder lineaActual = new StringBuilder();
		int yActual = y;

		for (int i = 0; i < palabras.length; i++) {
			final String prueba = lineaActual.length() == 0 ? palabras[i] : lineaActual + " " + palabras[i];
			final int anchoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, prueba);

			if (anchoLinea > anchoMax) {
				Render2D.dibujarStringConSombra(g, lineaActual.toString(), x, yActual, Color.WHITE, Color.BLACK);
				lineaActual.setLength(0);
				lineaActual.append(palabras[i]);
				yActual += 13;
			} else {
				lineaActual.setLength(0);
				lineaActual.append(prueba);
			}
		}

		if (lineaActual.length() > 0) {
			Render2D.dibujarStringConSombra(g, lineaActual.toString(), x, yActual, Color.WHITE, Color.BLACK);
		}
	}
}