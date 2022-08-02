package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elettorale.riparto.constants.Header;
import com.elettorale.riparto.constants.Prospetto2;
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

public class RipartoCamera extends AppoggioStampa{

	Logger log = LoggerFactory.getLogger(RipartoCamera.class);
	
	private Document document = new Document(PageSize.A4.rotate());
	private List<Base> list;
	private AtomicInteger pageCount = new AtomicInteger(1);
	private RipartoUtils ripartoUtils = new RipartoUtils();;

	public RipartoCamera(List<Base> list) {
		this.list = list;
	}

	public void eseguiRiparto(boolean isLocal) throws FileNotFoundException, DocumentException {


		String path;
		if(isLocal) {
			path = "C:\\workspace\\riparto.pdf";
		}else {
			path = "\\\\nas-files-srv2\\Condivisioni\\acc_inm\\testRipartoExcel\\ripartonew\\riparto.pdf";
		}
		PdfWriter.getInstance(document, new FileOutputStream(path));

		document.open();

		List<Base> nazionali = calcolaCifraNazionale();
		
		List<Elemento> ripartoColizioniNazionale = ripartoCoalizioniListeNazionali(nazionali);
		
		ripartoTraListeInCoalizione(ripartoColizioniNazionale, nazionali);
		
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
	}


