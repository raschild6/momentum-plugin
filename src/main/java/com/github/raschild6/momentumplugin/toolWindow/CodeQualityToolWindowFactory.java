package com.github.raschild6.momentumplugin.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import com.github.raschild6.momentumplugin.services.MyProjectService;

import javax.swing.*;
import java.awt.*;

public class CodeQualityToolWindowFactory implements ToolWindowFactory {

    private static final Logger LOG = Logger.getInstance(CodeQualityToolWindowFactory.class);

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        CodeQualityToolWindow codeQualityToolWindow = new CodeQualityToolWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(codeQualityToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(Project project) {
        return true;
    }

    public static class CodeQualityToolWindow {
        private final MyProjectService service;

        public CodeQualityToolWindow(ToolWindow toolWindow) {
            this.service = toolWindow.getProject().getService(MyProjectService.class);
        }

        public JPanel getContent() {
            // Crea un pannello per visualizzare i messaggi di errore
            JBPanel<JBPanel<?>> panel = new JBPanel<>(new GridLayout(2, 1));
            JBLabel label = new JBLabel("Code Quality Report");

            // Bottone di esempio per aggiornare i risultati (da collegare alla logica di analisi)
            JButton updateButton = new JButton("Run Analysis");
            updateButton.addActionListener(e -> {
                // Esegui la logica di analisi e aggiorna la finestra
                label.setText("Errors Found: " + service.runCodeAnalysis());
            });

            panel.add(label);
            panel.add(updateButton);
            return panel;
        }
    }
}
