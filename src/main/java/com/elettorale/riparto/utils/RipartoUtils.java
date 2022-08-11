package com.elettorale.riparto.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.elettorale.riparto.dto.General;

public class RipartoUtils {

	
	/**
	 * Superclasse da estendere per avere una gestione centralizzata
	 */
	public class Elemento extends General{
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
		private boolean minoranza;
		private boolean sorteggioReale;
		
		//COMPENSAZIONE
		private Integer ordineSottrazione;
		private boolean cedeSeggio;
		private boolean riceveSeggio;
		private boolean shift;
		private Integer seggioCompensazione = 0;
		
		
		//PROQUOTA
		private Integer proquota = 0;
		
		public Elemento(Elemento e) {
			this.id = e.getId();
			this.descrizione = e.getDescrizione();
			this.cifra = e.getCifra();
			this.resto = e.resto;
			this.sorteggio = e.getSorteggio();
			this.seggiQI = e.getSeggiQI();
			this.seggiResti = e.getSeggiResti();
			this.seggiDecimali = e.getSeggiDecimali();
			this.idCoalizione = e.idCoalizione;
			this.quoziente = new Quoziente(e.getQuoziente());
			this.territorio = new Territorio(e.getTerritorio());
		}
		public Elemento(Integer id, String descrizione, Integer cifra, Integer resto, List<String> descrizioni, Integer idCoalizione) {
			super();
			this.id = id;
			this.descrizione = descrizione;
			this.cifra = cifra;
			this.resto = resto;
			this.descrizioni = descrizioni;
			this.idCoalizione = idCoalizione;
		}
		public Elemento() {
			// TODO Auto-generated constructor stub
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

		public boolean isShift() {
			return shift;
		}
		public void setShift(boolean shift) {
			this.shift = shift;
		}
		public Integer getSeggioCompensazione() {
			return seggioCompensazione;
		}
		public void setSeggioCompensazione(Integer seggioCompensazione) {
			this.seggioCompensazione = seggioCompensazione;
		}
		public boolean isMinoranza() {
			return minoranza;
		}
		public void setMinoranza(boolean minoranza) {
			this.minoranza = minoranza;
		}
		public Integer getProquota() {
			return proquota;
		}
		public void setProquota(Integer proquota) {
			this.proquota = proquota;
		}
		public boolean isSorteggioReale() {
			return sorteggioReale;
		}
		public void setSorteggioReale(boolean sorteggioReale) {
			this.sorteggioReale = sorteggioReale;
		}
		@Override
		public String toString() {
			return this.descrizione+ " "+this.territorio.getDescrizione()+" SEGGI QI:"+this.seggiQI +", SEGGI DEC: "+this.seggiDecimali+", DEC: "+this.quoziente.decimale;
		}
	}

	
	protected class Quoziente{
		//numero di seggi interi
		private Integer quoziente;
		private BigDecimal quozienteDecimale;
		//resto del quoziente
		private Integer resto;
		//quoziente decimale
		private BigDecimal quozienteAttribuzione = BigDecimal.ZERO;
		//decimale escluso i seggi interi(quoziente)
		private BigDecimal decimale = BigDecimal.ZERO;
		
		public Quoziente(Quoziente q) {
			this.quoziente = q.getQuoziente();
			this.resto = q.getResto();
			this.quozienteAttribuzione = q.getQuozienteAttribuzione();
			this.decimale = q.getDecimale();
		}
		public Quoziente() {
		}
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
		public BigDecimal getQuozienteDecimale() {
			return quozienteDecimale;
		}
		public void setQuozienteDecimale(BigDecimal quozienteDecimale) {
			this.quozienteDecimale = quozienteDecimale;
		}
		@Override
		public String toString() {
			return "QUOZIENTE: " + this.quoziente +", RESTO: " + this.resto + ", DECIMALE: "+this.decimale +", QUOZ ATTR: "+this.quozienteAttribuzione;
		}
		
	}
	
