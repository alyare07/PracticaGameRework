package principal.entes.criaturas.neutrales;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.mapa.Mundo;
import principal.mapa.Mapa;
import principal.utilidades.Constantes;
import principal.utilidades.dijkstra.Dijkstra;
import principal.utilidades.dijkstra.Nodo;

public class CosaNeutral extends Criatura {
	private final static Random ALEATORIO = new Random(System.currentTimeMillis());
	private static final int ACCION_ESPERAR = 1;
	private static final int ACCION_MOVER = 2;
	private int destinoX;
	private int destinoY;
	private Nodo nodoDestino;
	private final Mapa MAPA;
	private final Dijkstra DIJKSTRA;
//	private final Color color;
	private final int R;
	private final int G;
	private final int B;
	

	private boolean enAccion;
	private int accion;
	private int tiempoAccionEsperaMs;
	private final static int TIEMPO_MS_ESPERA_REGEN_VIDA = 1000;
	private final static int TIEMPO_MS_ESPERA_ATACADO = 7000;

	public CosaNeutral(double x, double y, int ancho, int alto, Color color, final Mapa mapa, final double velocidad) {
		super(x, y, ancho, alto, velocidad, color);
		this.R = color.getRed();
		this.G = color.getGreen();
		this.B = color.getBlue();
		this.MAPA = mapa;
		this.destinoX = (int) x;
		this.destinoY = (int) y;
		this.DIJKSTRA = new Dijkstra(this.MAPA.getTILES(), this.MAPA.ladoTile(), (MAPA.getAncho() - MAPA.ladoTile()), (MAPA.getAlto() - MAPA.ladoTile()));
//		this.color = color;
		this.vidaRegen = 2;
		establecerVidaMaxima(15);
	}

