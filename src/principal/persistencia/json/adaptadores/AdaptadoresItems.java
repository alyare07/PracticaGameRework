package principal.persistencia.json.adaptadores;

import org.json.simple.JSONObject;

import principal.entes.objetos.items.Antorcha;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.comidas.BayaSilvestre;
import principal.entes.objetos.items.comidas.CarnePolloCocida;
import principal.entes.objetos.items.comidas.CarnePolloCruda;
import principal.entes.objetos.items.comidas.CuencoAguaHervida;
import principal.entes.objetos.items.comidas.CuencoAguaSucia;
import principal.entes.objetos.items.comidas.CuencoVacio;
import principal.entes.objetos.items.desplegables.KitCama;
import principal.entes.objetos.items.desplegables.KitCarpa;
import principal.entes.objetos.items.desplegables.KitFogata;
import principal.entes.objetos.items.desplegables.KitFogataAzul;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoAislamiento;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.monedas.ItemMoneda;
import principal.entes.objetos.items.monedas.TipoMoneda;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.mapa.Mundo;
import principal.persistencia.json.AdaptadorEntidad;
import principal.persistencia.json.LectorJSON;

/**
 * Adaptadores universales para todo el catálogo de ítems RPG.
 */
public final class AdaptadoresItems {

	private AdaptadoresItems() {
	}

	@SuppressWarnings("unchecked")
	private static void serializarItemBase(final Item i, final JSONObject json) {
		json.put("x", Integer.valueOf(i.getPosicionXInt()));
		json.put("y", Integer.valueOf(i.getPosicionYInt()));
		json.put("precio", Long.valueOf(i.getPrecioBasePlata()));
	}

	public static class AdaptadorItemMoneda implements AdaptadorEntidad<ItemMoneda> {
		@Override
		public String getId() {
			return "MONEDA";
		}

		@Override
		public Class<ItemMoneda> getClaseEntidad() {
			return ItemMoneda.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final ItemMoneda m) {
			final JSONObject json = new JSONObject();
			serializarItemBase(m, json);
			json.put("tipoMoneda", m.getTipo().name());
			json.put("valorPlata", Long.valueOf(m.getValorPlata()));
			return json;
		}

