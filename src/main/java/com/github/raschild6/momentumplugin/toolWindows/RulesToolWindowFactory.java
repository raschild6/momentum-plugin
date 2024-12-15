package com.github.raschild6.momentumplugin.toolWindows;

import com.github.raschild6.momentumplugin.managers.LogManager;
import com.github.raschild6.momentumplugin.managers.SonarManager;
import com.github.raschild6.momentumplugin.managers.RuleManager;
import com.github.raschild6.momentumplugin.toolWindows.tabs.LogTab;
import com.github.raschild6.momentumplugin.toolWindows.tabs.RuleTab;
import com.github.raschild6.momentumplugin.toolWindows.tabs.SonarConfigTab;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;

public class RulesToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // Recupera le istanze di Server e Configuration
        LogManager logManager = new LogManager();
        SonarManager sonarManager = new SonarManager(logManager);
        RuleManager ruleManager = new RuleManager(logManager, sonarManager);

        // Creazione della UI per il Tool Window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Tab panel
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab "Log"
        LogTab logTab = new LogTab(logManager);
        JPanel logPanel = logTab.createLogTab();

        // Tab "Gestione configurazione Sonar"
        JPanel configTab = new SonarConfigTab(sonarManager, logManager);

        // Tab "Regole"
        JPanel rulesTab = new RuleTab(ruleManager, logManager, sonarManager);

        tabbedPane.addTab("Rules", rulesTab);
        tabbedPane.addTab("Sonar Config", configTab);
        tabbedPane.addTab("Logs", logPanel);

        // Aggiungi il TabbedPane al pannello principale
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Creazione del contenuto del ToolWindow
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "Rules Manager", false);
        toolWindow.getContentManager().addContent(content);

    }

}
