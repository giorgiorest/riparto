package com.elettorale.riparto.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.lang.NonNull;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elettorale.riparto.calcolo.RipartoCamera;
import com.elettorale.riparto.dto.Base;
import com.elettorale.riparto.utils.Territorio;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class Riparto {

	@Autowired
	JdbcTemplate jdbcTemplate;

	Logger log = LoggerFactory.getLogger(Riparto.class);
		
	@ApiOperation(value = "Inserisci la data elezione nel formato DD/MM/YYYY")
	@RequestMapping(value = "/eseguiRipartoCamera",method = RequestMethod.GET)
	public ResponseEntity<String> eseguiRiparto(@ApiParam(example = "DD/MM/YYYY") @NonNull@RequestParam("dataElezione") String dataElezione) {

		StopWatch sw = new StopWatch();
		String path;
		try {
			sw.start();
			
			log.info("RECUPERO DATI PER ELEZIONE {}--------", dataElezione);
			Integer idEnteItalia = getEnte(dataElezione);
			
			List<Base> baseList = getData(idEnteItalia);
			List<Base> baseListCandi = getDataCandidati(idEnteItalia);
			List<Territorio> listTerritori = getSeggi(idEnteItalia);
			log.info("DATI RECUPERATI--------");
			
			RipartoCamera riparto = new RipartoCamera(baseList, baseListCandi, listTerritori);
			
			path = riparto.eseguiRiparto(false, dataElezione);
			
			log.info("FINE RIPARTO");
			sw.stop();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
		Calendar gc = GregorianCalendar.getInstance();

		return ResponseEntity.ok("Generato riparto in: " + String.valueOf(sw.getTotalTimeSeconds()) + "  ->  "
				+ gc.get(Calendar.HOUR_OF_DAY) + ":" + gc.get(Calendar.MINUTE)
				+ ", " + path);
	}

	public List<Territorio> getSeggi(Integer idEnteItalia){
		String getSeggi = "SELECT\r\n"
				+ "	TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK ID_PLURI,\r\n"
				+ "	TERPA.TERPA_DESCR_ENTE DESC_PLURI,\r\n"
				+ " SEGGI.SEGGI_NUM_SEGGI,\r\n"
				+ " TERPA.TERPA_COD_ENTE_LIVELLO COD_ENTE_PLURI,\r\n"
				+ "	TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK ID_CIRC,\r\n"
				+ "	TERPACP.TERPA_DESCR_ENTE DESC_CIRC,\r\n"
				+ " TERPACP.TERPA_COD_ENTE_LIVELLO\r\n"
				+ "FROM\r\n"
				+ "	CL_TERPA_TERRPARTECIPANTI TERPA\r\n"
				+ "LEFT JOIN CL_SEGGI_SEGGI SEGGI ON\r\n"
				+ "	SEGGI.SEGGI_TERPA_TERRPARTECIPANTE = TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK\r\n"
				+ "LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACP ON\r\n"
				+ "	TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACIRC ON\r\n"
				+ "	TERPACIRC.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPACP.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "WHERE\r\n"
				+ "	TERPA.TERPA_GERTE_GERARCTERRITORIO = 11\r\n"
				+ "CONNECT BY\r\n"
				+ "	PRIOR TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "START WITH\r\n"
				+ "	TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = ? ";
		
		Object[] parameters = { idEnteItalia };

		List<Territorio> baseList = new ArrayList<>();

		AtomicInteger i = new AtomicInteger();

		jdbcTemplate.query(getSeggi, parameters, new ResultSetExtractor<List<Object[]>>() {

			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {

					i.set(1);

					Territorio pluri = new Territorio();

					pluri.setId(rs.getInt(i.getAndIncrement()));
					pluri.setDescrizione(rs.getString(i.getAndIncrement()));
					pluri.setNumSeggi(rs.getInt(i.getAndIncrement()));
					pluri.setCodEnte(Integer.parseInt(rs.getString(i.getAndIncrement())));
					
					Territorio circ = new Territorio();

					circ.setId(rs.getInt(i.getAndIncrement()));
					circ.setDescrizione(rs.getString(i.getAndIncrement()));
					circ.setCodEnte(Integer.parseInt(rs.getString(i.getAndIncrement())));
					
					pluri.setPadre(circ);
					
					baseList.add(pluri);

				}
				return null;
			}
		});

		return baseList;
	};
	
	@SuppressWarnings("deprecation")
	public List<Base> getDataCandidati(Integer idEnteItalia){
		String getCandiUnu = "SELECT \r\n"
				+ "	V.VOTLE_CANLE_CANDIDATO,\r\n"
				+ "	V.VOTLE_CANLE_TERRPARTECIPANTE,\r\n"
				+ "	T.TERPA_DESCR_ENTE,\r\n"
				+ "	PADRE.TERPA_SEQ_TERRPARTECIPANTE_PK ,\r\n"
				+ "	PADRE.TERPA_DESCR_ENTE, \r\n"
				+ "	C.CANDI_DATA_NASCITA, \r\n"
				+ "	L.LISTE_SEQ_LISTA_PK,\r\n"
				+ "	L.LISTE_DESCR_LISTA ,\r\n"
				+ "	CONTR.CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "	SUM(V.VOTLE_NUM_VOTI_VAL) VOTI,\r\n"
				+ "	SUM(V.VOTLE_NUM_VOTI_SOLO_CANDID_VAL) VOTI_SOLO_CAND,\r\n"
				+ "	SCO.SCORP_VAL_SCORPORO ,\r\n"
				+ "	CANLE.CANLE_FLAG_ELETTO,	\r\n"
				+ "	SUM(VOTLI.VOTLI_NUM_VOTI_VAL)\r\n"
				+ "FROM CL_VOTLE_VOTILEADER V\r\n"
				+ "JOIN CL_CANLE_CANDIDLEADER CANLE ON CANLE.CANLE_CANDI_CANDIDATO  = V.VOTLE_CANLE_CANDIDATO \r\n"
				+ "							AND CANLE.CANLE_TERPA_TERRPARTECIPANTE  = V.VOTLE_CANLE_TERRPARTECIPANTE \r\n"
				+ "JOIN CL_SCRUT_SCRUTINI S ON S.SCRUT_SEQ_SCRUTINIO_PK  = V.VOTLE_SCRUT_SCRUTINIO \r\n"
				+ "JOIN CL_TERPA_TERRPARTECIPANTI T ON T.TERPA_SEQ_TERRPARTECIPANTE_PK  = V.VOTLE_CANLE_TERRPARTECIPANTE\r\n"
				+ "JOIN CL_TERPA_TERRPARTECIPANTI PADRE ON PADRE.TERPA_SEQ_TERRPARTECIPANTE_PK  = T.TERPA_TERPA_TERRPARTECIPANTE \r\n"
				+ "JOIN CL_CANDI_CANDIDATI C ON C.CANDI_SEQ_CANDIDATO_PK  = V.VOTLE_CANLE_CANDIDATO \r\n"
				+ "JOIN CL_CALIS_CANDIDATOLISTA CAL ON CAL.CALIS_CANLE_CANDIDATO = C.CANDI_SEQ_CANDIDATO_PK \r\n"
				+ "JOIN CL_LISTE_LISTE L ON L.LISTE_SEQ_LISTA_PK  = CAL.CALIS_LISTE_LISTA \r\n"
				+ "JOIN CL_VOTLI_VOTILISTA VOTLI ON VOTLI.VOTLI_LISTE_LISTA = CAL.CALIS_LISTE_LISTA \r\n"
				+ "							 AND	VOTLI.VOTLI_SCRUT_SCRUTINIO = S.SCRUT_SEQ_SCRUTINIO_PK \r\n"
				+ "LEFT JOIN CL_SCORP_SCORPORO SCO ON SCO.SCORP_CALIS_CANDIDATO = V.VOTLE_CANLE_CANDIDATO \r\n"
				+ "								AND SCO.SCORP_CALIS_TERRPARTECIPANTE  = V.VOTLE_CANLE_TERRPARTECIPANTE \r\n"
				+ "								AND SCO.SCORP_CALIS_LISTA = L.LISTE_SEQ_LISTA_PK \r\n"
				+ "JOIN CL_CONTR_CONTRASSEGNI CONTR ON\r\n"
				+ "			CONTR.CONTR_SEQ_CONTRASSEGNO_PK = l.LISTE_CONTR_CONTRASSEGNO\r\n"
				+ "			AND CONTR.CONTR_PRG_CONTRASSEGNO_PK = l.LISTE_CONTR_PRG_CONTRASSEGNO\r\n"
				+ "			AND CONTR_AGGRE_AGGREGATIRIPARTO IS NOT NULL\r\n"
				+ "WHERE S.SCRUT_TERPA_TERRPARTECIPANTE  IN (SELECT\r\n"
				+ "	terpa.TERPA_SEQ_TERRPARTECIPANTE_PK \r\n"
				+ "FROM\r\n"
				+ "	CL_TERPA_TERRPARTECIPANTI terpa\r\n"
				+ "CONNECT BY\r\n"
				+ "	PRIOR terpa.TERPA_SEQ_TERRPARTECIPANTE_PK = terpa.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "START WITH\r\n"
				+ "	terpa.TERPA_SEQ_TERRPARTECIPANTE_PK = ?)\r\n"
				+ "GROUP BY V.VOTLE_CANLE_CANDIDATO ,\r\n"
				+ "V.VOTLE_CANLE_TERRPARTECIPANTE , \r\n"
				+ "T.TERPA_DESCR_ENTE,\r\n"
				+ "PADRE.TERPA_SEQ_TERRPARTECIPANTE_PK ,\r\n"
				+ "PADRE.TERPA_DESCR_ENTE, \r\n"
				+ "C.CANDI_DATA_NASCITA,\r\n"
				+ "L.LISTE_SEQ_LISTA_PK, \r\n"
				+ "L.LISTE_DESCR_LISTA ,\r\n"
				+ "CONTR.CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "SCO.SCORP_VAL_SCORPORO ,\r\n"
				+ "CANLE.CANLE_FLAG_ELETTO";
		
		Object[] parameters = { idEnteItalia };

		List<Base> baseList = new ArrayList<>();

		AtomicInteger i = new AtomicInteger();

		jdbcTemplate.query(getCandiUnu, parameters, new ResultSetExtractor<List<Object[]>>() {

			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {

					i.set(1);

					Base b = new Base();

					b.setIdCandidato(rs.getInt(i.getAndIncrement()));
					b.setIdTerpaCandidato(rs.getInt(i.getAndIncrement()));
					b.setDescTerpaCandidato(rs.getString(i.getAndIncrement()));
					b.setIdCollegioPluri(rs.getInt(i.getAndIncrement()));
					b.setDescCollegioPluri(rs.getString(i.getAndIncrement()));
					b.setDataNascita(rs.getDate(i.getAndIncrement()));
					b.setIdLista(rs.getInt(i.getAndIncrement()));
					b.setDescLista(rs.getString(i.getAndIncrement()));
					b.setIdAggregatoRiparto(rs.getInt(i.getAndIncrement()));
					b.setVotiTotCand(rs.getInt(i.getAndIncrement()));
					b.setVotiSoloCand(rs.getInt(i.getAndIncrement()));
					b.setScorporo(rs.getInt(i.getAndIncrement()));
					b.setEletto(rs.getString(i.getAndIncrement()));
					b.setVotiLista(rs.getInt(i.getAndIncrement()));
					
					baseList.add(b);

				}
				return null;
			}
		});

		return baseList;
	}
	
	@SuppressWarnings("deprecation")
	public List<Base> getData(Integer idEnteItalia) {

		String getVotli ="WITH TERRITORIO AS (\r\n"
				+ "				SELECT\r\n"
				+ "					TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK ID_PLURI,\r\n"
				+ "					TERPA.TERPA_DESCR_ENTE DESC_PLURI,\r\n"
				+ "					SEGGI.SEGGI_NUM_SEGGI,\r\n"
				+ "					TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK ID_CIRC,\r\n"
				+ "					TERPACP.TERPA_DESCR_ENTE DESC_CIRC,\r\n"
				+ "                 TERPACP.TERPA_COD_ENTE_LIVELLO\r\n"
				+ "				FROM\r\n"
				+ "					CL_TERPA_TERRPARTECIPANTI TERPA\r\n"
				+ "				LEFT JOIN CL_SEGGI_SEGGI SEGGI ON\r\n"
				+ "					SEGGI.SEGGI_TERPA_TERRPARTECIPANTE = TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK\r\n"
				+ "				LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACP ON\r\n"
				+ "					TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "				LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACIRC ON\r\n"
				+ "					TERPACIRC.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPACP.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "				WHERE\r\n"
				+ "					TERPA.TERPA_GERTE_GERARCTERRITORIO = 11\r\n"
				+ "				CONNECT BY\r\n"
				+ "					PRIOR TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "				START WITH\r\n"
				+ "					TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = ? ),\r\n"
				+ "				VOTI_LISTA AS (\r\n"
				+ "				SELECT	\r\n"
				+ "					TERR.ID_PLURI,\r\n"
				+ "					TERR.DESC_PLURI,\r\n"
				+ "                 TERR.TERPA_COD_ENTE_LIVELLO COD_ENTE,\r\n"
				+ "					TERR.ID_CIRC,\r\n"
				+ "					TERR.DESC_CIRC,\r\n"
				+ "					TERR.SEGGI_NUM_SEGGI,\r\n"
				+ "					SUM(NVL(VOTLI.VOTLI_NUM_VOTI_VAL, 0) + NVL(SCORP.SCORP_VAL_SCORPORO, 0)) AS VOTI_LISTA,\r\n"
				+ "					LISTE.LISTE_DESCR_LISTA,\r\n"
				+ "					LISTE.LISTE_SEQ_LISTA_PK,\r\n"
				+ "					CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "					PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "					CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "					COTER.COTER_TERPA_TERRPARTECIPANTE,\r\n"
				+ "					COTER.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "					COTER.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "					COTER.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "					COTER.COTER_COALI_COALIZIONE,\r\n"
				+ "					COTER.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "					LISTE.LISTE_FLAG_MINORANZA,\r\n"
				+ "					PARTI.PARTI_CCP CCP\r\n"
				+ "				FROM\r\n"
				+ "					 CL_VOTLI_VOTILISTA VOTLI \r\n"
				+ "				INNER JOIN CL_LISTE_LISTE LISTE ON\r\n"
				+ "					LISTE.LISTE_SEQ_LISTA_PK = VOTLI.VOTLI_LISTE_LISTA\r\n"
				+ "				LEFT JOIN CL_SCORP_SCORPORO SCORP ON\r\n"
				+ "					SCORP.SCORP_CALIS_LISTA = LISTE.LISTE_SEQ_LISTA_PK\r\n"
				+ "				LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPA_SCO ON TERPA_SCO.TERPA_SEQ_TERRPARTECIPANTE_PK  = SCORP.SCORP_CALIS_TERRPARTECIPANTE\r\n"
				+ "				JOIN CL_CONTR_CONTRASSEGNI CONTR ON\r\n"
				+ "					CONTR.CONTR_SEQ_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_CONTRASSEGNO\r\n"
				+ "					AND CONTR.CONTR_PRG_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_PRG_CONTRASSEGNO\r\n"
				+ "					AND CONTR_AGGRE_AGGREGATIRIPARTO IS NOT NULL\r\n"
				+ "				JOIN CL_COTER_COALITERPA COTER ON\r\n"
				+ "					COTER.COTER_LISTE_LISTA = LISTE.LISTE_SEQ_LISTA_PK\r\n"
				+ "					AND COTER.COTER_ACQTE_ACQTERRITORIO = LISTE.LISTE_ACQTE_ACQTERRITORIO\r\n"
				+ "				JOIN TERRITORIO TERR ON TERR.ID_PLURI = COTER.COTER_TERPA_TERRPARTECIPANTE \r\n"
				+ "				LEFT JOIN CL_CONPA_CONTRASSEGNIPARTITI CONPA ON\r\n"
				+ "					CONPA.CONPA_CONTR_CONTRASSEGNO = CONTR.CONTR_SEQ_CONTRASSEGNO_PK\r\n"
				+ "					AND CONPA.CONPA_CONTR_PRG_CONTRASSEGNO = CONTR.CONTR_PRG_CONTRASSEGNO_PK\r\n"
				+ "				LEFT JOIN CL_PARTI_PARTITI PARTI ON\r\n"
				+ "					PARTI.PARTI_SEQ_PARTITO_PK = CONPA.CONPA_PARTI_PARTITO\r\n"
				+ "				WHERE\r\n"
				+ "					COTER.COTER_RICUS_RICUSAZIONE = 1\r\n"
				+ "					AND VOTLI.VOTLI_NUM_VOTI_VAl <> -1 \r\n"
				+ "					AND ( SCORP.SCORP_VAL_SCORPORO IS NULL OR SCORP.SCORP_VAL_SCORPORO > -1)\r\n"			
				+ "				GROUP BY\r\n"
				+ "					TERR.ID_PLURI,\r\n"
				+ "					TERR.DESC_PLURI,\r\n"
				+ "					TERR.ID_CIRC,\r\n"
				+ "					TERR.DESC_CIRC,\r\n"
				+ "                    TERR.TERPA_COD_ENTE_LIVELLO,\r\n"
				+ "					TERR.SEGGI_NUM_SEGGI,\r\n"
				+ "					LISTE.LISTE_DESCR_LISTA,\r\n"
				+ "					LISTE.LISTE_SEQ_LISTA_PK,\r\n"
				+ "					CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "					PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "					CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "					COTER.COTER_TERPA_TERRPARTECIPANTE,\r\n"
				+ "					COTER.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "					COTER.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "					COTER.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "					COTER.COTER_COALI_COALIZIONE,\r\n"
				+ "					COTER.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "					LISTE.LISTE_FLAG_MINORANZA,\r\n"
				+ "					PARTI.PARTI_CCP )\r\n"
				+ "				SELECT\r\n"
				+ "					V.*\r\n"
				+ "				FROM\r\n"
				+ "					VOTI_LISTA V ";
		Object[] parameters = { idEnteItalia };

		List<Base> baseList = new ArrayList<>();

		AtomicInteger i = new AtomicInteger();

		jdbcTemplate.query(getVotli, parameters, new ResultSetExtractor<List<Object[]>>() {

			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {

					i.set(1);

					Base b = new Base();

					b.setIdCollegioPluri(rs.getInt(i.getAndIncrement()));
					b.setDescCollegioPluri(rs.getString(i.getAndIncrement()));
					b.setCodEnte(Integer.parseInt(rs.getString(i.getAndIncrement())));
					b.setIdCircoscrizione(rs.getInt(i.getAndIncrement()));
					b.setDescCircoscrizione(rs.getString(i.getAndIncrement()));
					b.setNumSeggi(rs.getInt(i.getAndIncrement()));
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
	
	@SuppressWarnings("deprecation")
	public List<Base> getDataCirc(String dataElezione) {

		Integer idEnteItalia = getEnte(dataElezione);
		
		String getVotli ="WITH TERRITORIO AS (\r\n"
				+ "			SELECT\r\n"
				+ "				TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK ID_PLURI,\r\n"
				+ "				TERPA.TERPA_DESCR_ENTE DESC_PLURI,\r\n"
				+ "				SEGGI.SEGGI_NUM_SEGGI,\r\n"
				+ "				TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK ID_CIRC,\r\n"
				+ "				TERPACP.TERPA_DESCR_ENTE DESC_CIRC\r\n"
				+ "			FROM\r\n"
				+ "				CL_TERPA_TERRPARTECIPANTI TERPA\r\n"
				+ "			LEFT JOIN CL_SEGGI_SEGGI SEGGI ON\r\n"
				+ "				SEGGI.SEGGI_TERPA_TERRPARTECIPANTE = TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK\r\n"
				+ "			LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACP ON\r\n"
				+ "				TERPACP.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "			LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPACIRC ON\r\n"
				+ "				TERPACIRC.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPACP.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "			WHERE\r\n"
				+ "				TERPA.TERPA_GERTE_GERARCTERRITORIO = 11\r\n"
				+ "			CONNECT BY\r\n"
				+ "				PRIOR TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = TERPA.TERPA_TERPA_TERRPARTECIPANTE\r\n"
				+ "			START WITH\r\n"
				+ "				TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK = ? ),\r\n"
				+ "			VOTI_LISTA AS (\r\n"
				+ "			SELECT	\r\n"
				+ "				TERR.ID_PLURI,\r\n"
				+ "				TERR.DESC_PLURI,\r\n"
				+ "				TERR.ID_CIRC,\r\n"
				+ "				TERR.DESC_CIRC,\r\n"
				+ "				TERR.SEGGI_NUM_SEGGI,\r\n"
				+ "				SUM(NVL(VOTLI.VOTLI_NUM_VOTI_VAL, 0) + NVL(SCORP.SCORP_VAL_SCORPORO, 0)) AS VOTI_LISTA,\r\n"
				+ "				LISTE.LISTE_DESCR_LISTA,\r\n"
				+ "				LISTE.LISTE_SEQ_LISTA_PK,\r\n"
				+ "				CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "				PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "				CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "				COTER.COTER_TERPA_TERRPARTECIPANTE,\r\n"
				+ "				COTER.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "				COTER.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "				COTER.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "				COTER.COTER_COALI_COALIZIONE,\r\n"
				+ "				COTER.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "				LISTE.LISTE_FLAG_MINORANZA,\r\n"
				+ "				PARTI.PARTI_CCP CCP\r\n"
				+ "			FROM\r\n"
				+ "				 CL_VOTLI_VOTILISTA VOTLI \r\n"
				+ "			INNER JOIN CL_LISTE_LISTE LISTE ON\r\n"
				+ "				LISTE.LISTE_SEQ_LISTA_PK = VOTLI.VOTLI_LISTE_LISTA\r\n"
				+ "			LEFT JOIN CL_SCORP_SCORPORO SCORP ON\r\n"
				+ "				SCORP.SCORP_CALIS_LISTA = LISTE.LISTE_SEQ_LISTA_PK\r\n"
				+ "			LEFT JOIN CL_TERPA_TERRPARTECIPANTI TERPA_SCO ON TERPA_SCO.TERPA_SEQ_TERRPARTECIPANTE_PK  = SCORP.SCORP_CALIS_TERRPARTECIPANTE\r\n"
				+ "			JOIN CL_CONTR_CONTRASSEGNI CONTR ON\r\n"
				+ "				CONTR.CONTR_SEQ_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_CONTRASSEGNO\r\n"
				+ "				AND CONTR.CONTR_PRG_CONTRASSEGNO_PK = LISTE.LISTE_CONTR_PRG_CONTRASSEGNO\r\n"
				+ "				AND CONTR_AGGRE_AGGREGATIRIPARTO IS NOT NULL\r\n"
				+ "			JOIN CL_COTER_COALITERPA COTER ON\r\n"
				+ "				COTER.COTER_LISTE_LISTA = LISTE.LISTE_SEQ_LISTA_PK\r\n"
				+ "				AND COTER.COTER_ACQTE_ACQTERRITORIO = LISTE.LISTE_ACQTE_ACQTERRITORIO\r\n"
				+ "			JOIN TERRITORIO TERR ON TERR.ID_PLURI = COTER.COTER_TERPA_TERRPARTECIPANTE \r\n"
				+ "			LEFT JOIN CL_CONPA_CONTRASSEGNIPARTITI CONPA ON\r\n"
				+ "				CONPA.CONPA_CONTR_CONTRASSEGNO = CONTR.CONTR_SEQ_CONTRASSEGNO_PK\r\n"
				+ "				AND CONPA.CONPA_CONTR_PRG_CONTRASSEGNO = CONTR.CONTR_PRG_CONTRASSEGNO_PK\r\n"
				+ "			LEFT JOIN CL_PARTI_PARTITI PARTI ON\r\n"
				+ "				PARTI.PARTI_SEQ_PARTITO_PK = CONPA.CONPA_PARTI_PARTITO\r\n"
				+ "			WHERE\r\n"
				+ "				COTER.COTER_RICUS_RICUSAZIONE = 1\r\n"
				+ "			GROUP BY\r\n"
				+ "				TERR.ID_PLURI,\r\n"
				+ "				TERR.DESC_PLURI,\r\n"
				+ "				TERR.ID_CIRC,\r\n"
				+ "				TERR.DESC_CIRC,\r\n"
				+ "				TERR.SEGGI_NUM_SEGGI,\r\n"
				+ "				LISTE.LISTE_DESCR_LISTA,\r\n"
				+ "				LISTE.LISTE_SEQ_LISTA_PK,\r\n"
				+ "				CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "				PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "				CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "				COTER.COTER_TERPA_TERRPARTECIPANTE,\r\n"
				+ "				COTER.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "				COTER.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "				COTER.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "				COTER.COTER_COALI_COALIZIONE,\r\n"
				+ "				COTER.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "				LISTE.LISTE_FLAG_MINORANZA,\r\n"
				+ "				PARTI.PARTI_CCP )\r\n"
				+ "			SELECT\r\n"
				+ "				0 ID_PLURI,\r\n"
				+ "				0 DESC_PLURI,\r\n"
				+ "				V.ID_CIRC,\r\n"
				+ "				V.DESC_CIRC,\r\n"
				+ "				SUM(V.SEGGI_NUM_SEGGI)SEGGI_NUM_SEGGI,\r\n"
				+ "				SUM(VOTI_LISTA) AS VOTI_LISTA,\r\n"
				+ "				V.LISTE_DESCR_LISTA,\r\n"
				+ "				0 LISTE_SEQ_LISTA_PK,\r\n"
				+ "				CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "				PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "				CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "				0 COTER_TERPA_TERRPARTECIPANTE,\r\n"
				+ "				V.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "				V.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "				V.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "				V.COTER_COALI_COALIZIONE,\r\n"
				+ "				V.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "				V.LISTE_FLAG_MINORANZA,\r\n"
				+ "				V.CCP\r\n"
				+ "			FROM\r\n"
				+ "				VOTI_LISTA V\r\n"
				+ "            GROUP BY \r\n"
				+ "                0,\r\n"
				+ "				0,\r\n"
				+ "				V.ID_CIRC,\r\n"
				+ "				V.DESC_CIRC,\r\n"
				+ "				V.LISTE_DESCR_LISTA,\r\n"
				+ "				0,\r\n"
				+ "				CONTR_DESCR_CONTRASSEGNO,\r\n"
				+ "				PARTI_DESCR_DENOMINAZIONE,\r\n"
				+ "				CONTR_AGGRE_AGGREGATIRIPARTO,\r\n"
				+ "				0,\r\n"
				+ "				V.COTER_ACQTE_ACQTERRITORIO,\r\n"
				+ "				V.COTER_CONTR_CONTRASSEGNO,\r\n"
				+ "				V.COTER_CONTR_PRG_CONTRASSEGNO,\r\n"
				+ "				V.COTER_COALI_COALIZIONE,\r\n"
				+ "				V.COTER_RICUS_RICUSAZIONE,\r\n"
				+ "				V.LISTE_FLAG_MINORANZA,\r\n"
				+ "				V.CCP";
		Object[] parameters = { idEnteItalia };

		List<Base> baseList = new ArrayList<>();

		AtomicInteger i = new AtomicInteger();

		jdbcTemplate.query(getVotli, parameters, new ResultSetExtractor<List<Object[]>>() {

			@Override
			public List<Object[]> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {

					i.set(1);

					Base b = new Base();

					b.setIdCollegioPluri(rs.getInt(i.getAndIncrement()));
					b.setDescCollegioPluri(rs.getString(i.getAndIncrement()));
					b.setIdCircoscrizione(rs.getInt(i.getAndIncrement()));
					b.setDescCircoscrizione(rs.getString(i.getAndIncrement()));
					b.setNumSeggi(rs.getInt(i.getAndIncrement()));
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
	
	
	
	private Integer getEnte(String dataElezione) {
		Object[] parameters = { dataElezione };
		String query = "SELECT TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK   FROM CL_TERPA_TERRPARTECIPANTI TERPA JOIN CL_ELEZI_ELEZIONI ELE ON ELE.ELEZI_ID_ELEZIONE_PK  = TERPA.TERPA_ELEZI_ELEZIONE  							AND ELE.ELEZI_TIPEL_TIPOLOGIA_ELEZ = 2 JOIN CL_DATEL_DATEELEZIONI DATEL ON DATEL.DATEL_ID_DATAELEZIONE_PK  = ELE.ELEZI_DATEL_DATAELEZIONE  								AND DATEL.DATEL_DATA_ELEZIONE = TO_DATE(?, 'DD/MM/YYYY') JOIN CF_GERTE_GERARCTERRITORIO GERTE ON GERTE.GERTE_ID_GERARCTERRITORIO_PK  = TERPA.TERPA_GERTE_GERARCTERRITORIO   									AND GERTE.GERTE_TIPGE_TIPOGERARCHIA = 1 									AND GERTE.GERTE_TIPTE_TIPOTERRITORIO  = 1 CONNECT BY PRIOR TERPA.TERPA_SEQ_TERRPARTECIPANTE_PK  = TERPA.TERPA_TERPA_TERRPARTECIPANTE";
		@SuppressWarnings("deprecation")
		Integer idEnte = jdbcTemplate.query(query, parameters, new ResultSetExtractor<Integer>() {

			@Override
			public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				return rs.getInt(1);
			}
		
		});
		return idEnte;
	}

}
