package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import principal.configuracion.Dificultad;
import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.entes.objetos.EntradaCueva;
import principal.entes.objetos.fabricables.Cama;
import principal.entes.objetos.fabricables.Carpa;
import principal.entes.objetos.fabricables.Fogata;
import principal.entes.objetos.items.desplegables.ItemBrujula;
import principal.entes.objetos.items.equipablesmano.ItemMapa;
import principal.inventario.equipamiento.SlotManager;
import principal.mapa.Mundo;
import principal.mapa.NieblaGuerra;
import principal.mapa.Tile;
import principal.mapa.escenario.tps.PuertaSalidaCueva;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.recursos.ClaveHoja;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Widget cartográfico diegético puro. - Sin brújula: el mapa es tinta estática
 * en papel. No se centra en ti, no tiene halo brillante en tiempo real y
 * recuerda dónde lo dejaste al navegar. - Con brújula: se magnetiza, te sigue
 * los pasos, orienta la aguja y activa el HUD en Normal.
 * 
 * @version 6.0 (Vanilla Java 8 - Master Diegetic Cartography)
 */
public class MinimapaIGU extends ComponenteMenu {

	private static final int ICONO_PIONERO = 0;
	private static final int ICONO_CARPA = 1;
	private static final int ICONO_CAMA = 2;
	private static final int ICONO_FOGATA = 3;
	private static final int ICONO_CUEVA_ENTRADA = 4;
	private static final int ICONO_CUEVA_SALIDA = 5;
	private static final int ICONO_MASCOTA = 6;
	private static final int ICONO_COFRE = 7;
	private static final int ICONO_AGUJA_BRUJULA = 8;

	private static final int LADO_HUD = 76;
	private static final int MARGEN_HUD_X = 6;
	private static final int MARGEN_HUD_Y = 6;

	private static final int ANCHO_PERGAMINO = 320;
	private static final int ALTO_PERGAMINO = 210;
	private static final int MARGEN_INTERNO_X = 8;
	private static final int MARGEN_INTERNO_Y = 22;

	private final Rectangle areaHUD;
	private final Rectangle areaPergamino;
	private final Rectangle areaLienzoInterno;
	private boolean modoPergaminoAbierto = false;
	// Máquina de estados: Seguimiento GPS vs Inspección libre
	private boolean siguiendoJugador = false;
	private boolean lastTieneBrujula = false;
	// Búferes VRAM
	private VolatileImage bufferPergamino;
	private VolatileImage bufferHUD;
	private boolean pergaminoSucio = true;
	private boolean hudSucio = true;

	private long lastVersionNiebla = -1;
	private int lastPlayerTileX = Integer.MIN_VALUE;
	private int lastPlayerTileY = Integer.MIN_VALUE;

	// Ancla de posición persistente de la vista del mapa
	private String lastMundoNombre = "";
	private int centroFijadoTileX = Integer.MIN_VALUE;
	private int centroFijadoTileY = Integer.MIN_VALUE;

	// Navegación táctica (Pan)
	private int panTileOffsetX = 0;
	private int panTileOffsetY = 0;
	private int lastMouseDragX = -1;
	private int lastMouseDragY = -1;
	private boolean arrastrando = false;

	// Paleta cromática
	private static final Color FONDO_RADAR = new Color(12, 14, 20, 230);
	private static final Color BORDE_RADAR = new Color(55, 65, 80, 255);
	private static final Color BISEL_RADAR = new Color(25, 30, 40, 200);

	private static final Color FONDO_PERGAMINO = new Color(24, 20, 15, 245);
	private static final Color BORDE_PERGAMINO = new Color(135, 100, 60, 255);
	private static final Color TEXTO_TITULO = new Color(225, 195, 140);
	private static final Color TEXTO_ADVERTENCIA = new Color(210, 70, 70);
	private static final Color TEXTO_CARDINAL = new Color(200, 175, 120);
	private static final Color TEXTO_GUIA = new Color(160, 140, 110);