	protected List<Elemento> sort(List<Elemento> lista, TipoOrdinamento tipoOrdinamento, Ordinamento ordinamento) {
		
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

	protected List<Elemento> sortCustom(List<Elemento> lista, TipoOrdinamento tipoOrdinamento) {

//		AtomicBoolean parita = new AtomicBoolean();
		
		switch (tipoOrdinamento) {
		case RESTI:
			lista.sort((e1, e2) -> {
				if(e1.getResto().compareTo((e2.getResto())) == 0 && e1.getCifra().compareTo(e2.getCifra()) == 0) {
//					parita.set(true);
					e1.setSorteggio(true);
					e2.setSorteggio(true);
				}
				return e2.getResto().compareTo(e1.getResto());
			});
			
//			if(parita.get()) {
//				sortCustom(lista, TipoOrdinamento.CIFRA);
//			}
			break;
		case RESTI_PROQUOTA:
			
			lista.sort((e1, e2) -> {
				
				if(e1.getResto().compareTo((e2.getResto())) == 0) {
					e1.setSorteggio(true);
					e2.setSorteggio(true);
				}
				
				return e2.getResto().compareTo(e1.getResto());
			});
			
			break;
		case DECIMALI:
			lista.sort((e1, e2) -> {
				if(e1.getQuoziente().getDecimale().compareTo((e2.getQuoziente().getDecimale())) == 0 && e1.getCifra().compareTo(e2.getCifra()) == 0) {
//					parita.set(true);
					e1.setSorteggio(true);
					e2.setSorteggio(true);
				}
				return e2.getResto().compareTo(e1.getResto());
			});
			
//			if(parita.get()) {
//				sortCustom(lista, TipoOrdinamento.CIFRA);
//			}
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
	
	protected List<CandidatoUni> sortCandidati(List<CandidatoUni> lista) {

			lista.sort((e1, e2) -> {
				if(e1.getVoti().compareTo((e2.getVoti())) == 0) {
					e1.setParitaVoti(true);
					e2.setParitaVoti(true);
				}
				return e2.getVoti().compareTo(e1.getVoti());
			});
			
		return lista;
	}
	
	protected List<CandidatoUni> sortCandidatiDataNascita(List<CandidatoUni> lista) {

			lista.sort((e1, e2) -> {
				if(e1.getVoti().compareTo((e2.getVoti())) == 0) {
					e1.setSorteggio(true);
					e2.setSorteggio(true);
				}
				return e1.getVoti().compareTo(e2.getVoti());
			});
			
		return lista;
	}
	
	protected Quoziente getQuoziente(Integer dividend, Integer divisor, Quoziente quoziente, BigDecimal quozienteDecimale) {
		try {
			if(Objects.isNull(quoziente)) {
				quoziente = new Quoziente();
			}
			
			if(Objects.isNull(quozienteDecimale)) {
				
				quoziente.setQuoziente(dividend / divisor);
			}else {
				quoziente.setQuoziente(BigDecimal.valueOf(dividend).divide(quozienteDecimale, 0, RoundingMode.DOWN).intValue());
			}
			quoziente.setQuozienteDecimale(truncateDecimal(BigDecimal.valueOf((double)dividend / divisor), 6));
			quoziente.setResto(0);
			quoziente.setQuozienteAttribuzione(truncateDecimal(BigDecimal.valueOf((double)dividend/divisor),6));
			quoziente.setDecimale(quoziente.getQuozienteAttribuzione().subtract(BigDecimal.valueOf(quoziente.getQuoziente())));
			
		} catch (Exception e) {
			// errore null pointer return quoziente null
		}
		
		
		return 	quoziente;
	}

	protected BigDecimal getQuozienteDecimale(Integer dividend, Integer divisor) {
		try {
			return truncateDecimal(BigDecimal.valueOf((double)dividend / divisor), 6);
			
		} catch (Exception e) {
			// errore null pointer return quoziente null
			return 	null;
		}
		
	}
	
	protected BigDecimal truncateDecimal(BigDecimal value, int digit) {
		return value.setScale(digit, RoundingMode.DOWN);
	}
	
	protected Quoziente getQuozienteResto(Integer dividend, Integer divisor, Quoziente quoziente, BigDecimal quozienteDecimale) {
		
		try {
			if(Objects.isNull(quoziente)) {
				quoziente = new Quoziente();
			}
			quoziente = this.getQuoziente(dividend, divisor, quoziente, quozienteDecimale);
			
			if(Objects.isNull(divisor)) {
				BigDecimal resto =BigDecimal.valueOf(dividend).divideAndRemainder(quozienteDecimale)[1];
				quoziente.setResto(resto.intValue());
			}else {
				quoziente.setResto(dividend % divisor);
			}
		} catch (Exception e) {
			// errore null pointer return quoziente null
			e.printStackTrace();
		}
		
		return quoziente;
	}
	
	protected List<Elemento> assegnaSeggi(List<Elemento> elements, TipoOrdinamento tipoOrdinamento, Integer numSeggi){
		
		sortCustom(elements, tipoOrdinamento);
		boolean fineGiro = false;
		boolean trovataParita = false;
		
		AtomicInteger numSeggiAtomic = new AtomicInteger(numSeggi);
		
		while(numSeggiAtomic.get() > 0 && !fineGiro) {
			
			for (Elemento e : elements) {
				
				if(/*!e.getSorteggio() && */trovataParita || numSeggiAtomic.get() > 0) {
					switch (tipoOrdinamento) {
					case RESTI:
						//controllo ultimo seggio a sorteggio
						if((numSeggiAtomic.get() == 1 || trovataParita) && e.getSorteggio()) {
							numSeggiAtomic.getAndDecrement();
							e.setSorteggioReale(true);
							
							trovataParita = !trovataParita;
							
						}else {
							if(trovataParita) {
								break;
							}
							e.setSeggiResti(e.getSeggiResti() + 1);
						}
						break;
					case RESTI_PROQUOTA:
						e.setSeggiResti(e.getSeggiResti() + 1);
						break;
					case DECIMALI:
						e.setSeggiDecimali(e.getSeggiDecimali() + 1);
						break;
					default:
						break;
					}
					numSeggiAtomic.getAndDecrement();
			}
			}
			fineGiro = true;
		}
		
		return elements;
	}
	
	protected Quoziente assegnaseggiQIMassimiResti(List<Elemento> elements,
			Integer numSeggiDaAssegnare, Integer totVoti, TipoOrdinamento tipoAssegnazione, BigDecimal quozienteDecimale) {
		try {
			Quoziente q = getQuozienteResto(totVoti, numSeggiDaAssegnare, null, quozienteDecimale);
			
			sortCustom(elements, TipoOrdinamento.CIFRA);
			
			if(elements.size() == 1) {
				elements.stream().findFirst().get().setSeggiQI(numSeggiDaAssegnare);
			}else {
				elements.forEach(e->{
					Quoziente quoziente = null;
					if(Objects.isNull(quozienteDecimale)) {
						
						quoziente = getQuozienteResto(e.getCifra(), q.getQuoziente(), null, quozienteDecimale);
					}else {
						quoziente = getQuozienteResto(e.getCifra(), null, null, quozienteDecimale);
					}
					
					e.setSeggiQI(quoziente.getQuoziente());
					e.setResto(quoziente.getResto());
				});
			}
			
			int seggiRimanenti = elements.stream().mapToInt(Elemento::getSeggiQI).sum();
			
			assegnaSeggi(elements, tipoAssegnazione, numSeggiDaAssegnare-seggiRimanenti);
			
			return q;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException();
		}
		
		
	}
	
	protected Comparator<? super Elemento> compareByDecimale(Ordinamento ordinamento) {
		switch (ordinamento) {
		case DESC:
			return (le, re) -> re.getQuoziente().getDecimale().compareTo(le.getQuoziente().getDecimale());
		case ASC:
			return (le, re) -> le.getQuoziente().getDecimale().compareTo(re.getQuoziente().getDecimale());
		default:
			//default ordina ASC
			return (le, re) -> le.getQuoziente().getDecimale().compareTo(re.getQuoziente().getDecimale());
		}
	}
	
	protected Predicate<Elemento> partecipaCifraInCoalizione() {
		return l->l.getPartecipaInCoalizione().equals(PartecipaRiparto.SI.toString());
	}

	protected Predicate<Elemento> partecipaRipartoLista() {
		return l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString());
	}
	
	protected boolean puoAssegnareSeggio(Elemento ecc) {
		return ecc.getSeggiDecimali().compareTo(0) > 0 && !ecc.isCedeSeggio();
	}
	
	protected enum TipoOrdinamento{
		RESTI,
		CIFRA,
		DECIMALI,
		RESTI_PROQUOTA
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
	
	protected enum PartecipaRiparto{
		SI,NO;
	}
	
	protected enum Sbarramento{
		UNO(1),
		TRE(3),
		DIECI(10),
		VENTI(20);
		Sbarramento(int i) {
			this.value = i;
		}

		private Integer value;

		public Integer getValue() {
			return value;
		}

	}
	
	public class Confronto{
		private Integer id;
		private String descLista;
		private Integer seggiQICirc;
		private Integer seggiTotCirc;
		private Integer seggiNazionali;
		private Integer diff;
		
		private Integer cifraNazionale;

		private Territorio territorio;
		private boolean parita;
		
		public boolean isParita() {
			return parita;
		}
		public void setParita(boolean parita) {
			this.parita = parita;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer idAggregato) {
			this.id = idAggregato;
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
		public Territorio getTerritorio() {
			return territorio;
		}
		public void setTerritorio(Territorio territorio) {
			this.territorio = territorio;
		}
		
		@Override
		public String toString() {
			return this.descLista + " diff: "+this.diff;
		}
	}
	
	protected enum BloccoRiparto {
		BLOCCO_RIPARTO_COALIZIONE_SEGGI_DEFICITARI(1, "Blocco riparto compensazione seggi deficitari coalizione."), 
		BLOCCO_RIPARTO_COLLEGIO(2, "Blocco riparto compensazione seggi collegio."),
		BLOCCO_RIPARTO_NAZIONALE_PARITA_CIFRE(3,"Blocco riparto nazionale tra liste per parita' di resti e cifra elettorale nazionle di lista."),
		BLOCCO_RIPARTO_CIRCOSCRIZIONALE_PARITA_CIFRE(4, "Blocco riparto circoscrizionale per parita' di decimali quoziente attribuzione e cifra elettorale nazionale."),
		BLOCCO_RIPARTO_LISTE_CIRCOSCRIZIONALE_PARITA_CIFRE(5, "Blocco circoscrizionale: parita' di decimali quoziente attribuzione e di cifre circoscrizionali - Riparto Liste."),//oldBlocco circoscrizionale: parita' di decimali, quoziente attribuzione e di cifre circoscrizionali - Riparto Liste"; // scenario 7
		BLOCCO_RIPARTO_LISTE_NAZIONALI_PARITA_CIFRE (6,"Blocco circoscrizionale: parita' di decimali quoziente attribuzione e di cifre nazionali durante compensazione."), // scenario 6
		BLOCCO_RIPARTO_COLLEGI_CIRCOSCRIZIONALE_PARITA_CIFRE(7, "Blocco circoscrizionale: parita' di decimali quoziente attribuzione e di cifre circoscrizionali - Riparto Collegi");
		
		BloccoRiparto(Integer id, String value) {
			this.id = id;
			this.setValue(value);
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		private Integer id;
		private String value;
		
	}
	
	protected List<Confronto> sortByDiffCifra(List<Confronto> lista, Ordinamento ordinamento){
		
		switch (ordinamento) {
		case DESC:
			lista.sort((e1,e2) -> {
				//se seggi eccedntari uguali, ordino per maggior cifra
				if(e1.getDiff().compareTo(e2.getDiff()) == 0) {
					e1.setParita(true);
					e2.setParita(true);
					//Ordino per cifra maggiore
					return e1.getCifraNazionale().compareTo(e2.getCifraNazionale());
				}else {
					//ordino per maggior seggi eccedentari
					return e2.getDiff().compareTo(e1.getDiff());
				}
				
			});
			break;
		case ASC:
			lista.sort((e1,e2) -> {
				//se seggi eccedntari uguali, ordino per maggior cifra
				if(e1.getDiff().compareTo(e2.getDiff()) == 0) {
					e1.setParita(true);
					e2.setParita(true);
					//Ordino per cifra maggiore
					return e1.getCifraNazionale().compareTo(e2.getCifraNazionale());
				}else {
					//ordino per maggior seggi eccedentari
					return e1.getDiff().compareTo(e2.getDiff());
				}
				
			});
			break;

		default:
			break;
		}
		
		return lista;
		
	}
	
	protected enum CircoscirioneMinoranza{
		TRENTINO_ALTO_ADIGE(28),
		FRIULI_VENEZIA_GIULIA(9);
		
		private Integer codEnte;
		
		CircoscirioneMinoranza(Integer codEnte) {
			this.codEnte = codEnte;
		}
		
		public Integer getValue(CircoscirioneMinoranza circMin) {
			return circMin.getCodEnte();
		}

		public Integer getCodEnte() {
			return codEnte;
		}
		
	}
	
	
	public class ChiavePluri{
		private Integer idEnte;
		private Integer idAggregato;
		
		public ChiavePluri(Integer idEnte, Integer idAggregato) {
			super();
			this.idEnte = idEnte;
			this.idAggregato = idAggregato;
		}
		public Integer getIdEnte() {
			return idEnte;
		}
		public void setIdEnte(Integer idEnte) {
			this.idEnte = idEnte;
		}
		public Integer getIdAggregato() {
			return idAggregato;
		}
		public void setIdAggregato(Integer idAggregato) {
			this.idAggregato = idAggregato;
		}
		
		@Override
		public boolean equals(Object obj) {

			boolean ret = false;
			
			if(obj instanceof ChiavePluri) {
				ChiavePluri t = (ChiavePluri)obj;
				
				if (this.getIdAggregato().compareTo(t.getIdAggregato()) == 0
						&& this.getIdEnte().compareTo(t.getIdEnte()) == 0) {
					ret = true;
				}
			}
			return ret;
		}
	}
	
	public class CandidatoUni{
		private Integer id;
		private Date dataNascita;
		private Territorio territorio;
		private Integer voti;
		private Integer votiSoloCandidato;
		private boolean eletto;
		private boolean sorteggio;
		private boolean paritaVoti;
		private List<Elemento> liste = new ArrayList<>();
		private Integer posizione;
		
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public Date getDataNascita() {
			return dataNascita;
		}
		public void setDataNascita(Date dataNascita) {
			this.dataNascita = dataNascita;
		}
		public Territorio getTerritorio() {
			return territorio;
		}
		public void setTerritorio(Territorio territorio) {
			this.territorio = territorio;
		}
		public Integer getVoti() {
			return voti;
		}
		public void setVoti(Integer voti) {
			this.voti = voti;
		}
		public Integer getVotiSoloCandidato() {
			return votiSoloCandidato;
		}
		public void setVotiSoloCandidato(Integer votiSoloCandidato) {
			this.votiSoloCandidato = votiSoloCandidato;
		}
		public boolean isEletto() {
			return eletto;
		}
		public void setEletto(boolean eletto) {
			this.eletto = eletto;
		}
		public boolean isSorteggio() {
			return sorteggio;
		}
		public void setSorteggio(boolean sorteggio) {
			this.sorteggio = sorteggio;
		}
	
		public Integer getPosizione() {
			return posizione;
		}
		public void setPosizione(Integer posizione) {
			this.posizione = posizione;
		}
		public boolean isParitaVoti() {
			return paritaVoti;
		}
		public void setParitaVoti(boolean paritaVoti) {
			this.paritaVoti = paritaVoti;
		}
		public List<Elemento> getListe() {
			return liste;
		}
		public void setListe(List<Elemento> liste) {
			this.liste = liste;
		}
		@Override
		public String toString() {
			return this.id +" "+this.voti+" "+this.sorteggio;
		}
	}
	
	public Comparator<? super Territorio> sortByCodEnte() {
		return (e1,e2)-> e1.getCodEnte().compareTo(e2.getCodEnte());
	}
	
}
