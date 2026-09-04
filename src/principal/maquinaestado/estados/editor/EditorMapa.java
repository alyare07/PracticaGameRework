package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.controles.Raton;
import principal.entes.AsistenteCamara;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.inventario.Contenedor;
import principal.inventario.slot.Slot;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.recursos.TipoTerreno;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Editor de escenarios y mapas integrado en tiempo real con soporte de pinceles
 * multiforma, proyección de cámara, borrado táctico, edición de contenedores y
 * colocación de Spawns con identificadores dinámicos.
 * 
 * @version 2.6 (Vanilla Java 8 - In-Editor Spawns Management)
 */
public class EditorMapa implements EstadoJuego {

	private final GestorEstados GE;
	private final int LADO_TILE;
	private final int ANCHO;
	private final int ALTO;
	private final Terreno TERRENO;

	private int x;
	private int y;
	private final AsistenteCamara asistenteCamara;

	private final Raton RATON = SuperficieDibujo.obtenerSuperficieDibujo().RATON;
	private final Rectangle areaTileSelected = new Rectangle();
	private boolean tileApuntadoValido = false;
	private final Rectangle ultimaAreaTileAlterado = new Rectangle();

	private final Rectangle PALETA_MAPA;
	private final GrupoPaleta PALETAS;

	private int tamanoPincel = 1;
	private boolean pincelCircular = false;

	private final GestorTiempo GT = new GestorTiempo();
	private final GestorTiempo GT_COLOCACION = new GestorTiempo();
	private static final int TIEMPO_ESPERA_MS_COLOCACION = 180;

	private final MundoEditor MUNDO_EDITOR;
	private final Rectangle AREA_MOUSE_APUNTADO = new Rectangle(-1, -1, 1, 1);
	private final Rectangle AREA_BORRADO_AUX = new Rectangle();
	private final ArrayList<Ente> listaEntesABorrar = new ArrayList<Ente>(8);

	// Gestión de Contenedores e Ítems en el Editor
	private InventarioVault cofreAbierto = null;
	private final ItemPuntero itemPuntero = new ItemPuntero();
	private Ente contenedorEncontrado = null;

	private static final Font FUENTE_INFO = new Font(Font.SANS_SERIF, Font.PLAIN, 6);
	private static final Font FUENTE_SPAWN = new Font(Font.SANS_SERIF, Font.BOLD, 7);
	private VolatileImage bufferEditor;

	public EditorMapa(final int ladoTile, final int anchoTiles, final int altoTiles, final TipoTerreno tipoInicial,
			final GestorEstados ge) {
		this.GE = ge;
		this.LADO_TILE = ladoTile;
		this.ANCHO = anchoTiles * ladoTile;
		this.ALTO = altoTiles * ladoTile;
		this.TERRENO = new Terreno(anchoTiles, altoTiles, this.LADO_TILE, tipoInicial);

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height, this);
		this.MUNDO_EDITOR = new MundoEditor(this.TERRENO);
		this.asistenteCamara = new AsistenteCamara(0, 0, 16, 16);

