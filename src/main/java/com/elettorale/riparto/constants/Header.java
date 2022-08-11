package com.elettorale.riparto.constants;

public enum Header {
	
	DESC_LISTA("Descrizione lista"),
	CIFRA("Cifra lista"),
	CAND_UNI("Cand UNI"),
	PERC_CIFRA("% Lista"),
	PART_RIPARTO_LISTA("Partecipa"),
	CIFRA_COALI("Cifra coalizione"),
	PERC_COALI("% Coalizione"),
	PART_RIPARTO_COALI("Partecipa"),
	ID_COALI("id coalizione");

	private String value;
	
	Header(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
