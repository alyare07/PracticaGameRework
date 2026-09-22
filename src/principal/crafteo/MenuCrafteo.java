package principal.crafteo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import principal.controles.Raton;
import principal.entes.criaturas.jugador.Jugador;
import principal.inventario.Inventario;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Taller de fabricación y cocina a pantalla completa con buscador Zero-GC,
 * carrusel de pestañas, requisitos RPG, condiciones especiales y fabricación
 * continua (Zero-GC / O(1)).
 * 
 * @version 3.0 (Vanilla Java 8 - Zero-GC Search Engine Integration)
 */
public final class MenuCrafteo {

	private static MenuCrafteo instancia;

	// === PALETA CROMÁTICA TÁCTICA ===
	private static final Color FONDO_PRINCIPAL = new Color(14, 16, 22, 255);
	private static final Color FONDO_PANEL = new Color(20, 24, 34, 245);
	private static final Color BORDE_PANEL = new Color(55, 65, 85);
	private static final Color BORDE_RESALTADO = new Color(220, 180, 50);

	private static final Color COLOR_TITULO = new Color(245, 205, 70);
	private static final Color COLOR_TEXTO = new Color(220, 225, 235);
	private static final Color COLOR_SUBTEXTO = new Color(150, 160, 175);

	private static final Color COLOR_OK = new Color(70, 230, 110);
	private static final Color COLOR_FALTA = new Color(245, 75, 65);

	private static final Color COLOR_TAB_ACTIVA = new Color(35, 45, 65);
	private static final Color COLOR_TAB_INACTIVA = new Color(18, 20, 28);
	private static final Color COLOR_BOTON_CRAFT = new Color(190, 145, 30);
	private static final Color COLOR_BOTON_DISABLED = new Color(40, 45, 55);

