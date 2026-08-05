package principal.entes.criaturas.enemigos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import org.json.simple.JSONObject;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.proyectil.filtro.GolpeMeleContraJugador;
import principal.mapa.Mundo;
import principal.mapa.Mapa;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Sonidos;
import principal.utilidades.dijkstra.Dijkstra;
import principal.utilidades.dijkstra.Nodo;

public class Enemigo extends Criatura {
	public enum Estado {
		ESTANDAR("Estandar"), CAMINANDO("Caminando"), CORRIENDO("Corriendo"), ATACANDO("Atacando"), PERSIGUIENDO("Persiguiendo");

		private Estado(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		private final String DESCRIPCION;

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}

	private boolean pendienteADijkstra;
	private Nodo ant;
	private boolean colisiona;
	private final static int TIEMPO_MS_ESPERA_REGEN_VIDA = 10000;
	private final static int TIEMPO_MS_ESPERA_ATACADO = 7000;
	private final static int TIEMPO_MS_BUSQUEDA_FUERA_RANGO = 8000;
	private final static int TIEMPO_MS_ESPERA_ATAQUE = 1000;
	private final static int TIEMPO_MS_CARGA_ATAQUE_ESPERA = 250;//250
	private final static int TIEMPO_MS_ESPERA_RETOMAR_ATAQUE= 750;//750
	private final GestorTiempo GE_FUERA_DE_RANGO;
	private final GestorTiempo GE_ATAQUE_COOLDOWN;
	protected final GestorTiempo GT_CARGA_ATAQUE;
	protected final GestorTiempo GT_RETOMAR_ATAQUE;
	private double areaDeteccionAncho;
	private double areaDeteccionAlto;
	private boolean atacando;
	private double ataque = 25;
	private Estado estado;
	private boolean realizandoAtaque;
	//COD PRUEBA 23
	private Nodo nodoDestino;
	private int destinoX;
	private int destinoY;
	private final static Random ALEATORIO = new Random(System.currentTimeMillis());
	private static final int ACCION_ESPERAR = 1;
	private static final int ACCION_MOVER = 2;
	private boolean enAccion;
	private int accion;
	private final Dijkstra DIJKSTRA;
	private int tiempoAccionEsperaMs;
	//FIN COD PRUEBA 23

	public Enemigo(double x, double y, int ancho, int alto, double vida, double vidaMaxima, BufferedImage hoja, final Mundo mundo) {
		super(x, y, ancho, alto, vida, vidaMaxima, hoja);
		this.areaDeteccionAlto = 150;
		this.areaDeteccionAncho = 150;
		this.GE_FUERA_DE_RANGO = new GestorTiempo();
		this.GE_ATAQUE_COOLDOWN = new GestorTiempo();
		this.GT_CARGA_ATAQUE = new GestorTiempo();
		this.GT_RETOMAR_ATAQUE = new GestorTiempo();
		this.velocidad = 0.25;
		this.estado = Estado.ESTANDAR;
		this.mundo = mundo;
		//COD PRUEBA 23
		this.destinoX = (int) x;
		this.destinoY = (int) y;
		this.DIJKSTRA = new Dijkstra(this.mundo.getMapa().getTILES(), this.mundo.getMapa().ladoTile(), (this.mundo.getMapa().getAncho() - this.mundo.getMapa().ladoTile()), (this.mundo.getMapa().getAlto() - this.mundo.getMapa().ladoTile()));
		//FIN COD PRUEBA 23
	}

