package principal.entes.objetos.fabricables;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.animaciones.objetos.AnimacionesFogata;
import principal.clima.GestorZonasAmbiente;
import principal.clima.TipoClima;
import principal.crafteo.EstacionCrafteo;
import principal.crafteo.EstacionInteractiva;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.iluminacion.TipoLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.interaccion.Interactuable;
import principal.inventario.Inventario;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Entidad física de Fogata que hereda de ObjetoFabricable (Zero-GC / O(1)).
 * 
 * @version 3.0 (Vanilla Java 8 - ObjetoFabricable Architecture)
 */
public class Fogata extends ObjetoFabricable implements EstacionInteractiva, Interactuable {

	private static final long serialVersionUID = 1L;

	public static final int CAPACIDAD_MAX_MADERA = 10;
	public static final double SEGUNDOS_POR_MADERA = 60.0;
	public static final double VIDA_FOGATA = 30.0;
	public static final String COD_ANILLO_INFUSION = "Anillo de Oro";

	private int maderaAlmacenada;
	private double tiempoCombustibleRestante;
	private double tiempoResistenciaLluvia = 5.0;
	private boolean encendida;
	private boolean fuegoAzul;
	private int estadoVisual;

	private final AnimacionesFogata animaciones;
	private final GestorTiempo GT_DANIO_CONTACTO;
	private final GestorTiempo GT_AURA_REGEN;
	private final Rectangle areaAuraCurativa = new Rectangle();
	private final Rectangle areaContactoFuego = new Rectangle();
	private final int radio;

	private final AccionEntidad<Criatura> accionQuemarContacto = new AccionEntidad<Criatura>() {
		@Override
		public void ejecutar(final Criatura victima) {
			if (!victima.estaEliminado() && victima.getArea().intersects(Fogata.this.areaContactoFuego)) {
				victima.recibirDanioDirecto(1.0);
				victima.aplicarEfecto(TipoEfectoEstado.QUEMADURA, 2.5, 1.0);
				Globales.GESTOR_PARTICULAS.emitirExplosion(victima.getCentroX(), victima.getCentroY(), 2);
			}
		}
	};

	private final AccionEntidad<Criatura> accionAuraCurativa = new AccionEntidad<Criatura>() {
		@Override
		public void ejecutar(final Criatura criatura) {
			if (!criatura.estaEliminado()) {
				criatura.aplicarEfecto(TipoEfectoEstado.REGENERACION, 2.5, 1.5);
				Globales.GESTOR_PARTICULAS.emitirMagia(criatura.getCentroX(), criatura.getCentroY(), 1);
			}
		}
	};

	public Fogata(final int x, final int y, final int maderaInicial, final boolean encendida, final boolean fuegoAzul,
			final Ente propietario) {
		super(x, y, VIDA_FOGATA, propietario);
		this.maderaAlmacenada = Math.max(0, Math.min(CAPACIDAD_MAX_MADERA, maderaInicial));
		this.encendida = encendida && (this.maderaAlmacenada > 0);
		this.fuegoAzul = fuegoAzul;
		this.tiempoCombustibleRestante = this.encendida ? SEGUNDOS_POR_MADERA : 0.0;

		this.animaciones = new AnimacionesFogata();
		this.GT_DANIO_CONTACTO = new GestorTiempo();
		this.GT_AURA_REGEN = new GestorTiempo();

		this.actualizarEstadoVisual();
		this.radio = 45;
	}

	public Fogata(final int x, final int y, final int maderaInicial, final boolean encendida, final boolean fuegoAzul) {
		this(x, y, maderaInicial, encendida, fuegoAzul, Globales.JUGADOR);
	}

	public Fogata(final int x, final int y) {
		this(x, y, 2, true, false, Globales.JUGADOR);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		this.actualizarLuz();

		if (this.encendida) {
			this.actualizarCombustion(dt);
			this.actualizarDanioContacto();

			if (this.fuegoAzul && (this.mundo != null)) {
				if (this.GT_AURA_REGEN.transcurrioMiliSegundos(1000)) {
					this.GT_AURA_REGEN.establecerReferenciaTiempoActual();
					final int r = 64;
					this.areaAuraCurativa.setBounds(this.getCentroX() - r, this.getCentroY() - r, r * 2, r * 2);
					this.mundo.paraCadaCriaturaEn(this.areaAuraCurativa, true, this.accionAuraCurativa);
				}
			}
		}

		if (this.estadoVisual == AnimacionesFogata.APAGADA_HUMO) {
			if (this.animaciones.animacionFinalizada(AnimacionesFogata.APAGADA_HUMO)) {
				this.estadoVisual = AnimacionesFogata.BASE;
			}
		}

		this.animaciones.actualizar(this.estadoVisual);
	}

