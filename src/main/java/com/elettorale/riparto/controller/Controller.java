package com.elettorale.riparto.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.elettorale.riparto.calcolo.RipartoCamera;
import com.elettorale.riparto.dto.Base;

@RestController
//@RequestMapping("riparto")
public class Controller {

	@Autowired
	JdbcTemplate jdbcTemplate;

	Logger log = LoggerFactory.getLogger(Controller.class);
	
	@RequestMapping(value = "/esegui",method = RequestMethod.GET)
	public String getRiparto(HttpServletResponse response) {

		StopWatch sw = new StopWatch();
		try {
			sw.start();
			
			log.info("RECUPERO DATI--------");
			List<Base> baseList = getData();
			log.info("DATI RECUPERATI--------");
			
			RipartoCamera riparto = new RipartoCamera(baseList);
			
			riparto.eseguiRiparto();
			
			log.info("FINE RIPARTO");
			sw.stop();
		} catch (Exception e) {
			e.printStackTrace();
			return "ERRORE RIPARTO";
		}
		
		Calendar gc = GregorianCalendar.getInstance();

		return "FINE RIPARTO IN: " + String.valueOf(sw.getTotalTimeSeconds()) +"  ->  "+ gc.get(Calendar.HOUR_OF_DAY)+":"+gc.get(Calendar.MINUTE);
	}

	@RequestMapping(value = "/eseguilocal",method = RequestMethod.GET)
	public String getRipartoLocal(HttpServletResponse response) {

		StopWatch sw = new StopWatch();
		try {
			sw.start();
			
			log.info("RECUPERO DATI--------");
			List<Base> baseList = getDataLocal();
			log.info("DATI RECUPERATI--------");
			
			RipartoCamera riparto = new RipartoCamera(baseList);
			
			riparto.eseguiRiparto();
			
			log.info("FINE RIPARTO");
			sw.stop();
		} catch (Exception e) {
			e.printStackTrace();
			return "ERRORE RIPARTO ";
		}
		
		Calendar gc = GregorianCalendar.getInstance();

		return "FINE RIPARTO IN: " + String.valueOf(sw.getTotalTimeSeconds()) +"  ->  "+ gc.get(Calendar.HOUR_OF_DAY)+":"+gc.get(Calendar.MINUTE);
	}
	
