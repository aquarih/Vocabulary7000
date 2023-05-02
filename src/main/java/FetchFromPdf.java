import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchFromPdf {

    public static void main(String[] args) throws IOException {
        try (PDDocument document = PDDocument.load(new File("E:\\words.pdf"))) {

            document.getClass();

            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);
                String[] lines = pdfFileInText.split("\\r?\\n");
                List<String> newLines = new ArrayList<>();
                Pattern pattern = Pattern.compile(".*\\b\\w+\\W+\\d+(?:\\([^)]*\\))?$");
                int indexStart = Arrays.asList(lines).indexOf("依字母排序 ");
                int indexEnd = Arrays.asList(lines).indexOf("附錄 ");
                for (int i = indexStart; i < indexEnd; i++) {
                    String testLine = lines[i].trim();
                    while(testLine.matches("^[A-Z]|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|^[\\u4E00-\\u9FFF]+|^[\\u4e00-\\u9FFF]+\\s\\s[A-Z]")){
                        testLine = lines[++i].trim();
                    }

                    Matcher matcher = pattern.matcher(testLine);
                    String nextLine;
                    String newLine;

                    while (!matcher.find()) {
                        if(i+1 < lines.length - 1) {
                            nextLine = lines[i + 1].trim();
                            if(testLine.endsWith("/")) {
                                newLine = testLine + nextLine;
                            } else {
                                newLine = testLine + " " + nextLine;
                            }
                            testLine = testLine.replace(testLine, newLine);
                            matcher = pattern.matcher(testLine);
                            i++;
                        } else {
                          break;
                        }
                    }

//                    System.out.println(testLine);
                    newLines.add(testLine);
                }

                testRegSep(newLines);
            }
        }
    }

    public static void testRegSep(List<String> lines) {
        Pattern pattern = Pattern.compile("^\\s*([^\s]+)(?:\\s+\\(([^)]+)\\))?\\s+(?:(\\S+)\\s+)?(\\d+)$");
        for(String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()) {
                String word;

                if(matcher.group(2) == null) {
                    word = matcher.group(1);
                } else {
                    word = matcher.group(1) + " (" + matcher.group(2) + ")";
                }
                String speech = matcher.group(3);
                int level = Integer.parseInt(matcher.group(4));

                System.out.println("Word: " + word);
                System.out.println("Speech: " + speech);
                System.out.println("Level: " + level);
            }
        }

    }
}
