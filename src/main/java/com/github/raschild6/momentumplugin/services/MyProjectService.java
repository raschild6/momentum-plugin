package com.github.raschild6.momentumplugin.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class MyProjectService {

    private static final Logger LOG = Logger.getInstance(MyProjectService.class);
    private final Project project;

    public MyProjectService(Project project) {
        this.project = project;
        LOG.info("Project service initialized for " + project.getName());
    }

    // Simula un'analisi del codice
    public int runCodeAnalysis() {
        LOG.info("Running code analysis for project: " + project.getName());
        // In futuro: implementare l'analisi vera e propria
        return (int) (Math.random() * 10);  // Simula errori casuali
    }
}
