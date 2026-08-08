package principal.utilidades.funciones;

import java.awt.Point;
import java.util.ArrayList;

import principal.utilidades.Constantes;


public class Trayectorias {
	
	protected Trayectorias() {
		
	}
	
	/**
	 * Genera un vector[2][valor] -> [0] da los [valor X] , [1] da los [valor Y]. La cantidad de coordenadas 
	 * dependera de la velocidad especificada. La trayectoria sera en forma de arco (0° a 90° o 90° a 180°).
	 * @param px1 El punto X del primer punto
	 * @param py1 El punto Y del primer punto
	 * @param px2 El punto X del segundo punto
	 * @param py2 El punto Y del segundo punto
	 * @param tiempoMsTrayectoriaEnAnchoJuego El tiempo en milisegundos que se tardaria en desplazar hasta el final de 0 hasta el anchoJuego.
	 * @return El vector con cada posicion xy: vector[0] contiene las X. vector[1] contiene las Y. Ej -> vector[0][0] y vector[1][0] seria una coordena (x = vector[0][0],y = vector[1][0]).
	 * 			la cantidad de valores que hay de X e Y dependera de vector[0].length o vector[1].length ambos daran la misma cantidad de valores.
	 */
	 public int[][] getTrayectoiaBezier(int px1, int py1, int px2, int py2, final double tiempoMsTrayectoriaEnAnchoJuego) {
		final int aps = 60;
		final int MS_X_SEGUNDOS = 1000;	
		final double cantApsParaElTiempoEnAnchoPantalla = (tiempoMsTrayectoriaEnAnchoJuego/MS_X_SEGUNDOS)*aps;
		final double dist = calcularDistancia(px1, py1, px2, py2);
		final double cantApsParaElTiempoEnDistanciaEntrePuntos = dist <= (Constantes.ANCHO_JUEGO/4) ? dist * (cantApsParaElTiempoEnAnchoPantalla*1.5) / Constantes.ANCHO_JUEGO: dist * cantApsParaElTiempoEnAnchoPantalla / Constantes.ANCHO_JUEGO;
		final double vel = (1/cantApsParaElTiempoEnDistanciaEntrePuntos);
		
	 	int x1 = 0;
        int y1 = 0;
        int x2 = 0;
        int y2 = 0;
		if(this.calcularDistancia(px1, py1, 0, 0) < this.calcularDistancia(px2, py2, 0, 0)){
			x1 = px1;
	        y1 = py1;
	        x2 = px2;
	        y2 = py2;
		}else {
			x1 = px2;
	        y1 = py2;
	        x2 = px1;
	        y2 = py1;
		}
		
    	ArrayList<Point> puntos = new ArrayList<Point>();
    	int x3 = (x1 + x2) / 2;
        int y3 = Math.min(y1, y2) - 20;
        for (double t = 0; t <= 1; t += vel) {
            double x = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * x3 + t * t * x2;
            double y = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * y3 + t * t * y2;
            puntos.add(new Point((int)x,(int) y));
        }
        int[][] coords = new int[2][puntos.size()];
        int i = 0;
        for(Point p : puntos) {
        	coords[0][i] = p.x;
        	coords[1][i] = p.y;
        	i++;
        }
        return coords;
    }
	 
	 //-----------------------HACER QUE TAMBIEN TENGA COMO PARAMETRO LA VELOCIDAD. IGUAL QUE getTrayectoiaBezier().
	 public int[][] getTrayectoiaLineal(int px1, int py1, int px2, int py2) {
		 	int x1 = 0;
	        int y1 = 0;
	        int x2 = 0;
	        int y2 = 0;
			if(this.calcularDistancia(px1, py1, 0, 0) < this.calcularDistancia(px2, py2, 0, 0)){
				x1 = px1;
		        y1 = py1;
		        x2 = px2;
		        y2 = py2;
			}else {
				x1 = px2;
		        y1 = py2;
		        x2 = px1;
		        y2 = py1;
			}
	    	ArrayList<Point> puntos = new ArrayList<Point>();
	    	for (double t = 0; t <= 1; t += 0.01) {
	            double x = x1 + t * (x2 - x1);
	            double y = y1 + t * (y2 - y1);
	            puntos.add(new Point((int)x,(int) y));
	        }
	        int[][] coords = new int[2][puntos.size()];
	        int i = 0;
	        for(Point p : puntos) {
	        	coords[0][i] = p.x;
	        	coords[1][i] = p.y;
	        	i++;
	        }
	        return coords;
	    }
	 
	 public double calcularDistancia(double x1, double y1, double x2, double y2) {
	        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	    }

}
