package com.github.raschild6.momentumplugin.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

public class LogManager {
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);

    private JTextArea logTextArea;

    public LogManager() {
    }

    public JPanel initLogTab() {
        // Creazione di una finestra/tab per i log
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));

        // Creazione della JTextArea e la rendiamo non editabile
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);  // Non permettere all'utente di modificare il testo

        // Inserimento della JTextArea in un JScrollPane
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Aggiungere il JScrollPane al centro del logPanel
        logPanel.add(scrollPane, BorderLayout.CENTER);

        // Log iniziale
        log("** Plugin inizializzato **");

        return logPanel;
    }

    public void log(String message) {
        logger.info(message);
        logTextArea.append("\t" + message + "\n");
    }
}
