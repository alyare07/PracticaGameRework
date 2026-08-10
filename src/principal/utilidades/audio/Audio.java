package principal.utilidades.audio;

public interface Audio {
	void reproducir();

	void pausar();

	void detener();

	void repetir(boolean repetir);

	void actualizar(boolean reproducir);

	// Asigna el volumen en un rango lineal de 0.0 (silencio) a 1.0 (máximo)
	void setVolumen(double volumen);
}