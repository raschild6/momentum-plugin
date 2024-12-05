package com.github.raschild6.momentumplugin.toolWindows.tabs.details;

import org.sonar.api.server.rule.RulesDefinition.Rule;

import javax.swing.*;
import java.awt.*;

public class RuleDetailDialog extends JDialog {

    private Rule rule;
    private JTextArea descriptionTextArea;
    private JTextField severityField;

    public RuleDetailDialog(Frame parent, Rule rule) {
        super(parent, "Rule Details", true);
        this.rule = rule;

        setLayout(new BorderLayout());

        // Descrizione
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BorderLayout());
        descriptionPanel.add(new JLabel("Description:"), BorderLayout.NORTH);

        descriptionTextArea = new JTextArea(rule.htmlDescription() != null ? rule.htmlDescription() : "No description available.");
        descriptionTextArea.setEditable(true);
        descriptionPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);

        // Severità e altri dettagli
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(0, 2));  // Due colonne per "label" e "input"

        detailsPanel.add(new JLabel("Severity:"));
//        severityField = new JTextField(rule.defaultImpacts() != null ? rule.defaultImpacts() : "Normal");
        detailsPanel.add(severityField);

        // Bottone per salvare le modifiche
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveChanges());

        // Aggiungi al dialogo
        add(descriptionPanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        add(saveButton, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    private void saveChanges() {
        // Esegui le modifiche (ad esempio, aggiorna la descrizione, la severità, ecc.)

        // Chiudi la finestra
        dispose();
    }
}
