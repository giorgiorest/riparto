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
	class Elemento{
		private Integer id;
		private String descrizione;
		private Integer cifra;
		private Integer resto;
		private Boolean sorteggio = Boolean.FALSE;
		private Integer seggiResti = 0;
		private BigDecimal decimale;
		
		public Elemento(Integer id, String descrizione, Integer cifra, Integer resto) {
			super();
			this.id = id;
			this.descrizione = descrizione;
			this.cifra = cifra;
			this.resto = resto;
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
		public BigDecimal getDecimale() {
			return decimale;
		}
		public void setDecimale(BigDecimal decimale) {
			this.decimale = decimale;
		}
		@Override
		public String toString() {
			return "ID: " + this.id + ", DESCRIZIONE: " + this.descrizione + ", RESTO: " + this.resto + ", CIFRA: "
					+ this.cifra + ", SORT: " + this.sorteggio + ", SEGGI RESTI: " + this.seggiResti;
		}
	}
	
	class Quoziente{
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
	enum TipoOrdinamento{
		RESTI,
		CIFRA,
		DECIMALI
	}

	enum Ordinamento{
		ASC,
		DESC
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
				lista.sort((e1, e2) -> e1.getDecimale().compareTo(e2.getDecimale()));
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

	private List<Elemento> sortCustom(List<Elemento> lista, TipoOrdinamento tipoOrdinamento) {

		AtomicBoolean parita = new AtomicBoolean();
		
		switch (tipoOrdinamento) {
		case RESTI:
			lista.sort((e1, e2) -> {
				if(e1.getResto().equals(e2.getResto())) {
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
				if(e1.getDecimale().equals(e2.getDecimale())) {
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
				if(e1.getCifra().equals(e2.getCifra())) {
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
}