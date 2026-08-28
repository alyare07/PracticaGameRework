package principal.entes.objetos.items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Representa la base de todos los ítems recolectables del juego (armas,
 * pociones, consumibles, gemas).
 * <p>
 * <b>Mejoras Visuales y de Rendimiento:</b>
 * <ul>
 * <li><b>Levitación Armónica en el Suelo (Item Bobbing):</b> Los ítems tirados
 * en el mapa oscilan suavemente en el eje vertical mediante una función
 * senoidal, volviéndose muy visibles y atractivos de recoger (estilo
 * <i>Zelda</i> / <i>Minecraft</i>).</li>
 * <li><b>Sombra de Profundidad 3D:</b> Dibuja un óvalo translúcido fijo en la
 * base del suelo mientras el sprite del ítem flota por encima.</li>
 * <li><b>Zero-GC en Consultas de Área:</b> Reutiliza {@link #AREA_ENTE_RETORNO}
 * en {@link #getArea()}, eliminando miles de asignaciones
 * {@code new Rectangle()} por segundo.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public abstract class Item extends Objeto {

	private static final long serialVersionUID = -451309412394893821L;

	public static final int COD_ITEM_PORTABLE = 1;
	public static final int COD_ITEM_CONSUMIBLE = 2;

	/**
	 * Color negro translúcido pre-asignado para la sombra de contacto en el suelo
	 * (Zero-GC).
	 */
	private static final Color COLOR_SOMBRA_SUELO = new Color(0, 0, 0, 75);

	protected final ArrayList<String> LISTA_INFO;

	public Item(final int x, final int y) {
		super(x, y);
		this.LISTA_INFO = new ArrayList<String>();
	}

	public Item() {
		super(0, 0);
		this.LISTA_INFO = new ArrayList<String>();
	}

	// =========================================================================
	// === MÉTODOS ABSTRACTOS DE CONTRATO
	// =========================================================================

	public abstract BufferedImage getTexturaInventario();

	public abstract void pintarInventario(final Graphics2D g, final int x, final int y);

	public abstract int getTipoItem();

	public abstract String getNombre();

	public ArrayList<String> getInfo() {
		return this.LISTA_INFO;
	}

	protected void rellenarInfo(final ArrayList<String> listaInfo) {
	}

	// =========================================================================
	// === RENDERIZADO EN EL MUNDO CON LEVITACIÓN Y SOMBRA
	// =========================================================================

	/**
	 * Dibuja el ítem en el suelo con sombra de contacto y balanceo vertical
	 * sinusoidal continuo.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	@Override
	public void pintar(final Graphics2D g) {
		final int ancho = this.getAncho();
		final int alto = this.getAlto();

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO FUNCIONA EL ITEM BOBBING?
		 * --------------------------------------------------------------------- 1.
		 * DESFASAMIENTO POR COORDENADAS: - 'faseUnica = (this.x * 0.05) + (this.y *
		 * 0.05)' Si sueltas 3 pociones en el suelo juntas, cada una tiene una posición
		 * ligeramente distinta. Esto hace que no suban y bajen todas en bloque como
		 * robots, sino con un ritmo natural escalonado.
		 * 
		 * 2. ONDA SENOIDAL: - Math.sin(...) genera una oscilación suave entre -1.0 y
		 * +1.0. - Al multiplicar por 2.5, el ítem flota suavemente ±2.5 píxeles arriba
		 * y abajo.
		 * 
		 * 3. SOMBRA DE CONTACTO: La sombra se dibuja fija en el suelo ('this.y +
		 * alto'), mientras que el sprite se dibuja flotando ('this.y +
		 * offsetFlotacion'), creando una ilusión óptica de volumen y altura 3D
		 * inmediata.
		 * =====================================================================
		 */
		// 1. Cálculo de la onda senoidal de levitación
		final double faseUnica = (this.getPosicionX() * 0.05) + (this.getPosicionY() * 0.05);
		final int offsetFlotacion = (int) Math.round(Math.sin((Globales.animacion * 0.12) + faseUnica) * 1.5);

		// 2. Sombra de contacto elíptica en el suelo (Zero-GC)
		final int sombraAncho = Math.max(4, ancho - 4);
		final int sombraAlto = Math.max(2, alto / 4);
		final int sombraX = this.getPosicionXInt() + ((ancho - sombraAncho) / 2);
		final int sombraY = (this.getPosicionYInt() + alto) - (sombraAlto / 2);

		DibujoDebug.dibujarFiguraEllipseRefCamara(g, sombraX, sombraY, sombraAncho, sombraAlto, COLOR_SOMBRA_SUELO);

		// 3. Debug de colisiones si está activo
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}

		// 4. Sprite del ítem flotando suavemente en el eje vertical
		DibujoDebug.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(),
				this.getPosicionYInt() + offsetFlotacion);
	}

	// =========================================================================
	// === GESTIÓN DE ÁREA ZERO-GC
	// =========================================================================

	/**
	 * Retorna el delimitador rectangular del ítem reutilizando la estructura fija
	 * de Ente (0 allocations).
	 */
	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	// =========================================================================
	// === SERIALIZACIÓN JSON
	// =========================================================================

	protected abstract JSONObject exportarParaJSON();

	public abstract String exportarTipoItem();

	@SuppressWarnings("unchecked")
	public JSONObject getJsonItem() {
		final JSONObject datosItem = this.exportarParaJSON();
		final JSONObject item = new JSONObject();
		item.put("tipo", this.exportarTipoItem());
		item.put("entiti", datosItem);
		return item;
	}

	public static Item crearItemDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		Item i = null;
		final String tipoStr = json.get("tipo").toString();

		if (tipoStr.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Pistola.class))) {
			i = Pistola.crearDesdeJson((JSONObject) json.get("entiti"));
		} else if (tipoStr.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Consumible.class))) {
			i = Consumible.crearConsumible((JSONObject) json.get("entiti"));
		}
		return i;
	}
}