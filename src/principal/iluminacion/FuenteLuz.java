package principal.iluminacion;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;

/**
 * Emisor de luz dinámico con soporte para variación de temperatura cromática
 * (Chroma Jitter), conos de visión, parpadeo armónico y sincronización con
 * {@link Ente} (Zero-GC / O(1)).
 * 
 * @version 8.0
 */
public class FuenteLuz {

	// =========================================================================
	// === 1. IDENTIFICACIÓN Y TIPO DE LUZ
	// =========================================================================

	private final int indicePool;
	private boolean activa;
	private TipoLuz tipo;

	// =========================================================================
	// === 2. POSICIONAMIENTO, ANCLAJE Y OFFSETS
	// =========================================================================

	private double posX;
	private double posY;
	private Ente enteAnclado;

	private double offsetX = 0.0;
	private double offsetY = 0.0;
	private double anguloRotacion;

	// =========================================================================
	// === 3. RADIOS, FÍSICA Y ESPECTRO TÉRMICO (CHROMA JITTER)
	// =========================================================================

	private double radioBase;
	private double radioActual;
	private double tiempoFase;

	/**
	 * Índice de espectro térmico actual:
	 * <ul>
	 * <li><b>0:</b> Alta Temperatura (Núcleo incandescente brillante).</li>
	 * <li><b>1:</b> Temperatura Media (Color estándar de reposo).</li>
	 * <li><b>2:</b> Baja Temperatura (Brasa oscura / Carmesí).</li>
	 * </ul>
	 */
	private int varianteTermica = 1;

	// =========================================================================
	// === 4. CICLO DE VIDA TEMPORAL (FLASHES Y EXPLOSIONES)
	// =========================================================================

	private boolean temporal;
	private double duracionVidaTotal;
	private double tiempoVidaRestante;
	private double radioInicialFlash;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	public FuenteLuz(final int indicePool) {
		this.indicePool = indicePool;
		this.activa = false;
	}

	// =========================================================================
	// === SPAWN / INICIALIZACIÓN (ZERO-GC)
	// =========================================================================

	public void spawnFija(final double x, final double y, final TipoLuz tipo, final double radioPersonalizado) {
		this.posX = x;
		this.posY = y;
		this.enteAnclado = null;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;
		this.tiempoFase = Math.random() * 10.0;
		this.varianteTermica = 1;
		this.anguloRotacion = 0.0;
		this.temporal = false;
		this.activa = true;
	}

	public void spawnAnclada(final Ente ente, final TipoLuz tipo, final double radioPersonalizado) {
		this.enteAnclado = ente;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;
		this.tiempoFase = Math.random() * 10.0;
		this.varianteTermica = 1;
		this.anguloRotacion = 0.0;
		this.temporal = false;
		this.activa = true;

		if (ente != null) {
			this.actualizarPosicionEnte();
			ente.asignarLuz(this);
		}
	}

	public void spawnTemporal(final double x, final double y, final TipoLuz tipo, final double radioInicial,
			final double duracionSegundos) {
		this.spawnFija(x, y, tipo, radioInicial);
		this.temporal = true;
		this.duracionVidaTotal = Math.max(0.01, duracionSegundos);
		this.tiempoVidaRestante = this.duracionVidaTotal;
		this.radioInicialFlash = this.radioBase;
	}

	public void apagar() {
		if (!this.activa && (this.enteAnclado == null)) {
			return;
		}

		this.activa = false;
		this.temporal = false;

		if (this.enteAnclado != null) {
			final Ente enteTemporal = this.enteAnclado;
			this.enteAnclado = null;

			if (enteTemporal.getLuzAsignada() == this) {
				enteTemporal.asignarLuz(null);
			}
		}

		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.varianteTermica = 1;
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		// 1. Decaimiento de flash temporal
		if (this.temporal) {
			this.tiempoVidaRestante -= dt;
			if (this.tiempoVidaRestante <= 0.0) {
				this.apagar();
				return;
			}
			final double progreso = this.tiempoVidaRestante / this.duracionVidaTotal;
			this.radioBase = this.radioInicialFlash * (progreso * progreso);
		}

		// 2. Seguimiento de Ente
		this.actualizarPosicionEnte();

		// 3. Modulación de Llama Viva y Espectro Térmico (Chroma Jitter)
		if ((this.tipo != null) && this.tipo.isParpadea()) {
			this.tiempoFase += dt;
			final double t = this.tiempoFase;
			final double ondaFuego = Math.sin(t * 14.0) + (0.5 * Math.sin(t * 27.0));

			this.radioActual = this.radioBase + (ondaFuego * this.tipo.getAmplitudParpadeo());

			// Conmutación espectral térmica según la energía de la llama
			if (ondaFuego > 0.40) {
				this.varianteTermica = 0; // Alta temperatura (Núcleo incandescente)
			} else if (ondaFuego < -0.40) {
				this.varianteTermica = 2; // Baja temperatura (Enfriamiento/brasa)
			} else {
				this.varianteTermica = 1; // Temperatura nominal estándar
			}
		} else {
			this.radioActual = this.radioBase;
			this.varianteTermica = 1;
		}
	}

	public void actualizarPosicionEnte() {
		if (this.enteAnclado != null) {
			if (this.enteAnclado.estaEliminado()) {
				this.apagar();
				return;
			}
			this.posX = this.enteAnclado.getCentroX() + this.offsetX;
			this.posY = this.enteAnclado.getCentroY() + this.offsetY;
		}
	}

	// =========================================================================
	// === MÉTODOS DE ORIENTACIÓN Y OFFSETS (API PÚBLICA)
	// =========================================================================

	public void setOffset(final double offsetX, final double offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.actualizarPosicionEnte();
	}

	public void orientarSegunDireccion(final Direccion direccion) {
		if (direccion == null) {
			return;
		}
		switch (direccion) {
		case SUR:
			this.anguloRotacion = Math.PI * 0.5;
			break;
		case OESTE:
			this.anguloRotacion = Math.PI;
			break;
		case NORTE:
			this.anguloRotacion = Math.PI * 1.5;
			break;
		case ESTE:
		default:
			this.anguloRotacion = 0.0;
			break;
		}
	}

	public void orientarHaciaPunto(final double destinoX, final double destinoY) {
		this.anguloRotacion = Math.atan2(destinoY - this.posY, destinoX - this.posX);
	}

	public void setAnguloRotacion(final double radianes) {
		this.anguloRotacion = radianes;
	}

	public void setMultiplicadorRadio(final double factor) {
		if (this.tipo != null) {
			this.radioBase = Math.max(10.0, this.tipo.getRadioBase() * factor);
		}
	}

	// =========================================================================
	// === GETTERS INMUTABLES (ZERO-GC)
	// =========================================================================

	public int getIndicePool() {
		return this.indicePool;
	}

	public boolean isActiva() {
		return this.activa;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getOffsetX() {
		return this.offsetX;
	}

	public double getOffsetY() {
		return this.offsetY;
	}

	public double getRadioActual() {
		return this.radioActual;
	}

	public TipoLuz getTipo() {
		return this.tipo;
	}

	public Ente getEnteAnclado() {
		return this.enteAnclado;
	}

	public double getAnguloRotacion() {
		return this.anguloRotacion;
	}

	public int getVarianteTermica() {
		return this.varianteTermica;
	}
}