		this.inicializarCamara();
	}

	public EditorMapa(final Escenario esc, final GestorEstados ge) {
		this.GE = ge;
		this.TERRENO = (esc != null) ? esc.getTerreno() : new Terreno(50, 50, Constantes.LADO_TILE, TipoTerreno.TIERRA);
		this.ANCHO = this.TERRENO.getAncho();
		this.ALTO = this.TERRENO.getAlto();
		this.LADO_TILE = this.TERRENO.ladoTile();

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height, this);
		this.MUNDO_EDITOR = (esc != null) ? new MundoEditor(esc) : new MundoEditor(this.TERRENO);
		this.asistenteCamara = new AsistenteCamara(0, 0, 16, 16);

		this.inicializarCamara();
	}

	public EditorMapa(final Terreno terreno, final GestorEstados ge) {
		this(new Escenario(terreno, "[]", "[]", "[]", "[]"), ge);
	}

	public EditorMapa(final String rutaMapa, final GestorEstados ge) {
		this(EscenarioLoader.importarEscenario(new File(rutaMapa)), ge);
	}

	private void inicializarCamara() {
		this.x = this.ANCHO / 2;
		this.y = this.ALTO / 2;
		this.asistenteCamara.setPosicion(this.x, this.y);
		Globales.CAMARA.setEntidadEnfocada(this.asistenteCamara);
		Globales.CAMARA.deshabilitarGestorLimite();
		Globales.CAMARA.reiniciarZoom();
	}

	@Override
	public void actualizar() {
		this.actualizarZoom();
		this.mover();
		this.actualizarProyeccionRaton();
		this.actualizarAtajosPinceles();
		this.actualizarTileApuntado();
		this.PALETAS.actualizar(this.RATON);

		if (this.cofreAbierto != null) {
			this.actualizarCofreAbierto();
		} else {
			this.actualizarAperturaContenedor();
			this.alterarElementoSeleccionado();
			this.borrarElemento();
		}

		this.MUNDO_EDITOR.actualizar();

		if (Globales.TECLADO.TECLA_GUARDAR_MAPA.presionadoUnicaActualizacion()) {
			if (this.GT.transcurrioSegundos(1)) {
				this.GT.establecerReferenciaTiempoActual();
				this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			}
		} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)
				|| Globales.TECLADO.TECLA_ESCAPE.presionado()) {
			if (this.cofreAbierto != null) {
				this.cerrarCofre();
			} else {
				this.itemPuntero.limpiar();
				this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
				this.GE.disposeEditor();
			}
		}
	}

	private void actualizarAperturaContenedor() {
		final boolean teclaInteraccion = Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E);
		final boolean shiftClick = Globales.TECLADO.TECLA_CORRIENDO.presionado()
				&& this.RATON.presionadoClickIzqUnicaAct();

		if ((teclaInteraccion || shiftClick) && this.tileApuntadoValido) {
			final Rectangle areaCursor = new Rectangle(this.AREA_MOUSE_APUNTADO.x - 4, this.AREA_MOUSE_APUNTADO.y - 4,
					8, 8);
			this.contenedorEncontrado = null;

			this.MUNDO_EDITOR.paraCadaObjetoEn(areaCursor, new AccionEntidad<Objeto>() {
				@Override
				public void ejecutar(final Objeto obj) {
					if ((EditorMapa.this.contenedorEncontrado == null) && (obj instanceof Contenedor)) {
						EditorMapa.this.contenedorEncontrado = obj;
					}
				}
			});

			if (this.contenedorEncontrado != null) {
				this.cofreAbierto = ((Contenedor) this.contenedorEncontrado).getInventario();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}
	}

	private void actualizarCofreAbierto() {
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)) {
			this.cerrarCofre();
			GestorSonido.reproducir(IDSonido.GOLPE_1);
			return;
		}

		final Point pMouse = this.RATON.getPuntoPosicionEscalado();
		final boolean shiftClick = Globales.TECLADO.TECLA_CORRIENDO.presionado()
				&& this.RATON.presionadoClickIzqUnicaAct();
		if (shiftClick && !this.cofreAbierto.getArea().contains(pMouse)) {
			this.cerrarCofre();
			GestorSonido.reproducir(IDSonido.GOLPE_1);
			return;
		}

		this.cofreAbierto.actualizar(this.RATON, this.itemPuntero, null);

		if (this.RATON.presionadoClickDerUnicaAct()) {
			final Slot slotApuntado = this.cofreAbierto.getSlot(pMouse);
			if ((slotApuntado != null) && slotApuntado.contieneItem()) {
				slotApuntado.eliminarObjeto();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		if (this.RATON.presionadoClickIzqUnicaAct()) {
			final boolean dentroCofre = this.cofreAbierto.getArea().contains(pMouse);
			final boolean dentroPaleta = this.PALETAS.AREA.contains(pMouse)
					|| this.PALETAS.AREA_CABECERA.contains(pMouse);

			if (!dentroCofre && !dentroPaleta && !this.itemPuntero.contieneItem()) {
				this.cerrarCofre();
			}
		}
	}

	public void cerrarCofre() {
		this.cofreAbierto = null;
		this.contenedorEncontrado = null;
	}

	private void actualizarZoom() {
		final int rueda = this.RATON.getRotacionRueda();
		if (rueda < 0) {
			Globales.CAMARA.aumentarZoom();
		} else if (rueda > 0) {
			Globales.CAMARA.reducirZoom();
		}

		if (Globales.TECLADO.TECLA_ZOOM_IN.presionadoUnicaActualizacion()) {
			Globales.CAMARA.aumentarZoom();
		} else if (Globales.TECLADO.TECLA_ZOOM_OUT.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reducirZoom();
		}
		if (Globales.TECLADO.TECLA_ZOOM_REINICIAR.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reiniciarZoom();
		}
	}

	private void actualizarProyeccionRaton() {
		final int viewW = this.PALETA_MAPA.width;
		final int centroVX = viewW / 2;
		final int centroVY = Constantes.ALTO_JUEGO / 2;

		final double z = Math.max(0.2, Globales.CAMARA.getZoom());
		final int mouseX = this.RATON.getPosicionXEscalada();
		final int mouseY = this.RATON.getPosicionYEscalada();

		if (mouseX < viewW) {
			final double dx = (mouseX - centroVX) / z;
			final double dy = (mouseY - centroVY) / z;

			this.AREA_MOUSE_APUNTADO.x = (int) Math.round(this.x + dx);
			this.AREA_MOUSE_APUNTADO.y = (int) Math.round(this.y + dy);
			this.tileApuntadoValido = true;
		} else {
			this.tileApuntadoValido = false;
		}
	}

	private void actualizarAtajosPinceles() {
		if (Globales.TECLADO.TECLA_NUM_1.presionadoUnicaActualizacion()) {
			this.tamanoPincel = 1;
		} else if (Globales.TECLADO.TECLA_NUM_2.presionadoUnicaActualizacion()) {
			this.tamanoPincel = 2;
		} else if (Globales.TECLADO.TECLA_NUM_3.presionadoUnicaActualizacion()) {
			this.tamanoPincel = 3;
		} else if (Globales.TECLADO.TECLA_NUM_4.presionadoUnicaActualizacion()) {
			this.tamanoPincel = 4;
		}

		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionadoUnicaActualizacion()) {
			this.pincelCircular = !this.pincelCircular;
		}
	}

	private void actualizarTileApuntado() {
		if (!this.tileApuntadoValido) {
			return;
		}

		final int baseTX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
		final int baseTY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);

		final int offset = (this.tamanoPincel - 1) / 2;
		final int startTX = baseTX - offset;
		final int startTY = baseTY - offset;
		final int anchoPx = this.tamanoPincel * this.LADO_TILE;
		final int altoPx = this.tamanoPincel * this.LADO_TILE;

		this.areaTileSelected.setBounds(startTX * this.LADO_TILE, startTY * this.LADO_TILE, anchoPx, altoPx);
	}

	private void alterarElementoSeleccionado() {
		if (this.itemPuntero.contieneItem()) {
			return;
		}

		if (this.RATON.presionadoClickIzq() && this.tileApuntadoValido) {
			final Paleta paleta = this.PALETAS.getPaletaActual();
			if (paleta == null) {
				return;
			}

			// 1. Pincel de Suelos
			if (paleta instanceof PaletaTile) {
				final PaletaTile paletaTile = (PaletaTile) paleta;
				final Tile tilePaleta = paletaTile.getTileSeleccionado();
				if (tilePaleta == null) {
					return;
				}

				if (this.areaTileSelected.equals(this.ultimaAreaTileAlterado)) {
					return;
				}
				this.ultimaAreaTileAlterado.setBounds(this.areaTileSelected);

				final int startTX = this.areaTileSelected.x / this.LADO_TILE;
				final int startTY = this.areaTileSelected.y / this.LADO_TILE;

				for (int dy = 0; dy < this.tamanoPincel; dy++) {
					for (int dx = 0; dx < this.tamanoPincel; dx++) {
						final int curTX = startTX + dx;
						final int curTY = startTY + dy;

						if (this.pincelCircular && (this.tamanoPincel > 2)) {
							final double centroRel = (this.tamanoPincel - 1) / 2.0;
							final double dxRel = dx - centroRel;
							final double dyRel = dy - centroRel;
							final double distSq = (dxRel * dxRel) + (dyRel * dyRel);
							final double maxRadioSq = (this.tamanoPincel / 2.0) * (this.tamanoPincel / 2.0);
							if (distSq > maxRadioSq) {
								continue;
							}
						}

						this.TERRENO.establecerTileReferenciado(curTX * this.LADO_TILE, curTY * this.LADO_TILE,
								tilePaleta);
					}
				}
			}
			// 2. Colocación de Recursos y Objetos
			else if (paleta instanceof PaletaComplento) {
				final PaletaComplento paletaObj = (PaletaComplento) paleta;
				if (!this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
					return;
				}
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaComplento.EntradaPaleta entrada = paletaObj.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					final int posX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
					final int posY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

					final Objeto nuevoObj = paletaObj.crearInstanciaSeleccionada(posX, posY);
					if (nuevoObj != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevoObj);
					}
				}
			}
			// 3. Colocación de Criaturas
			else if (paleta instanceof PaletaCriaturas) {
				final PaletaCriaturas paletaCriat = (PaletaCriaturas) paleta;
				if (!this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
					return;
				}
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaCriaturas.EntradaCriatura entrada = paletaCriat.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					final int spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
					final int spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

					final int hitboxX = spriteX + entrada.margenX;
					final int hitboxY = spriteY + entrada.margenY;

					final Criatura nuevaCriat = entrada.creador.crear(hitboxX, hitboxY);
					if (nuevaCriat != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevaCriat);
					}
				}
			}
			// 4. Colocación de Puntos de Spawn
			else if (paleta instanceof PaletaSpawns) {
				final PaletaSpawns paletaSpawns = (PaletaSpawns) paleta;
				if (!this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
					return;
				}
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final String nombreSpawn = paletaSpawns.getNombreSpawnSeleccionado();
				final int snapX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE) * this.LADO_TILE;
				final int snapY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE) * this.LADO_TILE;

				this.MUNDO_EDITOR.agregarSpawn(new Spawn(snapX, snapY, nombreSpawn));
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}
	}

	private void borrarElemento() {
		if (this.itemPuntero.contieneItem()) {
			if (this.RATON.presionadoClickDerUnicaAct()) {
				this.itemPuntero.limpiar();
			}
			return;
		}

		final boolean clickDer = this.RATON.presionadoClickDer() || this.RATON.presionadoClickDerUnicaAct();

		if (clickDer && this.tileApuntadoValido) {
			final int radioBorrado = Math.max(8, this.LADO_TILE / 2);
			this.AREA_BORRADO_AUX.setBounds(this.AREA_MOUSE_APUNTADO.x - radioBorrado,
					this.AREA_MOUSE_APUNTADO.y - radioBorrado, radioBorrado * 2, radioBorrado * 2);

			// 1. Borrar Spawn si se hace clic derecho sobre él
			final Spawn spawnBorrado = this.MUNDO_EDITOR.eliminarSpawnEn(this.AREA_MOUSE_APUNTADO.x,
					this.AREA_MOUSE_APUNTADO.y, radioBorrado);
			if (spawnBorrado != null) {
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}

			// 2. Borrar Entidades y Objetos
			this.listaEntesABorrar.clear();

			this.MUNDO_EDITOR.paraCadaEnteEn(this.AREA_BORRADO_AUX, false, new AccionEntidad<Ente>() {
				@Override
				public void ejecutar(final Ente ente) {
					if ((ente != null) && !ente.estaEliminado()) {
						EditorMapa.this.listaEntesABorrar.add(ente);
					}
				}
			});

			for (int i = 0; i < this.listaEntesABorrar.size(); i++) {
				this.MUNDO_EDITOR.eliminarEntidad(this.listaEntesABorrar.get(i));
			}

			this.listaEntesABorrar.clear();
		}
	}

	private void mover() {
		int velocidad = 4;
		if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
			velocidad = 14;
		}

		if (Globales.TECLADO.TECLA_ARRIBA.presionado()) {
			this.y -= velocidad;
		}
		if (Globales.TECLADO.TECLA_ABAJO.presionado()) {
			this.y += velocidad;
		}
		if (Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
			this.x -= velocidad;
		}
		if (Globales.TECLADO.TECLA_DERECHA.presionado()) {
			this.x += velocidad;
		}

		this.asistenteCamara.setPosicion(this.x, this.y);
	}

	private void verificarBuffer(final Graphics2D g) {
		final int w = Constantes.ANCHO_JUEGO;
		final int h = Constantes.ALTO_JUEGO;

		if ((this.bufferEditor == null) || (this.bufferEditor.getWidth() != w) || (this.bufferEditor.getHeight() != h)
				|| (this.bufferEditor.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.bufferEditor != null) {
				this.bufferEditor.flush();
			}
			this.bufferEditor = g.getDeviceConfiguration().createCompatibleVolatileImage(w, h, Transparency.OPAQUE);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.verificarBuffer(g);

		final int viewW = this.PALETA_MAPA.width;
		final int centroVX = viewW / 2;
		final int centroVY = Constantes.ALTO_JUEGO / 2;
		final double z = Math.max(0.2, Globales.CAMARA.getZoom());

		// 1. Renderizado a escala 1:1 en VolatileImage
		final Graphics2D gBuf = this.bufferEditor.createGraphics();
		try {
			gBuf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gBuf.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			Render2D.dibujarRectanguloRelleno(gBuf, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

			this.TERRENO.pintar(gBuf);
			this.MUNDO_EDITOR.pintar(gBuf);
			this.pintarSpawnsEnMundo(gBuf);
			this.pintarPreviewColocacion(gBuf);

		} finally {
			gBuf.dispose();
		}

		// 2. Proyección sobre el Viewport del editor
		final Graphics2D gView = (Graphics2D) g.create();
		try {
			gView.setClip(this.PALETA_MAPA);
			gView.translate(centroVX, centroVY);
			gView.scale(z, z);
			gView.drawImage(this.bufferEditor, -Constantes.CENTROX, -Constantes.CENTROY, null);
		} finally {
			gView.dispose();
		}

		// 3. Renderizado de Contenedor / Cofre Abierto (HUD 1:1)
		if (this.cofreAbierto != null) {
			this.cofreAbierto.pintar(g);
			this.cofreAbierto.pintarTooltips(g);
		}

		// 4. Capas de interfaz lateral 1:1 y Tooltips
		this.pintarCoordenadas(g);
		this.PALETAS.pintar(g);
		this.pintarTooltipPaleta(g);

		// 5. Ítem flotante arrastrado con el cursor (Capa superior)
		this.itemPuntero.pintar(g, this.RATON.getPuntoPosicionEscalado());
	}

	private void pintarSpawnsEnMundo(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_SPAWN);

		for (final Spawn s : this.MUNDO_EDITOR.getPuntosSpawn()) {
			final int sx = s.getX();
			final int sy = s.getY();
			final boolean esComienzo = s.getNombre().equalsIgnoreCase(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);

			final Color colorMarco = esComienzo ? new Color(255, 215, 0) : new Color(70, 180, 255);
			final Color colorFondo = esComienzo ? new Color(255, 215, 0, 90) : new Color(70, 180, 255, 75);

			// Área translúcida y borde
			Render2D.dibujarRectanguloRellenoRefCamara(g, sx, sy, 16, 16, colorFondo);
			Render2D.dibujarRectanguloContornoRefCamara(g, sx, sy, 16, 16, colorMarco);

			// Nombre descriptivo flotante sobre el Spawn
			final String txt = s.getNombre();
			final int anchoTxt = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt);
			final int tx = (sx + 8) - (anchoTxt / 2);
			final int ty = sy - 3;

			Render2D.dibujarStringConSombraRefCamara(g, txt, tx, ty, colorMarco, Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	private void pintarPreviewColocacion(final Graphics2D g) {
		if (!this.tileApuntadoValido || this.itemPuntero.contieneItem()) {
			return;
		}

		final Paleta paleta = this.PALETAS.getPaletaActual();

		if (paleta instanceof PaletaTile) {
			if (this.pincelCircular && (this.tamanoPincel > 2)) {
				Render2D.dibujarFiguraEllipseRefCamara(g, this.areaTileSelected, Color.MAGENTA);
			} else {
				Render2D.dibujarRectanguloContornoRefCamara(g, this.areaTileSelected, Color.MAGENTA);
			}

		} else if (paleta instanceof PaletaComplento) {
			final PaletaComplento p = (PaletaComplento) paleta;
			final PaletaComplento.EntradaPaleta entrada = p.getEntradaSeleccionada();

			if ((entrada != null) && (entrada.icono != null)) {
				final int posX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
				final int posY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, posX, posY, 0.65f);
				final Color color = entrada.esCosechable ? Color.GREEN : Color.CYAN;
				Render2D.dibujarRectanguloContornoRefCamara(g, posX, posY, entrada.icono.getWidth(),
						entrada.icono.getHeight(), color);
			}

		} else if (paleta instanceof PaletaCriaturas) {
			final PaletaCriaturas p = (PaletaCriaturas) paleta;
			final PaletaCriaturas.EntradaCriatura entrada = p.getEntradaSeleccionada();

			if ((entrada != null) && (entrada.icono != null)) {
				final int spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
				final int spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, spriteX, spriteY, 0.65f);
				Render2D.dibujarRectanguloContornoRefCamara(g, spriteX + entrada.margenX, spriteY + entrada.margenY,
						entrada.anchoHitbox, entrada.altoHitbox, Color.RED);
			}
		} else if (paleta instanceof PaletaSpawns) {
			final PaletaSpawns p = (PaletaSpawns) paleta;
			final String nombre = p.getNombreSpawnSeleccionado();
			final int snapX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE) * this.LADO_TILE;
			final int snapY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE) * this.LADO_TILE;

			final boolean esComienzo = nombre.equalsIgnoreCase(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
			final Color colorMarco = esComienzo ? Color.YELLOW : Color.CYAN;

			Render2D.dibujarRectanguloRellenoRefCamara(g, snapX, snapY, 16, 16,
					new Color(colorMarco.getRed(), colorMarco.getGreen(), colorMarco.getBlue(), 100));
			Render2D.dibujarRectanguloContornoRefCamara(g, snapX, snapY, 16, 16, colorMarco);

			final Font fontPrevia = g.getFont();
			g.setFont(FUENTE_SPAWN);
			final int anchoTxt = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			Render2D.dibujarStringConSombraRefCamara(g, nombre, (snapX + 8) - (anchoTxt / 2), snapY - 3, colorMarco,
					Color.BLACK);
			g.setFont(fontPrevia);
		}
	}

	private void pintarTooltipPaleta(final Graphics2D g) {
		final Point pMouse = this.RATON.getPuntoPosicionEscalado();
		final Paleta paleta = this.PALETAS.getPaletaActual();

		if ((paleta != null) && paleta.AREA.contains(pMouse)) {
			final int relX = pMouse.x - (paleta.AREA.x + paleta.MARGEN);
			final int relY = pMouse.y - (paleta.AREA.y + paleta.MARGEN);
			final int paso = paleta.LADO_SLOT + paleta.MARGEN;
			final int col = relX / paso;
			final int fila = relY / paso;

			if ((col >= 0) && (col < paleta.COLUMNAS) && (fila >= 0) && (fila < paleta.FILAS)) {
				final int index = (paleta.paginaActual * paleta.ELEMENTOS_POR_PAGINA) + (fila * paleta.COLUMNAS) + col;
				if (index < paleta.getCantidadTotalElementos()) {
					final String nombre = paleta.getNombreElemento(index);
					Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltip(g, nombre, Color.WHITE,
							new Color(20, 20, 25, 230));
				}
			}
		}
	}

	private void pintarCoordenadas(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_INFO);

		Render2D.dibujarString(g,
				"Pincel: " + this.tamanoPincel + "x" + this.tamanoPincel + " ("
						+ (this.pincelCircular ? "Circulo" : "Cuadrado") + ") | Zoom: "
						+ String.format("%.2f", Globales.CAMARA.getZoom()) + "x",
				20, 165, Color.CYAN);
		Render2D.dibujarString(g,
				"Posicion Cursor: (X: " + this.AREA_MOUSE_APUNTADO.x + ", Y: " + this.AREA_MOUSE_APUNTADO.y + ")", 20,
				175, Color.WHITE);
		Render2D.dibujarString(g, "Camara Fantasma: (X: " + this.x + ", Y: " + this.y + ")", 20, 185, Color.GREEN);
		Render2D.dibujarString(g,
				"Teclas: [E / Shift+Click] Abrir Cofre | [Click Der en Cofre] Borrar Item | [Enter] Guardar | [Rueda] Zoom",
				20, 195, Color.YELLOW);

		g.setFont(fontPrevia);
	}

	public void guardarMapa(final String nombre) {
		final JSONObject jsonEntes = this.MUNDO_EDITOR.getEntesInJson();
		final String criaturas = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class);
		final String items = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class);
		final String complementos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class);
		final String objetos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class);
		final String spawns = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class);

		final Escenario esc = new Escenario(this.TERRENO, jsonEntes.get(criaturas).toString(),
				jsonEntes.get(items).toString(), jsonEntes.get(complementos).toString(),
				jsonEntes.get(objetos).toString(), jsonEntes.get(spawns).toString());

		final File carpetaDestino = new File("mundos" + File.separator + nombre);
		EscenarioLoader.exportarEscenario(esc, carpetaDestino);
		System.out.println("Mapa exportado exitosamente con Spawns en: " + carpetaDestino.getAbsolutePath());
	}

	public ItemPuntero getItemPuntero() {
		return this.itemPuntero;
	}
}