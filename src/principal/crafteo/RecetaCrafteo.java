package principal.crafteo;

import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.items.Item;
import principal.inventario.Inventario;
import principal.mapa.Mundo;

/**
 * Definición inmutable de receta con estación física, materiales, requisitos de
 * atributos RPG y condiciones ambientales/celestes del mundo (Zero-GC / O(1)).
 * 
 * @version 2.2 (Vanilla Java 8 - World & Celestial Requirements)
 */
public class RecetaCrafteo {

	private final String idReceta;
	private final String nombreVisible;
	private final String nombreMinusculas;
	private final EstacionCrafteo estacionRequerida;
	private final Ingrediente[] ingredientes;
	private final Item itemResultado;

	// === REQUISITOS DE ATRIBUTOS RPG ===
	private final int fuerzaRequerida;
	private final int agilidadRequerida;
	private final int inteligenciaRequerida;

	// === CONDICIÓN ESPECIAL DEL MUNDO / EVENTO (BLOQUE 4+) ===
	private final CondicionEspecialReceta condicionEspecial;
	private final String descripcionCondicionEspecial;

	/**
	 * Constructor maestro con requisitos de atributos y condición especial del
	 * mundo
	 */
	public RecetaCrafteo(final String idReceta, final String nombreVisible, final EstacionCrafteo estacionRequerida,
			final Ingrediente[] ingredientes, final Item itemResultado, final int fuerzaReq, final int agilidadReq,
			final int inteligenciaReq, final CondicionEspecialReceta condicionEspecial,
			final String descripcionCondicionEspecial) {

		this.idReceta = idReceta;
		this.nombreVisible = nombreVisible;
		this.estacionRequerida = (estacionRequerida != null) ? estacionRequerida : EstacionCrafteo.MANUAL;
		this.ingredientes = (ingredientes != null) ? ingredientes : new Ingrediente[0];
		this.itemResultado = itemResultado;
		this.fuerzaRequerida = Math.max(0, fuerzaReq);
		this.agilidadRequerida = Math.max(0, agilidadReq);
		this.inteligenciaRequerida = Math.max(0, inteligenciaReq);
		this.condicionEspecial = condicionEspecial;
		this.descripcionCondicionEspecial = descripcionCondicionEspecial;
		this.nombreMinusculas = normalizarSinAcentos(nombreVisible);
		;
	}

	/** Constructor con atributos pero sin condición especial */
	public RecetaCrafteo(final String idReceta, final String nombreVisible, final EstacionCrafteo estacionRequerida,
			final Ingrediente[] ingredientes, final Item itemResultado, final int fuerzaReq, final int agilidadReq,
			final int inteligenciaReq) {
		this(idReceta, nombreVisible, estacionRequerida, ingredientes, itemResultado, fuerzaReq, agilidadReq,
				inteligenciaReq, null, null);
	}

	/** Constructor estándar sin atributos ni condición especial */
	public RecetaCrafteo(final String idReceta, final String nombreVisible, final EstacionCrafteo estacionRequerida,
			final Ingrediente[] ingredientes, final Item itemResultado) {
		this(idReceta, nombreVisible, estacionRequerida, ingredientes, itemResultado, 0, 0, 0, null, null);
	}

	/**
	 * Valida estación física, atributos RPG, materiales y condición especial del
	 * mundo.
	 */
	public boolean cumpleRequisitos(final Inventario inventario, final Jugador jugador,
			final GestorCrafteo gestorCrafteo) {
		if ((inventario == null) || (this.itemResultado == null)) {
			return false;
		}

		// 1. Validar estación física disponible
		if ((gestorCrafteo != null) && !gestorCrafteo.isEstacionDisponible(this.estacionRequerida)) {
			return false;
		}

		// 2. Validar atributos RPG
		if (jugador != null) {
			if (jugador.getFuerzaTotal() < this.fuerzaRequerida) {
				return false;
			}
			if (jugador.getAgilidadTotal() < this.agilidadRequerida) {
				return false;
			}
			if (jugador.getInteligenciaTotal() < this.inteligenciaRequerida) {
				return false;
			}
		}

		// 3. Validar condición especial del mundo / clima / astronomía
		if (this.condicionEspecial != null) {
			final Mundo mundo = (jugador != null) ? jugador.getMundo() : null;
			if (!this.condicionEspecial.seCumple(jugador, mundo)) {
				return false;
			}
		}

		// 4. Validar materiales
		return this.tieneMaterialesSuficientes(inventario);
	}

