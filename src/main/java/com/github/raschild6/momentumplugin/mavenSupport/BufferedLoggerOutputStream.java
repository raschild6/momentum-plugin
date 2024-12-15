package com.github.raschild6.momentumplugin.mavenSupport;

import com.github.raschild6.momentumplugin.managers.LogManager;

import java.io.OutputStream;
import java.io.IOException;

public class BufferedLoggerOutputStream extends OutputStream {

    private final LogManager logManager;
    private final StringBuilder buffer;
    private static final int BUFFER_SIZE = 8192;  // 8 KB buffer

    public BufferedLoggerOutputStream(LogManager logManager) {
        this.logManager = logManager;
        this.buffer = new StringBuilder(BUFFER_SIZE);
    }

    @Override
    public void write(int b) throws IOException {
        // Aggiungi il singolo byte al buffer
        char c = (char) b;
        buffer.append(c);

        // Se il buffer raggiunge la dimensione massima, scrivi su logManager
        if (buffer.length() >= BUFFER_SIZE) {
            flushBuffer();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // Aggiungi i byte al buffer
        String output = new String(b, off, len);
        buffer.append(output);

        // Se il buffer raggiunge la dimensione massima, scrivi su logManager
        if (buffer.length() >= BUFFER_SIZE) {
            flushBuffer();
        }
    }

    // Quando il buffer è pieno o la scrittura è finita, invia il contenuto al logManager
    private void flushBuffer() throws IOException {
        if (!buffer.isEmpty()) {
            logManager.log(buffer.toString());  // Log su LogManager senza causare ricorsione
            buffer.setLength(0);  // Pulisce il buffer
        }
    }

    // Aggiungi un metodo per forzare lo svuotamento del buffer quando finisce l'output
    public void flush() throws IOException {
        flushBuffer();  // Svuota il buffer rimanente
    }
}