	private void actualizarCombustion(final double dt) {
		boolean enInterior = false;
		final GestorZonasAmbiente zonas = Globales.GESTOR_ZONAS_AMBIENTE;
		if (zonas != null) {
			final int totalZonas = zonas.getCantidadZonas();
			for (int i = 0; i < totalZonas; i++) {
				final ZonaAmbiente z = zonas.getZonaPorIndice(i);
				if ((z != null) && z.isEsInterior() && z.contiene(this.getCentroX(), this.getCentroY())) {
					enInterior = true;
					break;
				}
			}
		}

		if (!this.fuegoAzul && !enInterior && (Globales.GESTOR_CLIMA != null)) {
			final TipoClima clima = Globales.GESTOR_CLIMA.getClimaActual();
			double velocidadSofocacion = 0.0;

			switch (clima) {
			case LLUVIA_TORMENTA:
			case LLUVIA_ACIDA:
				velocidadSofocacion = 3.0;
				break;
			case VENTISCA:
				velocidadSofocacion = 2.0;
				break;
			case LLUVIA_LEVE:
				velocidadSofocacion = 1.0;
				break;
			case NIEVE:
				velocidadSofocacion = 0.6;
				break;
			default:
				this.tiempoResistenciaLluvia = 5.0;
				break;
			}

			if (velocidadSofocacion > 0.0) {
				this.tiempoResistenciaLluvia -= (dt * velocidadSofocacion);
				if ((Globales.animacion % 15) == 0) {
					Globales.GESTOR_PARTICULAS.emitirExplosion(this.getCentroX(), this.getCentroY(), 1);
				}

				if (this.tiempoResistenciaLluvia <= 0.0) {
					this.tiempoResistenciaLluvia = 5.0;
					this.apagar();
					if (this.mundo != null) {
						Globales.GESTOR_TEXTOS.agregarTexto("¡Apagada por la lluvia!", this.getCentroX(),
								this.getPosicionYInt() - 8, principal.igu.textos.TipoTextoFlotante.BLOQUEO);
					}
					return;
				}
			}
		}

		this.tiempoCombustibleRestante -= dt;
		if (this.tiempoCombustibleRestante <= 0.0) {
			this.maderaAlmacenada--;
			if (this.maderaAlmacenada > 0) {
				this.tiempoCombustibleRestante = SEGUNDOS_POR_MADERA;
			} else {
				this.maderaAlmacenada = 0;
				this.tiempoCombustibleRestante = 0.0;
				this.apagar();
			}
		}
	}

	private void actualizarDanioContacto() {
		if ((this.mundo != null) && this.GT_DANIO_CONTACTO.transcurrioMiliSegundos(800)) {
			this.GT_DANIO_CONTACTO.establecerReferenciaTiempoActual();
			this.areaContactoFuego.setBounds(this.getPosicionXInt() - 2, this.getPosicionYInt() - 2,
					this.getAncho() + 4, this.getAlto() + 4);
			this.mundo.paraCadaCriaturaEn(this.areaContactoFuego, true, this.accionQuemarContacto);
		}
	}

	private void actualizarLuz() {
		if (this.encendida) {
			final TipoLuz tipoDeseado = this.fuegoAzul ? TipoLuz.FOGATA_AZUL : TipoLuz.FOGATA;
			if ((this.luzAsignada == null) || !this.luzAsignada.isActiva()) {
				if (Globales.GESTOR_LUZ != null) {
					this.luzAsignada = Globales.GESTOR_LUZ.agregarLuzAnclada(this, tipoDeseado, this.radio);
				}
			} else if (this.luzAsignada.getTipo() != tipoDeseado) {
				this.luzAsignada.setTipo(tipoDeseado);
			}
		} else if (this.luzAsignada != null) {
			this.desvincularLuz();
		}
	}

