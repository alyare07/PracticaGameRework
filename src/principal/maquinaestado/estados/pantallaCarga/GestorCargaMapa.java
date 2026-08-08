package principal.maquinaestado.estados.pantallaCarga;

public class GestorCargaMapa extends GestorCarga{
    public GestorCargaMapa() {

    }

    public void cargar(final cargaMapa cj, final GestorCarga gc, final String nombreMapa, final boolean reset, final String nombreSpawn) {
	if (this.completo) {
	    this.completo = false;
	    this.porcentaje = 0;
	}

	this.hiloCarga = new Thread(new Runnable() {
	    @Override
	    public void run() {
		cj.cargarMapa(gc, nombreMapa, reset, nombreSpawn);
	    }
	});
	this.hiloCarga.start();
    }

}
