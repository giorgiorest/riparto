package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elettorale.riparto.constants.Header;
import com.elettorale.riparto.constants.Prospetto2;
import com.elettorale.riparto.constants.Prospetto5;
import com.elettorale.riparto.constants.Prospetto7;
import com.elettorale.riparto.constants.Prospetto8;
import com.elettorale.riparto.dto.Base;
import com.elettorale.riparto.dto.Coalizione;
import com.elettorale.riparto.utils.RipartoUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.StringUtils;

public class RipartoCamera extends AppoggioStampa{

	Logger log = LoggerFactory.getLogger(RipartoCamera.class);
	
	private Document document = new Document(PageSize.A4.rotate());
	private List<Base> list;
	private AtomicInteger pageCount = new AtomicInteger(1);
	private RipartoUtils ripartoUtils = new RipartoUtils();;
	private Map<Territorio, List<Coalizione>> mapCircListe = new HashMap<>();
	private Map<Territorio, List<Elemento>> mapCircListeElemento = new HashMap<>();
	
	//lista eccedntarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapEccedentarie = new HashMap<>();
	
	//mappa deficitarie(key idAggregato, value liste)
	private Map<Integer, List<Elemento>> mapDeficitarie = new HashMap<>();
	
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

		//START livello NAZIONE
		//PROSPETTO 1
		List<Coalizione> nazionali = calcolaCifraNazionale();
		
		List<Elemento> ripartoColizioniNazionale = ripartoCoalizioniListeNazionali(nazionali, dataElezione);
		
		List<Elemento> ripartoColizioni = ripartoTraListeInCoalizione(ripartoColizioniNazionale, nazionali);
		//END livello NAZIONE
		
		//START livello CIRCOSCRIZIONE
		ripartoListeCircoscrizione(nazionali);
		//START livello CIRCOSCRIZIONE
		
		//COnfranto tra livello circ e naz
		List<Prospetto6> listProspetto6 = confrontoCircoscrizionaleNazionale(ripartoColizioniNazionale);
		
//		//Compensazione Eccedentarie Deficitarie Livello circoscrizione
		compensazioneCircoscrizionale(listProspetto6);
		
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
		return path;
	}


	public List<Coalizione> calcolaCifraNazionale() throws DocumentException {
		log.info("ESTRAGGO LISTE NAZIONALI E SOMMO VOTI");

//		List<Base> nazionali = new ArrayList<>();
		List<Elemento> nazionali = new ArrayList<>();

		//Raggruppo dati a livello nazione
//		list.stream().collect(Collectors.groupingBy(Base::getIdAggregatoRiparto)).entrySet().stream().collect(
//				Collectors.toMap(y -> {
//					Base b = y.getValue().stream().findFirst().orElseThrow();
//					
//					return new Base(b.getDescLista(), b.getCoterCoali(), b.getIdAggregatoRiparto());
//				}, x -> x.getValue().stream().mapToInt(Base::getVotiLista).sum()))
//				.entrySet().forEach(e -> {
//					Base b = new Base(e.getKey().getDescLista(), e.getKey().getCoterCoali(), e.getKey().getIdAggregatoRiparto());
//					b.setVotiLista(e.getValue());
//					b.setCcp(e.getKey().getIdAggregatoRiparto());
//					nazionali.add(b);
//				});
		list.stream().collect(Collectors.groupingBy(Base::getIdAggregatoRiparto)).entrySet().forEach(y->{
			Base base = y.getValue().stream().findFirst().orElseThrow();
			nazionali.add(new Elemento(base.getIdAggregatoRiparto(), base.getDescLista(),
					y.getValue().stream().mapToInt(Base::getVotiLista).sum(), null, null, base.getCoterCoali()));
		});
				
		log.info("CIFRA LISTE NAZIONALI CALCOLATA");
		
		//Calcolo totale voti liste
		Integer sommaVoti = nazionali.stream().mapToInt(Elemento::getCifra).sum();
		
		this.setTotaleVotiValidi(sommaVoti);
		
		//Calcolo % Lista
		nazionali.forEach(c->c.setPercentualeLista(ripartoUtils.truncateDecimal(new BigDecimal(((double)c.getCifra()/sommaVoti)*100), 3)));
		
		//Sbarramento Liste 3%
		BigDecimal soglia3 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.TRE.getValue()/100)), 2);

