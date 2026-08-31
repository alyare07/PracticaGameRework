package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor maestro del subsistema de iluminación dinámica 2D, sombreado acelerado
 * en VRAM, ciclo solar de 24 horas, rayos volumétricos (God Rays) y aura
 * nocturna de penumbra suave.
 * 
 * @version 15.0
 */
public class GestorLuz {

	// =========================================================================
	// === 1. CAPACIDAD Y TEXTURIZADO
	// =========================================================================

	private static final int CAPACIDAD_LUCES = 256;
	private static final int RESOLUCION_HALO_HD = 256;

	private static final AlphaComposite COMPOSITE_LIMPIEZA = AlphaComposite.getInstance(AlphaComposite.CLEAR);
	private static final AlphaComposite COMPOSITE_NORMAL = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private static final AlphaComposite COMPOSITE_PERFORAR = AlphaComposite.getInstance(AlphaComposite.DST_OUT);

	private static final AlphaComposite[] COMPOSITES_TINTE_ATENUADO = new AlphaComposite[11];
	static {
		for (int i = 0; i <= 10; i++) {
			final float opacidad = (i / 10.0f) * 0.40f;
			COMPOSITES_TINTE_ATENUADO[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad);
		}
	}

	private static final AlphaComposite[] COMPOSITES_RELAMPAGO = new AlphaComposite[11];
	static {
		for (int i = 0; i <= 10; i++) {
			final float opacidad = (i / 10.0f) * 0.80f;
			COMPOSITES_RELAMPAGO[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad);
		}
	}

	// =========================================================================
	// === 2. POOL DE MEMORIA Y SUBSISTEMAS
	// =========================================================================

	private final FuenteLuz[] pool;
	private final int[] indicesLibres;
	private int topePila;

	private final FuenteLuz[] activas;
	private int cantidadActivas;

	private final CicloDiaNoche ciclo;
	private final GestorRayosSol rayosSol;
	private final OclusorSombras2D oclusorSombras;
	private VolatileImage lightmap;

	private final BufferedImage texturaMascaraAlphaHD;
	private final BufferedImage texturaMascaraAuraHD; // Máscara tenue para Aura Jugador
	private final BufferedImage[] texturasMascaraConoHD;
	private final BufferedImage[][] texturasHaloColor;
	private final BufferedImage[][] texturasHaloColorCono;

	// =========================================================================
	// === 3. ESTADOS DE AMBIENTE, CUEVAS Y BIOMAS
	// =========================================================================

	private boolean iluminacionHabilitada = true;
	private boolean modoAmbienteFijo = false;
	private Color colorAmbienteFijo = new Color(0, 0, 0, 255);

	private boolean transicionActiva = false;
	private Color colorTransicionOrigen;
	private Color colorTransicionDestino;
	private double tiempoTransicionTotal;
	private double tiempoTransicionActual;

	private Color colorTinteBioma = null;
	private double factorInmersionBioma = 0.0;

	private boolean flashGlobalActivo = false;
	private double duracionFlashGlobal = 0.0;
	private double tiempoFlashGlobalRestante = 0.0;
	private boolean flashGlobalRelampago = false;

	private int lastBaseR = -1, lastBaseG = -1, lastBaseB = -1, lastBaseA = -1;
	private Color colorAmbienteCalculado = new Color(0, 0, 0, 0);

	// =========================================================================
	// === CONSTRUCTOR: INICIALIZACIÓN Y PRE-HORNEADO
	// =========================================================================

