package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.inventario.CajaInfo;
import principal.inventario.Info;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

/**
 * Slot de equipamiento especializado en la gestión del arma activa del jugador.
 * 
 * <p>
 * <b>Arquitectura y Optimización Zero-GC:</b>
 * </p>
 * <ul>
 * <li><b>DTOs Preasignados en Memoria:</b> Mantiene instancias persistentes de
 * {@link Info} ({@code infoAtaque}, {@code infoAlcance}, etc.) y de
 * {@link HashMap}. En lugar de instanciar nuevos objetos en cada disparo o
 * cambio de arma, muta los valores de texto internamente (<i>In-Place Value
 * Mutation</i>).</li>
 * <li><b>Sincronización en Dos Fases:</b>
 * <ul>
 * <li><i>Fase Estructural (Evento):</i> {@link #actualizarLista()} solo se
 * ejecuta al equipar o desequipar un arma, actualizando el mapa de la
 * {@link CajaInfo}.</li>
 * <li><i>Fase Dinámica (Tick a 60 APS):</i>
 * {@link #sincronizarValoresArma(Arma)} solo actualiza el conteo de munición en
 * caliente si el arma equipada es de fuego.</li>
 * </ul>
 * </li>
 * <li><b>Icono Guía (Placeholder):</b> Si no hay arma equipada, proyecta la
 * silueta translúcida del tipo de equipamiento (arma) heredada de
 * {@link SlotEquipamiento}.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see SlotEquipamiento
 * @see Arma
 * @see CajaInfo
 */
public class SlotArma extends SlotEquipamiento {

	/***/
	/* ========================================================================= */
	/* 1. CONSTANTES GRÁFICAS Y RECURSOS */
	/* ========================================================================= */
	/***/
	private static final Font FUENTE_MUNICION = new Font(Font.SANS_SERIF, Font.PLAIN, 4);
	private static final String RUTA_LOGO_ARMA = "/imagenes/objetos/gun16x12_transparente.png";

	// Claves estándar para el mapa de información visual
	private static final String CLAVE_ATAQUE = "Ataque";
	private static final String CLAVE_ALCANCE = "Alcance";
	private static final String CLAVE_PENETRANTE = "Penetrante";
	private static final String CLAVE_MUNICION = "Municion";

	/***/
	/* ========================================================================= */
	/* 2. ESTRUCTURAS DE DATOS CACHEADAS (ZERO-GC) */
	/* ========================================================================= */
	/* Estas instancias se crean una sola vez en el constructor. Prohibido hacer */
	/* 'new Info()' o 'new HashMap<>()' durante la ejecución del juego. */
	/***/
	protected final HashMap<String, Info> lista;
	protected final CajaInfo cajaInfo;

	private final Info infoAtaque;
	private final Info infoAlcance;
	private final Info infoPenetrante;
	private final Info infoMunicion;

	/**
	 * Construye el slot de arma cargando su silueta translúcida y preasignando las
	 * estructuras de información para el panel de estadísticas.
	 * 
	 * @param area     Rectángulo con los límites y posición del slot en pantalla.
	 * @param cajaInfo Referencia a la caja de interfaz donde se listan los
	 *                 atributos del arma.
	 */
	public SlotArma(final Rectangle area, final CajaInfo cajaInfo) {
		super(area, Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(RUTA_LOGO_ARMA));

		this.cajaInfo = cajaInfo;
		this.lista = new HashMap<String, Info>();

		// Preasignación de instancias Info para mutación en caliente
		this.infoAtaque = new Info(CLAVE_ATAQUE, "0");
		this.infoAlcance = new Info(CLAVE_ALCANCE, "0");
		this.infoPenetrante = new Info(CLAVE_PENETRANTE, "false");
		this.infoMunicion = new Info(CLAVE_MUNICION, "0");
	}

	/***/
	/* ========================================================================= */
	/* 3. ACTUALIZACIÓN LÓGICA (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado de interacción del ratón y sincroniza en tiempo real los
	 * datos dinámicos del arma (ej: conteo de munición al disparar/recargar).
	 * 
	 * @param raton Instancia del controlador de entrada del ratón.
	 */
	@Override
	public void actualizar(final Raton raton) {
		super.actualizar(raton);
		if (this.item instanceof Arma) {
			this.sincronizarValoresArma((Arma) this.item);
		}
	}

	/**
	 * Sincroniza dinámicamente la munición del arma en cada ciclo lógico sin
	 * generar instancias de cadenas ni objetos intermedios.
	 * 
	 * @param arma Instancia del arma equipada actualmente.
	 */
	private void sincronizarValoresArma(final Arma arma) {
		if ((arma.getMunicion() != null) && this.lista.containsKey(CLAVE_MUNICION)) {
			this.infoMunicion.establecerValor(arma.getMunicion().toString());
		}
	}

