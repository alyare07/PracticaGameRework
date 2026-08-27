package principal.mapa.renderEntidades.camara;

import principal.entes.Ente;
import principal.mapa.renderEntidades.camara.efectos.EfectoInerciaDash;
import principal.mapa.renderEntidades.camara.efectos.EfectoOndaExpansiva;
import principal.mapa.renderEntidades.camara.efectos.EfectoPisoton;
import principal.mapa.renderEntidades.camara.efectos.EfectoRetrocesoDireccional;
import principal.mapa.renderEntidades.camara.efectos.EfectoTerremoto;
import principal.mapa.renderEntidades.camara.efectos.GestorEfectosCamara;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.Constantes;

/**
 * Gestiona el enfoque óptico, seguimiento de entidades, límites de mapa y la
 * lente de efectos especiales (sacudidas, impulsos, rotaciones y zoom).
 * <p>
 * <b>Conceptos Arquitectónicos:</b>
 * <ul>
 * <li><b>Centrado Óptico en Pantalla:</b> Calcula márgenes de compensación para
 * que el centro geométrico del {@link Ente} enfocado quede exactamente en el
 * punto medio de la resolución interna ({@link Constantes#CENTROX},
 * {@link Constantes#CENTROY}).</li>
 * <li><b>Zoom Base vs. Zoom Dinámico:</b> Mantiene un zoom base cuantizado a
 * múltiplos de 0.25 (para nitidez en reposo) y suma los offsets dinámicos de
 * efectos en tiempo real.</li>
 * <li><b>Compensación Geométrica por Rotación (Auto-Crop):</b> Modula el zoom
 * automáticamente al girar para que nunca queden esquinas negras visibles.</li>
 * <li><b>Gestión de Bordes:</b> Delega la restricción de coordenadas a
 * {@link GestorDeLimites} para no mostrar el vacío fuera del mapa.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public class Camara {

	// =========================================================================
	// === 1. OBJETIVO Y CENTRADO DE PANTALLA
	// =========================================================================

	/**
	 * Entidad actualmente seguida por la cámara (Jugador, AsistenteCamara, etc.).
	 */
	private Ente entidadEnfocada;

	/** Desplazamiento horizontal necesario para centrar la entidad en pantalla. */
	private int margenX;

	/** Desplazamiento vertical necesario para centrar la entidad en pantalla. */
	private int margenY;

	// =========================================================================
	// === 2. SUBSISTEMAS DE LÍMITES Y EFECTOS
	// =========================================================================

	/** Controlador de restricciones de bordes del mapa. */
	private final GestorDeLimites gestorLimite;

	/** Gestor modular de efectos cinemáticos (Zero-GC / O(1)). */
	private final GestorEfectosCamara gestorEfectos;

	// =========================================================================
	// === 3. ESTADO DE ZOOM BASE
	// =========================================================================

	/** Valor de zoom por defecto (escala 1:1 estándar). */
	private static final double ZOOM_BASE = 1.0;

	/** Zoom base escalonado del usuario (0.50, 0.75, 1.00, 1.25, 1.50...). */
	private double zoom = 1.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Crea la cámara e inicializa los gestores de límites y efectos.
	 *
	 * @param entidadEnfocada Entidad inicial a seguir (puede ser {@code null}).
	 */
	public Camara(final Ente entidadEnfocada) {
		this.gestorLimite = new GestorDeLimites();
		this.gestorEfectos = new GestorEfectosCamara();

		if (entidadEnfocada != null) {
			this.setEntidadEnfocada(entidadEnfocada);
		}
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	/**
	 * Actualiza los límites del mapa y avanza las fórmulas de los efectos activos.
	 * Se ejecuta de forma síncrona en el bucle lógico del motor.
	 */
	public void actualizar() {
		if (!this.gestorLimite.estaEliminado()) {
			this.gestorLimite.actualizar();
		}
		this.gestorEfectos.actualizar();
	}

	// =========================================================================
	// === CATÁLOGO DE DISPARADORES DE EFECTOS (API PÚBLICA)
	// =========================================================================

	/**
	 * Aplica un temblor caótico de pantalla con decaimiento cuadrático (Trauma
	 * Shake).
	 *
	 * @param duracionMs Duración en milisegundos.
	 * @param amplitudPx Amplitud máxima de vibración en píxeles.
	 */
	public void aplicarTemblor(final double duracionMs, final double amplitudPx) {
		this.gestorEfectos.<EfectoTerremoto>getEfecto(TipoEfectoCamara.TERREMOTO).configurar(amplitudPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.TERREMOTO, duracionMs, 1.0);
	}

	/**
	 * Aplica un golpe de zoom elástico instantáneo (Zoom Punch).
	 *
	 * @param fuerzaZoom Magnitud del salto de zoom (ej: 0.25 a 0.40).
	 * @param duracionMs Duración en milisegundos del retorno elástico.
	 */
	public void aplicarPisoton(final double fuerzaZoom, final double duracionMs) {
		this.gestorEfectos.<EfectoPisoton>getEfecto(TipoEfectoCamara.PISOTON).configurar(fuerzaZoom);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.PISOTON, duracionMs, 1.0);
	}

	/**
	 * Aplica un empuje direccional brusco con recuperación armónica amortiguada.
	 *
	 * @param dirX       Vector director X del disparo o golpe.
	 * @param dirY       Vector director Y del disparo o golpe.
	 * @param fuerzaPx   Desplazamiento máximo en píxeles.
	 * @param duracionMs Duración en milisegundos.
	 */
	public void aplicarRetroceso(final double dirX, final double dirY, final double fuerzaPx, final double duracionMs) {
		this.gestorEfectos.<EfectoRetrocesoDireccional>getEfecto(TipoEfectoCamara.RETROCESO_DIRECCIONAL)
				.configurarDireccion(dirX, dirY, fuerzaPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.RETROCESO_DIRECCIONAL, duracionMs, 1.0);
	}

	/**
	 * Conmuta el efecto de mareo continuo (balanceo pendular y respiración de
	 * zoom).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarModoBorracho(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BORRACHO, activar, 1.0);
	}

	/**
	 * Conmuta el pulso rítmico cardíaco doble (para estados de poca vida o sigilo
	 * tenso).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarLatido(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.LATIDO, activar, 1.0);
	}

	/**
	 * Conmuta el modo frenético (zoom cerrado y micro-temblor de alta energía).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarModoBerserk(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BERSERK, activar, 1.0);
	}

	/**
	 * Conmuta la respiración ambiental suave en el eje vertical (para descanso o
	 * meditación).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarRespiracion(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.RESPIRACION, activar, 1.0);
	}

	/**
	 * Conmuta la deriva eólica lateral continua con ráfagas turbulentas.
	 *
	 * @param activar    {@code true} para activar, {@code false} para desactivar.
	 * @param intensidad Fuerza del viento (1.0 = estándar).
	 */
	public void activarVientoTormenta(final boolean activar, final double intensidad) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.VIENTO_TORMENTA, activar, intensidad);
	}

	/**
	 * Aplica un aturdimiento caótico siguiendo trayectorias en forma de 8 (Curvas
	 * de Lissajous).
	 *
	 * @param duracionMs Duración en milisegundos.
	 */
	public void aplicarAturdimiento(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ATURDIMIENTO, duracionMs, 1.0);
	}

	/**
	 * Aplica un micro-zoom súbito con temblor de altísima frecuencia (Hitstop para
	 * críticos/parrys).
	 *
	 * @param duracionMs Duración en milisegundos (recomendado 60-120 ms).
	 */
	public void aplicarImpactoCritico(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.IMPACTO_CRITICO, duracionMs, 1.0);
	}

	/**
	 * Aplica un zoom-out impulsivo que regresa con resorte elástico (para
	 * explosiones de área).
	 *
	 * @param zoomOutMax Expansión máxima hacia afuera (ej: 0.20 a 0.30).
	 * @param duracionMs Duración en milisegundos.
	 */
	public void aplicarOndaExpansiva(final double zoomOutMax, final double duracionMs) {
		this.gestorEfectos.<EfectoOndaExpansiva>getEfecto(TipoEfectoCamara.ONDA_EXPANSIVA).configurar(zoomOutMax);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ONDA_EXPANSIVA, duracionMs, 1.0);
	}

	/**
	 * Aplica inercia retrasada en la dirección contraria al desplazamiento rápido
	 * (Dash Lag).
	 *
	 * @param dirX           Dirección X de la esquiva.
	 * @param dirY           Dirección Y de la esquiva.
	 * @param distanciaLagPx Píxeles de retraso de la cámara.
	 * @param duracionMs     Duración en milisegundos.
	 */
	public void aplicarInerciaDash(final double dirX, final double dirY, final double distanciaLagPx,
			final double duracionMs) {
		this.gestorEfectos.<EfectoInerciaDash>getEfecto(TipoEfectoCamara.INERCIA_DASH).configurarDireccion(dirX, dirY,
				distanciaLagPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.INERCIA_DASH, duracionMs, 1.0);
	}

	/**
	 * Conmuta el oleaje marítimo continuo (balanceo pendular de babor/estribor y
	 * sube y baja de olas).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarNavegacionBarco(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BARCO_NAVEGACION, activar, 1.0);
	}

	/**
	 * Aplica un sobresalto vertical súbito hacia arriba con micro-zoom (detección o
	 * trampa sorpresa).
	 *
	 * @param duracionMs Duración en milisegundos (recomendado 100-150 ms).
	 */
	public void aplicarSobresaltoAlerta(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ALERTA_SOBRESALTO, duracionMs, 1.0);
	}

	/**
	 * Conmuta el modo de apuntado táctico con arco/arma (zoom sostenido e
	 * inclinación cinematográfica).
	 *
	 * @param activar {@code true} para activar, {@code false} para desactivar.
	 */
	public void activarModoApuntado(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.CAMARA_LENTA_ENFOQUE, activar, 1.0);
	}

	/**
	 * Aplica una rotación acelerada con contracción hacia el centro (muerte por
	 * foso o vórtice).
	 *
	 * @param duracionMs Duración en milisegundos.
	 */
	public void aplicarCaidaAbismo(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.CAIDA_ABISMO, duracionMs, 1.0);
	}

	// =========================================================================
	// === MATEMÁTICA ÓPTICA Y ZOOM COMPENSADO (AUTO-CROP)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: COMPENSACIÓN POR ROTACIÓN (AUTO-CROP)
	 * -------------------------------------------------------------------------
	 * Cuando un rectángulo de 640x360 se rota dentro de una ventana de 640x360, sus
	 * 4 esquinas se meten hacia adentro, dejando ver triángulos negros del fondo.
	 * 
	 * Para solucionarlo automáticamente sin que el jugador lo note: 1. Calculamos
	 * la relación de aspecto: Aspecto = 640 / 360 = 1.7777... 2. Calculamos la
	 * envolvente trigonométrica mínima: Factor = cos(|θ|) + (Aspecto * sin(|θ|)) 3.
	 * Al multiplicar el zoom por este factor, la cámara se agranda EXACTAMENTE lo
	 * necesario para que los bordes del mapa tapen las esquinas negras.
	 * =========================================================================
	 */
	/**
	 * Calcula el Zoom total acumulado (Zoom Base + Offset de Efectos) aplicando
	 * compensación geométrica automática por rotación.
	 *
	 * @return Factor de zoom final ajustado.
	 */
	public double getZoomFinal() {
		final double zoomBaseConEfectos = Math.max(0.2, this.zoom + this.gestorEfectos.getOffsetZoom());
		final double rotacion = this.gestorEfectos.getAnguloRotacion();

		// Si no hay rotación, retornamos el zoom sin sobreescala
		if (rotacion == 0.0) {
			return zoomBaseConEfectos;
		}

		// Relación de aspecto nativa (640 / 360 = 1.7777778)
		final double aspecto = (double) Constantes.ANCHO_JUEGO / Constantes.ALTO_JUEGO;
		final double rotAbs = Math.abs(rotacion);

		// Fórmula de envolvente mínima para rectángulo rotado
		final double factorCompensacionRotacion = Math.cos(rotAbs) + (aspecto * Math.sin(rotAbs));

		return zoomBaseConEfectos * factorCompensacionRotacion;
	}

	// =========================================================================
	// === ENFOQUE, CENTRADO Y LÍMITES
	// =========================================================================

	/**
	 * Establece la entidad a seguir y recalcula los márgenes de centrado de
	 * pantalla.
	 *
	 * @param e Entidad a enfocar (Jugador, proyectil, asistente cinemático).
	 */
	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}

		this.entidadEnfocada = e;
		final int enteAncho = (e.getArea() != null) ? e.getArea().width : 0;
		final int enteAlto = (e.getArea() != null) ? e.getArea().height : 0;

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: MÁRGENES DE CENTRADO
		 * --------------------------------------------------------------------- Las
		 * coordenadas de pantalla se calculan como: xPantalla = (xMundo - camaraX) +
		 * margenX
		 * 
		 * Si margenX fuera simplemente CENTROX (320), la esquina superior izquierda del
		 * sprite quedaría en el centro. Al restar (enteAncho / 2), logramos que el
		 * CENTRO DEL SPRITE quede exactamente en el centro geométrico de la pantalla.
		 * =====================================================================
		 */
		this.margenX = Constantes.CENTROX - (enteAncho / 2);
		this.margenY = Constantes.CENTROY - (enteAlto / 2);

		if (this.entidadEnfocada != this.gestorLimite.getEntidadEnfocada()) {
			this.gestorLimite.eliminar();
		}
	}

	public Ente getEntidadEnfocada() {
		return this.entidadEnfocada;
	}

	/**
	 * Habilita el control automático de bordes basado en las dimensiones del mapa
	 * actual.
	 */
	public void habilitarGestorLimite() {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada);
	}

	/**
	 * Habilita el control de bordes especificando límites rectangulares
	 * personalizados.
	 */
	public void habilitarGestorLimite(final int limiteMaximoX, final int limiteMinimoX, final int limiteMaximoY,
			final int limiteMinimoY, final boolean contarDimensionEnte) {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada, limiteMaximoX, limiteMinimoX, limiteMaximoY,
				limiteMinimoY, contarDimensionEnte);
	}

	/**
	 * Deshabilita la restricción de bordes (la cámara seguirá a la entidad
	 * libremente).
	 */
	public void deshabilitarGestorLimite() {
		this.gestorLimite.eliminar();
	}

	// =========================================================================
	// === CONTROL DE ZOOM BASE
	// =========================================================================

	public double getZoom() {
		return this.zoom;
	}

	/**
	 * Establece el zoom base del usuario aplicando límites de seguridad y
	 * cuantización a pasos discretos de 0.25 (0.50, 0.75, 1.00, 1.25...) para
	 * preservar la nitidez.
	 *
	 * @param nuevoZoom Valor de zoom solicitado.
	 */
	public void setZoom(final double nuevoZoom) {
		final double zoomClampeado = Math.max(0.5, Math.min(2.5, nuevoZoom));
		this.zoom = Math.round(zoomClampeado * 4.0) / 4.0;
	}

	public void reiniciarZoom() {
		this.zoom = ZOOM_BASE;
	}

	public void aumentarZoom() {
		this.setZoom(this.zoom + 0.25);
	}

	public void reducirZoom() {
		this.setZoom(this.zoom - 0.25);
	}

	// =========================================================================
	// === COORDENADAS Y MÁRGENES
	// =========================================================================

	public GestorEfectosCamara getGestorEfectos() {
		return this.gestorEfectos;
	}

	public double getPosicionX() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionX()
				: this.gestorLimite.getPosicionX();
	}

	public double getPosicionY() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionY()
				: this.gestorLimite.getPosicionY();
	}

	public int getPosicionXInt() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionXInt()
				: this.gestorLimite.getPosicionXInt();
	}

	public int getPosicionYInt() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionYInt()
				: this.gestorLimite.getPosicionYInt();
	}

	public int getMargenX() {
		return this.margenX;
	}

	public int getMargenY() {
		return this.margenY;
	}
}