	public GestorLuz() {
		this.pool = new FuenteLuz[CAPACIDAD_LUCES];
		this.indicesLibres = new int[CAPACIDAD_LUCES];
		this.activas = new FuenteLuz[CAPACIDAD_LUCES];
		this.cantidadActivas = 0;
		this.topePila = CAPACIDAD_LUCES;

		for (int i = 0; i < CAPACIDAD_LUCES; i++) {
			this.pool[i] = new FuenteLuz(i);
			this.indicesLibres[i] = i;
		}

		this.ciclo = new CicloDiaNoche();
		this.rayosSol = new GestorRayosSol();
		this.oclusorSombras = new OclusorSombras2D();

		// 1. Horneado de máscaras alfa en HD
		this.texturaMascaraAlphaHD = this.hornearTexturaMascaraHD(255);
		this.texturaMascaraAuraHD = this.hornearTexturaMascaraHD(105); // Solo 40% de perforación para penumbra oscura

		final int totalTipos = TipoLuz.values().length;
		this.texturasMascaraConoHD = new BufferedImage[totalTipos];
		this.texturasHaloColor = new BufferedImage[totalTipos][3];
		this.texturasHaloColorCono = new BufferedImage[totalTipos][3];

		for (final TipoLuz tipo : TipoLuz.values()) {
			final int ordinal = tipo.ordinal();

			for (int nivel = 0; nivel < 3; nivel++) {
				this.texturasHaloColor[ordinal][nivel] = this.hornearTexturaColorHD(tipo, nivel);
			}

			if (tipo.isEsCono()) {
				this.texturasMascaraConoHD[ordinal] = this.hornearTexturaMascaraConoHD(tipo.getAnguloAperturaGrados());
				for (int nivel = 0; nivel < 3; nivel++) {
					this.texturasHaloColorCono[ordinal][nivel] = this.hornearTexturaColorConoHD(tipo, nivel);
				}
			}
		}
	}

	// =========================================================================
	// === PRE-HORNEADO PROCEDURAL DE GRADIENTES
	// =========================================================================

	private BufferedImage hornearTexturaMascaraHD(final int alphaCentro) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final float[] fracciones = { 0.0f, 0.50f, 1.0f };
		final Color[] colores = { new Color(255, 255, 255, alphaCentro),
				new Color(255, 255, 255, (int) (alphaCentro * 0.55f)), new Color(255, 255, 255, 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		g2d.fillOval(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD);
		g2d.dispose();
		return img;
	}

	private BufferedImage hornearTexturaMascaraConoHD(final double anguloApertura) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final float[] fracciones = { 0.0f, 0.55f, 1.0f };
		final Color[] colores = { new Color(255, 255, 255, 255), new Color(255, 255, 255, 150),
				new Color(255, 255, 255, 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		final double inicioAngulo = -(anguloApertura / 2.0);
		g2d.fill(new Arc2D.Double(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD, inicioAngulo, anguloApertura,
				Arc2D.PIE));
		g2d.dispose();
		return img;
	}

	private BufferedImage hornearTexturaColorHD(final TipoLuz tipo, final int nivelTermico) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final Color base = this.calcularColorTermico(tipo.getColorLuz(), nivelTermico, tipo == TipoLuz.MAGIA_ARCANO);
		final int aMax = (int) (tipo.getIntensidad() * 255.0f);
		final float[] fracciones = { 0.0f, 0.40f, 1.0f };
		final Color[] colores = { new Color(base.getRed(), base.getGreen(), base.getBlue(), aMax),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), (int) (aMax * 0.45f)),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		g2d.fillOval(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD);
		g2d.dispose();
		return img;
	}

