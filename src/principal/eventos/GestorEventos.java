package principal.eventos;

import java.util.ArrayList;

public class GestorEventos {

	private final ArrayList<SecuenciaEvento> secuenciasActivas = new ArrayList<SecuenciaEvento>(4);

	public void registrarSecuenciaActiva(final SecuenciaEvento secuencia) {
		if (secuencia != null && !this.secuenciasActivas.contains(secuencia)) {
			this.secuenciasActivas.add(secuencia);
		}
	}

	public void actualizar(final double dt) {
		for (int i = this.secuenciasActivas.size() - 1; i >= 0; i--) {
			final SecuenciaEvento s = this.secuenciasActivas.get(i);
			s.actualizar(dt);
			if (!s.isEnEjecucion()) {
				this.secuenciasActivas.remove(i);
			}
		}
	}

	public boolean haySecuenciaEnCurso() {
		return !this.secuenciasActivas.isEmpty();
	}
}