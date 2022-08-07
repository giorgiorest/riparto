package com.elettorale.riparto.stampa;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Stampa {

	public static  Document stampa() throws FileNotFoundException, DocumentException {
		Document document = new Document();
		
		PdfWriter.getInstance(document, new FileOutputStream("H:\\riparto.pdf"));

		document.open();
	
//		generaProspetto1(listaNazionali);
//		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
//		Chunk chunk = new Chunk("Hello World", font);
//		document.add(chunk);
		
		PdfPTable table = new PdfPTable(3);
		addTableHeader(table);
		addRows(table);
//		addCustomRows(table);

		document.add(table);
		document.close();

		return document;
	}

	private static void addRows(PdfPTable table) {
		table.addCell("row 1, col 1");
	    table.addCell("row 1, col 2");
	    table.addCell("row 1, col 3");
		
	}

	private static void addTableHeader(PdfPTable table) {
		
	}
	
}
