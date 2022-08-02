package com.elettorale.riparto.calcolo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
		
		ripartoCoalizioniListeNazionali(nazionali);
		
		document.close();
		
		log.info("GENERAZIONE DOCUMENTO RIPARTO");
	}

	private void ripartoCoalizioniListeNazionali(List<Base> nazionali) {
		
//		Integer votiCoalizioniListeAmmesse = nazionali.str
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
		
		//Sbarramento Coalizioni 3%
		BigDecimal soglia3 = ripartoUtils.truncateDecimal(new BigDecimal(sommaVoti *((double)Sbarramento.TRE.getValue()/100)), 2);
		
		this.setVotiValidi3(soglia3);
		
		nazionaliCoaliListe.forEach(el->{
			//calcolo partecipa riparto coalizione solo per liste in coalizione che partecipano al riparto
			if(el instanceof Coalizione) {
				if(BigDecimal.valueOf(((Coalizione)el).getNumVotiCoalizione()).compareTo(soglia3) >= 0) {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
					//Calcolo % Coalizione
					el.setPercentualeCoalizione(ripartoUtils.truncateDecimal(new BigDecimal(((double)((Coalizione)el).getNumVotiCoalizione()/sommaVoti)*100), 3));
				}else {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
				}
				
			}else {
				//negli altri casi la cifra coali è quella della lista solo se la lista partecipa al riparto
				if(el.getPartecipaRipartoLista().equals(PartecipaRiparto.SI.toString())) {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.SI.toString());
				}else {
					el.setPartecipaRipartoCoalizione(PartecipaRiparto.NO.toString());
				}
			}
		});
		
		log.info("GENERAZIONE PROSPETTO 1");
		generaProspetto1(nazionaliCoaliListe);
		log.info("FINE GENERAZIONE PROSPETTO 1");
		
		return nazionali;
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
					cell6.addElement(addParagraph(String.valueOf(c.getPercentualeCoalizione())));
					
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
