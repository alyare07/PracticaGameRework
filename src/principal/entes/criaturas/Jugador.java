package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;
import principal.animaciones.Animaciones;
import principal.entes.Ente;
import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.entes.objetos.particulas.Sangre;
import principal.entes.proyectil.GolpeMele;
import principal.mapa.Mundo;
import principal.mapa.Mapa;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.SonidoMP3;

public class Jugador extends Criatura {

	public enum Estado {
		ESTANDAR("Estandar"), CAMINANDO("Caminando"), CORRIENDO("Corriendo"), ATACANDO("Atacando"), ARROJANDO("Arrojando");

		private Estado(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		private final String DESCRIPCION;

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}

	protected final int MARGENX;
	protected final int MARGENY;
	protected int desplazamientoX;
	protected int desplazamientoY;
	protected Tile tilePisado;
	private double damage;
	private final GestorTiempo GT_ULTIMO_ATAQUE; 
	private final GestorTiempo GT_RECUPERACION_ESTAMINA;
	private static final int TIEMPO_MS_ESPERA_POR_ATAQUE = 600;
	private static final int TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE = TIEMPO_MS_ESPERA_POR_ATAQUE / 2;
	private static final int TIEMPO_MS_ESPERA_REGEN_VIDA = 5000;
	private static final int TIEMPO_MS_ESPERA_REGEN_ESTAMINA = 5000;
	private boolean dibujarAtaque;
	private final SonidoMP3 SONIDO_HIT_GOLPE;
	protected Shape areaRecoleccion;
	protected final int recoleccionLado = 50;
	/*
	 * RECOMIENDO CREAR UNA CLASE GESTORA DE LOS PTS DEL JUGADOR TALES COMO LA ESTAMINA Y LA VIDA. LIMPIAR CODIGO! 
	 */
	protected final double PTS_VIDAMAX_BASE = 100;
	protected final double PTS_DAMAGE_BASE = 5;
	protected double estamina;
	protected double maxEstamina;
	protected double puntoRecuperarEstaminaXseg;
	protected final float PTS_CONSUMIR_ESTAMINA = 0.5f;
	protected final byte LADO_INTERACCION_COFRE = 16;
	protected final HashMap<Estado, Estado> ESTADO = new HashMap<Estado, Estado>();
	
	
	public Jugador(int x, int y, int ancho, int alto, final BufferedImage hoja) {
		super(x, y, ancho, alto, 50, 50, hoja);
		MARGENX = Constantes.CENTROX - (ancho / 2);
		MARGENY = Constantes.CENTROY - (alto / 2);
		this.establecerVidaMaxima(PTS_VIDAMAX_BASE);
		this.damage = PTS_DAMAGE_BASE;
		this.velocidadEstandar = 0.5;
		this.GT_ULTIMO_ATAQUE = new GestorTiempo();
		this.GT_RECUPERACION_ESTAMINA = new GestorTiempo();
		this.dibujarAtaque = false;
		this.SONIDO_HIT_GOLPE = new SonidoMP3("sonidos/hit_punch.mp3");
		this.actualizarAreaRecoleccion();
		this.maxEstamina = 50; //150 - 200
		this.estamina = this.maxEstamina;
		this.puntoRecuperarEstaminaXseg = 1;
//		actualPerfil = Animaciones.JUGADOR.BASICA.animacionEstandar(this.direccion);
	}

