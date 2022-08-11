package com.elettorale.riparto.constants;

public enum Prospetto2 {
	DESC_LISTA("Descrizione lista"),
	CIFRA("Cifra elettorale lista"),
	SEGGI_QI("Seggi QI"),
	RESTI("Resti"),
	SEGGI_RESTI("Seggi Resti"),
	SORTEGGIO("Sorteggio");

	private String value;
	
	Prospetto2(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