	private void actualizarEstadoVisual() {
		if (this.encendida) {
			this.estadoVisual = this.fuegoAzul ? AnimacionesFogata.FUEGO_AZUL : AnimacionesFogata.ENCENDIDA;
		} else {
			this.estadoVisual = AnimacionesFogata.BASE;
		}
	}

	public void encender(final boolean fuegoAzul) {
		if (this.maderaAlmacenada <= 0) {
			return;
		}
		this.encendida = true;
		this.fuegoAzul = fuegoAzul;
		if (this.tiempoCombustibleRestante <= 0.0) {
			this.tiempoCombustibleRestante = SEGUNDOS_POR_MADERA;
		}
		this.actualizarEstadoVisual();
		this.animaciones.reiniciar(this.estadoVisual);
		GestorSonido.reproducir(IDSonido.SELECT);
	}

	public void apagar() {
		if (!this.encendida && (this.estadoVisual == AnimacionesFogata.BASE)) {
			return;
		}
		this.encendida = false;
		this.estadoVisual = AnimacionesFogata.APAGADA_HUMO;
		this.animaciones.reiniciar(AnimacionesFogata.APAGADA_HUMO);
		this.desvincularLuz();
		GestorSonido.reproducir(IDSonido.SELECT);
	}

	public boolean agregarMadera(final int cantidad) {
		if ((cantidad <= 0) || (this.maderaAlmacenada >= CAPACIDAD_MAX_MADERA)) {
			return false;
		}
		this.maderaAlmacenada = Math.min(CAPACIDAD_MAX_MADERA, this.maderaAlmacenada + cantidad);
		if (this.encendida && (this.tiempoCombustibleRestante <= 0.0)) {
			this.tiempoCombustibleRestante = SEGUNDOS_POR_MADERA;
		}
		return true;
	}

	@Override
	protected void alDestruir(final Ente causante) {
		if ((this.mundo != null) && (this.maderaAlmacenada > 0)) {
			this.mundo.meterEntidad(
					RecursoMaterial.crearMadera(this.getPosicionXInt(), this.getPosicionYInt(), this.maderaAlmacenada));
		}
	}

	@Override
	public String getTextoPrompt() {
		final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
		final Inventario inv = (Globales.GESTOR_INVENTARIO != null) ? Globales.GESTOR_INVENTARIO.getInventarioJugador()
				: null;

		final int maderaJugador = (inv != null) ? inv.contarItemGenericoTotal(RecursoMaterial.COD_MADERA) : 0;
		final boolean tieneAnillo = (inv != null) && (inv.contarItemGenericoTotal(COD_ANILLO_INFUSION) > 0);

		if (this.encendida) {
			if (!this.fuegoAzul && tieneAnillo) {
				return "Infundir Fuego Místico";
			}
			if (shift || (maderaJugador <= 0) || (this.maderaAlmacenada >= CAPACIDAD_MAX_MADERA)) {
				return "Apagar";
			}
			return "Leña (" + this.maderaAlmacenada + "/" + CAPACIDAD_MAX_MADERA + ")";
		}

		if (this.maderaAlmacenada > 0) {
			return "Encender (" + this.maderaAlmacenada + ")";
		}
		if (maderaJugador > 0) {
			return "Poner Leña y Encender";
		}
		return "Sin Combustible";
	}

