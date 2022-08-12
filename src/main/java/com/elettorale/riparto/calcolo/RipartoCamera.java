package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.JSpinner.ListEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elettorale.riparto.dto.Base;
import com.elettorale.riparto.dto.Coalizione;
import com.elettorale.riparto.utils.Prospetto9;
import com.elettorale.riparto.utils.Territorio;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

public class RipartoCamera extends AppoggioStampa{

	Logger log = LoggerFactory.getLogger(RipartoCamera.class);
	
	private List<Base> list;
	private List<Base> listCandidati;
	private Map<Territorio, List<Coalizione>> mapCircListe = new HashMap<>();
	private Map<Territorio, List<Elemento>> mapCircListeElemento = new HashMap<>();
	
	//----------------NAZIONE-----------------------------//
	//lista eccedntarie(key idCoalizione, value liste)
	private Map<Integer, List<Elemento>> mapEccedentarieCoalizioni = new HashMap<>();
	//mappa deficitarie(key idCoalizione, value liste)
	private Map<Integer, List<Elemento>> mapDeficitarieCoalizioni = new HashMap<>();
	//----------------NAZIONE-----------------------//
	
	
	private Map<Territorio, List<Elemento>> mapCircListeElemento11 = new HashMap<>();
	//----------------CIRCOSCRIZIONE----------------------//
	//lista eccedntarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapEccedentarieListeCirc = new HashMap<>();
	//mappa deficitarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapDeficitarieListeCirc = new HashMap<>();
	//----------------CIRCOSCRIZIONE----------------------//
	
	//----------------COLLEGIO PLURI----------------------//
	private Map<Territorio, List<Elemento>> mapPluriListeElemento = new HashMap<>();
	//lista eccedntarie(key idAggregato, value liste)
	private Map<ChiavePluri, List<Elemento>> mapEccedentarieListePluri = new HashMap<>();
	//mappa deficitarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapDeficitarieListePluri = new HashMap<>();
	//----------------COLLEGIO PLURI----------------------//
	
	private BloccoRiparto bloccoRiparto = null;
	
	private List<Prospetto9> listProspetto9 = new ArrayList<>();
	private List<Prospetto9> listProspetto14 = new ArrayList<>();
	
	List<Territorio> listTerritori;
	
	private Map<Territorio, List<CandidatoUni>> mapTerrCandidati = new HashMap<>();
	private Map<Territorio, List<CandidatoUni>> mapTerrCandidatiEletti = new HashMap<>();
	
	private Map<Integer, Integer> mapCoaliEletti = new HashMap<>();
	
	//map id deficitaria value DIFF prospetto 6
	private Map<Integer, Integer> mapDeficitariaDiff = new HashMap<>();
	//map id deficitaria value DIFF prospetto 11
	private Map<Integer, Integer> mapDeficitariaDiff11 = new HashMap<>();
	
	public RipartoCamera(List<Base> list, List<Base> listCandidati, List<Territorio> listTerritori) {
		this.list = list;
		this.listCandidati = listCandidati;
		this.listTerritori = listTerritori;
	}

	public String eseguiRiparto(boolean isLocal, String dataElezione) throws FileNotFoundException, DocumentException {

		dataElezione = dataElezione.replace("/", "_");

		String path;
		if(isLocal) {
			path = "\\\\nas-files-srv2\\Condivisioni\\acc_inm\\testRipartoExcel\\ripartonew\\riparto_"+dataElezione+".pdf";
		}else {
			path = "\\\\nas-files-srv2\\Condivisioni\\acc_inm\\testRipartoExcel\\ripartonew\\riparto_TEST_"+dataElezione+".pdf";
		}
		PdfWriter.getInstance(document, new FileOutputStream(path));

		document.open();

		//TODO gestione blocco da riparto
		
		//Proclama Eletto
		proclamaEletto();
		
		//Calcolo scrorporo
		determinaScorporo();
		
		//PROSPETTO 1
		List<Coalizione> nazionali = calcolaCifraNazionale();
		
		//PROSPETTO 2
		List<Elemento> ripartoColizioniNazionale = ripartoCoalizioniListeNazionali(nazionali, dataElezione);
			
		//PROSPETTO 3
		List<Elemento> ripartoListeInColizioniNazionale = ripartoTraListeInCoalizione(ripartoColizioniNazionale, nazionali);
		
		if(Objects.isNull(bloccoRiparto) || !bloccoRiparto.equals(BloccoRiparto.BLOCCO_RIPARTO_NAZIONALE_PARITA_CIFRE)) {
			
			//PROSPETTO 5
			ripartoListeCircoscrizione(nazionali);
			
			//PROSPETTO 6 -> Confronto tra livello circ e naz
			List<Confronto> listProspetto6 = confrontoCircoscrizionaleNazionaleCoalizioni(ripartoColizioniNazionale);
			
			if(Objects.isNull(bloccoRiparto) || !bloccoRiparto.equals(BloccoRiparto.BLOCCO_RIPARTO_CIRCOSCRIZIONALE_PARITA_CIFRE)) {
				
				//PROSPETTO 7-8-9 -> Compensazione Eccedentarie Deficitarie Livello circoscrizione coali
				
				//BLOCCO_RIPARTO_LISTE_NAZIONALI_PARITA_CIFRE
				compensazioneCircoscrizionaleCoalizioni(listProspetto6);
				
				//PROSPETTO 10 -> ripartizione seggi tra coalizioni in circosricioni post compensazione
				ripartoSeggiCoalizioniPostCompensazione(listProspetto6);
				
				if(Objects.isNull(bloccoRiparto) || !bloccoRiparto.equals(BloccoRiparto.BLOCCO_RIPARTO_LISTE_CIRCOSCRIZIONALE_PARITA_CIFRE)) {
					
					//PROSPETTO 11 -> Confronto tra livello circ e naz
					List<Confronto> listProspetto11 = confrontoCircoscrizionaleNazionaleListe(ripartoListeInColizioniNazionale);
					
					//PROSPETTO 12-13-14 -> Compensazione Eccedentarie Deficitarie Livello circoscrizione liste
					compensazioneCircoscrizionaleListe(listProspetto11);
					
					//PROSPETTO 15 riparto tra liste in collegio plurinominale
					ripartoListeCollegioPluri();
					
					//PROSPETTO 16
					Map<Territorio, List<Confronto>> mapConfronto =confrontoCircoscrizionalePluri();
					
					//PROSPETTO 17-18-19 -> Compensazione Eccedentarie Deficitarie Livello pluri liste
					compensazioneCollegioPluriListe(mapConfronto);
				}
			}
		}
		
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
		
		return path;
	}