	private static final Color COL_HIERBA_ACT = new Color(50, 125, 50);
	private static final Color COL_HIERBA_MEM = new Color(22, 45, 22);
	private static final Color COL_AGUA_ACT = new Color(40, 95, 175);
	private static final Color COL_AGUA_MEM = new Color(18, 35, 60);
	private static final Color COL_TIERRA_ACT = new Color(115, 85, 50);
	private static final Color COL_TIERRA_MEM = new Color(45, 35, 22);
	private static final Color COL_PIEDRA_ACT = new Color(100, 105, 115);
	private static final Color COL_PIEDRA_MEM = new Color(40, 42, 48);
	private static final Color COL_MURO_ACT = new Color(130, 50, 50);
	private static final Color COL_MURO_MEM = new Color(45, 20, 20);

	public MinimapaIGU() {
		super(new Rectangle(MARGEN_HUD_X, MARGEN_HUD_Y, LADO_HUD, LADO_HUD));
		this.areaHUD = this.area;
		this.areaPergamino = new Rectangle((Constantes.ANCHO_JUEGO - ANCHO_PERGAMINO) / 2,
				(Constantes.ALTO_JUEGO - ALTO_PERGAMINO) / 2, ANCHO_PERGAMINO, ALTO_PERGAMINO);

		this.areaLienzoInterno = new Rectangle(this.areaPergamino.x + MARGEN_INTERNO_X,
				this.areaPergamino.y + MARGEN_INTERNO_Y, this.areaPergamino.width - (MARGEN_INTERNO_X * 2),
				this.areaPergamino.height - MARGEN_INTERNO_Y - MARGEN_INTERNO_X);
	}

	public void conmutarPergamino() {
		this.modoPergaminoAbierto = !this.modoPergaminoAbierto;
		if (this.modoPergaminoAbierto) {
			final Mundo mundo = (Globales.JUGADOR != null) ? Globales.JUGADOR.getMundo() : null;
			if ((mundo != null) && (mundo.getNieblaGuerra() != null)) {
				mundo.getNieblaGuerra().actualizar(Globales.JUGADOR, mundo);
			}

			this.asegurarCentroInicial(mundo);
			this.marcarSucio();
		}
	}

	private void asegurarCentroInicial(final Mundo mundo) {
		if ((mundo == null) || (Globales.JUGADOR == null)) {
			return;
		}

		// Si cambió de submundo (ej. entró a una cueva) o es la primera vez que abre el
		// mapa en este mundo
		if (!mundo.getNombreMundo().equals(this.lastMundoNombre) || (this.centroFijadoTileX == Integer.MIN_VALUE)) {
			this.lastMundoNombre = mundo.getNombreMundo();

			// Ancla exactamente en la posición donde el jugador abrió el mapa por primera
			// vez en esta región
			this.centroFijadoTileX = Math.floorDiv(Globales.JUGADOR.getCentroX(), Constantes.LADO_TILE);
			this.centroFijadoTileY = Math.floorDiv(Globales.JUGADOR.getCentroY(), Constantes.LADO_TILE);

			this.panTileOffsetX = 0;
			this.panTileOffsetY = 0;
		}
	}

	public void cerrarPergamino() {
		this.modoPergaminoAbierto = false;
	}

	public boolean isPergaminoAbierto() {
		return this.modoPergaminoAbierto;
	}

	public void marcarSucio() {
		this.pergaminoSucio = true;
		this.hudSucio = true;
	}

	public void recentrar() {
		this.panTileOffsetX = 0;
		this.panTileOffsetY = 0;
		// Si tiene brújula, presionar recentrar reactiva el seguimiento automático del
		// jugador
		if (this.validarPermisoBrujula()) {
			this.siguiendoJugador = true;
			if (Globales.JUGADOR != null) {
				this.centroFijadoTileX = Math.floorDiv(Globales.JUGADOR.getCentroX(), Constantes.LADO_TILE);
				this.centroFijadoTileY = Math.floorDiv(Globales.JUGADOR.getCentroY(), Constantes.LADO_TILE);
			}
		}
		this.marcarSucio();
	}

