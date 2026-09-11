package principal.animaciones.jugador;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Set;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Criatura.Estado;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.inventario.equipamiento.SlotPiezaEquipo;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

public class AnimacionesJugador {

	private final HashMap<String, AnimacionDireccionada> ANIMACIONES;
	private static final byte[] OFFSETS_CASCO_Y = {
			// --- ESTÁNDAR / IDLE (Filas 0, 1, 2) ---
			0, 1, 1, 0, // Fila 0: Sur (0..3)
			0, 1, 1, 0, // Fila 1: Norte (4..7)
			0, 1, 1, 0, // Fila 2: Lado (8..11)

			// --- CAMINANDO (Filas 3, 4, 5) ---
			0, 1, 1, 0, // Fila 3: Sur (12..15)
			0, 1, 1, 0, // Fila 4: Norte (16..19)
			0, 1, 1, 0, // Fila 5: Lado (20..23)

			// --- ARMADO ESTÁNDAR (Filas 6, 7, 8) ---
			0, 1, 1, 0, // Fila 6: Sur (24..27)
			0, 1, 1, 0, // Fila 7: Norte (28..31)
			0, 1, 1, 0, // Fila 8: Lado (32..35)

			// --- ARMADO CAMINANDO (Filas 9, 10, 11) ---
			0, 1, 1, 0, // Fila 9: Sur (36..39)
			0, 1, 1, 0, // Fila 10: Norte (40..43)
			0, 1, 1, 0 // Fila 11: Lado (44..47)
	};
	// Mapeo exacto de los 48 fotogramas globales a los 9 recortes del peto (0..8)
	private static final byte[] MAPA_PETO_INDICE = {
			// --- ESTÁNDAR / IDLE (Filas 0, 1, 2) ---
			0, 1, 0, 0, // Fila 0: Sur (0..3)
			2, 3, 3, 2, // Fila 1: Norte (4..7)
			4, 5, 5, 4, // Fila 2: Lado (8..11)

			// --- CAMINANDO (Filas 3, 4, 5) ---
			0, 0, 1, 0, // Fila 3: Sur (12..15) -> 13 y 15 usan Peto 7 invertido
			2, 3, 3, 2, // Fila 4: Norte (16..19)
			4, 8, 8, 4, // Fila 5: Lado (20..23)

			// --- ARMADO ESTÁNDAR (Filas 6, 7, 8) ---
			0, 1, 0, 0, // Fila 6: Sur (24..27)
			2, 3, 3, 2, // Fila 7: Norte (28..31)
			6, 8, 8, 6, // Fila 8: Lado (32..35)

			// --- ARMADO CAMINANDO (Filas 9, 10, 11) ---
			0, 1, 1, 0, // Fila 9: Sur (36..39)
			2, 3, 3, 2, // Fila 10: Norte (40..43)
			6, 8, 8, 6 // Fila 11: Lado (44..47)
	};

