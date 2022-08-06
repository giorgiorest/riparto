package com.elettorale.riparto.utils;

import com.elettorale.riparto.utils.RipartoUtils.Elemento;

public class Prospetto9 {

	private Elemento eccedntaria;
	private Elemento deficitaria;
	
	public Elemento getEccedntaria() {
		return eccedntaria;
	}
	public void setEccedntaria(Elemento eccedntaria) {
		this.eccedntaria = eccedntaria;
	}
	public Elemento getDeficitaria() {
		return deficitaria;
	}
	public void setDeficitaria(Elemento deficitaria) {
		this.deficitaria = deficitaria;
	}
	public Prospetto9(Elemento eccedntaria, Elemento deficitaria) {
		super();
		this.eccedntaria = eccedntaria;
		this.deficitaria = deficitaria;
	}

	
}
