package com.neotys.draft.harfileconverter;

import de.sstoehr.harreader.model.HarEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

public class FileWriterHar {

	public static void writeHarEntriesToFile(Stream<HarEntry> entries, String outputPath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
			entries.forEach(entry -> writeEntryToFile(entry, writer));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeEntryToFile(HarEntry entry, BufferedWriter writer) {
		try {
			if (entry.getResponse() != null) {
				writer.write("Endpoint: " + extractShortenedPath(entry.getRequest().getUrl()) + "\n");
				writer.write("Status: " + entry.getResponse().getHttpVersion() + " " + entry.getResponse().getStatus() + "\n");
				if (entry.getResponse().getHeaders() != null) {
					writer.write("Headers:\n");
					entry.getResponse().getHeaders().forEach(header -> {
						try {
							writer.write(header.getName() + ": " + header.getValue() + "\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				} else {
					writer.write("Headers: No headers found\n");
				}

				writer.write("\n");

				if (entry.getResponse().getContent() != null) {
					writer.write(entry.getResponse().getContent().getText() + "\n");
				} else {
					writer.write("No response body found\n");
				}
			} else {
				writer.write("Response: No response found\n");
			}

			writer.write("------------------------------------------------------------\n");
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String extractShortenedPath(String url) {
		// Implementation of extractShortenedPath method
		return url; // Dummy implementation, replace with your logic
	}
}