		@Override
		public ItemMoneda deserializar(final JSONObject d, final Mundo mundo) {
			final ItemMoneda m = ItemMoneda.crearDesdeJson(d);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorPistola implements AdaptadorEntidad<Pistola> {
		@Override
		public String getId() {
			return "PISTOLA";
		}

		@Override
		public Class<Pistola> getClaseEntidad() {
			return Pistola.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Pistola p) {
			final JSONObject json = new JSONObject();
			serializarItemBase(p, json);
			json.put("codModelo", p.getCodigoModelo());
			json.put("balasCargador", Integer.valueOf(p.getBalasCargador()));
			return json;
		}

		@Override
		public Pistola deserializar(final JSONObject d, final Mundo mundo) {
			final Pistola p = Pistola.crearDesdeJson(d);
			p.setMundo(mundo);
			return p;
		}
	}

	public static class AdaptadorEscopetaRecortada implements AdaptadorEntidad<EscopetaRecortada> {
		@Override
		public String getId() {
			return "ESCOPETA_RECORTADA";
		}

		@Override
		public Class<EscopetaRecortada> getClaseEntidad() {
			return EscopetaRecortada.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final EscopetaRecortada e) {
			final JSONObject json = new JSONObject();
			serializarItemBase(e, json);
			json.put("balasCargador", Integer.valueOf(e.getBalasCargador()));
			return json;
		}

		@Override
		public EscopetaRecortada deserializar(final JSONObject d, final Mundo mundo) {
			final EscopetaRecortada e = EscopetaRecortada.crearDesdeJson(d);
			e.setMundo(mundo);
			return e;
		}
	}

	public static class AdaptadorEscopetaTactica implements AdaptadorEntidad<EscopetaTactica> {
		@Override
		public String getId() {
			return "ESCOPETA_TACTICA";
		}

		@Override
		public Class<EscopetaTactica> getClaseEntidad() {
			return EscopetaTactica.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final EscopetaTactica e) {
			final JSONObject json = new JSONObject();
			serializarItemBase(e, json);
			json.put("balasCargador", Integer.valueOf(e.getBalasCargador()));
			return json;
		}

		@Override
		public EscopetaTactica deserializar(final JSONObject d, final Mundo mundo) {
			final EscopetaTactica e = EscopetaTactica.crearDesdeJson(d);
			e.setMundo(mundo);
			return e;
		}
	}

	public static class AdaptadorEscopetaAutomatica implements AdaptadorEntidad<EscopetaAutomatica> {
		@Override
		public String getId() {
			return "ESCOPETA_AUTOMATICA";
		}

		@Override
		public Class<EscopetaAutomatica> getClaseEntidad() {
			return EscopetaAutomatica.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final EscopetaAutomatica e) {
			final JSONObject json = new JSONObject();
			serializarItemBase(e, json);
			json.put("balasCargador", Integer.valueOf(e.getBalasCargador()));
			return json;
		}

		@Override
		public EscopetaAutomatica deserializar(final JSONObject d, final Mundo mundo) {
			final EscopetaAutomatica e = EscopetaAutomatica.crearDesdeJson(d);
			e.setMundo(mundo);
			return e;
		}
	}

	public static class AdaptadorSubfusilLigero implements AdaptadorEntidad<SubfusilLigero> {
		@Override
		public String getId() {
			return "SUBFUSIL_LIGERO";
		}

		@Override
		public Class<SubfusilLigero> getClaseEntidad() {
			return SubfusilLigero.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final SubfusilLigero s) {
			final JSONObject json = new JSONObject();
			serializarItemBase(s, json);
			json.put("balasCargador", Integer.valueOf(s.getBalasCargador()));
			return json;
		}

		@Override
		public SubfusilLigero deserializar(final JSONObject d, final Mundo mundo) {
			final SubfusilLigero s = SubfusilLigero.crearDesdeJson(d);
			s.setMundo(mundo);
			return s;
		}
	}

	public static class AdaptadorRifleAsalto implements AdaptadorEntidad<RifleAsalto> {
		@Override
		public String getId() {
			return "RIFLE_ASALTO";
		}

		@Override
		public Class<RifleAsalto> getClaseEntidad() {
			return RifleAsalto.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final RifleAsalto r) {
			final JSONObject json = new JSONObject();
			serializarItemBase(r, json);
			json.put("balasCargador", Integer.valueOf(r.getBalasCargador()));
			return json;
		}

		@Override
		public RifleAsalto deserializar(final JSONObject d, final Mundo mundo) {
			final RifleAsalto r = RifleAsalto.crearDesdeJson(d);
			r.setMundo(mundo);
			return r;
		}
	}

	public static class AdaptadorAmetralladoraPesada implements AdaptadorEntidad<AmetralladoraPesada> {
		@Override
		public String getId() {
			return "AMETRALLADORA_PESADA";
		}

		@Override
		public Class<AmetralladoraPesada> getClaseEntidad() {
			return AmetralladoraPesada.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final AmetralladoraPesada a) {
			final JSONObject json = new JSONObject();
			serializarItemBase(a, json);
			json.put("balasCargador", Integer.valueOf(a.getBalasCargador()));
			return json;
		}

		@Override
		public AmetralladoraPesada deserializar(final JSONObject d, final Mundo mundo) {
			final AmetralladoraPesada a = AmetralladoraPesada.crearDesdeJson(d);
			a.setMundo(mundo);
			return a;
		}
	}

	public static class AdaptadorHerramienta implements AdaptadorEntidad<Herramienta> {
		@Override
		public String getId() {
			return "HERRAMIENTA";
		}

		@Override
		public Class<Herramienta> getClaseEntidad() {
			return Herramienta.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Herramienta h) {
			final JSONObject json = new JSONObject();
			serializarItemBase(h, json);
			json.put("codModelo", h.getCodigoModelo());
			json.put("damage", Integer.valueOf(h.getAtaque()));
			json.put("alcance", Integer.valueOf(h.getAlcance()));
			json.put("cadencia", Integer.valueOf(h.getCadenciaMs()));
			json.put("tipoHerramienta", h.getTipoHerramienta().name());
			json.put("potencia", Double.valueOf(h.getPotenciaCosecha()));
			json.put("durabilidadMax", Integer.valueOf(h.getDurabilidadMaxima()));
			json.put("durabilidad", Integer.valueOf(h.getDurabilidadActual()));
			return json;
		}

		@Override
		public Herramienta deserializar(final JSONObject d, final Mundo mundo) {
			final Herramienta h = Herramienta.crearDesdeJson(d);
			h.setMundo(mundo);
			return h;
		}
	}

	public static class AdaptadorPiezaEquipo implements AdaptadorEntidad<PiezaEquipo> {
		@Override
		public String getId() {
			return "PIEZA_EQUIPO";
		}

		@Override
		public Class<PiezaEquipo> getClaseEntidad() {
			return PiezaEquipo.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final PiezaEquipo p) {
			final JSONObject json = new JSONObject();
			serializarItemBase(p, json);
			json.put("codModelo", p.getCodigoModelo());
			json.put("tipoEquipo", p.getTipoEquipo().name());
			json.put("fuerza", Integer.valueOf(p.getBonifFuerza()));
			json.put("agilidad", Integer.valueOf(p.getBonifAgilidad()));
			json.put("inteligencia", Integer.valueOf(p.getBonifInteligencia()));
			json.put("defensa", Integer.valueOf(p.getArmaduraDefensa()));
			json.put("tipoAislamiento", p.getTipoAislamiento().name());
			json.put("valorAislamiento", Integer.valueOf(p.getValorAislamiento()));
			return json;
		}

		@Override
		public PiezaEquipo deserializar(final JSONObject d, final Mundo mundo) {
			final PiezaEquipo p = PiezaEquipo.crearDesdeJson(d);
			p.setMundo(mundo);
			return p;
		}
	}

	public static class AdaptadorAntorcha implements AdaptadorEntidad<Antorcha> {
		@Override
		public String getId() {
			return "ANTORCHA";
		}

		@Override
		public Class<Antorcha> getClaseEntidad() {
			return Antorcha.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Antorcha a) {
			final JSONObject json = new JSONObject();
			serializarItemBase(a, json);
			return json;
		}

		@Override
		public Antorcha deserializar(final JSONObject d, final Mundo mundo) {
			final Antorcha a = new Antorcha(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			a.setMundo(mundo);
			return a;
		}
	}

	public static class AdaptadorGranadaT1 implements AdaptadorEntidad<GranadaT1> {
		@Override
		public String getId() {
			return "GRANADA_T1";
		}

		@Override
		public Class<GranadaT1> getClaseEntidad() {
			return GranadaT1.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final GranadaT1 g) {
			final JSONObject json = new JSONObject();
			serializarItemBase(g, json);
			json.put("cant", Integer.valueOf(g.getCantidad()));
			return json;
		}

		@Override
		public GranadaT1 deserializar(final JSONObject d, final Mundo mundo) {
			final GranadaT1 g = GranadaT1.crearDesdeJson(d);
			g.setMundo(mundo);
			return g;
		}
	}

	public static class AdaptadorPocionVidaMenor implements AdaptadorEntidad<PocionVidaMenor> {
		@Override
		public String getId() {
			return "POCION_VIDA_MENOR";
		}

		@Override
		public Class<PocionVidaMenor> getClaseEntidad() {
			return PocionVidaMenor.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final PocionVidaMenor p) {
			final JSONObject json = new JSONObject();
			serializarItemBase(p, json);
			json.put("cant", Integer.valueOf(p.getCantidad()));
			return json;
		}

		@Override
		public PocionVidaMenor deserializar(final JSONObject d, final Mundo mundo) {
			final PocionVidaMenor p = PocionVidaMenor.crearDesdeJson(d);
			p.setMundo(mundo);
			return p;
		}
	}

	public static class AdaptadorCajaMunicion implements AdaptadorEntidad<CajaMunicion> {
		@Override
		public String getId() {
			return "CAJA_MUNICION";
		}

		@Override
		public Class<CajaMunicion> getClaseEntidad() {
			return CajaMunicion.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CajaMunicion c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("codModelo", c.getCodigoModelo());
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			return json;
		}

		@Override
		public CajaMunicion deserializar(final JSONObject d, final Mundo mundo) {
			final CajaMunicion c = CajaMunicion.crearDesdeJson(d);
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorRecursoMaterial implements AdaptadorEntidad<RecursoMaterial> {
		@Override
		public String getId() {
			return "RECURSO_MATERIAL";
		}

		@Override
		public Class<RecursoMaterial> getClaseEntidad() {
			return RecursoMaterial.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final RecursoMaterial r) {
			final JSONObject json = new JSONObject();
			serializarItemBase(r, json);
			json.put("codModelo", r.getCodigoModelo());
			json.put("cant", Integer.valueOf(r.getCantidad()));
			return json;
		}

		@Override
		public RecursoMaterial deserializar(final JSONObject d, final Mundo mundo) {
			final RecursoMaterial r = RecursoMaterial.crearDesdeJson(d);
			r.setMundo(mundo);
			return r;
		}
	}

	public static class AdaptadorBayaSilvestre implements AdaptadorEntidad<BayaSilvestre> {
		@Override
		public String getId() {
			return "BAYA_SILVESTRE";
		}

		@Override
		public Class<BayaSilvestre> getClaseEntidad() {
			return BayaSilvestre.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final BayaSilvestre b) {
			final JSONObject json = new JSONObject();
			serializarItemBase(b, json);
			json.put("cantidad", Integer.valueOf(b.getCantidad()));
			json.put("horaCaducidad", Double.valueOf(b.getHoraCaducidad()));
			json.put("podrido", Boolean.valueOf(b.isPodrido()));
			return json;
		}

		@Override
		public BayaSilvestre deserializar(final JSONObject d, final Mundo mundo) {
			final BayaSilvestre b = BayaSilvestre.crearDesdeJson(d);
			b.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			b.setMundo(mundo);
			return b;
		}
	}

	public static class AdaptadorCarnePolloCruda implements AdaptadorEntidad<CarnePolloCruda> {
		@Override
		public String getId() {
			return "POLLO_CRUDO";
		}

		@Override
		public Class<CarnePolloCruda> getClaseEntidad() {
			return CarnePolloCruda.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CarnePolloCruda c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			json.put("horaCaducidad", Double.valueOf(c.getHoraCaducidad()));
			json.put("podrido", Boolean.valueOf(c.isPodrido()));
			return json;
		}

		@Override
		public CarnePolloCruda deserializar(final JSONObject d, final Mundo mundo) {
			final CarnePolloCruda c = CarnePolloCruda.crearDesdeJson(d);
			c.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCarnePolloCocida implements AdaptadorEntidad<CarnePolloCocida> {
		@Override
		public String getId() {
			return "POLLO_COCIDO";
		}

		@Override
		public Class<CarnePolloCocida> getClaseEntidad() {
			return CarnePolloCocida.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CarnePolloCocida c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			json.put("horaCaducidad", Double.valueOf(c.getHoraCaducidad()));
			json.put("podrido", Boolean.valueOf(c.isPodrido()));
			return json;
		}

		@Override
		public CarnePolloCocida deserializar(final JSONObject d, final Mundo mundo) {
			final CarnePolloCocida c = CarnePolloCocida.crearDesdeJson(d);
			c.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCuencoVacio implements AdaptadorEntidad<CuencoVacio> {
		@Override
		public String getId() {
			return "CUENCO_VACIO";
		}

		@Override
		public Class<CuencoVacio> getClaseEntidad() {
			return CuencoVacio.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CuencoVacio c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			return json;
		}

		@Override
		public CuencoVacio deserializar(final JSONObject d, final Mundo mundo) {
			final CuencoVacio c = CuencoVacio.crearDesdeJson(d);
			c.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCuencoAguaSucia implements AdaptadorEntidad<CuencoAguaSucia> {
		@Override
		public String getId() {
			return "CUENCO_AGUA_SUCIA";
		}

		@Override
		public Class<CuencoAguaSucia> getClaseEntidad() {
			return CuencoAguaSucia.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CuencoAguaSucia c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			json.put("horaCaducidad", Double.valueOf(c.getHoraCaducidad()));
			json.put("podrido", Boolean.valueOf(c.isPodrido()));
			return json;
		}

		@Override
		public CuencoAguaSucia deserializar(final JSONObject d, final Mundo mundo) {
			final CuencoAguaSucia c = CuencoAguaSucia.crearDesdeJson(d);
			c.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCuencoAguaHervida implements AdaptadorEntidad<CuencoAguaHervida> {
		@Override
		public String getId() {
			return "CUENCO_AGUA_HERVIDA";
		}

		@Override
		public Class<CuencoAguaHervida> getClaseEntidad() {
			return CuencoAguaHervida.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CuencoAguaHervida c) {
			final JSONObject json = new JSONObject();
			serializarItemBase(c, json);
			json.put("cantidad", Integer.valueOf(c.getCantidad()));
			json.put("horaCaducidad", Double.valueOf(c.getHoraCaducidad()));
			json.put("podrido", Boolean.valueOf(c.isPodrido()));
			return json;
		}

		@Override
		public CuencoAguaHervida deserializar(final JSONObject d, final Mundo mundo) {
			final CuencoAguaHervida c = CuencoAguaHervida.crearDesdeJson(d);
			c.setPosicion(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorKitFogata implements AdaptadorEntidad<KitFogata> {
		@Override
		public String getId() {
			return "KIT_FOGATA";
		}

		@Override
		public Class<KitFogata> getClaseEntidad() {
			return KitFogata.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final KitFogata k) {
			final JSONObject json = new JSONObject();
			serializarItemBase(k, json);
			json.put("cant", Integer.valueOf(k.getCantidad()));
			return json;
		}

		@Override
		public KitFogata deserializar(final JSONObject d, final Mundo mundo) {
			final KitFogata k = KitFogata.crearDesdeJson(d);
			k.setMundo(mundo);
			return k;
		}
	}

	public static class AdaptadorKitFogataAzul implements AdaptadorEntidad<KitFogataAzul> {
		@Override
		public String getId() {
			return "KIT_FOGATA_AZUL";
		}

		@Override
		public Class<KitFogataAzul> getClaseEntidad() {
			return KitFogataAzul.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final KitFogataAzul k) {
			final JSONObject json = new JSONObject();
			serializarItemBase(k, json);
			json.put("cant", Integer.valueOf(k.getCantidad()));
			return json;
		}

		@Override
		public KitFogataAzul deserializar(final JSONObject d, final Mundo mundo) {
			final KitFogataAzul k = KitFogataAzul.crearDesdeJson(d);
			k.setMundo(mundo);
			return k;
		}
	}

	public static class AdaptadorKitCarpa implements AdaptadorEntidad<KitCarpa> {
		@Override
		public String getId() {
			return "KIT_CARPA";
		}

		@Override
		public Class<KitCarpa> getClaseEntidad() {
			return KitCarpa.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final KitCarpa k) {
			final JSONObject json = new JSONObject();
			serializarItemBase(k, json);
			json.put("cant", Integer.valueOf(k.getCantidad()));
			return json;
		}

		@Override
		public KitCarpa deserializar(final JSONObject d, final Mundo mundo) {
			final KitCarpa k = KitCarpa.crearDesdeJson(d);
			k.setMundo(mundo);
			return k;
		}
	}

	public static class AdaptadorKitCama implements AdaptadorEntidad<KitCama> {
		@Override
		public String getId() {
			return "KIT_CAMA";
		}

		@Override
		public Class<KitCama> getClaseEntidad() {
			return KitCama.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final KitCama k) {
			final JSONObject json = new JSONObject();
			serializarItemBase(k, json);
			json.put("cant", Integer.valueOf(k.getCantidad()));
			return json;
		}

		@Override
		public KitCama deserializar(final JSONObject d, final Mundo mundo) {
			final KitCama k = KitCama.crearDesdeJson(d);
			k.setMundo(mundo);
			return k;
		}
	}
}