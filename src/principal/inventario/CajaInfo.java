package principal.inventario;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

/**
 * Componente visual de interfaz de usuario diseñado para renderizar paneles
 * compactos con pares de información clave-valor (etiquetas y valores dinámicos
 * de estadísticas).
 * 
 * <p>
 * <b>Características y Algoritmo de Renderizado:</b>
 * </p>
 * <ul>
 * <li><b>Distribución Automática en Columnas (Multi-Column Wrapping):</b> Si la
 * lista de atributos excede el límite vertical ({@link #AREA}{@code .height}),
 * el algoritmo calcula el ancho máximo de la columna actual y salta
 * automáticamente a la siguiente columna hacia la derecha.</li>
 * <li><b>Optimización Zero-GC en Render:</b> Reutiliza la instancia inmutable
 * {@link #fuenteCaja} y mide cadenas mediante {@code MEDIDOR_STRING} con tipos
 * primitivos, evitando llamadas a {@code Font.deriveFont()} o la creación de
 * nuevos objetos en cada cuadro.</li>
 * <li><b>Paleta de Colores Configurable:</b> Permite diferenciar visualmente
 * las etiquetas estáticas de los valores variables.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Info
 * @see principal.inventario.equipamiento.SlotArma
 */
public class CajaInfo {

	/***/
	/* ========================================================================= */
	/* 1. PALETA DE COLORES Y FUENTES POR DEFECTO (GC FRIENDLY) */
	/* ========================================================================= */
	/***/
	private static final Color COLOR_BORDES_DEFECTO = Color.BLACK;
	private static final Color COLOR_LABELS_DEFECTO = Color.WHITE;
	private static final Color COLOR_VALORES_DEFECTO = Color.BLUE;
	private static final int ESPACIADO_INTERLINEADO = 1;
	private static final int MARGEN_LABEL_VALOR_DEFECTO = 2;

	/***/
	/* ========================================================================= */
	/* 2. ESTADO Y LÍMITES GEOMÉTRICOS */
	/* ========================================================================= */
	/***/
	protected final Rectangle AREA;
	protected final Font fuenteCaja;
	protected final int margenLabelValor;

	protected HashMap<String, Info> lista;
	protected Color colorBordes;
	protected Color colorLabels;
	protected Color colorValores;

	/**
	 * Construye una caja de información en una región específica de la pantalla.
	 * 
	 * @param area Límites espaciales (X, Y, ancho, alto) donde se dibujarán las
	 *             estadísticas.
	 */
	public CajaInfo(final Rectangle area) {
		this.AREA = area;
		this.lista = new HashMap<String, Info>();
		this.colorBordes = COLOR_BORDES_DEFECTO;
		this.colorLabels = COLOR_LABELS_DEFECTO;
		this.colorValores = COLOR_VALORES_DEFECTO;
		this.fuenteCaja = new Font(Font.SANS_SERIF, Font.PLAIN, 4);
		this.margenLabelValor = MARGEN_LABEL_VALOR_DEFECTO;
	}

	/***/
	/* ========================================================================= */
	/* 3. ALGORITMO DE RENDERIZADO EN COLUMNAS DINÁMICAS */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja la lista de pares clave-valor dentro de los límites del área asignada.
	 * 
	 * <p>
	 * <b>Funcionamiento del Algoritmo:</b>
	 * <ol>
	 * <li>Itera sobre las instancias de {@link Info} contenidas en el mapa.</li>
	 * <li>Mide la altura del texto y acumula el desplazamiento vertical en
	 * {@code y}.</li>
	 * <li>Si {@code y} sobrepasa el límite inferior del área, reinicia {@code y} a
	 * la parte superior y desplaza {@code x} hacia la derecha según el ancho máximo
	 * de la columna anterior.</li>
	 * <li>Dibuja la etiqueta (label) y a su derecha el valor correspondiente con su
	 * margen.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintar(final Graphics2D g) {
		if ((this.lista == null) || this.lista.isEmpty()) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		g.setFont(this.fuenteCaja);

		int x = this.AREA.x;
		int y = this.AREA.y;
		int maxAnchoColumnaActual = 0;
		int desplazamientoTotalX = 0;

		for (final Info info : this.lista.values()) {
			if (info == null) {
				continue;
			}

			final int altoLabel = this.calcularAltoPixeles(g, info, true);
			y += altoLabel;

			// Salto de columna si sobrepasa la altura máxima permitida
			if (y > (this.AREA.y + this.AREA.height)) {
				y = this.AREA.y + altoLabel;
				desplazamientoTotalX += maxAnchoColumnaActual + this.margenLabelValor;
				x = this.AREA.x + desplazamientoTotalX;
				maxAnchoColumnaActual = 0;
			}

			// 1. Dibujar etiqueta (Nombre de la estadística)
			DibujoDebug.dibujarString(g, info.getTexto(), x, y, this.colorLabels);

			final int auxAnchoLabel = this.calcularAnchoPixeles(g, info, true);
			final int auxAnchoValores = this.calcularAnchoPixeles(g, info, false);
			final int anchoParFila = auxAnchoLabel + auxAnchoValores;

			// Registrar el ancho máximo para calcular el salto de columna
			if (maxAnchoColumnaActual < anchoParFila) {
				maxAnchoColumnaActual = anchoParFila;
			}

			// 2. Dibujar valor correspondiente al lado de la etiqueta
			DibujoDebug.dibujarString(g, info.getValor(), x + auxAnchoLabel + this.margenLabelValor, y,
					this.colorValores);

			y += ESPACIADO_INTERLINEADO;
		}

		g.setFont(fuenteOriginal);
	}

	/***/
	/* ========================================================================= */
	/* 4. MEDIDORES DE TEXTO (ZERO-GC STRING METRICS) */
	/* ========================================================================= */
	/***/

	/**
	 * Calcula la altura en píxeles del texto según la fuente activa en el contexto
	 * gráfico.
	 */
	private int calcularAltoPixeles(final Graphics2D g, final Info i, final boolean label) {
		final String texto = label ? i.getTexto() : i.getValor();
		return Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, (texto != null) ? texto : "");
	}

	/**
	 * Calcula el ancho en píxeles del texto según la fuente activa en el contexto
	 * gráfico.
	 */
	private int calcularAnchoPixeles(final Graphics2D g, final Info i, final boolean label) {
		final String texto = label ? i.getTexto() : i.getValor();
		return Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, (texto != null) ? texto : "");
	}

	/***/
	/* ========================================================================= */
	/* 5. GESTIÓN DE DATOS Y CONFIGURACIÓN */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza la referencia del mapa de datos a renderizar.
	 * 
	 * @param lista Diccionario con los objetos {@link Info} precargados.
	 */
	public void actualizarLista(final HashMap<String, Info> lista) {
		this.lista = lista;
	}

	/**
	 * Obtiene el objeto de información asociado a una clave estadística.
	 * 
	 * @param clave Identificador del atributo (ej: "Ataque", "Municion").
	 * @return Instancia de {@link Info} encontrada, o {@code null} si no existe.
	 */
	public Info getInfo(final String clave) {
		return (this.lista != null) ? this.lista.get(clave) : null;
	}

	public Rectangle getArea() {
		return this.AREA;
	}

	public void setColorLabels(final Color colorLabels) {
		this.colorLabels = colorLabels;
	}

	public void setColorValores(final Color colorValores) {
		this.colorValores = colorValores;
	}
}