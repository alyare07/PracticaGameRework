package principal.ia.aEstrella;

/**
 * Montículo Binario Mínimo (Min-Heap) de alto rendimiento para el algoritmo A*.
 * 
 * Diseñado bajo arquitectura Zero-Allocation con arreglos planos primitivos.
 * Implementa desempate (Tie-Breaking) nativo comparando el costo H cuando los
 * costos F son equivalentes.
 */
public final class MinHeapAEstrella {

	private NodoA[] nodos;
	private float[] costosF;
	private float[] costosH;
	private int tamano;

	protected MinHeapAEstrella(final int capacidadInicial) {
		this.nodos = new NodoA[capacidadInicial];
		this.costosF = new float[capacidadInicial];
		this.costosH = new float[capacidadInicial];
		this.tamano = 0;
	}

	/**
	 * Limpia el montículo en tiempo O(1) sin crear objetos para el GC.
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
	 * Inserta un nodo en la cola ordenándolo por menor costo F y menor costo H.
	 *
	 * @param nodo Nodo a insertar.
	 * @param f    Costo total estimado (G + H).
	 * @param h    Heurística restante al objetivo.
	 */
	public void push(final NodoA nodo, final float f, final float h) {
		if (this.tamano >= this.nodos.length) {
			this.redimensionar(this.nodos.length * 2);
		}

		int i = this.tamano;
		this.tamano++;

		// Sift-Up con Tie-Breaking
		while (i > 0) {
			final int padre = (i - 1) >>> 1;

			// Si el padre ya es mejor o igual, encontramos la posición
			if (this.esMejorOIgual(this.costosF[padre], this.costosH[padre], f, h)) {
				break;
			}

			this.nodos[i] = this.nodos[padre];
			this.costosF[i] = this.costosF[padre];
			this.costosH[i] = this.costosH[padre];
			i = padre;
		}

		this.nodos[i] = nodo;
		this.costosF[i] = f;
		this.costosH[i] = h;
	}

	/**
	 * Extrae el nodo prioritario (menor F / menor H) de la raíz.
	 *
	 * @return El NodoA con menor costo, o null si está vacía.
	 */
	public NodoA poll() {
		if (this.tamano == 0) {
			return null;
		}

		final NodoA raiz = this.nodos[0];
		this.tamano--;

		if (this.tamano > 0) {
			final NodoA ultimoNodo = this.nodos[this.tamano];
			final float ultimoF = this.costosF[this.tamano];
			final float ultimoH = this.costosH[this.tamano];

			int i = 0;
			final int mitad = this.tamano >>> 1;

			// Sift-Down con Tie-Breaking
			while (i < mitad) {
				final int hijoIzq = (i << 1) + 1;
				final int hijoDer = hijoIzq + 1;
				int mejorHijo = hijoIzq;

				if ((hijoDer < this.tamano) && this.esMejor(this.costosF[hijoDer], this.costosH[hijoDer],
						this.costosF[hijoIzq], this.costosH[hijoIzq])) {
					mejorHijo = hijoDer;
				}

				if (this.esMejorOIgual(ultimoF, ultimoH, this.costosF[mejorHijo], this.costosH[mejorHijo])) {
					break;
				}

				this.nodos[i] = this.nodos[mejorHijo];
				this.costosF[i] = this.costosF[mejorHijo];
				this.costosH[i] = this.costosH[mejorHijo];
				i = mejorHijo;
			}

			this.nodos[i] = ultimoNodo;
			this.costosF[i] = ultimoF;
			this.costosH[i] = ultimoH;
		}

		return raiz;
	}

	private boolean esMejor(final float f1, final float h1, final float f2, final float h2) {
		if (f1 < f2) {
			return true;
		}
		if (f1 == f2) {
			return h1 < h2;
		}
		return false;
	}

	private boolean esMejorOIgual(final float f1, final float h1, final float f2, final float h2) {
		if (f1 < f2) {
			return true;
		}
		if (f1 == f2) {
			return h1 <= h2;
		}
		return false;
	}

	private void redimensionar(final int nuevaCapacidad) {
		final NodoA[] nuevosNodos = new NodoA[nuevaCapacidad];
		final float[] nuevosCostosF = new float[nuevaCapacidad];
		final float[] nuevosCostosH = new float[nuevaCapacidad];

		System.arraycopy(this.nodos, 0, nuevosNodos, 0, this.tamano);
		System.arraycopy(this.costosF, 0, nuevosCostosF, 0, this.tamano);
		System.arraycopy(this.costosH, 0, nuevosCostosH, 0, this.tamano);

		this.nodos = nuevosNodos;
		this.costosF = nuevosCostosF;
		this.costosH = nuevosCostosH;
	}
}