	private void ripartoTraListeInCoalizione(List<Elemento> ripartoColizioniNazionale, List<Base> nazionali) throws DocumentException {

		RipartoUtils utils = new RipartoUtils();		
		
		//mapping liste coalizoni in ELemento

		nazionali.stream().filter(coal->coal instanceof Coalizione && coal.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString())).forEach(coal ->{
			Coalizione c = (Coalizione)coal;
			
			//estraggo liste in coalizione per calcolo seggi
			List<Elemento> elements = new ArrayList<>();
			
			c.getListe().stream()
					.filter(l -> Objects.nonNull(l.getPartecipaRipartoLista())
							&& l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString()))
					.collect(Collectors.toList()).forEach(l-> elements.add(utils.new Elemento(c.getCoterCoali(), l.getDescLista(), l.getVotiLista(), null, null))); 
			
			Integer numSeggiDaAssegnare = ripartoColizioniNazionale.stream().filter(x->x.getId() == c.getCoterCoali()).mapToInt(a->a.getSeggiQI()+a.getSeggiResti()).sum();
			Integer totVoti = elements.stream().mapToInt(Elemento::getCifra).sum();
			
			Quoziente q = assegnaseggiQIMassimiResti(elements, numSeggiDaAssegnare, totVoti);
			
			log.info("GENERAZIONE PROSPETTO 3");
			try {
				generaProspetto3(elements, numSeggiDaAssegnare, totVoti, q.getQuoziente(), c.getDescCoalizione());
			} catch (DocumentException e1) {
				// TODO Auto-generated catch block
				log.error("ERRORE GENERAZIONE PROSPETTO 3 coalizione:" +c.getDescCoalizione());
			}
			log.info("FINE GENERAZIONE PROSPETTO 3");
		});
		
	}

	

	private void generaProspetto3(List<Elemento> elements, Integer numSeggiDaAssegnare, Integer totVoti, Integer quoziente, String coalizione) throws DocumentException {
		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 3"));
		
		document.add(addParagraph("COALIZIONE = "+ coalizione));
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = "+ String.valueOf(totVoti)));
		document.add(addParagraph("NUM SEGGI = "+ String.valueOf(numSeggiDaAssegnare)));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = "+ String.valueOf(quoziente)));
		
		document.add(Chunk.NEWLINE);
		document.add(addParagraph(""));
		
		float[] width = {40,30,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader2(table);
		
		elements.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescrizione()));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getCifra())));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI())));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getResto())));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getSeggiResti())));
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()))));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		
	}

	public List<Base> calcolaCifraNazionale() throws DocumentException {
		log.info("ESTRAGGO LISTE NAZIONALI E SOMMO VOTI");

		List<Base> nazionali = new ArrayList<>();

		//Raggruppo dati a livello nazione
		list.stream().collect(Collectors.groupingBy(Base::getDescLista)).entrySet().stream().collect(
				Collectors.toMap(y -> {
					Base b = y.getValue().stream().findFirst().orElseThrow();
					return new Base(b.getDescLista(), b.getCoterCoali());
				}, x -> x.getValue().stream().mapToInt(Base::getVotiLista).sum()))
				.entrySet().forEach(e -> {
					Base b = new Base(e.getKey().getDescLista(), e.getKey().getCoterCoali());
					b.setVotiLista(e.getValue());
					nazionali.add(b);
				});
		log.info("CIFRA LISTE NAZIONALI CALCOLATA");
		
		//Calcolo totale voti liste
		Integer sommaVoti = nazionali.stream().mapToInt(Base::getVotiLista).sum();
		
		this.setTotaleVotiValidi(sommaVoti);
		
		//Calcolo % Lista
		nazionali.forEach(c->c.setPercentualeLista(ripartoUtils.truncateDecimal(new BigDecimal(((double)c.getVotiLista()/sommaVoti)*100), 3)));
		
		//Sbarramento Liste 1%
		BigDecimal soglia1 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.TRE.getValue()/100)), 2);
		
		this.setVotiValidi1(soglia1);
		
		nazionali.forEach(e-> {
			if(BigDecimal.valueOf(e.getVotiLista()).compareTo(soglia1) >= 0) {
				e.setPartecipaRipartoLista(PartecipaRiparto.SI.toString());
			}else {
				e.setPartecipaRipartoLista(PartecipaRiparto.NO.toString());
			}
		});
		
		//Set Cifra Coalizioni 
		List<Base> nazionaliCoaliListe = new ArrayList<>();
		
		Map<Integer, List<Base>> mapCoali = nazionali.stream()
				.collect(Collectors.groupingBy(Base::getCoterCoali));
		
		mapCoali.forEach((k,v)->{
			
			if(v.size() > 1) {
				Coalizione coali = new Coalizione(v.stream().map(e -> new Base(e)).collect(Collectors.toList()));
				//sono in una coalizione e calcolo totale voti coali solo delle liste ammesse al riparto
				Integer votiCoali = v.stream().filter(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())).mapToInt(Base::getVotiLista).sum();
				coali.setNumVotiCoalizione(votiCoali);
				coali.setDescCoalizione(v.stream().map(Base::getDescLista).collect(Collectors.joining("-")));
				coali.setCoterCoali(k);
				nazionaliCoaliListe.add(coali);
			}else {
				//negli altri casi la cifra coali è quella della lista solo se la lista partecipa al riparto
				v.forEach(el -> {
					if(el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
						el.setCifraCoalizione(el.getVotiLista());
					}
					nazionaliCoaliListe.add(el);
				});
			}
			
		});
		
		//TODO Implemntare sbarramente minoranza + coalizione che partecipa con %cona < ed lista ammessa
		
		//Sbarramento Coalizioni 10%
		BigDecimal soglia3 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.DIECI.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia3);
		
		AtomicInteger sumvoticoali = new AtomicInteger(0);
		
		nazionaliCoaliListe.forEach(el->{
			//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
			if(el instanceof Coalizione) {
				if(BigDecimal.valueOf(((Coalizione)el).getNumVotiCoalizione()).compareTo(soglia3) >= 0) {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
					//Calcolo % Coalizione
					el.setPercentualeCoalizione(ripartoUtils.truncateDecimal(new BigDecimal(((double)((Coalizione)el).getNumVotiCoalizione()/sommaVoti)*100), 3));
					sumvoticoali.getAndAdd(((Coalizione)el).getNumVotiCoalizione());
				}else {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
				}
				
			}else {
				//negli altri casi la cifra coali è quella della lista solo se la lista partecipa al riparto
				if(el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
					sumvoticoali.getAndAdd(el.getVotiLista());
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				}else {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
				}
			}
		});
		
	
		log.info("GENERAZIONE PROSPETTO 1");
		generaProspetto1(nazionaliCoaliListe, sumvoticoali);
		log.info("FINE GENERAZIONE PROSPETTO 1");
		
		return nazionaliCoaliListe;
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
	
	private void generaProspetto1(List<Base> nazionali, AtomicInteger sumvoticoali) throws DocumentException {

		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 1"));
		
		document.add(addParagraph("TOTALE VOTI VALIDI = "+this.getTotaleVotiValidi().toString()));
		document.add(addParagraph("VOTI VALIDI 1% = "+this.getVotiValidi1().toString()));
		document.add(addParagraph("VOTI VALIDI 3% = "+this.getVotiValidi3().toString()));
		document.add(Chunk.NEWLINE);
		document.add(addParagraph(""));
		
		float[] width = {30,10,10,10,10,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader(table);
		
		IntStream idCoali = nazionali.stream().mapToInt(Base::getCoterCoali).distinct();
		
		idCoali.forEach(el -> {
			PdfPCell cell = new PdfPCell();
			PdfPCell cell2 = new PdfPCell();
			PdfPCell cell3 = new PdfPCell();
			PdfPCell cell4 = new PdfPCell();
			PdfPCell cell5 = new PdfPCell();
			PdfPCell cell6 = new PdfPCell();
			PdfPCell cell7 = new PdfPCell();
			PdfPCell cell8 = new PdfPCell();
			
			List<Base> list = nazionali.stream().filter(x->x.getCoterCoali().compareTo(el) == 0).collect(Collectors.toList()); 

			for (Base e : list) {
				if(e instanceof Coalizione) {
					Coalizione c = (Coalizione)e;
					c.getListe().forEach(m->{
						cell.addElement(addParagraph(m.getDescLista()));
						
						cell2.addElement(addParagraph(String.valueOf(m.getVotiLista())));
						
						cell3.addElement(addParagraph(String.valueOf(m.getPercentualeLista())));
						
						cell4.addElement(addParagraph(m.getPartecipaRipartoLista()));
						
					});
					cell5.addElement(addParagraph(String.valueOf(c.getNumVotiCoalizione())));
					cell6.addElement(addParagraph(Objects.isNull(c.getPercentualeCoalizione()) ? "" : String.valueOf(c.getPercentualeCoalizione())));
					
					cell7.addElement(addParagraph(e.getPartecipaRipartoCoalizione()));
					
					cell8.addElement(addParagraph(String.valueOf(e.getCoterCoali())));
				}else {
					
					cell.addElement(addParagraph(e.getDescLista()));
					
					cell2.addElement(addParagraph(String.valueOf(e.getVotiLista())));
					
					cell3.addElement(addParagraph(String.valueOf(e.getPercentualeLista())));
					
					cell4.addElement(addParagraph(e.getPartecipaRipartoLista()));
					
					cell5.addElement(addParagraph(String.valueOf(e.getVotiLista())));
					cell6.addElement(addParagraph(String.valueOf(e.getPercentualeLista())));
					
					cell7.addElement(addParagraph(e.getPartecipaRipartoCoalizione()));
					
					cell8.addElement(addParagraph(String.valueOf(e.getCoterCoali())));
				}
			}
		
			table.addCell(cell);
			table.addCell(cell2);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
			table.addCell(cell6);
			table.addCell(cell7);
			table.addCell(cell8);
			
		});
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
		document.add(addParagraph("VOTI VALIDI COALIZIONE = "+ String.valueOf(sumvoticoali)));
	}

	private List<Elemento> ripartoCoalizioniListeNazionali(List<Base> nazionali) throws DocumentException {
		
		//prendo solo le liste ammesse
		List<Base> listeCoaliAmmesse = nazionali.stream()
				.filter(e -> e.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString())
						)
				.collect(Collectors.toList());
		listeCoaliAmmesse.forEach(e ->{
			if( e instanceof Coalizione) {
				Coalizione c = (Coalizione)e;
				c.getListe().removeIf(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.NO.toString()));
			}
		});
		
		RipartoUtils utils = new RipartoUtils();		
		
		//mapping liste coalizoni in ELemento
		List<Elemento> elements = new ArrayList<>();
		
		listeCoaliAmmesse.forEach(e ->{
			if( e instanceof Coalizione) {
				Coalizione c = (Coalizione)e;
				c.getListe().removeIf(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.NO.toString()));
				
				elements.add(utils.new Elemento(c.getCoterCoali(), c.getDescCoalizione(), c.getNumVotiCoalizione(), null, c.getListe().stream().map(Base::getDescLista).collect(Collectors.toList())));
				
			}else {
				 elements.add(utils.new Elemento(e.getCoterCoali(), e.getDescLista(), e.getVotiLista(), null, Arrays.asList(e.getDescLista())));
			}
			
		});
		
		//Calcolo Tot votiCoali
		Integer sumVotiCOali = elements.stream().mapToInt(Elemento::getCifra).sum();
		
		//TODO recupera voti da db
		Integer numSeggi = 245;

		Quoziente q = utils.assegnaseggiQIMassimiResti(elements, numSeggi, sumVotiCOali);
		
		log.info("GENERAZIONE PROSPETTO 2");
		generaProspetto2(elements, sumVotiCOali, q.getQuoziente());
		log.info("FINE GENERAZIONE PROSPETTO 2");
		
		return elements;
	}
	
	private void generaProspetto2(List<Elemento> elements, Integer sumVotiCOali, Integer quoziente) throws DocumentException {

		document.newPage();

		document.setPageCount(pageCount.getAndIncrement());
		
		document.add(addParagraph("PROSPETTO 2"));
		
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = "+ String.valueOf(sumVotiCOali)));
		document.add(addParagraph("NUM SEGGI = "+ String.valueOf(245)));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = "+ String.valueOf(quoziente)));
		
//		document.add(addParagraph("VOTI VALIDI 3% = "+this.getVotiValidi3().toString()));
		document.add(Chunk.NEWLINE);
		document.add(addParagraph(""));
		
		float[] width = {40,30,10,10,10};

		PdfPTable table = new PdfPTable(width);
		
		table.setWidthPercentage(100);
		
		addTableHeader2(table);
		
		elements.forEach(e->{
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(e.getDescrizione()));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getCifra())));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getSeggiQI())));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getResto())));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getSeggiResti())));
			table.addCell(cell);			
			table.addCell(cell2);
			table.addCell(cell3);
			table.addCell(cell4);
			table.addCell(cell5);
		});
		
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()))));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());
		
		Paragraph p = new Paragraph();
		p.add(table);
		
		document.add(p);
	}

	private Paragraph addParagraph(String value) {
		value = Objects.isNull(value) ? "" : value;
		Paragraph p = new Paragraph();
		Chunk chunk = new Chunk(value, FontFactory.getFont(FontFactory.COURIER, 12,Font.BOLD));		
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
	
}
