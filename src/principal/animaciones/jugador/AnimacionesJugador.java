package principal.animaciones.jugador;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Set;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Criatura.Estado;
import principal.entes.criaturas.Jugador;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class AnimacionesJugador {

	private final HashMap<String, AnimacionDireccionada> ANIMACIONES;

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
			if (transparencia) {
				animDir.pintarConTransparencia(g, x, y, false, alpha, direccion, flash);
			} else {
				animDir.pintar(g, x, y, false, direccion, flash);
			}
		}
	}
}