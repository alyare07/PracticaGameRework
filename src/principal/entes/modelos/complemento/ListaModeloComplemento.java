package principal.entes.modelos.complemento;

/**
 * Fachada de compatibilidad que delega directamente en el Enum
 * {@link TipoModeloComplemento}.
 */
public abstract class ListaModeloComplemento {

	public static final int COD_BARRERA_INVISIBLE = TipoModeloComplemento.BARRERA_INVISIBLE.getId();
	public static final int COD_ARBOL_1 = TipoModeloComplemento.ARBOL_ROBLE.getId();
	public static final int COD_ARBOL_2 = TipoModeloComplemento.ARBOL_PINO.getId();
	public static final int COD_ARBOL_3 = TipoModeloComplemento.ARBOL_ABEDUL.getId();
	public static final int COD_ARBOL_4 = TipoModeloComplemento.ARBOL_FRUTAL.getId();
	public static final int COD_ARBOL_5 = TipoModeloComplemento.ARBOL_OTONO.getId();
	public static final int COD_ARBOL_6 = TipoModeloComplemento.ARBOL_ROJO.getId();
	public static final int COD_ARBOL_7 = TipoModeloComplemento.ARBOL_SAUCE.getId();
	public static final int COD_ARBOL_8 = TipoModeloComplemento.ARBOL_AZUL.getId();
	public static final int COD_ARBOL_9 = TipoModeloComplemento.ARBOL_PINO_NEVADO.getId();
	public static final int COD_ARBOL_10 = TipoModeloComplemento.ARBOL_SECO.getId();
	public static final int COD_ARBOL_11 = TipoModeloComplemento.ARBOL_TENEBROSO.getId();
	public static final int COD_ARBOL_12 = TipoModeloComplemento.ARBOL_PALMERA.getId();
	public static final int COD_ARBOL_13 = TipoModeloComplemento.ARBOL_PALMERA_SECA.getId();
	public static final int COD_ARBOL_14 = TipoModeloComplemento.ARBOL_CACTUS.getId();
	public static final int COD_ARBOL_15 = TipoModeloComplemento.ARBOL_SAKURA.getId();

	public static final int COD_ARBOL_1_NEVADO = TipoModeloComplemento.ARBOL_PINO_NEVADO.getId();
	public static final int COD_ARBOL_2_NEVADO = TipoModeloComplemento.ARBOL_SECO.getId();
	public static final int COD_ARBOL_3_NEVADO = TipoModeloComplemento.ARBOL_TENEBROSO.getId();
	public static final int COD_ARBOL_4_NEVADO = TipoModeloComplemento.ARBOL_PALMERA.getId();
	public static final int COD_CASA_1 = TipoModeloComplemento.CASA_1.getId();

	private ListaModeloComplemento() {
	}

	public static TipoModeloComplemento getModeloComplemento(final int codModeloComplemento) {
		return TipoModeloComplemento.desdeId(codModeloComplemento);
	}
}