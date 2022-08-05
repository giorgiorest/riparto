package com.elettorale.riparto.constants;

public enum Prospetto5 {
	DESC_LISTA("Descrizione lista"),
	CIFRA("Cifra elettorale lista"),
	QUOZ_ATTR("Quoziente atribuzione"),
	SEGGI_QI("Seggi QI"),
	DECIMALI("Decimali"),
	SEGGI_DECIMALI("Seggi Decimali");

	private String value;
	
	Prospetto5(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
