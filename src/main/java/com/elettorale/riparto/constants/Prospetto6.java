package com.elettorale.riparto.constants;

public enum Prospetto6 {
	LISTA_COAL("Descrizione lista/Coalizione"),
	SEGGI_QI("Seggi qi circ"),
	SEGGI_TOT("Seggi tot circ"),
	SEGGI_NAZ("Seggi Nazionali"),
	DIFFERENZA("Differenza");

	private String value;
	
	Prospetto6(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
