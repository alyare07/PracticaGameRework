package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.controles.Tecla;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.Boton;
import principal.maquinaestado.estados.menu.herramientas.CajaTecla;
import principal.maquinaestado.estados.menu.herramientas.Componente;
import principal.maquinaestado.estados.menu.herramientas.Label;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public class MenuConfiguracion extends Menu {
	protected final Rectangle AREA_CONFIGURACIONES;
	protected final int COMIENZO_X = 30;
	protected final int COMIENZO_Y = 20;
	protected final int ANCHO_AREA = Constantes.ANCHO_JUEGO - (this.COMIENZO_X * 2);
//	protected final Rectangle AREA_VISTA;
	protected int y;
	protected int desplazamiento;
	protected final ArrayList<Componente> COMPONENTES_DESPLAZABLES = new ArrayList<Componente>();
	protected Boton btnGuardar;
	protected Boton btnVolver;
	protected Boton btnSubir;
	protected Boton btnBajar;

	public MenuConfiguracion(final GestorEstados ge) {
		super(ge);
		this.AREA_CONFIGURACIONES = new Rectangle(this.COMIENZO_X, this.COMIENZO_Y, this.ANCHO_AREA, 400);
//		this.AREA_VISTA =  new Rectangle(this.COMIENZO_X, this.COMIENZO_Y, this.ANCHO_AREA, 300);
		this.y = this.DIMENSION.height;
		this.inicializarComponentesDesplazables();
		this.actualizarVisibilidadBotonesSubirBajar();

	}

	@Override
	public void actualizar() {
		for (final Componente c : this.COMPONENTES) {
			if (c.visible()) {
				c.actualizar();
			}
		}

		for (final Componente c : this.COMPONENTES_DESPLAZABLES) {
			if (c.visible()) {
				c.actualizar();
				if (c instanceof CajaTecla) {
					((CajaTecla) c).establecerDesplazamientoY(this.desplazamiento);
				}
			}
		}

		this.actualizarVisibilidadBtnGuardar();
		this.actualizarVisibilidadBotonesSubirBajar();

	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.FONDO, 0, 0);
//		DibujoDebug.dibujarRectanguloRelleno(g, new Rectangle(this.AREA_CONFIGURACIONES.x,this.AREA_CONFIGURACIONES.y-desplazamiento,this.AREA_CONFIGURACIONES.width,this.AREA_CONFIGURACIONES.height), Color.orange);
		DibujoDebug.dibujarRectanguloContorno(g,
				new Rectangle(this.AREA_CONFIGURACIONES.x, this.AREA_CONFIGURACIONES.y - this.desplazamiento - 1,
						this.AREA_CONFIGURACIONES.width, this.AREA_CONFIGURACIONES.height),
				Color.red);
		for (final Componente c : this.COMPONENTES) {
			if (c.visible()) {
				c.pintar(g);
			}
		}

		for (final Componente c : this.COMPONENTES_DESPLAZABLES) {
			if (c.visible()) {
				c.pintar(g, this.desplazamiento);
			}
		}

	}

	protected void actualizarVisibilidadBotonesSubirBajar() {
		if ((this.AREA_CONFIGURACIONES.height + this.AREA_CONFIGURACIONES.y) < this.DIMENSION.height) {
			if (this.btnSubir.visible()) {
				this.btnSubir.visible(false);
			}
			if (this.btnBajar.visible()) {
				this.btnBajar.visible(false);
			}
		} else if (((this.AREA_CONFIGURACIONES.y + this.AREA_CONFIGURACIONES.height) > this.DIMENSION.height)
				&& (this.desplazamiento == 0)) {
			if (this.btnSubir.visible()) {
				this.btnSubir.visible(false);
			}
			if (!this.btnBajar.visible()) {
				this.btnBajar.visible(true);
			}
		} else if (((this.AREA_CONFIGURACIONES.y + this.AREA_CONFIGURACIONES.height) > this.DIMENSION.height)
				&& ((this.DIMENSION.height + this.desplazamiento) >= (this.AREA_CONFIGURACIONES.y
						+ this.AREA_CONFIGURACIONES.height))) {
			if (!this.btnSubir.visible()) {
				this.btnSubir.visible(true);
			}
			if (this.btnBajar.visible()) {
				this.btnBajar.visible(false);
			}
		} else {
			if (!this.btnSubir.visible()) {
				this.btnSubir.visible(true);
			}
			if (!this.btnBajar.visible()) {
				this.btnBajar.visible(true);
			}
		}
	}

	// HAY QUE HACER QUE LOS BOTONES SUBIR Y BAJAR SOLO SE VEAN CUANDO CORRESPONDAN
	// Y NO EN TODO MOMENTO!!!!

	@Override
	protected void inicializarBotones() {

		// Estos componentes tiene posicion de dibujado fija!
		final int anchoArea = Constantes.ANCHO_JUEGO - (this.COMIENZO_X * 2);
		final Boton subir = new Boton("Subir", Color.gray,
				new Rectangle(this.COMIENZO_X + anchoArea + 4, this.COMIENZO_Y, 20, 15));
		subir.establecerAccion(() -> {
			this.moverArriba();
		});

		final Boton bajar = new Boton("Bajar", Color.gray,
				new Rectangle(this.COMIENZO_X + anchoArea + 4, this.DIMENSION.height - this.COMIENZO_Y, 20, 15));
		bajar.establecerAccion(() -> {
			this.moverAbajo();
		});

		final Boton volver = new Boton("Volver", Color.gray,
				new Rectangle(2, this.DIMENSION.height - this.COMIENZO_Y, 24, 15));
		volver.establecerAccion(() -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		});

		this.btnVolver = volver;

		final Boton guardar = new Boton("Guardar", Color.gray,
				new Rectangle(2, this.DIMENSION.height - this.COMIENZO_Y - 20, 24, 15));
		guardar.establecerTamanoLetra(6f);
		guardar.establecerAccion(() -> {
			for (final Componente c : this.COMPONENTES_DESPLAZABLES) {
				if (c instanceof CajaTecla) {
					final CajaTecla ct = (CajaTecla) c;
					ct.establecerCambiosEnTecla();
				}
			}

			Globales.TECLADO.guardarConfig();
		});

		this.btnGuardar = guardar;
		this.COMPONENTES.add(subir);
		this.COMPONENTES.add(bajar);
		this.COMPONENTES.add(volver);
		this.COMPONENTES.add(guardar);

		this.btnBajar = bajar;
		this.btnSubir = subir;

	}

	protected void inicializarComponentesDesplazables() {
		final int margenYLineas = 20;
		final int margenXLabelCT = 4;
		// Componentes dentro del campo con desplazamiento en el dibujado
		final Label lbInventario = new Label("Inventario", this.COMIENZO_X + 2, this.COMIENZO_Y + 20, Color.white, 9f);
		final CajaTecla ctInventario = new CajaTecla(
				new Rectangle(lbInventario.getPunto().x + lbInventario.getAncho() + margenXLabelCT,
						(lbInventario.getPunto().y - lbInventario.getAlto()) + margenXLabelCT, 80, 10),
				Color.white, Color.black, Globales.TECLADO.TECLA_INVENTARIO);

//		final Label lbCorrer = new Label("Correr", lbInventario.getPunto().x ,lbInventario.getPunto().y+20,Color.white ,9f );
//		final CajaTecla ctCorrer = new CajaTecla(new Rectangle(lbCorrer.getPunto().x + lbCorrer.getAncho()+margenXLabelCT , lbCorrer.getPunto().y-lbCorrer.getAlto()+margenXLabelCT,80,10), Color.white, Color.black,Constantes.TECLADO.TECLA_CORRIENDO);

		final Label lbCorrer = this.generarLabelPosicionado("Correr", lbInventario, Color.white, 9f);
		final CajaTecla ctCorrer = this.generarCajaTeclaPosicionado(lbCorrer, Color.white, Color.black,
				Globales.TECLADO.TECLA_CORRIENDO, margenXLabelCT);

		final Label lbAtacar = this.generarLabelPosicionado("Atacar", lbCorrer, Color.white, 9f);
		final CajaTecla ctAtacar = this.generarCajaTeclaPosicionado(lbAtacar, Color.white, Color.black,
				Globales.TECLADO.TECLA_ATACANDO, margenXLabelCT);

		final Label lbRecoger = this.generarLabelPosicionado("Recoger", lbAtacar, Color.white, 9f);
		final CajaTecla ctRecoger = this.generarCajaTeclaPosicionado(lbRecoger, Color.white, Color.black,
				Globales.TECLADO.TECLA_RECOGIENDO, margenXLabelCT);

		final Label lbMoverArriba = this.generarLabelPosicionado("Mover Arriba", lbRecoger, Color.white, 9f);
		final CajaTecla ctMoverArriba = this.generarCajaTeclaPosicionado(lbMoverArriba, Color.white, Color.black,
				Globales.TECLADO.TECLA_ARRIBA, margenXLabelCT);

		final Label lbMoverAbajo = this.generarLabelPosicionado("Mover Abajo", lbMoverArriba, Color.white, 9f);
		final CajaTecla ctMoverAbajo = this.generarCajaTeclaPosicionado(lbMoverAbajo, Color.white, Color.black,
				Globales.TECLADO.TECLA_ABAJO, margenXLabelCT);

		final Label lbMoverIzquierda = this.generarLabelPosicionado("Mover Izquierda", lbMoverAbajo, Color.white, 9f);
		final CajaTecla ctMoverIzquierda = this.generarCajaTeclaPosicionado(lbMoverIzquierda, Color.white, Color.black,
				Globales.TECLADO.TECLA_IZQUIERDA, margenXLabelCT);

		final Label lbMoverDerecha = this.generarLabelPosicionado("Mover Derecha", lbMoverIzquierda, Color.white, 9f);
		final CajaTecla ctMoverDerecha = this.generarCajaTeclaPosicionado(lbMoverDerecha, Color.white, Color.black,
				Globales.TECLADO.TECLA_DERECHA, margenXLabelCT);

		this.COMPONENTES_DESPLAZABLES.add(lbInventario);
		this.COMPONENTES_DESPLAZABLES.add(ctInventario);
		this.COMPONENTES_DESPLAZABLES.add(lbCorrer);
		this.COMPONENTES_DESPLAZABLES.add(ctCorrer);
		this.COMPONENTES_DESPLAZABLES.add(ctAtacar);
		this.COMPONENTES_DESPLAZABLES.add(lbAtacar);
		this.COMPONENTES_DESPLAZABLES.add(ctRecoger);
		this.COMPONENTES_DESPLAZABLES.add(lbRecoger);
		this.COMPONENTES_DESPLAZABLES.add(ctMoverArriba);
		this.COMPONENTES_DESPLAZABLES.add(lbMoverArriba);
		this.COMPONENTES_DESPLAZABLES.add(ctMoverAbajo);
		this.COMPONENTES_DESPLAZABLES.add(lbMoverAbajo);
		this.COMPONENTES_DESPLAZABLES.add(ctMoverIzquierda);
		this.COMPONENTES_DESPLAZABLES.add(lbMoverIzquierda);
		this.COMPONENTES_DESPLAZABLES.add(ctMoverDerecha);
		this.COMPONENTES_DESPLAZABLES.add(lbMoverDerecha);

//		this.ultimoLabelLineaInferior(lbMoverDerecha);
		this.AREA_CONFIGURACIONES.height = this.DIMENSION.height + 200;
	}

	/***
	 * Permite determinar la visibilidad del boton guardar en base a si las cajas de
	 * teclas possen valores diferentes a los determinado en sus Teclas
	 * referenciadas
	 * 
	 **/
	private void actualizarVisibilidadBtnGuardar() {
		boolean mostrar = false;
		for (final Componente c : this.COMPONENTES_DESPLAZABLES) {
			if (c instanceof CajaTecla) {
				final CajaTecla ct = (CajaTecla) c;
				if (ct.modificado()) {
					mostrar = true;
					break;
				}
			}
		}
		this.btnGuardar.visible(mostrar);

	}

	/**
	 * Permite generar un label en base al label posicionado en la linea superior al
	 * que se generara este, de forma que este label quede en la linea de abajo
	 * 
	 * @param texto         -> es el texto que mostrara el label
	 * @param labelSuperior -> es el label que se usa como referencia para generar
	 *                      este label pero en la siguiente linea de abajo
	 * @param colorLetra    -> es el color que tendra la letra
	 * @param tamanoLetra   -> es el tamano que tendran las letras del texto en el
	 *                      label
	 * @return Label -> retorna un nuevo label en base a los parametros pasados
	 * 
	 **/
	private Label generarLabelPosicionado(final String texto, final Label labelSuperior, final Color colorLetra,
			final float tamanoLetra) {
		return new Label(texto, labelSuperior.getPunto().x, labelSuperior.getPunto().y + 20, colorLetra, tamanoLetra);
	}

	/**
	 * Permite generar una CajaTecla en base al label posicionado en el costado
	 * izquierdo al que se generara este, de forma que esta CajaTecla quede en al
	 * lado derecho del label
	 * 
	 * @param labelIzquierdo  -> es el label que se usa como referencia para generar
	 *                        esta CajaTecla pero al costado derecho del label
	 * @param colorFondo      -> es el color que tendra el fondo de la CajaTecla
	 * @param colorLetra      -> es el color que tendra la letra
	 * @param teclaReferencia -> es la tecla a la que se cambiaran los valores en el
	 *                        teclado y usaran como referencia
	 * @return CajaTecla -> retorna una CajaTecla en base a los parametros pasados
	 * 
	 **/
	private CajaTecla generarCajaTeclaPosicionado(final Label labelIzquierdo, final Color colorFondo,
			final Color colorLetra, final Tecla teclaReferencia, final int margenXLabelCT) {
		return new CajaTecla(
				new Rectangle(labelIzquierdo.getPunto().x + labelIzquierdo.getAncho() + margenXLabelCT,
						(labelIzquierdo.getPunto().y - labelIzquierdo.getAlto()) + margenXLabelCT, 80, 10),
				colorFondo, colorLetra, teclaReferencia);
	}

	private void moverArriba() {
		if (this.y > this.DIMENSION.height) {
			this.y--;
			this.desplazamiento--;
		}
	}

	private void moverAbajo() {

		if (this.y < (this.AREA_CONFIGURACIONES.height + this.COMIENZO_Y)) {
			this.y++;
			this.desplazamiento++;
		}
	}

	private void ultimoLabelLineaInferior(final Label l) {
		this.AREA_CONFIGURACIONES.height = ((l.getPunto().y + l.getAlto()) - this.COMIENZO_Y) + 4;
	}

}
