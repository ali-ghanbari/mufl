package edu.utdallas.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class LegacyTextFile implements AutoCloseable {
    public static final int BUFFER_SIZE = 4096;

    private final BufferedReader br;
    private String remaining;

    public LegacyTextFile(String fileName) throws IOException {
        this(new File(fileName));
    }

    public LegacyTextFile(File file) throws IOException {
        this.br = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        this.remaining = "";
    }

    @Override
    public void close() throws Exception {
        this.br.close();
    }

    public String readLine() {
        String text = this.remaining;
        String result = null;
        try {
            String line = br.readLine();
            if (line == null) {
                if (text.isEmpty()) {
                    return null;
                }
                if (!text.startsWith("MutationDetails [") && !text.startsWith("[EXCEPTION] ")) {
                    throw new RuntimeException("malformed file");
                }
            }
            if (line != null) {
                text += line.trim();
            }
            if (!(text.startsWith("MutationDetails [") || text.startsWith("[EXCEPTION] "))) {
                throw new RuntimeException("malformed file");
            }
            while (text.indexOf("MutationDetails [", 1) < 0
                    && text.indexOf("[EXCEPTION] ", 1) < 0) {
                line = this.br.readLine();
                if (line != null) {
                    text += line.trim();
                } else {
                    break;
                }
            }
            int upper = text.indexOf("MutationDetails [", 1);
            if (upper < 0) {
                upper = text.indexOf("[EXCEPTION] ", 1);
            }
            if (upper < 0) {
                upper = text.length();
            }
            result = text.substring(0, upper);
            this.remaining = text.substring(upper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
