package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.dialogos.MensajeDialogo;
import principal.entes.criaturas.jugador.Jugador;
import principal.interaccion.Interactuable;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Entrada física e interactuable a una cueva inestable (Zero-GC / O(1)).
 * <p>
 * Características: 1. Ciclo finito O(1): Evalúa caducidad por diferencia
 * matemática con GestorAstronomico. 2. Gráficos dinámicos: Conmuta entre boca
 * oscura abierta y montículo de escombros. 3. Física adaptativa: Atravesable
 * cuando está abierta; sólida e intransitable al colapsar. 4. Advertencia
 * previa: Despliega diálogo con estimación de horas restantes antes de cruzar.
 * </p>
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class EntradaCueva extends Objeto implements Interactuable {

	private static final long serialVersionUID = 1L;

	private final String nombreMundoDestino;
	private final String nombreSpawnDestino;
	private final PuertaMundo puerta;

	private double duracionHoras = 72.0; // 3 días canónicos por defecto
	private double timestampColapso = -1.0; // -1 = No inicializado
	private boolean colapsada = false;

	// Spritesheet y cuadros
	private final ClaveHoja claveHoja;
	private final int spriteIndexAbierta;
	private final int spriteIndexColapsada;

	public EntradaCueva(final int x, final int y, final String mundoDestino, final String spawnDestino,
			final double duracionHoras, final ClaveHoja hoja, final int spriteAbierta, final int spriteColapsada) {
		super(x, y);
		this.nombreMundoDestino = (mundoDestino != null) ? mundoDestino : "cueva_1";
		this.nombreSpawnDestino = (spawnDestino != null) ? spawnDestino : "Comienzo";
		this.puerta = new PuertaMundo(this.nombreMundoDestino, this.nombreSpawnDestino);
		this.duracionHoras = Math.max(1.0, duracionHoras);
		this.claveHoja = (hoja != null) ? hoja : ClaveHoja.DUNGEON_16;
		this.spriteIndexAbierta = spriteAbierta;
		this.spriteIndexColapsada = spriteColapsada;
	}

	public EntradaCueva(final int x, final int y, final String mundoDestino, final String spawnDestino) {
		this(x, y, mundoDestino, spawnDestino, 72.0, ClaveHoja.DUNGEON_16, 50, 813);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.evaluarCaducidadLazy();
	}

	/**
	 * Evaluación perezosa en O(1) contra el SSOT astronómico.
	 */
	public void evaluarCaducidadLazy() {
		if (this.colapsada || (Globales.GESTOR_ASTRONOMICO == null)) {
			return;
		}

		// Inicialización perezosa al descubrir la entrada
		if (this.timestampColapso < 0.0) {
			this.timestampColapso = Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego() + this.duracionHoras;
		}

		// Comprobar si el tiempo astronómico ya rebasó la vida útil
		if (Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego() >= this.timestampColapso) {
			this.colapsarEntrada();
		}
	}

	public void colapsarEntrada() {
		if (!this.colapsada) {
			this.colapsada = true;
			if (this.mundo != null) {
				this.mundo.notificarModificacionEstructura();
			}
		}
	}

	public double getHorasRestantes() {
		this.evaluarCaducidadLazy();
		if (this.colapsada || (Globales.GESTOR_ASTRONOMICO == null)) {
			return 0.0;
		}
		return Math.max(0.0, this.timestampColapso - Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego());
	}

	// =========================================================================
	// INTERACCIÓN CONTEXTUAL [E]
	// =========================================================================

	@Override
	public String getTextoPrompt() {
		this.evaluarCaducidadLazy();
		return this.colapsada ? "Examinar Derrumbe" : "Explorar Cueva";
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		this.evaluarCaducidadLazy();

		// Caso 1: Entrada totalmente sellada
		if (this.colapsada) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			final MensajeDialogo msg = new MensajeDialogo("Entrada Bloqueada", new Color(180, 50, 50),
					"La cueva ha colapsado bajo miles de toneladas de roca. La falla geológica está sellada para siempre.",
					null);
			Globales.GESTOR_DIALOGOS.iniciarDialogo(msg);
			return;
		}

		// Caso 2: Intento de reingreso durante derrumbe activo
		if (Globales.GESTOR_DERRUMBES.isSecuenciaEscapeActiva()) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			final MensajeDialogo msg = new MensajeDialogo("Peligro Mortal", new Color(220, 40, 40),
					"¡El interior se está cayendo a pedazos! Es un suicidio volver a entrar.", null);
			Globales.GESTOR_DIALOGOS.iniciarDialogo(msg);
			return;
		}

		// Caso 3: Cueva abierta - Diálogo de advertencia
		final double horas = this.getHorasRestantes();
		final String horasTexto = String.valueOf((long) (horas * 10.0) / 10.0);

		final String texto = "Sientes vibraciones sordas en la roca. Estimas que la cueva colapsará en aprox. "
				+ horasTexto + " horas canónicas.\n¿Deseas adentrarte en las profundidades?";

		final MensajeDialogo dialogo = new MensajeDialogo("Falla Geológica Inestable", new Color(220, 180, 50), texto,
				null);

		dialogo.agregarOpcion("Adentrarse en la oscuridad", new Runnable() {
			@Override
			public void run() {
				if (Globales.GESTOR_DERRUMBES != null) {
					Globales.GESTOR_DERRUMBES.armarCueva(EntradaCueva.this.timestampColapso,
							EntradaCueva.this.duracionHoras, EntradaCueva.this);
				}
				EntradaCueva.this.puerta.teletransportar(jugador);
			}
		});

		dialogo.agregarOpcion("Alejarse por ahora", null);

		Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogo);
	}

	// =========================================================================
	// RENDERIZADO Y PROPIEDADES FÍSICAS
	// =========================================================================

	@Override
	public BufferedImage getTextura() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(this.claveHoja);
		if (hoja != null) {
			final int idx = this.colapsada ? this.spriteIndexColapsada : this.spriteIndexAbierta;
			return hoja.getSprite(idx);
		}
		return Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(), this.getPosicionYInt());

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			final Color c = this.colapsada ? Color.RED : Color.CYAN;
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), c);
		}
	}

	@Override
	public Rectangle getArea() {
		// Huella de colisión en la base (16x16 centrada)
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt() + 8, this.getPosicionYInt() + 14, 16, 16);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public boolean esSolido() {
		// Si colapsó bloquea el paso físico como un muro; abierta permite caminar sobre
		// ella
		return this.colapsada;
	}

	@Override
	public int getAncho() {
		return 32;
	}

	@Override
	public int getAlto() {
		return 32;
	}

	@Override
	public Objeto copiar() {
		final EntradaCueva copia = new EntradaCueva(this.getPosicionXInt(), this.getPosicionYInt(),
				this.nombreMundoDestino, this.nombreSpawnDestino, this.duracionHoras, this.claveHoja,
				this.spriteIndexAbierta, this.spriteIndexColapsada);
		copia.timestampColapso = this.timestampColapso;
		copia.colapsada = this.colapsada;
		return copia;
	}

	public boolean isColapsada() {
		return this.colapsada;
	}

	public String getNombreMundoDestino() {
		return this.nombreMundoDestino;
	}

	// =========================================================================
	// PERSISTENCIA JSON
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("mundoDestino", this.nombreMundoDestino);
		json.put("spawnDestino", this.nombreSpawnDestino);
		json.put("duracionHoras", Double.valueOf(this.duracionHoras));
		json.put("timestampColapso", Double.valueOf(this.timestampColapso));
		json.put("colapsada", Boolean.valueOf(this.colapsada));
		json.put("hoja", this.claveHoja.name());
		json.put("idxAbierta", Integer.valueOf(this.spriteIndexAbierta));
		json.put("idxColapsada", Integer.valueOf(this.spriteIndexColapsada));
		return json;
	}

	public static EntradaCueva crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new EntradaCueva(0, 0, "cueva_1", "Comienzo");
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String mundo = (json.get("mundoDestino") != null) ? json.get("mundoDestino").toString() : "cueva_1";
		final String spawn = (json.get("spawnDestino") != null) ? json.get("spawnDestino").toString() : "Comienzo";
		final double duracion = (json.get("duracionHoras") != null) ? ((Number) json.get("duracionHoras")).doubleValue()
				: 72.0;

		ClaveHoja hoja = ClaveHoja.DUNGEON_16;
		if (json.get("hoja") != null) {
			try {
				hoja = ClaveHoja.valueOf(json.get("hoja").toString());
			} catch (final Exception ignored) {
			}
		}

		final int idxAbierta = (json.get("idxAbierta") != null) ? ((Number) json.get("idxAbierta")).intValue() : 50;
		final int idxColapsada = (json.get("idxColapsada") != null) ? ((Number) json.get("idxColapsada")).intValue()
				: 813;

		final EntradaCueva cueva = new EntradaCueva(x, y, mundo, spawn, duracion, hoja, idxAbierta, idxColapsada);

		if (json.get("timestampColapso") != null) {
			cueva.timestampColapso = ((Number) json.get("timestampColapso")).doubleValue();
		}
		if (json.get("colapsada") != null) {
			cueva.colapsada = Boolean.parseBoolean(json.get("colapsada").toString());
		}

		return cueva;
	}

	public double getTimestampColapso() {
		return this.timestampColapso;
	}
}