	@Override
	public void actualizar(final Raton raton) {
		final boolean tieneBrujula = this.validarPermisoBrujula();

		// Transición de estados de la brújula:
		// Al equiparla por primera vez -> Se engancha automáticamente al jugador
		if (tieneBrujula && !this.lastTieneBrujula) {
			this.siguiendoJugador = true;
			this.recentrar();
		} else if (!tieneBrujula && this.lastTieneBrujula) {
			this.siguiendoJugador = false; // Al quitársela se desengancha
		}
		this.lastTieneBrujula = tieneBrujula;

		if (this.modoPergaminoAbierto) {
			if ((raton != null) && raton.presionadoClickDerUnicaAct()) {
				this.cerrarPergamino();
				return;
			}

			// [ESPACIO]: Re-engancha el seguimiento si tiene brújula, o vuelve al ancla si
			// no la tiene
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
				this.recentrar();
			}

			// Arrastre con ratón (Pan & Drag táctico)
			if (raton != null) {
				final Point pRaton = raton.getPuntoPosicionEscalado();
				if (raton.presionadoClickIzq()) {
					if (this.areaLienzoInterno.contains(pRaton)) {
						if (!this.arrastrando) {
							this.arrastrando = true;
							this.lastMouseDragX = pRaton.x;
							this.lastMouseDragY = pRaton.y;
						} else {
							final int dx = pRaton.x - this.lastMouseDragX;
							final int dy = pRaton.y - this.lastMouseDragY;
							final int dTilesX = dx / 2;
							final int dTilesY = dy / 2;

							if ((dTilesX != 0) || (dTilesY != 0)) {
								// Al arrastrar manualmente, se DESENGANCHA el seguimiento automático
								this.siguiendoJugador = false;

								this.panTileOffsetX -= dTilesX;
								this.panTileOffsetY -= dTilesY;
								this.lastMouseDragX += dTilesX * 2;
								this.lastMouseDragY += dTilesY * 2;
								this.marcarSucio();
							}
						}
					}
				} else {
					this.arrastrando = false;
				}
			}
		}

