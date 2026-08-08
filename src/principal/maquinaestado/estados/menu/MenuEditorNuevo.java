package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.Boton;
import principal.maquinaestado.estados.menu.herramientas.CajaTexto;
import principal.maquinaestado.estados.menu.herramientas.Label;
import principal.maquinaestado.estados.menu.herramientas.MuestraTile;

public class MenuEditorNuevo extends Menu {
	protected final int COMIENZO_LABELS_X = 50;
	protected final int COMIENZO_CAJA_TEXTOS_X = 350;
	protected CajaTexto ctAncho;
	protected CajaTexto ctAlto;
	protected CajaTexto ctIdModelo;
	protected Boton botonCrear;
	protected int auxAncho;
	protected int auxAlto;
	protected int auxIDTile;
	protected  MuestraTile muestraTile;

	public MenuEditorNuevo(GestorEstados ge) {
		super(ge);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.auxAncho = ctAncho.getNumeroParseado();
		this.auxAlto = ctAlto.getNumeroParseado();
		this.auxIDTile = ctIdModelo.getNumeroParseado();
		if (this.auxAncho > 0 && (this.auxAncho % 2 == 0) && this.auxAlto > 0 && (this.auxAlto % 2 == 0) && this.auxIDTile >= 1
				&& this.auxIDTile <= ListaModeloTile.getUltimoIdUsado()) {
			if (!botonCrear.visible()) {
				botonCrear.visible(true);
			}
		} else if (botonCrear.visible()) {
			botonCrear.visible(false);
		}
		this.muestraTile.cambiarIdTile(auxIDTile);
	}

	@Override
	protected void inicializarBotones() {

		final Label lAncho = new Label("Cantidad tiles a lo ancho (par)", this.COMIENZO_LABELS_X, 95, Color.white, 11f);
		ctAncho = new CajaTexto(new Rectangle(this.COMIENZO_CAJA_TEXTOS_X, 80, 40, 15), Color.white, Color.gray, Color.black);
		ctAncho.establecerSoloNumerico();
		ctAncho.establecerLimiteCaracteres(6);
		ctAncho.establecerTexto("50");

		final Label lAlto = new Label("Cantidad tiles a lo alto (par)", this.COMIENZO_LABELS_X, 125, Color.white, 11f);
		ctAlto = new CajaTexto(new Rectangle(this.COMIENZO_CAJA_TEXTOS_X, 110, 40, 15), Color.white, Color.gray, Color.black);
		ctAlto.establecerSoloNumerico();
		ctAlto.establecerLimiteCaracteres(6);
		ctAlto.establecerTexto("50");

		final Label lId = new Label("Num Tile terreno (1 - " + String.valueOf(ListaModeloTile.getUltimoIdUsado()) + ")", this.COMIENZO_LABELS_X, 155, Color.white, 11f);
		ctIdModelo = new CajaTexto(new Rectangle(this.COMIENZO_CAJA_TEXTOS_X, 140, 40, 15), Color.white, Color.gray, Color.black);
		ctIdModelo.establecerSoloNumerico();
		ctIdModelo.establecerLimiteCaracteres(2);
		ctIdModelo.establecerTexto(String.valueOf(ListaModeloTile.COD_TIERRA));
		
		this.muestraTile = new MuestraTile(ctIdModelo.getNumeroParseado(), ctIdModelo.getArea().x+ctIdModelo.getArea().width+4, ctIdModelo.getArea().y-8);

		final Boton botonVolver = new Boton("Volver", Color.GRAY, new Rectangle(COMIENZO_LABELS_X, this.DIMENSION.height - 100, 40, 20));
		botonVolver.establecerAccion(() -> {
			this.GE.editorMapaSeleccion();
			this.accionPostClick();
		});

		botonCrear = new Boton("Crear", Color.GRAY, new Rectangle(this.DIMENSION.width - (2 * COMIENZO_LABELS_X), this.DIMENSION.height - 100, 40, 20));
		botonCrear.establecerAccion(() -> {
			int ancho = ctAncho.getNumeroParseado();
			int alto = ctAlto.getNumeroParseado();
			if (ancho > 0 && alto > 0) {
				this.GE.editorMapa(ancho, alto, ctIdModelo.getNumeroParseado());
			}
			this.accionPostClick();
		});

		this.COMPONENTES.add(ctAncho);
		this.COMPONENTES.add(lAncho);
		this.COMPONENTES.add(ctAlto);
		this.COMPONENTES.add(lAlto);
		this.COMPONENTES.add(botonVolver);
		this.COMPONENTES.add(botonCrear);
		this.COMPONENTES.add(lId);
		this.COMPONENTES.add(ctIdModelo);
		this.COMPONENTES.add(this.muestraTile);

	}

}
