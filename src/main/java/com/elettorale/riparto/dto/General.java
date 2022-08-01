package com.elettorale.riparto.dto;

import java.math.BigDecimal;

public class General {

	private BigDecimal percentualeLista;
	private BigDecimal percentualeCoalizione;
	private Integer cifraCoalizione;
	private String partecipaRipartoLista;
	private String partecipaRipartoCoalizione;
	private Boolean isCoalizione;
	
	public BigDecimal getPercentualeLista() {
		return percentualeLista;
	}
	public void setPercentualeLista(BigDecimal percentualeLista) {
		this.percentualeLista = percentualeLista;
	}
	public BigDecimal getPercentualeCoalizione() {
		return percentualeCoalizione;
	}
	public void setPercentualeCoalizione(BigDecimal percentualeCoalizione) {
		this.percentualeCoalizione = percentualeCoalizione;
	}
	public Integer getCifraCoalizione() {
		return cifraCoalizione;
	}
	public void setCifraCoalizione(Integer cifraCoalizione) {
		this.cifraCoalizione = cifraCoalizione;
	}
	public String getPartecipaRipartoLista() {
		return partecipaRipartoLista;
	}
	public void setPartecipaRipartoLista(String partecipaRipartoLista) {
		this.partecipaRipartoLista = partecipaRipartoLista;
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
	
	
}
