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

/**
 * Slot de equipamiento especializado en el arma activa del jugador. Renderizado
 * Pixel-Art con sombra de contraste y micro-barra de estado (Zero-GC).
 * 
 * @version 3.1 (Vanilla Java 8 - Crisp Pixel Art)
 */
public class SlotArma extends SlotEquipamiento {

	private static final String RUTA_LOGO_ARMA = "/imagenes/objetos/gun16x12_transparente.png";

	// =========================================================================
	// === PALETA CROMÁTICA REACTIVA ZERO-GC
	// =========================================================================
	private static final Color COLOR_TEXTO_LLENO = new Color(255, 255, 255);
	private static final Color COLOR_TEXTO_MEDIO = new Color(255, 205, 50);
	private static final Color COLOR_TEXTO_VACIO = new Color(255, 65, 65);

	private static final Color COLOR_BARRA_FONDO = new Color(15, 15, 20, 220);
	private static final Color COLOR_BARRA_LLENA = new Color(40, 220, 240);
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
			// 1. Dibujar textura del arma limpia
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);

			// 2. Renderizado táctico de munición
			if (this.item instanceof Arma) {
				final Arma arma = (Arma) this.item;
				if (arma.esArmaDistancia()) {
					final Font fuentePrevia = g.getFont();
					g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 5.5f));

					if (arma.isRecargando()) {
						// Texto de recarga centrado con sombra negra
						final String txtRec = "REC";
						final int anchoRec = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtRec);
						final int xRec = area.x + ((area.width - anchoRec) / 2);
						final int yRec = (area.y + area.height) - 3;

						Render2D.dibujarStringConSombra(g, txtRec, xRec, yRec, COLOR_TEXTO_RECARGA, Color.BLACK);
					} else {
						final int balas = arma.getBalasCargador();
						final int capacidad = Math.max(1, arma.getCapacidadCargador());
						final double ratio = (double) balas / capacidad;

						// A. Micro-barra de cargador en la base (1 px de alto)
						final int barraAnchoMax = area.width - 2;
						final int barraProgreso = (int) Math.round(ratio * barraAnchoMax);
						final int barraX = area.x + 1;
						final int barraY = (area.y + area.height) - 2;

						Render2D.dibujarRectanguloRelleno(g, barraX, barraY, barraAnchoMax, 1, COLOR_BARRA_FONDO);
						if (barraProgreso > 0) {
							final Color colorBarra = (ratio > 0.35) ? COLOR_BARRA_LLENA
									: ((ratio > 0.15) ? COLOR_TEXTO_MEDIO : COLOR_TEXTO_VACIO);
							Render2D.dibujarRectanguloRelleno(g, barraX, barraY, barraProgreso, 1, colorBarra);
						}

						// B. Número en esquina inferior derecha con sombra de contraste de 1 px
						final String txtBalas = String.valueOf(balas);
						final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtBalas);

						final int xNum = (area.x + area.width) - anchoTexto - 1;
						final int yNum = (area.y + area.height) - 4;

						final Color colorNumero = (balas == 0) ? COLOR_TEXTO_VACIO
								: ((ratio <= 0.30) ? COLOR_TEXTO_MEDIO : COLOR_TEXTO_LLENO);

						Render2D.dibujarStringConSombra(g, txtBalas, xNum, yNum, colorNumero, Color.BLACK);
					}

					g.setFont(fuentePrevia);
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