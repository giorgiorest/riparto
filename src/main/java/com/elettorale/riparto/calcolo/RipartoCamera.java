package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elettorale.riparto.dto.Base;
import com.elettorale.riparto.dto.Coalizione;
import com.elettorale.riparto.utils.Prospetto9;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

public class RipartoCamera extends AppoggioStampa{

	Logger log = LoggerFactory.getLogger(RipartoCamera.class);
	
	private List<Base> list;
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
	private Map<Integer, List<Elemento>> mapEccedentarieListe = new HashMap<>();
	//mappa deficitarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapDeficitarieListe = new HashMap<>();
	//----------------CIRCOSCRIZIONE----------------------//
	
	
	private List<Prospetto9> listProspetto9 = new ArrayList<>();
	private List<Prospetto9> listProspetto14 = new ArrayList<>();
	
	public RipartoCamera(List<Base> list) {
		this.list = list;
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

		//PROSPETTO 1
		List<Coalizione> nazionali = calcolaCifraNazionale();
		
		//PROSPETTO 2
		List<Elemento> ripartoColizioniNazionale = ripartoCoalizioniListeNazionali(nazionali, dataElezione);
		
		//PROSPETTO 3
		List<Elemento> ripartoListeInColizioniNazionale = ripartoTraListeInCoalizione(ripartoColizioniNazionale, nazionali);
		
		//PROSPETTO 5
		ripartoListeCircoscrizione(nazionali);
		
		//PROSPETTO 6 -> Confronto tra livello circ e naz
		List<Confronto> listProspetto6 = confrontoCircoscrizionaleNazionaleCoalizioni(ripartoColizioniNazionale);
		
		//PROSPETTO 7-8-9 -> Compensazione Eccedentarie Deficitarie Livello circoscrizione coali
		compensazioneCircoscrizionaleCoalizioni(listProspetto6);
		
		//PROSPETTO 10 -> ripartizione seggi tra coalizioni in circosricioni post compensazione
		ripartoSeggiCoalizioniPostCompensazione(listProspetto6);
		
		//PROSPETTO 11 -> Confronto tra livello circ e naz
		List<Confronto> listProspetto11 = confrontoCircoscrizionaleNazionaleListe(ripartoListeInColizioniNazionale);
		
		//PROSPETTO 12-13-14 -> Compensazione Eccedentarie Deficitarie Livello circoscrizione liste
		compensazioneCircoscrizionaleListe(listProspetto11);
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
		
		return path;
	}

	private void ripartoSeggiCoalizioniPostCompensazione(List<Confronto> listProspetto6) {
		
		List<Territorio> circoscrizioni= mapCircListeElemento.keySet().stream().collect(Collectors.toList());
		
		List<Integer> idsCoalizioni = new ArrayList<>();
		
//		idsCoalizioni.addAll(listProspetto9.stream().map(e->e.getDeficitaria().getIdCoalizione()).distinct().collect(Collectors.toList()));
//		idsCoalizioni.addAll(listProspetto9.stream().map(e->e.getEccedntaria().getIdCoalizione()).distinct().collect(Collectors.toList()));
		
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

				Quoziente quozEletCirc = getQuoziente(numVoti,numSeggi, null);
				
				listeInCoalizione.forEach(e->e.setQuoziente(getQuoziente(e.getCifra(), quozEletCirc.getQuoziente(), null)));
				
				Quoziente q = new Quoziente();

				if(numSeggi > 0) {
					q = assegnaseggiQIMassimiResti(listeInCoalizione, numSeggi, numVoti,TipoOrdinamento.DECIMALI);
				}
				
				listeInCoalizioneAccumulator.addAll(listeInCoalizione);
				
				try {
					generaProspetto5_10(ente.getDescrizione(), listeInCoalizione, q.getQuoziente(), numSeggi, numVoti, 10);
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
			nazionali.add(new Elemento(base.getIdAggregatoRiparto(), base.getDescLista(),
					y.getValue().stream().mapToInt(Base::getVotiLista).sum(), null, null, base.getCoterCoali()));
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
		
		nazionali.forEach(e-> {
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
		});
		
		//Set Cifra Coalizioni 
		List<Coalizione> nazionaliCoaliListe = new ArrayList<>();
		
		Map<Integer, List<Elemento>> mapCoali = nazionali.stream()
				.collect(Collectors.groupingBy(Elemento::getIdCoalizione));
		
		mapCoali.forEach((k,v)->{
			
			Coalizione coali = new Coalizione(v);
			coali.setNumVotiCoalizione(v.stream().filter(partecipaRipartoLista().or(partecipaCifraInCoalizione())).mapToInt(Elemento::getCifra).sum());
			coali.setDescCoalizione(v.stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("-")));
			coali.setIdCoalizone(k);
			coali.setIsCoalizione(v.size()> 1);
			
			nazionaliCoaliListe.add(coali);
		});
		
		//TODO Implementare sbarramente minoranza + coalizione che partecipa con %cona < ed lista ammessa
		
		//Sbarramento Coalizioni 10%
		BigDecimal soglia10 = truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.DIECI.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia10);
		
		AtomicInteger sumvoticoali = new AtomicInteger(0);
		
		nazionaliCoaliListe.forEach(el->{
			//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
			if(BigDecimal.valueOf(((Coalizione)el).getNumVotiCoalizione()).compareTo(soglia10) >= 0) {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				//Calcolo % Coalizione
				el.setPercentualeCoalizione(truncateDecimal(new BigDecimal(((double)((Coalizione)el).getNumVotiCoalizione()/sommaVoti)*100), 3));
				sumvoticoali.getAndAdd(((Coalizione)el).getNumVotiCoalizione());
			}else {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
			}
		});
		