	/***/
	/* ========================================================================= */
	/* 4. SINCRONIZACIÓN ESTRUCTURAL DE ESTADÍSTICAS (POR EVENTO) */
	/* ========================================================================= */
	/***/

	/**
	 * Reconstruye el diccionario de estadísticas únicamente cuando cambia el ítem
	 * alojado. Muta las instancias de {@link Info} existentes y notifica a la
	 * {@link CajaInfo}.
	 */
	private void actualizarLista() {
		this.lista.clear();

		if (this.item instanceof Arma) {
			final Arma arma = (Arma) this.item;

			// Mutamos los valores de los DTOs preasignados
			this.infoAtaque.establecerValor(String.valueOf(arma.getAtaque()));
			this.infoAlcance.establecerValor(String.valueOf(arma.getAlcance()));
			this.infoPenetrante.establecerValor(String.valueOf(arma.esPenetrante()));

			this.lista.put(CLAVE_ATAQUE, this.infoAtaque);
			this.lista.put(CLAVE_ALCANCE, this.infoAlcance);
			this.lista.put(CLAVE_PENETRANTE, this.infoPenetrante);

			if (arma.getMunicion() != null) {
				this.infoMunicion.establecerValor(arma.getMunicion().toString());
				this.lista.put(CLAVE_MUNICION, this.infoMunicion);
			}
		}

		if (this.cajaInfo != null) {
			this.cajaInfo.actualizarLista(this.lista);
		}
	}

	/***/
	/* ========================================================================= */
	/* 5. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja el arma equipada o la silueta translúcida en caso de estar desarmado.
	 * Si el arma es una {@link Pistola}, renderiza la insignia con la munición
	 * actual.
	 * 
	 * @param g    Contexto gráfico 2D activo.
	 * @param area Límites de la casilla donde se dibujará el arma o placeholder.
	 */
	@Override
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item != null) {
			// 1. Dibujar el sprite del arma con el margen interno del slot
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);

			// 2. Si es arma de fuego con cargador, renderizar medidor de munición
			if (this.item instanceof Pistola) {
				final Pistola pistola = (Pistola) this.item;
				if (pistola.getMunicion() != null) {
					final Font fuenteOriginal = g.getFont();
					g.setFont(FUENTE_MUNICION);

					final String cantidadBalas = String.valueOf(pistola.getMunicion().getCantidad());
					final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g,
							cantidadBalas);
					final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);

					DibujoDebug.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - altoTexto - 1, 11, 6,
							Color.LIGHT_GRAY);
					DibujoDebug.dibujarString(g, cantidadBalas, area.x, (area.y + area.height) - (altoTexto / 2),
							Color.BLACK);
					DibujoDebug.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x + anchoTexto,
							(area.y + area.height) - altoTexto);

					g.setFont(fuenteOriginal);
				}
			}
		} else if (this.logo != null) {
			// Si no hay arma equipada, dibujar la silueta/logo del slot
			DibujoDebug.dibujarImagen(g, this.logo, area.x + 1, area.y + 5);
		}
	}

	/***/
	/* ========================================================================= */
	/* 6. CONTRATOS POLIMÓRFICOS Y ADMISIÓN */
	/* ========================================================================= */
	/***/

	/**
	 * Regla de negocio polimórfica para el sistema de
	 * {@link principal.utilidades.inventario.ItemPuntero}: Solo admite ítems que
	 * extiendan de {@link Arma}.
	 * 
	 * @param itemAColocar Ítem candidato a ser equipado en esta casilla.
	 * @return {@code true} si es una instancia de {@link Arma}; {@code false} si es
	 *         nulo u otro ítem.
	 */
	@Override
	public boolean puedeAceptar(final Item itemAColocar) {
		return (itemAColocar instanceof Arma);
	}

	/**
	 * Compatibilidad con la jerarquía anterior de {@link SlotEquipamiento}.
	 * 
	 * @param i Ítem a comprobar.
	 * @return {@code true} si es {@link Arma} o {@code null}.
	 */
	@Override
	public boolean validarAdmisionItem(final Item i) {
		return (i == null) || (i instanceof Arma);
	}

	/***/
	/* ========================================================================= */
	/* 7. MUTADORES DE ESTADO Y ACCESO */
	/* ========================================================================= */
	/***/

	@Override
	public void establecerObjeto(final Item obj) {
		super.establecerObjeto(obj);
		this.actualizarLista();
	}

	@Override
	public void eliminarObjeto() {
		super.eliminarObjeto();
		this.actualizarLista();
	}

	/**
	 * Obtiene el mapa cacheado con los atributos del arma.
	 * 
	 * @return Instancia reutilizable de {@link HashMap} con las estadísticas
	 *         activas.
	 */
	public HashMap<String, Info> getLista() {
		return this.lista;
	}
}