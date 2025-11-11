package com.shieldai.dashboard.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import com.opencsv.CSVWriter;
import com.shieldai.shared.DetectionReport;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    public String generateCSV(List<DetectionReport> detections) {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // Write headers
            String[] headers = {"Tool Name", "Detected Time", "Process Details", "Confidence", "OS Info", "Tool Type"};
            csvWriter.writeNext(headers);

            // Write data
            for (DetectionReport detection : detections) {
                String[] row = {
                    detection.getToolName(),
                    detection.getTimestamp(),
                    detection.getProcessDetails(),
                    String.format("%.1f%%", detection.getConfidence() * 100),
                    detection.getOsInfo(),
                    detection.getToolType()
                };
                csvWriter.writeNext(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
        return stringWriter.toString();
    }

    public byte[] generatePDF(List<DetectionReport> detections) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("ShieldAI Detection Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Font dateFont = new Font(Font.HELVETICA, 10);
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Create table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 4, 1.5f, 2, 1.5f});

            // Add headers
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            String[] headers = {"Tool Name", "Detected Time", "Process Details", "Confidence", "OS Info", "Type"};
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(240, 240, 240));
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Add data
            Font dataFont = new Font(Font.HELVETICA, 8);
            for (DetectionReport detection : detections) {
                table.addCell(new PdfPCell(new Phrase(detection.getToolName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(detection.getTimestamp(), dataFont)));
                
                String processDetails = detection.getProcessDetails();
                if (processDetails.length() > 50) {
                    processDetails = processDetails.substring(0, 50) + "...";
                }
                table.addCell(new PdfPCell(new Phrase(processDetails, dataFont)));
                
                table.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", detection.getConfidence() * 100), dataFont)));
                table.addCell(new PdfPCell(new Phrase(detection.getOsInfo(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(detection.getToolType(), dataFont)));
            }

            document.add(table);
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
        
        return baos.toByteArray();
    }
}