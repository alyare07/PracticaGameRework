package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;

import principal.controles.Raton;
import principal.entes.criaturas.Criatura;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.mapa.GroupTile;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Estado interactivo del motor que implementa el Editor de Mapas en tiempo
 * real.
 * <p>
 * <b>Funcionalidades y Arquitectura:</b>
 * <ul>
 * <li><b>Pincel Inteligente de Terrenos:</b> Modifica tiles individuales o
 * bloques {@link GroupTile} (2x2), recalculando automáticamente los autotiles y
 * variaciones en tiempo constante $O(1)$.</li>
 * <li><b>Colocación de Entidades y Complementos:</b> Permite posicionar objetos
 * interactivos, items y elementos decorativos con previsualización física de
 * cajas de colisión (T1/T2).</li>
 * <li><b>Proyección de Coordenadas de Ratón:</b> Transforma las coordenadas de
 * pantalla escaladas del cursor a coordenadas continuas y discretas del mundo
 * teniendo en cuenta el desplazamiento de la cámara.</li>
 * <li><b>Frustum Culling Adaptado a la UI:</b> Renderiza únicamente los tiles
 * visibles en el viewport libre, excluyendo de forma estricta el área reservada
 * para la paleta lateral de herramientas.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EditorMapa implements EstadoJuego {

	/** Gestor principal de la máquina de estados del juego. */
	private final GestorEstados GE;

	/** Tamaño en píxeles de un tile individual (ej: 16 px). */
	private final int LADO_TILE;

	/**
	 * Tamaño en píxeles de un bloque de tiles (habitualmente
	 * {@code LADO_TILE * 2}).
	 */
	private final int LADO_GRUPO_TILE;

	/** Ancho total del terreno editable en píxeles. */
	private final int ANCHO;

	/** Alto total del terreno editable en píxeles. */
	private final int ALTO;

	/** Instancia del terreno que se está editando en tiempo real. */
	private final Terreno MAPA;

	/** Posición X de la cámara libre del editor en el mundo. */
	private int x;

	/** Posición Y de la cámara libre del editor en el mundo. */
	private int y;

	/** Referencia al gestor de entrada del ratón. */
	private final Raton RATON = SuperficieDibujo.obetenerSuperficieDibujo().RATON;

	/**
	 * Delimitador espacial reutilizable del tile actualmente apuntado por el
	 * cursor.
	 */
	private final Rectangle areaTileSelected = new Rectangle();

	/**
	 * Bandera que indica si el cursor está apuntando a un tile válido dentro del
	 * lienzo.
	 */
	private boolean tileApuntadoValido = false;

	/**
	 * Registro del último tile alterado para evitar re-escrituras redundantes al
	 * mantener el clic.
	 */
	private final Rectangle ultimaAreaTileAlterado = new Rectangle();

	/**
	 * Delimitador del área de visualización del mapa (excluyendo la paleta
	 * lateral).
	 */
	private final Rectangle PALETA_MAPA;

	/**
	 * Gestor y contenedor de las paletas de herramientas (Tiles, Complementos,
	 * Entidades).
	 */
	private final GrupoPaleta PALETAS;

	/** Temporizador para regular el guardado de mapas por teclado. */
	private final GestorTiempo GT = new GestorTiempo();

	/**
	 * Temporizador para regular la cadencia de colocación de complementos y
	 * entidades.
	 */
	private final GestorTiempo GT_COLOCACION = new GestorTiempo();

	/**
	 * Tiempo mínimo de espera en milisegundos entre colocaciones sucesivas de
	 * complementos.
	 */
	private final int TIEMPO_ESPERA_MS_COLOCACION = 300;

	/** Contenedor de entidades activas en el entorno del editor. */
	private final MundoEditor MUNDO_EDITOR;

	/**
	 * Rectángulo auxiliar reutilizable para proyectar la punta del cursor en
	 * coordenadas de mundo.
	 */
	private final Rectangle AREA_MOUSE_APUNTADO = new Rectangle(-1, -1, 1, 1);

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/**
	 * Crea un nuevo mapa en blanco con las dimensiones especificadas y un modelo de
	 * terreno inicial.
	 *
	 * @param ladoTile     Dimensión en píxeles de cada tile.
	 * @param anchoTiles   Cantidad de tiles a lo ancho.
	 * @param altoTiles    Cantidad de tiles a lo alto.
	 * @param idModeloTile ID del modelo de terreno base inicial.
	 * @param ge           Gestor de estados del motor.
	 */
	public EditorMapa(final int ladoTile, final int anchoTiles, final int altoTiles, final int idModeloTile,
			final GestorEstados ge) {
		this.GE = ge;
		this.LADO_TILE = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.ANCHO = anchoTiles * ladoTile;
		this.ALTO = altoTiles * ladoTile;
		this.MAPA = new Terreno(anchoTiles, altoTiles, this.LADO_TILE, idModeloTile);

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height);
		this.MUNDO_EDITOR = new MundoEditor(this.MAPA);

		this.inicializarCamara();
	}

	/**
	 * Inicializa el editor a partir de una instancia de {@link Terreno} existente
	 * en memoria.
	 *
	 * @param terreno Instancia del terreno a editar.
	 * @param ge      Gestor de estados.
	 */
	public EditorMapa(final Terreno terreno, final GestorEstados ge) {
		this.GE = ge;
		this.MAPA = terreno;
		this.ANCHO = this.MAPA.getAncho();
		this.ALTO = this.MAPA.getAlto();
		this.LADO_TILE = this.MAPA.ladoTile();
		this.LADO_GRUPO_TILE = this.LADO_TILE * 2;

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height);
		this.MUNDO_EDITOR = new MundoEditor(this.MAPA);

		this.inicializarCamara();
	}

	/**
	 * Carga un mapa existente desde un archivo serializado en disco para su
	 * edición.
	 *
	 * @param rutaMapa Ruta relativa o absoluta del archivo de mapa (.mp).
	 * @param ge       Gestor de estados.
	 */
	public EditorMapa(final String rutaMapa, final GestorEstados ge) {
		this.GE = ge;
		this.MAPA = EscenarioLoader.importarEscenario(new File(rutaMapa)).getTerreno();
		this.ANCHO = this.MAPA.getAncho();
		this.ALTO = this.MAPA.getAlto();
		this.LADO_TILE = this.MAPA.ladoTile();
		this.LADO_GRUPO_TILE = this.LADO_TILE * 2;

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height);
		this.MUNDO_EDITOR = new MundoEditor(this.MAPA);

		this.inicializarCamara();
	}

	/**
	 * Configura el enfoque inicial de la cámara libre en la posición del jugador
	 * auxiliar.
	 */
	private void inicializarCamara() {
		Globales.CAMARA.setEntidadEnfocada(Globales.JUGADOR);
		Globales.JUGADOR.setPosicion(Globales.JUGADOR.getMargenX(), Globales.JUGADOR.getMargenY());
		this.x = Globales.JUGADOR.getMargenX();
		this.y = Globales.JUGADOR.getMargenY();
		Globales.JUGADOR.setPosicion(this.x, this.y);
	}

	// =========================================================================
	// === BUCLE DE ACTUALIZACIÓN LÓGICA (TICK)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: PROYECCIÓN DE COORDENADAS (PANTALLA A MUNDO)
	 * ------------------------------------------------------------------------- 1.
	 * El ratón entrega coordenadas escaladas en el espacio de pantalla (rr.x,
	 * rr.y). 2. Se le suma la posición de la cámara
	 * (Globales.CAMARA.getPosicionXInt()) y se resta el margen de centrado
	 * (getMargenX()) para obtener la coordenada exacta del cursor en píxeles
	 * continuos dentro del mundo (AREA_MOUSE_APUNTADO).
	 * =========================================================================
	 */
	@Override
	public void actualizar() {
		this.RATON.actualizar(SuperficieDibujo.obetenerSuperficieDibujo());
		final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();

		this.AREA_MOUSE_APUNTADO.x = (rr.x + Globales.CAMARA.getPosicionXInt()) - Globales.CAMARA.getMargenX();
		this.AREA_MOUSE_APUNTADO.y = (rr.y + Globales.CAMARA.getPosicionYInt()) - Globales.CAMARA.getMargenY();

		this.mover();
		this.actualizarTileApuntado();
		this.PALETAS.actualizar(this.RATON);
		this.alterarTileSeleccionado();
		this.MUNDO_EDITOR.actualizar();

		// Atajo de teclado: Guardar Mapa (con limitador de 1 segundo)
		if (Globales.TECLADO.TECLA_GUARDAR_MAPA.presionado()) {
			if (this.GT.transcurrioSegundos(1)) {
				this.GT.establecerReferenciaTiempoActual();
				this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			}
		} else if (Globales.TECLADO.TECLA_ESCAPE.presionado()) {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
			this.GE.disposeEditor();
		}

		// Atajo de teclado: Alternar modo de selección Grupo (2x2) vs Tile individual
		// (1x1)
		if (Globales.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado()) {
			if (!Globales.editorSelectGroupTile) {
				Globales.editorSelectGroupTile = true;
			}
		} else if (Globales.editorSelectGroupTile) {
			Globales.editorSelectGroupTile = false;
		}
	}

	/**
	 * Determina el tile o bloque del terreno sobre el que se encuentra el cursor
	 * del ratón. Optimizado para CERO asignaciones en el bucle mediante
	 * reutilización de {@link #areaTileSelected}.
	 */
	private void actualizarTileApuntado() {
		// Validar que el cursor no esté sobre la interfaz lateral de paletas
		if (this.AREA_MOUSE_APUNTADO.x > (this.x + this.PALETA_MAPA.width)) {
			this.tileApuntadoValido = false;
			return;
		}

		final Tile t = Globales.editorSelectGroupTile
				? this.MAPA.getGrupoTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y)
				: this.MAPA.getTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y);

		if (t == null) {
			this.tileApuntadoValido = false;
			return;
		}

		this.areaTileSelected.setBounds(t.getPosicionX(), t.getPosicionY(), t.getLado(), t.getLado());
		this.tileApuntadoValido = true;
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: PINCEL INTELIGENTE Y ACTUALIZACIÓN EN TIEMPO REAL O(1)
	 * ------------------------------------------------------------------------- Al
	 * mantener presionado el clic izquierdo: 1. Si la herramienta activa es
	 * 'PaletaTile': - Verifica que el tile no tenga ya el mismo modelo asignado. -
	 * Si es modo 'GroupTile', actualiza los 4 tiles (2x2) en lote sin instanciar
	 * 'Point'. - Si es modo individual, llama a 'MAPA.establecerTileReferenciado(x,
	 * y, tile)'. - 'Terreno' actualiza de forma inmediata los autotiles del tile y
	 * sus 4 vecinos cardinales. 2. Si la herramienta activa es 'PaletaComplento': -
	 * Posiciona el complemento centrado en el cursor con cadencia controlada por
	 * temporizador.
	 * =========================================================================
	 */
	private void alterarTileSeleccionado() {
		if (this.RATON.presionadoClickIzq() && this.PALETA_MAPA.intersects(this.RATON.getPuntoPresionado())) {
			final Paleta paleta = this.PALETAS.getPaletaActual();
			if (!this.tileApuntadoValido) {
				return;
			}

			final Tile tileTerrenoSeleccionado = Globales.editorSelectGroupTile
					? this.MAPA.getGrupoTileReferenciado(this.areaTileSelected.x, this.areaTileSelected.y)
					: this.MAPA.getTileReferenciado(this.areaTileSelected.x, this.areaTileSelected.y);

			if (paleta instanceof PaletaTile) {
				final PaletaTile paletaTile = ((PaletaTile) paleta);
				final Tile tilePaletaSeleccionado = paletaTile.getTileSeleccionado();
				if (tilePaletaSeleccionado == null) {
					return;
				}

				if (paletaTile.valoresYaEstalecidosPreviamente(tileTerrenoSeleccionado)) {
					return;
				}
				if (this.areaTileSelected.equals(this.ultimaAreaTileAlterado)) {
					return;
				}
				this.ultimaAreaTileAlterado.setBounds(this.areaTileSelected);

				if (tileTerrenoSeleccionado instanceof GroupTile) {
					final GroupTile gt = (GroupTile) tileTerrenoSeleccionado;

					// Actualización en bloque 2x2 sin instanciar objetos
					this.MAPA.establecerTileReferenciado(gt.getTile1().getPosicionX(), gt.getTile1().getPosicionY(),
							tilePaletaSeleccionado);
					this.MAPA.establecerTileReferenciado(gt.getTile2().getPosicionX(), gt.getTile2().getPosicionY(),
							tilePaletaSeleccionado);
					this.MAPA.establecerTileReferenciado(gt.getTile3().getPosicionX(), gt.getTile3().getPosicionY(),
							tilePaletaSeleccionado);
					this.MAPA.establecerTileReferenciado(gt.getTile4().getPosicionX(), gt.getTile4().getPosicionY(),
							tilePaletaSeleccionado);
				} else {
					this.MAPA.establecerTileReferenciado(this.areaTileSelected.x, this.areaTileSelected.y,
							tilePaletaSeleccionado);
				}

			} else if (paleta instanceof PaletaComplento) {
				final PaletaComplento paletaComplemento = ((PaletaComplento) paleta);
				if (paletaComplemento.getComplementoSeleccionado() != null) {
					if (paletaComplemento.valoresYaEstalecidosPreviamente(tileTerrenoSeleccionado)) {
						return;
					}
					if (!this.GT_COLOCACION.transcurrioMiliSegundos(this.TIEMPO_ESPERA_MS_COLOCACION)) {
						return;
					}
					this.GT_COLOCACION.establecerReferenciaTiempoActual();

					final int pos = paletaComplemento.getPosicionamientoActual();
					final Complemento c = paletaComplemento.getComplementoSeleccionado();
					final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();

					final int posX = (rr.x + Globales.CAMARA.getPosicionXInt()) - Globales.CAMARA.getMargenX()
							- (c.getTextura().getWidth() / 2);
					final int posY = (rr.y + Globales.CAMARA.getPosicionYInt()) - Globales.CAMARA.getMargenY()
							- (c.getTextura().getHeight() / 2);

					switch (pos) {
					case PaletaComplento.POSICIONAMIENTO_CENTRO:
						this.MUNDO_EDITOR.meterEntidad(new Complemento(posX, posY, c.getCodigoModelo()));
						break;
					default:
						break;
					}
				}
			}
		}
	}

	/**
	 * Controla el desplazamiento libre de la cámara en el editor mediante el
	 * teclado, aplicando velocidad aumentada al mantener pulsada la tecla de
	 * correr.
	 */
	private void mover() {
		int velocidad = 1;
		if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
			velocidad = 8;
		}
		if (Globales.TECLADO.TECLA_ARRIBA.presionado()) {
			if ((this.y - velocidad) >= 0) {
				this.y -= velocidad;
				Globales.JUGADOR.setPosicion(this.x, this.y);
			}
		}
		if (Globales.TECLADO.TECLA_ABAJO.presionado()) {
			if ((this.y + velocidad) <= (this.ALTO - (Constantes.ALTO_JUEGO / 2))) {
				this.y += velocidad;
				Globales.JUGADOR.setPosicion(this.x, this.y);
			}
		}
		if (Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
			if ((this.x - velocidad) >= 0) {
				this.x -= velocidad;
				Globales.JUGADOR.setPosicion(this.x, this.y);
			}
		}
		if (Globales.TECLADO.TECLA_DERECHA.presionado()) {
			if ((this.x + velocidad) <= (this.ANCHO - this.PALETAS.AREA.width)) {
				this.x += velocidad;
				Globales.JUGADOR.setPosicion(this.x, this.y);
			}
		}
	}

	// =========================================================================
	// === BUCLE DE RENDERIZADO (FRAME)
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarTerreno(g);
		this.MUNDO_EDITOR.pintar(g);
		this.pintarCoordenadas(g);
		this.pintarTileSelectedTerreno(g);
		this.PALETAS.pintar(g);
	}

	/**
	 * Renderiza el terreno aplicando Frustum Culling alineado a la grilla y
	 * excluyendo el área lateral de la paleta.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	private void pintarTerreno(final Graphics2D g) {
		final int minX = Globales.CAMARA.getPosicionXInt() - Constantes.CENTROX - this.LADO_GRUPO_TILE;
		final int maxX = (Globales.CAMARA.getPosicionXInt() + Constantes.CENTROX + this.LADO_GRUPO_TILE)
				- this.PALETAS.AREA.width;

		final int minY = Globales.CAMARA.getPosicionYInt() - Constantes.CENTROY - this.LADO_GRUPO_TILE;
		final int maxY = Globales.CAMARA.getPosicionYInt() + Constantes.CENTROY + this.LADO_GRUPO_TILE;

		final int inicioX = Math.floorDiv(minX, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;
		final int finX = Math.floorDiv(maxX, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;

		final int inicioY = Math.floorDiv(minY, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;
		final int finY = Math.floorDiv(maxY, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;

		GroupTile gt = null;

		for (int yActual = inicioY; yActual <= finY; yActual += this.LADO_GRUPO_TILE) {
			for (int xActual = inicioX; xActual <= finX; xActual += this.LADO_GRUPO_TILE) {
				gt = this.MAPA.getGrupoTileReferenciado(xActual, yActual);
				if (gt != null) {
					gt.pintarEditor(g);
				}
			}
		}
	}

	/**
	 * Muestra en pantalla la previsualización del cursor de selección y la silueta
	 * del complemento a colocar.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	private void pintarTileSelectedTerreno(final Graphics2D g) {
		if (this.tileApuntadoValido && (this.PALETAS.getPaletaActual() instanceof PaletaTile)) {
			DibujoDebug.dibujarRectanguloContorno(g,
					(this.areaTileSelected.x - Globales.CAMARA.getPosicionXInt()) + Globales.CAMARA.getMargenX(),
					(this.areaTileSelected.y - Globales.CAMARA.getPosicionYInt()) + Globales.CAMARA.getMargenY(),
					this.areaTileSelected.width, this.areaTileSelected.height, Color.MAGENTA);
		} else if (this.tileApuntadoValido && (this.PALETAS.getPaletaActual() instanceof PaletaComplento)) {
			final PaletaComplento p = (PaletaComplento) this.PALETAS.getPaletaActual();
			if (p.getComplementoSeleccionado() != null) {
				final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();

				final int posX = rr.x - (p.getComplementoSeleccionado().getTextura().getWidth() / 2);
				final int posY = rr.y - (p.getComplementoSeleccionado().getTextura().getHeight() / 2);

				DibujoDebug.dibujarImagen(g, p.getComplementoSeleccionado().getTextura(), posX, posY);

				// Previsualización de cajas de colisión para complementos
				if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
					final int codModelo = p.getComplementoSeleccionado().getCodigoModelo();
					final Object modelo = ListaModeloComplemento.getModeloComplemento(codModelo);

					if (modelo instanceof ModeloComplementoT1) {
						final Rectangle colision = ((ModeloComplementoT1) modelo)
								.getMargenesInterseccionEnBasePosicion(posX, posY);
						DibujoDebug.dibujarRectanguloContorno(g, colision, Color.YELLOW);
					} else if (modelo instanceof ModeloComplementoT2) {
						for (final Rectangle colision : ((ModeloComplementoT2) modelo)
								.getMargenesInterseccionEnBasePosicion(posX, posY)) {
							DibujoDebug.dibujarRectanguloContorno(g, colision, Color.YELLOW);
						}
					}
				}
			}
		}
	}

	/**
	 * Renderiza el HUD de información de coordenadas del cursor y de la cámara en
	 * pantalla.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	private void pintarCoordenadas(final Graphics2D g) {
		if (this.PALETAS.getPaletaActual() instanceof PaletaTile) {
			DibujoDebug.dibujarString(g,
					"Area Apuntada: " + (this.tileApuntadoValido
							? ("(X: " + this.areaTileSelected.x + " , Y: " + this.areaTileSelected.y + " , W: "
									+ this.areaTileSelected.width + " , H: " + this.areaTileSelected.height + ")")
							: "none"),
					20, 170, Color.WHITE);
		} else {
			DibujoDebug.dibujarString(g,
					"Area Mouse Apuntado: " + ("(X: " + this.AREA_MOUSE_APUNTADO.x + " , Y: "
							+ this.AREA_MOUSE_APUNTADO.y + " , W: " + this.AREA_MOUSE_APUNTADO.width + " , H: "
							+ this.AREA_MOUSE_APUNTADO.height + ")"),
					20, 170, Color.WHITE);
			DibujoDebug.dibujarRectanguloContorno(g, this.RATON.getRectanguloPosicionEscalado(), Color.BLUE);
		}
		DibujoDebug.dibujarString(g, "X Centro: " + this.x, 20, 180, Color.GREEN);
		DibujoDebug.dibujarString(g, "Y Centro: " + this.y, 20, 190, Color.GREEN);
	}

	// =========================================================================
	// === PERSISTENCIA Y GUARDADO
	// =========================================================================

	/**
	 * Empaqueta el terreno y todas las entidades activas del editor en un
	 * {@link Escenario} y lo exporta a disco en formato estructurado.
	 *
	 * @param nombre Nombre del archivo de destino (ej: "Mapa_2026.mp").
	 */
	public void guardarMapa(final String nombre) {
		final JSONObject jsonEntes = this.MUNDO_EDITOR.getEntesInJson();
		final String criaturas = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class);
		final String items = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class);
		final String complementos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class);
		final String objetos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class);

		final Escenario esc = new Escenario(this.MAPA, jsonEntes.get(criaturas).toString(),
				jsonEntes.get(items).toString(), jsonEntes.get(complementos).toString(),
				jsonEntes.get(objetos).toString());

		final File carpetaDestino = new File("mundos" + File.separator + nombre);
		EscenarioLoader.exportarEscenario(esc, carpetaDestino);
		System.out.println("Mapa guardado exitosamente en: " + carpetaDestino.getAbsolutePath());
	}
}