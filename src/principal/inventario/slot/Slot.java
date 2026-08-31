package principal.inventario.slot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotManager;
import principal.utilidades.Render2D;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

/**
 * Representa una celda o casilla interactiva dentro de cualquier interfaz de
 * usuario (IGU) capaz de contener, validar y renderizar un {@link Item}.
 * 
 * <p>
 * <b>Principios de Diseño y Rendimiento:</b>
 * <ul>
 * <li><b>Zero-GC en Render/Update:</b> No instancia objetos temporales (como
 * {@code Rectangle} o {@code Point}) en los métodos que se ejecutan a 60 APS o
 * por cada fotograma.</li>
 * <li><b>Fuentes Estáticas Cacheadas:</b> Mantiene instancias únicas de
 * {@link Font} para evitar el alto costo de {@code Font.deriveFont()} durante
 * el renderizado del bucle de juego.</li>
 * <li><b>Polimorfismo de Casilla (Open/Closed Principle):</b> Las reglas de
 * inserción y extracción están delegadas a {@link #puedeAceptar(Item)} y
 * {@link #puedeExtraer()}, permitiendo crear casillas de armaduras, tiendas o
 * crafteo sin acoplar lógica condicional en los gestores.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Item
 * @see SlotManager
 */
public class Slot {

	/***/
	/* ========================================================================= */
	/* 1. RECURSOS GRÁFICOS Y CONSTANTES (GC FRIENDLY) */
	/* ========================================================================= */
	/* Reutilizamos instancias inmutables de fuentes para no presionar al GC */
	/* al medir cadenas y dibujar metadatos (cantidades, munición, tooltips). */
	/***/
	private static final Font FUENTE_CANTIDAD = new Font(Font.SANS_SERIF, Font.PLAIN, 5);
	private static final Font FUENTE_MUNICION = new Font(Font.SANS_SERIF, Font.PLAIN, 4);
	private static final Font FUENTE_TOOLTIP = new Font(Font.SANS_SERIF, Font.PLAIN, 5);

	protected static final int MARGEN_ESPACIADO_DEFECTO = 2;

	/***/
	/* ========================================================================= */
	/* 2. ESTADO Y LÍMITES ESPACIALES */
	/* ========================================================================= */
	/***/
	protected final Rectangle AREA;
	protected final int MARGEN_ESPACIADO;
	protected Item item;
	protected boolean apuntado;
	protected int valorPrioridad; // A menor valor numérico, mayor prioridad de interacción/render

	/**
	 * Construye un slot con límites definidos y un ítem inicial precargado.
	 * 
	 * @param area Rectángulo con la posición (X, Y) y dimensiones (ancho, alto) en
	 *             pantalla.
	 * @param item Ítem inicial que ocupará la casilla (puede ser {@code null}).
	 */
	public Slot(final Rectangle area, final Item item) {
		this.AREA = area;
		this.item = item;
		this.MARGEN_ESPACIADO = MARGEN_ESPACIADO_DEFECTO;
	}

	/**
	 * Construye un slot vacío con límites definidos.
	 * 
	 * @param area Rectángulo con la posición (X, Y) y dimensiones (ancho, alto) en
	 *             pantalla.
	 */
	public Slot(final Rectangle area) {
		this(area, null);
	}

	/**
	 * Construye un slot vacío en una coordenada específica utilizando las
	 * dimensiones estándar provistas por {@link SlotManager#getLadoSlots()}.
	 * 
	 * @param x Posición X en píxeles sobre la pantalla.
	 * @param y Posición Y en píxeles sobre la pantalla.
	 */
	public Slot(final int x, final int y) {
		this(new Rectangle(x, y, SlotManager.getLadoSlots(), SlotManager.getLadoSlots()), null);
	}

	/***/
	/* ========================================================================= */
	/* 3. CONTRATO POLIMÓRFICO DE REGLAS DE NEGOCIO (OCP) */
	/* ========================================================================= */
	/* Estos métodos eliminan la necesidad de comprobar 'instanceof' en los */
	/* gestores. Cada subclase define sus propias reglas de acceso y admisión. */
	/***/

	/**
	 * Valida si un ítem cumple con las condiciones para ingresar en esta casilla.
	 * 
	 * <p>
	 * <b>Nota de Sobrescritura:</b> Las subclases de equipamiento (ej:
	 * {@code SlotArma}, {@code SlotCasco}) o casillas con restricciones deben
	 * sobrescribir este método para validar el tipo o requisitos del ítem antes de
	 * permitir la transferencia.
	 * </p>
	 * 
	 * @param itemAColocar Ítem candidato que se desea depositar.
	 * @return {@code true} si el ítem es admitido; {@code false} si es rechazado o
	 *         {@code null}.
	 */
	public boolean puedeAceptar(final Item itemAColocar) {
		return itemAColocar != null;
	}

