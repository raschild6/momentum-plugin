package com.github.raschild6.momentumplugin.managers;

import com.github.raschild6.momentumplugin.mavenSupport.LoggerOutputStream;
import com.github.raschild6.momentumplugin.models.SummaryRule;
import com.github.raschild6.momentumplugin.toolWindows.tabs.RuleTab;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.maven.cli.MavenCli;
import org.sonar.api.server.ServerSide;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ServerSide
public class RuleManager {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final LogManager logManager;
    private final SonarManager sonarManager;
    private final ChatGPTManager chatGPTManager;

    public RuleManager(LogManager logManager, SonarManager sonarManager) {
        this.logManager = logManager;
        this.sonarManager = sonarManager;
        chatGPTManager = new ChatGPTManager(logManager);

    }

    public Map<String, String> loadProfiles(SonarManager sonarManager,
                                            JComboBox<AbstractMap.SimpleEntry<String, String>> profileComboBox) {
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

            if(activeProfileEntry != null) {
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
        if(profileKey == null)
            return;

        tableModel.setRowCount(0);

        List<SummaryRule> rules = sonarManager.getRulesForProfile(
            SonarManager.currentConfiguration.getServerUrl(),
            new String(SonarManager.currentConfiguration.getToken()),
            profileKey);

        for(SummaryRule rule : rules) {
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

        if(option == JOptionPane.OK_OPTION) {
            String ruleName = ruleNameField.getText().trim();
            String ruleDescription = ruleDescriptionArea.getText().trim();

            if(!ruleName.isEmpty() && !ruleDescription.isEmpty()) {
                String apiKey = System.getenv("OPENAI_API_KEY");
                if(apiKey == null || apiKey.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "La chiave API OpenAI non è configurata nelle variabili di ambiente.", "Errore",
                        JOptionPane.ERROR_MESSAGE);
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
                            if(ruleContent != null) {
                                saveRuleToFile(ruleName, ruleContent);
                            } else {
                                JOptionPane.showMessageDialog(null, "Errore nella generazione della regola.", "Errore",
                                    JOptionPane.ERROR_MESSAGE);
                                logManager.log("Error while generating rule content.");
                            }
                        } catch(Exception e) {
                            JOptionPane.showMessageDialog(null,
                                "Errore nella generazione della regola: " + e.getMessage(), "Errore",
                                JOptionPane.ERROR_MESSAGE);
                            logManager.log("Error while generating rule content: " + e);
                        }
                        RuleTab.stopLoading();
                    }
                };

                worker.execute();

            } else {
                JOptionPane.showMessageDialog(null, "Nome e descrizione della regola sono obbligatori.", "Errore",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveRuleToFile(String ruleName, String ruleContent) {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, ".config", "MomentumPlugin", "rules");

        try {
            if(!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path ruleFile = configDir.resolve(ruleName.replaceAll("\\s+", "_") + ".java");
            Files.writeString(ruleFile, ruleContent);

            JOptionPane.showMessageDialog(null, "Regola salvata con successo in: " + ruleFile.toAbsolutePath(),
                "Successo", JOptionPane.INFORMATION_MESSAGE);

            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            VirtualFile
                virtualFile =
                LocalFileSystem.getInstance().refreshAndFindFileByPath(ruleFile.toAbsolutePath().toString());
            if(virtualFile != null) {
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            } else {
                logManager.log("Cannot find the saved file for opening.");
                JOptionPane.showMessageDialog(null, "Impossibile trovare il file salvato per l'apertura.", "Errore",
                    JOptionPane.WARNING_MESSAGE);
            }

        } catch(IOException e) {
            logManager.log("Error while saving the file: " + e);
            JOptionPane.showMessageDialog(null, "Errore nel salvataggio del file: " + e.getMessage(), "Errore",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openTemplatesFolder() {
        String userHome = System.getProperty("user.home");
        Path templatesDir = Paths.get(userHome, ".config", "MomentumPlugin", "templates");

        openFolder(templatesDir);
    }

    public void generateTemplateButton() {
        Path templatePathFolder = generateSonarRuleJar();

        if(templatePathFolder != null) {
            JOptionPane.showMessageDialog(null, "Template generato con successo.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            logManager.log("Template generated successfully.");

            askOpenFolder(templatePathFolder);

        } else {
            JOptionPane.showMessageDialog(null, "Errore durante la generazione del template.", "Errore",
                JOptionPane.ERROR_MESSAGE);
            logManager.log("Error while generating the template.");
        }
    }

    public void askOpenFolder(Path templatePathFolder) {
        int choice = JOptionPane.showConfirmDialog(
            null,
            "Vuoi aprire la cartella del template creato?",
            "Aprire la cartella?",
            JOptionPane.YES_NO_OPTION
        );

        if(choice ==JOptionPane.YES_OPTION){
            openFolder(templatePathFolder);
        }
    }

    private void openFolder(Path templatesDir) {
        try {
            File templateFolder = new File(templatesDir.toString());

            // Apri l'esplora risorse nella cartella
            if(templateFolder.exists()) {
                Desktop.getDesktop().open(templateFolder);
            } else {
                JOptionPane.showMessageDialog(null, "Template non trovato.", "Errore", JOptionPane.ERROR_MESSAGE);
                logManager.log("Template folder not found.");
            }
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Errore durante l'apertura della cartella: " + e.getMessage(), "Errore",
                JOptionPane.ERROR_MESSAGE);
            logManager.log("Error while opening the folder: " + e);
        }
    }

    private Path generateSonarRuleJar() {
        String userHome = System.getProperty("user.home");
        Path tempDir = Paths.get(userHome, ".config", "MomentumPlugin", "templates",
            "temp-" + LocalDateTime.now().format(DATE_TIME_FORMATTER));

        try {
            // Extract sonar-plugin-template.zip from resources inside the JAR
            InputStream zipInputStream = getClass().getClassLoader().getResourceAsStream("sonar-plugin-template.zip");

            if (zipInputStream == null) {
                logManager.log("Error: sonar-plugin-template.zip not found in the JAR.");
                JOptionPane.showMessageDialog(null, "Template sonar-plugin-template.zip non trovato nel JAR.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Files.createDirectories(tempDir);

            // Unzip the sonar-plugin-template.zip
            unzip(zipInputStream, tempDir);

            return tempDir;

        } catch (IOException e) {
            logManager.log("Error while generating the temporary project: " + e);
            JOptionPane.showMessageDialog(null, "Error while generating the project: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void unzip(InputStream zipInputStream, Path targetDirectory) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path entryPath = targetDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipIn.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }

    public void loadRuleOnTemplate() {
        JFileChooser folderTemplateChooser = new JFileChooser();

        // Imposta la directory predefinita
        String userHome = System.getProperty("user.home");
        File defaultDirectory = new File(Paths.get(userHome, ".config", "MomentumPlugin", "templates").toString());
        folderTemplateChooser.setCurrentDirectory(defaultDirectory);

        folderTemplateChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderTemplateChooser.setDialogTitle("Seleziona la cartella del Template");

        int result = folderTemplateChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderTemplateChooser.getSelectedFile();
            Path templateFolderPath = selectedFolder.toPath();

            JFileChooser fileChooser = new JFileChooser();

            // Imposta la directory predefinita
            defaultDirectory = new File(Paths.get(userHome, ".config", "MomentumPlugin", "rules").toString());
            fileChooser.setCurrentDirectory(defaultDirectory);

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("File Java", "java"));
            folderTemplateChooser.setDialogTitle("Selezione la regole da inserire nel Template");

            // Abilita la selezione multipla
            fileChooser.setMultiSelectionEnabled(true);

            result = fileChooser.showOpenDialog(null);
            if(result == JFileChooser.APPROVE_OPTION) {
                // Ottieni i file selezionati
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for(File selectedFile : selectedFiles) {
                    Path rulePath = selectedFile.toPath();

                    // Now, move the rule file to the correct directory
                    Path targetDir = templateFolderPath.resolve("sonar-plugin-template/src/main/java/org/sonar/samples/java");
                    try {
                        Files.createDirectories(targetDir);
                        Files.copy(rulePath, targetDir.resolve(rulePath.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch(IOException e) {
                        JOptionPane.showMessageDialog(null,
                            "Errore durante il trasferimento della regola " + rulePath.getFileName() + ": " +
                                e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                        logManager.log("Error while moving the rule file " + rulePath.getFileName() + ": " + e);
                    }
                }
                JOptionPane.showMessageDialog(null, "Regole caricate con successo sul Template: " +
                        templateFolderPath.getFileName(), "Successo",
                    JOptionPane.INFORMATION_MESSAGE);
                logManager.log("Rules loaded successfully on the Template.");

                Path targetDir = templateFolderPath.resolve("sonar-plugin-template/src/main/java/org/sonar/samples/java");
                askOpenFolder(targetDir);
            }
        }
    }

    public void loadTemplateOnProfile() {
        JFileChooser folderTemplateChooser = new JFileChooser();

        // Imposta la directory predefinita
        String userHome = System.getProperty("user.home");
        File defaultDirectory = new File(Paths.get(userHome, ".config", "MomentumPlugin", "templates").toString());
        folderTemplateChooser.setCurrentDirectory(defaultDirectory);

        folderTemplateChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderTemplateChooser.setDialogTitle("Seleziona il Template da caricare sul profilo SonarQube");

        int result = folderTemplateChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderTemplateChooser.getSelectedFile();

            // Now, compile the Maven project
            String jarFilePath = compileMavenProject(selectedFolder.toPath().toString());

            if (jarFilePath != null) {
                // After compilation, upload the JAR to SonarQube
                sonarManager.uploadToSonarQube(jarFilePath, SonarManager.currentConfiguration.getServerUrl(),
                    SonarManager.currentConfiguration.getToken());
            } else {
                JOptionPane.showMessageDialog(null, "Error during Maven project compilation.", "Error", JOptionPane.ERROR_MESSAGE);
                logManager.log("Error during Maven project compilation.");
            }
        }
    }

    private String compileMavenProject(String projectDir) {
        runMavenTask(projectDir, new String[]{"clean", "package"});

        // Assuming the JAR is in the "target" directory of the Maven project
        Path jarPath = Paths.get(projectDir, "target", "plugin.jar"); // Adjust as needed
        if (Files.exists(jarPath)) {
            return jarPath.toString();
        }
        return null;
    }

    public void runMavenTask(String workingDirectory, String[] goals) {
        // Crea una nuova istanza di MavenCli
        MavenCli cli = new MavenCli();

        // Ottieni il tuo loggerManager come PrintStream
        PrintStream outStream = new PrintStream(new LoggerOutputStream(logManager));
        PrintStream errStream = new PrintStream(new LoggerOutputStream(logManager));

        // Esegui il comando Maven
        int result = cli.doMain(goals, new File(workingDirectory).getAbsolutePath(), outStream, errStream);

        // Gestisci l'esito dell'esecuzione
        if (result != 0) {
            logManager.log("Error while executing Maven command");
            JOptionPane.showMessageDialog(null, "Errore durante l'esecuzione del comando Maven", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

}
