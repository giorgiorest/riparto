package com.elettorale.riparto.dto;

import java.util.Date;

public class Base extends General{

	private Integer idCandidato;
	private String nome;
	private String cognome;
	private Date dataNascita;
	private Integer idTerpaCandidato;
	private String descTerpaCandidato;
	private Integer idCollegioPluri;
	private String descCollegioPluri;
	private Integer idCircoscrizione;
	private String descCircoscrizione;
	private Integer numSeggi;
	private Integer votiTotCand;
	private Integer votiSoloCand;
	private Integer votiLista;
	private String descLista;
	private Integer idLista;
	private String descContrassegno;
	private String descPartito;
	private Integer idAggregatoRiparto;
	private Integer coteTerpa;
	private Integer coterAcqte;
	private Integer coterContr;
	private Integer coterPrgContr;
	private Integer coterCoali;
	private Integer coterRicus;
	private String flagMinoranza;
	private Integer ccp;
	
	public Base() {
		// TODO Auto-generated constructor stub
	}
	
	public Base(String descLista, Integer coterCoali) {
		this.descLista = descLista;
		this.coterCoali = coterCoali;
	}
	public Integer getIdCandidato() {
		return idCandidato;
	}
	public String getNome() {
		return nome;
	}
	public String getCognome() {
		return cognome;
	}
	public Date getDataNascita() {
		return dataNascita;
	}
	public Integer getIdTerpaCandidato() {
		return idTerpaCandidato;
	}
	public String getDescTerpaCandidato() {
		return descTerpaCandidato;
	}
	public Integer getIdCollegioPluri() {
		return idCollegioPluri;
	}
	public String getDescCollegioPluri() {
		return descCollegioPluri;
	}
	public Integer getIdCircoscrizione() {
		return idCircoscrizione;
	}
	public String getDescCircoscrizione() {
		return descCircoscrizione;
	}
	public Integer getNumSeggi() {
		return numSeggi;
	}
	public Integer getVotiTotCand() {
		return votiTotCand;
	}
	public Integer getVotiSoloCand() {
		return votiSoloCand;
	}
	public Integer getVotiLista() {
		return votiLista;
	}
	public String getDescLista() {
		return descLista;
	}
	public Integer getIdLista() {
		return idLista;
	}
	public String getDescContrassegno() {
		return descContrassegno;
	}
	public String getDescPartito() {
		return descPartito;
	}
	public Integer getIdAggregatoRiparto() {
		return idAggregatoRiparto;
	}
	public Integer getCoteTerpa() {
		return coteTerpa;
	}
	public Integer getCoterAcqte() {
		return coterAcqte;
	}
	public Integer getCoterContr() {
		return coterContr;
	}
	public Integer getCoterPrgContr() {
		return coterPrgContr;
	}
	public Integer getCoterCoali() {
		return coterCoali;
	}
	public Integer getCoterRicus() {
		return coterRicus;
	}
	public String getFlagMinoranza() {
		return flagMinoranza;
	}
	public Integer getCcp() {
		return ccp;
	}
	public void setIdCandidato(Integer idCandidato) {
		this.idCandidato = idCandidato;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	public void setDataNascita(Date dataNascita) {
		this.dataNascita = dataNascita;
	}
	public void setIdTerpaCandidato(Integer idTerpaCandidato) {
		this.idTerpaCandidato = idTerpaCandidato;
	}
	public void setDescTerpaCandidato(String descTerpaCandidato) {
		this.descTerpaCandidato = descTerpaCandidato;
	}
	public void setIdCollegioPluri(Integer idCollegioPluri) {
		this.idCollegioPluri = idCollegioPluri;
	}
	public void setDescCollegioPluri(String descCollegioPluri) {
		this.descCollegioPluri = descCollegioPluri;
	}
	public void setIdCircoscrizione(Integer idCircoscrizione) {
		this.idCircoscrizione = idCircoscrizione;
	}
	public void setDescCircoscrizione(String descCircoscrizione) {
		this.descCircoscrizione = descCircoscrizione;
	}
	public void setNumSeggi(Integer numSeggi) {
		this.numSeggi = numSeggi;
	}
	public void setVotiTotCand(Integer votiTotCand) {
		this.votiTotCand = votiTotCand;
	}
	public void setVotiSoloCand(Integer votiSoloCand) {
		this.votiSoloCand = votiSoloCand;
	}
	public void setVotiLista(Integer votiLista) {
		this.votiLista = votiLista;
	}
	public void setDescLista(String descLista) {
		this.descLista = descLista;
	}
	public void setIdLista(Integer idLista) {
		this.idLista = idLista;
	}
	public void setDescContrassegno(String descContrassegno) {
		this.descContrassegno = descContrassegno;
	}
	public void setDescPartito(String descPartito) {
		this.descPartito = descPartito;
	}
	public void setIdAggregatoRiparto(Integer idAggregatoRiparto) {
		this.idAggregatoRiparto = idAggregatoRiparto;
	}
	public void setCoteTerpa(Integer coteTerpa) {
		this.coteTerpa = coteTerpa;
	}
	public void setCoterAcqte(Integer coterAcqte) {
		this.coterAcqte = coterAcqte;
	}
	public void setCoterContr(Integer coterContr) {
		this.coterContr = coterContr;
	}
	public void setCoterPrgContr(Integer coterPrgContr) {
		this.coterPrgContr = coterPrgContr;
	}
	public void setCoterCoali(Integer coterCoali) {
		this.coterCoali = coterCoali;
	}
	public void setCoterRicus(Integer coterRicus) {
		this.coterRicus = coterRicus;
	}
	public void setFlagMinoranza(String flagMinoranza) {
		this.flagMinoranza = flagMinoranza;
	}
	public void setCcp(Integer ccp) {
		this.ccp = ccp;
	}
	
	
	

	
}
