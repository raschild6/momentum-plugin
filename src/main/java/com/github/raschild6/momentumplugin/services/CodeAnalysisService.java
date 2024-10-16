package com.github.raschild6.momentumplugin.services;

import com.github.raschild6.momentumplugin.models.Issue;
import com.github.raschild6.momentumplugin.models.Severity;
import com.github.raschild6.momentumplugin.toolWindow.CodeQualityToolWindowFactory;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.util.ArrayList;

@Service(Service.Level.PROJECT)
public final class CodeAnalysisService {

    private static final Logger LOG = Logger.getInstance(CodeAnalysisService.class);
    private final Project project;

    public ArrayList<Issue> issues = new ArrayList<>();
    private long globalIdError = 0;

    public CodeAnalysisService(Project project) {
        this.project = project;
    }

    public int runCodeAnalysis(Project project) {
        LOG.info("Start code analysis for project: " + project.getName());
        appendOperationLabel("-- Star code analysis for project: " + project.getName(), Color.WHITE);

        int result = (int) (Math.random() * 10);

        Issue issue = new Issue(globalIdError);
        issue.setDescription(String.valueOf(result));
        issue.setSeverity(Severity.values()[(int) (Math.random() * Severity.values().length)]);
        globalIdError++;

        addUniqueIssue(issue);

        appendNumberLabel(issue.getSeverity());

        printAllIssues();

        LOG.info("End code analysis for project: " + project.getName());
        appendOperationLabel("-- End code analysis for project: " + project.getName(), Color.WHITE);

        return result;
    }

    private void appendLogArea(String message, Color color) {
        CodeQualityToolWindowFactory codeQualityToolWindowFactory = CodeQualityToolWindowFactory.getInstance();
        if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getLogArea() != null) {
            codeQualityToolWindowFactory.appendLogArea(message, color);
        }
    }

    private void appendOperationLabel(String message, Color color) {
        CodeQualityToolWindowFactory codeQualityToolWindowFactory = CodeQualityToolWindowFactory.getInstance();
        if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getOperationLabel() != null) {
            codeQualityToolWindowFactory.appendOperationLabel(message, color);
        }
    }

    private void appendNumberLabel(Severity severity) {
        CodeQualityToolWindowFactory codeQualityToolWindowFactory = CodeQualityToolWindowFactory.getInstance();
        switch(severity) {
            case ERROR -> {
                if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getErrorNumberLabel() != null) {
                    codeQualityToolWindowFactory.appendNumberError();
                }
            }
            case WARNING -> {
                if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getWarningNumberLabel() != null) {
                    codeQualityToolWindowFactory.appendNumberWarning();
                }
            }
            case INFO -> {
                if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getInfoNumberLabel() != null) {
                    codeQualityToolWindowFactory.appendNumberInfo();
                }
            }
        }
    }

    public void clearIssues(){
        issues.clear();
        globalIdError = 0;
    }

    private void printAllIssues(){
        CodeQualityToolWindowFactory codeQualityToolWindowFactory = CodeQualityToolWindowFactory.getInstance();
        if (codeQualityToolWindowFactory != null && codeQualityToolWindowFactory.getLogArea() != null) {
            codeQualityToolWindowFactory.updateLogFromIssues(codeQualityToolWindowFactory.getLogArea(), issues);
        }
    }

    private void addUniqueIssue(Issue issue) {
        issues.removeIf(i -> i.getId() == issue.getId());
        issues.add(issue);
    }
}
