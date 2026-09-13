package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.Estacion;
import principal.recursos.ClaveHoja;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Módulo visual del HUD que renderiza la tarjeta táctica del Ciclo Solar y
 * Calendario. Integra marco biselado hermanado con el termómetro, astrolabio
 * rotatorio empotrado a la derecha y telemetría micro pixel-art de 4 líneas a
 * la izquierda (Zero-GC / O(1)).
 * 
 * @version 5.1 (Vanilla Java 8 - Micro m3x6 Typography & Anti-Overlap Layout)
 */
public class RelojCiclo {

	private static final int MARGEN_DERECHO = 6;
	private static final int ANCHO_TARJETA = 120; // 120 px: otorga 70 px limpios para el texto
	private static final int ALTO_TARJETA = 48;

	private static final int DIAMETRO_MARCO = 44;
	private static final int RADIO_DISCO = 20;

	// Paleta cromática compartida con TermometroIGU
	private static final Color COLOR_FONDO = new Color(16, 20, 26, 225);
	private static final Color COLOR_BORDE = new Color(55, 60, 75, 240);

	private static final Color COLOR_ANIO_SEM = Color.WHITE;
	private static final Color COLOR_DIA_SEMANA = new Color(210, 225, 240);
	private static final Color COLOR_HORA = new Color(255, 215, 90);

	private final Rectangle areaTarjeta;
	private final int xMarco;
	private final int yMarco;
	private final int centroDialX;
	private final int centroDialY;

	private final HojaSprite hojaDisco;
	private BufferedImage imgDisco;
	private final BufferedImage imgMarco;

	// Caché Dirty-Flag (Zero-GC)
	private int lastMinuto = -1;
	private int lastDia = -1;

	private String cachedEstacion = "Primavera";
	private Color cachedColorEstacion = Estacion.PRIMAVERA.getColorIdentificador();
	private String cachedAnioSem = "Año 1 · Sem 1";
	private String cachedDia = "Día 1 · Lun";
	private String cachedHora = "12:00";

	private boolean visible = true;

	public RelojCiclo() {
		final int posX = Constantes.ANCHO_JUEGO - ANCHO_TARJETA - MARGEN_DERECHO; // 640 - 112 - 6 = 522
		final int posY = 6;

		this.areaTarjeta = new Rectangle(posX, posY, ANCHO_TARJETA, ALTO_TARJETA);

		// El marco de 44x44 queda empotrado a la derecha con 2 px de margen interior
		this.xMarco = (posX + ANCHO_TARJETA) - DIAMETRO_MARCO - 2;
		this.yMarco = posY + 2;

		this.centroDialX = this.xMarco + (DIAMETRO_MARCO / 2);
		this.centroDialY = this.yMarco + (DIAMETRO_MARCO / 2);

		this.hojaDisco = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.IGU_DISCO_CICLO_TIME);
		final HojaSprite hojaMarco = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.IGU_MARCO_TIME);

		this.imgDisco = (this.hojaDisco != null) ? this.hojaDisco.getSprite(0)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
		this.imgMarco = (hojaMarco != null) ? hojaMarco.getSprite(0) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public void actualizar() {
		if (!this.visible || (Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();
		final int minutoActual = (int) Math.round(ciclo.getHoraActual() * 60.0);
		final int diaActual = ciclo.getDiaActual();

		if ((minutoActual != this.lastMinuto) || (diaActual != this.lastDia)) {
			this.lastMinuto = minutoActual;
			this.lastDia = diaActual;

			final int anio = ciclo.getAnioActual();
			final int semanaAnio = ciclo.getSemanaAnio();
			final int diaAnio = ciclo.getDiaDelAnio();
			final String diaSemanaCorto = ciclo.getNombreDiaSemanaCorto();
			final Estacion est = ciclo.getEstacionActual();

			this.cachedEstacion = est.getNombre();
			this.cachedColorEstacion = est.getColorIdentificador();
			this.cachedAnioSem = "Año " + anio + " · Sem " + semanaAnio;
			this.cachedDia = "Día " + diaAnio + " · " + diaSemanaCorto;
			this.cachedHora = ciclo.getHoraFormato24h();

			if (this.hojaDisco != null) {
				this.imgDisco = this.hojaDisco.getSprite(est.ordinal());
			}
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.visible || (Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return;
		}

		final int x = this.areaTarjeta.x;
		final int y = this.areaTarjeta.y;
		final int w = this.areaTarjeta.width;
		final int h = this.areaTarjeta.height;

		// 1. Chasis táctico idéntico al termómetro
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		// 2. Astrolabio Rotatorio empotrado a la derecha
		final double horaActual = Globales.GESTOR_LUZ.getCiclo().getHoraActual();
		final double anguloRadianes = -((horaActual / 24.0) * (Math.PI * 2.0));

		final AffineTransform transformOriginal = g.getTransform();
		try {
			g.translate(this.centroDialX, this.centroDialY);
			g.rotate(anguloRadianes);
			Render2D.dibujarImagen(g, this.imgDisco, -RADIO_DISCO, -RADIO_DISCO);
		} finally {
			g.setTransform(transformOriginal);
		}

		// 3. Bisel exterior ornamental dorado
		Render2D.dibujarImagen(g, this.imgMarco, this.xMarco, this.yMarco);

		// 4. Telemetría micro pixel-art a la izquierda
		this.pintarTelemetriaCompacta(g, x + 6, y);
	}

	private void pintarTelemetriaCompacta(final Graphics2D g, final int xTexto, final int yBase) {
		final Font fontPrevia = g.getFont();

		// =====================================================================
		// LÍNEAS 1, 2 Y 3: m5x7 Fina a 14f (Soporte nativo de 'ñ' y trazo nítido)
		// =====================================================================
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		// Línea 1: Estación
		Render2D.dibujarStringConSombra(g, this.cachedEstacion, xTexto, yBase + 11, this.cachedColorEstacion,
				Color.BLACK);

		// Línea 2: Año y Semana (¡La 'ñ' se dibuja impecable!)
		Render2D.dibujarStringConSombra(g, this.cachedAnioSem, xTexto, yBase + 21, COLOR_ANIO_SEM, Color.BLACK);

		// Línea 3: Día y DíaSem
		Render2D.dibujarStringConSombra(g, this.cachedDia, xTexto, yBase + 31, COLOR_DIA_SEMANA, Color.BLACK);

		// =====================================================================
		// LÍNEA 4: Hora Digital (Más grande y destacada en BOLD 16f)
		// =====================================================================
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));
		Render2D.dibujarStringConSombra(g, this.cachedHora, xTexto, yBase + 44, COLOR_HORA, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public Rectangle getArea() {
		return this.areaTarjeta;
	}

}