//		Sbarramento Liste 1%
		BigDecimal soglia1 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.UNO.getValue()/100)), 2);
		
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
		BigDecimal soglia10 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.DIECI.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia10);
		
		AtomicInteger sumvoticoali = new AtomicInteger(0);
		
		nazionaliCoaliListe.forEach(el->{
			//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
			if(BigDecimal.valueOf(((Coalizione)el).getNumVotiCoalizione()).compareTo(soglia10) >= 0) {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				//Calcolo % Coalizione
				el.setPercentualeCoalizione(ripartoUtils.truncateDecimal(new BigDecimal(((double)((Coalizione)el).getNumVotiCoalizione()/sommaVoti)*100), 3));
				sumvoticoali.getAndAdd(((Coalizione)el).getNumVotiCoalizione());
			}else {
				el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
			}
		});
		
		creazioneMappaLivelloCircoscrizione(nazionaliCoaliListe);
	
		log.info("GENERAZIONE PROSPETTO 1");
		generaProspetto1(nazionaliCoaliListe, sumvoticoali);
		log.info("FINE GENERAZIONE PROSPETTO 1");
		
		return nazionaliCoaliListe;
	}

	private Predicate<Elemento> partecipaCifraInCoalizione() {
		return l->l.getPartecipaInCoalizione().equals(PartecipaRiparto.SI.toString());
	}

	private Predicate<Elemento> partecipaRipartoLista() {
		return l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString());
	}

	private void creazioneMappaLivelloCircoscrizione(List<Coalizione> nazionaliCoaliListe) {
		
		
		//Prendo solo le lste che hanno superato lo sbarramento
		List<Coalizione> listeAmmesse = nazionaliCoaliListe.stream()
				.filter(c -> c.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString()))
				.collect(Collectors.toList());
		
