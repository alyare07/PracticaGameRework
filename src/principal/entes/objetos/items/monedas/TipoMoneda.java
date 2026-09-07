package principal.entes.objetos.items.monedas;

import principal.recursos.TexturaItem;

/**
 * Define las denominaciones monetarias del juego y sus equivalencias.
 * Escala: 1 Oro = 100 Plata.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoMoneda {

	PLATA("Moneda de Plata", 1L, TexturaItem.ANILLO_PLATA_INV, TexturaItem.ANILLO_PLATA_MAPA),
	ORO("Moneda de Oro", 100L, TexturaItem.ANILLO_ORO_INV, TexturaItem.ANILLO_ORO_MAPA);

	private final String nombre;
	private final long equivalenciaPlata;
	private final TexturaItem texturaInv;
	private final TexturaItem texturaMapa;

	TipoMoneda(final String nombre, final long equivalenciaPlata, final TexturaItem texturaInv,
			final TexturaItem texturaMapa) {
		this.nombre = nombre;
		this.equivalenciaPlata = equivalenciaPlata;
		this.texturaInv = texturaInv;
		this.texturaMapa = texturaMapa;
	}

	public String getNombre() {
		return this.nombre;
	}

	public long getEquivalenciaPlata() {
		return this.equivalenciaPlata;
	}

	public TexturaItem getTexturaInv() {
		return this.texturaInv;
	}

	public TexturaItem getTexturaMapa() {
		return this.texturaMapa;
	}
}