	private void proclamaEletto() {
		
		listCandidati.stream().collect(Collectors.groupingBy(Base::getIdTerpaCandidato)).entrySet().forEach(el->{
			
			List<CandidatoUni> listCand = new ArrayList<>();
			Territorio uni = new Territorio(el.getKey(), TipoTerritorio.COLLEGIO_UNI, null, null, null);
			
			Base bb = el.getValue().stream().findFirst().orElseThrow(()-> new RuntimeException("Nessun Valore trovato"));
			
			Territorio pluri = new Territorio(bb.getIdCollegioPluri(), TipoTerritorio.COLLEGIO_PLURI, bb.getDescCollegioPluri(), null, null);
			
			uni.setPadre(pluri);
			
			el.getValue().stream().collect(Collectors.groupingBy(Base::getIdCandidato)).entrySet().forEach(candi->{

				Base b = el.getValue().stream().filter(x->x.getIdCandidato().compareTo(candi.getKey()) == 0).findFirst().orElseThrow(()-> new RuntimeException("Nessun Valore trovato"));
				
				uni.setDescrizione(b.getDescTerpaCandidato());
				
				CandidatoUni cand = new CandidatoUni();
				
				cand.setId(candi.getKey());
				cand.setDataNascita(b.getDataNascita());
				cand.setVoti(b.getVotiTotCand());
				cand.setVotiSoloCandidato(b.getVotiSoloCand());
				cand.setTerritorio(uni);
				cand.setEletto(b.getEletto().equals("S"));
				cand.setIdCoalizione(b.getCoterCoali());
				
				List<Elemento> liste = new ArrayList<>();
				
				candi.getValue().forEach(l->{
					liste.add(new Elemento(l.getIdAggregatoRiparto(), l.getDescLista(), l.getVotiLista(), null, null, null));
				});
				cand.setListe(liste);
				
				listCand.add(cand);
			});
			
			mapTerrCandidati.put(uni, listCand);
			
		});
		
		mapTerrCandidati.entrySet().forEach(e->{
			if(!e.getValue().stream().anyMatch(l->l.isEletto())) {
				sortCandidati(e.getValue());
				
				AtomicInteger position = new AtomicInteger(1);
				
				for (int i = 0; i < e.getValue().size(); i++) {
					CandidatoUni candidato = e.getValue().get(i);
					
					if(candidato.isParitaVoti()) {
						candidato.setPosizione(position.get());
					}else {
						candidato.setPosizione(position.getAndIncrement());
					}
				}
				
				List<CandidatoUni> uniVincenti = e.getValue().stream().filter(c->c.getPosizione().compareTo(1) == 0).collect(Collectors.toList());
				
				if(uniVincenti.size() == 1) {
					uniVincenti.stream().findFirst().get().setEletto(true);
					log.info("Eletto trovato in {}, idCoali {}", e.getKey().getDescrizione(), uniVincenti.stream().findFirst().get().getIdCoalizione());
				}else {
					sortCandidatiDataNascita(uniVincenti);
					
					if(uniVincenti.stream().allMatch(c->c.isSorteggio())) {
						e.getKey().setSorteggioCollegio(true);
						log.info("Sorteggio candidati in {}", e.getKey().getDescrizione());
					}else {
						uniVincenti.stream().findFirst().get().setEletto(true);
						log.info("Eletto più giovane in {}", e.getKey().getDescrizione());
					}
				}
				
				mapTerrCandidatiEletti.put(e.getKey(), e.getValue());
			}
		});
		
		
		//Calcolo eletti nazionali per coalizione
		mapTerrCandidatiEletti.values().forEach(e->{
			e.stream().collect(Collectors.groupingBy(CandidatoUni::getIdCoalizione)).entrySet().forEach(l->{
				Long countEletti = l.getValue().stream().filter(x->x.isEletto()).count();
				
				if(mapCoaliEletti.containsKey(l.getKey())) {
					Integer numEle = mapCoaliEletti.get(l.getKey());
					mapCoaliEletti.computeIfPresent(l.getKey(), (k,v)-> mapCoaliEletti.get(l.getKey()) + countEletti.intValue());
				}else {
					mapCoaliEletti.put(l.getKey(), countEletti.intValue());
				}
			});
		});
		
//		if(!ripartoPrevisionale) {
//			gestione salvataggio dati flag eletto
//		}
	
	}
	
	private void determinaScorporo(){
		
//		Elemento e = new Elemento(1, "L1", 13, null, null, null);
//		Elemento e1 = new Elemento(1, "L2", 20, null, null, null);
//		Elemento e2 = new Elemento(1, "L3", 27, null, null, null);
//		Elemento e3 = new Elemento(1, "L4", 40, null, null, null);
//		
//		List<Elemento> li = Arrays.asList(e, e1,e2,e3);
//		Integer votiSoloCand = 20;
//		Integer sumVoti = li.stream().mapToInt(Elemento::getCifra).sum();
//		
//		BigDecimal quozienteDecimale = getQuozienteDecimale(sumVoti, votiSoloCand);
//		
//		Quoziente q = assegnaseggiQIMassimiResti(li, votiSoloCand, sumVoti,TipoOrdinamento.RESTI_PROQUOTA, quozienteDecimale);
//		
//		li.forEach(l->l.setProquota(l.getSeggiQI()+l.getSeggiResti()));
//		System.out.println();
		mapTerrCandidati.entrySet().forEach(e->{
			
			e.getValue().forEach(candi->{
				
				if(candi.getVotiSoloCandidato().compareTo(0) > 0) {
					
					//Calcolo Tot votiCoali
					Integer sumVoti = candi.getListe().stream().mapToInt(Elemento::getCifra).sum();
					
					//recupera voti da db
					Integer votiSoloCand  = candi.getVotiSoloCandidato();
					
					BigDecimal quozienteDecimale = getQuozienteDecimale(sumVoti, votiSoloCand);
					
					assegnaseggiQIMassimiResti(candi.getListe(), votiSoloCand, sumVoti,TipoOrdinamento.RESTI_PROQUOTA, quozienteDecimale);
					
					candi.getListe().forEach(listeProquota->{
						
						list.stream().filter(
								l -> l.getIdCollegioPluri().compareTo(candi.getTerritorio().getPadre().getId()) == 0
										&& l.getIdAggregatoRiparto().compareTo(listeProquota.getId()) == 0)
								.forEach(r -> {
									int proquota = listeProquota.getProquota() + listeProquota.getSeggiQI() + listeProquota.getSeggiResti();
									r.setProquota(proquota);

								});
					});
				}
			});
		});
	}

	private void ripartoListeCollegioPluri() {
		
		fillMappaCollegioPluriListeAmmesse();
		
		mapPluriListeElemento.entrySet().forEach(m->{

			Integer numVoti = m.getValue().stream().mapToInt(Elemento::getCifra).sum();
			
			Quoziente quozEletCirc = getQuoziente(numVoti, m.getKey().getNumSeggi(), null, null);
			
			m.getValue().forEach(e->e.setQuoziente(getQuoziente(e.getCifra(), quozEletCirc.getQuoziente(), null, null)));
			
			assegnaseggiQIMassimiResti(m.getValue(), m.getKey().getNumSeggi(), numVoti, TipoOrdinamento.DECIMALI, null);
			
			//controllo eventuale blocco da riparto nazionale per parita di cifra
			if(m.getValue().stream().anyMatch(l->l.isSorteggioReale())) {
				this.bloccoRiparto = BloccoRiparto.BLOCCO_RIPARTO_COLLEGI_CIRCOSCRIZIONALE_PARITA_CIFRE;
			}
			try {
				generaProspetto5_10_15(m.getKey().getDescrizione(), m.getValue(), quozEletCirc.getQuoziente(), m.getKey().getNumSeggi(), numVoti, 15);
			} catch (DocumentException e) {
				log.error("ERRORE GENERAZIONE PROSPETTO 15 CIRCOSCRIZIONE:" +m.getKey().getDescrizione());
				e.printStackTrace();
			}
		});
		
	}

	private void fillMappaCollegioPluriListeAmmesse() {
		//recupero id aggregato delle liste che partecipano al riparto
		Set<Integer> idsPartecipanoRiparto = mapCircListe.values().stream().flatMap(List::stream)
				.collect(Collectors.toList()).stream().map(c -> c.getListe()).collect(Collectors.toList()).stream()
				.flatMap(List::stream).filter(o -> o.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString()))
				.map(Elemento::getId).collect(Collectors.toSet());

