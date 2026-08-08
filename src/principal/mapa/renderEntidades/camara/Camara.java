package principal.mapa.renderEntidades.camara;

import principal.entes.Ente;
import principal.utilidades.Constantes;

public class Camara {
	private Ente entidadEnfocada;
	private int margenX;
	private int margenY;
	private final GestorDeLimites gestorLimite;

	public Camara(final Ente entidadEnfocada) {
		this.entidadEnfocada = entidadEnfocada;
		this.gestorLimite = new GestorDeLimites();
	}

	/**
	 * Permite a la camara actualizar el gestor de limites en caso de que este
	 * habilitado. Caso contrario no hara alguna actualizacion del mismo.
	 */
	public void actualizar() {
		if (!this.gestorLimite.estaEliminado()) {
			this.gestorLimite.actualizar();
		}
	}

	public Ente getEntidadEnfocada() {
		return this.entidadEnfocada;
	}

	/**
	 * Habilita el gestor de limites para que no se desplace la pantalla a partir de
	 * ciertos limites en determinado eje. Este metodo tomara como referencia los
	 * limites del mapa que contenga la entidad en su mundo. Para que de esta forma
	 * la camara se centre en el jugador siempre y cuando no se este pasando los
	 * limites. Caso contrario el gestor tomara el control en dicha coordenada y
	 * liberara a la entidad del enfoque para que no se vea mas alla del limite. Al
	 * dejar de pasarse del limite a la entidad se le devolvera el foco.
	 */
	public void habilitarGestorLimite() {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada);
	}

	/**
	 * Habilita el gestor de limites para que no se desplace la pantalla a partir de
	 * ciertos limites en determinado eje. Este metodo tomara como referencia los
	 * limites establecidos en los parametros. Para que de esta forma la camara se
	 * centre en el jugador siempre y cuando no se este pasando los limites. Caso
	 * contrario el gestor tomara el control en dicha coordenada y liberara a la
	 * entidad del enfoque para que no se vea mas alla del limite. Al dejar de
	 * pasarse del limite a la entidad se le devolvera el foco.
	 * 
	 * @param limiteMaximoX       Limite maximo que la entidad podra llegar en el
	 *                            eje X sin que se le saque el foco.
	 * @param limiteMinimoX       Limite minimo que la entidad podra llegar en el
	 *                            eje X sin que se le saque el foco.
	 * @param limiteMaximoY       Limite maximo que la entidad podra llegar en el
	 *                            eje Y sin que se le saque el foco.
	 * @param limiteMinimoY       Limite minimo que la entidad podra llegar en el
	 *                            eje Y sin que se le saque el foco.
	 * @param contarDimensionEnte Especifica si se debe tener en cuenta el ancho y
	 *                            alto del jugador en cada limite.
	 * @since 1.0
	 */
	public void habilitarGestorLimite(final int limiteMaximoX, final int limiteMinimoX, final int limiteMaximoY,
			final int limiteMinimoY, final boolean contarDimensionEnte) {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada, limiteMaximoX, limiteMinimoX, limiteMaximoY,
				limiteMinimoY, contarDimensionEnte);
	}

	/**
	 * Deshabilita el Gestor de limites. obteniendo todo el foco sin control alguno
	 * de limites en los deplazamientos de la entidad que tiene el foco.
	 * 
	 * @since 1.0
	 */
	public void deshabilitarGestorLimite() {
		this.gestorLimite.eliminar();
	}

	/**
	 * Establece la entidad que tendra el foco de la camara. Este metodo deshabilita
	 * automaticamente el Gestor de limites. En caso de querer habilitarlo debera
	 * hacerlo llamando al metodo correspondiente.
	 * 
	 * @param e La entidad que tendra el foco de la camara.
	 * @since 1.0
	 */
	public void setEntidadEnfocada(final Ente e) {
		this.entidadEnfocada = e;
		this.margenX = Constantes.CENTROX - (e.getArea().width / 2);
		this.margenY = Constantes.CENTROY - (e.getArea().height / 2);
		if (this.entidadEnfocada != this.gestorLimite.getEntidadEnfocada()) {
			this.gestorLimite.eliminar();
		}
	}

	public double getPosicionX() {
		if (this.gestorLimite.estaEliminado()) {
			return this.entidadEnfocada.getPosicionX();
		} else {
			return this.gestorLimite.getPosicionX();
		}
	}

	public double getPosicionY() {
		if (this.gestorLimite.estaEliminado()) {
			return this.entidadEnfocada.getPosicionY();
		} else {
			return this.gestorLimite.getPosicionY();
		}
	}

	public int getPosicionXInt() {
//		return this.entidadEnfocada.getPosicionX()-this.entidadEnfocada.getPosicionXInt()>=0.75? this.entidadEnfocada.getPosicionXInt()+1 : this.entidadEnfocada.getPosicionXInt();
		if (this.gestorLimite.estaEliminado()) {
			return (int) (this.entidadEnfocada.getPosicionX() + 0.0f);
		} else {
			return this.gestorLimite.getPosicionXInt();
		}
	}

	public int getPosicionYInt() {
//		return this.entidadEnfocada.getPosicionY()-this.entidadEnfocada.getPosicionYInt()>=0.75? this.entidadEnfocada.getPosicionYInt()+1 : this.entidadEnfocada.getPosicionYInt();
		if (this.gestorLimite.estaEliminado()) {
			return (int) (this.entidadEnfocada.getPosicionY() + 0.0f);
		} else {
			return this.gestorLimite.getPosicionYInt();
		}
	}

	public int getMargenX() {
		return this.margenX;
	}

	public int getMargenY() {
		return this.margenY;
	}
}