	// Dimensiones nativas
	private final Rectangle areaPantalla = new Rectangle(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
	private final Rectangle panelLista = new Rectangle(10, 48, 225, 302);
	private final Rectangle panelDetalle = new Rectangle(242, 48, 388, 302);
	private final Rectangle botonCerrar = new Rectangle(612, 8, 18, 18);
	private final Rectangle botonFabricar = new Rectangle(360, 310, 150, 26);

	// === BUSCADOR ZERO-GC ===
	private final Rectangle areaBuscador = new Rectangle(14, 52, 217, 18);
	private final Rectangle botonLimpiarBusqueda = new Rectangle(217, 54, 12, 14);
	private final StringBuilder sbBusqueda = new StringBuilder(24);
	private String textoBusquedaCacheado = "";
	private boolean focoBuscador = false;

	private final int[] indicesFiltrados = new int[256];
	private int totalFiltrados = 0;

	// === CARRUSEL HORIZONTAL DE PESTAÑAS ===
	private final EstacionCrafteo[] pestanas = { null, EstacionCrafteo.MANUAL, EstacionCrafteo.FOGATA,
			EstacionCrafteo.MESA_TRABAJO, EstacionCrafteo.HORNO, EstacionCrafteo.YUNQUE };

	private final String[] nombresPestanas = { "Todas", "A Mano", "Fogata", "Mesa", "Horno", "Yunque" };

	private static final int TAB_W = 75;
	private static final int TAB_H = 18;
	private static final int TAB_SPACING = 4;
	private static final int PASO_SCROLL_PESTANAS = 40;

	private final Rectangle areaBarraPestanasVisible = new Rectangle(28, 24, 560, 20);
	private final Rectangle botonScrollIzq = new Rectangle(10, 24, 14, 18);
	private final Rectangle botonScrollDer = new Rectangle(592, 24, 14, 18);

	private int scrollPestanasX = 0;
	private final int maxScrollPestanasX;

	private int pestanaActiva = 0;
	private boolean abierto = false;
	private int indiceRecetaSeleccionada = 0;
	private int scrollLista = 0;

	private static final int ALTO_ITEM_LISTA = 24;
	private static final int MAX_ITEMS_VISIBLES = 9;

	public boolean isFocoBuscador() {
		return this.abierto && this.focoBuscador;
	}

	public static synchronized MenuCrafteo getInstancia() {
		if (instancia == null) {
			instancia = new MenuCrafteo();
		}
		return instancia;
	}

	private MenuCrafteo() {
		final int anchoTotalPestanas = (this.pestanas.length * (TAB_W + TAB_SPACING)) - TAB_SPACING;
		this.maxScrollPestanasX = Math.max(0, anchoTotalPestanas - this.areaBarraPestanasVisible.width);
		this.filtrarRecetas();
	}

	public void abrir(final EstacionCrafteo estacionPreseleccionada) {
		this.abierto = true;
		this.scrollLista = 0;
		this.focoBuscador = false;

		if (estacionPreseleccionada != null) {
			for (int i = 0; i < this.pestanas.length; i++) {
				if (this.pestanas[i] == estacionPreseleccionada) {
					this.pestanaActiva = i;
					final int tabX = i * (TAB_W + TAB_SPACING);
					this.scrollPestanasX = Math.max(0,
							Math.min(this.maxScrollPestanasX, tabX - (this.areaBarraPestanasVisible.width / 2)));
					this.filtrarRecetas();
					return;
				}
			}
		}
		this.pestanaActiva = 0;
		this.filtrarRecetas();
	}

	public void abrir() {
		this.abrir(null);
	}

	public void cerrar() {
		this.abierto = false;
		this.focoBuscador = false;
	}

	public void conmutar() {
		if (this.abierto) {
			this.cerrar();
		} else {
			this.abrir();
		}
	}

	public boolean isAbierto() {
		return this.abierto;
	}

	// =========================================================================
	// FILTRADO DETERMINISTA ZERO-GC (POR EVENTO)
	// =========================================================================

	private void filtrarRecetas() {
		final List<RecetaCrafteo> todas = CatalogoRecetas.getRecetas();
		final EstacionCrafteo filtroEstacion = this.pestanas[this.pestanaActiva];
		final boolean hayTexto = !this.textoBusquedaCacheado.isEmpty();

		this.totalFiltrados = 0;

		for (int i = 0; i < todas.size(); i++) {
			final RecetaCrafteo r = todas.get(i);

			if ((filtroEstacion != null) && (r.getEstacionRequerida() != filtroEstacion)) {
				continue;
			}

			if (hayTexto && (r.getNombreMinusculas().indexOf(this.textoBusquedaCacheado) == -1)) {
				continue;
			}

			if (this.totalFiltrados < this.indicesFiltrados.length) {
				this.indicesFiltrados[this.totalFiltrados++] = i;
			}
		}

		this.scrollLista = 0;
		this.indiceRecetaSeleccionada = (this.totalFiltrados > 0) ? this.indicesFiltrados[0] : -1;
	}

	// =========================================================================
	// ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar(final Raton raton) {
		if (!this.abierto || (raton == null)) {
			return;
		}

		final Point pMouse = raton.getPuntoPosicionEscalado();

		// 1. Cerrar con Escape
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.cerrar();
			return;
		}

		// 2. Control de botón [X]
		if (raton.presionadoClickIzqUnicaAct() && this.botonCerrar.contains(pMouse)) {
			this.cerrar();
			return;
		}

		// 3. Foco del Buscador y Clics
		if (raton.presionadoClickIzqUnicaAct()) {
			if (this.areaBuscador.contains(pMouse)) {
				if (this.botonLimpiarBusqueda.contains(pMouse) && (this.sbBusqueda.length() > 0)) {
					this.sbBusqueda.setLength(0);
					this.textoBusquedaCacheado = "";
					this.filtrarRecetas();
				}
				this.focoBuscador = true;
			} else {
				this.focoBuscador = false;
			}
		}

		// 4. Entrada por teclado en el Buscador
		if (this.focoBuscador) {
			this.actualizarEscrituraBuscador();
		}

		// 5. Controles del Carrusel de Pestañas
		this.actualizarControlesCarrusel(raton, pMouse);

