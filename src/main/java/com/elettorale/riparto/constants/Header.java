package com.elettorale.riparto.constants;

public enum Header {
	
	DESC_LISTA("Descrizione lista"),
	CIFRA("Cifra elettorale lista"),
	PERC_CIFRA("% Lista"),
	PART_RIPARTO_LISTA("Partecipa al riparto"),
	CIFRA_COALI("Cifra elettorale coalizione"),
	PERC_COALI("% Coalizione"),
	PART_RIPARTO_COALI("Partecipa al riparto"),
	ID_COALI("id coalizione");

	private String value;
	
	Header(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
