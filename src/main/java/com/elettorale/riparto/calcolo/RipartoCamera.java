package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.elettorale.riparto.controller.Controller;
import com.elettorale.riparto.dto.Base;
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
import com.lowagie.text.pdf.PdfCell;

public class RipartoCamera extends AppoggioStampa{

	Logger log = LoggerFactory.getLogger(Controller.class);
	
	private Document document = new Document(PageSize.A4.rotate());
	private List<Base> list;
	private AtomicInteger pageCount = new AtomicInteger(1);
	private RipartoUtils ripartoUtils = new RipartoUtils();;

	public RipartoCamera(List<Base> list) {
		this.list = list;
	}

	public void eseguiRiparto() throws FileNotFoundException, DocumentException {


		PdfWriter.getInstance(document, new FileOutputStream("H:\\riparto.pdf"));

		document.open();

		calcolaCifraNazionale();
		
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
	}

	public void calcolaCifraNazionale() throws DocumentException {
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
		BigDecimal soglia1 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti * ((double)Sbarramento.UNO.getValue()/100)), 2);
		
		this.setVotiValidi1(soglia1);
		
		nazionali.forEach(e-> {
			if(BigDecimal.valueOf(e.getVotiLista()).compareTo(soglia1) >= 0) {
				e.setPartecipaRipartoLista(PartecipaRiparto.SI.toString());
			}else {
				e.setPartecipaRipartoLista(PartecipaRiparto.NO.toString());
			}
		});
		
		//Set Cifra Coalizioni 
		Map<Integer, List<Base>> mapCoali = nazionali.stream()
				.filter(l -> l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString()))
				.collect(Collectors.groupingBy(Base::getCoterCoali));
		
		mapCoali.forEach((k,v)->{
			
			if(v.size() > 1) {
				//sono in una coalizione e calcolo totale voti coali solo delle liste ammesse al riparto
				Integer votiCoali = v.stream().filter(l->l.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())).mapToInt(Base::getVotiLista).sum();
				v.forEach(el -> el.setCifraCoalizione(votiCoali));
			}else {
				//negli altri casi la cifra coali è quella della lista solo se la lista partecipa al riparto
				v.forEach(el -> {
					if(el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
						el.setCifraCoalizione(el.getVotiLista());
					}
				});
			}
			
		});
		
		//Sbarramento Coalizioni 3%
		BigDecimal soglia3 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.TRE.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia3);
		
		mapCoali.forEach((k,v)->{
			
			if(v.size() > 1) {
				v.forEach(el -> {
					//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
					if(BigDecimal.valueOf(el.getCifraCoalizione()).compareTo(soglia3) >= 0 && el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
						el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
					}else {
						el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
					}
				});
			}else {
				//negli altri casi la cifra coali è quella della lista solo se la lista partecipa al riparto
				v.forEach(el -> {
					if(el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
						el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
					}else {
						el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
					}
				});
			}
			
		});
		
		//Calcolo % Coalizione
		mapCoali.forEach((k,v)->{
			v.forEach(el -> {
				if(el.getPartecipaRipartoCoalizione().equals(PartecipaRiparto.SI.toString())) {
					el.setPercentualeCoalizione(ripartoUtils.truncateDecimal(new BigDecimal(((double)el.getCifraCoalizione()/sommaVoti)*100), 3));
				}
			});
		});
		
		log.info("GENERAZIONE PROSPETTO 1");
		generaProspetto1(nazionali);
		log.info("FINE GENERAZIONE PROSPETTO 1");
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
	
	private void generaProspetto1(List<Base> nazionali) throws DocumentException {

		nazionali.sort((e1,e2) -> e1.getCoterCoali().compareTo(e2.getCoterCoali()));
		
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
			
			nazionali.stream().filter(x->x.getCoterCoali().compareTo(el) == 0).forEach(e->{
				PdfPCell cell = new PdfPCell();				
				cell.addElement(addParagraph(e.getDescLista()));
				table.addCell(cell);
				
				PdfPCell cell2 = new PdfPCell();	
				cell2.addElement(addParagraph(String.valueOf(e.getVotiLista())));
				table.addCell(cell2);
				
				PdfPCell cell3 = new PdfPCell();	
				cell3.addElement(addParagraph(String.valueOf(e.getPercentualeLista())));
				table.addCell(cell3);
				
				PdfPCell cell4 = new PdfPCell();	
				cell4.addElement(addParagraph(e.getPartecipaRipartoLista()));
				table.addCell(cell4);
				
				PdfPCell cell5 = new PdfPCell();	
				cell5.addElement(addParagraph(String.valueOf(e.getCifraCoalizione())));
				table.addCell(cell5);
				
				PdfPCell cell6 = new PdfPCell();	
				cell6.addElement(addParagraph(String.valueOf(e.getPercentualeCoalizione())));
				table.addCell(cell6);
				
				PdfPCell cell7 = new PdfPCell();	
				cell7.addElement(addParagraph(e.getPartecipaRipartoCoalizione()));
				table.addCell(cell7);
				
				PdfPCell cell8 = new PdfPCell();	
				cell8.addElement(addParagraph(String.valueOf(e.getCoterCoali())));
				table.addCell(cell8);
			});
		
			
		});
//		nazionali.forEach(e-> {
//			table.addCell(getTablePhrase(e.getDescLista()));
//			table.addCell(getTablePhrase(String.valueOf(e.getVotiLista())));
//			table.addCell(getTablePhrase(String.valueOf(e.getPercentualeLista())));
//			table.addCell(getTablePhrase(e.getPartecipaRipartoLista()));
//			table.addCell(getTablePhrase(String.valueOf(e.getCifraCoalizione())));
//			table.addCell(getTablePhrase(String.valueOf(e.getPercentualeCoalizione())));
//			table.addCell(getTablePhrase(e.getPartecipaRipartoCoalizione()));
//			table.addCell(getTablePhrase(String.valueOf(e.getCoterCoali())));
//			
//		});
		
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
	
	private Phrase getTablePhrase(String value) {
		value = Objects.isNull(value) ? "" : value;
		return new Phrase(new Chunk(value, FontFactory.getFont(FontFactory.COURIER, 8,BaseColor.BLACK)));
	}

	private static void addTableHeader(PdfPTable table) {
		Stream.of(Header.values())
	      .forEach(columnTitle -> {
	        PdfPCell header = new PdfPCell();
	        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	        header.setBorderWidth(2);
	        header.setPhrase(new Phrase(columnTitle.getValue()));
	        table.addCell(header);
	    });		
	}
}