		//fill mappa chiave territorio, value liste
		list.stream().collect(Collectors.groupingBy(Base::getIdCollegioPluri)).entrySet().forEach(e->{
			
			Base base = e.getValue().stream().findFirst().orElseThrow(() -> new RuntimeException("no value found for base in crea mappa collegio pluri"));
			
			Territorio ente = new Territorio(e.getKey(), TipoTerritorio.COLLEGIO_PLURI, base.getDescCollegioPluri(), base.getNumSeggi(), base.getCodEnte());
			Territorio padre = new Territorio(base.getIdCircoscrizione(), TipoTerritorio.CIRCOSCRIZIONE, base.getDescCircoscrizione(), null, base.getCodEnte());
			//TODO recupera cod ente pluri
			ente.setPadre(padre);
			
			List<Elemento> listeCollegioPluri = e.getValue().stream().map(l->{
				Elemento ele = null;
				if(idsPartecipanoRiparto.contains(l.getIdAggregatoRiparto())) {
					ele = new Elemento(l.getIdAggregatoRiparto(), l.getDescLista(), l.getVotiLista(), null, null, l.getCoterCoali());
					ele.setTerritorio(ente);
				}
				return ele;
			}).filter(o->o!= null).collect(Collectors.toList());
			
			mapPluriListeElemento.put(ente, listeCollegioPluri);
		});
	}

	private void ripartoSeggiCoalizioniPostCompensazione(List<Confronto> listProspetto6) {
		
		List<Territorio> circoscrizioni= mapCircListeElemento.keySet().stream().collect(Collectors.toList());
		
		List<Integer> idsCoalizioni = new ArrayList<>();
		
		idsCoalizioni.addAll(listProspetto6.stream().map(x->x.getId()).distinct().collect(Collectors.toList()));
		
		circoscrizioni.forEach(ente->{
			log.info(ente.getDescrizione());
			List<Elemento> elements = mapCircListeElemento.get(ente).stream().filter(l->idsCoalizioni.contains(l.getIdCoalizione())).collect(Collectors.toList());
			
			List<Elemento> listeInCoalizioneAccumulator = new ArrayList<>();
			
			mapCircListe.get(ente).stream().filter(k->Objects.nonNull(k.getIdCoalizone()) && idsCoalizioni.contains(k.getIdCoalizone())).forEach(coal->{
				
				List<Elemento> listeInCoalizione = new ArrayList<>();
				
				listeInCoalizione = coal.getListe().stream().filter(w->w.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())).collect(Collectors.toList());
				
				//Calcolo Tot votiCoali
				Integer numVoti = listeInCoalizione.stream().mapToInt(Elemento::getCifra).sum();
				
				Integer numSeggi = elements.stream().filter(r->r.getIdCoalizione().compareTo(coal.getIdCoalizone()) == 0).collect(Collectors.toList()).stream().mapToInt(l -> l.getSeggiQI() + l.getSeggiDecimali()
						+ (Objects.isNull(l.getSeggioCompensazione()) ? 0 : l.getSeggioCompensazione())).sum();

				Quoziente quozEletCirc = getQuoziente(numVoti,numSeggi, null, null);
				
				listeInCoalizione.forEach(e->e.setQuoziente(getQuoziente(e.getCifra(), quozEletCirc.getQuoziente(), null, null)));
				
				Quoziente q = new Quoziente();

				if(numSeggi > 0) {
					q = assegnaseggiQIMassimiResti(listeInCoalizione, numSeggi, numVoti,TipoOrdinamento.DECIMALI, null);
				}
				
				listeInCoalizioneAccumulator.addAll(listeInCoalizione);
				
				//controllo eventuale blocco da riparto nazionale per parita di cifra
				if(elements.stream().anyMatch(l->l.isSorteggioReale())) {
					this.bloccoRiparto = BloccoRiparto.BLOCCO_RIPARTO_LISTE_CIRCOSCRIZIONALE_PARITA_CIFRE;
				}
				
				try {
					generaProspetto5_10_15(ente.getDescrizione(), listeInCoalizione, q.getQuoziente(), numSeggi, numVoti, 10);
				} catch (DocumentException e1) {
					e1.printStackTrace();
				}
			});
			mapCircListeElemento11.put(ente, listeInCoalizioneAccumulator);
			
			
		});
		
	}

	private List<Coalizione> calcolaCifraNazionale() throws DocumentException {
		log.info("ESTRAGGO LISTE NAZIONALI E SOMMO VOTI");

		List<Elemento> nazionali = new ArrayList<>();

		//Raggruppo dati a livello nazione
		list.stream().collect(Collectors.groupingBy(Base::getIdAggregatoRiparto)).entrySet().forEach(y->{
			Base base = y.getValue().stream().findFirst().orElseThrow(() -> new RuntimeException( "No value find for base in calcola cifra nazionale" ));
			Elemento element = new Elemento(base.getIdAggregatoRiparto(), base.getDescLista(),
					y.getValue().stream().mapToInt(Base::getVotiLista).sum(), null, null, base.getCoterCoali());
			element.setMinoranza(y.getValue().stream().anyMatch(k-> Objects.nonNull(k.getFlagMinoranza()) && k.getFlagMinoranza().equals("S")));
			
			if(element.isMinoranza()) {
				
				Base b = y.getValue().stream()
						.filter(k -> Objects.nonNull(k.getFlagMinoranza())
								&& k.getFlagMinoranza().equals("S")).findFirst()
						.orElseThrow(() -> new RuntimeException("Cod ente non trovato"));
				
				Territorio t = new Territorio(b.getIdCircoscrizione(), TipoTerritorio.CIRCOSCRIZIONE, b.getDescCircoscrizione(),  null, b.getCodEnte());
				
				element.setTerritorio(t);
			}
			nazionali.add(element);
		});
				
		log.info("CIFRA LISTE NAZIONALI CALCOLATA");
		
		//Calcolo totale voti liste
		Integer sommaVoti = nazionali.stream().mapToInt(Elemento::getCifra).sum();
		
		this.setTotaleVotiValidi(sommaVoti);
		
		//Calcolo % Lista
		nazionali.forEach(c->c.setPercentualeLista(truncateDecimal(new BigDecimal(((double)c.getCifra()/sommaVoti)*100), 3)));
		
		//Sbarramento Liste 3%
		BigDecimal soglia3 = truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.TRE.getValue()/100)), 2);

		//Sbarramento Liste 1%
		BigDecimal soglia1 = truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.UNO.getValue()/100)), 2);
		
		this.setVotiValidi1(soglia3);
		
		//Calcolo soglia per regioni con liste di minoranza
		Map<Integer, List<Base>> mapCodEnteMinoranza = new HashMap<>();
		Map<Integer, BigDecimal> mapCodEnteSoglia20 = new HashMap<>();
		Map<String, BigDecimal> mapTerrCifraSoglia20 = new HashMap<>();
		
		list.stream().collect(Collectors.groupingBy(Base::getCodEnte)).entrySet().forEach(z->{
			mapCodEnteMinoranza.put(Integer.parseInt(String.valueOf(z.getKey())), z.getValue());
		});
		
		Arrays.asList(CircoscirioneMinoranza.values()).forEach(e->{
			Integer sumVotiCirc = mapCodEnteMinoranza.get(e.getCodEnte()).stream().mapToInt(Base::getVotiLista).sum();
			BigDecimal soglia20 = truncateDecimal(new BigDecimal(sumVotiCirc * ((double)Sbarramento.VENTI.getValue()/100)), 2);
			
			mapCodEnteSoglia20.put(e.getCodEnte(), soglia20);
		});
		
		nazionali.forEach(e-> {
			
			BigDecimal soglia20 = Objects.nonNull(e.getTerritorio()) && mapCodEnteSoglia20.containsKey(e.getTerritorio().getCodEnte()) ? mapCodEnteSoglia20.get(e.getTerritorio().getCodEnte()) : null;
			
			if(Objects.nonNull(soglia20)) {
			
				mapTerrCifraSoglia20.put(e.getTerritorio().getDescrizione(), soglia20);
				
				if(e.isMinoranza() && BigDecimal.valueOf(e.getCifra()).compareTo(soglia20) >= 0) {
					e.setPartecipaRipartoLista(PartecipaRiparto.SI.toString());
				}else {
					e.setPartecipaRipartoLista(PartecipaRiparto.NO.toString());
				}
			}else {
				
				if(BigDecimal.valueOf(e.getCifra()).compareTo(soglia3) >= 0) {
					e.setPartecipaRipartoLista(PartecipaRiparto.SI.toString());
				}else {
					e.setPartecipaRipartoLista(PartecipaRiparto.NO.toString());
				}
				
				//Percentuale 1% per le coalizioni
				if(BigDecimal.valueOf(e.getCifra()).compareTo(soglia1) >= 0) {
					e.setPartecipaInCoalizione(PartecipaRiparto.SI.toString());
				}else {
					e.setPartecipaInCoalizione(PartecipaRiparto.NO.toString());
				}
			}
			
		});
		
		//Set Cifra Coalizioni 
		List<Coalizione> nazionaliCoaliListe = new ArrayList<>();
		
		Map<Integer, List<Elemento>> mapCoali = nazionali.stream()
				.collect(Collectors.groupingBy(Elemento::getIdCoalizione));
		
		mapCoali.forEach((k,v)->{
			
			Coalizione coali = new Coalizione(v);
			coali.setNumVotiCoalizione(v.stream().filter(partecipaRipartoLista().or(partecipaCifraInCoalizione())).mapToInt(Elemento::getCifra).sum());
			coali.setDescCoalizione(v.stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("/")));
			coali.setIdCoalizone(k);
			coali.setIsCoalizione(v.size()> 1);
			Integer numCandEletti = mapCoaliEletti.containsKey(k) ? (mapCoaliEletti.get(k)) : 0;
			coali.setNumCandUniEletti(numCandEletti);
			
			nazionaliCoaliListe.add(coali);
		});
		
		//Sbarramento Coalizioni 10%
		BigDecimal soglia10 = truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.DIECI.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia10);
		
		AtomicInteger sumvoticoali = new AtomicInteger(0);
		
		nazionaliCoaliListe.forEach(el->{
			//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
			if ((el.getListe().stream().anyMatch(c->c.getPartecipaRipartoLista().compareTo(PartecipaRiparto.SI.toString()) == 0))
					|| BigDecimal.valueOf(((Coalizione) el).getNumVotiCoalizione()).compareTo(soglia10) >= 0) {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				//Calcolo % Coalizione
				el.setPercentualeCoalizione(truncateDecimal(new BigDecimal(((double)((Coalizione)el).getNumVotiCoalizione()/sommaVoti)*100), 3));
				sumvoticoali.getAndAdd(((Coalizione)el).getNumVotiCoalizione());
			}else {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
			}
		});
		
		generaProspetto1(nazionaliCoaliListe, sumvoticoali, mapTerrCifraSoglia20);
		
		return nazionaliCoaliListe;
	}

	private void creazioneMappaLivelloCircoscrizione(List<Coalizione> nazionaliCoaliListe) {
		
		//Prendo solo le lste che hanno superato lo sbarramento
		List<Coalizione> listeAmmesse = nazionaliCoaliListe.stream()
				.filter(c -> c.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString()))
				.collect(Collectors.toList());
		
