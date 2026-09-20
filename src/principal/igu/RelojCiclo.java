package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import principal.astronomia.Estacion;
import principal.astronomia.GestorAstronomico;
import principal.recursos.ClaveHoja;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Módulo visual del HUD que renderiza la tarjeta del Ciclo Solar y Calendario.
 * Consulta directamente al Dominio Astronómico (Zero-GC / O(1)).
 * 
 * @version 6.0 (Vanilla Java 8 - Sovereign Astronomy Integration)
 */
public class RelojCiclo {

	private static final int MARGEN_DERECHO = 6;
	private static final int ANCHO_TARJETA = 120;
	private static final int ALTO_TARJETA = 48;

	private static final int DIAMETRO_MARCO = 44;
	private static final int RADIO_DISCO = 20;

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

	private int lastMinuto = -1;
	private int lastDia = -1;

	private String cachedEstacion = "Primavera";
	private Color cachedColorEstacion = Estacion.PRIMAVERA.getColorIdentificador();
	private String cachedAnioSem = "Año 1 · Sem 1";
	private String cachedDia = "Día 1 · Lun";
	private String cachedHora = "12:00";

	private boolean visible = true;

	public RelojCiclo() {
		final int posX = Constantes.ANCHO_JUEGO - ANCHO_TARJETA - MARGEN_DERECHO;
		final int posY = 6;

		this.areaTarjeta = new Rectangle(posX, posY, ANCHO_TARJETA, ALTO_TARJETA);
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
		if (!this.visible || (Globales.GESTOR_ASTRONOMICO == null)) {
			return;
		}

		final GestorAstronomico astro = Globales.GESTOR_ASTRONOMICO;
		final int minutoActual = (int) Math.round(astro.getHoraActual() * 60.0);
		final int diaActual = astro.getDiaActual();

		if ((minutoActual != this.lastMinuto) || (diaActual != this.lastDia)) {
			this.lastMinuto = minutoActual;
			this.lastDia = diaActual;

			final int anio = astro.getAnioActual();
			final int semanaAnio = astro.getSemanaAnio();
			final int diaAnio = astro.getDiaDelAnio();
			final String diaSemanaCorto = astro.getNombreDiaSemanaCorto();
			final Estacion est = astro.getEstacionActual();

			this.cachedEstacion = est.getNombre();
			this.cachedColorEstacion = est.getColorIdentificador();
			this.cachedAnioSem = "Año " + anio + " · Sem " + semanaAnio;
			this.cachedDia = "Día " + diaAnio + " · " + diaSemanaCorto;
			this.cachedHora = astro.getHoraFormato24h();

			if (this.hojaDisco != null) {
				this.imgDisco = this.hojaDisco.getSprite(est.ordinal());
			}
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.visible || (Globales.GESTOR_ASTRONOMICO == null)) {
			return;
		}

		final int x = this.areaTarjeta.x;
		final int y = this.areaTarjeta.y;
		final int w = this.areaTarjeta.width;
		final int h = this.areaTarjeta.height;

		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final double horaActual = Globales.GESTOR_ASTRONOMICO.getHoraActual();
		final double anguloRadianes = -((horaActual / 24.0) * (Math.PI * 2.0));

		final AffineTransform transformOriginal = g.getTransform();
		try {
			g.translate(this.centroDialX, this.centroDialY);
			g.rotate(anguloRadianes);
			Render2D.dibujarImagen(g, this.imgDisco, -RADIO_DISCO, -RADIO_DISCO);
		} finally {
			g.setTransform(transformOriginal);
		}

		Render2D.dibujarImagen(g, this.imgMarco, this.xMarco, this.yMarco);
		this.pintarTelemetriaCompacta(g, x + 6, y);
	}

	private void pintarTelemetriaCompacta(final Graphics2D g, final int xTexto, final int yBase) {
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		Render2D.dibujarStringConSombra(g, this.cachedEstacion, xTexto, yBase + 11, this.cachedColorEstacion,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, this.cachedAnioSem, xTexto, yBase + 21, COLOR_ANIO_SEM, Color.BLACK);
		Render2D.dibujarStringConSombra(g, this.cachedDia, xTexto, yBase + 31, COLOR_DIA_SEMANA, Color.BLACK);

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