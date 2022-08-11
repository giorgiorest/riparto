package com.elettorale.riparto.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.elettorale.riparto.utils.RipartoUtils.Elemento;

public class Coalizione {

	private Integer idCoalizone;
	private List<Elemento> liste;
	private Integer numVotiCoalizione;
	private String descCoalizione;
	private BigDecimal percentualeCoalizione;
	private String partecipaRipartoCoalizione;
	private Boolean isCoalizione;
	private Integer numCandUniEletti;
	
	public Coalizione() {
		// TODO Auto-generated constructor stub
	}
	
	public Coalizione(List<Elemento> liste) {
		this.liste = new ArrayList<>();
		this.liste.addAll(liste);
	}
	public List<Elemento> getListe() {
		return liste;
	}
	public void setListe(List<Elemento> liste) {
		this.liste = liste;
	}
	public Integer getNumVotiCoalizione() {
		return numVotiCoalizione;
	}
	public void setNumVotiCoalizione(Integer numVotiCoalizione) {
		this.numVotiCoalizione = numVotiCoalizione;
	}
	public String getDescCoalizione() {
		return descCoalizione;
	}
	public void setDescCoalizione(String descCoalizione) {
		this.descCoalizione = descCoalizione;
	}
	public BigDecimal getPercentualeCoalizione() {
		return percentualeCoalizione;
	}
	public void setPercentualeCoalizione(BigDecimal percentualeCoalizione) {
		this.percentualeCoalizione = percentualeCoalizione;
	}
	public String getPartecipaRipartoCoalizione() {
		return partecipaRipartoCoalizione;
	}
	public void setPartecipaRipartoCoalizione(String partecipaRipartoCoalizione) {
		this.partecipaRipartoCoalizione = partecipaRipartoCoalizione;
	}
	public Boolean getIsCoalizione() {
		return isCoalizione;
	}
	public void setIsCoalizione(Boolean isCoalizione) {
		this.isCoalizione = isCoalizione;
	}
	public Integer getIdCoalizone() {
		return idCoalizone;
	}
	public void setIdCoalizone(Integer idCoalizone) {
		this.idCoalizone = idCoalizone;
	}

	public Integer getNumCandUniEletti() {
		return numCandUniEletti;
	}

	public void setNumCandUniEletti(Integer numCandUniEletti) {
		this.numCandUniEletti = numCandUniEletti;
	}
	
	
}
