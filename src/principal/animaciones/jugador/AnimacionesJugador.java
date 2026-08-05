package principal.animaciones.jugador;

import java.awt.Graphics2D;
import java.util.HashMap;
import principal.animaciones.Animacion;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.Jugador.Estado;
import principal.utilidades.Constantes;

public class AnimacionesJugador {
	
	private final Animacion ANIMACION_ESTANDAR_DERECHA;
	private final Animacion ANIMACION_ESTANDAR_IZQUIERDA;
	private final Animacion ANIMACION_ESTANDAR_ARRIBA;
	private final Animacion ANIMACION_ESTANDAR_ABAJO;
	
	private final Animacion ANIMACION_BASICA_DERECHA;
	private final Animacion ANIMACION_BASICA_IZQUIERDA;
	private final Animacion ANIMACION_BASICA_ARRIBA;
	private final Animacion ANIMACION_BASICA_ABAJO;
	
	private final Animacion ANIMACION_ARMADO_ESTANDAR_DERECHA;
	private final Animacion ANIMACION_ARMADO_ESTANDAR_IZQUIERDA;
	private final Animacion ANIMACION_ARMADO_ESTANDAR_ARRIBA;
	private final Animacion ANIMACION_ARMADO_ESTANDAR_ABAJO;
	
	private final Animacion ANIMACION_ARMADO_DERECHA;
	private final Animacion ANIMACION_ARMADO_IZQUIERDA;
	private final Animacion ANIMACION_ARMADO_ARRIBA;
	private final Animacion ANIMACION_ARMADO_ABAJO;
	
	private final int TIEMPO_MS_POR_FRAME = 150;
	
	
	public AnimacionesJugador() {
		this.ANIMACION_ESTANDAR_DERECHA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ESTANDAR_DERECHA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ESTANDAR_IZQUIERDA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ESTANDAR_IZQUIERDA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ESTANDAR_ARRIBA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ESTANDAR_ARRIBA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ESTANDAR_ABAJO = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ESTANDAR_ABAJO, true, this.TIEMPO_MS_POR_FRAME);
		
		this.ANIMACION_BASICA_ABAJO = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ABAJO, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_BASICA_ARRIBA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_ARRIBA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_BASICA_IZQUIERDA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_IZQUIERDA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_BASICA_DERECHA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.BASICA_DERECHA, true, this.TIEMPO_MS_POR_FRAME);
		
		this.ANIMACION_ARMADO_ESTANDAR_DERECHA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_ESTANDAR_DERECHA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_ESTANDAR_IZQUIERDA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_ESTANDAR_IZQUIERDA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_ESTANDAR_ARRIBA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_ESTANDAR_ARRIBA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_ESTANDAR_ABAJO = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_ESTANDAR_ABAJO, true, this.TIEMPO_MS_POR_FRAME);
		
		
		this.ANIMACION_ARMADO_DERECHA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_PISTOLA_DERECHA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_IZQUIERDA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_PISTOLA_IZQUIERDA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_ARRIBA = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_PISTOLA_ARRIBA, true, this.TIEMPO_MS_POR_FRAME);
		this.ANIMACION_ARMADO_ABAJO = new Animacion(Constantes.LISTA_HOJAS_SPRITES.JUGADOR.ARMADO_PISTOLA_ABAJO, true, this.TIEMPO_MS_POR_FRAME);
		
	}
	
	
	public void pintar(final Graphics2D g, final int x, final int y) {
		final Jugador jugador = Constantes.JUGADOR;
		final boolean transparencia = jugador.atrasDeComplemento();
		final float alpha = 0.5f;
		final Direccion direccion = jugador.getDireccion();
		final HashMap<Estado, Estado> ESTADO = jugador.getEstado();
		if(jugador.pistolaEquipada() && !ESTADO.containsKey(Estado.ARROJANDO)) {
			if(ESTADO.containsKey(Estado.ESTANDAR)) {
				if(direccion == Direccion.OESTE) {
					if(transparencia) {
						ANIMACION_ARMADO_ESTANDAR_IZQUIERDA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ARMADO_ESTANDAR_IZQUIERDA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.NORTE) {
					if(transparencia) {
						ANIMACION_ARMADO_ESTANDAR_ARRIBA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ARMADO_ESTANDAR_ARRIBA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.ESTE) {
					if(transparencia) {
						ANIMACION_ARMADO_ESTANDAR_DERECHA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ARMADO_ESTANDAR_DERECHA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.SUR) {
					if(transparencia) {
						ANIMACION_ARMADO_ESTANDAR_ABAJO.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ARMADO_ESTANDAR_ABAJO.pintar(g, x, y ,false);
					}
				}
				return;
			}
			if(direccion == Direccion.OESTE) {
				if(transparencia) {
					ANIMACION_ARMADO_IZQUIERDA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_ARMADO_IZQUIERDA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.NORTE) {
				if(transparencia) {
					ANIMACION_ARMADO_ARRIBA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_ARMADO_ARRIBA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.ESTE) {
				if(transparencia) {
					ANIMACION_ARMADO_DERECHA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_ARMADO_DERECHA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.SUR) {
				if(transparencia) {
					ANIMACION_ARMADO_ABAJO.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_ARMADO_ABAJO.pintar(g, x, y ,false);
				}
			}
		}else {
			if(ESTADO.containsKey(Estado.ESTANDAR)) {
				if(direccion == Direccion.OESTE) {
					if(transparencia) {
						ANIMACION_ESTANDAR_IZQUIERDA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ESTANDAR_IZQUIERDA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.NORTE) {
					if(transparencia) {
						ANIMACION_ESTANDAR_ARRIBA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ESTANDAR_ARRIBA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.ESTE) {
					if(transparencia) {
						ANIMACION_ESTANDAR_DERECHA.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ESTANDAR_DERECHA.pintar(g, x, y ,false);
					}
				}else if(direccion == Direccion.SUR) {
					if(transparencia) {
						ANIMACION_ESTANDAR_ABAJO.pintarConTransparencia(g, x, y, false, alpha);
					}else {
						ANIMACION_ESTANDAR_ABAJO.pintar(g, x, y ,false);
					}
				}
				return;
			}
			if(direccion == Direccion.OESTE) {
				if(transparencia) {
					ANIMACION_BASICA_IZQUIERDA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_BASICA_IZQUIERDA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.NORTE) {
				if(transparencia) {
					ANIMACION_BASICA_ARRIBA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_BASICA_ARRIBA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.ESTE) {
				if(transparencia) {
					ANIMACION_BASICA_DERECHA.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_BASICA_DERECHA.pintar(g, x, y ,false);
				}
			}else if(direccion == Direccion.SUR) {
				if(transparencia) {
					ANIMACION_BASICA_ABAJO.pintarConTransparencia(g, x, y, false, alpha);
				}else {
					ANIMACION_BASICA_ABAJO.pintar(g, x, y ,false);
				}
			}
		}
	}

}
