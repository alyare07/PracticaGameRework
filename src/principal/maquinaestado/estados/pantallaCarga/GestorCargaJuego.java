package principal.maquinaestado.estados.pantallaCarga;

public class GestorCargaJuego extends GestorCarga {
	public GestorCargaJuego() {
		
	}
	
	public void cargar(final CargaJuego cj, final String rutaMundo, final GestorCarga gc) {
		if(this.completo) {
			this.completo = false;
			this.porcentaje = 0;
		}
		
		this.hiloCarga = new Thread(new Runnable() {
			@Override
			public void run() {
				cj.cargarJuego(rutaMundo, gc);
			}
		});
		this.hiloCarga.start();
	}


}
