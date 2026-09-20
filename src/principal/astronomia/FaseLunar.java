package principal.astronomia;

/**
 * Catálogo del ciclo lunar de 8 fases distribuido en el calendario canónico
 * de 28 días por mes/estación (3.5 días por fase).
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC O(1))
 */
public enum FaseLunar {

	LUNA_NUEVA("Luna Nueva", 248, 0),
	LUNA_CRECIENTE_CONCAVA("Creciente Cóncava", 230, 1),
	CUARTO_CRECIENTE("Cuarto Creciente", 210, 2),
	LUNA_GIBOSA_CRECIENTE("Gibosa Creciente", 185, 3),
	LUNA_LLENA("Luna Llena", 160, 4),
	LUNA_GIBOSA_MENGUANTE("Gibosa Menguante", 185, 5),
	CUARTO_MENGUANTE("Cuarto Menguante", 210, 6),
	LUNA_MENGUANTE_CONCAVA("Menguante Cóncava", 230, 7);

	private static final FaseLunar[] VALORES = FaseLunar.values();

	private final String nombreVisible;
	private final int alphaPenumbraNocturna;
	private final int indiceSprite;

	FaseLunar(final String nombreVisible, final int alphaPenumbraNocturna, final int indiceSprite) {
		this.nombreVisible = nombreVisible;
		this.alphaPenumbraNocturna = alphaPenumbraNocturna;
		this.indiceSprite = indiceSprite;
	}

	/**
	 * Resuelve la fase lunar activa en O(1) en base al día actual (1 a 112).
	 */
	public static FaseLunar obtenerPorDia(final int diaActual) {
		final int diaMes = (Math.max(1, diaActual) - 1) % 28; // 0 a 27
		final int indice = (int) (diaMes / 3.5); // 0 a 7
		return VALORES[Math.max(0, Math.min(7, indice))];
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public int getAlphaPenumbraNocturna() {
		return this.alphaPenumbraNocturna;
	}

	public int getIndiceSprite() {
		return this.indiceSprite;
	}
}