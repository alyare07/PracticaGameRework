package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.controles.Raton;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.mapa.mapas.ManifiestoMapa;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario.TipoAmbiente;
import principal.maquinaestado.estados.editor.metadatos.TipoIluminacionInterior;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.persistencia.json.LectorJSON;
import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.IDMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class VentanaModalMundo extends ComponenteMenu {

	private static final int ANCHO_MODAL = 370;
	private static final int ALTO_MODAL = 275;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 250);
	private static final Color COLOR_BORDE = new Color(220, 180, 50);

	private final EditorMapa editor;
	private MetadatosEscenario metadatos;
	private boolean abierta = false;

	// Control de pestañas: 0 = PROYECTO / SUBMUNDOS, 1 = ATMOSFERA
	private int pestanaActiva = 0;
	private final Rectangle tabProyecto = new Rectangle();
	private final Rectangle tabAtmosfera = new Rectangle();

	// Pestaña Proyecto
	private CajaTextoPixel ctNombreVisibleProyecto;
	private final List<String> listaSubmundos = new ArrayList<String>();
	private int idxSubmundo = 0;
	private final Rectangle areaBtnSubmundo = new Rectangle();
	private BotonPixel btnCargarSubmundo;
	private BotonPixel btnEliminarSubmundo;
	private BotonPixel btnImportarSubmundo;

	// Creación de nuevo submundo
	private CajaTextoPixel ctNuevoSubNombre;
	private CajaTextoPixel ctNuevoSubAncho;
	private CajaTextoPixel ctNuevoSubAlto;
	private int idxNuevoAmbiente = 1;
	private final Rectangle areaBtnNuevoAmbiente = new Rectangle();
	private int idxNuevoSuelo = 0;
	private final Rectangle areaBtnNuevoSuelo = new Rectangle();
	private BotonPixel btnConfirmarCreacion;

	// Pestaña Atmósfera
	private CajaTextoPixel ctNombreVisibleSubmundo;
	private int idxMusica = 0;
	private int idxBioma = 0;
	private int idxClima = 0;
	private int idxAmbiente = 0;
	private int idxIluminacion = 0;

	private final Rectangle areaBtnMusica = new Rectangle();
	private final Rectangle areaBtnBioma = new Rectangle();
	private final Rectangle areaBtnClima = new Rectangle();
	private final Rectangle areaBtnAmbiente = new Rectangle();
	private final Rectangle areaBtnIluminacion = new Rectangle();

	private BotonPixel btnAceptar;
	private BotonPixel btnCerrar;

	public VentanaModalMundo(final EditorMapa editor) {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.editor = editor;
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		// Pestañas
		this.tabProyecto.setBounds(x + 10, y + 26, 170, 16);
		this.tabAtmosfera.setBounds(x + 190, y + 26, 170, 16);

		// Pestaña Proyecto
		this.ctNombreVisibleProyecto = new CajaTextoPixel(new Rectangle(x + 140, y + 48, 210, 16), "Valle Verde", 32,
				false);
		this.areaBtnSubmundo.setBounds(x + 140, y + 70, 210, 18);

		this.btnCargarSubmundo = new BotonPixel("Saltar", new Rectangle(x + 140, y + 92, 65, 18), () -> {
			this.saltarAlSubmundoSeleccionado();
		});

		this.btnImportarSubmundo = new BotonPixel("Importar", new Rectangle(x + 210, y + 92, 70, 18), () -> {
			this.importarSubmundoExterno();
		});

		this.btnEliminarSubmundo = new BotonPixel("Eliminar", new Rectangle(x + 285, y + 92, 65, 18), () -> {
			this.confirmarYEliminarSubmundo();
		});

		// Creación nuevo submundo
		this.ctNuevoSubNombre = new CajaTextoPixel(new Rectangle(x + 140, y + 130, 130, 16), "casa_1", 16, false, true);
		this.ctNuevoSubNombre.setPermitirEspacios(false);
		this.ctNuevoSubAncho = new CajaTextoPixel(new Rectangle(x + 140, y + 152, 45, 16), "25", 4, true);
		this.ctNuevoSubAlto = new CajaTextoPixel(new Rectangle(x + 220, y + 152, 45, 16), "25", 4, true);
		this.areaBtnNuevoAmbiente.setBounds(x + 140, y + 174, 140, 18);
		this.areaBtnNuevoSuelo.setBounds(x + 140, y + 196, 170, 18);

		this.btnConfirmarCreacion = new BotonPixel("+ Crear Submundo", new Rectangle(x + 140, y + 220, 170, 18), () -> {
			this.crearNuevoSubmundoEnProyecto();
		});

		// Pestaña Atmósfera
		this.ctNombreVisibleSubmundo = new CajaTextoPixel(new Rectangle(x + 140, y + 48, 210, 16), "Submundo", 32,
				false);
		this.areaBtnMusica.setBounds(x + 140, y + 72, 210, 18);
		this.areaBtnBioma.setBounds(x + 140, y + 98, 210, 18);
		this.areaBtnClima.setBounds(x + 140, y + 124, 210, 18);
		this.areaBtnAmbiente.setBounds(x + 140, y + 150, 210, 18);
		this.areaBtnIluminacion.setBounds(x + 140, y + 176, 210, 18);

		this.btnAceptar = new BotonPixel("Aplicar", new Rectangle(x + 40, (y + ALTO_MODAL) - 26, 110, 18), () -> {
			this.guardarCambiosAtmosfera();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cerrar", new Rectangle(x + 220, (y + ALTO_MODAL) - 26, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final MetadatosEscenario metadatosActuales) {
		this.metadatos = (metadatosActuales != null) ? metadatosActuales : new MetadatosEscenario();
		this.ctNombreVisibleSubmundo.setTexto(this.metadatos.getNombreVisible());

		if ((this.editor != null) && (this.editor.getManifiesto() != null)) {
			this.ctNombreVisibleProyecto.setTexto(this.editor.getManifiesto().getNombreVisible());
		}

		this.idxMusica = (this.metadatos.getMusicaFondo() != null) ? this.metadatos.getMusicaFondo().ordinal() : 0;
		this.idxBioma = (this.metadatos.getPerfilBioma() != null) ? this.metadatos.getPerfilBioma().ordinal() : 0;
		this.idxClima = (this.metadatos.getClimaInicial() != null) ? this.metadatos.getClimaInicial().ordinal() : 0;
		this.idxAmbiente = (this.metadatos.getTipoAmbiente() != null) ? this.metadatos.getTipoAmbiente().ordinal() : 0;
		this.idxIluminacion = (this.metadatos.getIluminacionInterior() != null)
				? this.metadatos.getIluminacionInterior().ordinal()
				: 0;

		this.poblarSubmundos();
		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	private void poblarSubmundos() {
		this.listaSubmundos.clear();
		if ((this.editor != null) && (this.editor.getManifiesto() != null)) {
			this.listaSubmundos.addAll(this.editor.getManifiesto().getListaIdsSubmundos());
		}
		if (this.listaSubmundos.isEmpty()) {
			this.listaSubmundos.add("exterior");
		}
		this.idxSubmundo = 0;
		if ((this.editor != null) && (this.editor.getIdSubmundoActivo() != null)) {
			for (int i = 0; i < this.listaSubmundos.size(); i++) {
				if (this.listaSubmundos.get(i).equalsIgnoreCase(this.editor.getIdSubmundoActivo())) {
					this.idxSubmundo = i;
					break;
				}
			}
		}
	}

	private void saltarAlSubmundoSeleccionado() {
		if (this.listaSubmundos.isEmpty()) {
			return;
		}
		final String subElegido = this.listaSubmundos.get(this.idxSubmundo);
		if (this.editor != null) {
			this.guardarNombreProyectoSiCambio();
			this.cerrar();
			this.editor.conmutarSubmundoEnCaliente(subElegido);
		}
	}

	private void guardarNombreProyectoSiCambio() {
		if ((this.editor == null) || (this.editor.getManifiesto() == null)
				|| (this.editor.getDirectorioProyecto() == null)) {
			return;
		}
		final String nuevoNom = this.ctNombreVisibleProyecto.getTexto().trim();
		if (nuevoNom.isEmpty()) {
			return;
		}

		final ManifiestoMapa man = this.editor.getManifiesto();
		if (!nuevoNom.equals(man.getNombreVisible())) {
			final File fMan = new File(this.editor.getDirectorioProyecto(), ManifiestoMapa.NOMBRE_MANIFIESTO);
			try {
				final JSONObject json = new JSONObject();
				json.put("idMapa", man.getIdMapa());
				json.put("nombreVisible", nuevoNom);
				json.put("mundoComienzo", man.getMundoComienzo());
				json.put("spawnComienzo", man.getSpawnComienzo());

				final JSONArray subArr = new JSONArray();
				for (final String subId : man.getListaIdsSubmundos()) {
					final ManifiestoMapa.EntradaSubmundo ent = man.getSubmundo(subId);
					final JSONObject jEntry = new JSONObject();
					jEntry.put("id", ent.getId());
					jEntry.put("archivo", ent.getArchivoRelativo());
					jEntry.put("tipoAmbiente", ent.getTipoAmbiente());
					jEntry.put("modoCarga", ent.getModoCarga().name());
					subArr.add(jEntry);
				}
				json.put("submundos", subArr);

				try (final BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(fMan), StandardCharsets.UTF_8))) {
					writer.write(json.toJSONString());
					writer.flush();
				}
			} catch (final Exception ignored) {
			}
		}
	}

	private void importarSubmundoExterno() {
		if ((this.editor == null) || (this.editor.getDirectorioProyecto() == null)) {
			return;
		}

		final File dirProyecto = this.editor.getDirectorioProyecto();
		final File dirSubmundos = new File(dirProyecto, "submundos");
		dirSubmundos.mkdirs();

		final JFileChooser selector = new JFileChooser(new File("mapas"));
		selector.setDialogTitle("Seleccionar Submundo (.wld o .json)");
		selector.setFileFilter(new FileNameExtensionFilter("Submundos (*.wld, *.json)", "wld", "json"));

		final int res = selector.showOpenDialog(null);
		if ((res != JFileChooser.APPROVE_OPTION) || (selector.getSelectedFile() == null)) {
			return;
		}

		final File archivoOrigen = selector.getSelectedFile();
		final String nombreSugerido = archivoOrigen.getName().replace(".wld", "").replace(".json", "").toLowerCase()
				.replaceAll("[^a-z0-9_]", "_");

		final String idNuevo = JOptionPane.showInputDialog(null, "Ingresa el ID del submundo en este proyecto:",
				nombreSugerido);
		if ((idNuevo == null) || idNuevo.trim().isEmpty()) {
			return;
		}
		final String idNorm = idNuevo.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");

		for (final String s : this.listaSubmundos) {
			if (s.equalsIgnoreCase(idNorm)) {
				JOptionPane.showMessageDialog(null,
						"Ya existe un submundo con el ID '" + idNorm + "' en este proyecto.", "ID Duplicado",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		final Object[] opcionesEstrategia = { "Copiar al Proyecto (Independiente)", "Vincular sin Copiar (Compartido)",
				"Cancelar" };
		final int eleccionEstrategia = JOptionPane.showOptionDialog(null,
				"¿Como deseas incorporar el submundo al proyecto?\n\n"
						+ "• 'Copiar al Proyecto': Duplica el archivo en 'submundos/'. Los cambios solo afectaran a este mapa.\n"
						+ "• 'Vincular sin Copiar': Conecta directamente al archivo original sin duplicarlo (compartido entre mapas).\n",
				"Estrategia de Importacion", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				opcionesEstrategia, opcionesEstrategia[0]);

		if ((eleccionEstrategia != 0) && (eleccionEstrategia != 1)) {
			return;
		}

		final boolean modoCopia = (eleccionEstrategia == 0);
		String archivoRelativoFinal;
		File archivoParaInspeccion;

		try {
			if (modoCopia) {
				final File archivoDestino = new File(dirSubmundos, idNorm + ".wld");
				Files.copy(archivoOrigen.toPath(), archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
				archivoRelativoFinal = "submundos/" + idNorm + ".wld";
				archivoParaInspeccion = archivoDestino;
			} else {
				try {
					archivoRelativoFinal = dirProyecto.toPath().relativize(archivoOrigen.toPath()).toString()
							.replace('\\', '/');
				} catch (final IllegalArgumentException e) {
					archivoRelativoFinal = archivoOrigen.getAbsolutePath().replace('\\', '/');
				}
				archivoParaInspeccion = archivoOrigen;
			}

			String amb = "INTERIOR";
			try {
				final StringBuilder sb = new StringBuilder();
				try (final BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(archivoParaInspeccion), StandardCharsets.UTF_8))) {
					String l;
					while ((l = reader.readLine()) != null) {
						sb.append(l);
					}
				}
				String texto = sb.toString().trim();
				if (!texto.startsWith("{")) {
					texto = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(texto);
				}
				final JSONObject json = (JSONObject) new JSONParser().parse(texto);
				final JSONObject meta = LectorJSON.getObjeto(json, "metadatos");
				if ((meta != null) && meta.containsKey("tipoAmbiente")) {
					amb = meta.get("tipoAmbiente").toString().toUpperCase();
				}
			} catch (final Exception ignored) {
			}

			final String modoCarga = "EXTERIOR".equalsIgnoreCase(amb) ? "PESADO" : "LIGERO";
			this.registrarSubmundoEnManifiestoDisco(dirProyecto, idNorm, archivoRelativoFinal, amb, modoCarga);

			final int saltar = JOptionPane.showConfirmDialog(null,
					"¡Submundo importado con exito (" + (modoCopia ? "Copia" : "Compartido")
							+ ")!\n¿Deseas abrirlo ahora en el editor?",
					"Submundo Vinculado", JOptionPane.YES_NO_OPTION);
			this.cerrar();
			if (saltar == JOptionPane.YES_OPTION) {
				this.editor.conmutarSubmundoEnCaliente(idNorm);
			} else {
				this.editor.conmutarSubmundoEnCaliente(this.editor.getIdSubmundoActivo());
			}

		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error al importar el archivo: " + e.getMessage(), "Error I/O",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("unchecked")
	private void confirmarYEliminarSubmundo() {
		if (this.listaSubmundos.isEmpty() || (this.editor == null) || (this.editor.getManifiesto() == null)
				|| (this.editor.getDirectorioProyecto() == null)) {
			return;
		}

		final String subAEliminar = this.listaSubmundos.get(this.idxSubmundo);
		final Object[] opciones = { "Eliminar Archivo Fisico", "Solo Desvincular", "Cancelar" };

		final int seleccion = JOptionPane.showOptionDialog(null,
				"¿Que deseas hacer con el submundo '" + subAEliminar + "'?\n\n"
						+ "• 'Eliminar Archivo Fisico': Lo desvincula de mapa.mp y borra el archivo .wld del disco.\n"
						+ "• 'Solo Desvincular': Quita el submundo del proyecto pero conserva el archivo .wld.\n",
				"Confirmar Eliminacion de Submundo", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
				null, opciones, opciones[2]);

		if ((seleccion != 0) && (seleccion != 1)) {
			return;
		}

		final boolean borrarFisico = (seleccion == 0);
		final File dirProyecto = this.editor.getDirectorioProyecto();
		final ManifiestoMapa man = this.editor.getManifiesto();

		if (borrarFisico) {
			final File archivoAEliminar = man.resolverArchivoSubmundo(dirProyecto, subAEliminar);
			if ((archivoAEliminar != null) && archivoAEliminar.exists()) {
				archivoAEliminar.delete();
			}
		}

		final List<String> restantes = new ArrayList<String>();
		final JSONObject jsonNuevoMp = new JSONObject();
		jsonNuevoMp.put("idMapa", man.getIdMapa());
		jsonNuevoMp.put("nombreVisible", man.getNombreVisible());

		final JSONArray subArr = new JSONArray();
		for (final String subId : man.getListaIdsSubmundos()) {
			if (subId.equalsIgnoreCase(subAEliminar)) {
				continue;
			}
			restantes.add(subId);
			final ManifiestoMapa.EntradaSubmundo ent = man.getSubmundo(subId);
			final JSONObject jEntry = new JSONObject();
			jEntry.put("id", ent.getId());
			jEntry.put("archivo", ent.getArchivoRelativo());
			jEntry.put("tipoAmbiente", ent.getTipoAmbiente());
			jEntry.put("modoCarga", ent.getModoCarga().name());
			subArr.add(jEntry);
		}
		jsonNuevoMp.put("submundos", subArr);

		String nuevoMundoComienzo = man.getMundoComienzo();
		if (nuevoMundoComienzo.equalsIgnoreCase(subAEliminar)) {
			nuevoMundoComienzo = !restantes.isEmpty() ? restantes.get(0) : "exterior";
		}
		jsonNuevoMp.put("mundoComienzo", nuevoMundoComienzo);
		jsonNuevoMp.put("spawnComienzo", man.getSpawnComienzo());

		final File fMan = new File(dirProyecto, ManifiestoMapa.NOMBRE_MANIFIESTO);
		try (final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(fMan), StandardCharsets.UTF_8))) {
			writer.write(jsonNuevoMp.toJSONString());
			writer.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		this.cerrar();

		if (restantes.isEmpty()) {
			final int delCarpeta = JOptionPane.showConfirmDialog(null, "Era el ultimo submundo del proyecto '"
					+ man.getNombreVisible() + "'.\n"
					+ "¿Deseas eliminar tambien la carpeta completa del proyecto del disco para no dejar basura?",
					"Eliminar Proyecto Vacio", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (delCarpeta == JOptionPane.YES_OPTION) {
				this.borrarDirectorioRecursivo(dirProyecto);
			}

			this.editor.redirigirANuevoProyecto();
			return;
		}

		final boolean estabaAbierto = subAEliminar.equalsIgnoreCase(this.editor.getIdSubmundoActivo());
		if (estabaAbierto) {
			this.editor.conmutarSubmundoSinGuardar(nuevoMundoComienzo);
		} else {
			this.editor.conmutarSubmundoEnCaliente(this.editor.getIdSubmundoActivo());
		}
	}

	private void borrarDirectorioRecursivo(final File archivoODirectorio) {
		if (archivoODirectorio.isDirectory()) {
			final File[] hijos = archivoODirectorio.listFiles();
			if (hijos != null) {
				for (final File hijo : hijos) {
					this.borrarDirectorioRecursivo(hijo);
				}
			}
		}
		archivoODirectorio.delete();
	}

	@SuppressWarnings("unchecked")
	private void crearNuevoSubmundoEnProyecto() {
		if ((this.editor == null) || (this.editor.getManifiesto() == null)
				|| (this.editor.getDirectorioProyecto() == null)) {
			return;
		}

		final String idNuevo = this.ctNuevoSubNombre.getTexto().trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
		if (idNuevo.isEmpty()) {
			return;
		}

		for (final String s : this.listaSubmundos) {
			if (s.equalsIgnoreCase(idNuevo)) {
				JOptionPane.showMessageDialog(null, "Ya existe un submundo con el ID: " + idNuevo, "Submundo Existente",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		final int ancho = Math.max(15, Math.min(2000, this.ctNuevoSubAncho.getNumeroEntero(25)));
		final int alto = Math.max(15, Math.min(2000, this.ctNuevoSubAlto.getNumeroEntero(25)));
		final TipoAmbiente amb = (this.idxNuevoAmbiente == 0) ? TipoAmbiente.EXTERIOR
				: ((this.idxNuevoAmbiente == 1) ? TipoAmbiente.INTERIOR : TipoAmbiente.CUEVA);
		final TipoTerreno suelo = TipoTerreno.values()[this.idxNuevoSuelo];

		final File dirProyecto = this.editor.getDirectorioProyecto();
		final File dirSubmundos = new File(dirProyecto, "submundos");
		dirSubmundos.mkdirs();

		final File archivoNuevoWld = new File(dirSubmundos, idNuevo + ".wld");

		final Terreno terreno = new Terreno(ancho, alto, Constantes.LADO_TILE, suelo);
		final JSONArray arrSpawns = new JSONArray();
		final int sx = (ancho / 2) * Constantes.LADO_TILE;
		final int sy = (alto / 2) * Constantes.LADO_TILE;
		arrSpawns.add(new Spawn(sx, sy, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO).exportarParaJSON());

		final MetadatosEscenario meta = new MetadatosEscenario();
		meta.setNombreVisible(idNuevo);
		meta.setTipoAmbiente(amb);
		if (amb == TipoAmbiente.INTERIOR) {
			meta.setIluminacionInterior(TipoIluminacionInterior.CLARA);
		}

		final Escenario esc = new Escenario(terreno, null, null, null, null, arrSpawns, null, null, null, meta);
		EscenarioLoader.exportarEscenario(esc, archivoNuevoWld);

		this.registrarSubmundoEnManifiestoDisco(dirProyecto, idNuevo, "submundos/" + idNuevo + ".wld", amb.name(),
				amb == TipoAmbiente.EXTERIOR ? "PESADO" : "LIGERO");

		this.cerrar();
		this.editor.conmutarSubmundoEnCaliente(idNuevo);
	}

	@SuppressWarnings("unchecked")
	private void registrarSubmundoEnManifiestoDisco(final File dirProyecto, final String id, final String archivoRel,
			final String ambiente, final String modoCarga) {
		final File fMan = new File(dirProyecto, ManifiestoMapa.NOMBRE_MANIFIESTO);
		try {
			final ManifiestoMapa man = ManifiestoMapa.cargarDesdeDirectorio(dirProyecto);
			final JSONObject json = new JSONObject();
			json.put("idMapa", man.getIdMapa());
			json.put("nombreVisible", man.getNombreVisible());
			json.put("mundoComienzo", man.getMundoComienzo());
			json.put("spawnComienzo", man.getSpawnComienzo());

			final JSONArray subArr = new JSONArray();
			for (final String subId : man.getListaIdsSubmundos()) {
				final ManifiestoMapa.EntradaSubmundo ent = man.getSubmundo(subId);
				final JSONObject jEntry = new JSONObject();
				jEntry.put("id", ent.getId());
				jEntry.put("archivo", ent.getArchivoRelativo());
				jEntry.put("tipoAmbiente", ent.getTipoAmbiente());
				jEntry.put("modoCarga", ent.getModoCarga().name());
				subArr.add(jEntry);
			}

			final JSONObject jNueva = new JSONObject();
			jNueva.put("id", id);
			jNueva.put("archivo", archivoRel);
			jNueva.put("tipoAmbiente", ambiente);
			jNueva.put("modoCarga", modoCarga);
			subArr.add(jNueva);

			json.put("submundos", subArr);

			try (final BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fMan), StandardCharsets.UTF_8))) {
				writer.write(json.toJSONString());
				writer.flush();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
	}

	private void guardarCambiosAtmosfera() {
		if (this.metadatos != null) {
			this.metadatos.setNombreVisible(this.ctNombreVisibleSubmundo.getTexto().trim());
			this.metadatos.setMusicaFondo(IDMusica.values()[this.idxMusica]);
			this.metadatos.setPerfilBioma(PerfilClima.values()[this.idxBioma]);
			this.metadatos.setClimaInicial(TipoClima.values()[this.idxClima]);
			this.metadatos.setTipoAmbiente(TipoAmbiente.values()[this.idxAmbiente]);
			this.metadatos.setIluminacionInterior(TipoIluminacionInterior.values()[this.idxIluminacion]);

			this.guardarNombreProyectoSiCambio();

			if (Globales.GESTOR_CLIMA != null) {
				Globales.GESTOR_CLIMA.setPerfilBioma(this.metadatos.getPerfilBioma());
				Globales.GESTOR_CLIMA.setClima(this.metadatos.getClimaInicial(), 0.0);
			}

			if (Globales.GESTOR_LUZ != null) {
				if (this.metadatos.esCueva()) {
					Globales.GESTOR_LUZ.establecerModoCueva(true);
				} else if (this.metadatos.esInterior()) {
					Globales.GESTOR_LUZ.establecerAmbienteTransicion(this.metadatos.resolverColorLuzEfectivo(), 0.0);
				} else {
					Globales.GESTOR_LUZ.restablecerModoExterior();
				}
			}
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();

			if (this.tabProyecto.contains(p)) {
				this.pestanaActiva = 0;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}
			if (this.tabAtmosfera.contains(p)) {
				this.pestanaActiva = 1;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}

			if (this.pestanaActiva == 0) {
				if (this.areaBtnSubmundo.contains(p) && !this.listaSubmundos.isEmpty()) {
					this.idxSubmundo = (this.idxSubmundo + 1) % this.listaSubmundos.size();
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				} else if (this.areaBtnNuevoAmbiente.contains(p)) {
					this.idxNuevoAmbiente = (this.idxNuevoAmbiente + 1) % 3;
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				} else if (this.areaBtnNuevoSuelo.contains(p)) {
					this.idxNuevoSuelo = (this.idxNuevoSuelo + 1) % TipoTerreno.values().length;
					GestorSonido.reproducir(IDSonido.SELECT_MENU);
				}
			} else if (this.areaBtnMusica.contains(p)) {
				this.idxMusica = (this.idxMusica + 1) % IDMusica.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnBioma.contains(p)) {
				this.idxBioma = (this.idxBioma + 1) % PerfilClima.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnClima.contains(p)) {
				this.idxClima = (this.idxClima + 1) % TipoClima.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnAmbiente.contains(p)) {
				this.idxAmbiente = (this.idxAmbiente + 1) % TipoAmbiente.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnIluminacion.contains(p)
					&& (TipoAmbiente.values()[this.idxAmbiente] == TipoAmbiente.INTERIOR)) {
				this.idxIluminacion = (this.idxIluminacion + 1) % TipoIluminacionInterior.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		if (this.pestanaActiva == 0) {
			this.ctNombreVisibleProyecto.actualizar(raton);
			this.btnCargarSubmundo.actualizar(raton);
			this.btnImportarSubmundo.actualizar(raton);
			this.btnEliminarSubmundo.actualizar(raton);
			this.ctNuevoSubNombre.actualizar(raton);
			this.ctNuevoSubAncho.actualizar(raton);
			this.ctNuevoSubAlto.actualizar(raton);
			this.btnConfirmarCreacion.actualizar(raton);
		} else {
			this.ctNombreVisibleSubmundo.actualizar(raton);
			this.btnAceptar.actualizar(raton);
		}

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

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
				new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		this.pintarPestana(g, this.tabProyecto, "PROYECTO & SUBMUNDOS", this.pestanaActiva == 0);
		this.pintarPestana(g, this.tabAtmosfera, "ATMOSFERA & NOMBRE", this.pestanaActiva == 1);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		if (this.pestanaActiva == 0) {
			Render2D.dibujarStringConSombra(g, "Nombre Region:", x + 16, y + 60, Color.WHITE, Color.BLACK);
			this.ctNombreVisibleProyecto.pintar(g);

			Render2D.dibujarStringConSombra(g, "Submundo Activo:", x + 16, y + 84, Color.WHITE, Color.BLACK);
			final String subAct = !this.listaSubmundos.isEmpty() ? this.listaSubmundos.get(this.idxSubmundo)
					: "exterior";
			this.pintarBotonSelector(g, this.areaBtnSubmundo, "< " + subAct + " >", new Color(100, 240, 120));

			this.btnCargarSubmundo.pintar(g);
			this.btnImportarSubmundo.pintar(g);
			this.btnEliminarSubmundo.pintar(g);

			// Crear Submundo
			Render2D.dibujarLinea(g, x + 16, y + 116, (x + w) - 16, y + 116, new Color(60, 65, 80));
			Render2D.dibujarStringConSombra(g, "--- NUEVO SUBMUNDO ---", x + 120, y + 114, new Color(220, 180, 50),
					Color.BLACK);

			Render2D.dibujarStringConSombra(g, "ID / Archivo:", x + 16, y + 142, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Tamano (W x H):", x + 16, y + 164, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Ambiente:", x + 16, y + 187, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Suelo Inicial:", x + 16, y + 209, Color.WHITE, Color.BLACK);

			this.ctNuevoSubNombre.pintar(g);
			this.ctNuevoSubAncho.pintar(g);
			Render2D.dibujarStringConSombra(g, "x", x + 195, y + 164, Color.WHITE, Color.BLACK);
			this.ctNuevoSubAlto.pintar(g);

			final String[] ambNombres = { "EXTERIOR", "INTERIOR", "CUEVA" };
			this.pintarBotonSelector(g, this.areaBtnNuevoAmbiente, "< " + ambNombres[this.idxNuevoAmbiente] + " >",
					new Color(255, 200, 60));

			this.pintarSelectorSuelo(g, this.areaBtnNuevoSuelo);
			this.btnConfirmarCreacion.pintar(g);

		} else {
			Render2D.dibujarStringConSombra(g, "Nombre Visible:", x + 16, y + 60, Color.WHITE, Color.BLACK);
			this.ctNombreVisibleSubmundo.pintar(g);

			Render2D.dibujarStringConSombra(g, "Musica de Fondo:", x + 16, y + 86, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Bioma / Clima Base:", x + 16, y + 112, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Clima Inicial:", x + 16, y + 138, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Tipo de Ambiente:", x + 16, y + 164, Color.WHITE, Color.BLACK);

			final boolean esInterior = (TipoAmbiente.values()[this.idxAmbiente] == TipoAmbiente.INTERIOR);
			Render2D.dibujarStringConSombra(g, "Luz de Interior:", x + 16, y + 190,
					esInterior ? Color.WHITE : Color.GRAY, Color.BLACK);

			this.pintarBotonSelector(g, this.areaBtnMusica, IDMusica.values()[this.idxMusica].name(),
					new Color(220, 180, 50));
			this.pintarBotonSelector(g, this.areaBtnBioma, PerfilClima.values()[this.idxBioma].getNombreVisible(),
					new Color(220, 180, 50));
			this.pintarBotonSelector(g, this.areaBtnClima, TipoClima.values()[this.idxClima].getNombre(),
					new Color(220, 180, 50));
			this.pintarBotonSelector(g, this.areaBtnAmbiente, TipoAmbiente.values()[this.idxAmbiente].name(),
					new Color(100, 240, 120));

			if (esInterior) {
				this.pintarBotonSelector(g, this.areaBtnIluminacion,
						TipoIluminacionInterior.values()[this.idxIluminacion].getNombreVisible(),
						new Color(255, 215, 140));
			} else {
				this.pintarBotonSelector(g, this.areaBtnIluminacion, "[N/A - Solo Interiores]", Color.GRAY);
			}

			this.btnAceptar.pintar(g);
		}

		this.btnCerrar.pintar(g);
		g.setFont(fontPrevia);
	}

	private void pintarSelectorSuelo(final Graphics2D g, final Rectangle r) {
		Render2D.dibujarRectanguloRelleno(g, r, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, r, new Color(220, 180, 50));

		final TipoTerreno tipo = TipoTerreno.values()[this.idxNuevoSuelo];
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(tipo);

		if ((set != null) && (set.getSpriteBase() != null)) {
			final BufferedImage sprite = set.getSpriteBase();
			Render2D.dibujarImagen(g, sprite, r.x + 2, r.y + 1);
		}

		final String texto = tipo.getNombre();
		Render2D.dibujarStringConSombra(g, texto, r.x + 22, r.y + 13, Color.WHITE, Color.BLACK);
	}

	private void pintarPestana(final Graphics2D g, final Rectangle r, final String texto, final boolean seleccionada) {
		Render2D.dibujarRectanguloRelleno(g, r, seleccionada ? new Color(220, 180, 50) : new Color(30, 35, 45));
		Render2D.dibujarRectanguloContorno(g, r, Color.BLACK);
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		Render2D.dibujarStringConSombra(g, texto, r.x + ((r.width - ancho) / 2), r.y + 12,
				seleccionada ? Color.BLACK : Color.GRAY, seleccionada ? Color.WHITE : Color.BLACK);
	}

	private void pintarBotonSelector(final Graphics2D g, final Rectangle r, final String valor,
			final Color colorTexto) {
		Render2D.dibujarRectanguloRelleno(g, r, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, r, new Color(75, 80, 95));
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, valor);
		Render2D.dibujarStringConSombra(g, valor, r.x + ((r.width - ancho) / 2), r.y + 13, colorTexto, Color.BLACK);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}