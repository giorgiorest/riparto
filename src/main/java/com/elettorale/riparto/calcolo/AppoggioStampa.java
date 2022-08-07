package com.elettorale.riparto.calcolo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elettorale.riparto.constants.Header;
import com.elettorale.riparto.constants.Prospetto2;
import com.elettorale.riparto.constants.Prospetto5;
import com.elettorale.riparto.constants.Prospetto7;
import com.elettorale.riparto.constants.Prospetto8;
import com.elettorale.riparto.dto.Coalizione;
import com.elettorale.riparto.utils.Prospetto9;
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

public class AppoggioStampa extends RipartoUtils {

	Logger log = LoggerFactory.getLogger(AppoggioStampa.class);
	
	protected Document document = new Document(PageSize.A4.rotate());
	
	private Integer totaleVotiValidi;
	private BigDecimal votiValidi1;
	private BigDecimal votiValidi3;
	private BigDecimal votiValidi10;
	private BigDecimal votiValidi20;

	public Integer getTotaleVotiValidi() {
		return totaleVotiValidi;
	}

	public void setTotaleVotiValidi(Integer totaleVotiValidi) {
		this.totaleVotiValidi = totaleVotiValidi;
	}

	public BigDecimal getVotiValidi1() {
		return votiValidi1;
	}

	public void setVotiValidi1(BigDecimal votiValidi1) {
		this.votiValidi1 = votiValidi1;
	}

	public BigDecimal getVotiValidi3() {
		return votiValidi3;
	}

	public void setVotiValidi3(BigDecimal votiValidi3) {
		this.votiValidi3 = votiValidi3;
	}

	public BigDecimal getVotiValidi10() {
		return votiValidi10;
	}

	public void setVotiValidi10(BigDecimal votiValidi10) {
		this.votiValidi10 = votiValidi10;
	}

	public BigDecimal getVotiValidi20() {
		return votiValidi20;
	}

	public void setVotiValidi20(BigDecimal votiValidi20) {
		this.votiValidi20 = votiValidi20;
	}

