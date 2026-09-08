package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.mapa.Mundo;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal interactivo para configurar y renombrar puntos de Spawn.
 * Permite asignar nombres personalizados o convertir el spawn en "Comienzo"
 * garantizando la coherencia del mapa de spawns (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class VentanaModalSpawn extends ComponenteMenu {

	private static final int ANCHO_MODAL = 320;
	private static final int ALTO_MODAL = 160;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(255, 215, 0); // Oro
	private static final Color COLOR_BORDE_SOMBRA = new Color(8, 10, 14);

	private final Mundo mundo;
	private Spawn spawnSeleccionado;
	private boolean abierta = false;

	private CajaTextoPixel ctNombre;
	private BotonPixel btnHacerComienzo;
	private BotonPixel btnGuardar;
	private BotonPixel btnCerrar;

	public VentanaModalSpawn(final Mundo mundo) {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.mundo = mundo;
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.ctNombre = new CajaTextoPixel(new Rectangle(x + 110, y + 42, 190, 16), "Comienzo", 24, false);

		this.btnHacerComienzo = new BotonPixel("Marcar como 'Comienzo'", new Rectangle(x + 20, y + 72, 280, 16), () -> {
			this.ctNombre.setTexto(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		});

		this.btnGuardar = new BotonPixel("Guardar", new Rectangle(x + 30, y + ALTO_MODAL - 28, 120, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cancelar", new Rectangle(x + 170, y + ALTO_MODAL - 28, 120, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final Spawn spawn) {
		if (spawn == null) {
			return;
		}
		this.spawnSeleccionado = spawn;
		this.ctNombre.setTexto(spawn.getNombre());
		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
		this.spawnSeleccionado = null;
	}

	private void guardarCambios() {
		if ((this.spawnSeleccionado == null) || (this.mundo == null)) {
			return;
		}

		final String nuevoNombre = this.ctNombre.getTexto().trim();
		if (nuevoNombre.isEmpty()) {
			return;
		}

		final String nombreAnterior = this.spawnSeleccionado.getNombre();
		if (!nuevoNombre.equalsIgnoreCase(nombreAnterior)) {
			// Reemplazo atómico en la tabla hash de Mundo para mantener clave y valor sincronizados
			this.mundo.eliminarSpawn(nombreAnterior);
			this.spawnSeleccionado.setNombre(nuevoNombre);
			this.mundo.agregarSpawn(this.spawnSeleccionado);
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}

		this.ctNombre.actualizar(raton);
		this.btnHacerComienzo.actualizar(raton);
		this.btnGuardar.actualizar(raton);
		this.btnCerrar.actualizar(raton);
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.abierta) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Fondo sombreado y marco ornamental
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_BORDE_SOMBRA);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		// 2. Cabecera en m5x7 (16f)
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DE PUNTO DE SPAWN";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 20, new Color(255, 220, 80), Color.BLACK);

		// 3. Coordenadas y Etiquetas
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, "Nombre:", x + 16, y + 54, Color.WHITE, Color.BLACK);

		if (this.spawnSeleccionado != null) {
			final String posTxt = "Posicion: (" + this.spawnSeleccionado.getX() + ", " + this.spawnSeleccionado.getY() + ")";
			Render2D.dibujarStringConSombra(g, posTxt, x + 16, y + 104, Color.LIGHT_GRAY, Color.BLACK);
		}

		// 4. Componentes interactivos
		this.ctNombre.pintar(g);
		this.btnHacerComienzo.pintar(g);
		this.btnGuardar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}