		// 6. Scroll en la lista de recetas
		if (this.panelLista.contains(pMouse)) {
			final int rot = raton.getRotacionRueda();
			if (rot != 0) {
				final int maxScroll = Math.max(0, this.totalFiltrados - MAX_ITEMS_VISIBLES);
				this.scrollLista = Math.max(0, Math.min(maxScroll, this.scrollLista + rot));
			}
		}

		// 7. Clic sobre un ítem de la lista filtrada
		if (raton.presionadoClickIzqUnicaAct() && this.panelLista.contains(pMouse)
				&& !this.areaBuscador.contains(pMouse)) {
			int yItem = this.panelLista.y + 26;

			for (int f = this.scrollLista; f < this.totalFiltrados; f++) {
				final Rectangle areaItem = new Rectangle(this.panelLista.x + 4, yItem, this.panelLista.width - 8,
						ALTO_ITEM_LISTA);
				if (areaItem.contains(pMouse)) {
					this.indiceRecetaSeleccionada = this.indicesFiltrados[f];
					return;
				}
				yItem += ALTO_ITEM_LISTA + 2;
			}
		}

		// 8. Botón [ FABRICAR ]
		if (raton.presionadoClickIzqUnicaAct() && this.botonFabricar.contains(pMouse)) {
			final List<RecetaCrafteo> todas = CatalogoRecetas.getRecetas();
			if ((this.indiceRecetaSeleccionada >= 0) && (this.indiceRecetaSeleccionada < todas.size())) {
				final RecetaCrafteo receta = todas.get(this.indiceRecetaSeleccionada);
				final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();
				final Jugador j = Globales.JUGADOR;
				final GestorCrafteo gc = Globales.GESTOR_CRAFTEO;

				if (receta.cumpleRequisitos(inv, j, gc)) {
					Globales.GESTOR_CRAFTEO.fabricar(receta);
				}
			}
		}
	}

	private void actualizarEscrituraBuscador() {
		boolean cambio = false;
		// Enter confirma la búsqueda y libera el foco
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)) {
			this.focoBuscador = false;
			return;
		}
		// Backspace
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_BACK_SPACE)) {
			if (this.sbBusqueda.length() > 0) {
				this.sbBusqueda.setLength(this.sbBusqueda.length() - 1);
				cambio = true;
			}
		}
		// Espacio
		else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if (this.sbBusqueda.length() < 18) {
				this.sbBusqueda.append(' ');
				cambio = true;
			}
		}

		// Letras A-Z
		for (int k = KeyEvent.VK_A; k <= KeyEvent.VK_Z; k++) {
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(k)) {
				if (this.sbBusqueda.length() < 18) {
					this.sbBusqueda.append((char) (k + 32)); // Convertir a minúscula ASCII
					cambio = true;
				}
				break;
			}
		}

		// Números 0-9
		for (int k = KeyEvent.VK_0; k <= KeyEvent.VK_9; k++) {
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(k)) {
				if (this.sbBusqueda.length() < 18) {
					this.sbBusqueda.append((char) k);
					cambio = true;
				}
				break;
			}
		}

		if (cambio) {
			this.textoBusquedaCacheado = this.sbBusqueda.toString();
			this.filtrarRecetas();
		}
	}

	private void actualizarControlesCarrusel(final Raton raton, final Point pMouse) {
		if (this.areaBarraPestanasVisible.contains(pMouse)) {
			final int rot = raton.getRotacionRueda();
			if (rot != 0) {
				this.scrollPestanasX = Math.max(0,
						Math.min(this.maxScrollPestanasX, this.scrollPestanasX + (rot * PASO_SCROLL_PESTANAS)));
			}
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			if (this.botonScrollIzq.contains(pMouse)) {
				this.scrollPestanasX = Math.max(0, this.scrollPestanasX - (TAB_W + TAB_SPACING));
				return;
			}
			if (this.botonScrollDer.contains(pMouse)) {
				this.scrollPestanasX = Math.min(this.maxScrollPestanasX, this.scrollPestanasX + (TAB_W + TAB_SPACING));
				return;
			}

			if (this.areaBarraPestanasVisible.contains(pMouse)) {
				for (int i = 0; i < this.pestanas.length; i++) {
					final int tabX = (this.areaBarraPestanasVisible.x + (i * (TAB_W + TAB_SPACING)))
							- this.scrollPestanasX;
					if ((pMouse.x >= tabX) && (pMouse.x < (tabX + TAB_W))
							&& (pMouse.y >= this.areaBarraPestanasVisible.y)
							&& (pMouse.y < (this.areaBarraPestanasVisible.y + TAB_H))) {
						this.pestanaActiva = i;
						this.filtrarRecetas();
						return;
					}
				}
			}
		}
	}

	// =========================================================================
	// RENDERIZADO DEL TALLER (640x360)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		if (!this.abierto) {
			return;
		}

		// 1. Fondo opaco
		Render2D.dibujarRectanguloRelleno(g, this.areaPantalla, FONDO_PRINCIPAL);

		// 2. Cabecera y botón [X]
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));
		Render2D.dibujarStringConSombra(g, "TALLER DE FABRICACION Y COCINA", 10, 16, COLOR_TITULO, Color.BLACK);

		Render2D.dibujarRectanguloRelleno(g, this.botonCerrar, COLOR_TAB_INACTIVA);
		Render2D.dibujarRectanguloContorno(g, this.botonCerrar, BORDE_PANEL);
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
		Render2D.dibujarStringConSombra(g, "X", this.botonCerrar.x + 5, this.botonCerrar.y + 13, COLOR_TEXTO,
				Color.BLACK);

		// 3. Carrusel de Pestañas
		this.pintarPestanasCarrusel(g);

		// 4. Paneles
		Render2D.dibujarRectanguloRelleno(g, this.panelLista, FONDO_PANEL);
		Render2D.dibujarRectanguloContorno(g, this.panelLista, BORDE_PANEL);

		Render2D.dibujarRectanguloRelleno(g, this.panelDetalle, FONDO_PANEL);
		Render2D.dibujarRectanguloContorno(g, this.panelDetalle, BORDE_PANEL);

		// 5. Contenido
		this.pintarBuscador(g);
		this.pintarListaRecetas(g);
		this.pintarDetalleReceta(g);

		g.setFont(fontPrevia);
	}

	private void pintarBuscador(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.areaBuscador, COLOR_TAB_INACTIVA);
		Render2D.dibujarRectanguloContorno(g, this.areaBuscador, this.focoBuscador ? BORDE_RESALTADO : BORDE_PANEL);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));

		if (this.sbBusqueda.length() == 0) {
			Render2D.dibujarStringConSombra(g, "Buscar...", this.areaBuscador.x + 4, this.areaBuscador.y + 13,
					COLOR_SUBTEXTO, Color.BLACK);
		} else {
			final String cursor = this.focoBuscador && (((Globales.animacion / 20) % 2) == 0) ? "|" : "";
			Render2D.dibujarStringConSombra(g, this.textoBusquedaCacheado + cursor, this.areaBuscador.x + 4,
					this.areaBuscador.y + 13, COLOR_TEXTO, Color.BLACK);

			// Botón [x] para limpiar
			Render2D.dibujarStringConSombra(g, "x", this.botonLimpiarBusqueda.x + 2, this.botonLimpiarBusqueda.y + 11,
					COLOR_SUBTEXTO, Color.BLACK);
		}
	}

	private void pintarPestanasCarrusel(final Graphics2D g) {
		final GestorCrafteo gc = Globales.GESTOR_CRAFTEO;

		final boolean puedeIzq = this.scrollPestanasX > 0;
		Render2D.dibujarRectanguloRelleno(g, this.botonScrollIzq, puedeIzq ? COLOR_TAB_ACTIVA : COLOR_TAB_INACTIVA);
		Render2D.dibujarRectanguloContorno(g, this.botonScrollIzq, puedeIzq ? BORDE_RESALTADO : BORDE_PANEL);
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 11f));
		Render2D.dibujarStringConSombra(g, "<", this.botonScrollIzq.x + 4, this.botonScrollIzq.y + 13,
				puedeIzq ? COLOR_TITULO : COLOR_SUBTEXTO, Color.BLACK);

		final boolean puedeDer = this.scrollPestanasX < this.maxScrollPestanasX;
		Render2D.dibujarRectanguloRelleno(g, this.botonScrollDer, puedeDer ? COLOR_TAB_ACTIVA : COLOR_TAB_INACTIVA);
		Render2D.dibujarRectanguloContorno(g, this.botonScrollDer, puedeDer ? BORDE_RESALTADO : BORDE_PANEL);
		Render2D.dibujarStringConSombra(g, ">", this.botonScrollDer.x + 4, this.botonScrollDer.y + 13,
				puedeDer ? COLOR_TITULO : COLOR_SUBTEXTO, Color.BLACK);

		final Shape clipAnterior = g.getClip();
		try {
			g.setClip(this.areaBarraPestanasVisible);

			for (int i = 0; i < this.pestanas.length; i++) {
				final int tabX = (this.areaBarraPestanasVisible.x + (i * (TAB_W + TAB_SPACING))) - this.scrollPestanasX;
				final int tabY = this.areaBarraPestanasVisible.y;

				if (((tabX + TAB_W) < this.areaBarraPestanasVisible.x)
						|| (tabX > (this.areaBarraPestanasVisible.x + this.areaBarraPestanasVisible.width))) {
					continue;
				}

				final boolean activa = (this.pestanaActiva == i);
				final EstacionCrafteo est = this.pestanas[i];

				Render2D.dibujarRectanguloRelleno(g, tabX, tabY, TAB_W, TAB_H,
						activa ? COLOR_TAB_ACTIVA : COLOR_TAB_INACTIVA);
				Render2D.dibujarRectanguloContorno(g, tabX, tabY, TAB_W, TAB_H, activa ? BORDE_RESALTADO : BORDE_PANEL);

				final boolean disponible = (est == null) || ((gc != null) && gc.isEstacionDisponible(est));
				final Color colorTexto = activa ? COLOR_TITULO : (disponible ? COLOR_TEXTO : COLOR_SUBTEXTO);

				g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
				Render2D.dibujarStringConSombra(g, this.nombresPestanas[i], tabX + 8, tabY + 13, colorTexto,
						Color.BLACK);

				if (disponible && (est != null)) {
					Render2D.dibujarRectanguloRelleno(g, (tabX + TAB_W) - 6, tabY + 4, 3, 3, COLOR_OK);
				}
			}
		} finally {
			g.setClip(clipAnterior);
		}
	}

	private void pintarListaRecetas(final Graphics2D g) {
		final List<RecetaCrafteo> todas = CatalogoRecetas.getRecetas();
		final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();
		final Jugador j = Globales.JUGADOR;
		final GestorCrafteo gc = Globales.GESTOR_CRAFTEO;

		int yItem = this.panelLista.y + 26;
		int dibujados = 0;

		for (int f = this.scrollLista; f < this.totalFiltrados; f++) {
			if (dibujados >= MAX_ITEMS_VISIBLES) {
				break;
			}

			final int idxReal = this.indicesFiltrados[f];
			final RecetaCrafteo r = todas.get(idxReal);

			final boolean seleccionada = (this.indiceRecetaSeleccionada == idxReal);
			final boolean fabricable = r.cumpleRequisitos(inv, j, gc);

			final Rectangle areaItem = new Rectangle(this.panelLista.x + 4, yItem, this.panelLista.width - 8,
					ALTO_ITEM_LISTA);

			Render2D.dibujarRectanguloRelleno(g, areaItem, seleccionada ? COLOR_TAB_ACTIVA : COLOR_TAB_INACTIVA);
			Render2D.dibujarRectanguloContorno(g, areaItem, seleccionada ? BORDE_RESALTADO : BORDE_PANEL);

			final BufferedImage icon = r.getItemResultado().getTexturaInventario();
			if (icon != null) {
				Render2D.dibujarImagen(g, icon, areaItem.x + 3, areaItem.y + 4);
			}

			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
			final Color cTxt = seleccionada ? COLOR_TITULO : (fabricable ? COLOR_TEXTO : COLOR_SUBTEXTO);
			Render2D.dibujarStringConSombra(g, r.getNombreVisible(), areaItem.x + 24, areaItem.y + 16, cTxt,
					Color.BLACK);

			final String badge = fabricable ? "[OK]" : "[X]";
			final Color cBadge = fabricable ? COLOR_OK : COLOR_FALTA;
			Render2D.dibujarStringConSombra(g, badge, (areaItem.x + areaItem.width) - 22, areaItem.y + 16, cBadge,
					Color.BLACK);

			yItem += ALTO_ITEM_LISTA + 2;
			dibujados++;
		}
	}

	private void pintarDetalleReceta(final Graphics2D g) {
		final List<RecetaCrafteo> todas = CatalogoRecetas.getRecetas();
		if ((this.indiceRecetaSeleccionada < 0) || (this.indiceRecetaSeleccionada >= todas.size())) {
			return;
		}

		final RecetaCrafteo r = todas.get(this.indiceRecetaSeleccionada);
		final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();
		final Jugador j = Globales.JUGADOR;
		final GestorCrafteo gc = Globales.GESTOR_CRAFTEO;

		final int x = this.panelDetalle.x + 12;

		// 1. Icono y Título
		final BufferedImage icon = r.getItemResultado().getTexturaInventario();
		if (icon != null) {
			Render2D.dibujarRectanguloRelleno(g, x, this.panelDetalle.y + 12, 34, 34, COLOR_TAB_INACTIVA);
			Render2D.dibujarRectanguloContorno(g, x, this.panelDetalle.y + 12, 34, 34, BORDE_RESALTADO);
			Render2D.dibujarImagen(g, icon, x + 9, this.panelDetalle.y + 21);
		}

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 15f));
		Render2D.dibujarStringConSombra(g, r.getNombreVisible(), x + 42, this.panelDetalle.y + 26, COLOR_TITULO,
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
		Render2D.dibujarStringConSombra(g, "Tipo: " + r.getItemResultado().exportarTipoItem(), x + 42,
				this.panelDetalle.y + 40, COLOR_SUBTEXTO, Color.BLACK);

		// 2. Estación Requerida
		final boolean estOk = (r.getEstacionRequerida() == EstacionCrafteo.MANUAL)
				|| ((gc != null) && gc.isEstacionDisponible(r.getEstacionRequerida()));
		final String txtEst = "Estacion: " + r.getEstacionRequerida().name() + (estOk ? " [En Rango]" : " [¡Falta!]");
		Render2D.dibujarStringConSombra(g, txtEst, x, this.panelDetalle.y + 66, estOk ? COLOR_OK : COLOR_FALTA,
				Color.BLACK);

		// 3. Requisitos de Atributos RPG
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
		Render2D.dibujarStringConSombra(g, "REQUISITOS DE ATRIBUTO:", x, this.panelDetalle.y + 88, COLOR_TEXTO,
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
		int yReq = this.panelDetalle.y + 102;

		if (!r.tieneRequisitosAtributos()) {
			Render2D.dibujarStringConSombra(g, "* Ninguno (Conocimiento basico)", x + 6, yReq, COLOR_SUBTEXTO,
					Color.BLACK);
			yReq += 14;
		} else {
			if (r.getFuerzaRequerida() > 0) {
				final boolean ok = (j != null) && (j.getFuerzaTotal() >= r.getFuerzaRequerida());
				final int actual = (j != null) ? j.getFuerzaTotal() : 0;
				final String txt = "* Fuerza: " + r.getFuerzaRequerida() + " (Tienes: " + actual + ")";
				Render2D.dibujarStringConSombra(g, txt, x + 6, yReq, ok ? COLOR_OK : COLOR_FALTA, Color.BLACK);
				yReq += 14;
			}
			if (r.getInteligenciaRequerida() > 0) {
				final boolean ok = (j != null) && (j.getInteligenciaTotal() >= r.getInteligenciaRequerida());
				final int actual = (j != null) ? j.getInteligenciaTotal() : 0;
				final String txt = "* Inteligencia: " + r.getInteligenciaRequerida() + " (Tienes: " + actual + ")";
				Render2D.dibujarStringConSombra(g, txt, x + 6, yReq, ok ? COLOR_OK : COLOR_FALTA, Color.BLACK);
				yReq += 14;
			}
			if (r.getAgilidadRequerida() > 0) {
				final boolean ok = (j != null) && (j.getAgilidadTotal() >= r.getAgilidadRequerida());
				final int actual = (j != null) ? j.getAgilidadTotal() : 0;
				final String txt = "* Agilidad: " + r.getAgilidadRequerida() + " (Tienes: " + actual + ")";
				Render2D.dibujarStringConSombra(g, txt, x + 6, yReq, ok ? COLOR_OK : COLOR_FALTA, Color.BLACK);
				yReq += 14;
			}
		}

		// 3.1. Requisitos Especiales
		if (r.tieneCondicionEspecial()) {
			yReq += 4;
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
			Render2D.dibujarStringConSombra(g, "CONDICIONES ESPECIALES:", x, yReq, COLOR_TEXTO, Color.BLACK);

			yReq += 14;
			final principal.mapa.Mundo m = (j != null) ? j.getMundo() : null;
			final boolean condOk = r.getCondicionEspecial().seCumple(j, m);
			final String txtCond = "* " + r.getDescripcionCondicionEspecial()
					+ (condOk ? " [CUMPLIDO]" : " [¡PENDIENTE!]");

			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
			Render2D.dibujarStringConSombra(g, txtCond, x + 6, yReq, condOk ? COLOR_OK : COLOR_FALTA, Color.BLACK);
			yReq += 14;
		}

		// 4. Materiales Requeridos
		yReq += 6;
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
		Render2D.dibujarStringConSombra(g, "MATERIALES NECESARIOS:", x, yReq, COLOR_TEXTO, Color.BLACK);

		yReq += 16;
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));

		final Ingrediente[] ings = r.getIngredientes();
		for (int i = 0; i < ings.length; i++) {
			final Ingrediente ing = ings[i];
			final int disponible = (inv != null) ? inv.contarItemGenericoTotal(ing.getCodModeloItem()) : 0;
			final boolean ok = disponible >= ing.getCantidad();

			final String txt = "* " + ing.getCodModeloItem() + ": " + disponible + " / " + ing.getCantidad();
			Render2D.dibujarStringConSombra(g, txt, x + 6, yReq, ok ? COLOR_OK : COLOR_FALTA, Color.BLACK);
			yReq += 14;
		}

		// 5. Botón [ FABRICAR ]
		final boolean puedeFabricar = r.cumpleRequisitos(inv, j, gc);
		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();
		final boolean hoverBoton = this.botonFabricar.contains(pMouse) && puedeFabricar;

		final Color bgBoton = puedeFabricar ? (hoverBoton ? COLOR_TITULO : COLOR_BOTON_CRAFT) : COLOR_BOTON_DISABLED;
		Render2D.dibujarRectanguloRelleno(g, this.botonFabricar, bgBoton);
		Render2D.dibujarRectanguloContorno(g, this.botonFabricar, puedeFabricar ? BORDE_RESALTADO : BORDE_PANEL);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
		final Color cTxtBoton = puedeFabricar ? (hoverBoton ? Color.BLACK : Color.WHITE) : COLOR_SUBTEXTO;
		Render2D.dibujarStringConSombra(g, "FABRICAR", this.botonFabricar.x + 44, this.botonFabricar.y + 18, cTxtBoton,
				Color.BLACK);
	}
}