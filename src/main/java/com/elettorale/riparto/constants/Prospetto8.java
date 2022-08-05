package com.elettorale.riparto.constants;

public enum Prospetto8 {
	NUMER("Numero"),
	CIRC("Circoscrizione"),
	DECIMALI("Decimali"),
	SEGGI_QI("Seggi QI"),
	SEGGI_DEC("Seggi Decimali"),
	LISTE("Liste Coalizioni"),
	ORDINE("Ordine sottr");

	private String value;
	
	Prospetto8(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