	// Offsets verticales del Peto (solo frames 2 y 26 bajan 1 px)
	private static final byte[] OFFSETS_PETO_Y = { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	public static final String ESTANDAR = "Estandar";
	public static final String CAMINANDO = "Caminando";
	public static final String ARMADO_ESTANDAR = "Armado Estandar";
	public static final String ARMADO_CAMINANDO = "Armado Caminando";

	private final int TIEMPO_MS_POR_FRAME = 150;

	public AnimacionesJugador() {
		this.ANIMACIONES = new HashMap<String, AnimacionDireccionada>();

		// 1. Obtener la hoja normal y la hoja invertida desde GestorTexturas (0
		// lecturas de disco)
		final HojaSprite hojaNormal = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.JUGADOR);
		final HojaSprite hojaVolteada = Globales.GESTOR_TEXTURAS.getHojaVolteadaH(ClaveHoja.JUGADOR);

		final int framesPorFila = 4;

		// --- ESTÁNDAR (Filas 0, 1, 2) ---
		this.ANIMACIONES.put(ESTANDAR,
				new AnimacionDireccionada(
						new Animacion(hojaNormal.recortarRango(4, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Norte
						new Animacion(hojaNormal.recortarRango(0, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Sur
						new Animacion(hojaNormal.recortarRango(8, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Este
						new Animacion(hojaVolteada.recortarRango(8, framesPorFila), true, this.TIEMPO_MS_POR_FRAME) // Oeste
																													// (Volteado)
				));

		// --- CAMINANDO (Filas 3, 4, 5) ---
		this.ANIMACIONES.put(CAMINANDO, new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(16, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(12, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(20, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaVolteada.recortarRango(20, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50)));

		// --- ARMADO ESTÁNDAR (Filas 6, 7, 8) ---
		this.ANIMACIONES.put(ARMADO_ESTANDAR,
				new AnimacionDireccionada(
						new Animacion(hojaNormal.recortarRango(28, framesPorFila), true, this.TIEMPO_MS_POR_FRAME),
						new Animacion(hojaNormal.recortarRango(24, framesPorFila), true, this.TIEMPO_MS_POR_FRAME),
						new Animacion(hojaNormal.recortarRango(32, framesPorFila), true, this.TIEMPO_MS_POR_FRAME),
						new Animacion(hojaVolteada.recortarRango(32, framesPorFila), true, this.TIEMPO_MS_POR_FRAME)));

		// --- ARMADO CAMINANDO (Filas 9, 10, 11) ---
		this.ANIMACIONES.put(ARMADO_CAMINANDO, new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(40, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(36, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(44, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaVolteada.recortarRango(44, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50)));
	}

	public void actualizar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		final Direccion direccion = jugador.getDireccion();
		final Set<Estado> estados = jugador.getEstado();
		final boolean conPistola = jugador.pistolaEquipada() && !estados.contains(Estado.ARROJANDO);

		final String clave;
		if (conPistola) {
			clave = estados.contains(Estado.ESTANDAR) ? ARMADO_ESTANDAR : ARMADO_CAMINANDO;
		} else {
			clave = estados.contains(Estado.ESTANDAR) ? ESTANDAR : CAMINANDO;
		}

		final AnimacionDireccionada animDir = this.ANIMACIONES.get(clave);
		if (animDir != null) {
			animDir.actualizar(direccion);
		}
	}

	public void pintar(final Graphics2D g, final int x, final int y) {
		final Jugador jugador = Globales.JUGADOR;
		if (jugador == null) {
			return;
		}

		final boolean transparencia = jugador.atrasDeComplemento();
		final boolean flash = jugador.estaEnFlashDanio();
		final float alpha = 0.5f;
		final Direccion direccion = jugador.getDireccion();
		final Set<Estado> estados = jugador.getEstado();

		final String clave;
		if (jugador.pistolaEquipada() && !estados.contains(Estado.ARROJANDO)) {
			clave = estados.contains(Estado.ESTANDAR) ? ARMADO_ESTANDAR : ARMADO_CAMINANDO;
		} else {
			clave = estados.contains(Estado.ESTANDAR) ? ESTANDAR : CAMINANDO;
		}

		final AnimacionDireccionada animDir = this.ANIMACIONES.get(clave);
		if (animDir != null) {
			// 1. CUERPO BASE
			if (transparencia) {
				animDir.pintarConTransparencia(g, x, y, false, alpha, direccion, flash);
			} else {
				animDir.pintar(g, x, y, false, direccion, flash);
			}

			final Animacion animActual = animDir.getAnimacion(direccion);
			final int paso = (animActual != null) ? animActual.getSpritePosicion() : 0;

			// 2. PETO (Debajo del casco)
			this.pintarPeto(g, x, y, direccion, estados, paso, flash, transparencia, alpha);

			// 3. CASCO (Encima de cuerpo y peto)
			this.pintarCasco(g, x, y, direccion, estados, paso, flash, transparencia, alpha);
		}
	}

	private void pintarCasco(final Graphics2D g, final int x, final int y, final Direccion direccion,
			final Set<Estado> estados, final int paso, final boolean flash, final boolean transparencia,
			final float alpha) {

		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			return;
		}

		final SlotPiezaEquipo slotCasco = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager()
				.getSlotCasco();

		if ((slotCasco == null) || !slotCasco.contieneItem() || !(slotCasco.getItem() instanceof PiezaEquipo)) {
			return;
		}

		final PiezaEquipo pieza = (PiezaEquipo) slotCasco.getItem();
		final ClaveHoja claveHoja = pieza.getHojaEquipado();
		if (claveHoja == null) {
			return;
		}

		final boolean esOeste = (direccion == Direccion.OESTE);
		final HojaSprite hoja = esOeste ? Globales.GESTOR_TEXTURAS.getHojaVolteadaH(claveHoja)
				: Globales.GESTOR_TEXTURAS.getHoja(claveHoja);

		if (hoja == null) {
			return;
		}

		// Frame 0 = SUR, Frame 1 = NORTE, Frame 2 = ESTE / OESTE
		final int indiceCuadro;
		switch (direccion) {
		case NORTE:
			indiceCuadro = 1;
			break;
		case SUR:
			indiceCuadro = 0;
			break;
		case ESTE:
		case OESTE:
		default:
			indiceCuadro = 2;
			break;
		}

		// Cálculo matemático O(1) del índice global (0 a 47) en la hoja maestra
		final boolean conPistola = Globales.JUGADOR.pistolaEquipada() && !estados.contains(Estado.ARROJANDO);
		final boolean enMovimiento = !estados.contains(Estado.ESTANDAR);

		int baseFila = (conPistola ? 24 : 0) + (enMovimiento ? 12 : 0);
		switch (direccion) {
		case SUR:
			break;
		case NORTE:
			baseFila += 4;
			break;
		case ESTE:
		case OESTE:
		default:
			baseFila += 8;
			break;
		}

		final int indiceGlobal = baseFila + (paso % 4);
		final int bobY = OFFSETS_CASCO_Y[indiceGlobal];

		final java.awt.image.BufferedImage spriteCasco = flash ? hoja.getSpriteFlash(indiceCuadro)
				: hoja.getSprite(indiceCuadro);

		if (transparencia) {
			Render2D.dibujarImagenConTransparencia(g, spriteCasco, x, y + bobY, alpha);
		} else {
			Render2D.dibujarImagen(g, spriteCasco, x, y + bobY);
		}
	}

	private void pintarPeto(final Graphics2D g, final int x, final int y, final Direccion direccion,
			final Set<Estado> estados, final int paso, final boolean flash, final boolean transparencia,
			final float alpha) {

		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			return;
		}

		final SlotPiezaEquipo slotTorso = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager()
				.getSlotTorso();

		if ((slotTorso == null) || !slotTorso.contieneItem() || !(slotTorso.getItem() instanceof PiezaEquipo)) {
			return;
		}

		final PiezaEquipo pieza = (PiezaEquipo) slotTorso.getItem();
		final ClaveHoja claveHoja = pieza.getHojaEquipado();
		if (claveHoja == null) {
			return;
		}

		// Cálculo matemático O(1) del índice global (0 a 47)
		final boolean conPistola = Globales.JUGADOR.pistolaEquipada() && !estados.contains(Estado.ARROJANDO);
		final boolean enMovimiento = !estados.contains(Estado.ESTANDAR);

		int baseFila = (conPistola ? 24 : 0) + (enMovimiento ? 12 : 0);
		switch (direccion) {
		case SUR:
			break;
		case NORTE:
			baseFila += 4;
			break;
		case ESTE:
		case OESTE:
		default:
			baseFila += 8;
			break;
		}

		final int indiceGlobal = baseFila + (paso % 4);
		final int indiceCuadro = MAPA_PETO_INDICE[indiceGlobal];
		final int bobY = OFFSETS_PETO_Y[indiceGlobal];

		// Selección de hoja: usa volteada si mira al Oeste O si es el paso especial
		// (15)
		final boolean usarVolteada = (direccion == Direccion.OESTE);
//		final boolean usarVolteada = (direccion == Direccion.OESTE) || (indiceGlobal == 15);
		final HojaSprite hoja = usarVolteada ? Globales.GESTOR_TEXTURAS.getHojaVolteadaH(claveHoja)
				: Globales.GESTOR_TEXTURAS.getHoja(claveHoja);

		if (hoja == null) {
			return;
		}

		final java.awt.image.BufferedImage spritePeto = flash ? hoja.getSpriteFlash(indiceCuadro)
				: hoja.getSprite(indiceCuadro);

		if (transparencia) {
			Render2D.dibujarImagenConTransparencia(g, spritePeto, x, y + bobY, alpha);
		} else {
			Render2D.dibujarImagen(g, spritePeto, x, y + bobY);
		}
	}
}