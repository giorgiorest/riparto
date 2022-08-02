package com.elettorale.riparto.calcolo;

import java.math.BigDecimal;

import com.elettorale.riparto.utils.RipartoUtils;

public class AppoggioStampa extends RipartoUtils {

	private Integer totaleVotiValidi;
	private BigDecimal votiValidi1;
	private BigDecimal votiValidi3;
	private BigDecimal votiValidi10;
	private BigDecimal votiValidi20;
	
	public Integer getTotaleVotiValidi() {
		return totaleVotiValidi;
	}
	public void setTotaleVotiValidi(Integer totaleVotiValidi) {
		this.totaleVotiValidi = totaleVotiValidi;
	}
	public BigDecimal getVotiValidi1() {
		return votiValidi1;
	}
	public void setVotiValidi1(BigDecimal votiValidi1) {
		this.votiValidi1 = votiValidi1;
	}
	public BigDecimal getVotiValidi3() {
		return votiValidi3;
	}
	public void setVotiValidi3(BigDecimal votiValidi3) {
		this.votiValidi3 = votiValidi3;
	}
	public BigDecimal getVotiValidi10() {
		return votiValidi10;
	}
	public void setVotiValidi10(BigDecimal votiValidi10) {
		this.votiValidi10 = votiValidi10;
	}
	public BigDecimal getVotiValidi20() {
		return votiValidi20;
	}
	public void setVotiValidi20(BigDecimal votiValidi20) {
		this.votiValidi20 = votiValidi20;
	}
	
	
}
