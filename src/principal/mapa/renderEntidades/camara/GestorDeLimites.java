package principal.mapa.renderEntidades.camara;

import java.awt.Rectangle;

import principal.entes.Ente;
import principal.utilidades.Constantes;

public class GestorDeLimites extends Ente{
    private int x;
    private int y;
    private int ancho;
    private int alto;
    private Ente entidadEnfocada;
    private int limiteMaximoY;
    private int limiteMaximoX;
    private int limiteMinimoY;
    private int limiteMinimoX;
    private boolean eliminado;
    private boolean gestionandoX;
    private boolean gestionandoY;

    public GestorDeLimites() {

    }

    /**
     * Actualiza el gestor de limites y verifica si la entidad que tiene a cargo
     * sobrepasa alguno de los limites. De ser asi tomara el control de la/s ejes
     * (X/Y) que se haya sobrepasado para no desplazar mas la camara en dicho/s
     * eje/s. Se devolvera el control cuando la entidad deje de sobrepasar los
     * limites.
     */
    @Override
    public void actualizar() {
	if (this.entidadEnfocada == null) {
	    return;
	}
	if (this.entidadEnfocada.getPosicionXInt() <= this.limiteMinimoX) {
	    if (!this.gestionandoX) {
		this.gestionandoX = true;
		this.x = this.limiteMinimoX;
//				System.out.println("Limite minimo X: "+this.limiteMinimoX);
	    }

	} else if (this.entidadEnfocada.getPosicionXInt() >= this.limiteMaximoX) {
	    if (!this.gestionandoX) {
		this.gestionandoX = true;
		this.x = this.limiteMaximoX;
//				System.out.println("Limite maximo X: "+this.limiteMaximoX);
	    }

	} else if (this.gestionandoX) {
	    this.gestionandoX = false;
	}

	if (this.entidadEnfocada.getPosicionYInt() <= this.limiteMinimoY) {
	    if (!this.gestionandoY) {
		this.gestionandoY = true;
		this.y = this.limiteMinimoY;
//				System.out.println("Limite minimo Y: "+this.limiteMinimoY);
	    }

	} else if (this.entidadEnfocada.getPosicionYInt() >= this.limiteMaximoY) {
	    if (!this.gestionandoY) {
		this.gestionandoY = true;
		this.y = this.limiteMaximoY;
//				System.out.println("Limite maximo Y: "+this.limiteMaximoY);
	    }

	} else if (this.gestionandoY) {
	    this.gestionandoY = false;
	}
    }

    public void setEntidadEnfocada(final Ente e) {
	this.entidadEnfocada = e;
	this.limiteMaximoX = this.entidadEnfocada.getMundo().getTerreno().getAncho() - Constantes.ANCHO_JUEGO / 2
		- this.entidadEnfocada.getArea().width / 2;
	this.limiteMaximoY = this.entidadEnfocada.getMundo().getTerreno().getAlto() - Constantes.ALTO_JUEGO / 2
		- this.entidadEnfocada.getArea().height / 2;
	this.limiteMinimoX = Constantes.ANCHO_JUEGO / 2 - this.entidadEnfocada.getArea().width / 2;
	this.limiteMinimoY = Constantes.ALTO_JUEGO / 2 - this.entidadEnfocada.getArea().height / 2;
    }

    public void setEntidadEnfocada(final Ente e, final int limiteMaximoX, final int limiteMinimoX,
	    final int limiteMaximoY, final int limiteMinimoY, final boolean contarDimensionEnte) {
	this.entidadEnfocada = e;
	this.limiteMaximoX = contarDimensionEnte ? limiteMaximoX - this.entidadEnfocada.getArea().width / 2
		: limiteMaximoX;
	this.limiteMaximoY = contarDimensionEnte ? limiteMaximoY - this.entidadEnfocada.getArea().height / 2
		: limiteMaximoY;
	this.limiteMinimoX = contarDimensionEnte ? limiteMinimoX - this.entidadEnfocada.getArea().width / 2
		: limiteMinimoX;
	this.limiteMinimoY = contarDimensionEnte ? limiteMinimoY - this.entidadEnfocada.getArea().height / 2
		: limiteMinimoY;
    }

    public Ente getEntidadEnfocada() {
	return this.entidadEnfocada;
    }

    public boolean gestionandoX() {
	return this.gestionandoX;
    }

    public boolean gestionandoY() {
	return this.gestionandoY;
    }

    @Override
    public void eliminar() {
	this.eliminado = true;
    }

    public void restituir() {
	this.eliminado = false;
    }

    @Override
    public int getPosicionXInt() {
	if (this.gestionandoX) {
	    return this.x;
	} else {
	    return this.entidadEnfocada.getPosicionXInt();
	}
    }

    @Override
    public int getPosicionYInt() {
	if (this.gestionandoY) {
	    return this.y;
	} else {
	    return this.entidadEnfocada.getPosicionYInt();
	}
    }

    @Override
    public double getPosicionX() {
	if (this.gestionandoX) {
	    return this.x;
	} else {
	    return this.entidadEnfocada.getPosicionX();
	}
    }

    @Override
    public double getPosicionY() {
	if (this.gestionandoY) {
	    return this.y;
	} else {
	    return this.entidadEnfocada.getPosicionY();
	}
    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {
    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {
    }

    @Override
    public boolean estaEliminado() {
	return this.eliminado;
    }

    @Override
    public Rectangle getArea() {
	return new Rectangle(this.x, this.y, this.ancho, this.alto);
    }

}
