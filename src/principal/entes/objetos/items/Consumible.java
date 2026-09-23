package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.items.desplegables.KitFogata;
import principal.entes.objetos.items.desplegables.KitFogataAzul;
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
	// === SUBSISTEMA DE DESCOMPOSICIÓN ZERO-GC (BLOQUE 1) ===
	protected boolean perecedero = false;
	protected double horaCaducidad = -1.0;
	protected boolean podrido = false;

	// Caché para regenerar tooltip solo cuando cambia la hora entera (Zero-GC)
	private int ultimaHoraEnteraCaducidad = Integer.MIN_VALUE;
	private boolean ultimoEstadoPodrido = false;

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
		this.verificarCaducidad();
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
		return this.podrido ? (this.nombre + " (Podrido)") : this.nombre;
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
		}
		if (codModelo.equals("Madera") || codModelo.equals("Piedra")) {
			return RecursoMaterial.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.BayaSilvestre.CODIGO)
				|| codModelo.equals("BayaSilvestre")) {
			return principal.entes.objetos.items.comidas.BayaSilvestre.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.CarnePolloCruda.CODIGO)
				|| codModelo.equals("CarnePolloCruda")) {
			return principal.entes.objetos.items.comidas.CarnePolloCruda.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.CarnePolloCocida.CODIGO)
				|| codModelo.equals("CarnePolloCocida")) {
			return principal.entes.objetos.items.comidas.CarnePolloCocida.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.CuencoVacio.CODIGO)
				|| codModelo.equals("CuencoVacio")) {
			return principal.entes.objetos.items.comidas.CuencoVacio.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.CuencoAguaSucia.CODIGO)
				|| codModelo.equals("CuencoAguaSucia")) {
			return principal.entes.objetos.items.comidas.CuencoAguaSucia.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.comidas.CuencoAguaHervida.CODIGO)
				|| codModelo.equals("CuencoAguaHervida")) {
			return principal.entes.objetos.items.comidas.CuencoAguaHervida.crearDesdeJson(json);
		}

		if (codModelo.equals("Kit de Fogata") || codModelo.equals("KitFogata")) {
			return KitFogata.crearDesdeJson(json);
		}
		if (codModelo.equals(KitFogataAzul.COD_KIT_FOGATA_AZUL) || codModelo.equals("KitFogataAzul")) {
			return KitFogataAzul.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.desplegables.KitCarpa.COD_KIT_CARPA)
				|| codModelo.equals("KitCarpa")) {
			return principal.entes.objetos.items.desplegables.KitCarpa.crearDesdeJson(json);
		}
		if (codModelo.equals(principal.entes.objetos.items.desplegables.KitCama.COD_KIT_CAMA)
				|| codModelo.equals("KitCama")) {
			return principal.entes.objetos.items.desplegables.KitCama.crearDesdeJson(json);
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
			return TexturaItem.MADERA_INV;
		}
		if (cod.equals("Piedra")) {
			return TexturaItem.PIEDRA_INV;
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
			return TexturaItem.MADERA_MAPA;
		}
		if (cod.equals("Piedra")) {
			return TexturaItem.PIEDRA_MAPA;
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

	// =========================================================================
	// === LÓGICA DE CADUCIDAD PEREZOSA Y TOOLTIP ZERO-GC
	// =========================================================================

	public void configurarPerecedero(final double horasVidaUtil) {
		this.perecedero = true;
		if (Globales.GESTOR_ASTRONOMICO != null) {
			this.horaCaducidad = Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego() + horasVidaUtil;
		} else {
			this.horaCaducidad = horasVidaUtil;
		}
		this.podrido = false;
		this.forzarRefrescoTooltip();
	}

	public void establecerCaducidadDirecta(final double horaCaducidadExacta, final boolean estaPodrido) {
		this.perecedero = true;
		this.horaCaducidad = horaCaducidadExacta;
		this.podrido = estaPodrido;
		this.forzarRefrescoTooltip();
	}

	public void verificarCaducidad() {
		if (!this.perecedero || this.podrido) {
			return;
		}
		if (Globales.GESTOR_ASTRONOMICO != null) {
			if (Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego() >= this.horaCaducidad) {
				this.mutarAPodrido();
			}
		}
	}

	public void mutarAPodrido() {
		this.podrido = true;
		this.forzarRefrescoTooltip();
	}

	public void forzarRefrescoTooltip() {
		this.ultimaHoraEnteraCaducidad = Integer.MIN_VALUE;
		this.ultimoEstadoPodrido = !this.podrido;
	}

	@Override
	public java.util.ArrayList<String> getInfo() {
		this.verificarCaducidad();
		this.actualizarCacheTooltip();
		return this.LISTA_INFO;
	}

	private void actualizarCacheTooltip() {
		if (!this.perecedero) {
			return;
		}

		if (this.podrido) {
			if (!this.ultimoEstadoPodrido) {
				this.ultimoEstadoPodrido = true;
				this.LISTA_INFO.clear();
				this.LISTA_INFO.add("Estado: Descompuesto");
				this.LISTA_INFO.add("Peligro: Puede causar malestar o intoxicacion.");
			}
			return;
		}

		final double horaActual = (Globales.GESTOR_ASTRONOMICO != null)
				? Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego()
				: 0.0;
		final double horasRestantes = Math.max(0.0, this.horaCaducidad - horaActual);
		final int horasEnteras = (int) Math.ceil(horasRestantes);

		if (horasEnteras != this.ultimaHoraEnteraCaducidad) {
			this.ultimaHoraEnteraCaducidad = horasEnteras;
			this.LISTA_INFO.clear();
			this.rellenarInfo(this.LISTA_INFO);
			this.LISTA_INFO.add("Caduca en aprox: " + horasEnteras + " h");
		}
	}

	public boolean isPerecedero() {
		return this.perecedero;
	}

	public boolean isPodrido() {
		return this.podrido;
	}

	public double getHoraCaducidad() {
		return this.horaCaducidad;
	}
}