	private BufferedImage hornearTexturaColorConoHD(final TipoLuz tipo, final int nivelTermico) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final Color base = this.calcularColorTermico(tipo.getColorLuz(), nivelTermico, false);
		final int aMax = (int) (tipo.getIntensidad() * 255.0f);
		final float[] fracciones = { 0.0f, 0.45f, 1.0f };
		final Color[] colores = { new Color(base.getRed(), base.getGreen(), base.getBlue(), aMax),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), (int) (aMax * 0.40f)),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		final double inicioAngulo = -(tipo.getAnguloAperturaGrados() / 2.0);
		g2d.fill(new Arc2D.Double(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD, inicioAngulo,
				tipo.getAnguloAperturaGrados(), Arc2D.PIE));
		g2d.dispose();
		return img;
	}

	private Color calcularColorTermico(final Color base, final int nivel, final boolean esArcano) {
		if (nivel == 1) {
			return base;
		}

		int r = base.getRed();
		int g = base.getGreen();
		int b = base.getBlue();

		if (esArcano) {
			if (nivel == 0) {
				g = Math.min(255, g + 25);
				b = Math.min(255, b + 15);
			} else {
				r = Math.min(255, r + 45);
				g = Math.max(0, g - 40);
			}
		} else if (nivel == 0) {
			r = Math.min(255, r + 15);
			g = Math.min(255, g + 35);
			b = Math.min(255, b + 15);
		} else {
			r = Math.min(255, r + 10);
			g = Math.max(0, g - 30);
			b = Math.max(0, b - 10);
		}

		return new Color(r, g, b);
	}

	// =========================================================================
	// === POOL Y GESTIÓN DE LUCES (ZERO-GC)
	// =========================================================================

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo) {
		return this.agregarLuzEstatica(x, y, tipo, (tipo != null) ? tipo.getRadioBase() : 75.0);
	}

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo, final double radio) {
		if ((this.topePila == 0) || (tipo == null)) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnFija(x, y, tipo, radio);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo) {
		return this.agregarLuzAnclada(ente, tipo, (tipo != null) ? tipo.getRadioBase() : 100.0);
	}

	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo, final double radio) {
		if ((ente == null) || (tipo == null)) {
			return null;
		}

		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz l = this.activas[i];
			if (l.getEnteAnclado() == ente) {
				l.spawnAnclada(ente, tipo, radio);
				return l;
			}
		}

		if (this.topePila == 0) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnAnclada(ente, tipo, radio);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public FuenteLuz dispararFlashPosicional(final double x, final double y, final double radio,
			final double duracion) {
		return this.dispararFlashPosicional(x, y, TipoLuz.DESTELLO_EXPLOSION, radio, duracion);
	}

	public FuenteLuz dispararFlashPosicional(final double x, final double y, final TipoLuz tipo, final double radio,
			final double duracion) {
		if ((this.topePila == 0) || (tipo == null)) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnTemporal(x, y, tipo, radio, duracion);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public void dispararFlashGlobal(final double duracionSegundos, final boolean esRelampago) {
		this.flashGlobalActivo = true;
		this.duracionFlashGlobal = Math.max(0.05, duracionSegundos);
		this.tiempoFlashGlobalRestante = this.duracionFlashGlobal;
		this.flashGlobalRelampago = esRelampago;
	}

	public void removerLuzDe(final Ente ente) {
		if (ente == null) {
			return;
		}
		for (int i = 0; i < this.cantidadActivas; i++) {
			if (this.activas[i].getEnteAnclado() == ente) {
				final FuenteLuz luz = this.activas[i];
				luz.apagar();
				this.indicesLibres[this.topePila++] = luz.getIndicePool();
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
				break;
			}
		}
	}

	public void apagarTodasLasLuces() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			luz.apagar();
			this.indicesLibres[this.topePila++] = luz.getIndicePool();
			this.activas[i] = null;
		}
		this.cantidadActivas = 0;
	}

	public boolean isPosicionIluminada(final double mundoX, final double mundoY) {
		if (!this.modoAmbienteFijo && (this.ciclo.getColorAmbienteActual().getAlpha() < 50)) {
			return true;
		}
		if (this.flashGlobalActivo) {
			return true;
		}

		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			final double dx = mundoX - luz.getPosX();
			final double dy = mundoY - luz.getPosY();
			final double distSq = (dx * dx) + (dy * dy);
			final double radio = luz.getRadioActual();

			if (distSq <= (radio * radio)) {
				if (!luz.getTipo().isEsCono()) {
					return true;
				}
				final double anguloPunto = Math.atan2(dy, dx);
				final double diff = Math.atan2(Math.sin(anguloPunto - luz.getAnguloRotacion()),
						Math.cos(anguloPunto - luz.getAnguloRotacion()));

				final double semiApertura = Math.toRadians(luz.getTipo().getAnguloAperturaGrados() / 2.0);
				if (Math.abs(diff) <= semiApertura) {
					return true;
				}
			}
		}
		return false;
	}

	public float getNivelLuzEn(final double mundoX, final double mundoY) {
		if (this.isPosicionIluminada(mundoX, mundoY)) {
			return 1.0f;
		}
		final Color c = this.modoAmbienteFijo ? this.colorAmbienteFijo : this.ciclo.getColorAmbienteActual();
		return 1.0f - (c.getAlpha() / 255.0f);
	}

	public int getAlphaOscuridadActual() {
		if (this.modoAmbienteFijo) {
			return this.colorAmbienteFijo.getAlpha();
		}
		return this.ciclo.getColorAmbienteActual().getAlpha();
	}

	public void establecerModoCueva(final boolean total) {
		this.establecerAmbienteTransicion(total ? new Color(0, 0, 0, 255) : new Color(10, 15, 30, 235), 1.2);
	}

	public void restablecerModoExterior() {
		this.iluminacionHabilitada = true;
		this.modoAmbienteFijo = false;
		this.transicionActiva = false;
		this.colorTinteBioma = null;
		this.factorInmersionBioma = 0.0;
	}

	public void establecerAmbienteTransicion(final Color destino, final double duracion) {
		this.iluminacionHabilitada = true;
		this.colorTransicionOrigen = this.modoAmbienteFijo ? this.colorAmbienteFijo
				: this.ciclo.getColorAmbienteActual();
		this.colorTransicionDestino = (destino != null) ? destino : new Color(0, 0, 0, 255);
		this.modoAmbienteFijo = true;
		this.tiempoTransicionTotal = Math.max(0.1, duracion);
		this.tiempoTransicionActual = 0.0;
		this.transicionActiva = true;
	}

	public void setTinteBiomaExterior(final Color tinte, final double factorInmersion) {
		this.colorTinteBioma = tinte;
		this.factorInmersionBioma = factorInmersion;
	}

	public void actualizar() {
		if (!this.iluminacionHabilitada) {
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		if (!this.modoAmbienteFijo) {
			this.ciclo.actualizar(dt);
		} else if (this.transicionActiva) {
			this.tiempoTransicionActual += dt;
			final double factor = Math.min(1.0, this.tiempoTransicionActual / this.tiempoTransicionTotal);

			final int r = (int) (this.colorTransicionOrigen.getRed()
					+ ((this.colorTransicionDestino.getRed() - this.colorTransicionOrigen.getRed()) * factor));
			final int g = (int) (this.colorTransicionOrigen.getGreen()
					+ ((this.colorTransicionDestino.getGreen() - this.colorTransicionOrigen.getGreen()) * factor));
			final int b = (int) (this.colorTransicionOrigen.getBlue()
					+ ((this.colorTransicionDestino.getBlue() - this.colorTransicionOrigen.getBlue()) * factor));
			final int a = (int) (this.colorTransicionOrigen.getAlpha()
					+ ((this.colorTransicionDestino.getAlpha() - this.colorTransicionOrigen.getAlpha()) * factor));

			this.colorAmbienteFijo = new Color(r, g, b, a);
			if (factor >= 1.0) {
				this.transicionActiva = false;
			}
		}

		final boolean hayTormenta = (Globales.GESTOR_CLIMA != null) && Globales.GESTOR_CLIMA.isTormentaActiva();
		this.rayosSol.actualizar(dt, this.ciclo.getHoraActual(), this.modoAmbienteFijo, hayTormenta);

		int i = 0;
		while (i < this.cantidadActivas) {
			final FuenteLuz luz = this.activas[i];

			if ((luz.getEnteAnclado() != null) && luz.getEnteAnclado().estaEliminado()) {
				luz.apagar();
				this.indicesLibres[this.topePila++] = luz.getIndicePool();
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
				continue;
			}

			luz.actualizar(dt);

			if (luz.isActiva()) {
				i++;
			} else {
				this.indicesLibres[this.topePila++] = luz.getIndicePool();
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
			}
		}

		if (this.flashGlobalActivo) {
			this.tiempoFlashGlobalRestante -= dt;
			if (this.tiempoFlashGlobalRestante <= 0.0) {
				this.flashGlobalActivo = false;
			}
		}
	}

	private void verificarLightmap(final Graphics2D g) {
		if ((this.lightmap == null) || (this.lightmap.getWidth() != Constantes.ANCHO_JUEGO)
				|| (this.lightmap.getHeight() != Constantes.ALTO_JUEGO)
				|| (this.lightmap.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.lightmap != null) {
				this.lightmap.flush();
			}
			this.lightmap = g.getDeviceConfiguration().createCompatibleVolatileImage(Constantes.ANCHO_JUEGO,
					Constantes.ALTO_JUEGO, Transparency.TRANSLUCENT);
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.iluminacionHabilitada) {
			return;
		}

		this.rayosSol.pintar(g);

		int rBase, gBase, bBase, aBase;
		if (this.modoAmbienteFijo) {
			rBase = this.colorAmbienteFijo.getRed();
			gBase = this.colorAmbienteFijo.getGreen();
			bBase = this.colorAmbienteFijo.getBlue();
			aBase = this.colorAmbienteFijo.getAlpha();
		} else if ((this.colorTinteBioma != null) && (this.factorInmersionBioma > 0.0)) {
			final Color solar = this.ciclo.getColorAmbienteActual();
			final double f = this.factorInmersionBioma;
			rBase = (int) (solar.getRed() + ((this.colorTinteBioma.getRed() - solar.getRed()) * f * 0.6));
			gBase = (int) (solar.getGreen() + ((this.colorTinteBioma.getGreen() - solar.getGreen()) * f * 0.6));
			bBase = (int) (solar.getBlue() + ((this.colorTinteBioma.getBlue() - solar.getBlue()) * f * 0.6));
			aBase = Math.max(solar.getAlpha(), (int) (this.colorTinteBioma.getAlpha() * f));
		} else {
			final Color solar = this.ciclo.getColorAmbienteActual();
			rBase = solar.getRed();
			gBase = solar.getGreen();
			bBase = solar.getBlue();
			aBase = solar.getAlpha();
		}

		final double factorFlash = (this.flashGlobalActivo && (this.duracionFlashGlobal > 0.0))
				? (this.tiempoFlashGlobalRestante / this.duracionFlashGlobal)
				: 0.0;

		final int alphaSombra = (int) Math.round(aBase * (1.0 - factorFlash));

		if ((alphaSombra <= 0) && (!this.flashGlobalActivo || !this.flashGlobalRelampago)) {
			return;
		}

		this.verificarLightmap(g);

		do {
			final int val = this.lightmap.validate(g.getDeviceConfiguration());
			if (val == VolatileImage.IMAGE_INCOMPATIBLE) {
				this.verificarLightmap(g);
			}

			final Graphics2D gLight = this.lightmap.createGraphics();
			try {
				gLight.setComposite(COMPOSITE_LIMPIEZA);
				gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);

				if (alphaSombra > 0) {
					if ((rBase != this.lastBaseR) || (gBase != this.lastBaseG) || (bBase != this.lastBaseB)
							|| (alphaSombra != this.lastBaseA)) {
						this.lastBaseR = rBase;
						this.lastBaseG = gBase;
						this.lastBaseB = bBase;
						this.lastBaseA = alphaSombra;
						this.colorAmbienteCalculado = new Color(rBase, gBase, bBase, alphaSombra);
					}
					gLight.setComposite(COMPOSITE_NORMAL);
					gLight.setColor(this.colorAmbienteCalculado);
					gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
				}

				if (this.cantidadActivas > 0) {
					this.pintarLuces(gLight, alphaSombra);
				}

				if (this.flashGlobalActivo && this.flashGlobalRelampago && (factorFlash > 0.5)) {
					final int idxRel = Math.max(0, Math.min(10, (int) Math.round(((factorFlash - 0.5) / 0.5) * 10.0)));
					gLight.setComposite(COMPOSITES_RELAMPAGO[idxRel]);
					gLight.setColor(Color.WHITE);
					gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
				}

			} finally {
				gLight.dispose();
			}

			Render2D.dibujarImagen(g, this.lightmap, 0, 0);

		} while (this.lightmap.contentsLost());
	}

	private void pintarLuces(final Graphics2D gLight, final int alphaSombra) {
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoomFinal() : 1.0;
		final double shakeX = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetX() : 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetY() : 0.0;
		final double rotCam = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getAnguloRotacion() : 0.0;

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		final int enteAncho = ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null))
				? Globales.CAMARA.getEntidadEnfocada().getAncho()
				: 0;
		final int enteAlto = ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null))
				? Globales.CAMARA.getEntidadEnfocada().getAlto()
				: 0;

		final double centroMundoCamX = camX + (enteAncho / 2.0);
		final double centroMundoCamY = camY + (enteAlto / 2.0);

		final double cos = Math.cos(rotCam);
		final double sin = Math.sin(rotCam);

		// =====================================================================
		// PASE A: PERFORACIÓN DE PENUMBRA (DST_OUT) + OCLUSIÓN DE SOMBRAS 2D
		// =====================================================================
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			final int radioPantalla = (int) Math.round(luz.getRadioActual() * z);
			final int diametro = radioPantalla * 2;

			final double dx = (luz.getPosX() - centroMundoCamX) * z;
			final double dy = (luz.getPosY() - centroMundoCamY) * z;
			final double rx = (dx * cos) - (dy * sin);
			final double ry = (dx * sin) + (dy * cos);

			final int screenX = (int) Math.round(Constantes.CENTROX + shakeX + rx) - radioPantalla;
			final int screenY = (int) Math.round(Constantes.CENTROY + shakeY + ry) - radioPantalla;

			if (((screenX + diametro) < 0) || (screenX > Constantes.ANCHO_JUEGO) || ((screenY + diametro) < 0)
					|| (screenY > Constantes.ALTO_JUEGO)) {
				continue;
			}

			gLight.setComposite(COMPOSITE_PERFORAR);
			if (luz.getTipo().isEsCono()) {
				final double rotTotal = luz.getAnguloRotacion() + rotCam;
				final int cx = screenX + radioPantalla;
				final int cy = screenY + radioPantalla;
				gLight.rotate(rotTotal, cx, cy);
				gLight.drawImage(this.texturasMascaraConoHD[luz.getTipo().ordinal()], screenX, screenY, diametro,
						diametro, null);
				gLight.rotate(-rotTotal, cx, cy);
			} else if (luz.getTipo() == TipoLuz.AURA_JUGADOR) {
				// Aura del Jugador: Perforación suave de penumbra tenue (sin agujero blanco)
				gLight.drawImage(this.texturaMascaraAuraHD, screenX, screenY, diametro, diametro, null);
			} else {
				// Antorchas, fogatas y fuego: Perforación plena
				gLight.drawImage(this.texturaMascaraAlphaHD, screenX, screenY, diametro, diametro, null);
			}

			// Oclusión de sombras detrás de muros (solo para fuentes de luz reales)
			if (luz.getTipo() != TipoLuz.AURA_JUGADOR) {
				this.oclusorSombras.proyectarSombrasPaseA(gLight, luz, centroMundoCamX, centroMundoCamY, z, shakeX,
						shakeY, this.colorAmbienteCalculado);
			}
		}

		// =====================================================================
		// PASE B: TINTE CROMÁTICO TÉRMICO
		// =====================================================================
		final int indiceCompositeTinte = Math.max(0, Math.min(10, (int) Math.round((alphaSombra / 200.0) * 10.0)));

		if (indiceCompositeTinte > 0) {
			for (int i = 0; i < this.cantidadActivas; i++) {
				final FuenteLuz luz = this.activas[i];
				final int radioPantalla = (int) Math.round(luz.getRadioActual() * z);
				final int diametro = radioPantalla * 2;

				final double dx = (luz.getPosX() - centroMundoCamX) * z;
				final double dy = (luz.getPosY() - centroMundoCamY) * z;
				final double rx = (dx * cos) - (dy * sin);
				final double ry = (dx * sin) + (dy * cos);

				final int screenX = (int) Math.round(Constantes.CENTROX + shakeX + rx) - radioPantalla;
				final int screenY = (int) Math.round(Constantes.CENTROY + shakeY + ry) - radioPantalla;

				if (((screenX + diametro) < 0) || (screenX > Constantes.ANCHO_JUEGO) || ((screenY + diametro) < 0)
						|| (screenY > Constantes.ALTO_JUEGO)) {
					continue;
				}

				final int nivelTermico = luz.getVarianteTermica();
				final int ordinalTipo = luz.getTipo().ordinal();

				gLight.setComposite(COMPOSITES_TINTE_ATENUADO[indiceCompositeTinte]);
				if (luz.getTipo().isEsCono()) {
					final double rotTotal = luz.getAnguloRotacion() + rotCam;
					final int cx = screenX + radioPantalla;
					final int cy = screenY + radioPantalla;
					gLight.rotate(rotTotal, cx, cy);
					gLight.drawImage(this.texturasHaloColorCono[ordinalTipo][nivelTermico], screenX, screenY, diametro,
							diametro, null);
					gLight.rotate(-rotTotal, cx, cy);
				} else {
					gLight.drawImage(this.texturasHaloColor[ordinalTipo][nivelTermico], screenX, screenY, diametro,
							diametro, null);
				}

				if (luz.getTipo() != TipoLuz.AURA_JUGADOR) {
					this.oclusorSombras.proyectarSombrasPaseB(gLight, luz, centroMundoCamX, centroMundoCamY, z, shakeX,
							shakeY);
				}
			}
		}
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public CicloDiaNoche getCiclo() {
		return this.ciclo;
	}

	public GestorRayosSol getRayosSol() {
		return this.rayosSol;
	}

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}

	public void setIluminacionHabilitada(final boolean h) {
		this.iluminacionHabilitada = h;
	}

	public boolean isIluminacionHabilitada() {
		return this.iluminacionHabilitada;
	}

	public boolean isModoAmbienteFijo() {
		return this.modoAmbienteFijo;
	}

	public Color getColorAmbienteFijo() {
		return this.colorAmbienteFijo;
	}
}