	@SuppressWarnings("deprecation")
	public List<Base> getData() {

//		String query = "WITH territorio as (   SELECT terpa.TERPA_SEQ_TERRPARTECIPANTE_PK, terpa.terpa_descr_ente, seggi.seggi_num_seggi,     terpaCP.terpa_cod_ente_livello codCP, terpaCP.terpa_descr_ente descCP, terpaCP.TERPA_SEQ_TERRPARTECIPANTE_PK terrCP, terpaCIRC.terpa_cod_ente_livello codCIRC, terpaCIRC.terpa_descr_ente descCIRC   FROM CL_TERPA_TERRPARTECIPANTI terpa    left JOIN CL_SEGGI_SEGGI seggi ON seggi.SEGGI_TERPA_TERRPARTECIPANTE = terpa.TERPA_TERPA_TERRPARTECIPANTE   left JOIN CL_TERPA_TERRPARTECIPANTI terpaCP ON terpaCP.TERPA_SEQ_TERRPARTECIPANTE_PK = terpa.TERPA_TERPA_TERRPARTECIPANTE   left JOIN CL_TERPA_TERRPARTECIPANTI terpaCIRC ON terpaCIRC.TERPA_SEQ_TERRPARTECIPANTE_PK = terpaCP.TERPA_TERPA_TERRPARTECIPANTE   where terpa.terpa_gerte_gerarcterritorio = 12   connect by prior terpa.TERPA_SEQ_TERRPARTECIPANTE_PK = terpa.TERPA_TERPA_TERRPARTECIPANTE   start with terpa.TERPA_SEQ_TERRPARTECIPANTE_PK = ? ),  candLis AS (   SELECT canle.CANLE_CANDI_CANDIDATO, canle.CANLE_TERPA_TERRPARTECIPANTE, calis.CALIS_LISTE_LISTA, terr.terpa_descr_ente,     candi.CANDI_DESCR_NOME, altno.ALTNO_DESCR_ALTRONOME || ' ' || candi.CANDI_ALTNO_ALTRONOME as altronome, candi.CANDI_DESCR_COGNOME,      NVL(candi.CANDI_CODICE_FISCALE,'') CANDI_CODICE_FISCALE, candi.CANDI_DATA_NASCITA, canle.CANLE_NUM_CANDIDATURA, seggi_num_seggi, codCP, codCiRC, descCP, terrCP, descCirc   FROM territorio terr   JOIN CL_CANLE_CANDIDLEADER canle ON canle.CANLE_TERPA_TERRPARTECIPANTE = terr.TERPA_SEQ_TERRPARTECIPANTE_PK   JOIN CL_CANDI_CANDIDATI candi ON candi.CANDI_SEQ_CANDIDATO_PK = canle.CANLE_CANDI_CANDIDATO   left JOIN CT_ALTNO_ALTRONOME altno ON altno.ALTNO_SEQ_ALTRONOME_PK = candi.CANDI_ALTNO_ALTRONOME    JOIN CL_CALIS_CANDIDATOLISTA calis ON calis.CALIS_CANLE_CANDIDATO = canle.CANLE_CANDI_CANDIDATO    WHERE calis.CALIS_CANLE_TERRPARTECIPANTE = CANLE.CANLE_TERPA_TERRPARTECIPANTE  ),  votiLeader AS (   SELECT CL.*, nvl(votle.VOTLE_NUM_VOTI_VAL, 0) as VOTLE_NUM_VOTI_VAL, nvl(votle.VOTLE_NUM_VOTI_SOLO_CANDID_VAL, 0) as VOTLE_NUM_VOTI_SOLO_CANDID_VAL   FROM candLis cl    LEFT JOIN CL_VOTLE_VOTILEADER votle ON VOTLE.VOTLE_CANLE_CANDIDATO = cl.CANLE_CANDI_CANDIDATO AND votle.VOTLE_CANLE_TERRPARTECIPANTE = cl.CANLE_TERPA_TERRPARTECIPANTE  ),  votiListe AS (   SELECT votli.votli_liste_lista, SUM(nvl(votli.VOTLI_NUM_VOTI_VAL, 0) + NVL(scorp.SCORP_VAL_SCORPORO, 0)) AS votiVal, liste.LISTE_DESCR_LISTA, liste.liste_seq_lista_pk,   CONTR_DESCR_CONTRASSEGNO, PARTI_DESCR_DENOMINAZIONE, CONTR_AGGRE_AGGREGATIRIPARTO, coter.COTER_TERPA_TERRPARTECIPANTE, coter.COTER_ACQTE_ACQTERRITORIO, coter.COTER_CONTR_CONTRASSEGNO, coter.COTER_CONTR_PRG_CONTRASSEGNO, coter.COTER_COALI_COALIZIONE, coter.COTER_RICUS_RICUSAZIONE, liste.LISTE_FLAG_MINORANZA   FROM candLis cl    JOIN CL_VOTLI_VOTILISTA votli ON VOTLI.VOTLI_LISTE_LISTA = cl.CALIS_LISTE_LISTA    JOIN CL_LISTE_LISTE liste ON liste.LISTE_SEQ_LISTA_PK = VOTLI.VOTLI_LISTE_LISTA    left JOIN CL_SCORP_SCORPORO scorp ON scorp.SCORP_CALIS_LISTA = liste.LISTE_SEQ_LISTA_PK and scorp.SCORP_CALIS_CANDIDATO = cl.CANLE_CANDI_CANDIDATO and scorp.SCORP_CALIS_TERRPARTECIPANTE = cl.CANLE_TERPA_TERRPARTECIPANTE    JOIN CL_CONTR_CONTRASSEGNI contr ON contr.CONTR_SEQ_CONTRASSEGNO_PK = liste.LISTE_CONTR_CONTRASSEGNO AND contr.CONTR_PRG_CONTRASSEGNO_PK = liste.LISTE_CONTR_PRG_CONTRASSEGNO  and CONTR_AGGRE_AGGREGATIRIPARTO is not null   JOIN CL_COTER_COALITERPA coter ON coter.COTER_LISTE_LISTA = liste.LISTE_SEQ_LISTA_PK and coter.COTER_ACQTE_ACQTERRITORIO = liste.LISTE_ACQTE_ACQTERRITORIO   left JOIN CL_CONPA_CONTRASSEGNIPARTITI conpa      ON CONPA.CONPA_CONTR_CONTRASSEGNO = CONTR.CONTR_SEQ_CONTRASSEGNO_PK      AND CONPA.CONPA_CONTR_PRG_CONTRASSEGNO = CONTR.CONTR_PRG_CONTRASSEGNO_PK   left JOIN CL_PARTI_PARTITI parti      ON PARTI.PARTI_SEQ_PARTITO_PK = CONPA.CONPA_PARTI_PARTITO where coter.COTER_RICUS_RICUSAZIONE = 1  GROUP BY votli_liste_lista, LISTE_DESCR_LISTA, liste_seq_lista_pk, coter.COTER_COALI_COALIZIONE, CONTR_DESCR_CONTRASSEGNO, PARTI_DESCR_DENOMINAZIONE,     CONTR_AGGRE_AGGREGATIRIPARTO, coter.COTER_TERPA_TERRPARTECIPANTE, coter.COTER_ACQTE_ACQTERRITORIO, coter.COTER_CONTR_CONTRASSEGNO, coter.COTER_CONTR_PRG_CONTRASSEGNO, coter.COTER_COALI_COALIZIONE, coter.COTER_RICUS_RICUSAZIONE, liste.LISTE_FLAG_MINORANZA ) SELECT CONTR_AGGRE_AGGREGATIRIPARTO CCP, votli_liste_lista CODLISTAGRUPPO, seggi_num_seggi NUMSEGGILISTA, codCIRC CODCIRCCAMERA, descCIRC DESCCIRCCAMERA, CANLE_TERPA_TERRPARTECIPANTE CODCOLLEGIOCAMERA, terpa_descr_ente DESCCOLLUNI, codCP CODCOLLPLURI, descCP DESCCOLLPLURI, CANLE_CANDI_CANDIDATO CODCANDIDATO, NVL(VOTLE_NUM_VOTI_VAL, 0) VOTICANDIDATO, NVL(VOTLE_NUM_VOTI_SOLO_CANDID_VAL, 0) VOTISOLOCAND, CANDI_DATA_NASCITA DATANASCITA, CANDI_CODICE_FISCALE CODICEFISCALE, votiVal VOTILISTA, nvl(LISTE_FLAG_MINORANZA,-1) LISTAMINORANZA, COTER_COALI_COALIZIONE CODCOAL, CANLE_NUM_CANDIDATURA NUMORDINE, CANLE_TERPA_TERRPARTECIPANTE || codCP NUMPROGRAREALISTA, NVL ( LISTE_DESCR_LISTA, nvl(PARTI_DESCR_DENOMINAZIONE, CONTR_DESCR_CONTRASSEGNO)) AS DESCLISTA, 0 NUMVOTICIRCOSCRIZIONE, COTER_TERPA_TERRPARTECIPANTE, COTER_ACQTE_ACQTERRITORIO, COTER_CONTR_CONTRASSEGNO, COTER_CONTR_PRG_CONTRASSEGNO, COTER_COALI_COALIZIONE, COTER_RICUS_RICUSAZIONE, terrCP  		 FROM votiLeader vle   JOIN votiListe vli ON vli.votli_liste_lista = vle.CALIS_LISTE_LISTA";
		String queryNew = "WITH TERRITORIO AS (SELECT        TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK ID_PLURI,        TERPA.TERPA_DESCR_ENTE DESC_PLURI,        SEGGI.SEGGI_NUM_SEGGI,        TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK    ID_CIRC,        TERPACP.TERPA_DESCR_ENTE                 DESC_CIRC            FROM        CL_TERPA_TERRPARTECIPANTI  TERPA        LEFT JOIN CL_SEGGI_SEGGI             SEGGI ON SEGGI.SEGGI_TERPA_TERRPARTECIPANTE = TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK        LEFT JOIN CL_TERPA_TERRPARTECIPANTI  TERPACP ON TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE        LEFT JOIN CL_TERPA_TERRPARTECIPANTI  TERPACIRC ON TERPACIRC.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPACP.TERPA_TERPA_TERRPARTECIPANTE    WHERE        TERPA.TERPA_GERTE_GERARCTERRITORIO = 11    CONNECT BY        PRIOR TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE    START WITH TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = ?    ),VOTI_CANLE AS (    	SELECT   CANDI.CANDI_SEQ_CANDIDATO_PK ID_CANDIDATO,   CANDI.CANDI_DESCR_NOME,  CANDI.CANDI_DESCR_COGNOME,   CANDI.CANDI_DATA_NASCITA,  CALIS.CALIS_LISTE_LISTA ID_LISTA,  TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK  ID_TERPA_CANDIDATO,   TERPA.TERPA_DESCR_ENTE DESC_TERPA_CANDIDATO,   TERPA.TERPA_TERPA_TERRPARTECIPANTE ID_TERPA_PLURI,   T.ID_PLURI,  T.DESC_PLURI,  T.ID_CIRC,  T.DESC_CIRC,  T.SEGGI_NUM_SEGGI,  SUM(VOTLE.VOTLE_NUM_VOTI_VAL)VOTLE_NUM_VOTI_VAL,     SUM(VOTLE.VOTLE_NUM_VOTI_SOLO_CANDID_VAL)VOTLE_NUM_VOTI_SOLO_CANDID_VAL  FROM CL_VOTLE_VOTILEADER VOTLE  INNER JOIN CL_CANLE_CANDIDLEADER CANLE ON CANLE.CANLE_CANDI_CANDIDATO  = VOTLE.VOTLE_CANLE_CANDIDATO  AND VOTLE.VOTLE_CANLE_TERRPARTECIPANTE = CANLE.CANLE_TERPA_TERRPARTECIPANTE   INNER JOIN CL_CANDI_CANDIDATI CANDI ON CANDI.CANDI_SEQ_CANDIDATO_PK  = CANLE.CANLE_CANDI_CANDIDATO   INNER JOIN CL_TERPA_TERRPARTECIPANTI TERPA  ON TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK  = VOTLE.VOTLE_CANLE_TERRPARTECIPANTE  INNER JOIN TERRITORIO T ON T.ID_PLURI = TERPA.TERPA_TERPA_TERRPARTECIPANTE  INNER JOIN CL_CALIS_CANDIDATOLISTA CALIS ON CALIS.CALIS_CANLE_CANDIDATO  = CANLE.CANLE_CANDI_CANDIDATO   GROUP BY TERPA.TERPA_DESCR_ENTE,   CANDI.CANDI_SEQ_CANDIDATO_PK,   CANDI.CANDI_DESCR_NOME,  CANDI.CANDI_DESCR_COGNOME,  CANDI.CANDI_DATA_NASCITA,  CALIS.CALIS_LISTE_LISTA,  TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK,   TERPA.TERPA_DESCR_ENTE,   TERPA.TERPA_TERPA_TERRPARTECIPANTE,   T.ID_PLURI,  T.DESC_PLURI,  T.ID_CIRC,  T.DESC_CIRC,  T.SEGGI_NUM_SEGGI  ), VOTI_LISTA AS ( 	SELECT  		CANLE.ID_CANDIDATO, 	  	CANLE.CANDI_DESCR_NOME,	  	CANLE.CANDI_DESCR_COGNOME, 	  	CANLE.CANDI_DATA_NASCITA,	  	CANLE.ID_TERPA_CANDIDATO, 	  	CANLE.DESC_TERPA_CANDIDATO, 	  	CANLE.ID_TERPA_PLURI, 	  	CANLE.DESC_PLURI,	  	CANLE.ID_CIRC,	  	CANLE.DESC_CIRC,	  	CANLE.SEGGI_NUM_SEGGI,	  	CANLE.VOTLE_NUM_VOTI_VAL,   	  	CANLE.VOTLE_NUM_VOTI_SOLO_CANDID_VAL,        SUM(NVL(VOTLI.VOTLI_NUM_VOTI_VAL, 0) + NVL(SCORP.SCORP_VAL_SCORPORO, 0)) AS VOTI_LISTA,        LISTE.LISTE_DESCR_LISTA,        LISTE.LISTE_SEQ_LISTA_PK,        CONTR_DESCR_CONTRASSEGNO,        PARTI_DESCR_DENOMINAZIONE,        CONTR_AGGRE_AGGREGATIRIPARTO,        COTER.COTER_TERPA_TERRPARTECIPANTE,        COTER.COTER_ACQTE_ACQTERRITORIO,        COTER.COTER_CONTR_CONTRASSEGNO,        COTER.COTER_CONTR_PRG_CONTRASSEGNO,        COTER.COTER_COALI_COALIZIONE,        COTER.COTER_RICUS_RICUSAZIONE,        LISTE.LISTE_FLAG_MINORANZA,        PARTI.PARTI_CCP CCP 	FROM VOTI_CANLE CANLE 	INNER JOIN CL_VOTLI_VOTILISTA VOTLI ON VOTLI.VOTLI_LISTE_LISTA  = CANLE.ID_LISTA 	INNER JOIN CL_LISTE_LISTE LISTE ON LISTE.LISTE_SEQ_LISTA_PK = VOTLI.VOTLI_LISTE_LISTA  	LEFT JOIN CL_SCORP_SCORPORO SCORP ON SCORP.SCORP_CALIS_LISTA = LISTE.LISTE_SEQ_LISTA_PK                                             AND SCORP.SCORP_CALIS_CANDIDATO = CANLE.ID_CANDIDATO                                             AND SCORP.SCORP_CALIS_TERRPARTECIPANTE = CANLE.ID_TERPA_CANDIDATO  	JOIN CL_CONTR_CONTRASSEGNI CONTR ON CONTR.CONTR_SEQ_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_CONTRASSEGNO                                            AND CONTR.CONTR_PRG_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_PRG_CONTRASSEGNO                                            AND CONTR_AGGRE_AGGREGATIRIPARTO IS NOT NULL  	JOIN CL_COTER_COALITERPA COTER ON COTER.COTER_LISTE_LISTA = LISTE.LISTE_SEQ_LISTA_PK                                          AND COTER.COTER_ACQTE_ACQTERRITORIO = LISTE.LISTE_ACQTE_ACQTERRITORIO  	LEFT JOIN CL_CONPA_CONTRASSEGNIPARTITI CONPA ON CONPA.CONPA_CONTR_CONTRASSEGNO = CONTR.CONTR_SEQ_CONTRASSEGNO_PK                                                        AND CONPA.CONPA_CONTR_PRG_CONTRASSEGNO = CONTR.CONTR_PRG_CONTRASSEGNO_PK  	LEFT JOIN CL_PARTI_PARTITI PARTI ON PARTI.PARTI_SEQ_PARTITO_PK = CONPA.CONPA_PARTI_PARTITO    	WHERE        	COTER.COTER_RICUS_RICUSAZIONE = 1    GROUP BY     	CANLE.ID_CANDIDATO, 	  	CANLE.CANDI_DESCR_NOME,	  	CANLE.CANDI_DESCR_COGNOME, 	  	CANLE.CANDI_DATA_NASCITA,	  	CANLE.ID_TERPA_CANDIDATO, 	  	CANLE.DESC_TERPA_CANDIDATO, 	  	CANLE.ID_TERPA_PLURI, 	  	CANLE.DESC_PLURI,	  	CANLE.ID_CIRC,	  	CANLE.DESC_CIRC,	  	CANLE.SEGGI_NUM_SEGGI,	  	CANLE.VOTLE_NUM_VOTI_VAL,   	  	CANLE.VOTLE_NUM_VOTI_SOLO_CANDID_VAL,        LISTE.LISTE_DESCR_LISTA,        LISTE.LISTE_SEQ_LISTA_PK,        CONTR_DESCR_CONTRASSEGNO,        PARTI_DESCR_DENOMINAZIONE,        CONTR_AGGRE_AGGREGATIRIPARTO,        COTER.COTER_TERPA_TERRPARTECIPANTE,        COTER.COTER_ACQTE_ACQTERRITORIO,        COTER.COTER_CONTR_CONTRASSEGNO,        COTER.COTER_CONTR_PRG_CONTRASSEGNO,        COTER.COTER_COALI_COALIZIONE,        COTER.COTER_RICUS_RICUSAZIONE,        LISTE.LISTE_FLAG_MINORANZA,        PARTI.PARTI_CCP  )  SELECT V.*  FROM VOTI_LISTA V";

		Object[] parameters = { 105255592 };

		List<Base> baseList = new ArrayList<>();

		AtomicInteger i = new AtomicInteger();

		jdbcTemplate.query(queryNew, parameters, new ResultSetExtractor<List<Object[]>>() {

			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {

					i.set(1);

					Base b = new Base();

					b.setIdCandidato(rs.getInt(i.getAndIncrement()));
					b.setNome(rs.getString(i.getAndIncrement()));
					b.setCognome(rs.getString(i.getAndIncrement()));
					b.setDataNascita(rs.getDate(i.getAndIncrement()));
					b.setIdTerpaCandidato(rs.getInt(i.getAndIncrement()));
					b.setDescTerpaCandidato(rs.getString(i.getAndIncrement()));
					b.setIdCollegioPluri(rs.getInt(i.getAndIncrement()));
					b.setDescCollegioPluri(rs.getString(i.getAndIncrement()));
					b.setIdCircoscrizione(rs.getInt(i.getAndIncrement()));
					b.setDescCircoscrizione(rs.getString(i.getAndIncrement()));
					b.setNumSeggi(rs.getInt(i.getAndIncrement()));
					b.setVotiTotCand(rs.getInt(i.getAndIncrement()));
					b.setVotiSoloCand(rs.getInt(i.getAndIncrement()));
					b.setVotiLista(rs.getInt(i.getAndIncrement()));
					b.setDescLista(rs.getString(i.getAndIncrement()));
					b.setIdLista(rs.getInt(i.getAndIncrement()));
					b.setDescContrassegno(rs.getString(i.getAndIncrement()));
					b.setDescPartito(rs.getString(i.getAndIncrement()));
					b.setIdAggregatoRiparto(rs.getInt(i.getAndIncrement()));
					b.setCoteTerpa(rs.getInt(i.getAndIncrement()));
					b.setCoterAcqte(rs.getInt(i.getAndIncrement()));
					b.setCoterContr(rs.getInt(i.getAndIncrement()));
					b.setCoterPrgContr(rs.getInt(i.getAndIncrement()));
					b.setCoterCoali(rs.getInt(i.getAndIncrement()));
					b.setCoterRicus(rs.getInt(i.getAndIncrement()));
					b.setFlagMinoranza(rs.getString(i.getAndIncrement()));
					b.setCcp(rs.getInt(i.getAndIncrement()));

					baseList.add(b);

				}
				return null;
			}
		});

