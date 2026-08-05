package principal.utilidades.funciones;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class GeneradorTooltip {
	
	protected GeneradorTooltip() {
		
	}
	
	/**
	 * Calcula la posicion necesaria para general un tooltip en el 
	 * punto mencionado. Los valores se calculan dividiando la pantalla
	 * en cuatro partes iguales (MITAD HORIZONTAL Y VERTICAL) y analizando
	 * en cual cuadrante se esta ubicado. De esta forma se desplazara un poco
	 * la posicion segun sea necesario para que el tooltip no se salga
	 * de los bordes de la pantalla.
	 * @param pi Punto como referencia donde se desea general el tooltip
	 * @return El punto calculado.
	 */
	private Point generarTooltip(final Point pi) {
		final int x = pi.x;
		final int y = pi.y;
		final Point centroCanvas = new Point(Constantes.CENTROX, Constantes.CENTROY);
		final Point centroCanvasEscalado = new Point((int) (centroCanvas.x * Constantes.FACTOR_ESCALADO_X),
				(int) (centroCanvas.y * Constantes.FACTOR_ESCALADO_Y));
		final int margenCursor = 5;

		final Point pf = new Point();

		if (x <= centroCanvasEscalado.x) {
			if (y <= centroCanvasEscalado.y) { // si esta arriba a la izquierda
				pf.x = x + Constantes.LADO_CURSOR + margenCursor;
				pf.y = y + Constantes.LADO_CURSOR + margenCursor; // se dibuja abajo a la derecha
			} else {
				pf.x = x + Constantes.LADO_CURSOR + margenCursor;
				pf.y = y - Constantes.LADO_CURSOR + margenCursor;
			}
		} else {
			if (y <= centroCanvasEscalado.y) {
				pf.x = x - Constantes.LADO_CURSOR - margenCursor;
				pf.y = y + Constantes.LADO_CURSOR + margenCursor;
			} else {
				pf.x = x - Constantes.LADO_CURSOR - margenCursor;
				pf.y = y - Constantes.LADO_CURSOR + margenCursor;
			}
		}

		return pf;
	}

	/**
	 * Calcula en que punto cardinal se ubica el punto en la pantalla.
	 * @param pi El punto a calcular
	 * @return "no" Si el punto esta arriba a la izquierda (NOROESTE).
	 * 		   "so" Si el punto esta abajo a la izquierda (SUROESTE).
	 * 		   "ne" Si el punto esta arriba a la derecha (NORESTE).
	 *  	   "se" Si el punto esta abajo a la derecha (SURESTE).
	 */
	private String getPosicionTooltip(final Point pi) {
		final int x = pi.x;
		final int y = pi.y;
		final Point centroCanvas = new Point(Constantes.CENTROX, Constantes.CENTROY);
		final Point centroCanvasEscalado = new Point((int) (centroCanvas.x * Constantes.FACTOR_ESCALADO_X),
				(int) (centroCanvas.y * Constantes.FACTOR_ESCALADO_Y));
		String posicion = "";
		if (x <= centroCanvasEscalado.x) {
			if (y <= centroCanvasEscalado.y) { // si esta arriba a la izquierda
				posicion = "no";
			} else {
				posicion = "so";
			}
		} else {
			if (y <= centroCanvasEscalado.y) {
				posicion = "ne";
			} else {
				posicion = "se";
			}
		}

		return posicion;

	}

	/**
	 * Dibuja el tooltip en la posicion del puntero ESCALADO y calculado para 
	 * que no se salga de los bordes de la pantalla.El texto se vera
	 * en un solo renglon.
	 * @param g La clase Graphics.
	 * @param texto El texto que tendra el tooltip.
	 * @param colorLetra El color de la letra.
	 * @param colorFondo El color del fondo.
	 */
	public void dibujarTooltip(final Graphics2D g, final String texto, final Color colorLetra,
			final Color colorFondo) {
		final SuperficieDibujo sd = SuperficieDibujo.obetenerSuperficieDibujo();
		final Point posicionRaton = sd.RATON.getPuntoPosicionSinEscalar();
		final Point posicionTooltip = this.generarTooltip(posicionRaton);
		final String pistaPosicion = this.getPosicionTooltip(posicionTooltip);
		final Point posicionTooltipEscalada = new Point((int) (posicionTooltip.x / Constantes.FACTOR_ESCALADO_X),
				(int) (posicionTooltip.y / Constantes.FACTOR_ESCALADO_Y));
		final int ancho = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int alto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
		final int margenFuente = 2;

		Rectangle tooltip = null;
		switch (pistaPosicion) {
		case "no":
			tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y, ancho + margenFuente * 2,
					alto);
			break;
		case "ne":
			tooltip = new Rectangle(posicionTooltipEscalada.x - ancho, posicionTooltipEscalada.y,
					ancho + margenFuente * 2, alto);
			break;
		case "so":
			tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y - alto,
					ancho + margenFuente * 2, alto);
			break;
		case "se":
			tooltip = new Rectangle(posicionTooltipEscalada.x - ancho, posicionTooltipEscalada.y - alto,
					ancho + margenFuente * 2, alto);
			break;
		}
		DibujoDebug.dibujarRectanguloRelleno(g, tooltip, colorFondo);
		DibujoDebug.dibujarString(g, texto,
				new Point(tooltip.x + margenFuente, tooltip.y + tooltip.height - margenFuente), colorLetra);
	}
	
	/**
	 * Dibuja el tooltip en la posicion del puntero ESCALADO y calculado para 
	 * que no se salga de los bordes de la pantalla. El texto contendra en el
	 * primer renglon centrado el nombre del item y por debajo un renglon para
	 * cada String que contenga el array de getInfo() de la clase {@link Item}.
	 * @param g La clase Graphics.
	 * @param item El item a dibujar el tooltip.
	 */
	public void dibujarTooltipItem(final Graphics2D g, final Item item) {
		
		final Color colorNombre = Color.gray;
		final Color colorInfo = Color.lightGray;
		final Color colorFondo = Color.black;
		
		final SuperficieDibujo sd = SuperficieDibujo.obetenerSuperficieDibujo();
		final Point posicionRaton = sd.RATON.getPuntoPosicionSinEscalar();
		final Point posicionTooltip = this.generarTooltip(posicionRaton);
		final String pistaPosicion = this.getPosicionTooltip(posicionTooltip);
		final Point posicionTooltipEscalada = new Point((int) (posicionTooltip.x / Constantes.FACTOR_ESCALADO_X),
				(int) (posicionTooltip.y / Constantes.FACTOR_ESCALADO_Y));
		
		final int margenFuente = 2;
		final int anchoNombre = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, item.getNombre());
		final int altoNombre = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, item.getNombre());
		
		int yDesplazamiento = altoNombre;
		
		g.setFont(g.getFont().deriveFont(g.getFont().getSize()-2));
		
		int anchoInfo = 0;
		int altoInfo = 0;
		
		int maxAnchoInfo = 0;
		
		{
			int anchoAux = 0;
			
			for(String linea : item.getInfo()) {
				anchoAux = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, linea);
				if(anchoAux > maxAnchoInfo) {
					maxAnchoInfo = anchoAux;
				}
			}
			
			if(anchoNombre > maxAnchoInfo) {
				maxAnchoInfo = anchoNombre;
			}
			
		}
		
		
		
	
		
		//MEDIR EL AREA DE ALTO
		int despX = 0;
		int altoArea = altoNombre;
		
		for(String linea : item.getInfo()) {
			anchoInfo = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, linea);
			altoInfo = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);

			altoArea+=altoInfo;
		}
		
		
		for(String linea : item.getInfo()) {
			anchoInfo = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, linea);
			altoInfo = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);
			
			
			
			despX = maxAnchoInfo - anchoInfo;
			
			Rectangle tooltip = null;
			switch (pistaPosicion) {
			case "no":
				tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y+yDesplazamiento, anchoInfo + margenFuente * 2 + despX,
						altoInfo);
				break;
			case "ne":
				tooltip = new Rectangle(posicionTooltipEscalada.x - anchoInfo -despX, posicionTooltipEscalada.y+yDesplazamiento,
						anchoInfo + margenFuente * 2 + despX, altoInfo);
				break;
			case "so":
				tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y - altoArea+yDesplazamiento,
						anchoInfo + margenFuente * 2 + despX, altoInfo);
				break;
			case "se":
				tooltip = new Rectangle(posicionTooltipEscalada.x - anchoInfo -despX, posicionTooltipEscalada.y - altoArea+yDesplazamiento,
						anchoInfo + margenFuente * 2 + despX, altoInfo);
				break;
			}
			
			
			DibujoDebug.dibujarRectanguloRelleno(g, tooltip, colorFondo);
			DibujoDebug.dibujarString(g, linea,
					new Point(tooltip.x + margenFuente, tooltip.y + tooltip.height - margenFuente), colorInfo);
			
			
			
			yDesplazamiento+=altoInfo;
		}
		
		
		g.setFont(g.getFont().deriveFont(g.getFont().getSize()+2));
		
		if(anchoNombre > maxAnchoInfo) {
			despX = anchoNombre - maxAnchoInfo;
		}else {
			despX =  maxAnchoInfo - anchoNombre;
		}
		
		//NOMBRE
				Rectangle tooltip = null;
				switch (pistaPosicion) {
				case "no":
					tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y, anchoNombre + margenFuente * 2 + despX,
							altoNombre);
					break;
				case "ne":
					tooltip = new Rectangle(posicionTooltipEscalada.x - anchoNombre -despX, posicionTooltipEscalada.y,
							anchoNombre + margenFuente * 2 + despX, altoNombre);
					break;
				case "so":
					tooltip = new Rectangle(posicionTooltipEscalada.x, posicionTooltipEscalada.y -  altoArea,
							anchoNombre + margenFuente * 2 + despX, altoNombre);
					break;
				case "se":
					tooltip = new Rectangle(posicionTooltipEscalada.x - anchoNombre -despX, posicionTooltipEscalada.y - altoArea,
							anchoNombre + margenFuente * 2 + despX, altoNombre);
					break;
				}
				
				
				DibujoDebug.dibujarRectanguloRelleno(g, tooltip, colorFondo);
				DibujoDebug.dibujarString(g, item.getNombre(),
						new Point(tooltip.x + margenFuente + (despX/2), tooltip.y + tooltip.height - margenFuente), colorNombre);
		
	}

}