	@Override
	public void interactuar(final Jugador jugador) {
		final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
		final Inventario inv = (Globales.GESTOR_INVENTARIO != null) ? Globales.GESTOR_INVENTARIO.getInventarioJugador()
				: null;

		if (this.encendida) {
			if (!this.fuegoAzul && (inv != null) && (inv.contarItemGenericoTotal(COD_ANILLO_INFUSION) > 0)) {
				inv.extraerItemGenerico(COD_ANILLO_INFUSION, 1);
				this.setFuegoAzul(true);
				GestorSonido.reproducir(IDSonido.SELECT);
				Globales.GESTOR_PARTICULAS.emitirMagia(this.getCentroX(), this.getCentroY(), 15);
				Globales.GESTOR_TEXTOS.agregarTexto("¡Fuego Místico Despertado!", this.getCentroX(),
						this.getPosicionYInt() - 8, principal.igu.textos.TipoTextoFlotante.MANA);
				return;
			}

			if (shift) {
				this.apagar();
				return;
			}

			if ((inv != null) && (this.maderaAlmacenada < CAPACIDAD_MAX_MADERA)) {
				if (inv.contarItemGenericoTotal(RecursoMaterial.COD_MADERA) > 0) {
					inv.extraerItemGenerico(RecursoMaterial.COD_MADERA, 1);
					this.agregarMadera(1);
					GestorSonido.reproducir(IDSonido.SELECT);
					Globales.GESTOR_TEXTOS.agregarTexto("+1 Leña", this.getCentroX(), this.getPosicionYInt() - 8,
							principal.igu.textos.TipoTextoFlotante.ESTADO);
				} else {
					this.apagar();
				}
			} else {
				this.apagar();
			}
		} else if (this.maderaAlmacenada > 0) {
			this.encender(this.fuegoAzul);
		} else if ((inv != null) && (inv.contarItemGenericoTotal(RecursoMaterial.COD_MADERA) > 0)) {
			inv.extraerItemGenerico(RecursoMaterial.COD_MADERA, 1);
			this.maderaAlmacenada = 1;
			this.encender(this.fuegoAzul);
			Globales.GESTOR_TEXTOS.agregarTexto("+1 Leña", this.getCentroX(), this.getPosicionYInt() - 8,
					principal.igu.textos.TipoTextoFlotante.ESTADO);
		} else {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
		}
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.eliminado;
	}

	@Override
	public EstacionCrafteo getTipoEstacion() {
		return this.encendida ? EstacionCrafteo.FOGATA : null;
	}

	@Override
	public void pintar(final Graphics2D g) {
		final boolean enFlash = this.estaEnFlashDanio();
		this.animaciones.pintar(g, this.getPosicionXInt(), this.getPosicionYInt(), this.estadoVisual, false, true,
				enFlash);
		this.pintarIndicadorVida(g);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("madera", Integer.valueOf(this.maderaAlmacenada));
		json.put("encendida", Boolean.valueOf(this.encendida));
		json.put("fuegoAzul", Boolean.valueOf(this.fuegoAzul));
		json.put("vida", Double.valueOf(this.vida));
		return json;
	}

	public static Fogata crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Fogata(0, 0);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final int madera = (json.get("madera") != null) ? ((Number) json.get("madera")).intValue() : 2;
		final boolean encendida = (json.get("encendida") != null) ? ((Boolean) json.get("encendida")).booleanValue()
				: true;
		final boolean fuegoAzul = (json.get("fuegoAzul") != null) ? ((Boolean) json.get("fuegoAzul")).booleanValue()
				: false;
		final double vida = (json.get("vida") != null) ? ((Number) json.get("vida")).doubleValue() : VIDA_FOGATA;

		final Fogata f = new Fogata(x, y, madera, encendida, fuegoAzul, Globales.JUGADOR);
		f.setVida(vida);
		return f;
	}

	@Override
	public int getAncho() {
		return Constantes.LADO_TILE;
	}

	@Override
	public int getAlto() {
		return Constantes.LADO_TILE;
	}

	@Override
	public BufferedImage getTextura() {
		return this.animaciones.getAnimacion(this.estadoVisual) != null
				? Globales.GESTOR_TEXTURAS.getHoja(principal.recursos.ClaveHoja.FOGATA).getSprite(0)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		final Fogata copia = new Fogata(this.getPosicionXInt(), this.getPosicionYInt(), this.maderaAlmacenada,
				this.encendida, this.fuegoAzul, this.propietario);
		copia.setVida(this.vida);
		return copia;
	}

	public boolean isEncendida() {
		return this.encendida;
	}

	public boolean isFuegoAzul() {
		return this.fuegoAzul;
	}

	public void setFuegoAzul(final boolean fuegoAzul) {
		this.fuegoAzul = fuegoAzul;
		if (this.encendida) {
			this.actualizarEstadoVisual();
			this.actualizarLuz();
		}
	}

	public int getMaderaAlmacenada() {
		return this.maderaAlmacenada;
	}

	public double getTiempoCombustibleRestante() {
		return this.tiempoCombustibleRestante;
	}
}