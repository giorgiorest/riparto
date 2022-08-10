package com.elettorale.riparto.utils;

import com.elettorale.riparto.utils.RipartoUtils.TipoTerritorio;

public class Territorio {
	private Integer id;
	private String descrizione;
	private TipoTerritorio tipoTerritorio;
	private Integer numSeggi;
	private Integer codEnte;
	private Territorio padre;
	
	private boolean sorteggioCollegio;
	
	public Territorio() {
	}
	public Territorio(Integer id, TipoTerritorio tipoTerritorio, String descrizione, Integer numSeggi, Integer codEnte) {
		super();
		this.id = id;
		this.tipoTerritorio = tipoTerritorio;
		this.descrizione = descrizione;
		this.setNumSeggi(numSeggi);
		this.codEnte = codEnte;
	}
	
	public Territorio(Territorio t) {
		super();
		this.id = t.getId();
		this.tipoTerritorio = t.getTipoTerritorio();
		this.descrizione = t.getDescrizione();
		this.numSeggi = t.getNumSeggi();
		this.codEnte = t.getCodEnte();
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
	
	public Territorio getPadre() {
		return padre;
	}

	public void setPadre(Territorio padre) {
		this.padre = padre;
	}

	public boolean isSorteggioCollegio() {
		return sorteggioCollegio;
	}

	public void setSorteggioCollegio(boolean sorteggioCollegio) {
		this.sorteggioCollegio = sorteggioCollegio;
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