	@Override
	public void actualizar() {
		curar();
		if (Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
			tomarAccion();
		}
		if(mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea())) {
			if(!this.atrasDeComplemento) {
				this.atrasDeComplemento = true;
			}
		}else {
			if(this.atrasDeComplemento) {
				this.atrasDeComplemento = false;
			}
		}
	}

	public boolean recibiendoAtaque() {
		return !this.GT_ATACADO.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_ATACADO);
	}

	private void curar() {
		if (this.vida < this.vidaMaxima) {
			if ((!recibiendoAtaque()) && (this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA))) {
				this.curar(this.vidaRegen);
				this.GT_CURACION.establecerReferenciaTiempoActual();
			}
		} else {
			return;
		}
	}

	private void mover() {
		
		if (destinoX == this.getPosicionXInt() && destinoY == getPosicionYInt()) {
			if (this.nodoDestino == null) {
				cambiarDestinoAlAzar();

			} else {
				this.enAccion = false;
				return;
			}
		} else {
			if (this.nodoDestino == null) {
				cambiarDestinoAlAzar();

			} else {
				if (this.MAPA.getTileReferenciado(this.getPosicionXInt(), this.getPosicionYInt()) == this.MAPA.getTileReferenciado(destinoX, destinoY)) {
					/*
					 * HACER EL ELSE IF DE ABAJO EN ESTE MISMO METODO ACA. ESPERO ENTENDERME :/
					 */
					if (this.getPosicionXInt() < this.destinoX) {
						if ( this.destinoX-this.getPosicionXInt() < this.velocidad) {
							this.x = this.destinoX;
						}else {
							this.x += this.velocidad;
						}
					} else if (this.getPosicionXInt() > this.destinoX) {
						if(this.getPosicionXInt() - this.destinoX < this.velocidad) {
							this.x = this.velocidad;
						}else {
							this.x -= this.velocidad;
						}
					}
					if (this.getPosicionYInt() < this.destinoY) {
						if (this.destinoY - this.getPosicionYInt()< this.velocidad) {
							this.y = this.destinoY;
						}else {
							this.y += this.velocidad;
						}
					} else if (this.getPosicionYInt() > this.destinoY) {
						if (this.getPosicionYInt() - this.destinoY < this.velocidad) {
							this.y = this.destinoY;
						}else {
							this.y -= this.velocidad;
						}
					}
					return;
				}
			}
			if (this.nodoDestino == null) {
				cambiarDestinoAlAzar();
				if (this.nodoDestino == null) {
					return;
				}
			}
//			this.nodoDestino = DIJKSTRA.getNodoCercano((int) this.x, (int) this.y);
			this.nodoDestino = DIJKSTRA.getNodoCercano4P((int) this.x, (int) this.y);
			if (this.nodoDestino == null) {
				cambiarDestinoAlAzar();
				if (this.nodoDestino == null) {
					return;
				}
			}
			
			if (this.getPosicionXInt() < this.nodoDestino.TILE.getPosicionX()) {
				if((this.nodoDestino.TILE.getPosicionX() - this.getPosicionXInt()) < this.velocidad) {
					this.x = this.nodoDestino.TILE.getPosicionX();
				}else {
					this.x += this.velocidad;
				}
				
			} else if (this.getPosicionXInt() > this.nodoDestino.TILE.getPosicionX()) {
				if((this.getPosicionXInt() - this.nodoDestino.TILE.getPosicionX()) < this.velocidad) {
					this.x = this.nodoDestino.TILE.getPosicionX();
				}else {
					this.x -= this.velocidad;
				}
			}
			if (this.getPosicionYInt() < this.nodoDestino.TILE.getPosicionY()) {
				if((this.nodoDestino.TILE.getPosicionY() - this.getPosicionYInt()) < this.velocidad) {
					this.y = this.nodoDestino.TILE.getPosicionY();
				}else {
					this.y += this.velocidad;
				}
			} else if (this.getPosicionYInt() > this.nodoDestino.TILE.getPosicionY()) {
				if((this.getPosicionYInt() - this.nodoDestino.TILE.getPosicionY()) < this.velocidad) {
					this.y = this.nodoDestino.TILE.getPosicionY();
				}else {
					this.y -= this.velocidad;
				}
			}

		}
	}

	private void esperar() {
		if (GT_ESPERA.transcurrioMiliSegundos(this.tiempoAccionEsperaMs)) {
			this.enAccion = false;
		}
	}

	private void generarTiempoDeEspera() {
		final int minMs = 1500;
		final int maxMs = 10000;
		this.tiempoAccionEsperaMs = ALEATORIO.nextInt(maxMs - minMs + 1) + minMs;
		this.GT_ESPERA.establecerReferenciaTiempoActual();
	}

	private void tomarAccion() {
		if (enAccion) {
			switch (this.accion) {
			case ACCION_ESPERAR:
				esperar();
				break;
			case ACCION_MOVER:
				mover();
				break;
			}
			return;
		}

		this.accion = ALEATORIO.nextInt(2 - 1 + 1) + 1;
		ALEATORIO.setSeed(System.currentTimeMillis());
		this.enAccion = true;

		if (enAccion) {
			switch (this.accion) {
			case ACCION_ESPERAR:
				generarTiempoDeEspera();
				esperar();
				break;
			case ACCION_MOVER:
				cambiarDestinoAlAzar();
				mover();
				break;
			}
		}

	}

	private void cambiarDestinoAlAzar() {
		boolean destinoFactible = false;
		final int desplazamiento = this.MAPA.ladoTile() * 3;
		int maxX = this.getPosicionXInt() + desplazamiento;
		int minX = this.getPosicionXInt() - desplazamiento;
		int maxY = this.getPosicionYInt() + desplazamiento;
		int minY = this.getPosicionYInt() - desplazamiento;
		while (destinoFactible == false) {
			this.destinoX = ALEATORIO.nextInt(maxX - minX + 1) + minX;
			ALEATORIO.setSeed(System.currentTimeMillis());
			this.destinoY = ALEATORIO.nextInt(maxY - minY + 1) + minY;
			ALEATORIO.setSeed(System.currentTimeMillis());
			if (MAPA.getTileReferenciado(destinoX, destinoY) != null && (!MAPA.getTileReferenciado(destinoX, destinoY).esSolidoDisktra())) {
				destinoFactible = true;
			}
		}
		this.DIJKSTRA.actualizar(new Point(destinoX / MAPA.ladoTile(), destinoY / MAPA.ladoTile()));
		this.nodoDestino = DIJKSTRA.getNodoCercano4P((int) this.x, (int) this.y);
	}
	

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		this.reducirVida(damage);
		super.recibirAtaque(damage, causante);
		this.GT_ATACADO.establecerReferenciaTiempoActual();
	}
	
	
	public static Criatura crearDesdeJSON(final JSONObject json, Mapa mapa) {
		Criatura criatura = null;
		try {
			double x = Double.parseDouble(json.get("x").toString());
			double y = Double.parseDouble(json.get("y").toString());
			int ancho = Integer.parseInt(json.get("w").toString());
			int alto = Integer.parseInt(json.get("h").toString());
			double velocidad = Double.parseDouble(json.get("vel").toString());
			double vida = Double.parseDouble(json.get("hp").toString());
			int r = Integer.parseInt(json.get("r").toString());
			int g = Integer.parseInt(json.get("g").toString());
			int b = Integer.parseInt(json.get("b").toString());
			
			criatura = new CosaNeutral(x, y, ancho, alto, new Color(r, g, b), mapa, velocidad);
			criatura.establecerVida(vida);
		} catch (Exception e) {
			System.out.println("Error al crear CosaNeutral desde JSON: "+e.getMessage());
		}
		return criatura;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		JSONObject json = new JSONObject();
		json.put("x", getPosicionX());
		json.put("y", getPosicionY());
		json.put("w", this.ANCHO);
		json.put("h", this.ALTO);
		json.put("vel", this.velocidad);
		json.put("hp",this.vida);
		json.put("r", this.R);
		json.put("g", this.G);
		json.put("b", this.B);
		return json;
	}

	@Override
	public  String exportarTipoCriatura() {
		return "CosaNeutral";
	}
	
	

}