//		Creo il territorio circoscrizione
		List<Territorio> territorioCircList = list.stream().collect(Collectors.groupingBy(Base::getIdCircoscrizione)).entrySet().stream().map(m->{
			Base circ = m.getValue().stream().findFirst().orElseThrow(() -> new RuntimeException("no value found for base in crea mappa circ"));
			
			Integer numSeggiCirc = listTerritori.stream().filter(p->p.getPadre().getId().compareTo(m.getKey()) == 0).mapToInt(Territorio::getNumSeggi).sum();
//			m.getValue().stream().collect(Collectors.groupingBy(Base::getIdCollegioPluri))
//					.entrySet().stream()
//					.collect(Collectors.toMap(x -> x.getKey(),
//							y -> (y.getValue().stream().map(Base::getNumSeggi).distinct().findFirst().orElseThrow(
//									() -> new RuntimeException("Num seggi NULL in crea mappa circoscrizionale")))))
//					.entrySet().stream().mapToInt(p -> p.getValue()).sum();
					
			return new Territorio(m.getKey(), TipoTerritorio.CIRCOSCRIZIONE, circ.getDescCircoscrizione(), numSeggiCirc, circ.getCodEnte());
		}).collect(Collectors.toList());
		
//		Creo mappa con territorio circ e valore lista di liste circ ammese
		
		territorioCircList.forEach(terpa->{
			
			List<Coalizione> listaCoalizione = new ArrayList<>();

			listeAmmesse.stream().forEach(coal->{
				
				Coalizione coali = null;
				
				List<Elemento> liste = new ArrayList<>();
				
				coal.getListe().forEach(lista->{
					
					List<Base> listeCircoscrizione = list.stream().filter(l -> l.getIdAggregatoRiparto().compareTo(lista.getId()) == 0
							&& l.getIdCircoscrizione().compareTo(terpa.getId()) == 0).collect(Collectors.toList());
					
					Integer sumVotiListaCirc = listeCircoscrizione.stream().mapToInt(Base::getVotiLista).sum();
					
					if(!listeCircoscrizione.isEmpty()) {
						
						Base firstElemento = listeCircoscrizione.stream().findFirst().orElseThrow(() -> new RuntimeException("no value found for base in crea mappa circ"));
						
						Elemento el = new Elemento(firstElemento.getIdAggregatoRiparto(), firstElemento.getDescLista(), sumVotiListaCirc, null, null, firstElemento.getCoterCoali());
						el.setPartecipaRipartoLista(lista.getPartecipaRipartoLista());
						el.setPartecipaInCoalizione(lista.getPartecipaInCoalizione());
						el.setTerritorio(new Territorio(firstElemento.getIdCircoscrizione(),
								TipoTerritorio.CIRCOSCRIZIONE, firstElemento.getDescCircoscrizione(), sumVotiListaCirc,
								firstElemento.getCodEnte()));
						liste.add(el);
					}
					
				});
				
				coali = new Coalizione(liste);
				coali.setDescCoalizione(liste.stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("/")));
				coali.setNumVotiCoalizione(liste.stream().mapToInt(Elemento::getCifra).sum());
				coali.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				coali.setIsCoalizione(liste.size() > 1);
				coali.setIdCoalizone(coal.getIdCoalizone());
				
				listaCoalizione.add(coali);
			
			
		});
			mapCircListe.put(terpa,listaCoalizione);
		
		});
		
		
	}

	private List<Elemento> ripartoCoalizioniListeNazionali(List<Coalizione> nazionali, String dataElezione) throws DocumentException {
		
		//prendo solo le liste ammesse
		List<Coalizione> listeCoaliAmmesse = nazionali.stream()
				.filter(e -> e.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString())
						)
				.collect(Collectors.toList());
		listeCoaliAmmesse.forEach(e ->{
				Coalizione c = (Coalizione)e;
				c.getListe().removeIf(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.NO.toString()));
		});
		
		//mapping liste coalizoni in ELemento
		List<Elemento> elements = new ArrayList<>();
		
		listeCoaliAmmesse.forEach(e ->{
			if( e instanceof Coalizione) {
				Coalizione c = (Coalizione)e;
				c.getListe().removeIf(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.NO.toString()));
				
				elements.add(new Elemento(null, c.getDescCoalizione(), c.getNumVotiCoalizione(),
						null, c.getListe().stream().map(Elemento::getDescrizione).collect(Collectors.toList()),
						c.getIdCoalizone()));
				
			}else {
				elements.add(new Elemento(null, e.getDescCoalizione(), e.getNumVotiCoalizione(), null,
						Arrays.asList(e.getDescCoalizione()), e.getIdCoalizone()));
			}
		});
		
		//Calcolo Tot votiCoali
		Integer sumVotiCOali = elements.stream().mapToInt(Elemento::getCifra).sum();
		
		//recupera voti da db
		Integer numSeggi  = listTerritori.stream().mapToInt(Territorio::getNumSeggi).sum();

		Quoziente q = assegnaseggiQIMassimiResti(elements, numSeggi, sumVotiCOali,TipoOrdinamento.RESTI, null);
		
		generaProspetto2(elements, sumVotiCOali, q.getQuoziente(), 2, null, numSeggi);
		
		//controllo eventuale blocco da riparto nazionale per parita di cifra
		if(elements.stream().anyMatch(l->l.isSorteggioReale())) {
			this.bloccoRiparto = BloccoRiparto.BLOCCO_RIPARTO_NAZIONALE_PARITA_CIFRE;
		}

		creazioneMappaLivelloCircoscrizione(nazionali);
		
		return elements;
	}
	
	private List<Elemento> ripartoTraListeInCoalizione(List<Elemento> ripartoColizioniNazionale, List<Coalizione> nazionali) throws DocumentException {

		List<Elemento> ret = new ArrayList<>();
		
		//mapping liste coalizoni in ELemento
		nazionali.stream().filter(coal->coal.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString())).forEach(coal ->{
			//estraggo liste in coalizione per calcolo seggi
			List<Elemento> elements = new ArrayList<>();
			
			coal.getListe().stream()
					.filter(l -> Objects.nonNull(l.getPartecipaRipartoLista())
							&& l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString()))
					.collect(Collectors.toList()).forEach(l-> elements.add(l)); 
			
			Integer numSeggiDaAssegnare = ripartoColizioniNazionale.stream().filter(x->x.getIdCoalizione().compareTo(coal.getIdCoalizone()) == 0).mapToInt(a->a.getSeggiQI()+a.getSeggiResti()).sum();
			Integer totVoti = elements.stream().mapToInt(Elemento::getCifra).sum();
			
			Quoziente q = assegnaseggiQIMassimiResti(elements, numSeggiDaAssegnare, totVoti, TipoOrdinamento.RESTI, null);
			
			ret.addAll(elements);
			
			try {
				generaProspetto3(elements, numSeggiDaAssegnare, totVoti, q.getQuoziente(), coal.getDescCoalizione());
			} catch (DocumentException e1) {
				log.error("ERRORE GENERAZIONE PROSPETTO 3 coalizione:" +coal.getDescCoalizione());
			}
			
			//controllo eventuale blocco da riparto nazionale per parita di cifra
			if(elements.stream().anyMatch(l->l.getSorteggio())) {
				this.bloccoRiparto = BloccoRiparto.BLOCCO_RIPARTO_NAZIONALE_PARITA_CIFRE;
			}
		});
		
		return ret;
	}

	private void ripartoListeCircoscrizione(List<Coalizione> nazionali) {
		
		mapCircListe.entrySet().forEach(x->{

			List<Elemento> elementiListe = new ArrayList<>();
			
			x.getValue().forEach(coali -> {
				List<Elemento> ammesse = coali.getListe().stream().filter(partecipaRipartoLista()).collect(Collectors.toList());
				ammesse.forEach(k->k.setTerritorio(x.getKey()));
				elementiListe.addAll(ammesse);
			});
			
			List<Elemento> elements = new ArrayList<>();
			
			elementiListe.stream().collect(Collectors.groupingBy(Elemento::getIdCoalizione)).entrySet().forEach(m->{
				
				Elemento ele = new Elemento(null,
						m.getValue().stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("/")),
						m.getValue().stream().mapToInt(Elemento::getCifra).sum(), null, null, m.getKey());
				ele.setTerritorio(x.getKey());
				elements.add(ele);
			});
			
			Integer numVoti = elements.stream().mapToInt(Elemento::getCifra).sum();
			
			Quoziente quozEletCirc = getQuoziente(numVoti, x.getKey().getNumSeggi(), null, null);
			
			elements.forEach(e->e.setQuoziente(getQuoziente(e.getCifra(), quozEletCirc.getQuoziente(), null, null)));
			
			Integer numSeggi = listTerritori.stream().filter(l->l.getPadre().getId().compareTo(x.getKey().getId()) == 0).mapToInt(Territorio::getNumSeggi).sum(); 
					
			assegnaseggiQIMassimiResti(elements, numSeggi, numVoti, TipoOrdinamento.DECIMALI, null);
			
			mapCircListeElemento.put(x.getKey(), elements);
			
			//controllo eventuale blocco da riparto nazionale per parita di cifra
			if(elements.stream().anyMatch(l->l.isSorteggioReale())) {
				this.bloccoRiparto = BloccoRiparto.BLOCCO_RIPARTO_CIRCOSCRIZIONALE_PARITA_CIFRE;
			}
			try {
				generaProspetto5_10_15(x.getKey().getDescrizione(), elements, quozEletCirc.getQuoziente(), x.getKey().getNumSeggi(), numVoti, 5);
			} catch (DocumentException e) {
				log.error("ERRORE GENERAZIONE PROSPETTO 5 CIRCOSCRIZIONE:" +x.getKey().getDescrizione());
				e.printStackTrace();
			}
		});
	}

	private List<Confronto> confrontoCircoscrizionaleNazionaleCoalizioni(List<Elemento> ripartoColizioniNazionale) throws DocumentException {
		
		List<Confronto> listProspetto6 = new ArrayList<>();
		
		ripartoColizioniNazionale.forEach(l->{
			Confronto prosp = new Confronto();
			
			List<Elemento> lista = new ArrayList<>();
			
			mapCircListeElemento.values().stream().flatMap(List::stream)
					.filter(s -> s.getIdCoalizione().compareTo(l.getIdCoalizione()) == 0).collect(Collectors.toList())
					.forEach(m -> {
						lista.add(m);
					});
			
			Integer totSeggiQI = lista.stream().mapToInt(Elemento::getSeggiQI).sum();

			Integer totSeggiDecimali = lista.stream().mapToInt(Elemento::getSeggiDecimali).sum();
			
			prosp.setId(l.getIdCoalizione());
			prosp.setDescLista(l.getDescrizione());
			prosp.setSeggiQICirc(totSeggiQI);
			prosp.setSeggiTotCirc(totSeggiQI+totSeggiDecimali);
			prosp.setSeggiNazionali(l.getSeggiQI()+l.getSeggiResti());
			prosp.setDiff(prosp.getSeggiTotCirc()-prosp.getSeggiNazionali());			
			
			prosp.setCifraNazionale(l.getCifra());
			
			listProspetto6.add(prosp);
			
			//popolo mappa eccedentarie deficitarie
			if(prosp.getDiff().compareTo(0) > 0) {
				//ECCENTARIE
				mapEccedentarieCoalizioni.put(lista.stream().map(Elemento::getIdCoalizione).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per eccedentarie" )), lista);
			}else if(prosp.getDiff().compareTo(0) < 0){
				//DEFICITARIE
				mapDeficitarieCoalizioni.put(lista.stream().map(Elemento::getIdCoalizione).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per deficitarie" )), lista);
			}
		});
		
		generaProspetto6_11_16(listProspetto6, 6, null);
		
		return listProspetto6;
	}
	
	private void compensazioneCircoscrizionaleCoalizioni(List<Confronto> listProspetto6) throws DocumentException {

		List<Confronto> eccedntarieNaz = listProspetto6.stream().filter(l->l.getDiff().compareTo(0)>0).collect(Collectors.toList());
		
		eccedntarieNaz = sortByDiffCifra(eccedntarieNaz, Ordinamento.DESC);
		
		eccedntarieNaz.forEach(e->{
			List<Elemento> elements = mapEccedentarieCoalizioni.get(e.getId());
			
			elements.sort(compareByDecimale(Ordinamento.DESC));
		});
		
		List<Elemento> listaDeficitarie = mapDeficitarieCoalizioni.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		listProspetto6.stream().filter(l->l.getDiff().compareTo(0) < 0).forEach(l->{
			mapDeficitariaDiff.put(l.getId(), l.getDiff());
		});

		AtomicInteger ordineSottrazione = new AtomicInteger(1);
		
		eccedntarieNaz.forEach(e->{
			
			log.info("Lista ECCEDNTARIA: {}", e.getDescLista());
			
			boolean isSorteggio = false;
			
			AtomicInteger seggiDaAssegnare = new AtomicInteger(e.getDiff());
			
			List<Elemento> listeEccedntarie = mapEccedentarieCoalizioni.get(e.getId());
			
			while (seggiDaAssegnare.get() > 0 || !isSorteggio) {
				
				listeEccedntarie.sort(compareByDecimale(Ordinamento.DESC));
				
				List<Elemento> eccedntarie = listeEccedntarie.stream().filter(l->l.getIdCoalizione().compareTo(e.getId()) == 0).collect(Collectors.toList());
				
				//ciclo eccedntarie e vedo se posso dare nella stessa ente
				for(Elemento ecc : eccedntarie) {
					//se ottenuto seggi con i decimali può cedere e non ha già ceduto il seggio decimale
					log.info("Eccedentaria {}", ecc.getTerritorio().getDescrizione());

					if(seggiDaAssegnare.get() == 0) {
						return;
					}
					
					if(puoAssegnareSeggio(ecc)) {
						Territorio terrEcc = ecc.getTerritorio();
						
						//cerco deficitaria nello stesso territorio
						assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, false, TipoTerritorio.NAZIONALE);
						
					}
					
					//se ho assegnato tutti i seggi esco dal loop
					if(seggiDaAssegnare.get() == 0) {
						break;
					}
				}//end loop eccedentarie
				
				//SHIFT cerco in altre circoscrizioni
				for(Elemento ecc : eccedntarie) {
					//se ottenuto seggi con i decimali può cedere e non ha già ceduto il seggio decimale
					log.info("Eccedentaria SHIFT {}", ecc.getTerritorio().getDescrizione());

					if(seggiDaAssegnare.get() == 0) {
						return;
					}
					
					if(puoAssegnareSeggio(ecc)) {
						Territorio terrEcc = ecc.getTerritorio();
						
						boolean assegnato = false;
						
						//CASISTICA SHIFT
						//cerco deficitaria in altro territorio
						if(!assegnato) {
							log.info("SHIFT");
							assegnato = assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, true, TipoTerritorio.NAZIONALE);

							if(!assegnato) {
								log.info("CASO NON GESTITO!!!!!!!!!!!!!!!!!!!!!");
							}
							break;
						}
					}
					
					//se ho assegnato tutti i seggi esco dal loop
					if(seggiDaAssegnare.get() == 0) {
						break;
					}
				}//end loop eccedentarie
				
			}//end while
		});
		
		mapEccedentarieCoalizioni.entrySet().forEach(e->{
			try {
				generaProspetto7_12(e.getValue(), 7);
			} catch (DocumentException e1) {
				log.error("ERROR GENERAZIONE PROSPETTO 7:{}", e1.getMessage());			
				}
		});
		
		List<Elemento> deficitarie = mapDeficitarieCoalizioni.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		deficitarie.sort(compareByDecimale(Ordinamento.DESC));
		
		try {
			generaProspetto8_13(deficitarie, 8);
		} catch (DocumentException e1) {
			log.error("ERROR GENERAZIONE PROSPETTO 8:{}", e1.getMessage());	
		}
		
		try {
			generaProspetto9_14(listProspetto9, 9);
		} catch (DocumentException e1) {
			log.error("ERROR GENERAZIONE PROSPETTO 9:{}", e1.getMessage());	
		}
	}

	private boolean assegnaSeggiDeficitaria(List<Elemento> listaDeficitarie, AtomicInteger ordineSottrazione,
			AtomicInteger seggiDaAssegnare, Elemento eccedentaria, Territorio terrEccedentaria, boolean isShift, TipoTerritorio tipoTerritorio) {
		listaDeficitarie.sort(compareByDecimale(Ordinamento.ASC));
		
		boolean assegnato = false;
		
		for (Elemento def : listaDeficitarie) {
			//se deficitaria nello stesso territorio e non ha preso seggi con decimali può prendere seggio
			boolean raggiuntoSeggiNaz = false;
			
			Long seggiRicevuti = null;
			Integer seggiDaRicevere = null;
			int seggiResidui;
			
			switch (tipoTerritorio) {
			case NAZIONALE:
				seggiRicevuti = listaDeficitarie.stream().filter(k->k.isRiceveSeggio() && def.getIdCoalizione().compareTo(k.getIdCoalizione()) == 0).count();
				seggiDaRicevere = mapDeficitariaDiff.get(def.getIdCoalizione());
				seggiResidui = seggiRicevuti.intValue() + seggiDaRicevere ;
				if(seggiResidui == 0) {
					raggiuntoSeggiNaz = !raggiuntoSeggiNaz;
				}
				break;
			case CIRCOSCRIZIONE:
				seggiRicevuti = listaDeficitarie.stream().filter(k->k.isRiceveSeggio() && def.getId().compareTo(k.getId()) == 0).count();
				seggiDaRicevere = mapDeficitariaDiff11.get(def.getId());
				seggiResidui = seggiRicevuti.intValue() + seggiDaRicevere ;
				if(seggiResidui == 0) {
					raggiuntoSeggiNaz = !raggiuntoSeggiNaz;
				}
				break;
			case COLLEGIO_PLURI:

				break;
			default:
				break;
			}
				
			
			if((terrEccedentaria.equals(def.getTerritorio()) || isShift) && def.getSeggiDecimali().compareTo(0) == 0 && !def.isRiceveSeggio() && !raggiuntoSeggiNaz) {
				
				log.info("Assegnato 1 seggio a: {} in {}. RIMANENTI: {}", def.getDescrizione(),def.getTerritorio().getDescrizione(), seggiDaAssegnare.get());
				seggiDaAssegnare.getAndDecrement();
				
				def.setRiceveSeggio(true);
				def.setOrdineSottrazione(ordineSottrazione.get());
				def.setShift(isShift);
				def.setSeggioCompensazione(1);
				
				eccedentaria.setCedeSeggio(true);
				eccedentaria.setOrdineSottrazione(ordineSottrazione.getAndIncrement());
				eccedentaria.setSeggioCompensazione(-1);
				
				assegnato = !assegnato;
				
				switch (tipoTerritorio) {
				case NAZIONALE:
					listProspetto9.add(new Prospetto9(eccedentaria, def));
					break;
				case CIRCOSCRIZIONE:
					listProspetto14.add(new Prospetto9(eccedentaria, def));
					break;
				case COLLEGIO_PLURI:
	
					break;
				default:
					break;
				}
				break;
			}
		}//end loop deficitarie
		return assegnato;
	}
	
	private List<Confronto> confrontoCircoscrizionaleNazionaleListe(List<Elemento> ripartoListeNazionale) throws DocumentException {
		
		List<Confronto> listProspetto6 = new ArrayList<>();
		
		ripartoListeNazionale.forEach(l->{
			Confronto prosp = new Confronto();
			
			List<Elemento> lista = new ArrayList<>();
			
			mapCircListeElemento11.values().stream().flatMap(List::stream)
					.filter(s -> s.getId().compareTo(l.getId()) == 0).collect(Collectors.toList())
					.stream().collect(Collectors.groupingBy(Elemento::getId)).entrySet().forEach(m -> {
						m.getValue().forEach(lis->{
							
							lista.add(lis);
						});
			});
			
			if(!lista.isEmpty()) {
				Integer totSeggiQI = lista.stream().mapToInt(Elemento::getSeggiQI).sum();
				
				Integer totSeggiDecimali = lista.stream().mapToInt(Elemento::getSeggiDecimali).sum();
				
				prosp.setId(l.getId());
				prosp.setDescLista(l.getDescrizione());
				prosp.setSeggiQICirc(totSeggiQI);
				prosp.setSeggiTotCirc(totSeggiQI+totSeggiDecimali);
				prosp.setSeggiNazionali(l.getSeggiQI()+l.getSeggiResti());
				prosp.setDiff(prosp.getSeggiTotCirc()-prosp.getSeggiNazionali());			
				
				prosp.setCifraNazionale(l.getCifra());
				
				listProspetto6.add(prosp);
				
				//popolo mappa eccedentarie deficitarie
				if(prosp.getDiff().compareTo(0) > 0) {
					//ECCENTARIE
					mapEccedentarieListeCirc.put(lista.stream().map(Elemento::getId).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per eccedentarie" )), lista);
				}else if(prosp.getDiff().compareTo(0) < 0){
					//DEFICITARIE
					
					listProspetto6.stream().filter(j->j.getDiff().compareTo(0) < 0).forEach(ll->{
						mapDeficitariaDiff11.put(ll.getId(), ll.getDiff());
					});
					mapDeficitarieListeCirc.put(lista.stream().map(Elemento::getId).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per deficitarie" )), lista);
				}
				
			}
		});
		
		generaProspetto6_11_16(listProspetto6, 11, null);
		
		return listProspetto6;
	}

	private void compensazioneCircoscrizionaleListe(List<Confronto> listProspetto11) throws DocumentException {

		List<Confronto> eccedntarieNaz = listProspetto11.stream().filter(l->l.getDiff().compareTo(0)>0).collect(Collectors.toList());
		
		eccedntarieNaz = sortByDiffCifra(eccedntarieNaz, Ordinamento.DESC);
		
		eccedntarieNaz.forEach(e->{
			List<Elemento> elements = mapEccedentarieListeCirc.get(e.getId());
			
			elements.sort(compareByDecimale(Ordinamento.DESC));
		});
		
		List<Elemento> listaDeficitarie = mapDeficitarieListeCirc.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		AtomicInteger ordineSottrazione = new AtomicInteger(1);
		
		eccedntarieNaz.forEach(e->{
			
			log.info("Lista ECCEDNTARIA: {}", e.getDescLista());
			
			boolean isSorteggio = false;
			
			AtomicInteger seggiDaAssegnare = new AtomicInteger(e.getDiff());
			
			List<Elemento> listeEccedntarie = mapEccedentarieListeCirc.get(e.getId());
			
			while (seggiDaAssegnare.get() > 0 || !isSorteggio) {
				
				listeEccedntarie.sort(compareByDecimale(Ordinamento.DESC));
				
				List<Elemento> eccedntarie = listeEccedntarie.stream().filter(l->l.getId().compareTo(e.getId()) == 0).collect(Collectors.toList());
				
				//ciclo eccedntarie e vedo se posso dare nella stessa ente
				for(Elemento ecc : eccedntarie) {
					//se ottenuto seggi con i decimali può cedere e non ha già ceduto il seggio decimale
					log.info("Eccedentaria {}", ecc.getTerritorio().getDescrizione());

					if(seggiDaAssegnare.get() == 0) {
						return;
					}
					
					if(puoAssegnareSeggio(ecc)) {
						Territorio terrEcc = ecc.getTerritorio();
						
						//cerco deficitaria nello stesso territorio
						assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, false, TipoTerritorio.CIRCOSCRIZIONE);
						
					}else {
						log.info("Che cazzo succede?????");
						seggiDaAssegnare.getAndDecrement();
					}
					
					//se ho assegnato tutti i seggi esco dal loop
					if(seggiDaAssegnare.get() == 0) {
						break;
					}
				}//end loop eccedentarie
				
				//SHIFT cerco in altre circoscrizioni
				for(Elemento ecc : eccedntarie) {
					//se ottenuto seggi con i decimali può cedere e non ha già ceduto il seggio decimale
					log.info("Eccedentaria SHIFT {}", ecc.getTerritorio().getDescrizione());

					if(seggiDaAssegnare.get() == 0) {
						return;
					}
					
					if(puoAssegnareSeggio(ecc)) {
						Territorio terrEcc = ecc.getTerritorio();
						
						boolean assegnato = false;
						
						//CASISTICA SHIFT
						//cerco deficitaria in altro territorio
						if(!assegnato) {
							log.info("SHIFT");
							assegnato = assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, true, TipoTerritorio.CIRCOSCRIZIONE);

							if(!assegnato) {
								log.info("CASO NON GESTITO!!!!!!!!!!!!!!!!!!!!!");
							}
							break;
						}
					}
					
					//se ho assegnato tutti i seggi esco dal loop
					if(seggiDaAssegnare.get() == 0) {
						break;
					}
				}//end loop eccedentarie
				
			}//end while
		});
		
		mapEccedentarieListeCirc.entrySet().forEach(e->{
			try {
				generaProspetto7_12(e.getValue(), 12);
			} catch (DocumentException e1) {
				log.error("ERROR GENERAZIONE PROSPETTO 12:{}", e1.getMessage());			
				}
		});
		
		List<Elemento> deficitarie = mapDeficitarieListeCirc.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		deficitarie.sort(compareByDecimale(Ordinamento.DESC));
		
		try {
			generaProspetto8_13(deficitarie, 13);
		} catch (DocumentException e1) {
			log.error("ERROR GENERAZIONE PROSPETTO 12:{}", e1.getMessage());	
		}
		
		try {
			generaProspetto9_14(listProspetto14, 14);
		} catch (DocumentException e1) {
			log.error("ERROR GENERAZIONE PROSPETTO 14:{}", e1.getMessage());	
		}
	}

	private Map<Territorio, List<Confronto>> confrontoCircoscrizionalePluri() throws DocumentException {
		
		Map<Territorio, List<Confronto>> mapCircoscrizioneConfronto = new HashMap<>();
		
		//loop per ogni circoscrizione
		mapCircListeElemento11.entrySet().forEach(e->{
			
			List<Confronto> listProspetto16 = new ArrayList<>();
			
			e.getValue().stream().forEach(listaCirc->{
				
				List<Territorio> pluri = mapPluriListeElemento.keySet().stream().filter(t->t.getPadre().getId().compareTo(e.getKey().getId()) ==0).collect(Collectors.toList());
				
				List<Elemento> listePluri = new ArrayList<>();;
				
				pluri.forEach(t->{
					listePluri.addAll(mapPluriListeElemento.get(t).stream().filter(l->l.getId().compareTo(listaCirc.getId()) == 0).collect(Collectors.toList()));
				});
				
				Confronto prosp = new Confronto();
				
				Integer totSeggiQI = listePluri.stream().mapToInt(Elemento::getSeggiQI).sum();

				Integer totSeggiDecimali = listePluri.stream().mapToInt(Elemento::getSeggiDecimali).sum();
				
				prosp.setId(listaCirc.getId());
				prosp.setDescLista(listaCirc.getDescrizione());
				prosp.setSeggiQICirc(totSeggiQI);
				prosp.setSeggiTotCirc(totSeggiQI+totSeggiDecimali);
				prosp.setSeggiNazionali(listaCirc.getSeggiQI()+listaCirc.getSeggiDecimali()+listaCirc.getSeggioCompensazione());
				prosp.setDiff(prosp.getSeggiTotCirc()-prosp.getSeggiNazionali());			
				
				prosp.setCifraNazionale(listaCirc.getCifra());
				
				prosp.setTerritorio(listaCirc.getTerritorio());
				
				listProspetto16.add(prosp);
				
				//popolo mappa eccedentarie deficitarie
				if(prosp.getDiff().compareTo(0) > 0) {
					//ECCENTARIE
					Elemento ee = listePluri.stream().findFirst().orElseThrow(() -> new RuntimeException( "no value found per eccedentarie pluri" ));
					ChiavePluri chiave = new  ChiavePluri(ee.getTerritorio().getId(), ee.getId());
					mapEccedentarieListePluri.put(chiave, listePluri);
				}else if(prosp.getDiff().compareTo(0) < 0){
					//DEFICITARIE
					mapDeficitarieListePluri.put(listePluri.stream().map(Elemento::getId).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per deficitarie pluri" )), listePluri);
				}
				
			});
			
			try {
				generaProspetto6_11_16(listProspetto16, 16, e.getKey().getDescrizione());
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}
			
			mapCircoscrizioneConfronto.put(e.getKey(), listProspetto16);
		});
		
		return mapCircoscrizioneConfronto;
	}
	
	private void compensazioneCollegioPluriListe(Map<Territorio, List<Confronto>> mapConfronto) throws DocumentException {

		//ECCEDENTARIE
		List<Confronto> eccedntarieCirc = mapConfronto.values().stream().flatMap(List::stream).filter(l->l.getDiff().compareTo(0) > 0).collect(Collectors.toList());
		
		eccedntarieCirc = sortByDiffCifra(eccedntarieCirc, Ordinamento.DESC);
		
		//per ogni eccedntaria stampo prospetto e recupero dati di ogni collegio pluri da mappa
		eccedntarieCirc.stream().collect(Collectors.groupingBy(Confronto::getId)).entrySet().forEach(mapIdListaListe->{
			
			Set<Integer> idCirc = mapIdListaListe.getValue().stream().map(o->o.getTerritorio().getId()).collect(Collectors.toSet());
			
			List<Elemento> listaCollegiPluri = mapPluriListeElemento.values().stream().flatMap(List::stream)
					.filter(l -> l.getId().compareTo(mapIdListaListe.getKey()) == 0 && idCirc.contains(l.getTerritorio().getPadre().getId())).collect(Collectors.toList());
			
			listaCollegiPluri.sort(compareByDecimale(Ordinamento.DESC));
			
			try {
				generaProspetto17_18(listaCollegiPluri, 17);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}
		});
		
		//DEFICITARIE
		List<Confronto> deficitarieCirc = mapConfronto.values().stream().flatMap(List::stream).filter(l->l.getDiff().compareTo(0) < 0).collect(Collectors.toList());
		
		deficitarieCirc = sortByDiffCifra(deficitarieCirc, Ordinamento.DESC);
		
		//per ogni eccedntaria stampo prospetto e recupero dati di ogni collegio pluri da mappa
		deficitarieCirc.stream().collect(Collectors.groupingBy(Confronto::getId)).entrySet().forEach(mapIdListaListe->{
			
			Set<Integer> idCirc = mapIdListaListe.getValue().stream().map(o->o.getTerritorio().getId()).collect(Collectors.toSet());
			
			List<Elemento> listaCollegiPluri = mapPluriListeElemento.values().stream().flatMap(List::stream)
					.filter(l -> l.getId().compareTo(mapIdListaListe.getKey()) == 0 && idCirc.contains(l.getTerritorio().getPadre().getId())).collect(Collectors.toList());
			
			listaCollegiPluri.sort(compareByDecimale(Ordinamento.ASC));
			
			try {
				generaProspetto17_18(listaCollegiPluri, 18);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}
		});
		
		
		
		//prospetto 19
		List<Territorio> circoscizioni = new ArrayList<>();
		
		mapConfronto.entrySet().forEach(e->{
			if(e.getValue().stream().anyMatch(l->l.getDiff() > 0 || l.getDiff() < 0)) {
				circoscizioni.add(e.getKey());
			}
		});
		
		
		circoscizioni.sort(sortByCodEnte());
		
		circoscizioni.forEach(circ->{
			List<Confronto> listCircConfronto = mapConfronto.get(circ);
			
			Map<Integer, Integer> mapIdAggSeggiEcc = new HashMap<>();
			
			//cerco da quale eccedentaria partire: quella con DIFF più alta
			//a parita di DIFF prendo liste nei pluri che hanno preso seggi qui e che hanno decimali più BASSI
			List<Confronto> eccedntarie = listCircConfronto.stream().filter(l->l.getDiff().compareTo(0) > 0).collect(Collectors.toList());
			
			eccedntarie = sortByDiffCifra(eccedntarie, Ordinamento.DESC);
			
			List<Elemento> eccCirc = new ArrayList<>();
			Map<Integer, Integer> mapIdListaDiff = new HashMap<>();
			
			eccedntarie.stream().forEach(s->{
				List<Territorio> collegiPluri = mapPluriListeElemento.keySet().stream()
						.filter(t -> t.getPadre().getId().compareTo(s.getTerritorio().getId()) == 0)
						.collect(Collectors.toList());
				
				mapIdAggSeggiEcc.put(s.getId(), s.getDiff());
				
				List<Elemento> ele =  collegiPluri.stream().map(c -> mapPluriListeElemento.get(c).stream()
						.filter(l -> l.getId().compareTo(s.getId()) == 0 && l.getSeggiDecimali().compareTo(1) == 0).sorted(sortDecimaleAsc())
						.limit(mapIdAggSeggiEcc.get(s.getId())).collect(Collectors.toList())).findFirst().get();
				
				eccCirc.addAll(ele);
				
				mapIdListaDiff.put(s.getId(), s.getDiff());
			});
			
			
			Map<Integer, Integer> mapIdAggSeggiDef = new HashMap<>();
			
			//cerco da quale deficitaria partire: quella con DIFF più basse
			//a parita di DIFF prendo liste nei pluri che NON hanno preso seggi qui e che hanno decimali più ALTI
			List<Confronto> deficitarie = listCircConfronto.stream().filter(l->l.getDiff().compareTo(0) < 0).collect(Collectors.toList());
			
			deficitarie = sortByDiffCifra(deficitarie, Ordinamento.ASC);
			
			List<Elemento> defCirc = new ArrayList<>();
			
			deficitarie.stream().filter(l->l.isParita()).forEach(s->{
				//il terr è pluri mentre in canna ho l'uni
				List<Territorio> collegiPluri = mapPluriListeElemento.keySet().stream()
						.filter(t -> t.getPadre().getId().compareTo(s.getTerritorio().getId()) == 0)
						.collect(Collectors.toList());
				
				mapIdAggSeggiDef.put(s.getId(), Math.abs(s.getDiff()));
				
				List<Elemento> ele = collegiPluri.stream().map(c -> mapPluriListeElemento.get(c).stream()
						.filter(l -> l.getId().compareTo(s.getId()) == 0 && l.getSeggiDecimali().compareTo(0) == 0).sorted(sortDecimaleDesc())
						.limit(mapIdAggSeggiDef.get(s.getId()))
						.collect(Collectors.toList())).findFirst().get();
				defCirc.addAll(ele);
			});
			

			List<Prospetto9> prospetto = new ArrayList<>();
			
			log.info("CIRC: {}", circ.getDescrizione());
			
			eccCirc.forEach(e->{
				log.info("ECC: {}", e.getDescrizione());
				AtomicInteger seggiEcc = new AtomicInteger(mapIdAggSeggiEcc.get(e.getId()));
				
				while (seggiEcc.get() > 0) {
					boolean assegnato = false;
					if(defCirc.isEmpty()) {
						log.error("----------------------------------------       SHIFT no def found----------------------------------");
						seggiEcc.getAndDecrement();
					}
					for (Elemento def : defCirc) {
						AtomicInteger seggiDef = new AtomicInteger(mapIdAggSeggiDef.get(def.getId()));
						if (seggiEcc.get() > 0 /*&& seggiDef.get() != defCirc.stream()
								.filter(d -> d.getId().compareTo(def.getId()) == 0 && d.isRiceveSeggio()).count()*/
								&& !def.isRiceveSeggio()) {
							
							assegnato = !assegnato;
							def.setRiceveSeggio(true);
							e.setCedeSeggio(true);
							seggiEcc.getAndDecrement();
							log.error("{} CEDE A {}", e.getDescrizione(), def.getDescrizione());
							prospetto.add(new Prospetto9(e, def));
						}
						
						if(!assegnato) {
							log.error("----------------------------------------       SHIFT ----------------------------------");
							seggiEcc.getAndDecrement();
						}
					}
				}
			});
			
			try {
				generaProspetto19(prospetto, circ.getDescrizione());
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}
		});
	}

	private Comparator<? super Elemento> sortDecimaleDesc() {
		return (e1, e2)->e2.getQuoziente().getDecimale().compareTo(e1.getQuoziente().getDecimale());
	}

	private Comparator<? super Elemento> sortDecimaleAsc() {
		return (e1, e2)->e1.getQuoziente().getDecimale().compareTo(e2.getQuoziente().getDecimale());
	}
}