		return baseList;

	}
	
	
	public List<Base> getDataLocal() {
		
		String queryNew = "SELECT * FROM PROVA_RIPARTO";
		
		List<Base> baseList = new ArrayList<>();
		
		AtomicInteger i = new AtomicInteger();
		
		jdbcTemplate.query(queryNew, new ResultSetExtractor<List<Object[]>>() {
			
			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {
					
					i.set(1);
					
					Base b = new Base();
					
					b.setIdCandidato(rs.getInt(i.getAndIncrement()));
					b.setNome(rs.getString(i.getAndIncrement()));
					b.setCognome(rs.getString(i.getAndIncrement()));
					b.setDataNascita(rs.getDate(i.getAndIncrement()));
					b.setIdTerpaCandidato(rs.getInt(i.getAndIncrement()));
					b.setDescTerpaCandidato(rs.getString(i.getAndIncrement()));
					b.setIdCollegioPluri(rs.getInt(i.getAndIncrement()));
					b.setDescCollegioPluri(rs.getString(i.getAndIncrement()));
					b.setIdCircoscrizione(rs.getInt(i.getAndIncrement()));
					b.setDescCircoscrizione(rs.getString(i.getAndIncrement()));
					b.setNumSeggi(rs.getInt(i.getAndIncrement()));
					b.setVotiTotCand(rs.getInt(i.getAndIncrement()));
					b.setVotiSoloCand(rs.getInt(i.getAndIncrement()));
					b.setVotiLista(rs.getInt(i.getAndIncrement()));
					b.setDescLista(rs.getString(i.getAndIncrement()));
					b.setIdLista(rs.getInt(i.getAndIncrement()));
					b.setDescContrassegno(rs.getString(i.getAndIncrement()));
					b.setDescPartito(rs.getString(i.getAndIncrement()));
					b.setIdAggregatoRiparto(rs.getInt(i.getAndIncrement()));
					b.setCoteTerpa(rs.getInt(i.getAndIncrement()));
					b.setCoterAcqte(rs.getInt(i.getAndIncrement()));
					b.setCoterContr(rs.getInt(i.getAndIncrement()));
					b.setCoterPrgContr(rs.getInt(i.getAndIncrement()));
					b.setCoterCoali(rs.getInt(i.getAndIncrement()));
					b.setCoterRicus(rs.getInt(i.getAndIncrement()));
					b.setFlagMinoranza(rs.getString(i.getAndIncrement()));
					b.setCcp(rs.getInt(i.getAndIncrement()));
					
					baseList.add(b);
					
				}
				return null;
			}
		});
		
		return baseList;
		
	}
}