		creazioneMappaLivelloCircoscrizione(nazionaliCoaliListe);
	
		generaProspetto1(nazionaliCoaliListe, sumvoticoali);
		
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
			
			Integer numSeggiCirc = m.getValue().stream().collect(Collectors.groupingBy(Base::getIdCollegioPluri))
					.entrySet().stream()
					.collect(Collectors.toMap(x -> x.getKey(),
							y -> (y.getValue().stream().map(Base::getNumSeggi).distinct().findFirst().orElseThrow(
									() -> new RuntimeException("Num seggi NULL in crea mappa circoscrizionale")))))
					.entrySet().stream().mapToInt(p -> p.getValue()).sum();
					
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
				coali.setDescCoalizione(liste.stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("-")));
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
		Integer numSeggi  = list.stream().collect(Collectors.groupingBy(Base::getIdCollegioPluri)).entrySet().stream()
				.collect(Collectors.toMap(x -> x.getKey(), y -> y.getValue().stream().findFirst().get().getNumSeggi()))
				.values().stream().mapToInt(s -> s).sum();

		Quoziente q = assegnaseggiQIMassimiResti(elements, numSeggi, sumVotiCOali,TipoOrdinamento.RESTI);
		
		generaProspetto2(elements, sumVotiCOali, q.getQuoziente(), 2, null, numSeggi);
		
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
			
			Quoziente q = assegnaseggiQIMassimiResti(elements, numSeggiDaAssegnare, totVoti, TipoOrdinamento.RESTI);
			
			ret.addAll(elements);
			
			try {
				generaProspetto3(elements, numSeggiDaAssegnare, totVoti, q.getQuoziente(), coal.getDescCoalizione());
			} catch (DocumentException e1) {
				log.error("ERRORE GENERAZIONE PROSPETTO 3 coalizione:" +coal.getDescCoalizione());
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
			
			Quoziente quozEletCirc = getQuoziente(numVoti, x.getKey().getNumSeggi(), null);
			
			elements.forEach(e->e.setQuoziente(getQuoziente(e.getCifra(), quozEletCirc.getQuoziente(), null)));
			
			assegnaseggiQIMassimiResti(elements, x.getKey().getNumSeggi(), numVoti, TipoOrdinamento.DECIMALI);
			
			mapCircListeElemento.put(x.getKey(), elements);
			
			try {
				generaProspetto5_10(x.getKey().getDescrizione(), elements, quozEletCirc.getQuoziente(), x.getKey().getNumSeggi(), numVoti, 5);
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
					.stream().collect(Collectors.groupingBy(Elemento::getIdCoalizione)).entrySet().forEach(m -> {
						m.getValue().forEach(lis->{
							
							lista.add(lis);
						});
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
		
		generaProspetto6_11(listProspetto6, 6);
		
		return listProspetto6;
	}
	
	private void compensazioneCircoscrizionaleCoalizioni(List<Confronto> listProspetto6) throws DocumentException {

		List<Confronto> eccedntarieNaz = listProspetto6.stream().filter(l->l.getDiff().compareTo(0)>0).collect(Collectors.toList());
		
		eccedntarieNaz = sortByDiffCifra(eccedntarieNaz);
		
		eccedntarieNaz.forEach(e->{
			List<Elemento> elements = mapEccedentarieCoalizioni.get(e.getId());
			
			elements.sort(compareByDecimale(Ordinamento.DESC));
		});
		
		List<Elemento> listaDeficitarie = mapDeficitarieCoalizioni.values().stream().flatMap(List::stream).collect(Collectors.toList());
		

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
			
			if((terrEccedentaria.equals(def.getTerritorio()) || isShift) && def.getSeggiDecimali().compareTo(0) == 0 && !def.isRiceveSeggio()) {
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
					mapEccedentarieListe.put(lista.stream().map(Elemento::getId).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per eccedentarie" )), lista);
				}else if(prosp.getDiff().compareTo(0) < 0){
					//DEFICITARIE
					mapDeficitarieListe.put(lista.stream().map(Elemento::getId).distinct().findFirst().orElseThrow(() -> new RuntimeException( "no value found per deficitarie" )), lista);
				}
				
			}
		});
		
		generaProspetto6_11(listProspetto6, 11);
		
		return listProspetto6;
	}

	private void compensazioneCircoscrizionaleListe(List<Confronto> listProspetto11) throws DocumentException {

		List<Confronto> eccedntarieNaz = listProspetto11.stream().filter(l->l.getDiff().compareTo(0)>0).collect(Collectors.toList());
		
		eccedntarieNaz = sortByDiffCifra(eccedntarieNaz);
		
		eccedntarieNaz.forEach(e->{
			List<Elemento> elements = mapEccedentarieListe.get(e.getId());
			
			elements.sort(compareByDecimale(Ordinamento.DESC));
		});
		
		List<Elemento> listaDeficitarie = mapDeficitarieListe.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		AtomicInteger ordineSottrazione = new AtomicInteger(1);
		
		eccedntarieNaz.forEach(e->{
			
			log.info("Lista ECCEDNTARIA: {}", e.getDescLista());
			
			boolean isSorteggio = false;
			
			AtomicInteger seggiDaAssegnare = new AtomicInteger(e.getDiff());
			
			List<Elemento> listeEccedntarie = mapEccedentarieListe.get(e.getId());
			
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
		
		mapEccedentarieListe.entrySet().forEach(e->{
			try {
				generaProspetto7_12(e.getValue(), 12);
			} catch (DocumentException e1) {
				log.error("ERROR GENERAZIONE PROSPETTO 12:{}", e1.getMessage());			
				}
		});
		
		List<Elemento> deficitarie = mapDeficitarieListe.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
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

	
	
}