	/**
	 * Valida si el ítem contenido puede ser extraído o arrastrado hacia el puntero.
	 * 
	 * <p>
	 * <b>Casos de uso:</b> Casillas de solo lectura (como el resultado de una mesa
	 * de crafteo antes de pagar materiales) o casillas de tiendas que requieran
	 * transacciones.
	 * </p>
	 * 
	 * @return {@code true} si el ítem puede ser retirado libremente; {@code false}
	 *         en caso contrario.
	 */
	public boolean puedeExtraer() {
		return true;
	}

	/***/
	/* ========================================================================= */
	/* 4. ACTUALIZACIÓN LÓGICA (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado de hover/apuntado mediante la posición del ratón y
	 * verifica si el ítem contenido ha sido eliminado lógicamente del juego.
	 * 
	 * @param raton Instancia del controlador de entrada del ratón.
	 */
	public void actualizar(final Raton raton) {
		if ((raton != null) && raton.getRectanguloPosicionEscalado().intersects(this.AREA)) {
			this.apuntado = true;
		} else {
			this.apuntado = false;
		}
		this.verificarEliminacion();
	}

	/**
	 * Variante de actualización para slots reflejados en el HUD inferior (IGU),
	 * evaluando colisiones sobre un área alternativa sin alterar el área base del
	 * inventario.
	 * 
	 * @param raton   Instancia del controlador de entrada del ratón.
	 * @param areaHUD Límites espaciales temporales del slot en el HUD.
	 */
	public void actualizarIGU(final Raton raton, final Rectangle areaHUD) {
		if ((raton != null) && raton.getRectanguloPosicionEscalado().intersects(areaHUD)) {
			this.apuntado = true;
		} else {
			this.apuntado = false;
		}
		this.verificarEliminacion();
	}

	/**
	 * Saneamiento reactivo: Si el ítem contenido fue marcado como eliminado por
	 * alguna mecánica externa (ej: arma destruida, consumible agotado), se
	 * desvincula del slot inmediatamente para evitar estados corruptos.
	 */
	protected void verificarEliminacion() {
		if (this.contieneItem() && this.item.estaEliminado()) {
			this.eliminarObjeto();
		}
	}

	/***/
	/* ========================================================================= */
	/* 5. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Renderizado completo del slot: Dibuja el contenedor de fondo y el ítem (Capa
	 * 1).
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintar(final Graphics2D g) {
		this.pintarArea(g, this.AREA);
		this.pintarObjeto(g, this.AREA);
	}

	/**
	 * Renderizado completo del slot sobre un área personalizada (utilizado para
	 * proyección en HUD).
	 * 
	 * @param g    Contexto gráfico 2D activo.
	 * @param area Límites espaciales donde se proyectará la casilla.
	 */
	public void pintar(final Graphics2D g, final Rectangle area) {
		this.pintarArea(g, area);
		this.pintarObjeto(g, area);
	}

