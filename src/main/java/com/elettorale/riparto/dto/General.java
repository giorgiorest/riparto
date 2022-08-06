package com.elettorale.riparto.dto;

import java.math.BigDecimal;

import com.elettorale.riparto.utils.RipartoUtils;

public class General extends RipartoUtils{

	private BigDecimal percentualeLista;
	private String partecipaRipartoLista;
	private String partecipaInCoalizione;
	
	
	public General() {
		// TODO Auto-generated constructor stub
	}
//	public General(Base b) {
//		super();
//		this.percentualeLista = b.getPercentualeLista();
//		this.partecipaRipartoLista = b.getPartecipaRipartoLista();
//	}
	public BigDecimal getPercentualeLista() {
		return percentualeLista;
	}
	public void setPercentualeLista(BigDecimal percentualeLista) {
		this.percentualeLista = percentualeLista;
	}
	public String getPartecipaRipartoLista() {
		return partecipaRipartoLista;
	}
	public void setPartecipaRipartoLista(String partecipaRipartoLista) {
		this.partecipaRipartoLista = partecipaRipartoLista;
	}
	public String getPartecipaInCoalizione() {
		return partecipaInCoalizione;
	}
	public void setPartecipaInCoalizione(String partecipaInCoalizione) {
		this.partecipaInCoalizione = partecipaInCoalizione;
	}
	
	
}
