package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.inventario.CajaInfo;
import principal.inventario.Info;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

/**
 * Slot de equipamiento especializado en la gestión del arma activa del jugador.
 * Sincroniza dinámicamente el conteo de balas en recámara, reserva total en
 * mochila y estado visual de recarga activa en tiempo real (Zero-GC).
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class SlotArma extends SlotEquipamiento {

	private static final Font FUENTE_MUNICION = new Font(Font.SANS_SERIF, Font.PLAIN, 4);
	private static final String RUTA_LOGO_ARMA = "/imagenes/objetos/gun16x12_transparente.png";

	private static final Color COLOR_FONDO_RECARGA = new Color(30, 30, 30, 220);
	private static final Color COLOR_TEXTO_RECARGA = new Color(255, 185, 40);

	private static final String CLAVE_ATAQUE = "Ataque";
	private static final String CLAVE_ALCANCE = "Alcance";
	private static final String CLAVE_PENETRANTE = "Penetrante";
	private static final String CLAVE_MUNICION = "Municion";

	protected final HashMap<String, Info> lista;
	protected final CajaInfo cajaInfo;

	private final Info infoAtaque;
	private final Info infoAlcance;
	private final Info infoPenetrante;
	private final Info infoMunicion;

	public SlotArma(final Rectangle area, final CajaInfo cajaInfo) {
		super(area, Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(RUTA_LOGO_ARMA));

		this.cajaInfo = cajaInfo;
		this.lista = new HashMap<String, Info>();

		this.infoAtaque = new Info(CLAVE_ATAQUE, "0");
		this.infoAlcance = new Info(CLAVE_ALCANCE, "0");
		this.infoPenetrante = new Info(CLAVE_PENETRANTE, "false");
		this.infoMunicion = new Info(CLAVE_MUNICION, "0");
	}

	@Override
	public void actualizar(final Raton raton) {
		super.actualizar(raton);
		if (this.item instanceof Arma) {
			this.sincronizarValoresArma((Arma) this.item);
		}
	}

	/**
	 * Sincroniza dinámicamente la munición del cargador y la reserva total de la
	 * mochila mutando las instancias persistentes de Info sin generar objetos para
	 * el Garbage Collector.
	 */
	private void sincronizarValoresArma(final Arma arma) {
		if (arma.esArmaDistancia() && this.lista.containsKey(CLAVE_MUNICION)) {
			final int reserva = Globales.GESTOR_INVENTARIO.getInventarioJugador()
					.contarMunicionTotal(arma.getTipoMunicionRequerida());

			final String textoMunicion = arma.isRecargando() ? "REC... [" + reserva + "]"
					: arma.getBalasCargador() + "/" + arma.getCapacidadCargador() + " [" + reserva + "]";

			this.infoMunicion.establecerValor(textoMunicion);
		}
	}

	private void actualizarLista() {
		this.lista.clear();

		if (this.item instanceof Arma) {
			final Arma arma = (Arma) this.item;

			this.infoAtaque.establecerValor(String.valueOf(arma.getAtaque()));
			this.infoAlcance.establecerValor(String.valueOf(arma.getAlcance()));
			this.infoPenetrante.establecerValor(String.valueOf(arma.esPenetrante()));

			this.lista.put(CLAVE_ATAQUE, this.infoAtaque);
			this.lista.put(CLAVE_ALCANCE, this.infoAlcance);
			this.lista.put(CLAVE_PENETRANTE, this.infoPenetrante);

			if (arma.esArmaDistancia()) {
				final int reserva = Globales.GESTOR_INVENTARIO.getInventarioJugador()
						.contarMunicionTotal(arma.getTipoMunicionRequerida());
				this.infoMunicion.establecerValor(
						arma.getBalasCargador() + "/" + arma.getCapacidadCargador() + " [" + reserva + "]");
				this.lista.put(CLAVE_MUNICION, this.infoMunicion);
			}
		}

		if (this.cajaInfo != null) {
			this.cajaInfo.actualizarLista(this.lista);
		}
	}

	@Override
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item != null) {
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);

			if (this.item instanceof Arma) {
				final Arma arma = (Arma) this.item;
				if (arma.esArmaDistancia()) {
					final Font fuenteOriginal = g.getFont();
					g.setFont(FUENTE_MUNICION);

					if (arma.isRecargando()) {
						Render2D.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - 6, 14, 5,
								COLOR_FONDO_RECARGA);
						Render2D.dibujarString(g, "REC...", area.x + 1, (area.y + area.height) - 2,
								COLOR_TEXTO_RECARGA);
					} else {
						final String cantidadBalas = String.valueOf(arma.getBalasCargador());
						final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidadBalas);
						final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);

						Render2D.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - altoTexto - 1, 11, 6,
								Color.LIGHT_GRAY);
						Render2D.dibujarString(g, cantidadBalas, area.x, (area.y + area.height) - (altoTexto / 2),
								Color.BLACK);
						Render2D.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x + anchoTexto,
								(area.y + area.height) - altoTexto);
					}

					g.setFont(fuenteOriginal);
				}
			}
		} else if (this.logo != null) {
			Render2D.dibujarImagen(g, this.logo, area.x + 1, area.y + 5);
		}
	}

	@Override
	public boolean puedeAceptar(final Item itemAColocar) {
		return (itemAColocar instanceof Arma);
	}

	@Override
	public boolean validarAdmisionItem(final Item i) {
		return (i == null) || (i instanceof Arma);
	}

	@Override
	public void establecerObjeto(final Item obj) {
		super.establecerObjeto(obj);
		this.actualizarLista();
	}

	@Override
	public void eliminarObjeto() {
		super.eliminarObjeto();
		this.actualizarLista();
	}

	public HashMap<String, Info> getLista() {
		return this.lista;
	}
}