		// Sincronización de posición y seguimiento
		if ((Globales.JUGADOR != null) && (Globales.JUGADOR.getMundo() != null)) {
			final NieblaGuerra ng = Globales.JUGADOR.getMundo().getNieblaGuerra();
			final int pTileX = Math.floorDiv(Globales.JUGADOR.getCentroX(), Constantes.LADO_TILE);
			final int pTileY = Math.floorDiv(Globales.JUGADOR.getCentroY(), Constantes.LADO_TILE);

			// Si el seguimiento está activo, el centro persigue al jugador en cada paso
			if (tieneBrujula && this.siguiendoJugador) {
				this.centroFijadoTileX = pTileX;
				this.centroFijadoTileY = pTileY;
			}

			final boolean cambioPosicion = tieneBrujula && this.siguiendoJugador
					&& ((pTileX != this.lastPlayerTileX) || (pTileY != this.lastPlayerTileY));
			final boolean cambioNiebla = ((ng != null) && (ng.getVersionVision() != this.lastVersionNiebla));

			if (cambioPosicion || cambioNiebla) {
				this.lastPlayerTileX = pTileX;
				this.lastPlayerTileY = pTileY;
				if (ng != null) {
					this.lastVersionNiebla = ng.getVersionVision();
				}
				this.marcarSucio();
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		final Mundo mundo = (Globales.JUGADOR != null) ? Globales.JUGADOR.getMundo() : null;
		if ((mundo == null) || (mundo.getNieblaGuerra() == null)) {
			return;
		}

		final Dificultad dif = Globales.dificultad;
		final boolean tieneMapa = this.validarPermisoMapa(dif);
		final boolean tieneBrujula = this.validarPermisoBrujula();

		// MODO PERGAMINO MODAL (Tecla M)
		if (this.modoPergaminoAbierto) {
			this.pintarModoPergamino(g, mundo, tieneMapa, tieneBrujula, dif);
			return;
		}

		// MODO MINI-RADAR EN HUD (Consultas semánticas limpias)
		if (dif.permiteMinimapaHUD()) {
			if (dif.requiereBrujulaParaHUD()) {
				if (tieneMapa && tieneBrujula) {
					this.pintarModoHUD(g, mundo, true);
				}
			} else if (tieneMapa) {
				this.pintarModoHUD(g, mundo, tieneBrujula);
			}
		}
		// En Difícil y Hardcore el HUD nunca tiene radar flotante
	}

	private boolean validarPermisoMapa(final Dificultad dif) {
		if (!dif.requiereItemMapaParaVer()) {
			return true;
		}
		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			return false;
		}

		final SlotManager sm = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager();
		final boolean enPrincipal = (sm.getSlotArma() != null) && (sm.getSlotArma().getItem() instanceof ItemMapa);
		final boolean enSecundaria = (sm.getSlotManoSecundaria() != null)
				&& (sm.getSlotManoSecundaria().getItem() instanceof ItemMapa);
		final boolean empunado = enPrincipal || enSecundaria;

		if (dif.requiereMapaEnMano()) {
			return empunado;
		}
		if (empunado) {
			return true;
		}

		for (int i = 0; i < sm.getSlotsPrincipales().size(); i++) {
			if (sm.getSlotsPrincipales().get(i).getItem() instanceof ItemMapa) {
				return true;
			}
		}
		for (int i = 0; i < sm.getSlotsAlmacen().size(); i++) {
			if (sm.getSlotsAlmacen().get(i).getItem() instanceof ItemMapa) {
				return true;
			}
		}
		return false;
	}

	private boolean validarPermisoBrujula() {
		if (Globales.dificultad.tieneBrujulaPorDefecto()) {
			return true;
		}
		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			return false;
		}
		final SlotManager sm = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager();

		if ((sm.getSlotArma() != null) && (sm.getSlotArma().getItem() instanceof ItemBrujula)) {
			return true;
		}
		if ((sm.getSlotManoSecundaria() != null) && (sm.getSlotManoSecundaria().getItem() instanceof ItemBrujula)) {
			return true;
		}