	@Override
	public void actualizar() {
		curar();
		if (Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
			actualizarAtaque();
			if(this.estado != Estado.ATACANDO) {
				this.tomarAccion();
			}
		}
		if(Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if(Constantes.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Constantes.JUGADOR.getDamage());
			}
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

	private void actualizarAtaque() {
		if(this.realizandoAtaque) {
			if(this.enAccion) {
				this.enAccion = false;
			}
			if(this.GT_CARGA_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_CARGA_ATAQUE_ESPERA)) {
				//Ataque del enemigo al jugador
				final Rectangle rangoMele = this.rangoAtaqueMele();
//				if(rangoMele.intersects(Constantes.JUGADOR.getRectangulo())) {
////					Constantes.JUGADOR.recibirAtaque(this.ataque,this);
//					
//				}
				this.mundo.crearProyectil(new GolpeMeleContraJugador(this.ataque, false, this.mundo, rangoMele.x, rangoMele.y, rangoMele.width, rangoMele.height, this.direccion, this));
				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.GE_ATAQUE_COOLDOWN.establecerReferenciaTiempoActual();
			}
			return;
		}else {
			if(!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_RETOMAR_ATAQUE)) {
				return;
			}
		}
		
		
		if (atacando) {
			this.estado = Estado.ATACANDO;
			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				if(this.rangoAtaqueMele().intersects(Constantes.JUGADOR.getRectangulo())) {
					if(GE_ATAQUE_COOLDOWN.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_ATAQUE)) {
						
						//realiza la carga del ataque
						if(!this.realizandoAtaque) {
							this.realizandoAtaque = true;
							final double xJugador = Constantes.JUGADOR.getPosicionX();
							final double yJugador = Constantes.JUGADOR.getPosicionY();
							
							byte posx = 0;
							byte posy = 0;
							
							if(this.x < xJugador) {
								posx = -1;
							}else {
								posx = 1;
							}
							if(this.y < yJugador) {
								posy = -1;
							}else {
								posy = 1;
							}
							
							double difX = posx < 0 ? xJugador - x: x -xJugador;
							double difY = posy < 0 ? yJugador - y: y - yJugador;
							if(difX > difY) { 
								//si el enemigo esta a la izquierda entonces mirara a la derecha por que ahi esta el jugador
								if(posx < 0) {
									this.direccion = Direccion.ESTE;
									actualPerfil = hoja.getSprite(1);
									
								}else {
									this.direccion = Direccion.OESTE;
									actualPerfil = hoja.getSpriteInvertidoHorizontal(1);
								}
							}else {
								if(posy < 0) {
									this.direccion = Direccion.SUR;
									actualPerfil = hoja.getSprite(3);
								}else {
									this.direccion = Direccion.NORTE;
									actualPerfil = hoja.getSprite(5);
								}
							}
							
							this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
						}
						
						
					}
					return;
				}else {
					if (getRectangulo().intersects(Constantes.JUGADOR.getRectangulo())) {
						if (this.x < Constantes.JUGADOR.getPosicionX()) {
							modificarPosicionX(velocidad);
							if ((Constantes.JUGADOR.getPosicionX() - this.x) <= 0.25) {
								this.x = Constantes.JUGADOR.getPosicionX();
							}
						} else {
							if (this.x > Constantes.JUGADOR.getPosicionX()) {
								modificarPosicionX(-velocidad);
								if ((this.x - Constantes.JUGADOR.getPosicionX()) <= 0.25) {
									this.x = Constantes.JUGADOR.getPosicionX();
								}
							}
						}

						if (this.y < Constantes.JUGADOR.getPosicionY()) {
							modificarPosicionY(velocidad);
							if ((Constantes.JUGADOR.getPosicionY() - this.y) <= 0.25) {
								this.y = Constantes.JUGADOR.getPosicionY();
							}
						} else {
							if (this.y > Constantes.JUGADOR.getPosicionY()) {
								modificarPosicionY(-velocidad);
								if ((this.y - Constantes.JUGADOR.getPosicionY()) <= 0.25) {
									this.y = Constantes.JUGADOR.getPosicionY();
								}
							}
						}
					}
				}
				mover(this.mundo.getDijkstra(), this.mundo.getMapa());
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			} else if (!this.GE_FUERA_DE_RANGO.transcurrioMiliSegundos(TIEMPO_MS_BUSQUEDA_FUERA_RANGO)) {
				mover(this.mundo.getDijkstra(), this.mundo.getMapa());
			} else {
				this.atacando = false;
				this.pendienteADijkstra = false;
				this.mundo.getDijkstra().reducirCriaturasPendientes();
			}
		} else {
			if (this.estado != Estado.ESTANDAR) {
				actualPerfil = hoja.getSprite(0);
				this.estado = Estado.ESTANDAR;
			}

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				this.atacando = true;
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}
	}
	
	
	//COD PRUEBA 23
	
	private void tomarAccion() {
		if (enAccion) {
			switch (this.accion) {
			case ACCION_ESPERAR:
				esperar();
				break;
			case ACCION_MOVER:
				moverRandom();
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
				moverRandom();
				break;
			}
		}

	}
	
	private void esperar() {
		if (GT_ESPERA.transcurrioMiliSegundos(this.tiempoAccionEsperaMs)) {
			this.enAccion = false;
		}
		if (this.estado != Estado.ESTANDAR) {
			actualPerfil = hoja.getSprite(0);
			this.estado = Estado.ESTANDAR;
		}
	}

	private void generarTiempoDeEspera() {
		final int minMs = 1500;
		final int maxMs = 10000;
		this.tiempoAccionEsperaMs = ALEATORIO.nextInt(maxMs - minMs + 1) + minMs;
		this.GT_ESPERA.establecerReferenciaTiempoActual();
	}
	
	private void moverRandom() {
		
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
				if (this.mundo.getMapa().getTileReferenciado(this.getPosicionXInt(), this.getPosicionYInt()) == this.mundo.getMapa().getTileReferenciado(destinoX, destinoY)) {
					/*
					 * HACER EL ELSE IF DE ABAJO EN ESTE MISMO METODO ACA. ESPERO ENTENDERME :/
					 */
					if (this.getPosicionXInt() < this.destinoX) {
						if ( this.destinoX-this.getPosicionXInt() < this.velocidad) {
							this.x = this.destinoX;
//							this.modificarPosicionX();
						}else {
//							this.x += this.velocidad;
							this.modificarPosicionX(this.velocidad);
						}
					} else if (this.getPosicionXInt() > this.destinoX) {
						if(this.getPosicionXInt() - this.destinoX < this.velocidad) {
							this.x = this.velocidad;
						}else {
							this.modificarPosicionX(-this.velocidad);
//							this.x -= this.velocidad;
						}
					}
					if (this.getPosicionYInt() < this.destinoY) {
						if (this.destinoY - this.getPosicionYInt()< this.velocidad) {
							this.y = this.destinoY;
						}else {
							this.modificarPosicionY(this.velocidad);
//							this.y += this.velocidad;
						}
					} else if (this.getPosicionYInt() > this.destinoY) {
						if (this.getPosicionYInt() - this.destinoY < this.velocidad) {
							this.y = this.destinoY;
						}else {
							this.modificarPosicionY(-this.velocidad);
//							this.y -= this.velocidad;
						}
					}
					if(this.estado!=Estado.CAMINANDO) {
						this.estado=Estado.CAMINANDO;
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
//					this.x += this.velocidad;
					this.modificarPosicionX(this.velocidad);
				}
				
			} else if (this.getPosicionXInt() > this.nodoDestino.TILE.getPosicionX()) {
				if((this.getPosicionXInt() - this.nodoDestino.TILE.getPosicionX()) < this.velocidad) {
					this.x = this.nodoDestino.TILE.getPosicionX();
				}else {
					this.modificarPosicionX(-this.velocidad);
//					this.x -= this.velocidad;
				}
			}
			if (this.getPosicionYInt() < this.nodoDestino.TILE.getPosicionY()) {
				if((this.nodoDestino.TILE.getPosicionY() - this.getPosicionYInt()) < this.velocidad) {
					this.y = this.nodoDestino.TILE.getPosicionY();
				}else {
					this.modificarPosicionY(this.velocidad);
//					this.y += this.velocidad;
				}
			} else if (this.getPosicionYInt() > this.nodoDestino.TILE.getPosicionY()) {
				if((this.getPosicionYInt() - this.nodoDestino.TILE.getPosicionY()) < this.velocidad) {
					this.y = this.nodoDestino.TILE.getPosicionY();
				}else {
//					this.y -= this.velocidad;
					this.modificarPosicionY(-this.velocidad);
				}
			}
			if(this.estado!=Estado.CAMINANDO) {
				this.estado=Estado.CAMINANDO;
			}

		}
	}
	
	
	private void cambiarDestinoAlAzar() {
		boolean destinoFactible = false;
		final int desplazamiento = this.mundo.getMapa().ladoTile() * 3;
		int maxX = this.getPosicionXInt() + desplazamiento;
		int minX = this.getPosicionXInt() - desplazamiento;
		int maxY = this.getPosicionYInt() + desplazamiento;
		int minY = this.getPosicionYInt() - desplazamiento;
		while (destinoFactible == false) {
			this.destinoX = ALEATORIO.nextInt(maxX - minX + 1) + minX;
			ALEATORIO.setSeed(System.currentTimeMillis());
			this.destinoY = ALEATORIO.nextInt(maxY - minY + 1) + minY;
			ALEATORIO.setSeed(System.currentTimeMillis());
			if (this.mundo.getMapa().getTileReferenciado(destinoX, destinoY) != null && (!this.mundo.getMapa().getTileReferenciado(destinoX, destinoY).esSolidoDisktra())) {
				destinoFactible = true;
			}
		}
		this.DIJKSTRA.actualizar(new Point(destinoX / this.mundo.getMapa().ladoTile(), destinoY / this.mundo.getMapa().ladoTile()));
		this.nodoDestino = DIJKSTRA.getNodoCercano4P((int) this.x, (int) this.y);
	}
	//FIN COD PRUEBA 23

	
	
	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
			DibujoDebug.dibujarFiguraEllipseRefCamara(g, new Rectangle((int)(this.x - (areaDeteccionAncho / 2) + (this.ANCHO / 2)) ,  (int)(this.y - (areaDeteccionAlto / 2) + (this.ALTO / 2)),(int)this.areaDeteccionAncho, (int)this.areaDeteccionAlto),Color.red);
			DibujoDebug.dibujarFiguraEllipseRefCamara(g, new Rectangle((int)(this.x - (areaDeteccionAncho / 8) + (this.ANCHO / 2)), (int) (this.y - (areaDeteccionAlto / 8) + (this.ALTO / 2)) ,(int)(this.areaDeteccionAncho/4), (int) (this.areaDeteccionAlto/4)), Color.orange);
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

	protected void mover(final Dijkstra d, final Mapa mapa) {
		if (!this.colisiona && this.getPosicionTile().distance(Constantes.JUGADOR.getPosicionTile()) <= (1.0)) {
			if (this.pendienteADijkstra) {
				this.pendienteADijkstra = false;
				d.reducirCriaturasPendientes();
			}
			if (getRectangulo().intersects(Constantes.JUGADOR.getRectangulo())) {
				return;
			}
			final int jugadorPosicionX = Constantes.JUGADOR.getPosicionXInt();
			final int jugadorPosicionY = Constantes.JUGADOR.getPosicionYInt();
			if (this.x < jugadorPosicionX) {
				if (!mapa.intersecta(new Rectangle((int) (x + velocidad), (int) y, ANCHO, ALTO))) {
					modificarPosicionX(velocidad);
//					x += velocidad;
				} else {
					colisiona = true;
				}
			} else {
				if (this.x > jugadorPosicionX) {
					if (!mapa.intersecta(new Rectangle((int) (x - velocidad), (int) y, ANCHO, ALTO))) {
//						x -= velocidad;
						modificarPosicionX(-velocidad);
					} else {
						colisiona = true;
					}
				}
			}

			if (!colisiona && this.y < jugadorPosicionY) {
				if (!mapa.intersecta(new Rectangle((int) (this.x), (int) (this.y + velocidad), ANCHO, ALTO))) {
//					y += velocidad;
					modificarPosicionY(velocidad);
				} else {
					colisiona = true;
				}
			} else {
				if (!colisiona && this.y > jugadorPosicionY) {
					if (!mapa.intersecta(new Rectangle((int) (this.x), (int) (this.y + velocidad), ANCHO, ALTO))) {
//						y -= velocidad;
						modificarPosicionY(-velocidad);
					} else {
						colisiona = true;
					}
				}
			}
			if (!colisiona) {
				return;
			}

		}
		if (!this.pendienteADijkstra) {
			this.pendienteADijkstra = true;
			d.aumentarCriaturasPendientes();
		}
		if (ant != null && ant.distancia == 0 && colisiona) {
			return;
		}

		final Nodo n = d.getNodoCercano4P((int) this.x, (int) this.y);
		if (this.ant != n) {
			this.ant = n;
			this.colisiona = false;
		}
		if (n == null) {
			return;
		}

//		if (n.TILE.esSolidoDisktra()) {
//			final int jugadorPosicionX = Constantes.JUGADOR.getPosicionXInt();
//			final int jugadorPosicionY = Constantes.JUGADOR.getPosicionYInt();
//			if (jugadorPosicionX < this.x && jugadorPosicionY < this.y) {
//				this.y -= velocidad;
//			} else {
//				if (jugadorPosicionX == this.x && jugadorPosicionY < this.y) {
//					this.x -= velocidad;
//				} else {
//					if (jugadorPosicionX > this.x && jugadorPosicionY < this.y) {
//						this.y -= velocidad;
//					} else {
//						if (jugadorPosicionX > this.x && jugadorPosicionY == this.y) {
//							this.y += velocidad;
//						} else {
//							if (jugadorPosicionX > this.x && jugadorPosicionY > this.y) {
//								this.y += velocidad;
//							} else {
//								if (jugadorPosicionX == this.x && jugadorPosicionY > this.y) {
//									this.x += velocidad;
//								} else {
//									if (jugadorPosicionX < this.x && jugadorPosicionY > this.y) {
//										this.y += velocidad;
//									} else {
//										if (jugadorPosicionX < this.x && jugadorPosicionY == this.y) {
//											this.y += velocidad;
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			return;
//		}
		final Point posNodo = n.TILE.getPosicion();
//		System.out.println("posNodo: " + posNodo);

		if (this.x < posNodo.x) {
//			x += velocidad;
			modificarPosicionX(velocidad);
			if ((posNodo.x - this.x) <= 0.25) {
				this.x = posNodo.x;
			}
		} else {
			if (this.x > posNodo.x) {
//				x -= velocidad;
				modificarPosicionX(-velocidad);
				if ((this.x - posNodo.x) <= 0.25) {
					this.x = posNodo.x;
				}
			}
		}

		if (this.y < posNodo.y) {
//			y += velocidad;
			modificarPosicionY(velocidad);
			if ((posNodo.y - this.y) <= 0.25) {
				this.y = posNodo.y;
			}
		} else {
			if (this.y > posNodo.y) {
//				y -= velocidad;
				modificarPosicionY(-velocidad);
				if ((this.y - posNodo.y) <= 0.25) {
					this.y = posNodo.y;
				}
			}
		}

	}

	public Ellipse2D getAreaDeteccionLogica() {
		return new Ellipse2D.Double(this.x - (this.areaDeteccionAncho / 2) + (this.ANCHO / 2), this.y - (this.areaDeteccionAlto / 2) + (this.ALTO / 2), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
	}
	
	
	protected Rectangle rangoAtaqueMele() {
		double xRango = this.x + (this.ANCHO/2);
		double yRango = this.y + (this.ALTO/2);
		final double alcanceRango = 10;
		final double anchoRango = 4;
		
		if(this.direccion == Direccion.NORTE) {
			return new Rectangle((int)xRango,(int) (yRango - alcanceRango),(int) anchoRango,(int) alcanceRango);
		}else if(this.direccion == Direccion.SUR) {
			return new Rectangle((int)xRango,(int) yRango,(int) anchoRango,(int) alcanceRango);
		}else if(this.direccion == Direccion.OESTE) {
			return new Rectangle((int)(xRango-alcanceRango),(int) yRango,(int) alcanceRango,(int) anchoRango);
		}else {
			return new Rectangle((int)xRango,(int) yRango,(int) alcanceRango,(int) anchoRango);
		}
	}
	
	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		this.reducirVida(damage);
		if(causante instanceof Jugador) {
			this.GT_ATACADO.establecerReferenciaTiempoActual();
			//prueba de atacar cuando es atacado
			this.atacando = true;
			this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
		}
		super.recibirAtaque(damage, causante);
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		int resto = Constantes.GLOBALES.animacion % 20;
		if (desplazamientoX > 0) {
			this.direccion = Direccion.ESTE;
			if (resto > 10 && resto < 20) {
				actualPerfil = hoja.getSprite(1);
			} else {
				actualPerfil = hoja.getSprite(7);
			}
		} else if (desplazamientoX < 0) {
			this.direccion = Direccion.OESTE;
			if (resto > 10 && resto < 20) {
				actualPerfil = hoja.getSpriteInvertidoHorizontal(1);
			} else {
				actualPerfil = hoja.getSpriteInvertidoHorizontal(7);
			}
		}
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		int resto = Constantes.GLOBALES.animacion % 20;
		if (desplazamientoY > 0) {
			this.direccion = Direccion.SUR;
			if (resto > 10 && resto < 20) {
				actualPerfil = hoja.getSprite(3);
			} else {
				actualPerfil = hoja.getSprite(6);
			}
		} else if (desplazamientoY < 0) {
			this.direccion = Direccion.NORTE;
			if (resto > 10 && resto < 20) {
				actualPerfil = hoja.getSprite(5);
			} else {
				actualPerfil = hoja.getSprite(8);
			}
		}
		this.y += desplazamientoY;
	}
	
	public static Criatura crearDesdeJSON(final JSONObject json, Mundo mundo) {
		Criatura criatura = null;
		try {
			double x = Double.parseDouble(json.get("x").toString());
			double y = Double.parseDouble(json.get("y").toString());
			int ancho = Integer.parseInt(json.get("w").toString());
			int alto = Integer.parseInt(json.get("h").toString());
			double vida = Double.parseDouble(json.get("hp").toString());
			double vidaMax = Double.parseDouble(json.get("maxhp").toString());
			
			criatura = new Enemigo(x, y, ancho, alto, vidaMax, vidaMax, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/jugadores.png").getSubimage(48, 48, 48, 48),mundo);
			criatura.establecerVida(vida);
		} catch (Exception e) {
			System.out.println("Error al crear Enemigo desde JSON: "+e.getMessage());
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
		json.put("hp",this.vida);
		json.put("maxhp",this.vidaMaxima);
		return json;
	}
	
	@Override
	public  String exportarTipoCriatura() {
		return "Enemigo";
	}
	
	@Override
	public void eliminar() {
		Sonidos.SONIDO_DEAD_CRIATURE.reproducir();
		this.eliminado = true;
	}

}
