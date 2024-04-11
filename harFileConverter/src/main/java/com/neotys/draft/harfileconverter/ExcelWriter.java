package com.neotys.draft.harfileconverter;

import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ExcelWriter {

	private Workbook workbook;
	private Sheet sheet;
	private Row row;
	private Cell cell;

	public ExcelWriter(String fileName) {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("HAR Data");
	}

	public void writeData(Stream<HarEntry> streamHarEntries) {
		AtomicInteger rowNum = new AtomicInteger();
		streamHarEntries.forEach(entry -> {
			row = sheet.createRow(rowNum.getAndIncrement());
			writeTimestamp(String.valueOf(entry.getStartedDateTime().getTime()), 0);
			writeEndpoint(entry.getRequest().getUrl(), 1);
			writeResponseDetails(entry, 2);
		});
	}

	private void writeTimestamp(String url, int column) {
		cell = row.createCell(column);
		cell.setCellValue(extractShortenedPath(url));
	}

	private void writeEndpoint(String url, int column) {
		cell = row.createCell(column);
		cell.setCellValue(extractShortenedPath(url));
	}

	private void writeResponseDetails(HarEntry entry, int column) {
		cell = row.createCell(column);
		StringBuilder responseDetails = new StringBuilder();
		//System.out.println(entry.getStartedDateTime().toString());

		responseDetails.append(entry.getResponse().getHttpVersion()).append(".0 ").append(entry.getResponse().getStatus()).append("\n"); // @TODO : remove raw fix
		if (entry.getResponse().getHeaders() != null) {
			responseDetails.append("\n");
			Set<String> headersToRemove = new HashSet<>();
			headersToRemove.add("X-Firefox-Spdy: h2"); // @TODO : headers to remove here
			entry.getResponse().getHeaders().forEach(header -> {
				String headerString = header.getName() + ": " + header.getValue();
				if (!headersToRemove.contains(headerString)) {
					responseDetails.append(headerString).append("\n");
				}
			});
		} else {
			responseDetails.append("Headers: No headers found\n");
		}
		responseDetails.append("\n\n");
		if (entry.getResponse().getContent() != null) {
			responseDetails.append(entry.getResponse().getContent().getText());
		} else {
			responseDetails.append("No response body found");
		}
		cell.setCellValue(responseDetails.toString());
	}

	public void save(String fileName) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
			workbook.write(fileOut);
		}
	}

	public static String extractShortenedPath(String url) {
		int thirdSlashIndex = url.indexOf('/', url.indexOf('/', url.indexOf('/') + 1) + 1);
		return thirdSlashIndex == -1 || thirdSlashIndex == url.length() - 1 ? url : url.substring(thirdSlashIndex + 1);
	}
}