//		Creo il territorio circoscrizione
		List<Territorio> territorioCircList = list.stream().collect(Collectors.groupingBy(Base::getIdCircoscrizione)).entrySet().stream().map(m->{
			Base circ = m.getValue().stream().findFirst().orElseThrow();
			
			Integer numSeggiCirc = m.getValue().stream().collect(Collectors.groupingBy(Base::getIdCollegioPluri)).entrySet().stream().collect(
					Collectors.toMap(x -> x.getKey(), y -> (y.getValue().stream().map(Base::getNumSeggi).distinct().findFirst().orElseThrow()))).entrySet().stream().mapToInt(p->p.getValue()).sum();
					
					//map(Base::getNumSeggi).distinct().mapToInt(l->l).sum();
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
						
						listeCircoscrizione.stream().forEach(l->{
							Elemento el = new Elemento(l.getIdAggregatoRiparto(), l.getDescLista(), sumVotiListaCirc, null, null, l.getCoterCoali());
							el.setPartecipaRipartoLista(lista.getPartecipaRipartoLista());
							el.setPartecipaInCoalizione(lista.getPartecipaInCoalizione());
							liste.add(el);
						});
						
//						Base base = listeCircoscrizione.stream().findFirst().orElseThrow();
//						
//						Base b = new Base();
//						b.setDescLista(base.getDescLista());
//						b.setDescCircoscrizione(terpa.getDescrizione());
//						b.setIdCircoscrizione(terpa.getId());
//						b.setCoterCoali(base.getCoterCoali());
//						b.setIdAggregatoRiparto(base.getIdAggregatoRiparto());
//						b.setVotiLista(sumVotiListaCirc);
//						b.setPartecipaRipartoLista(lista.getPartecipaRipartoLista());
						
					}
					
				});
				
				coali = new Coalizione(liste);
				coali.setDescCoalizione(liste.stream().map(Elemento::getDescrizione).distinct().collect(Collectors.joining("-")));
				coali.setNumVotiCoalizione(liste.stream().mapToInt(Elemento::getCifra).sum());
				coali.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				
				listaCoalizione.add(coali);
			
			
		});
			mapCircListe.put(terpa,listaCoalizione);
		
		});
		
		
	}

	private void generaProspetto1(List<Coalizione> nazionali, AtomicInteger sumvoticoali) throws DocumentException {

		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 1", 15));
		
		document.add(addParagraph("TOTALE VOTI VALIDI = "+this.getTotaleVotiValidi().toString(), 12));
		document.add(addParagraph("VOTI VALIDI 3% = "+this.getVotiValidi1().toString(), 12));
		document.add(addParagraph("VOTI VALIDI 10% = "+this.getVotiValidi3().toString(), 12));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 12));
		
		float[] width = {20,10,10,5,10,10,5,35};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader(table);
		
		nazionali.forEach(coal->{
			coal.getListe().forEach(lista->{
				PdfPCell cell = new PdfPCell();
				PdfPCell cell2 = new PdfPCell();
				PdfPCell cell3 = new PdfPCell();
				PdfPCell cell4 = new PdfPCell();
				PdfPCell cell5 = new PdfPCell();
				PdfPCell cell6 = new PdfPCell();
				PdfPCell cell7 = new PdfPCell();
				PdfPCell cell8 = new PdfPCell();
				
				//LISTA
				cell.addElement(addParagraph(lista.getDescrizione(), 10));
				cell2.addElement(addParagraph(String.valueOf(lista.getCifra()), 10));
				cell3.addElement(addParagraph(String.valueOf(lista.getPercentualeLista()), 10));
				cell4.addElement(addParagraph(lista.getPartecipaRipartoLista(), 10));

				//COALIZIONE
				cell5.addElement(addParagraph(String.valueOf(coal.getNumVotiCoalizione()), 10));
				cell6.addElement(addParagraph(String.valueOf(Objects.isNull(coal.getPercentualeCoalizione()) ? "" : coal.getPercentualeCoalizione()), 10));
				cell7.addElement(addParagraph(coal.getPartecipaRipartoCoalizione(), 10));
				cell8.addElement(addParagraph(String.valueOf(coal.getDescCoalizione()), 10));

				table.addCell(cell);
				table.addCell(cell2);
				table.addCell(cell3);
				table.addCell(cell4);
				table.addCell(cell5);
				table.addCell(cell6);
				table.addCell(cell7);
				table.addCell(cell8);
				
			});
		});

		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		document.add(addParagraph("VOTI VALIDI COALIZIONE = "+ String.valueOf(sumvoticoali), 10));

			
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
		
		RipartoUtils utils = new RipartoUtils();		
		
		//mapping liste coalizoni in ELemento
		List<Elemento> elements = new ArrayList<>();
		
		listeCoaliAmmesse.forEach(e ->{
			if( e instanceof Coalizione) {
				Coalizione c = (Coalizione)e;
				c.getListe().removeIf(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.NO.toString()));
				
				elements.add(utils.new Elemento(null, c.getDescCoalizione(), c.getNumVotiCoalizione(),
						null, c.getListe().stream().map(Elemento::getDescrizione).collect(Collectors.toList()),
						c.getIdCoalizone()));
				
			}else {
				elements.add(utils.new Elemento(null, e.getDescCoalizione(), e.getNumVotiCoalizione(), null,
						Arrays.asList(e.getDescCoalizione()), e.getIdCoalizone()));
			}
			
		});
		
		//Calcolo Tot votiCoali
		Integer sumVotiCOali = elements.stream().mapToInt(Elemento::getCifra).sum();
		
		//TODO recupera voti da db
		Integer numSeggi = 245;

		Quoziente q = utils.assegnaseggiQIMassimiResti(elements, numSeggi, sumVotiCOali,TipoOrdinamento.RESTI);
		
		log.info("GENERAZIONE PROSPETTO 2");
		generaProspetto2(elements, sumVotiCOali, q.getQuoziente());
		log.info("FINE GENERAZIONE PROSPETTO 2");
		
		return elements;
	}
	
	private void generaProspetto2(List<Elemento> elements, Integer sumVotiCOali, Integer quoziente) throws DocumentException {

		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 2", 15));
		
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = "+ String.valueOf(sumVotiCOali), 15));
		document.add(addParagraph("NUM SEGGI = "+ String.valueOf(245), 15));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = "+ String.valueOf(quoziente), 15));
		
		if(elements.stream().map(Elemento::getSorteggio).filter(w->w).findAny().isPresent()) {
			document.add(addParagraph("BLOCCO NAZIONALE", 12));
		}

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));
		
		float[] width = {40,30,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader2(table);
		
		elements.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescrizione(), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getCifra()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getResto()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getSeggiResti()), 10));
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
	}

	private List<Elemento> ripartoTraListeInCoalizione(List<Elemento> ripartoColizioniNazionale, List<Coalizione> nazionali) throws DocumentException {

		List<Elemento> ret = new ArrayList<>();
		RipartoUtils utils = new RipartoUtils();		
		
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
			
			log.info("GENERAZIONE PROSPETTO 3");
			try {
				generaProspetto3(elements, numSeggiDaAssegnare, totVoti, q.getQuoziente(), coal.getDescCoalizione());
			} catch (DocumentException e1) {
				// TODO Auto-generated catch block
				log.error("ERRORE GENERAZIONE PROSPETTO 3 coalizione:" +coal.getDescCoalizione());
			}
			log.info("FINE GENERAZIONE PROSPETTO 3");
		});
		
		return ret;
	}

	private void generaProspetto3(List<Elemento> elements, Integer numSeggiDaAssegnare, Integer totVoti, Integer quoziente, String coalizione) throws DocumentException {
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 3", 15));
		
		document.add(addParagraph("COALIZIONE = "+ coalizione, 15));
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = "+ String.valueOf(totVoti), 15));
		document.add(addParagraph("NUM SEGGI = "+ String.valueOf(numSeggiDaAssegnare), 15));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = "+ String.valueOf(quoziente), 15));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));
		
		float[] width = {40,30,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader2(table);
		
		elements.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescrizione(), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getCifra()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getResto()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getSeggiResti()), 10));
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		if(elements.stream().map(Elemento::getSorteggio).distinct().findFirst().orElse(false)) {
			Paragraph pp = new Paragraph();
			Chunk chunk = new Chunk("Alcuni seggi non sono stati assegnati.", FontFactory.getFont(FontFactory.COURIER, 15,Font.BOLD, BaseColor.RED));		
			pp.add(chunk);
			document.add(pp);
		}
		
		document.add(p);
		
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
				log.info("GENERAZIONE PROSPETTO 5");
				generaProspetto5(x.getKey().getDescrizione(), elements, quozEletCirc.getQuoziente(), x.getKey().getNumSeggi(), numVoti);
				log.info("FINE GENERAZIONE PROSPETTO 5");
			} catch (DocumentException e) {
				log.error("ERRORE GENERAZIONE PROSPETTO 5 CIRCOSCRIZIONE:" +x.getKey().getDescrizione());
				e.printStackTrace();
			}
		});
		mapCircListe = null;
	}

	private void generaProspetto5(String descCirc, List<Elemento> elements, Integer quoziente, Integer numSeggi, Integer numVoti) throws DocumentException {
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 5", 15));
		
		document.add(addParagraph("CIRCOSCRIZIONE = "+ descCirc, 15));
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = "+ String.valueOf(numVoti), 15));
		document.add(addParagraph("NUM SEGGI = "+ String.valueOf(numSeggi), 10));
		int sum = elements.stream().mapToInt(l->l.getSeggiDecimali()+l.getSeggiQI()).sum();
		Paragraph seggi = new Paragraph();
		Chunk chunk1 = new Chunk("SEGGI ASSEGNATI = "+sum, FontFactory.getFont(FontFactory.COURIER, 15,Font.BOLD, BaseColor.RED));		
		seggi.add(chunk1);
		document.add(seggi);
		document.add(addParagraph("QUOZIENETE ELETTORALE CIRC = "+ String.valueOf(quoziente), 10));
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 10));
		
		float[] width = {40,20,20,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader5(table);
		
		elements.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescrizione(), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getCifra()), 10));
			PdfPCell cell12 = new PdfPCell();
			cell12.addElement(addParagraph(String.valueOf(e.getQuoziente().getQuozienteAttribuzione()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getQuoziente().getDecimale()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getSeggiDecimali()), 10));
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell12);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 10));
		
		if(elements.stream().map(Elemento::getSorteggio).distinct().findFirst().orElse(false)) {
			Paragraph pp = new Paragraph();
			Chunk chunk = new Chunk("Alcuni seggi non sono stati assegnati.", FontFactory.getFont(FontFactory.COURIER, 15,Font.BOLD, BaseColor.RED));		
			pp.add(chunk);
			document.add(pp);
		}
		
	}
	
	private List<Prospetto6> confrontoCircoscrizionaleNazionale(List<Elemento> ripartoColizioniNazionale) throws DocumentException {
		
		List<Coalizione> elements = new ArrayList<>();
		
		List<Prospetto6> listProspetto6 = new ArrayList<>();
		
		ripartoColizioniNazionale.forEach(l->{
			Prospetto6 prosp = new Prospetto6();
			
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
				mapEccedentarie.put(lista.stream().map(Elemento::getIdCoalizione).distinct().findFirst().orElseThrow(), lista);
			}else if(prosp.getDiff().compareTo(0) < 0){
				//DEFICITARIE
				mapDeficitarie.put(lista.stream().map(Elemento::getIdCoalizione).distinct().findFirst().orElseThrow(), lista);
			}
		});
		
		log.info("GENERAZIONE PROSPETTO 6");
		generaProspetto6(listProspetto6);
		log.info("FINE GENERAZIONE PROSPETTO 6");
		
		return listProspetto6;
	}
	
	private void generaProspetto6(List<Prospetto6> listProspetto6) throws DocumentException {
		
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 6", 15));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));
		
		float[] width = {30,20,20,20,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader6(table);
		
		listProspetto6.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescLista(), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getSeggiQICirc()), 10));
			PdfPCell cell12 = new PdfPCell();
			cell12.addElement(addParagraph(String.valueOf(e.getSeggiTotCirc()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiNazionali()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getDiff()), 10));
			
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell12);
			table.addCell(cell3);
			table.addCell(cell4);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		
	}

	private void compensazioneCircoscrizionale(List<Prospetto6> listProspetto6) throws DocumentException {

		RipartoUtils utils = new RipartoUtils();
		
		List<Prospetto6> eccedntarieNaz = listProspetto6.stream().filter(l->l.getDiff().compareTo(0)>0).collect(Collectors.toList());
		
		eccedntarieNaz = utils.sortByDiffCifra(eccedntarieNaz);
		
		eccedntarieNaz.forEach(e->{
			List<Elemento> elements = mapEccedentarie.get(e.getId());
			
			elements.sort(compareByDecimale(Ordinamento.DESC));
		});
		
		List<Elemento> listaDeficitarie = mapDeficitarie.values().stream().flatMap(List::stream).collect(Collectors.toList());
		

		AtomicInteger ordineSottrazione = new AtomicInteger(1);
		
		eccedntarieNaz.forEach(e->{
			
			log.info("Lista ECCEDNTARIA: {}", e.getDescLista());
			
			boolean isSorteggio = false;
			
			AtomicInteger seggiDaAssegnare = new AtomicInteger(e.getDiff());
			
			List<Elemento> listeEccedntarie = mapEccedentarie.get(e.getId());
			
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
						
						boolean assegnato = false;
						
						//cerco deficitaria nello stesso territorio
						assegnato = assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, false);
						
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
							assegnato = assegnaSeggiDeficitaria(listaDeficitarie, ordineSottrazione, seggiDaAssegnare, ecc, terrEcc, true);

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
		
		mapEccedentarie.entrySet().forEach(e->{
			try {
				log.info("GENERAZIONE PROSPETTO 7");
				generaProspetto7(e.getValue());
				log.info("FINE GENERAZIONE PROSPETTO 7");
			} catch (DocumentException e1) {
				log.error("ERROR GENERAZIONE PROSPETTO 7:{}", e1.getMessage());			
				}
		});
		
		List<Elemento> deficitarie = mapDeficitarie.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		deficitarie.sort(compareByDecimale(Ordinamento.DESC));
		
		log.info("GENERAZIONE PROSPETTO 8");
		try {
			generaProspetto8(deficitarie);
		} catch (DocumentException e1) {
			log.error("ERROR GENERAZIONE PROSPETTO 8:{}", e1.getMessage());	
		}
		log.info("FINE GENERAZIONE PROSPETTO 8");
		
	}

	private boolean puoAssegnareSeggio(Elemento ecc) {
		return ecc.getSeggiDecimali().compareTo(0) > 0 && !ecc.isCedeSeggio();
	}

	private boolean assegnaSeggiDeficitaria(List<Elemento> listaDeficitarie, AtomicInteger ordineSottrazione,
			AtomicInteger seggiDaAssegnare, Elemento eccedentaria, Territorio terrEccedentaria, boolean isShift) {
		listaDeficitarie.sort(compareByDecimale(Ordinamento.ASC));
		
		boolean assegnato = false;
		
		for (Elemento def : listaDeficitarie) {
			//se deficitaria nello stesso territorio e non ha preso seggi con decimali può prendere seggio
			
			if((terrEccedentaria.equals(def.getTerritorio()) || isShift) && def.getSeggiDecimali().compareTo(0) == 0 && !def.isRiceveSeggio()) {
				log.info("Assegnato 1 seggio a: {} in {}. RIMANENTI: {}", def.getDescrizione(),def.getTerritorio().getDescrizione(), seggiDaAssegnare.get());
				seggiDaAssegnare.getAndDecrement();
				
				def.setRiceveSeggio(true);
				eccedentaria.setCedeSeggio(true);
				
				def.setOrdineSottrazione(ordineSottrazione.get());
				eccedentaria.setOrdineSottrazione(ordineSottrazione.getAndIncrement());
				
				def.setShift(isShift);
				assegnato = !assegnato;
				break;
			}
		}//end loop deficitarie
		return assegnato;
	}

	private void generaProspetto8(List<Elemento> lista) throws DocumentException {

		lista.sort(compareByDecimale(Ordinamento.ASC));
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 8", 15));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));
		
		float[] width = {10,25,10,10,10,25,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader8(table);
		
		lista.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(String.valueOf(e.getTerritorio().getCodEnte()), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getTerritorio().getDescrizione()), 10));
			PdfPCell cell12 = new PdfPCell();
			cell12.addElement(addParagraph(String.valueOf(e.getQuoziente().getDecimale()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getSeggiDecimali()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getDescrizione()), 10));
			PdfPCell cell6 = new PdfPCell();
			cell6.addElement(addParagraph(String.valueOf(Objects.isNull(e.getOrdineSottrazione()) ? "" : e.getOrdineSottrazione()+(e.isShift() ? " (*)" : "")), 10));
			
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell12);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
			table.addCell(cell6);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		
	}

	private Comparator<? super Elemento> compareByDecimale(Ordinamento ordinamento) {
		switch (ordinamento) {
		case DESC:
			return (le, re) -> re.getQuoziente().getDecimale().compareTo(le.getQuoziente().getDecimale());
		case ASC:
			return (le, re) -> le.getQuoziente().getDecimale().compareTo(re.getQuoziente().getDecimale());
		default:
			//default ordina ASC
			return (le, re) -> le.getQuoziente().getDecimale().compareTo(re.getQuoziente().getDecimale());
		}
	}
	
	private void generaProspetto7(List<Elemento> lista) throws DocumentException {
		
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		String descLista = lista.stream().map(Elemento::getDescrizione).distinct().findFirst().orElseThrow();
		document.add(addParagraph("PROSPETTO 7", 15));
		document.add(addParagraph(descLista, 13));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));
		
		float[] width = {10,30,20,20,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader7(table);
		
		lista.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(String.valueOf(e.getTerritorio().getCodEnte()), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getTerritorio().getDescrizione()), 10));
			PdfPCell cell12 = new PdfPCell();
			cell12.addElement(addParagraph(String.valueOf(e.getQuoziente().getDecimale()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getSeggiDecimali()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(Objects.isNull(e.getOrdineSottrazione()) ? "" : e.getOrdineSottrazione()), 10));
			
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell12);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		
	}

	
	private Paragraph addParagraph(String value, int fontSize) {
		value = Objects.isNull(value) ? "" : value;
		Paragraph p = new Paragraph();
		Chunk chunk = new Chunk(value, FontFactory.getFont(FontFactory.COURIER, fontSize,Font.BOLD));		
		p.add(chunk);
		return p;
	}
	
	@SuppressWarnings("unused")
	private Phrase getTablePhrase(String value) {
		value = Objects.isNull(value) ? "" : value;
		return new Phrase(new Chunk(value, FontFactory.getFont(FontFactory.COURIER, 8,BaseColor.BLACK)));
	}

	private void addTableHeader(PdfPTable table) {
		Stream.of(Header.values())
	      .forEach(columnTitle -> {
	        PdfPCell header = new PdfPCell();
	        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        header.setBorderWidth(2);
	        header.setPhrase(new Phrase(columnTitle.getValue()));
	        table.addCell(header);
	    });		
	}
	
	private void addTableHeader2(PdfPTable table) {
		Stream.of(Prospetto2.values())
	      .forEach(columnTitle -> {
	        PdfPCell header = new PdfPCell();
	        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        header.setBorderWidth(2);
	        header.setPhrase(new Phrase(columnTitle.getValue()));
	        table.addCell(header);
	    });		
	}
		
	private void addTableHeader5(PdfPTable table) {
		Stream.of(Prospetto5.values())
	      .forEach(columnTitle -> {
	        PdfPCell header = new PdfPCell();
	        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        header.setBorderWidth(2);
	        header.setPhrase(new Phrase(columnTitle.getValue()));
	        table.addCell(header);
	    });		
	}
	
	private void addTableHeader6(PdfPTable table) {
		Stream.of(com.elettorale.riparto.constants.Prospetto6.values())
	      .forEach(columnTitle -> {
	        PdfPCell header = new PdfPCell();
	        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        header.setBorderWidth(2);
	        header.setPhrase(new Phrase(columnTitle.getValue()));
	        table.addCell(header);
	    });		
	}
	
	private void addTableHeader7(PdfPTable table) {
		Stream.of(Prospetto7.values())
		.forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});		
	}

	private void addTableHeader8(PdfPTable table) {
		Stream.of(Prospetto8.values())
		.forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});		
	}

	
	enum PartecipaRiparto{
		SI,NO;
	}
	
	enum Sbarramento{
		UNO(1),
		TRE(3),
		DIECI(10),
		VENTI(20);
		Sbarramento(int i) {
			this.value = i;
		}

		private Integer value;

		public Integer getValue() {
			return value;
		}

	}
}
