package principal.maquinaestado.estados;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.criaturas.jugador.Jugador;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Orquestador cinemático de la Singularidad Óptica y el Salto Temporal O(1).
 * Gobierna el fundido, la deformación elástica de cámara y el despertar matutino
 * con cero asignaciones en el Heap (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8 - Optical Singularity Engine)
 */
public class GestorTransicionSueno {

	private static final GestorTransicionSueno INSTANCIA = new GestorTransicionSueno();

	public enum EstadoTransicion {
		INACTIVO, IMPLOSION, DESPERTAR
	}

	private EstadoTransicion estado = EstadoTransicion.INACTIVO;

	// Parámetros temporales (en segundos reales)
	private static final double DURACION_IMPLOSION = 0.65;
	private static final double DURACION_DESPERTAR = 0.85;

	private double temporizadorFase = 0.0;
	private double horasASaltar = 8.0;
	private boolean descansoEnCama = false;

	// Constantes de color estáticas Zero-GC
	private static final Color COLOR_NEGRO_NOCHE = Color.BLACK;
	private static final Color COLOR_DESTELLO_MATUTINO = new Color(255, 225, 140); // Ámbar dorado suave
	private static final AlphaComposite COMPOSITE_OPACO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	// Tabla de opacidades precalculadas de 0 a 100%
	private static final AlphaComposite[] COMPOSITES = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i / 100.0f);
		}
	}

	private GestorTransicionSueno() {
	}

	public static GestorTransicionSueno getInstancia() {
		return INSTANCIA;
	}

	/**
	 * Inicia la secuencia de sueño con contracción gravitacional de cámara.
	 *
	 * @param horas Horas in-game de descanso (típicamente 8.0).
	 * @param enCama true si duerme en Cama de interior, false si es Carpa exterior.
	 */
	public void iniciar(final double horas, final boolean enCama) {
		if (this.estado != EstadoTransicion.INACTIVO) {
			return;
		}

		this.horasASaltar = Math.max(1.0, horas);
		this.descansoEnCama = enCama;
		this.temporizadorFase = 0.0;
		this.estado = EstadoTransicion.IMPLOSION;

		// Bloqueo físico de la criatura
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.detenerMovimiento();
			Globales.JUGADOR.setEstadoEstandar();
		}

		GestorSonido.reproducir(IDSonido.SELECT);
	}

	public void actualizar(final double dt) {
		if (this.estado == EstadoTransicion.INACTIVO) {
			return;
		}

		this.temporizadorFase += dt;

		// =====================================================================
		// FASE 1: IMPLOSIÓN HACIA EL TORSO DEL PERSONAJE
		// =====================================================================
		if (this.estado == EstadoTransicion.IMPLOSION) {
			final double progreso = Math.min(1.0, this.temporizadorFase / DURACION_IMPLOSION);
			// Curva Ease-In cuadrática para el zoom
			final double zoomExtra = (progreso * progreso) * 0.85;

			if (Globales.CAMARA != null) {
				Globales.CAMARA.setOffsetZoomTransicion(zoomExtra);
			}

			// Al alcanzar el 100% de oscuridad, ejecuta el salto O(1)
			if (this.temporizadorFase >= DURACION_IMPLOSION) {
				this.ejecutarSaltoTemporalCuantico();
				this.temporizadorFase = 0.0;
				this.estado = EstadoTransicion.DESPERTAR;
			}
		}
		// =====================================================================
		// FASE 2: DESPERTAR Y EXPANSION MATUTINA
		// =====================================================================
		else if (this.estado == EstadoTransicion.DESPERTAR) {
			final double progreso = Math.min(1.0, this.temporizadorFase / DURACION_DESPERTAR);
			// Curva Ease-Out cuadrática para regresar a la escala base
			final double factorRestante = 1.0 - progreso;
			final double zoomExtra = (factorRestante * factorRestante) * 0.85;

			if (Globales.CAMARA != null) {
				Globales.CAMARA.setOffsetZoomTransicion(zoomExtra);
			}

			if (this.temporizadorFase >= DURACION_DESPERTAR) {
				this.finalizar();
			}
		}
	}

	/**
	 * Ejecución analítica instantánea en forma cerrada O(1) de todos los motores
	 * mientras la pantalla está en oscuridad total (0 ms CPU).
	 */
	private void ejecutarSaltoTemporalCuantico() {
		final double dtHoras = this.horasASaltar;
		final Jugador j = Globales.JUGADOR;

		// 1. Salto Astronómico de 8 horas
		if (Globales.GESTOR_ASTRONOMICO != null) {
			Globales.GESTOR_ASTRONOMICO.ejecutarSaltoTemporal(dtHoras);
		}

		// 2. Catch-Up Metabólico (Digestión basal y recuperación de sueño)
		if (Globales.GESTOR_METABOLISMO != null) {
			Globales.GESTOR_METABOLISMO.aplicarSaltoTemporalSueno(dtHoras);
		}

		// 3. Estabilización Térmica
		if (Globales.GESTOR_TERMICO_JUGADOR != null) {
			Globales.GESTOR_TERMICO_JUGADOR.estabilizarPorDescanso();
		}

		// 4. Beneficio de Descanso y Salud
		if ((j != null) && !j.estaEliminado()) {
			// Si no sufrió inanición severa, recupera salud por descanso
			if ((Globales.GESTOR_METABOLISMO != null) && !Globales.GESTOR_METABOLISMO.isInanicion()) {
				final double porcentajeCuracion = this.descansoEnCama ? 0.50 : 0.30;
				final double hpCurar = j.getVidaMaxima() * porcentajeCuracion;
				j.curar(hpCurar);
			}
			j.setEstamina(j.getLimiteEstamina());
		}

		// 5. Catch-Up Climático
		if (Globales.GESTOR_CLIMA != null) {
			Globales.GESTOR_CLIMA.actualizar();
		}

		GestorSonido.reproducir(IDSonido.SELECT);
	}

	private void finalizar() {
		this.estado = EstadoTransicion.INACTIVO;
		this.temporizadorFase = 0.0;
		if (Globales.CAMARA != null) {
			Globales.CAMARA.setOffsetZoomTransicion(0.0);
		}
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.setEstadoEstandar();
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.estado == EstadoTransicion.INACTIVO) {
			return;
		}

		final int w = Constantes.ANCHO_JUEGO;
		final int h = Constantes.ALTO_JUEGO;

		if (this.estado == EstadoTransicion.IMPLOSION) {
			final double progreso = Math.min(1.0, this.temporizadorFase / DURACION_IMPLOSION);
			final int idxAlpha = Math.max(0, Math.min(100, (int) Math.round(progreso * 100.0)));

			g.setComposite(COMPOSITES[idxAlpha]);
			Render2D.dibujarRectanguloRelleno(g, 0, 0, w, h, COLOR_NEGRO_NOCHE);
			g.setComposite(COMPOSITE_OPACO);

		} else if (this.estado == EstadoTransicion.DESPERTAR) {
			final double progreso = Math.min(1.0, this.temporizadorFase / DURACION_DESPERTAR);
			final double opacidad = 1.0 - progreso;
			final int idxAlpha = Math.max(0, Math.min(100, (int) Math.round(opacidad * 100.0)));

			// Fundido matutino dorado disolviéndose suavemente a la luz del día
			g.setComposite(COMPOSITES[idxAlpha]);
			Render2D.dibujarRectanguloRelleno(g, 0, 0, w, h, COLOR_DESTELLO_MATUTINO);
			g.setComposite(COMPOSITE_OPACO);
		}
	}

	public boolean isActivo() {
		return this.estado != EstadoTransicion.INACTIVO;
	}

	public EstadoTransicion getEstado() {
		return this.estado;
	}
}