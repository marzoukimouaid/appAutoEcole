package Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class PDFGenerator {

    private static java.util.List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String word : words) {
            String potentialLine = (line.length() == 0) ? word : line + " " + word;
            float textWidth = font.getStringWidth(potentialLine) / 1000 * fontSize;
            if (textWidth > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                }
                line.append(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    
    public static void generatePDF(String header, String title, String candidateDetails, String content, String footer, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();

            float margin = 50;
            float yStart = mediaBox.getHeight() - margin;
            float width = mediaBox.getWidth() - 2 * margin;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, yStart);
                for (String line : header.split("\n")) {
                    cs.showText(line);
                    cs.newLineAtOffset(0, -14);
                }
                cs.endText();


                float headerHeight = 14 * header.split("\n").length;
                float yAfterHeader = yStart - headerHeight - 10;
                cs.setStrokingColor(Color.LIGHT_GRAY);
                cs.setLineWidth(1);
                cs.moveTo(margin, yAfterHeader);
                cs.lineTo(margin + width, yAfterHeader);
                cs.stroke();


                float yPosition = yAfterHeader - 30;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 20;
                float titleX = margin + (width - titleWidth) / 2;
                cs.newLineAtOffset(titleX, yPosition);
                cs.showText(title);
                cs.endText();


                yPosition -= 30;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, yPosition);
                String invoiceDetails = candidateDetails + "    Date: " + java.time.LocalDate.now();
                cs.showText(invoiceDetails);
                cs.endText();


                yPosition -= 20;
                cs.setStrokingColor(Color.LIGHT_GRAY);
                cs.setLineWidth(1);
                cs.moveTo(margin, yPosition);
                cs.lineTo(margin + width, yPosition);
                cs.stroke();


                yPosition -= 30;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, yPosition);
                float leading = 14f;
                java.util.List<String> wrappedLines = wrapText(content, PDType1Font.HELVETICA, 12, width);
                for (String line : wrappedLines) {
                    cs.showText(line);
                    cs.newLineAtOffset(0, -leading);
                }
                cs.endText();


                float footerY = margin + 40;
                cs.setStrokingColor(Color.LIGHT_GRAY);
                cs.setLineWidth(1);
                cs.moveTo(margin, footerY);
                cs.lineTo(margin + width, footerY);
                cs.stroke();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                float footerTextWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footer) / 1000 * 10;
                float footerX = margin + (width - footerTextWidth) / 2;
                cs.newLineAtOffset(footerX, margin);
                cs.showText(footer);
                cs.endText();
            }
            document.save(file);
        }
    }

    public static void generateInvoice(String header, String candidateDetails, String invoiceContent, String footer, File file) throws IOException {
        generatePDF(header, "Facture", candidateDetails, invoiceContent, footer, file);
    }

    public static void generateEmploiDuTemps(String header, String candidateDetails, String scheduleContent, String footer, File file) throws IOException {
        generatePDF(header, "Emploi du Temps", candidateDetails, scheduleContent, footer, file);
    }




    public static void generateMonthlyCalendarAndAppointments(
            String header,
            String candidateName,
            YearMonth month,
            Map<LocalDate, List<?>> eventsByDay,
            List<?> sortedEvents,
            String footer,
            File file
    ) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();

            float margin = 40;
            float width = mediaBox.getWidth() - 2 * margin;
            float startY = mediaBox.getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                drawHeader(cs, header, margin, startY, width);


                float yPos = startY - 70;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(month.toString()) / 1000 * 16;
                float titleX = margin + (width - titleWidth) / 2;
                cs.newLineAtOffset(titleX, yPos);
                String monthTitle = month.getMonth().name() + " " + month.getYear();
                cs.showText(capitalize(monthTitle));
                cs.endText();


                yPos -= 20;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, yPos);
                cs.showText(candidateName);
                cs.endText();


                yPos -= 40;
                float tableHeight = 220;
                drawCalendarTable(cs, month, eventsByDay, margin, yPos, width, tableHeight);


                float afterTableY = yPos - tableHeight - 20;
                drawAppointmentsList(cs, sortedEvents, margin, afterTableY, width);


                drawFooter(cs, footer, margin, 60, width, mediaBox);
            }
            document.save(file);
        }
    }

    private static void drawHeader(PDPageContentStream cs, String header, float margin, float startY, float width) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(margin, startY);
        for (String line : header.split("\n")) {
            cs.showText(line);
            cs.newLineAtOffset(0, -14);
        }
        cs.endText();

        float headerHeight = 14 * header.split("\n").length;
        float yAfterHeader = startY - headerHeight - 5;
        cs.setStrokingColor(Color.LIGHT_GRAY);
        cs.setLineWidth(1);
        cs.moveTo(margin, yAfterHeader);
        cs.lineTo(margin + width, yAfterHeader);
        cs.stroke();
    }

    private static void drawCalendarTable(PDPageContentStream cs,
                                          YearMonth month,
                                          Map<LocalDate, List<?>> eventsByDay,
                                          float startX,
                                          float startY,
                                          float tableWidth,
                                          float tableHeight) throws IOException {

        float cellWidth = tableWidth / 7;
        float rowHeight = tableHeight / 7;
        float currentY = startY;


        String[] dayNames = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.75f);


        cs.moveTo(startX, currentY);
        cs.lineTo(startX + tableWidth, currentY);
        cs.stroke();

        float nextY = currentY - rowHeight;


        for (int col = 0; col < 7; col++) {
            float cellX = startX + col * cellWidth;

            cs.moveTo(cellX, currentY);
            cs.lineTo(cellX, nextY);
            cs.stroke();


            float textX = cellX + 5;
            float textY = currentY - 15;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(textX, textY);
            cs.showText(dayNames[col]);
            cs.endText();
        }

        cs.moveTo(startX + 7 * cellWidth, currentY);
        cs.lineTo(startX + 7 * cellWidth, nextY);
        cs.stroke();


        cs.moveTo(startX, nextY);
        cs.lineTo(startX + tableWidth, nextY);
        cs.stroke();

        currentY = nextY;


        LocalDate firstOfMonth = LocalDate.of(month.getYear(), month.getMonth(), 1);
        int lengthOfMonth = month.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int colIndex = startDayOfWeek - 1;
        int rowIndex = 0;

        for (int day = 1; day <= lengthOfMonth; day++) {
            float cellTopY = startY - rowHeight * (rowIndex + 1);
            float cellLeftX = startX + colIndex * cellWidth;


            float dayTextX = cellLeftX + 3;
            float dayTextY = cellTopY - 12;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
            cs.newLineAtOffset(dayTextX, dayTextY);
            cs.showText(String.valueOf(day));
            cs.endText();


            LocalDate theDay = firstOfMonth.withDayOfMonth(day);
            if (eventsByDay.containsKey(theDay)) {
                List<?> evList = eventsByDay.get(theDay);
                float eventTextY = dayTextY - 10;
                for (Object evObj : evList) {
                    String shortLabel = evObj.toString();
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 8);
                    cs.newLineAtOffset(dayTextX, eventTextY);
                    cs.showText(shortLabel);
                    cs.endText();
                    eventTextY -= 9;
                    if (eventTextY < cellTopY - rowHeight + 10) {
                        break;
                    }
                }
            }

            colIndex++;
            if (colIndex == 7) {

                colIndex = 0;
                rowIndex++;


                float rowY = startY - rowHeight * (rowIndex + 1);
                cs.moveTo(startX, rowY);
                cs.lineTo(startX + tableWidth, rowY);
                cs.stroke();


                for (int c = 0; c <= 7; c++) {
                    float lineX = startX + c * cellWidth;
                    cs.moveTo(lineX, rowY);
                    cs.lineTo(lineX, rowY + rowHeight);
                    cs.stroke();
                }
            }
        }


        for (int r = rowIndex; r < 7; r++) {
            float rowY = startY - rowHeight * (r + 1);

            cs.moveTo(startX, rowY);
            cs.lineTo(startX + tableWidth, rowY);
            cs.stroke();


            for (int c = 0; c <= 7; c++) {
                float lineX = startX + c * cellWidth;
                cs.moveTo(lineX, rowY);
                cs.lineTo(lineX, rowY + rowHeight);
                cs.stroke();
            }
        }
    }

    private static void drawAppointmentsList(PDPageContentStream cs,
                                             List<?> sortedEvents,
                                             float marginX,
                                             float startY,
                                             float width) throws IOException {
        float yPos = startY;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(marginX, yPos);
        cs.showText("Appointments:");
        cs.endText();

        yPos -= 20;

        if (sortedEvents.isEmpty()) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(marginX, yPos);
            cs.showText("Aucun rendez-vous ce mois-ci.");
            cs.endText();
            return;
        }

        for (Object evObj : sortedEvents) {
            String lineText = evObj.toString();
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(marginX, yPos);
            cs.showText(lineText);
            cs.endText();

            yPos -= 14;
            if (yPos < 80) {
                break;
            }
        }
    }

    private static void drawFooter(PDPageContentStream cs,
                                   String footer,
                                   float margin,
                                   float footerHeight,
                                   float width,
                                   PDRectangle mediaBox) throws IOException {
        cs.setStrokingColor(Color.LIGHT_GRAY);
        cs.setLineWidth(1);
        cs.moveTo(margin, footerHeight + 5);
        cs.lineTo(margin + width, footerHeight + 5);
        cs.stroke();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
        float footerTextWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footer) / 1000 * 10;
        float footerX = margin + (width - footerTextWidth) / 2;
        cs.newLineAtOffset(footerX, footerHeight - 10);
        cs.showText(footer);
        cs.endText();
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
