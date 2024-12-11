package com.github.raschild6.momentumplugin.managers;

import com.github.raschild6.momentumplugin.models.SummaryRule;
import com.github.raschild6.momentumplugin.toolWindows.RulesToolWindowFactory;
import com.github.raschild6.momentumplugin.toolWindows.tabs.RuleTab;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ServerSide
public class RuleManager {
    private static final Logger logger = LoggerFactory.getLogger(RuleManager.class);

    private final LogManager logManager;
    private final ChatGPTManager chatGPTManager;

    public RuleManager(LogManager logManager) {
        this.logManager = logManager;
        chatGPTManager = new ChatGPTManager(logManager);
    }

    public Map<String, String> loadProfiles(SonarManager sonarManager, JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox) {
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

    public String getSelectedProfileKey(JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox,
                                         Map<String, String> profilesSonarMap,
                                        boolean isKey) {
        return profileComboBox.getSelectedIndex() != -1 ?
            isKey ? ((AbstractMap.SimpleEntry<String, String>) profileComboBox.getSelectedItem()).getKey() :
                profilesSonarMap.get(
                    ((AbstractMap.SimpleEntry<String, String>) profileComboBox.getSelectedItem()).getKey()) :
            null;
    }

    public void loadRules(SonarManager sonarManager, DefaultTableModel tableModel, String profileKey) {
        if (profileKey == null)
            return;

        tableModel.setRowCount(0);

        List<SummaryRule> rules = sonarManager.getRulesForProfile(
            SonarManager.currentConfiguration.getServerUrl(),
            new String(SonarManager.currentConfiguration.getToken()),
            profileKey);

        for (SummaryRule rule : rules) {
            tableModel.addRow(new Object[]{
                rule.getKey(),
                rule.getName(),
                rule.getSeverity(),
                rule.getStatus(),
                rule.getType(),
                rule.isTemplate()
            });
        }
    }

    public void createNewRule() {
        JTextField ruleNameField = new JTextField();
        JTextArea ruleDescriptionArea = new JTextArea(5, 80);
        ruleDescriptionArea.setLineWrap(true);
        ruleDescriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(ruleDescriptionArea);

        Object[] message = {
            "Inserisci il nome della regola:", ruleNameField,
            "Inserisci la descrizione della regola:", scrollPane
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crea Nuova Regola", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String ruleName = ruleNameField.getText().trim();
            String ruleDescription = ruleDescriptionArea.getText().trim();

            if (!ruleName.isEmpty() && !ruleDescription.isEmpty()) {
                String apiKey = System.getenv("OPENAI_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "La chiave API OpenAI non è configurata nelle variabili di ambiente.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Usa SwingWorker per eseguire la generazione della regola
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() {
                        RuleTab.startLoading();
                        return chatGPTManager.generateRuleContent(ruleName, ruleDescription, apiKey);
                    }

                    @Override
                    protected void done() {
                        try {
                            String ruleContent = get();
                            if (ruleContent != null) {
                                saveRuleToFile(ruleName, ruleContent);
                            } else {
                                JOptionPane.showMessageDialog(null, "Errore nella generazione della regola.", "Errore", JOptionPane.ERROR_MESSAGE);
                                logManager.log("Error while generating rule content.");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Errore nella generazione della regola: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                            logManager.log("Error while generating rule content: " + e);
                        }
                        RuleTab.stopLoading();
                    }
                };

                worker.execute();

            } else {
                JOptionPane.showMessageDialog(null, "Nome e descrizione della regola sono obbligatori.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveRuleToFile(String ruleName, String ruleContent) {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, ".config", "MomentumPlugin", "rules");

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path ruleFile = configDir.resolve(ruleName.replaceAll("\\s+", "_") + ".java");
            Files.writeString(ruleFile, ruleContent);

            JOptionPane.showMessageDialog(null, "Regola salvata con successo in: " + ruleFile.toAbsolutePath(), "Successo", JOptionPane.INFORMATION_MESSAGE);

            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            VirtualFile
                virtualFile =
                LocalFileSystem.getInstance().refreshAndFindFileByPath(ruleFile.toAbsolutePath().toString());
            if (virtualFile != null) {
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            } else {
                logManager.log("Cannot find the saved file for opening.");
                JOptionPane.showMessageDialog(null, "Impossibile trovare il file salvato per l'apertura.", "Errore", JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException e) {
            logger.error("Errore nel salvataggio della regola", e);
            JOptionPane.showMessageDialog(null, "Errore nel salvataggio del file: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

}
