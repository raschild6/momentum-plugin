package com.github.raschild6.momentumplugin.toolWindows.tabs;

import com.github.raschild6.momentumplugin.managers.LogManager;
import com.github.raschild6.momentumplugin.managers.SonarManager;
import com.github.raschild6.momentumplugin.models.Configuration;
import com.github.raschild6.momentumplugin.toolWindows.supports.CollapsiblePanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class SonarConfigTab extends JPanel {
    private final SonarManager sonarManager;
    private final LogManager logManager;

    public SonarConfigTab(SonarManager sonarManager, LogManager logManager) {
        this.sonarManager = sonarManager;
        this.logManager = logManager;

        setLayout(new BorderLayout());

        // Pannello principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // Sezione 1: Configurazione del server
        JPanel serverPanel = createTitledPanel("");
        serverPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JTextField serverUrlField = new JTextField(30);
        JButton testConnectionButton = new JButton("Test Connection");
        serverPanel.add(createCenteredLabel("Server URL:"));
        serverPanel.add(serverUrlField);
        serverPanel.add(testConnectionButton);
        serverPanel.setBorder(JBUI.Borders.empty(10));

        // Sezione 2: Autenticazione
        JPanel authPanel = createTitledPanel("");
        authPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JPasswordField tokenField = new JPasswordField(50);
        JButton testAuthButton = new JButton("Test Authentication");
        authPanel.add(createCenteredLabel("Access Token: "));
        authPanel.add(tokenField);
        authPanel.add(testAuthButton);
        authPanel.setBorder(JBUI.Borders.empty(10));

        // Sezione 3: Profilo attivo
        JPanel profilePanel = createTitledPanel("");
        profilePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JComboBox<String> profileComboBox = new JComboBox<>();
        JButton updateProfilesButton = new JButton("Refresh Profiles");
        profilePanel.add(createCenteredLabel("Profile:"));
        profilePanel.add(profileComboBox);
        profilePanel.add(updateProfilesButton);
        profilePanel.setBorder(JBUI.Borders.empty(10));

        // Sezione 4: Proxy settings
        JPanel proxyPanel = createTitledPanel("");
        proxyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JTextField proxyHostField = new JTextField(20);
        JTextField proxyPortField = new JTextField(20);
        JTextField proxyUsernameField = new JTextField(20); 
        JPasswordField proxyPasswordField = new JPasswordField(20); 
        proxyPanel.add(createCenteredLabel("Host:"));
        proxyPanel.add(proxyHostField);
        proxyPanel.add(createCenteredLabel("Port:"));
        proxyPanel.add(proxyPortField);
        proxyPanel.add(createCenteredLabel("Username:"));
        proxyPanel.add(proxyUsernameField);
        proxyPanel.add(createCenteredLabel("Password:"));
        proxyPanel.add(proxyPasswordField);
        proxyPanel.setBorder(JBUI.Borders.empty(10));


        // Pannelli espandibili
        CollapsiblePanel collapsibleServerPanel = new CollapsiblePanel("SonarQube Server Configuration", serverPanel);
        CollapsiblePanel collapsibleAuthPanel = new CollapsiblePanel("Authentication", authPanel);
        CollapsiblePanel collapsibleProfilePanel = new CollapsiblePanel("Choose Profile", profilePanel);
        CollapsiblePanel collapsibleProxyPanel = new CollapsiblePanel("Proxy Settings", proxyPanel);

        // Pulsanti Salva/Annulla
        JPanel actionPanel = new JPanel();
        actionPanel.setBorder(JBUI.Borders.emptyTop(10));
        JButton saveButton = new JButton("Save Configuration");
        JButton importButton = new JButton("Import Configuration");
        JButton cancelButton = new JButton("Reset Fields");
        actionPanel.add(saveButton);
        actionPanel.add(importButton);
        actionPanel.add(cancelButton);

        // Aggiunta dei pannelli al mainPanel
        mainPanel.add(actionPanel);
        mainPanel.add(collapsibleServerPanel);
        mainPanel.add(collapsibleAuthPanel);
        mainPanel.add(collapsibleProfilePanel);
        mainPanel.add(collapsibleProxyPanel);

        // Aggiunta del mainPanel in uno JScrollPane per abilitare lo scroll
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Aggiunta dello scrollPane al pannello principale
        add(scrollPane, BorderLayout.CENTER);

        // Assegnazione dei listener
        testConnectionButton.addActionListener(e -> testConnection(serverUrlField.getText()));

        testAuthButton.addActionListener(e -> testAuthentication(serverUrlField.getText(), new String(tokenField.getPassword())));

        updateProfilesButton.addActionListener(e -> updateProfiles(profileComboBox, serverUrlField.getText(), new String(tokenField.getPassword())));

        saveButton.addActionListener(e -> saveConfiguration(
            serverUrlField.getText(),
            tokenField.getPassword(),
            profileComboBox.getSelectedIndex(),
            proxyHostField.getText(),
            proxyPortField.getText(),
            proxyUsernameField.getText(),
            proxyPasswordField.getPassword()
        ));

        importButton.addActionListener(e -> importConfiguration(
            true,
            serverUrlField,
            tokenField,
            profileComboBox,
            proxyHostField,
            proxyPortField,
            proxyUsernameField,
            proxyPasswordField));

        cancelButton.addActionListener(e -> setFields(
            serverUrlField,
            tokenField,
            profileComboBox,
            proxyHostField,
            proxyPortField,
            proxyUsernameField,
            proxyPasswordField,
            new Configuration("",
                "".toCharArray(),
                -1,
                "",
                "",
                "",
                "".toCharArray())));

        initConfiguration(serverUrlField,
            tokenField,
            profileComboBox,
            proxyHostField,
            proxyPortField,
            proxyUsernameField,
            proxyPasswordField);
    }

    private void initConfiguration(JTextField serverUrlField,
                                   JPasswordField tokenField,
                                   JComboBox<String> profileComboBox,
                                   JTextField proxyHostField,
                                   JTextField proxyPortField,
                                   JTextField proxyUsernameField,
                                   JPasswordField proxyPasswordField) {
        importConfiguration(false,
            serverUrlField,
            tokenField,
            profileComboBox,
            proxyHostField,
            proxyPortField,
            proxyUsernameField,
            proxyPasswordField);
    }

    /**
     * Crea un pannello con un titolo e un layout a griglia (2 colonne).
     */
    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    /**
     * Crea una JLabel centrata.
     */
    private JLabel createCenteredLabel(String text) {
        return new JLabel(text, SwingConstants.CENTER);
    }

    /**
     * Testa la connessione al server SonarQube.
     */
    private void testConnection(String serverUrl) {
        if (sonarManager.testConnection(serverUrl)) {
            JOptionPane.showMessageDialog(this, "Connessione riuscita!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.logManager.log("Connection to SonarQube server successful.");
        } else {
            JOptionPane.showMessageDialog(this, "Connessione fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            this.logManager.log("Failed to connect to SonarQube server.");
        }
    }

    /**
     * Testa l'autenticazione con il token fornito.
     */
    private void testAuthentication(String serverUrl, String token) {
        if (sonarManager.testAuthentication(serverUrl, token)) {
            JOptionPane.showMessageDialog(this, "Autenticazione riuscita!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.logManager.log("Authentication with SonarQube server successful.");
        } else {
            JOptionPane.showMessageDialog(this, "Autenticazione fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            this.logManager.log("Failed to authenticate with SonarQube server.");
        }
    }

    /**
     * Aggiorna i profili disponibili nel JComboBox.
     */
    private void updateProfiles(JComboBox<String> profileComboBox, String serverUrl, String token) {
        String[] profiles = sonarManager.getProfiles(serverUrl, token);
        profileComboBox.removeAllItems();
        for (String profile : profiles) {
            profileComboBox.addItem(profile);
        }
        JOptionPane.showMessageDialog(this, "Profili aggiornati!", "Successo", JOptionPane.INFORMATION_MESSAGE);
        this.logManager.log("Profiles updated successfully.");
    }

    /**
     * Salva la configurazione corrente.
     */
    private void saveConfiguration(String serverUrl, char[] token, int activeProfileIndex, String proxyHost,
                                   String proxyPort, String proxyUsername, char[] proxyPassword) {
        boolean success = sonarManager.saveConfiguration(
            serverUrl,
            token,
            activeProfileIndex,
            proxyHost,
            proxyPort,
            proxyUsername,
            proxyPassword
        );
        if (success) {
            JOptionPane.showMessageDialog(this, "Configurazione salvata con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.logManager.log("Configuration saved successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Errore nel salvataggio della configurazione.", "Errore", JOptionPane.ERROR_MESSAGE);
            this.logManager.log("Failed to save configuration.");
        }
    }

    /**
     * Importa la configurazione salvata, se trovata in /{user}/.config/MomentumPlugin/sonar-config.json.
     */
    private void importConfiguration(boolean verbose,
                                     JTextField serverUrlField,
                                     JPasswordField tokenField,
                                     JComboBox<String> profileComboBox,
                                     JTextField proxyHostField,
                                     JTextField proxyPortField,
                                     JTextField proxyUsernameField,
                                     JPasswordField proxyPasswordField) {
        Configuration configuration = sonarManager.importConfiguration();
        if (configuration != null) {
            this.setFields(
                serverUrlField,
                tokenField,
                profileComboBox,
                proxyHostField,
                proxyPortField,
                proxyUsernameField,
                proxyPasswordField,
                configuration);
            this.logManager.log("Configuration imported successfully.");
            if(verbose){
                JOptionPane.showMessageDialog(this, "Configurazione importata con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            this.logManager.log("Missing configuration.");
            if(verbose) {
                JOptionPane.showMessageDialog(this, "Errore nell'importazione della configurazione.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Setta i campi ai valori passati input.
     */
    private void setFields(JTextField serverUrlField,
                           JPasswordField tokenField,
                           JComboBox<String> profileComboBox,
                           JTextField proxyHostField,
                           JTextField proxyPortField,
                           JTextField proxyUsernameField,
                           JPasswordField proxyPasswordField,
                            Configuration configuration) {
        serverUrlField.setText(configuration.getServerUrl());
        tokenField.setText(new String(configuration.getToken()));
        profileComboBox.setSelectedItem(configuration.getActiveProfileIndex());
        proxyHostField.setText(configuration.getProxyHost());
        proxyPortField.setText(configuration.getProxyPort());
        proxyUsernameField.setText(configuration.getProxyUsername());
        proxyPasswordField.setText(new String(configuration.getProxyPassword()));
    }
}