	/**
	 * Dibuja únicamente el marco y fondo del slot sin su contenido. Utilizado para
	 * crear el efecto visual de "casilla vacía" mientras el ítem es arrastrado.
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintarSoloSlot(final Graphics2D g) {
		this.pintarArea(g, this.AREA);
	}

	/**
	 * Renderizado de información flotante (Tooltip) en la Capa 2 de la IGU. Se debe
	 * ejecutar en una pasada posterior al dibujo de todas las ventanas.
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintarTooltip(final Graphics2D g) {
		if (this.apuntado && this.contieneItem()) {
			final Font fuenteOriginal = g.getFont();
			g.setFont(FUENTE_TOOLTIP);
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipItem(g, this.item);
			g.setFont(fuenteOriginal);
		}
	}

	/**
	 * Dibuja el fondo de la celda y resalta el contorno si el ratón está encima.
	 * 
	 * @param g    Contexto gráfico 2D.
	 * @param area Rectángulo donde se dibujará la base.
	 */
	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		Render2D.dibujarRectanguloRelleno(g, area, Inventario.BLANCO_TRANSPARENTE);
		if (this.apuntado) {
			Render2D.dibujarRectanguloContorno(g, area, Color.YELLOW);
		}
	}

	/**
	 * Dibuja el sprite del ítem contenido junto con sus metadatos superpuestos
	 * (acumulación de consumibles o contador de munición para armas de fuego).
	 * 
	 * @param g    Contexto gráfico 2D.
	 * @param area Límites de la casilla donde se acomodará el ítem.
	 */
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item == null) {
			return;
		}

		// 1. Dibujar textura del ítem con el offset de margen interno
		this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);

		// 2. Metadatos de Consumible (Cantidad de stack en esquina superior izquierda)
		if (this.item instanceof Consumible) {
			final Font fuenteOriginal = g.getFont();
			g.setFont(FUENTE_CANTIDAD);

			Render2D.dibujarRectanguloRelleno(g, area.x, area.y, 6, 6, Color.LIGHT_GRAY);
			Render2D.dibujarString(g, String.valueOf(((Consumible) this.item).getCantidad()), area.x, area.y + 6,
					Color.BLACK);

			g.setFont(fuenteOriginal);
		}
		// 3. Metadatos de Arma de Fuego (Munición restante e ícono de bala en esquina
		// inferior)
		else if (this.item instanceof Pistola) {
			final Font fuenteOriginal = g.getFont();
			g.setFont(FUENTE_MUNICION);

			final String cantidadBalas = String.valueOf(((Pistola) this.item).getMunicion().getCantidad());
			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidadBalas);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);

			Render2D.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - altoTexto - 1, 11, 6,
					Color.LIGHT_GRAY);
			Render2D.dibujarString(g, cantidadBalas, area.x, (area.y + area.height) - (altoTexto / 2), Color.BLACK);
			Render2D.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x + anchoTexto,
					(area.y + area.height) - altoTexto);

			g.setFont(fuenteOriginal);
		}
	}

	/***/
	/* ========================================================================= */
	/* 6. DETECCIÓN DE INTERSECCIONES Y LÍMITES */
	/* ========================================================================= */
	/***/

	/**
	 * Verifica si la posición escalada del ratón interseca los límites del slot.
	 * 
	 * @param raton Instancia del controlador del ratón.
	 * @return {@code true} si hay colisión rectangular; {@code false} si no o si el
	 *         ratón es {@code null}.
	 */
	public boolean ratonIntersecta(final Raton raton) {
		if (raton == null) {
			return false;
		}
		return raton.getRectanguloPosicionEscalado().intersects(this.AREA);
	}

	/**
	 * Comprueba la colisión con un punto de coordenadas sin generar instancias en
	 * memoria (Zero-GC).
	 * 
	 * @param punto Coordenada escalar (X, Y) a evaluar.
	 * @return {@code true} si el punto está contenido dentro del slot;
	 *         {@code false} en caso contrario.
	 */
	public boolean intersecta(final Point punto) {
		if (punto == null) {
			return false;
		}
		return this.AREA.contains(punto.x, punto.y);
	}

	/***/
	/* ========================================================================= */
	/* 7. GETTERS, SETTERS Y MUTADORES DE ESTADO */
	/* ========================================================================= */
	/***/

	public void establecerObjeto(final Item obj) {
		this.item = obj;
	}

	public void eliminarObjeto() {
		this.item = null;
	}

	public boolean contieneItem() {
		return this.item != null;
	}

	public Item getItem() {
		return this.item;
	}

	/**
	 * Obtiene el ítem contenido casteado a {@link Arrojadizo} si corresponde.
	 * 
	 * @return El objeto casteado a {@link Arrojadizo}, o {@code null} si está vacío
	 *         o es otro tipo de ítem.
	 */
	public Arrojadizo getItemArrojadizo() {
		if (this.item instanceof Arrojadizo) {
			return (Arrojadizo) this.item;
		}
		return null;
	}

	public boolean estaApuntado() {
		return this.apuntado;
	}

	public void setX(final int x) {
		this.AREA.x = x;
	}

	public void setY(final int y) {
		this.AREA.y = y;
	}

	public void setAncho(final int ancho) {
		this.AREA.width = ancho;
	}

	public void setAlto(final int alto) {
		this.AREA.height = alto;
	}

	public int getX() {
		return this.AREA.x;
	}

	public int getY() {
		return this.AREA.y;
	}

	public int getAncho() {
		return this.AREA.width;
	}

	public int getAlto() {
		return this.AREA.height;
	}

	public Rectangle getArea() {
		return this.AREA;
	}

	public void setValorPrioridad(final int valor) {
		this.valorPrioridad = valor;
	}

	public int getValorPrioridad() {
		return this.valorPrioridad;
	}
}