	public boolean puedeCraftear(final Inventario inventario) {
		return this.tieneMaterialesSuficientes(inventario);
	}

	public boolean tieneMaterialesSuficientes(final Inventario inventario) {
		if (inventario == null) {
			return false;
		}
		for (int i = 0; i < this.ingredientes.length; i++) {
			final Ingrediente ing = this.ingredientes[i];
			final int disponible = inventario.contarItemGenericoTotal(ing.getCodModeloItem());
			if (disponible < ing.getCantidad()) {
				return false;
			}
		}
		return true;
	}

	public boolean craftear(final Inventario inventario) {
		if (!this.tieneMaterialesSuficientes(inventario)) {
			return false;
		}

		for (int i = 0; i < this.ingredientes.length; i++) {
			final Ingrediente ing = this.ingredientes[i];
			inventario.extraerItemGenerico(ing.getCodModeloItem(), ing.getCantidad());
		}

		return inventario.agregarObjeto((Item) this.itemResultado.copiar());
	}

	public String getIdReceta() {
		return this.idReceta;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public EstacionCrafteo getEstacionRequerida() {
		return this.estacionRequerida;
	}

	public Ingrediente[] getIngredientes() {
		return this.ingredientes;
	}

	public Item getItemResultado() {
		return this.itemResultado;
	}

	public String getNombreMinusculas() {
		return this.nombreMinusculas;
	}

	public int getFuerzaRequerida() {
		return this.fuerzaRequerida;
	}

	public int getAgilidadRequerida() {
		return this.agilidadRequerida;
	}

	public int getInteligenciaRequerida() {
		return this.inteligenciaRequerida;
	}

	public boolean tieneRequisitosAtributos() {
		return (this.fuerzaRequerida > 0) || (this.agilidadRequerida > 0) || (this.inteligenciaRequerida > 0);
	}

	public boolean tieneCondicionEspecial() {
		return this.condicionEspecial != null;
	}

	public CondicionEspecialReceta getCondicionEspecial() {
		return this.condicionEspecial;
	}

	public String getDescripcionCondicionEspecial() {
		return this.descripcionCondicionEspecial;
	}

	/**
	 * Convierte a minúsculas y reemplaza vocales acentuadas por vocales planas una
	 * sola vez al arrancar (Zero-GC en caliente).
	 */
	private static String normalizarSinAcentos(final String texto) {
		if (texto == null) {
			return "";
		}

		final String lower = texto.toLowerCase();
		final StringBuilder sb = new StringBuilder(lower.length());

		for (int i = 0; i < lower.length(); i++) {
			final char c = lower.charAt(i);
			switch (c) {
			case 'á':
			case 'à':
			case 'ä':
			case 'â':
				sb.append('a');
				break;
			case 'é':
			case 'è':
			case 'ë':
			case 'ê':
				sb.append('e');
				break;
			case 'í':
			case 'ì':
			case 'ï':
			case 'î':
				sb.append('i');
				break;
			case 'ó':
			case 'ò':
			case 'ö':
			case 'ô':
				sb.append('o');
				break;
			case 'ú':
			case 'ù':
			case 'ü':
			case 'û':
				sb.append('u');
				break;
			default:
				sb.append(c);
				break;
			}
		}

		return sb.toString();
	}
}