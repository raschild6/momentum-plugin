package com.github.raschild6.momentumplugin.toolWindows.tabs;

import com.github.raschild6.momentumplugin.managers.LogManager;
import com.github.raschild6.momentumplugin.managers.SonarManager;
import com.github.raschild6.momentumplugin.models.Configuration;
import com.github.raschild6.momentumplugin.toolWindows.supports.CollapsiblePanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SonarConfigTab extends JPanel {
    private final SonarManager sonarManager;
    private final LogManager logManager;

    public SonarConfigTab(SonarManager sonarManager, LogManager logManager) {
        this.sonarManager = sonarManager;
        this.logManager = logManager;

        Map<String, String> profilesSonarMap = new HashMap<>();

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
        JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox = new JComboBox<>();
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

        updateProfilesButton.addActionListener(e -> {
            updateProfiles(profileComboBox, serverUrlField.getText(), new String(tokenField.getPassword()));
            updateConfiguration(
                serverUrlField.getText(),
                tokenField.getPassword(),
                null,
                proxyHostField.getText(),
                proxyPortField.getText(),
                proxyUsernameField.getText(),
                proxyPasswordField.getPassword()
            );
        });

        saveButton.addActionListener(e -> saveConfiguration(
            serverUrlField.getText(),
            tokenField.getPassword(),
            profileComboBox.getSelectedIndex() != -1 ?
                ((AbstractMap.SimpleEntry<String, String>) profileComboBox.getSelectedItem()).getKey() :
                null,
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
                null,
                "",
                "",
                "",
                "".toCharArray())));

        addFocusListeners(() -> updateConfiguration(
                serverUrlField.getText(),
                tokenField.getPassword(),
                profileComboBox.getSelectedIndex() != -1 ?
                    profilesSonarMap.get(
                        ((AbstractMap.SimpleEntry<String, String>) profileComboBox.getSelectedItem()).getKey()) :
                        null,
                proxyHostField.getText(),
                proxyPortField.getText(),
                proxyUsernameField.getText(),
                proxyPasswordField.getPassword()
            ),
            serverUrlField, tokenField,proxyHostField, proxyPortField, proxyUsernameField, proxyPasswordField);

        initConfiguration(serverUrlField,
            tokenField,
            profileComboBox,
            proxyHostField,
            proxyPortField,
            proxyUsernameField,
            proxyPasswordField);

        final Map<String, String> loadedProfiles = loadProfiles(profileComboBox);
        profilesSonarMap.putAll(loadedProfiles);

    }

    /**
     * Inizializza la configurazione del server SonarQube.
     */
    private void initConfiguration(JTextField serverUrlField,
                                   JPasswordField tokenField,
                                   JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox,
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
     * Carica i profili disponibili dal server SonarQube.
     */
    private Map<String, String> loadProfiles(JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox) {
        Map<String, String> profiles = null;
        if(SonarManager.currentConfiguration != null &&
            SonarManager.currentConfiguration.getServerUrl() != null &&
            SonarManager.currentConfiguration.getToken() != null) {

            profiles = sonarManager.getProfiles(SonarManager.currentConfiguration.getServerUrl(),
                new String(SonarManager.currentConfiguration.getToken()));

            profileComboBox.removeAllItems();

            profiles.forEach((key, value) -> profileComboBox.addItem(new AbstractMap.SimpleEntry<>(key, value)));

            Map.Entry<String, String> activeProfileEntry = profiles.entrySet().stream().filter(
                entry -> entry.getKey().equals(SonarManager.currentConfiguration.getActiveProfile())
            ).findFirst().orElse(null);

            if(activeProfileEntry != null){
                profileComboBox.setSelectedItem(activeProfileEntry);
            } else {
                profileComboBox.setSelectedIndex(-1);
            }
        }
        return profiles;
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
    private void updateProfiles(JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox, String serverUrl, String token) {
        Map<String, String> profiles = sonarManager.getProfiles(serverUrl, token);

        profileComboBox.removeAllItems();

        profiles.forEach((key, value) -> profileComboBox.addItem(new AbstractMap.SimpleEntry<>(key, value)));

        profileComboBox.setSelectedIndex(-1);

        this.logManager.log("Profiles updated successfully.");
    }

    /**
     * Salva la configurazione corrente.
     */
    private void saveConfiguration(String serverUrl, char[] token, String activeProfile, String proxyHost,
                                   String proxyPort, String proxyUsername, char[] proxyPassword) {
        boolean success = sonarManager.saveConfiguration(
            serverUrl,
            token,
            activeProfile,
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
                                     JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox,
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

            sonarManager.setCurrentConfiguration(configuration);

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
                           JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox,
                           JTextField proxyHostField,
                           JTextField proxyPortField,
                           JTextField proxyUsernameField,
                           JPasswordField proxyPasswordField,
                            Configuration configuration) {

        serverUrlField.setText(configuration.getServerUrl());
        tokenField.setText(new String(configuration.getToken()));

        String activeProfileKey = configuration.getActiveProfile();
        Optional<Map.Entry<String, String>> activeProfileEntry =
            sonarManager.getProfiles(serverUrlField.getText(), new String(tokenField.getPassword()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(activeProfileKey))
                .findFirst();

        if(activeProfileEntry.isPresent()){
            profileComboBox.setSelectedItem(activeProfileEntry.get());
        } else {
            profileComboBox.setSelectedIndex(-1);
        }

        proxyHostField.setText(configuration.getProxyHost());
        proxyPortField.setText(configuration.getProxyPort());
        proxyUsernameField.setText(configuration.getProxyUsername());
        proxyPasswordField.setText(new String(configuration.getProxyPassword()));

        sonarManager.setCurrentConfiguration(configuration);
    }

    /**
        * Aggiunge un listener di focus ai componenti passati come input.
     */
    private void addFocusListeners(Runnable onFocusLostAction, JComponent... components) {
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onFocusLostAction.run();
            }
        };
        for (JComponent component : components) {
            component.addFocusListener(listener);
        }
    }

    /**
     * Aggiorna la configurazione corrente.
     */
    private void updateConfiguration(String serverUrl, char[] token, String activeProfile, String proxyHost,
                                     String proxyPort, String proxyUsername, char[] proxyPassword) {
        Configuration configuration = new Configuration(
            serverUrl,
            token,
            activeProfile,
            proxyHost,
            proxyPort,
            proxyUsername,
            proxyPassword);

        sonarManager.setCurrentConfiguration(configuration);
    }

}
