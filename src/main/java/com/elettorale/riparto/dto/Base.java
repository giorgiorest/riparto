package com.elettorale.riparto.dto;

import java.util.Date;

public class Base {

	private Integer idCandidato;
	private String nome;
	private String cognome;
	private Date dataNascita;
	private Integer idTerpaCandidato;
	private String descTerpaCandidato;
	private Integer votiTotCand;
	private Integer votiSoloCand;
	
	//
	private Integer idCollegioPluri;
	private String descCollegioPluri;
	private Integer idCircoscrizione;
	private String descCircoscrizione;
	private Integer codEnte;
	private Integer numSeggi;
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
	
	private Integer scorporo; 
	private String eletto;
	
	private Integer proquota;
	
	public Base() {
		super();
	}
	
	public Base(Base b) {

//		super(b);
		this.ccp = b.getCcp();
		this.cognome = b.getCognome();
		this.coterAcqte = b.getCoterAcqte();
		this.coterCoali = b.getCoterCoali();
		this.coterContr = b.getCoterContr();
		this.coterPrgContr = b.getCoterPrgContr();
		this.coterRicus = b.getCoterRicus();
		this.coteTerpa = b.getCoteTerpa();
		this.dataNascita = b.getDataNascita();
		this.descCircoscrizione = b.getDescCircoscrizione();
		this.descCollegioPluri = b.getDescCollegioPluri();
		this.descContrassegno = b.getDescContrassegno();
		this.descLista = b.getDescLista();
		this.descPartito = b.getDescPartito();
		this.descTerpaCandidato = b.getDescTerpaCandidato();
		this.flagMinoranza = b.getFlagMinoranza();
		this.idAggregatoRiparto = b.getIdAggregatoRiparto();
		this.idCandidato = b.getIdCandidato();
		this.idCircoscrizione = b.getIdCircoscrizione();
		this.idCollegioPluri = b.getIdCollegioPluri();
		this.idLista = b.getIdLista();
		this.idTerpaCandidato = b.getIdTerpaCandidato();
		this.nome = b.getNome();
		this.numSeggi = b.getNumSeggi();
		this.votiLista = b.getVotiLista();
		this.votiSoloCand = b.getVotiSoloCand();
		this.votiTotCand = b.getVotiTotCand();
		this.codEnte = b.getCodEnte();
		
//		Base base = new Base();
//		base.setCcp(b.getCcp());
//		base.setCifraCoalizione(b.getCifraCoalizione());
//		base.setCognome(b.getCognome());
//		base.setCoterAcqte(b.getCoterAcqte());
//		base.setCoterCoali(b.getCoterCoali());
//		base.setCoterContr(b.getCoterContr());
//		base.setCoterPrgContr(b.getCoterPrgContr());
//		base.setCoterRicus(b.getCoterRicus());
//		base.setCoteTerpa(b.getCoteTerpa());
//		base.setDataNascita(b.getDataNascita());
//		base.setDescCircoscrizione(b.getDescCircoscrizione());
//		base.setDescCollegioPluri(b.getDescCollegioPluri());
//		base.setDescContrassegno(b.getDescContrassegno());
//		base.setDescLista(b.getDescLista());
//		base.setDescPartito(b.getDescPartito());
//		base.setDescTerpaCandidato(b.getDescTerpaCandidato());
//		base.setFlagMinoranza(b.getFlagMinoranza());
//		base.setIdAggregatoRiparto(b.getIdAggregatoRiparto());
//		base.setIdCandidato(b.getIdCandidato());
//		base.setIdCircoscrizione(b.getIdCircoscrizione());
//		base.setIdCollegioPluri(b.getIdCollegioPluri());
//		base.setIdLista(b.getIdLista());
//		base.setIdTerpaCandidato(b.getIdTerpaCandidato());
//		base.setIsCoalizione(b.getIsCoalizione());
//		base.setNome(b.getNome());
//		base.setNumSeggi(b.getNumSeggi());
//		base.setPartecipaRipartoCoalizione(b.getPartecipaRipartoCoalizione());
//		base.setPartecipaRipartoLista(b.getPartecipaRipartoLista());
//		base.setPercentualeCoalizione(b.getPercentualeCoalizione());
//		base.setPercentualeLista(b.getPercentualeLista());
//		base.setVotiLista(b.getVotiLista());
//		base.setVotiSoloCand(b.getVotiSoloCand());
//		base.setVotiTotCand(b.getVotiTotCand());
		
	}
	
	public Base(String descLista, Integer coterCoali, Integer aggRiparto) {
		super();
		this.descLista = descLista;
		this.coterCoali = coterCoali;
		this.idAggregatoRiparto = aggRiparto;
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

	public Integer getCodEnte() {
		return codEnte;
	}

	public void setCodEnte(Integer codEnte) {
		this.codEnte = codEnte;
	}

	public Integer getScorporo() {
		return scorporo;
	}

	public void setScorporo(Integer scorporo) {
		this.scorporo = scorporo;
	}

	public String getEletto() {
		return eletto;
	}

	public void setEletto(String eletto) {
		this.eletto = eletto;
	}

	public Integer getProquota() {
		return proquota;
	}

	public void setProquota(Integer proquota) {
		this.proquota = proquota;
	}
	
	
	

	
}