	protected void generaProspetto1(List<Coalizione> nazionali, AtomicInteger sumvoticoali) throws DocumentException {

		document.newPage();

		document.add(addParagraph("PROSPETTO 1", 15));

		document.add(addParagraph("TOTALE VOTI VALIDI = " + this.getTotaleVotiValidi().toString(), 12));
		document.add(addParagraph("VOTI VALIDI 3% = " + this.getVotiValidi1().toString(), 12));
		document.add(addParagraph("VOTI VALIDI 10% = " + this.getVotiValidi3().toString(), 12));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 12));

		float[] width = { 20, 10, 10, 5, 10, 10, 5, 35 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader(table);

		nazionali.forEach(coal -> {
			coal.getListe().forEach(lista -> {
				PdfPCell cell = new PdfPCell();
				PdfPCell cell2 = new PdfPCell();
				PdfPCell cell3 = new PdfPCell();
				PdfPCell cell4 = new PdfPCell();
				PdfPCell cell5 = new PdfPCell();
				PdfPCell cell6 = new PdfPCell();
				PdfPCell cell7 = new PdfPCell();
				PdfPCell cell8 = new PdfPCell();

				// LISTA
				cell.addElement(addParagraph(lista.getDescrizione(), 10));
				cell2.addElement(addParagraph(String.valueOf(lista.getCifra()), 10));
				cell3.addElement(addParagraph(String.valueOf(lista.getPercentualeLista()), 10));
				cell4.addElement(addParagraph(lista.getPartecipaRipartoLista(), 10));

				// COALIZIONE
				cell5.addElement(addParagraph(String.valueOf(coal.getNumVotiCoalizione()), 10));
				cell6.addElement(addParagraph(
						String.valueOf(
								Objects.isNull(coal.getPercentualeCoalizione()) ? "" : coal.getPercentualeCoalizione()),
						10));
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
		document.add(addParagraph("VOTI VALIDI COALIZIONE = " + String.valueOf(sumvoticoali), 10));

		log.info("generato PROSPETTO 1");
	}

	protected void generaProspetto2(List<Elemento> elements, Integer sumVotiCOali, Integer quoziente, int numProsp,
			String circ, Integer numSeggi) throws DocumentException {

		document.newPage();

		document.add(addParagraph("PROSPETTO " + numProsp, 15));
		if (circ != null) {
			document.add(addParagraph("CIRCOSCRIZIONE = " + circ, 15));
		}
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = " + String.valueOf(sumVotiCOali), 15));
		document.add(addParagraph("NUM SEGGI = " + String.valueOf(numSeggi), 15));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = " + String.valueOf(quoziente), 15));

		if (elements.stream().map(Elemento::getSorteggio).filter(w -> w).findAny().isPresent()) {
			document.add(addParagraph("BLOCCO NAZIONALE", 12));
		}

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 40, 30, 10, 10, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader2(table);

		elements.forEach(e -> {
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
		table.addCell(
				new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());

		Paragraph p = new Paragraph();
		p.add(table);

		document.add(p);
		
		log.info("generato PROSPETTO "+numProsp);
	}

	protected void generaProspetto3(List<Elemento> elements, Integer numSeggiDaAssegnare, Integer totVoti,
			Integer quoziente, String coalizione) throws DocumentException {
		
		document.newPage();

		document.add(addParagraph("PROSPETTO 3", 15));

		document.add(addParagraph("COALIZIONE = " + coalizione, 15));
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = " + String.valueOf(totVoti), 15));
		document.add(addParagraph("NUM SEGGI = " + String.valueOf(numSeggiDaAssegnare), 15));
		document.add(addParagraph("QUOZIENETE ELETTORALE NAZ = " + String.valueOf(quoziente), 15));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 40, 30, 10, 10, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader2(table);

		elements.forEach(e -> {
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
		table.addCell(
				new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());

		Paragraph p = new Paragraph();
		p.add(table);

		if (elements.stream().map(Elemento::getSorteggio).distinct().findFirst().orElse(false)) {
			Paragraph pp = new Paragraph();
			Chunk chunk = new Chunk("Alcuni seggi non sono stati assegnati.",
					FontFactory.getFont(FontFactory.COURIER, 15, Font.BOLD, BaseColor.RED));
			pp.add(chunk);
			document.add(pp);
		}

		document.add(p);

		log.info("generato PROSPETTO 3");
	}

	protected void generaProspetto5_10(String descCirc, List<Elemento> elements, Integer quoziente, Integer numSeggi,
			Integer numVoti, int prosp) throws DocumentException {
		document.newPage();

		document.add(addParagraph("PROSPETTO " + prosp, 15));

		document.add(addParagraph("CIRCOSCRIZIONE = " + descCirc, 15));
		document.add(addParagraph("TOTALE VOTI VALIDI LISTE COALI = " + String.valueOf(numVoti), 15));
		document.add(addParagraph("NUM SEGGI = " + String.valueOf(numSeggi), 10));
		int sum = elements.stream().mapToInt(l -> l.getSeggiDecimali() + l.getSeggiQI()).sum();
		Paragraph seggi = new Paragraph();
		Chunk chunk1 = new Chunk("SEGGI ASSEGNATI = " + sum,
				FontFactory.getFont(FontFactory.COURIER, 15, Font.BOLD, BaseColor.RED));
		seggi.add(chunk1);
		document.add(seggi);
		document.add(addParagraph("QUOZIENETE ELETTORALE CIRC = " + String.valueOf(quoziente), 10));
		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 10));

		float[] width = { 40, 20, 20, 10, 10, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader5(table);

		elements.forEach(e -> {
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
		table.addCell(
				new PdfPCell(addParagraph(String.valueOf(elements.stream().mapToInt(Elemento::getSeggiQI).sum()), 10)));
		table.addCell(new PdfPCell());
		table.addCell(new PdfPCell());

		Paragraph p = new Paragraph();
		p.add(table);

		document.add(p);

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 10));

		if (elements.stream().map(Elemento::getSorteggio).distinct().findFirst().orElse(false)) {
			Paragraph pp = new Paragraph();
			Chunk chunk = new Chunk("Alcuni seggi non sono stati assegnati.",
					FontFactory.getFont(FontFactory.COURIER, 15, Font.BOLD, BaseColor.RED));
			pp.add(chunk);
			document.add(pp);
		}

		log.info("generato PROSPETTO "+prosp);
	}

	protected void generaProspetto6_11(List<Confronto> listProspetto6, int prosp) throws DocumentException {

		document.newPage();

		document.add(addParagraph("PROSPETTO "+prosp, 15));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 30, 20, 20, 20, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader6(table);

		listProspetto6.forEach(e -> {
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

		log.info("generato PROSPETTO "+prosp);
	}

	protected void generaProspetto7_12(List<Elemento> lista, int i) throws DocumentException {

		document.newPage();

		String descLista = lista.stream().map(Elemento::getDescrizione).distinct().findFirst().orElseThrow(() -> new RuntimeException( "Descrizione lista NULL in genera prospetto 7" ));
		document.add(addParagraph("PROSPETTO "+i, 15));
		document.add(addParagraph(descLista, 13));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 10, 30, 20, 20, 10, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader7(table);

		lista.forEach(e -> {
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
			cell5.addElement(addParagraph(
					String.valueOf(Objects.isNull(e.getOrdineSottrazione()) ? "" : e.getOrdineSottrazione()), 10));

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

		log.info("generato PROSPETTO "+i);
	}

	protected void generaProspetto8_13(List<Elemento> lista, int i) throws DocumentException {

		lista.sort(compareByDecimale(Ordinamento.ASC));
		document.newPage();

		document.add(addParagraph("PROSPETTO "+i, 15));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 10, 25, 10, 10, 10, 25, 10 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		addTableHeader8(table);

		lista.forEach(e -> {
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
			cell6.addElement(addParagraph(String.valueOf(Objects.isNull(e.getOrdineSottrazione()) ? ""
					: e.getOrdineSottrazione() + (e.isShift() ? " (*)" : "")), 10));

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

		log.info("generato PROSPETTO "+i);
	}

	protected void generaProspetto9_14(List<Prospetto9> lista, int i) throws DocumentException {

		document.newPage();

		document.add(addParagraph("PROSPETTO "+i, 15));

		document.add(Chunk.NEWLINE);
		document.add(addParagraph("", 15));

		float[] width = { 20, 15, 14, 20, 15, 14, 2 };

		PdfPTable table = new PdfPTable(width);

		table.setWidthPercentage(100);

		Arrays.asList("Eccedntaria", "Circoscrizione", "Decimali", "Deficitaria", "Circoscrizione", "Decimali", "Shift")
				.forEach(columnTitle -> {
					PdfPCell header = new PdfPCell();
					header.setBackgroundColor(BaseColor.LIGHT_GRAY);
					header.setBorderWidth(2);
					header.setPhrase(new Phrase(columnTitle));
					table.addCell(header);
				});

		lista.forEach(e -> {
			PdfPCell cell = new PdfPCell();
			cell.addElement(addParagraph(String.valueOf(e.getEccedntaria().getDescrizione()), 10));
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(addParagraph(String.valueOf(e.getEccedntaria().getTerritorio().getDescrizione()), 10));
			PdfPCell cell12 = new PdfPCell();
			cell12.addElement(addParagraph(String.valueOf(e.getEccedntaria().getQuoziente().getDecimale()), 10));
			PdfPCell cell3 = new PdfPCell();
			cell3.addElement(addParagraph(String.valueOf(e.getDeficitaria().getDescrizione()), 10));
			PdfPCell cell4 = new PdfPCell();
			cell4.addElement(addParagraph(String.valueOf(e.getDeficitaria().getTerritorio().getDescrizione()), 10));
			PdfPCell cell5 = new PdfPCell();
			cell5.addElement(addParagraph(String.valueOf(e.getDeficitaria().getQuoziente().getDecimale()), 10));
			PdfPCell cell6 = new PdfPCell();
			cell6.addElement(addParagraph(String.valueOf(e.getDeficitaria().isShift() ? "*" : ""), 10));

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

		log.info("generato PROSPETTO "+i);
	}

	protected Paragraph addParagraph(String value, int fontSize) {
		value = Objects.isNull(value) ? "" : value;
		Paragraph p = new Paragraph();
		Chunk chunk = new Chunk(value, FontFactory.getFont(FontFactory.COURIER, fontSize, Font.BOLD));
		p.add(chunk);
		return p;
	}

	@SuppressWarnings("unused")
	protected Phrase getTablePhrase(String value) {
		value = Objects.isNull(value) ? "" : value;
		return new Phrase(new Chunk(value, FontFactory.getFont(FontFactory.COURIER, 8, BaseColor.BLACK)));
	}

	protected void addTableHeader(PdfPTable table) {
		Stream.of(Header.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

	protected void addTableHeader2(PdfPTable table) {
		Stream.of(Prospetto2.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

	protected void addTableHeader5(PdfPTable table) {
		Stream.of(Prospetto5.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

	protected void addTableHeader6(PdfPTable table) {
		Stream.of(com.elettorale.riparto.constants.Prospetto6.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

	protected void addTableHeader7(PdfPTable table) {
		Stream.of(Prospetto7.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

	protected void addTableHeader8(PdfPTable table) {
		Stream.of(Prospetto8.values()).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle.getValue()));
			table.addCell(header);
		});
	}

}
