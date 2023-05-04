import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import pojo.Voc;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FetchFromPdf {

    public static void main(String[] args) {
        int start = 0, end = 0;
        List<Voc> vSelected;

        List<String> vOrigin = new ArrayList<>(fetchWordsFromPdf("E:\\words.pdf"));
        String unusedWords = "counter, cushion, definite, desperate, favorable, forecast, fragile";

        for (String word : vOrigin) {
            if (word.contains("convince")) {
                start = vOrigin.indexOf(word);
            } else if (word.contains("gear")) {
                end = vOrigin.indexOf(word);
            }
        }

        List<Voc> vRange = new ArrayList<>(linesSepByReg(vOrigin.subList(start, end)));
        int levelMax = vRange.get(0).getLevel();
        if (levelMax >= 3) {
            vRange.removeIf(vocInRange -> vocInRange.getLevel() < 3 || vocInRange.getLevel() > levelMax);
        }

        String[] results = new String[4];
        Map<String, String> speechOfWords = new LinkedHashMap<>();

        for (String usedWord : unusedWords.split(", ")) {
            String speech;

            Optional<String> speechOpt = vRange.stream()
                    .filter(obj -> usedWord.equals(obj.getWord()))
                    .map(Voc::getSpeech)
                    .findFirst();

            if (speechOpt.isPresent()) {
                if (speechOpt.get().contains("/")) {
                    String[] speeches = speechOpt.get().split("/");
                    Random random = new Random();
                    speech = speeches[random.nextInt(speeches.length)];
                } else {
                    speech = speechOpt.get();
                }

                speechOfWords.put(usedWord, speech);
                vRange.removeIf(v -> usedWord.equals(v.getWord()));
                System.out.println("The Word " + usedWord + " is found.");
                System.out.println(vRange.size());
            } else {
                System.out.println("The Word " + usedWord + " is not found.");
            }
        }

        deleteWordsFromDoc(vRange);

        System.out.println(vRange.size());
        for (Map.Entry<String, String> entry : speechOfWords.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        for (Map.Entry<String, String> entry : speechOfWords.entrySet()) {
            String speech = entry.getValue();
            vSelected = getSelectedList(vRange, speech);

            int j = (int) (Math.random() * (3 + 1));
            Set<Integer> usedIndexes = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                if (i == j) {
                    results[i] = entry.getKey();
                } else {
                    int nextIndex = getNextAvailableIndex(usedIndexes, vSelected.size());
                    results[i] = vSelected.get(nextIndex).getWord();
                    usedIndexes.add(nextIndex);
                }
            }

            System.out.println("(A) " + results[0] + " (B) " + results[1] + " (C) "+ results[2] + " (D) " + results[3] + " " + speech);
        }
    }

    public static List<Voc> getSelectedList(List<Voc> temp, String speech) {
        List<Voc> selected = temp.stream()
                .filter(v -> v.getSpeech().contains(speech))
                .collect(Collectors.toCollection(ArrayList::new));

        if (selected.size() < 3) {
            return selected;
        } else {
            List<Voc> result = new ArrayList<>();
            Random random = new Random();
            int resultCount = 0;

            while (resultCount < 3) {
                int randomIndex = random.nextInt(selected.size());
                Voc resultVoc = selected.get(randomIndex);
                if (resultVoc.getLevel() == 4 && selected.size() > 0) {
                    if (random.nextDouble() < 0.8) {
                        result.add(resultVoc);
                        resultCount++;
                        selected.remove(randomIndex);
                    }
                } else if (resultVoc.getLevel() == 3) {
                    if (random.nextDouble() < 0.2) {
                        result.add(resultVoc);
                        resultCount++;
                        selected.remove(randomIndex);
                    }
                }
            }

            return result;
        }
    }

    public static List<String> fetchWordsFromPdf(String pdfPath) {
        List<String> newLines = null;

        try (PDDocument document = PDDocument.load(new File("E:\\words.pdf"))) {

            document.getClass();

            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);
                String[] lines = pdfFileInText.split("\\r?\\n");
                newLines = new ArrayList<>();
                Pattern pattern = Pattern.compile(".*\\b\\w+\\W+\\d+(?:\\([^)]*\\))?$");
                int indexStart = Arrays.asList(lines).indexOf("依字母排序 ");
                int indexEnd = Arrays.asList(lines).indexOf("附錄 ");
                for (int i = indexStart; i < indexEnd; i++) {
                    String testLine = lines[i].trim();
                    while (testLine.matches("^[A-Z]|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|^[\\u4E00-\\u9FFF]+|^[\\u4e00-\\u9FFF]+\\s\\s[A-Z]")) {
                        testLine = lines[++i].trim();
                    }

                    Matcher matcher = pattern.matcher(testLine);
                    String nextLine;
                    String newLine;

                    while (!matcher.find()) {
                        if (i + 1 < lines.length - 1) {
                            nextLine = lines[i + 1].trim();
                            if (testLine.endsWith("/")) {
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
            }
        } catch (Exception e) {
            System.out.println("PDF取出資料過程出錯!");
        }

        return newLines;
    }

    public static List<String> fetchWordsFromDoc(String docPath) {
        List<String> existedWords;

        try {
            File file = new File(docPath);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            XWPFDocument document = new XWPFDocument(fis);
            // 獲取文檔內所有段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            existedWords = new ArrayList<>();

            String delimiter = "\\([ABCD]\\)(\s)?";

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text.startsWith("(A)")) {
                    String[] segments = text.split(delimiter);
                    for (String segment : segments) {
                        if (!segment.matches("^\\s*$|^[\\s　]*$") && !existedWords.contains(segment)) {
                            existedWords.add(segment.trim());
                        }
                    }
                }
            }

            for (String word : existedWords) {
                String original = getOriginalText(word);
                int i = existedWords.indexOf(word);
                existedWords.set(i, original);
            }

            fis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return existedWords;
    }

    public static void deleteWordsFromDoc(List<Voc> rangeWords/*, String path*/) {
        Set<String> vExisted = new HashSet<>(fetchWordsFromDoc("E:\\01.docx"));

        rangeWords.removeIf(word -> vExisted.contains(word.getWord()));

    }

    public static List<Voc> linesSepByReg(List<String> lines) {
//        String[] lines = {"contend v. 5","content n./adj. 4","content(ment) v./(n.) 4","criterion/criteria n. 5","you (your, yours, yourself, yourselves) pron. 1"};
        Pattern pattern = Pattern.compile("^\\s*([^\s]+)(?:\\s+\\(([^)]+)\\))?\\s+(?:(\\S+)\\s+)?(\\d+)$");
        List<Voc> vocList = new ArrayList<>();
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

                vocList.add(new Voc(word, speech, level));

//                System.out.println("Word: " + word);
//                System.out.println("Speech: " + speech);
//                System.out.println("Level: " + level);
            }
        }
        return vocList;
    }

    public static String getOriginalText(String text) {
//        List<String> wordList = new ArrayList<>();
        Properties properties = new Properties();
//        String result = "";
        String originalWord = "";

        properties.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

//        List<CoreMap> words = document.get(CoreAnnotations.SentencesAnnotation.class);
//        for (CoreMap wordTemp : words) {
        for (CoreLabel token : document.get(CoreAnnotations.TokensAnnotation.class)) {
            originalWord = token.get(CoreAnnotations.LemmaAnnotation.class);
//                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                result = originalWord + " / " + pos;
//                wordList.add(result);
        }
//        }

        return originalWord;
    }

    private static int getNextAvailableIndex(Set<Integer> usedIndexes, int size) {
        int nextIndex = (int) (Math.random() * size);
        while (usedIndexes.contains(nextIndex)) {
            nextIndex = (int) (Math.random() * size);
        }
        return nextIndex;
    }
}