	@Override
	public void pintar(final Graphics2D g) {
		Animaciones.JUGADOR.pintar(g,Constantes.getXDesplazamientoCamara(this.getPosicionXInt()),Constantes.getYDesplazamientoCamara(this.getPosicionYInt()));
		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() && Constantes.GLOBALES.estadoJuego) {
			g.setColor(Color.BLUE);
			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionArriba(0));

			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionAbajo(0));

			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionDerecha(0));

			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionIzquierda(0));
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, getArea(), Color.cyan);
		}
		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
			pintarAreaRecoleccion(g);
		}
		
		this.pintarAreaArrojar(g);
	}

	@Override
	public void actualizar() {
		if(Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if(Constantes.RATON.presionadoClickIzqUnicaAct()) {
				Constantes.CAMARA.setEntidadEnfocada(this);
			}else if(Constantes.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Constantes.JUGADOR.getDamage());
			}
		}
		if(this.eliminado) {
			return;
		}
		
		this.curar();
		
		final Mapa mapa = mundo.getMapa();
		this.tilePisado = mapa.getTileReferenciado(getRectanguloInterseccionAbajo(0).x, getRectanguloInterseccionAbajo(0).y);
		actualizarMovimientos();
		actualizarRecogidaItems();
		this.actualizarArrojar();
		actualizarAtaque();
	}

	private void actualizarAtaque() {
		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() && this.ESTADO.containsKey(Estado.ATACANDO) && GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			this.sacarEstado(Estado.ATACANDO);
		}
		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() || this.ESTADO.containsKey(Estado.ARROJANDO)) {
			if (this.dibujarAtaque) {
				if (GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE)) {
					this.dibujarAtaque = false;
				}
			}
			return;
		}
		

		if (GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			GT_ULTIMO_ATAQUE.establecerReferenciaTiempoActual();
			this.dibujarAtaque = true;
			this.meterEstado(Estado.ATACANDO);
			this.realizarAtaque(mundo);
			
		} else {
			if (GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE)) {
				this.dibujarAtaque = false;
			}
		}
		
		
	}
	
	private void curar() {
		if (this.vida < this.vidaMaxima) {
			if ( (this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA))) {
				this.curar(this.vidaRegen);
				this.GT_CURACION.establecerReferenciaTiempoActual();
			}
		} else {
			return;
		}
	}
	
	private void meterEstado(final Estado estado) {
		if(this.ESTADO.containsKey(estado)) {
		}else {
			this.ESTADO.put(estado, estado);
		}
	}
	
	private void sacarEstado(final Estado estado) {
		if(this.ESTADO.containsKey(estado)) {
			this.ESTADO.remove(estado);
		}
	}
	
	private void realizarAtaque(final Mundo mundo) {
		final Arma armaEquipada = this.getArmaEquipada();
		if(armaEquipada instanceof Pistola) {
			final Pistola pistola = (Pistola) armaEquipada;
			if(this.direccion == Direccion.OESTE) {
				pistola.disparar((int) x - 8, (int) y + 8, direccion, mundo,this); 
			}else if(this.direccion == Direccion.NORTE) {
				pistola.disparar((int) x + 8, (int) y - 8, direccion, mundo,this); 
			}else if(this.direccion == Direccion.ESTE) {
				pistola.disparar((int) x + 8, (int) y + 8, direccion, mundo,this); 
			}else if(this.direccion == Direccion.SUR) {
				pistola.disparar((int) x + 8, (int) y + 8, direccion, mundo,this); 
			}
		}else if(armaEquipada instanceof Desarmado) {
			
//			final Pistola pistola = (Pistola) armaEquipada;
			if(this.direccion == Direccion.OESTE) {
				this.ataqueMele((int) x+8, (int) y + 8, direccion, mundo); 
			}else if(this.direccion == Direccion.NORTE) {
				this.ataqueMele((int) x + 8, (int) y + 8, direccion, mundo); 
			}else if(this.direccion == Direccion.ESTE) {
				this.ataqueMele((int) x + 8, (int) y + 8, direccion, mundo); 
			}else if(this.direccion == Direccion.SUR) {
				this.ataqueMele((int) x + 8, (int) y + 8, direccion, mundo); 
			}
		}
	}
	
	private void ataqueMele(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo mundo) {
		final int alcanceAtaque = 12;
		final int anchoAtaque = 4;
		if(direccion == Direccion.OESTE) {
			mundo.crearProyectil(new GolpeMele(this.damage , false, mundo, xOrigen-alcanceAtaque, yOrigen, alcanceAtaque, anchoAtaque, direccion, this));
		}else if(direccion == Direccion.ESTE){
			mundo.crearProyectil(new GolpeMele(this.damage , false, mundo, xOrigen, yOrigen, alcanceAtaque, anchoAtaque, direccion, this));
		}else if(direccion == Direccion.NORTE) {
			mundo.crearProyectil(new GolpeMele(this.damage , false, mundo, xOrigen - (anchoAtaque/2), yOrigen - alcanceAtaque, anchoAtaque, alcanceAtaque, direccion, this));
		}else{
			mundo.crearProyectil(new GolpeMele(this.damage , false, mundo, xOrigen - (anchoAtaque/2), yOrigen, anchoAtaque, alcanceAtaque, direccion, this));
		}
		
	}

	private void atacar(final Criatura c, final Mundo esc) {
		c.recibirAtaque(this.damage,this);
		int x = c.getPosicionXInt() + (c.getRectangulo().width / 2);
		int y = this.getPosicionYInt() + (c.getRectangulo().height - 4);
		this.SONIDO_HIT_GOLPE.reproducir();
		esc.agregarParticula(new Sangre(x, y));
	}

	private void actualizarRecogidaItems() {
		if (!Constantes.TECLADO.TECLA_RECOGIENDO.presionado()) {
			return;
		}
		if (tilePisado == null) {
			return;
		}
		if (Constantes.TECLEO_RECOGIDA.transcurrioMiliSegundos(300)) {
			Constantes.TECLEO_RECOGIDA.establecerReferenciaTiempoActual();
		} else {
			return;
		}
		this.actualizarAreaRecoleccion();
		ArrayList<Item> listaItems = new ArrayList<Item>(mundo.getItemsIntersectados(this.areaRecoleccion)); 
		for (Item item : listaItems) {
			if (Constantes.INVENTARIO.agregarObjeto(item)) {
				if (item instanceof Consumible) {
					if (((Consumible) item).getCantidad() == 0) {
						item.eliminar();
					}
				} else {
					item.eliminar();
				}
			}
		}
	}

	private void actualizarArrojar() {
		if(Constantes.INVENTARIO.getSlotArrojadizo().contieneItem()) {
			this.meterEstado(Estado.ARROJANDO);
			if(Constantes.RATON.presionadoClickIzqUnicaAct()) {
				final Rectangle areaRaton = Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
				final Arrojadizo item = Constantes.INVENTARIO.getSlotArrojadizo().getItemArrojadizo();
				item.arrojar(areaRaton.x, areaRaton.y, direccion, this.mundo, this);
				Constantes.INVENTARIO.getSlotArrojadizo().eliminarObjeto();
			}else if(Constantes.RATON.presionadoClickDerUnicaAct()){
				Constantes.INVENTARIO.getSlotArrojadizo().eliminarObjeto();
			}
		}
		if(!Constantes.INVENTARIO.getSlotArrojadizo().contieneItem() && this.ESTADO.containsKey(Estado.ARROJANDO)) {
			this.sacarEstado(Estado.ARROJANDO);
		}
	}
	
	public String getStringEstados() {
		StringBuilder sb = new StringBuilder();
		for(Estado e : this.ESTADO.values()) {
			sb.append(e.toString() + "  ");
		}
		return sb.toString();
	}
	
		
	public void solicitarActualizacionAreaRecoleccionSinRecoger() {
		this.actualizarAreaRecoleccion();
	}
	public double getEstamina() {
		return this.estamina;
	}
	
	public double getDamage() {
		return this.damage;
	}
	
	public double getLimiteEstamina() {
		return this.maxEstamina;
	}
	
	private boolean gastarEstamina() {
		boolean puedeCorrer = false;
		if((this.estamina-this.PTS_CONSUMIR_ESTAMINA)>=0) {
			this.estamina-=this.PTS_CONSUMIR_ESTAMINA;
			puedeCorrer = true;
			this.GT_RECUPERACION_ESTAMINA.establecerReferenciaTiempoActual();
		}
		return puedeCorrer;
	}
	
	private void recuperarEstamina() {
		if(!this.ESTADO.containsKey(Estado.CORRIENDO) && this.GT_RECUPERACION_ESTAMINA.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_ESTAMINA)) {
			if(this.estamina< this.maxEstamina && (this.estamina+this.puntoRecuperarEstaminaXseg)<=maxEstamina) {
				// EN ESTOS CASOS TAMBIEN SE PODRIA USAR UN GESTOR_TIEMPO PARA TENER UN MEJOR CONTROL DE ESTAS COSAS COMO LA VELOCIDAD DE RECUPERACION.
				if(this.ESTADO.containsKey(Estado.CAMINANDO)) {
					this.estamina+=(this.puntoRecuperarEstaminaXseg/60)/2;
				}else {
					this.estamina+=(this.puntoRecuperarEstaminaXseg/60);
				}
				  
			}else {
				this.estamina = this.maxEstamina;
			}
		}
	}
	
	public Rectangle getAreaInteraccionCofre() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.LADO_INTERACCION_COFRE, this.LADO_INTERACCION_COFRE+3);
	}

	private void actualizarAreaRecoleccion() {
		this.areaRecoleccion = new Ellipse2D.Double(this.x - (this.recoleccionLado / 2) + (this.ANCHO / 2), this.y - (this.recoleccionLado / 2) + (this.ALTO / 2),
				this.recoleccionLado, this.recoleccionLado);
	}

	private void pintarAreaRecoleccion(final Graphics2D g) {
		this.actualizarAreaRecoleccion();
		DibujoDebug.dibujarFiguraEllipseRefCamara(g, new Rectangle(this.getPosicionXInt()- (recoleccionLado / 2) + (this.ANCHO / 2),this.getPosicionYInt() - (recoleccionLado / 2) + (this.ALTO / 2) ,this.recoleccionLado, this.recoleccionLado), Color.cyan);
	}
	
	private void pintarAreaArrojar(final Graphics2D g) {
		if(Constantes.INVENTARIO.getSlotArrojadizo().contieneItem()) {
			final Rectangle posRaton = Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			final Arrojadizo item = Constantes.INVENTARIO.getSlotArrojadizo().getItemArrojadizo();
			DibujoDebug.dibujarFiguraEllipseRefCamara(g, new Rectangle(posRaton.x-item.getDiamentroAreaCaida()/2, posRaton.y - item.getDiamentroAreaCaida()/2,item.getDiamentroAreaCaida() , item.getDiamentroAreaCaida()), Color.blue);
		}
	}
	
	public Shape getAreaDeteccion() {
		return this.areaRecoleccion;
	}

	private void actualizarMovimientos() {

		boolean enMovimiento = false;
		boolean corriendo = false;
		
		// movimiento en eje x e y
		establecerVelocidadStardar();
		if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
			
			if(Constantes.TECLADO.TECLA_ARRIBA.presionado() || Constantes.TECLADO.TECLA_ABAJO.presionado() ||
					Constantes.TECLADO.TECLA_DERECHA.presionado() || Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {
				if(this.gastarEstamina()) {
					velocidad = this.velocidadEstandar * 1.5;
					corriendo = true;
				}
			}
		}else {
			this.recuperarEstamina();
		}
		if (tilePisado != null) {
			velocidad += (ListaModeloTile.getModelo(tilePisado.getCodModelo()).getAlteracionVelocidad());
			if (velocidad < 0) {
				velocidad = 0;
			}

		}

		if (Constantes.TECLADO.TECLA_ARRIBA.presionado()) {
			
			if (!(((int) (this.y - velocidad)) < 0) && !(mundo.colisionaConObjetoSolido(getRectanguloInterseccionArriba((int) velocidad)))) {
				this.modificarPosicionY(-velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.NORTE;

		}
		if (Constantes.TECLADO.TECLA_ABAJO.presionado()) {
			
			if (!(this.y + velocidad > (mundo.getMapa().getAlto() - this.ALTO)) && !(mundo.colisionaConObjetoSolido(getRectanguloInterseccionAbajo((int) velocidad)))) {
				this.modificarPosicionY(velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.SUR;
		}

		if (Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {
			
			if (!(this.x - velocidad < 0) && !(mundo.colisionaConObjetoSolido(getRectanguloInterseccionIzquierda((int) velocidad)))) {
				this.modificarPosicionX(-velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.OESTE;
		}
		if (Constantes.TECLADO.TECLA_DERECHA.presionado()) {


			if (!(this.x + velocidad > (mundo.getMapa().getAncho() - this.ANCHO)) && !(mundo.colisionaConObjetoSolido(getRectanguloInterseccionDerecha((int) velocidad)))) {
				this.modificarPosicionX(velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.ESTE;
		}

		if(mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getRectanguloInterseccionGeneral())) {
			if(!this.atrasDeComplemento) {
				this.atrasDeComplemento = true;
			}
		}else {
			if(this.atrasDeComplemento) {
				this.atrasDeComplemento = false;
			}
		}
		if (corriendo) {
			this.meterEstado(Estado.CORRIENDO);
			this.sacarEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.ESTANDAR);
		}else {
			this.sacarEstado(Estado.CORRIENDO);
		}
		if (!enMovimiento) {
			this.meterEstado(Estado.ESTANDAR);
			this.sacarEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.CORRIENDO);
		} else if (!this.ESTADO.containsKey(Estado.CORRIENDO)) {
			this.meterEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.ESTANDAR);
			this.sacarEstado(Estado.CORRIENDO);
		}

	}
	
	public boolean pistolaEquipada() {
		return Constantes.INVENTARIO.getArmaEquipada() instanceof Arma && !(Constantes.INVENTARIO.getArmaEquipada() instanceof Desarmado); // cambiar Arma por pistola en un futuro
	}
	
	public Arma getArmaEquipada() {
		return (Arma) Constantes.INVENTARIO.getArmaEquipada();
	}

	@Override
	protected void pintarIndicadorVida(final Graphics2D g) {
		final int posicionX = MARGENX;
		final int posicionY = MARGENY;
		final Rectangle indicador = new Rectangle(posicionX - 1, posicionY - 5, this.ANCHO + 2, 4);
		final int porcentajeVida = (int) (this.vida * 100 / this.vidaMaxima);
		final int pocentajeBarraVidaActual = porcentajeVida * this.ANCHO / 100;
		final Rectangle barraVidaActual = new Rectangle(posicionX, posicionY - 4, pocentajeBarraVidaActual, 2);
		DibujoDebug.dibujarRectanguloRelleno(g, indicador, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, barraVidaActual, Color.RED);
		
		g.setFont(g.getFont().deriveFont(4f));
		DibujoDebug.dibujarString(g, String.valueOf(this.vida) + "/" + String.valueOf(this.vidaMaxima), posicionX, posicionY-6, Color.white);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	public int getDesplazamientoX() {
		return this.desplazamientoX;
	}

	public int getDesplazamientoY() {
		return this.desplazamientoY;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		this.x += desplazamientoX;
		this.desplazamientoX += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y += desplazamientoY;
		this.desplazamientoY += desplazamientoY;
	}

	public void establecerPosicion(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public int getPosicionXParado() {
		return (int) x + 3;
	}

	public int getPosicionYParado() {
		return (int) y + ALTO - 1;
	}

	public Point getPosicionParado() {
		return new Point((int) x + 3, (int) y + ALTO - 1);
	}

	public Point getPosicionTileParado() {
		return new Point((int) (x + 3) / Constantes.GLOBALES.ladoTile, (int) (y + ALTO - 1) / Constantes.GLOBALES.ladoTile);
	}

	public int getMargenX() {
		return MARGENX;
	}

	public int getMargenY() {
		return MARGENY;
	}


	@Override
	public Rectangle getRectangulo() {
		return new Rectangle((int) x, (int) y, ANCHO, ALTO);
	}

	public Rectangle getRectanguloInterseccionDerecha(final int velocidad) {
		return new Rectangle((int) x + ANCHO + velocidad - 3, (int) (y + (ALTO / 2)), 1, (ALTO / 2) - 1);
	}

	public Rectangle getRectanguloInterseccionIzquierda(final int velocidad) {
		return new Rectangle((int) x - velocidad + 2, (int) (y + (ALTO / 2)), 1, (ALTO / 2) - 1);
	}

	public Rectangle getRectanguloInterseccionArriba(final int velocidad) {
		return new Rectangle((int) x + 3, (int) y - velocidad - 1 + ((ALTO / 2)), ANCHO - 6, 1);
	}

	public Rectangle getRectanguloInterseccionAbajo(final int velocidad) {
		return new Rectangle((int) x + 3, (int) y + ALTO + velocidad-1, ANCHO - 6, 2);
	}
	public Rectangle getRectanguloInterseccionGeneral() {
		return new Rectangle((int)x -2,(int)y - 1 + (ALTO / 2), ANCHO-3,ALTO/2 -2);
	}

	@Override
	public Point getPosicionTile() {
		return new Point((int) this.x / Constantes.GLOBALES.ladoTile, (int) this.y / Constantes.GLOBALES.ladoTile);
	}

	public String getVelocidad() {
		return String.format("%.2f", this.velocidad);
	}


	public HashMap<Estado, Estado> getEstado() {
		return this.ESTADO;
	}

	@Override
	public void recibirAtaque(double damage, final Ente causante) {
		this.reducirVida(damage);
		super.recibirAtaque(damage, causante);
	}

	@Override
	public JSONObject exportarParaJSON() {
		return null;
	}
	
	@Override
	public  String exportarTipoCriatura() {
		return "Player";
	}

	public void reducirVida(double damage) {
		super.reducirVida(damage);
		if(this.eliminado) {
			
		}
	}

	public void restablecer(final Mundo mundo) {
		this.eliminado = false;
		this.establecerVidaMaxima(PTS_VIDAMAX_BASE);
		this.sanar();
		this.damage = PTS_DAMAGE_BASE;
		this.mundo = mundo;
		Constantes.INVENTARIO.vaciar();
		
	}

}