		for (int i = 0; i < sm.getSlotsPrincipales().size(); i++) {
			if (sm.getSlotsPrincipales().get(i).getItem() instanceof ItemBrujula) {
				return true;
			}
		}
		for (int i = 0; i < sm.getSlotsAlmacen().size(); i++) {
			if (sm.getSlotsAlmacen().get(i).getItem() instanceof ItemBrujula) {
				return true;
			}
		}
		return false;
	}

	// =========================================================================
	// MINI-RADAR HUD EN VRAM (CERO OPF RESIDUAL)
	// =========================================================================
	private void pintarModoHUD(final Graphics2D g, final Mundo mundo, final boolean tieneBrujula) {
		final int rx = this.areaHUD.x;
		final int ry = this.areaHUD.y;
		final int rw = this.areaHUD.width;
		final int rh = this.areaHUD.height;

		Render2D.dibujarRectanguloRelleno(g, rx, ry, rw, rh, FONDO_RADAR);
		Render2D.dibujarRectanguloContorno(g, rx, ry, rw, rh, BORDE_RADAR);
		Render2D.dibujarRectanguloContorno(g, rx + 1, ry + 1, rw - 2, rh - 2, BISEL_RADAR);

		final int interiorW = rw - 4;
		final int interiorH = rh - 4;

		if ((this.bufferHUD == null) || (this.bufferHUD.getWidth() != interiorW)
				|| (this.bufferHUD.getHeight() != interiorH)) {
			this.bufferHUD = g.getDeviceConfiguration().createCompatibleVolatileImage(interiorW, interiorH,
					Transparency.OPAQUE);
			this.hudSucio = true;
		}

		final int valHUD = this.bufferHUD.validate(g.getDeviceConfiguration());
		if ((valHUD == VolatileImage.IMAGE_RESTORED) || (valHUD == VolatileImage.IMAGE_INCOMPATIBLE)) {
			this.hudSucio = true;
		}

		if (this.hudSucio) {
			final Graphics2D gh = this.bufferHUD.createGraphics();
			try {
				gh.setColor(FONDO_RADAR);
				gh.fillRect(0, 0, interiorW, interiorH);

				final int centroTileX = Math.floorDiv(Globales.JUGADOR.getCentroX(), Constantes.LADO_TILE);
				final int centroTileY = Math.floorDiv(Globales.JUGADOR.getCentroY(), Constantes.LADO_TILE);
				final int radioTilesX = interiorW / 4;
				final int radioTilesY = interiorH / 4;
				final int cx = interiorW / 2;
				final int cy = interiorH / 2;

				final NieblaGuerra niebla = mundo.getNieblaGuerra();

				for (int dy = -radioTilesY; dy <= radioTilesY; dy++) {
					final int tY = centroTileY + dy;
					for (int dx = -radioTilesX; dx <= radioTilesX; dx++) {
						final int tX = centroTileX + dx;
						final byte estado = niebla.getEstado(tX, tY);
						if (estado == NieblaGuerra.TERRA_INCOGNITA) {
							continue;
						}

						final boolean activo = tieneBrujula && (estado == NieblaGuerra.VISION_ACTIVA);
						final Color c = this.resolverColorTile(mundo, tX, tY, activo);
						if (c != null) {
							gh.setColor(c);
							gh.fillRect(cx + (dx * 2), cy + (dy * 2), 2, 2);
						}
					}
				}
			} finally {
				gh.dispose();
			}
			this.hudSucio = false;
		}

		g.drawImage(this.bufferHUD, rx + 2, ry + 2, null);

		if (tieneBrujula) {
			final int cxPintado = rx + (rw / 2);
			final int cyPintado = ry + (rh / 2);
			this.pintarIconoAtlas(g, ICONO_PIONERO, cxPintado - 4, cyPintado - 4);
			this.pintarAgujaDireccion(g, cxPintado, cyPintado, Globales.JUGADOR.getDireccion(), 4);
		}
	}

	// =========================================================================
	// MODO PERGAMINO DIEGÉTICO (SIN BRÚJULA = TINTA ESTÁTICA EN PAPEL)
	// =========================================================================
	private void pintarModoPergamino(final Graphics2D g, final Mundo mundo, final boolean tieneMapa,
			final boolean tieneBrujula, final Dificultad dif) {
		final int px = this.areaPergamino.x;
		final int py = this.areaPergamino.y;
		final int pw = this.areaPergamino.width;
		final int ph = this.areaPergamino.height;

		Render2D.dibujarRectanguloRelleno(g, px, py, pw, ph, FONDO_PERGAMINO);
		Render2D.dibujarRectanguloContorno(g, px, py, pw, ph, BORDE_PERGAMINO);
		Render2D.dibujarRectanguloContorno(g, px + 2, py + 2, pw - 4, ph - 4, BISEL_RADAR);

		final Font fPrevia = g.getFont();

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 9f));
		final String nombreSubmundo = ((mundo.getEscenario() != null) && (mundo.getEscenario().getMetadatos() != null))
				? mundo.getEscenario().getMetadatos().getNombreVisible()
				: "Región Desconocida";
		Render2D.dibujarStringConSombra(g, "CARTOGRAFÍA: " + nombreSubmundo, px + 8, py + 14, TEXTO_TITULO,
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 6f));
		Render2D.dibujarString(g, "[M / Clic Der] Cerrar", (px + pw) - 68, py + 13, TEXTO_TITULO);

		if (!tieneMapa) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 8f));
			final String aviso = dif.esHardcore() ? "¡ORIENTACIÓN PERDIDA! DEBES SOSTENER EL MAPA EN UNA MANO"
					: "REQUIERES UN MAPA CARTOGRÁFICO EN TU INVENTARIO";
			final int anchoAviso = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, aviso);
			Render2D.dibujarStringConSombra(g, aviso, px + ((pw - anchoAviso) / 2), py + (ph / 2), TEXTO_ADVERTENCIA,
					Color.BLACK);
			g.setFont(fPrevia);
			return;
		}

		final int w = this.areaLienzoInterno.width;
		final int h = this.areaLienzoInterno.height;

		if ((this.bufferPergamino == null) || (this.bufferPergamino.getWidth() != w)
				|| (this.bufferPergamino.getHeight() != h)) {
			this.bufferPergamino = g.getDeviceConfiguration().createCompatibleVolatileImage(w, h, Transparency.OPAQUE);
			this.pergaminoSucio = true;
		}

		final int val = this.bufferPergamino.validate(g.getDeviceConfiguration());
		if ((val == VolatileImage.IMAGE_RESTORED) || (val == VolatileImage.IMAGE_INCOMPATIBLE)) {
			this.pergaminoSucio = true;
		}

		if (this.pergaminoSucio) {
			this.hornearLienzoTerreno(mundo, tieneBrujula);
		}

		// 1. Dibujar el lienzo de terreno
		g.drawImage(this.bufferPergamino, this.areaLienzoInterno.x, this.areaLienzoInterno.y, null);
		Render2D.dibujarRectanguloContorno(g, this.areaLienzoInterno, BISEL_RADAR);

		// 2. Proyección de POIs (Carpas, Camas, Fogatas, Salidas)
		this.pintarPuntosDeInteres(g, mundo, dif, tieneBrujula);

		// 3. Proyección del Pionero y Brújula (Únicamente si tiene el ítem brújula)
		if (tieneBrujula && (Globales.JUGADOR != null)) {
			final int centroLienzoX = this.areaLienzoInterno.x + (this.areaLienzoInterno.width / 2);
			final int centroLienzoY = this.areaLienzoInterno.y + (this.areaLienzoInterno.height / 2);
			final int pioneroX = centroLienzoX - (this.panTileOffsetX * 2);
			final int pioneroY = centroLienzoY - (this.panTileOffsetY * 2);

			if (this.areaLienzoInterno.contains(pioneroX, pioneroY)) {
				this.pintarIconoAtlas(g, ICONO_PIONERO, pioneroX - 4, pioneroY - 4);
				this.pintarAgujaDireccion(g, pioneroX, pioneroY, Globales.JUGADOR.getDireccion(), 5);
			}

			this.pintarInstrumentoBrujula(g, px, py, pw, ph);
		}

		// Guía inferior dinámica según el estado del mapa
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 6f));
		final String guia;
		if (tieneBrujula) {
			if (this.siguiendoJugador) {
				guia = "[Brújula] Siguiendo al pionero · [Arrastrar] Inspección libre";
			} else {
				guia = "[Inspección Libre] Vista desacoplada · [ESPACIO] Re-enganchar pionero";
			}
		} else {
			guia = ((this.panTileOffsetX != 0) || (this.panTileOffsetY != 0))
					? "[Sin Brújula] Mapa mudo · [Arrastrar] Mover vista · [ESPACIO] Resetear ancla"
					: "[Sin Brújula] Mapa mudo de papel (Deduce tu posición por geografía)";
		}
		Render2D.dibujarString(g, guia, px + 8, (py + ph) - 4, TEXTO_GUIA);

		g.setFont(fPrevia);
	}

	private void hornearLienzoTerreno(final Mundo mundo, final boolean tieneBrujula) {
		if (this.bufferPergamino == null) {
			return;
		}

		final Graphics2D gb = this.bufferPergamino.createGraphics();
		try {
			gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

			final int lw = this.areaLienzoInterno.width;
			final int lh = this.areaLienzoInterno.height;

			gb.setColor(new Color(14, 12, 10));
			gb.fillRect(0, 0, lw, lh);

			this.asegurarCentroInicial(mundo);

			// DIEGÉTICO: El centro es la posición fijada + el desplazamiento manual (Pan)
			final int centroTileX = this.centroFijadoTileX + this.panTileOffsetX;
			final int centroTileY = this.centroFijadoTileY + this.panTileOffsetY;

			final int radioTilesX = lw / 4;
			final int radioTilesY = lh / 4;
			final int cx = lw / 2;
			final int cy = lh / 2;

			final NieblaGuerra niebla = mundo.getNieblaGuerra();

			for (int dy = -radioTilesY; dy <= radioTilesY; dy++) {
				final int tY = centroTileY + dy;
				final int posY = cy + (dy * 2);

				for (int dx = -radioTilesX; dx <= radioTilesX; dx++) {
					final int tX = centroTileX + dx;
					final byte estado = niebla.getEstado(tX, tY);
					if (estado == NieblaGuerra.TERRA_INCOGNITA) {
						continue;
					}

					// DIEGÉTICO ESTRICTO: Sin brújula, CERO halo brillante. Todo es tinta seca de
					// memoria uniforme
					final boolean activo = tieneBrujula && (estado == NieblaGuerra.VISION_ACTIVA);

					final Color c = this.resolverColorTile(mundo, tX, tY, activo);
					if (c != null) {
						gb.setColor(c);
						gb.fillRect(cx + (dx * 2), posY, 2, 2);
					}
				}
			}
		} finally {
			gb.dispose();
		}

		this.pergaminoSucio = false;
	}

	// =========================================================================
	// FILTRADO ESTRICTO DE POIs
	// =========================================================================
	private void pintarPuntosDeInteres(final Graphics2D g, final Mundo mundo, final Dificultad dif,
			final boolean tieneBrujula) {
		final NieblaGuerra niebla = mundo.getNieblaGuerra();

		this.asegurarCentroInicial(mundo);

		final int centroTileX = this.centroFijadoTileX + this.panTileOffsetX;
		final int centroTileY = this.centroFijadoTileY + this.panTileOffsetY;

		final int cxLienzo = this.areaLienzoInterno.x + (this.areaLienzoInterno.width / 2);
		final int cyLienzo = this.areaLienzoInterno.y + (this.areaLienzoInterno.height / 2);

		for (final Ente e : mundo.getEntes()) {
			if ((e == null) || e.estaEliminado() || (e instanceof Jugador)) {
				continue;
			}

			int iconoIndex = -1;
			if (e instanceof Carpa) {
				iconoIndex = ICONO_CARPA;
			} else if (e instanceof Cama) {
				iconoIndex = ICONO_CAMA;
			} else if (e instanceof Fogata) {
				iconoIndex = ICONO_FOGATA;
			} else if (e instanceof EntradaCueva) {
				iconoIndex = ICONO_CUEVA_ENTRADA;
			} else if (e instanceof ZonaTP) {
				final ZonaTP ztp = (ZonaTP) e;
				if (ztp.getPuertaTP() instanceof PuertaSalidaCueva) {
					iconoIndex = ICONO_CUEVA_SALIDA;
				}
			} else if (e instanceof Mascota) {
				iconoIndex = ICONO_MASCOTA;
			}

			if (iconoIndex >= 0) {
				final int eTileX = Math.floorDiv(e.getCentroX(), Constantes.LADO_TILE);
				final int eTileY = Math.floorDiv(e.getCentroY(), Constantes.LADO_TILE);

				// Filtrado semántico: Ocultar POIs en la niebla según la dificultad
				if (!dif.permiteIconosEnOscuridad()) {
					if (niebla.getEstado(eTileX, eTileY) == NieblaGuerra.TERRA_INCOGNITA) {
						continue;
					}
				}

				final int pantallaX = (cxLienzo + ((eTileX - centroTileX) * 2)) - 4;
				final int pantallaY = (cyLienzo + ((eTileY - centroTileY) * 2)) - 4;

				if (this.areaLienzoInterno.contains(pantallaX + 4, pantallaY + 4)) {
					this.pintarIconoAtlas(g, iconoIndex, pantallaX, pantallaY);
				}
			}
		}
	}

	private void pintarInstrumentoBrujula(final Graphics2D g, final int px, final int py, final int pw, final int ph) {
		final int brujulaX = (px + pw) - 22;
		final int brujulaY = py + 20;
		this.pintarIconoAtlas(g, ICONO_AGUJA_BRUJULA, brujulaX, brujulaY);

		final Font fPrev = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 7f));

		final int centroX = this.areaLienzoInterno.x + (this.areaLienzoInterno.width / 2);
		final int centroY = this.areaLienzoInterno.y + (this.areaLienzoInterno.height / 2);

		Render2D.dibujarString(g, "N", centroX - 2, this.areaLienzoInterno.y + 7, TEXTO_CARDINAL);
		Render2D.dibujarString(g, "S", centroX - 2, (this.areaLienzoInterno.y + this.areaLienzoInterno.height) - 1,
				TEXTO_CARDINAL);
		Render2D.dibujarString(g, "O", this.areaLienzoInterno.x + 2, centroY + 3, TEXTO_CARDINAL);
		Render2D.dibujarString(g, "E", (this.areaLienzoInterno.x + this.areaLienzoInterno.width) - 7, centroY + 3,
				TEXTO_CARDINAL);

		g.setFont(fPrev);
	}

	private void pintarIconoAtlas(final Graphics2D g, final int index, final int x, final int y) {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.IGU_ICONOS_MINIMAPA);
		if (hoja != null) {
			final BufferedImage sprite = hoja.getSprite(index);
			if (sprite != null) {
				g.drawImage(sprite, x, y, null);
			}
		}
	}

	private void pintarAgujaDireccion(final Graphics2D g, final int cx, final int cy,
			final principal.entes.criaturas.Criatura.Direccion dir, final int dist) {
		int px = cx;
		int py = cy;
		switch (dir) {
		case NORTE:
			py -= dist;
			break;
		case SUR:
			py += dist;
			break;
		case ESTE:
			px += dist;
			break;
		case OESTE:
			px -= dist;
			break;
		}
		Render2D.dibujarRectanguloRelleno(g, px - 1, py - 1, 2, 2, Color.WHITE);
	}

	private Color resolverColorTile(final Mundo mundo, final int tx, final int ty, final boolean activo) {
		if (mundo.getTerreno() == null) {
			return null;
		}
		final Tile t = mundo.getTerreno().getTileGrid(tx, ty);
		if (t == null) {
			return null;
		}

		if (t.esSolido()) {
			return activo ? COL_MURO_ACT : COL_MURO_MEM;
		}

		final String nombre = (t.getTipoTerreno() != null) ? t.getTipoTerreno().name().toLowerCase() : "";
		if (nombre.contains("agua")) {
			return activo ? COL_AGUA_ACT : COL_AGUA_MEM;
		}
		if (nombre.contains("roca") || nombre.contains("piedra")) {
			return activo ? COL_PIEDRA_ACT : COL_PIEDRA_MEM;
		}
		if (nombre.contains("arena") || nombre.contains("tierra")) {
			return activo ? COL_TIERRA_ACT : COL_TIERRA_MEM;
		}

		return activo ? COL_HIERBA_ACT : COL_HIERBA_MEM;
	}
}