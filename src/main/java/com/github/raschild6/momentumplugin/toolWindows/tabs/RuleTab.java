package com.github.raschild6.momentumplugin.toolWindows.tabs;

import com.github.raschild6.momentumplugin.managers.LogManager;
import com.github.raschild6.momentumplugin.managers.RuleManager;
import com.github.raschild6.momentumplugin.managers.SonarManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RuleTab extends JPanel {
    private final RuleManager ruleManager;
    private final LogManager logManager;
    private final SonarManager sonarManager;

    public static JProgressBar progressBar = new JProgressBar();

    private DefaultTableModel tableModel;

    public RuleTab(RuleManager ruleManager, LogManager logManager, SonarManager sonarManager) {
        this.ruleManager = ruleManager;
        this.logManager = logManager;
        this.sonarManager = sonarManager;

        final Map<String, String> loadedProfiles = new HashMap<>();

        this.setLayout(new BorderLayout());

        // Colonne della tabella basate sulla struttura della classe SummaryRule
        String[] columnNames = {"Key", "Name", "Severity", "Status", "Type", "Template"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Creazione della tabella con il nuovo modello
        JTable rulesTable = new JTable(tableModel);

        // Configurazione delle dimensioni preferite per ogni colonna
        rulesTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Key
        rulesTable.getColumnModel().getColumn(1).setPreferredWidth(700); // Name
        rulesTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Severity
        rulesTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        rulesTable.getColumnModel().getColumn(4).setPreferredWidth(50); // Type
        rulesTable.getColumnModel().getColumn(5).setPreferredWidth(50);  // Template

        rulesTable.removeColumn(rulesTable.getColumnModel().getColumn(4)); // Remove Type
        rulesTable.removeColumn(rulesTable.getColumnModel().getColumn(4)); // Remove Template

        // Top panel con selectbox per i profili
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Label "Select Profile:"
        JLabel profileLabel = new JLabel("Select Profile: ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = JBUI.insetsRight(10); // Spaziatura a destra della label
        gbc.anchor = GridBagConstraints.WEST; // Allineamento a sinistra
        topPanel.add(profileLabel, gbc);

        // SelectBox (largo)
        JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox = new JComboBox<>();
        profileComboBox.addActionListener(new ProfileSelectionHandler());
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Espande orizzontalmente il selectbox
        gbc.fill = GridBagConstraints.HORIZONTAL; // Riempie lo spazio disponibile
        topPanel.add(profileComboBox, gbc);

        // Bottone "Refresh Profiles" (dimensione fissa)
        JButton updateProfilesButton = new JButton("Refresh Profiles");
        gbc.gridx = 2;
        gbc.weightx = 0; // Nessuna espansione per il bottone
        gbc.fill = GridBagConstraints.NONE; // Dimensione naturale del bottone
        gbc.insets = JBUI.insetsLeft(10); // Spaziatura a sinistra del bottone
        topPanel.add(updateProfilesButton, gbc);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        gbc.gridx = 3;
        gbc.weightx = 0; // Nessuna espansione per il bottone
        gbc.fill = GridBagConstraints.NONE; // Dimensione naturale del bottone
        gbc.insets = JBUI.insetsLeft(10); // Spaziatura a sinistra del bottone
        topPanel.add(progressBar, gbc);

        // Configura il pannello
        topPanel.setBorder(JBUI.Borders.empty(10));
        this.add(topPanel, BorderLayout.NORTH);

        // Barra di scorrimento per la tabella
        JScrollPane scrollPane = new JScrollPane(rulesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        // Pulsante di import in basso
        JPanel newRuleButtons = new JPanel();
        newRuleButtons.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton newRuleButton = new JButton("Chat-GPT New Rule");
        newRuleButton.addActionListener(e -> this.ruleManager.createNewRule());
        newRuleButtons.add(newRuleButton);

        Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        JButton openTemplateButton = new JButton(folderIcon);
        openTemplateButton.setToolTipText("Open Templates Folder");  // Optional: Tooltip text
        openTemplateButton.addActionListener(e -> this.ruleManager.openTemplatesFolder());
        newRuleButtons.add(openTemplateButton);

        JButton generateTemplateButton = new JButton("+");
        generateTemplateButton.setFont(new Font("Arial", Font.PLAIN, 24));
        generateTemplateButton.setToolTipText("Generate New Template");  // Optional: Tooltip text
        generateTemplateButton.addActionListener(e -> this.ruleManager.generateTemplateButton());
        newRuleButtons.add(generateTemplateButton);

        JButton loadRuleButton = new JButton("Load Rule on Profile");
        loadRuleButton.addActionListener(e -> this.ruleManager.loadRuleOnTemplate());
        newRuleButtons.add(loadRuleButton);

        JButton loadTemplateButton = new JButton("Load Template on Profile");
        loadTemplateButton.addActionListener(e -> this.ruleManager.loadTemplateOnProfile());
        newRuleButtons.add(loadTemplateButton);

        this.add(newRuleButtons, BorderLayout.SOUTH);

        updateProfilesButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            loadedProfiles.clear();
            loadedProfiles.putAll(this.ruleManager.loadProfiles(this.sonarManager, profileComboBox));
        });

        // Caricamento iniziale dei profili
        loadedProfiles.clear();
        loadedProfiles.putAll(this.ruleManager.loadProfiles(this.sonarManager, profileComboBox));

        // Caricamento iniziale delle regole se c'è un profilo selezionato
        this.ruleManager.loadRules(this.sonarManager, tableModel,
            this.ruleManager.getSelectedProfileKey(profileComboBox, loadedProfiles, true));
    }

    private class ProfileSelectionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox<AbstractMap.SimpleEntry<String, String>> source =
                (JComboBox<AbstractMap.SimpleEntry<String, String>>) e.getSource();

            // Differisci l'elaborazione per attendere l'aggiornamento dello stato della JComboBox
            SwingUtilities.invokeLater(() -> {
                // Ora l'elemento selezionato è aggiornato
                AbstractMap.SimpleEntry<String, String> selectedEntry =
                    (AbstractMap.SimpleEntry<String, String>) source.getSelectedItem();

                // Ottieni la chiave del profilo selezionato (o null se nessuno è selezionato)
                String selectedProfileKey = (selectedEntry != null) ? selectedEntry.getKey() : null;

                // Se esiste un profilo selezionato, carica le regole
                if (selectedProfileKey != null) {
                    ruleManager.loadRules(sonarManager, tableModel, selectedProfileKey);
                    logManager.log("Loaded rules for profile: " + selectedEntry.getKey() + "=" + selectedEntry.getValue());
                }
            });
        }
    }

    public static void startLoading() {
        progressBar.setVisible(true);
    }

    public static void stopLoading() {
        progressBar.setVisible(false);
    }

}
