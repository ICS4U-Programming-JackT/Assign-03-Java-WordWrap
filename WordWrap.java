import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class WordWrap {

    /** Line break constant. */
    private static final String NEWLINE = "\n";

    /** Separator used to pass line + index. */
    private static final String DELIMITER = "::";

    /** Private constructor. */
    private WordWrap() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Public entry point for word wrapping.
     * @param inputString the input sentence
     * @param numChar maximum characters per line
     * @return wrapped string
     */
    public static String wrapWords(final String inputString, final int numChar) {
        if (inputString == null || inputString.isEmpty()) {
            return "";
        }
        String[] allWords = inputString.split(" ");
        List<String> wordsList = new ArrayList<>(Arrays.asList(allWords));

        return wordWrapRecursive(wordsList, 0, numChar);
    }

    /**
     * Recursively builds the wrapped text.
     * @param words The list of words
     * @param index current position
     * @param limit max width
     * @return wrapped output
     */
    private static String wordWrapRecursive(
        final List<String> words,
        final int index,
        final int limit) {

        // Builds one wrapped line
        // splits line from next index
        // keeps recursion going until done.
        if (index >= words.size()) {
            return "";
        }

        String resultString = buildLine(words, index, limit, "", 0);
        String[] parts = resultString.split(DELIMITER);
        String line = parts[0];
        int nextIndex = Integer.parseInt(parts[1]);

        return line + NEWLINE + wordWrapRecursive(words, nextIndex, limit);
    }

    /**
     * Recursively constructs one wrapped line.
     * @param words list of words
     * @param index current word index
     * @param limit max width
     * @param line current built text
     * @param counter character count
     * @return "line::nextIndex"
     */
    private static String buildLine(
        final List<String> words,
        final int index,
        final int limit,
        final String line,
        final int counter) {

        // Handles long words and normal wrap
        // checks if next word fits
        // returns line and next index.
        if (index >= words.size()) {
            return line + DELIMITER + index;
        }

        String word = words.get(index);
        int wordLen = word.length();

        if (wordLen > limit) {
            if (counter == 0) {
                String part = word.substring(0, limit);
                String rest = word.substring(limit);
                words.set(index, rest);
                return part + DELIMITER + index;
            }
            return line + DELIMITER + index;
        }

        if (counter == 0) {
            return buildLine(
                words, index + 1, limit, word, wordLen);
        }

        if (counter + 1 + wordLen <= limit) {
            String newLine = line + " " + word;
            int newCounter = counter + 1 + wordLen;
            return buildLine(
                words, index + 1, limit, newLine, newCounter);
        }

        return line + DELIMITER + index;
    }

    /**
     * Reads input file formatted as alternating String and limit.
     * @param inputFileName input file path
     * @return list of [text, limitAsString]
     */
    public static ArrayList<String[]> readInputFile(
        final String inputFileName) {

        // Reads text + limit pairs
        // trims whitespace manually
        // stores pairs for later use.
        ArrayList<String[]> data = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(new File(inputFileName))) {
            // Read each line
            while (scanner.hasNextLine()) {
                String text = scanner.nextLine();
                int textStart = 0;
                int textEnd = text.length();
                
                // Go through each line
                while (textStart < textEnd
                    && text.charAt(textStart) == ' ') {
                    textStart++;
                }
                while (textEnd > textStart
                    && text.charAt(textEnd - 1) == ' ') {
                    textEnd--;
                }
                text = text.substring(textStart, textEnd);

                if (scanner.hasNextLine()) {
                    String limitLine = scanner.nextLine();
                    int limitStart = 0;
                    int limitEnd = limitLine.length();

                    while (limitStart < limitEnd
                        && limitLine.charAt(limitStart) == ' ') {
                        limitStart++;
                    }
                    while (limitEnd > limitStart
                        && limitLine.charAt(limitEnd - 1) == ' ') {
                        limitEnd--;
                    }
                    limitLine = limitLine.substring(
                        limitStart, limitEnd);

                    if (!text.isEmpty()) {
                        data.add(new String[] {
                            text, limitLine
                        });
                    }
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(
                "Input file not found: " + inputFileName);
        }
        return data;
    }

    /**
     * Writes wrapped output to a file.
     * @param outputFileName output file path
     * @param wrappedBlocks list of wrapped strings
     */
    public static void writeOutputFile(
        final String outputFileName,
        final ArrayList<String> wrappedBlocks) {

        // Writes each wrapped block
        // removes trailing newline
        // separates blocks with blank line.
        try (FileWriter writer =
            new FileWriter(outputFileName)) {

            for (int i = 0; i < wrappedBlocks.size(); i++) {
                String block = wrappedBlocks.get(i);

                if (block.endsWith(NEWLINE)) {
                    block = block.substring(
                        0,
                        block.length() - NEWLINE.length());
                }

                writer.write(block);

                if (i < wrappedBlocks.size() - 1) {
                    writer.write(NEWLINE + NEWLINE);
                }
            }
        } catch (IOException e) {
            System.err.println(
                "Error writing to file: " + outputFileName);
        }
    }

    /**
     * Main entry point.
     * @param args not used
     */
    public static void main(final String[] args) {
        // I/O files
        String inputFile = "input.txt";
        String outputFile = "output.txt";

        // Initialize input and wrapped data
        ArrayList<String[]> inputData =
            readInputFile(inputFile);
        ArrayList<String> wrappedLines =
            new ArrayList<>();

        for (String[] pair : inputData) {
            String text = pair[0];
            int limit;

            try {
                limit = Integer.parseInt(pair[1]);
                wrappedLines.add(wrapWords(text, limit));
            } catch (NumberFormatException e) {
                System.err.println(
                    "Invalid limit format: "
                    + pair[1]
                    + ". Skipping item.");
            }
        }

        writeOutputFile(outputFile, wrappedLines);
        System.out.println(
            "Word wrap complete. Output written to "
            + outputFile);
    }
}
