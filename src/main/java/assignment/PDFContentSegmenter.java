package assignment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class PDFContentSegmenter {
    public static void main(String[] args) {
        String pdfPath = "C:\\Users\\dell\\Downloads\\sample pdf.pdf";
        List<String> segments = new ArrayList<>();
try{
     PDDocument document = PDDocument.load(new File(pdfPath));
     PDFTextStripper pdfStripper = new PDFTextStripper();
     String text = pdfStripper.getText(document); 
     String[] lines = text.split("\n");
     int cutsRequired = 4; 
    analyzeWhitespace(lines, segments, cutsRequired);

for (int i = 0; i < segments.size(); i++) {
                createSegmentPDF(segments.get(i), "output/segment_" + (i + 1) + ".pdf");
            }

            document.close();
        } catch (IOException e) {
            System.err.println("Error while processing the PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void analyzeWhitespace(String[] lines, List<String> segments, int cutsRequired) {
        StringBuilder currentSegment = new StringBuilder();
        List<Integer> whitespaceGaps = new ArrayList<>();
        int significantWhitespaceThreshold = 2; 
        int lastLine = -1;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) {
                int gapSize = 1; 
                while (i + 1 < lines.length && lines[i + 1].trim().isEmpty()) {
                    gapSize++;
                    i++;
                }
if (gapSize >= significantWhitespaceThreshold) {
                    whitespaceGaps.add(lastLine);
                    if (currentSegment.length() > 0) {
                        segments.add(currentSegment.toString().trim());
                        currentSegment.setLength(0);
                    }
                }
            } else {
            currentSegment.append(lines[i]).append("\n");
       lastLine = i;
            }
        }
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString().trim());
        }

     
        if (whitespaceGaps.size() > cutsRequired) {
            for (int i = 0; i < cutsRequired; i++) {
                int cutIndex = whitespaceGaps.get(i);
                if (cutIndex >= 0 && cutIndex < segments.size()) {
                    String segment = segments.get(cutIndex);
                    segments.remove(cutIndex);
                    segments.add(segment);
                }
            }
        }
    }



    private static void createSegmentPDF(String content, String outputPath) {
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs(); // Create output directory if it doesn't exist
            }

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    String sanitizedContent = content.replaceAll("\\p{Cntrl}", "")
                                                     .replace("\u2212", "-")  
                                                     .replaceAll("[^\\p{Print}]", "?");

                    for (String line : sanitizedContent.split("\n")) {
                        if (!line.trim().isEmpty()) {
                            contentStream.showText(line.trim());
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }

                    contentStream.endText(); 
                }

                document.save(outputPath);
                System.out.println("PDF created successfully at " + outputPath);
            }
        } catch (IOException e) {
            System.err.println("Error while creating the PDF segment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}


