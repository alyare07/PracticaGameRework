package principal.ia.dijkstra;

/**
 * Montículo Binario Mínimo (Min-Heap) de alto rendimiento y cero asignaciones
 * en memoria. Diseñado específicamente para el hilo de Dijkstra usando arreglos
 * planos contiguos.
 */
final class MinHeapDijkstra {

	private NodoD[] nodos;
	private double[] distancias;
	private int tamano;

	protected MinHeapDijkstra(final int capacidadInicial) {
		this.nodos = new NodoD[capacidadInicial];
		this.distancias = new double[capacidadInicial];
		this.tamano = 0;
	}

	/**
	 * Limpia la cola en O(1) sin destruir los arreglos ni generar objetos para el
	 * GC.
	 */
	public void clear() {
		this.tamano = 0;
	}

	public boolean isEmpty() {
		return this.tamano == 0;
	}

	public int size() {
		return this.tamano;
	}

	/**
	 * Inserta un nodo y su distancia flotante aplicando sift-up binario.
	 */
	public void push(final NodoD nodo, final double dist) {
		if (this.tamano >= this.nodos.length) {
			this.redimensionar(this.nodos.length * 2);
		}

		int i = this.tamano;
		this.tamano++;

		// Sift-Up
		while (i > 0) {
			final int padre = (i - 1) >>> 1;
			if (dist >= this.distancias[padre]) {
				break;
			}
			this.nodos[i] = this.nodos[padre];
			this.distancias[i] = this.distancias[padre];
			i = padre;
		}

		this.nodos[i] = nodo;
		this.distancias[i] = dist;
	}

	/**
	 * Extrae el nodo con la menor distancia acumulada en la raíz.
	 * 
	 * @return El NodoD prioritario.
	 */
	public NodoD poll() {
		if (this.tamano == 0) {
			return null;
		}

		final NodoD raiz = this.nodos[0];
		this.tamano--;

		if (this.tamano > 0) {
			final NodoD ultimoNodo = this.nodos[this.tamano];
			final double ultimaDist = this.distancias[this.tamano];

			int i = 0;
			final int mitad = this.tamano >>> 1;

			// Sift-Down
			while (i < mitad) {
				final int hijoIzq = (i << 1) + 1;
				final int hijoDer = hijoIzq + 1;
				int menorHijo = hijoIzq;

				if ((hijoDer < this.tamano) && (this.distancias[hijoDer] < this.distancias[hijoIzq])) {
					menorHijo = hijoDer;
				}

				if (ultimaDist <= this.distancias[menorHijo]) {
					break;
				}

				this.nodos[i] = this.nodos[menorHijo];
				this.distancias[i] = this.distancias[menorHijo];
				i = menorHijo;
			}

			this.nodos[i] = ultimoNodo;
			this.distancias[i] = ultimaDist;
		}

		return raiz;
	}

	/**
	 * Redimensionamiento de emergencia solo si el mapa excede la capacidad inicial
	 * estimada.
	 */
	private void redimensionar(final int nuevaCapacidad) {
		final NodoD[] nuevosNodos = new NodoD[nuevaCapacidad];
		final double[] nuevasDistancias = new double[nuevaCapacidad];

		System.arraycopy(this.nodos, 0, nuevosNodos, 0, this.tamano);
		System.arraycopy(this.distancias, 0, nuevasDistancias, 0, this.tamano);

		this.nodos = nuevosNodos;
		this.distancias = nuevasDistancias;
	}
}
