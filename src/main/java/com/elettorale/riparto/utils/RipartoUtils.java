package com.elettorale.riparto.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RipartoUtils {

	
	/**
	 * Superclasse da estendere per avere una gestione centralizzata
	 */
	public class Elemento{
		private Integer id;
		private String descrizione;
		private Integer cifra;
		private Integer resto;
		private Boolean sorteggio = Boolean.FALSE;
		private Integer seggiQI = 0;
		private Integer seggiResti = 0;
		private Integer seggiDecimali = 0;
		private List<String> descrizioni;
		private Territorio territorio;
		private Quoziente quoziente;
		private Integer idCoalizione;
		
		//COMPENSAZIONE
		private Integer ordineSottrazione;
		private boolean cedeSeggio;
		private boolean riceveSeggio;
		
		public Elemento(Integer id, String descrizione, Integer cifra, Integer resto, List<String> descrizioni, Integer idCoalizione) {
			super();
			this.id = id;
			this.descrizione = descrizione;
			this.cifra = cifra;
			this.resto = resto;
			this.descrizioni = descrizioni;
			this.idCoalizione = idCoalizione;
		}
		public String getDescrizione() {
			return descrizione;
		}
		public void setDescrizione(String descrizione) {
			this.descrizione = descrizione;
		}
		public Integer getCifra() {
			return cifra;
		}
		public void setCifra(Integer cifra) {
			this.cifra = cifra;
		}
		public Integer getId() {
			return id;
		}
		public Integer getResto() {
			return resto;
		}
		public void setResto(Integer resti) {
			this.resto = resti;
		}
		public Boolean getSorteggio() {
			return sorteggio;
		}
		public void setSorteggio(Boolean sorteggio) {
			this.sorteggio = sorteggio;
		}
		public Integer getSeggiResti() {
			return seggiResti;
		}
		public void setSeggiResti(Integer seggiResti) {
			this.seggiResti = seggiResti;
		}
		public List<String> getDescrizioni() {
			return descrizioni;
		}
		public void setDescrizioni(List<String> descrizioni) {
			this.descrizioni = descrizioni;
		}
		public Integer getSeggiQI() {
			return seggiQI;
		}
		public void setSeggiQI(Integer seggiQI) {
			this.seggiQI = seggiQI;
		}
		
		public Territorio getTerritorio() {
			return territorio;
		}
		public void setTerritorio(Territorio territorio) {
			this.territorio = territorio;
		}
		public Integer getSeggiDecimali() {
			return seggiDecimali;
		}
		public void setSeggiDecimali(Integer seggiDecimali) {
			this.seggiDecimali = seggiDecimali;
		}
		public Quoziente getQuoziente() {
			return quoziente;
		}
		public void setQuoziente(Quoziente quoziente) {
			this.quoziente = quoziente;
		}
		public Integer getIdCoalizione() {
			return idCoalizione;
		}
		public void setIdCoalizione(Integer idCoalizione) {
			this.idCoalizione = idCoalizione;
		}
		public Integer getOrdineSottrazione() {
			return ordineSottrazione;
		}
		public void setOrdineSottrazione(Integer ordineSottrazione) {
			this.ordineSottrazione = ordineSottrazione;
		}
		public boolean isCedeSeggio() {
			return cedeSeggio;
		}
		public void setCedeSeggio(boolean cedeSeggio) {
			this.cedeSeggio = cedeSeggio;
		}
		public boolean isRiceveSeggio() {
			return riceveSeggio;
		}
		public void setRiceveSeggio(boolean riceveSeggio) {
			this.riceveSeggio = riceveSeggio;
		}

	}

	public class Territorio{
		private Integer id;
		private String descrizione;
		private TipoTerritorio tipoTerritorio;
		private Integer numSeggi;
		private Integer codEnte;
		
		public Territorio(Integer id, TipoTerritorio tipoTerritorio, String descrizione, Integer numSeggi, Integer codEnte) {
			super();
			this.id = id;
			this.tipoTerritorio = tipoTerritorio;
			this.descrizione = descrizione;
			this.setNumSeggi(numSeggi);
			this.codEnte = codEnte;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public TipoTerritorio getTipoTerritorio() {
			return tipoTerritorio;
		}
		public void setTipoTerritorio(TipoTerritorio tipoTerritorio) {
			this.tipoTerritorio = tipoTerritorio;
		}
		public String getDescrizione() {
			return descrizione;
		}
		public void setDescrizione(String descrizione) {
			this.descrizione = descrizione;
		}
		public Integer getNumSeggi() {
			return numSeggi;
		}
		public void setNumSeggi(Integer numSeggi) {
			this.numSeggi = numSeggi;
		}
		public Integer getCodEnte() {
			return codEnte;
		}
		public void setCodEnte(Integer codEnte) {
			this.codEnte = codEnte;
		}
		@Override
		public String toString() {
			return this.descrizione;
		}
		
		@Override
		public boolean equals(Object obj) {
			boolean ret = false;
			
			if(obj instanceof Territorio) {
				Territorio t = (Territorio)obj;
				
				if(this.getId().compareTo(t.getId()) == 0) {
					ret = true;
				}
			}
			return ret;
		}
	}
	
	protected class Quoziente{
		//numero di seggi interi
		private Integer quoziente;
		//resto del quoziente
		private Integer resto;
		//quoziente decimale
		private BigDecimal quozienteAttribuzione = BigDecimal.ZERO;
		//decimale escluso i seggi interi(quoziente)
		private BigDecimal decimale = BigDecimal.ZERO;
		
		public Integer getQuoziente() {
			return quoziente;
		}
		public void setQuoziente(Integer quoziente) {
			this.quoziente = quoziente;
		}
		public Integer getResto() {
			return resto;
		}
		public void setResto(Integer resto) {
			this.resto = resto;
		}
		
		public BigDecimal getDecimale() {
			return decimale;
		}
		public void setDecimale(BigDecimal decimale) {
			this.decimale = decimale;
		}
		public BigDecimal getQuozienteAttribuzione() {
			return quozienteAttribuzione;
		}
		public void setQuozienteAttribuzione(BigDecimal quozienteAttribuzione) {
			this.quozienteAttribuzione = quozienteAttribuzione;
		}
		@Override
		public String toString() {
			return "QUOZIENTE: " + this.quoziente +", RESTO: " + this.resto + ", DECIMALE: "+this.decimale +", QUOZ ATTR: "+this.quozienteAttribuzione;
		}
		
	}
	
	public List<Elemento> sort(List<Elemento> lista, TipoOrdinamento tipoOrdinamento, Ordinamento ordinamento) {
		
		switch (tipoOrdinamento) {
		case RESTI:
			if(ordinamento.equals(Ordinamento.ASC)) {
				lista.sort((e1, e2) -> e1.getResto().compareTo(e2.getResto()));
			}else {
				sortCustom(lista, tipoOrdinamento);
			}
			
			break;
		case DECIMALI:
			if(ordinamento.equals(Ordinamento.ASC)) {
				lista.sort((e1, e2) -> e1.getQuoziente().getDecimale().compareTo(e2.getQuoziente().getDecimale()));
			}else {
				sortCustom(lista, tipoOrdinamento);
			}
			
			break;
		case CIFRA:
			if(ordinamento.equals(Ordinamento.ASC)) {
				lista.sort((e1, e2) -> e1.getCifra().compareTo(e2.getCifra()));
			}else {
				sortCustom(lista, tipoOrdinamento);
			}
			
			break;
			
		default:
			break;
		}
		return lista;
	}

	public List<Elemento> sortCustom(List<Elemento> lista, TipoOrdinamento tipoOrdinamento) {

		AtomicBoolean parita = new AtomicBoolean();
		
		switch (tipoOrdinamento) {
		case RESTI:
			lista.sort((e1, e2) -> {
				if(e1.getResto() == (e2.getResto())) {
					parita.set(true);
				}
				return e2.getResto().compareTo(e1.getResto());
			});
			
			if(parita.get()) {
				sortCustom(lista, TipoOrdinamento.CIFRA);
			}
			break;
		case DECIMALI:
			lista.sort((e1, e2) -> {
				if(e1.getQuoziente().getDecimale() == (e2.getQuoziente().getDecimale())) {
					parita.set(true);
				}
				return e2.getResto().compareTo(e1.getResto());
			});
			
			if(parita.get()) {
				sortCustom(lista, TipoOrdinamento.CIFRA);
			}
			break;
		case CIFRA:
			lista.sort((e1, e2) -> {
				if(e1.getCifra() == (e2.getCifra())) {
					e1.setSorteggio(Boolean.TRUE);
					e2.setSorteggio(Boolean.TRUE);
				}
				return e2.getCifra().compareTo(e1.getCifra());
			});
			break;
			
		default:
			break;
		}
		return lista;
	}
	
	public Quoziente getQuoziente(Integer dividend, Integer divisor, Quoziente quoziente) {
		try {
			if(Objects.isNull(quoziente)) {
				quoziente = new Quoziente();
			}
			
			quoziente.setQuoziente(dividend / divisor);
			quoziente.setResto(0);
			quoziente.setQuozienteAttribuzione(truncateDecimal(BigDecimal.valueOf((double)dividend/divisor),6));
			quoziente.setDecimale(quoziente.getQuozienteAttribuzione().subtract(BigDecimal.valueOf(quoziente.getQuoziente())));
			
		} catch (Exception e) {
			// errore null pointer return quoziente null
		}
		
		
		return 	quoziente;
	}

	public BigDecimal truncateDecimal(BigDecimal value, int digit) {
		return value.setScale(digit, RoundingMode.DOWN);
	}
	
	public Quoziente getQuozienteResto(Integer dividend, Integer divisor, Quoziente quoziente) {
		
		try {
			quoziente = this.getQuoziente(dividend, divisor, quoziente);
			quoziente.setResto(dividend % divisor);
		} catch (Exception e) {
			// errore null pointer return quoziente null
		}
		
		return quoziente;
	}
	
	public List<Elemento> assegnaSeggi(List<Elemento> elements, TipoOrdinamento tipoOrdinamento, Integer numSeggi){
		
		sortCustom(elements, tipoOrdinamento);
		boolean fineGiro = false;
		
		while(numSeggi > 0 && !fineGiro) {
			for(Elemento e : elements)
				if(!e.sorteggio && numSeggi > 0) {
					switch (tipoOrdinamento) {
					case RESTI:
						e.setSeggiResti(e.getSeggiResti() + 1);
						break;
					case DECIMALI:
						e.setSeggiDecimali(e.getSeggiDecimali() + 1);
						break;
					default:
						break;
					}
					numSeggi--;
			}
			fineGiro = true;
		}
		
		return elements;
	}
	
	public Quoziente assegnaseggiQIMassimiResti(List<Elemento> elements,
			Integer numSeggiDaAssegnare, Integer totVoti, TipoOrdinamento tipoAssegnazione) {
		Quoziente q = getQuozienteResto(totVoti, numSeggiDaAssegnare, null);
		
		sortCustom(elements, TipoOrdinamento.CIFRA);
		
		if(elements.size() == 1) {
			elements.stream().findFirst().get().setSeggiQI(numSeggiDaAssegnare);
		}else {
			elements.forEach(e->{
				Quoziente quoziente = getQuozienteResto(e.getCifra(), q.getQuoziente(), null);
				
				e.setSeggiQI(quoziente.getQuoziente());
				e.setResto(quoziente.getResto());
			});
		}
		
		assegnaSeggi(elements, tipoAssegnazione, numSeggiDaAssegnare-elements.stream().mapToInt(Elemento::getSeggiQI).sum());
		
		return q;
	}
	
	protected enum TipoOrdinamento{
		RESTI,
		CIFRA,
		DECIMALI
	}

	protected enum Ordinamento{
		ASC,
		DESC
	}
	
	protected enum TipoTerritorio{
		NAZIONALE,
		CIRCOSCRIZIONE,
		COLLEGIO_PLURI,
		COLLEGIO_UNI;
	}
	
	public class Prospetto6{
		private Integer idAggregato;
		private String descLista;
		private Integer seggiQICirc;
		private Integer seggiTotCirc;
		private Integer seggiNazionali;
		private Integer diff;
		
		private Integer cifraNazionale;
		
		public Integer getIdAggregato() {
			return idAggregato;
		}
		public void setIdAggregato(Integer idAggregato) {
			this.idAggregato = idAggregato;
		}
		public String getDescLista() {
			return descLista;
		}
		public void setDescLista(String descLista) {
			this.descLista = descLista;
		}
		public Integer getSeggiQICirc() {
			return seggiQICirc;
		}
		public void setSeggiQICirc(Integer seggiQICirc) {
			this.seggiQICirc = seggiQICirc;
		}
		public Integer getSeggiTotCirc() {
			return seggiTotCirc;
		}
		public void setSeggiTotCirc(Integer seggiTotCirc) {
			this.seggiTotCirc = seggiTotCirc;
		}
		public Integer getSeggiNazionali() {
			return seggiNazionali;
		}
		public void setSeggiNazionali(Integer seggiNazionali) {
			this.seggiNazionali = seggiNazionali;
		}
		public Integer getDiff() {
			return diff;
		}
		public void setDiff(Integer diff) {
			this.diff = diff;
		}
		public Integer getCifraNazionale() {
			return cifraNazionale;
		}
		public void setCifraNazionale(Integer cifraNazionale) {
			this.cifraNazionale = cifraNazionale;
		}
	}
	
	public List<Prospetto6> sortByDiffCifra(List<Prospetto6> lista){
		
		lista.sort((e1,e2) -> {
			//se seggi eccedntari uguali, ordino per maggior cifra
			if(e1.getDiff().compareTo(e2.getDiff()) == 0) {
				//Ordino per cifra maggiore
				return e1.getCifraNazionale().compareTo(e2.getCifraNazionale());
			}else {
				//ordino per maggior seggi eccedentari
				return e2.getDiff().compareTo(e1.getDiff());
			}
			
		});
		return lista;
		
	}
}
