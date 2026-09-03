package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Consumible extends Item {

	private static final long serialVersionUID = 504856170135227071L;

	protected final String nombre;
	protected final String codigoModelo;
	protected final TexturaItem texturaInv;
	protected final TexturaItem texturaMapa;
	protected final int limite;
	private int cantidad;

	public Consumible(final int x, final int y, final int cantidad, final String codModelo, final String nombre,
			final TexturaItem texturaInv, final TexturaItem texturaMapa, final int limite) {
		super(x, y);
		this.codigoModelo = (codModelo != null) ? codModelo : "";
		this.nombre = (nombre != null) ? nombre : this.codigoModelo;
		this.texturaInv = texturaInv;
		this.texturaMapa = texturaMapa;
		this.limite = Math.max(1, limite);
		this.establecerCantidad(cantidad);
	}

	public Consumible(final int cantidad, final String codModelo, final String nombre, final TexturaItem texturaInv,
			final TexturaItem texturaMapa, final int limite) {
		this(0, 0, cantidad, codModelo, nombre, texturaInv, texturaMapa, limite);
	}

	// Sobrecarga de compatibilidad transitoria mientras se migran las demás clases
	// hijas
	public Consumible(final int x, final int y, final int cantidad, final String codModelo) {
		this(x, y, cantidad, codModelo, codModelo, resolverTexturaInvDefecto(codModelo),
				resolverTexturaMapaDefecto(codModelo), resolverLimiteDefecto(codModelo));
	}

	public Consumible(final int cantidad, final String codModelo) {
		this(0, 0, cantidad, codModelo);
	}

	public void establecerCantidad(final int cantidad) {
		if (cantidad > this.limite) {
			this.cantidad = this.limite;
		} else {
			this.cantidad = Math.max(0, cantidad);
			if (this.cantidad == 0) {
				this.eliminar();
			}
		}
	}

	public int agregarCantidad(final int cant) {
		int resto = 0;
		if ((this.cantidad + cant) > this.limite) {
			resto = (this.cantidad + cant) - this.limite;
			this.cantidad = this.limite;
		} else {
			this.cantidad += cant;
		}
		return resto;
	}

	public void reducirCantidad(final int cant) {
		this.cantidad = Math.max(0, this.cantidad - cant);
		if (this.cantidad == 0) {
			this.eliminar();
		}
	}

	public abstract void consumir(final Criatura c);

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		Render2D.dibujarImagen(g, this.getTexturaInventario(), x, y);
	}

	public String getCodigoModelo() {
		return this.codigoModelo;
	}

	public int getCantidad() {
		return this.cantidad;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return (this.texturaInv != null) ? Globales.GESTOR_TEXTURAS.get(this.texturaInv)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public BufferedImage getTextura() {
		return (this.texturaMapa != null) ? Globales.GESTOR_TEXTURAS.get(this.texturaMapa)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public int getLimite() {
		return this.limite;
	}

	@Override
	public boolean esSolido() {
		return false;
	}

	@Override
	public int getTipoItem() {
		return Item.COD_ITEM_CONSUMIBLE;
	}

	@Override
	public int getAncho() {
		return 16;
	}

	@Override
	public int getAlto() {
		return 16;
	}

	@Override
	public String getNombre() {
		return this.nombre;
	}

	@Override
	public String exportarTipoItem() {
		return "Consumible";
	}

	public static Consumible crearConsumible(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : "";

		if (codModelo.equals("Pocion Vida Menor")) {
			return PocionVidaMenor.crearDesdeJson(json);
		}
		if (codModelo.startsWith("Caja Municion") || codModelo.startsWith("Cartuchos")
				|| codModelo.startsWith("Cinta")) {
			return CajaMunicion.crearDesdeJson(json);
		} else if (codModelo.equals("Madera") || codModelo.equals("Piedra")) {
			return RecursoMaterial.crearDesdeJson(json);
		}

		return null;
	}

	// --- Resolutores transitorios para desacoplar de ListaModelosItem ---
	private static TexturaItem resolverTexturaInvDefecto(final String cod) {
		if (cod == null) {
			return TexturaItem.POCION_ROJA_INV;
		}
		if (cod.contains("Pocion") && cod.contains("Vida")) {
			return TexturaItem.POCION_ROJA_INV;
		}
		if (cod.contains("Pocion")) {
			return TexturaItem.POCION_AZUL_INV;
		}
		if (cod.contains("Municion") || cod.contains("Cartuchos") || cod.contains("Cinta")) {
			return TexturaItem.CAJA_MUNICION_INV;
		}
		if (cod.contains("Granada")) {
			return TexturaItem.GRANADA_T1_INV;
		}
		if (cod.equals("Madera")) {
			return TexturaItem.BOTAS_CUERO_INV;
		}
		if (cod.equals("Piedra")) {
			return TexturaItem.ANILLO_PLATA_INV;
		}
		return TexturaItem.POCION_ROJA_INV;
	}

	private static TexturaItem resolverTexturaMapaDefecto(final String cod) {
		if (cod == null) {
			return TexturaItem.POCION_ROJA_MAPA;
		}
		if (cod.contains("Pocion") && cod.contains("Vida")) {
			return TexturaItem.POCION_ROJA_MAPA;
		}
		if (cod.contains("Pocion")) {
			return TexturaItem.POCION_AZUL_MAPA;
		}
		if (cod.contains("Municion") || cod.contains("Cartuchos") || cod.contains("Cinta")) {
			return TexturaItem.CAJA_MUNICION_MAPA;
		}
		if (cod.contains("Granada")) {
			return TexturaItem.GRANADA_T1_MAPA;
		}
		if (cod.equals("Madera")) {
			return TexturaItem.BOTAS_CUERO_MAPA;
		}
		if (cod.equals("Piedra")) {
			return TexturaItem.POCION_AZUL_MAPA;
		}
		return TexturaItem.POCION_ROJA_MAPA;
	}

	private static int resolverLimiteDefecto(final String cod) {
		if (cod == null) {
			return 99;
		}
		if (cod.equals("Madera") || cod.equals("Piedra")) {
			return 999;
		}
		if (cod.contains("Municion") || cod.contains("Cartuchos")) {
			return 150;
		}
		return 99;
	}
}