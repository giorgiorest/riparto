package com.elettorale.riparto.constants;

public enum Prospetto7 {
	NUMER("Numero"),
	CIRC("Circoscrizione"),
	DECIMALI("Decimali"),
	SEGGI_QI("Seggi QI"),
	SEGGI_DEC("Seggi Decimali"),
	ORDINE("Ordine sottr");

	private String value;
	
	Prospetto7(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
