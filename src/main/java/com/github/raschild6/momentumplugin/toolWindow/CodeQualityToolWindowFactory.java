package com.github.raschild6.momentumplugin.toolWindow;

import com.github.raschild6.momentumplugin.models.Issue;
import com.github.raschild6.momentumplugin.models.Severity;
import com.github.raschild6.momentumplugin.services.CodeAnalysisService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;

public final class CodeQualityToolWindowFactory implements ToolWindowFactory {

    private static CodeQualityToolWindowFactory instance;
    private CodeAnalysisService codeAnalysisService;


    private JTextArea logArea;
    private JLabel errorNumberLabel, warningNumberLabel, infoNumberLabel, operationLabel;

    public CodeQualityToolWindowFactory() {
        instance = this;
    }

    public static CodeQualityToolWindowFactory getInstance() {
        return instance;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        codeAnalysisService = project.getService(CodeAnalysisService.class);

        JPanel mainPanel = getContent();
        var content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public JLabel getErrorNumberLabel() {
        return errorNumberLabel;
    }

    public JLabel getWarningNumberLabel() {
        return warningNumberLabel;
    }

    public JLabel getInfoNumberLabel() {
        return infoNumberLabel;
    }

    public JLabel getOperationLabel() {
        return operationLabel;
    }

    public void updateLogFromIssues(JTextArea logArea, ArrayList<Issue> issues) {
        logArea.setText("");
        for (Issue issue : issues) {
            appendLogArea("Issue ID: " + issue.getId(), getColorBySeverity(issue.getSeverity()));
        }
    }

    private Color getColorBySeverity(Severity severity) {
        return severity == null ? Color.WHITE :
            switch(severity) {
                case ERROR -> Color.RED;
                case WARNING -> Color.ORANGE;
                case INFO -> Color.GREEN;
            };
    }

    public void appendLogArea(String message, Color color) {
        logArea.setForeground(color);
        logArea.append(message + "\n");
    }

    public void appendNumberError() {
        errorNumberLabel.setText(String.valueOf(Integer.parseInt(errorNumberLabel.getText()) + 1));
    }

    public void appendNumberWarning() {
        warningNumberLabel.setText(String.valueOf(Integer.parseInt(warningNumberLabel.getText()) + 1));
    }

    public void appendNumberInfo() {
        infoNumberLabel.setText(String.valueOf(Integer.parseInt(infoNumberLabel.getText()) + 1));
    }

    public void appendOperationLabel(String message, Color color) {
        operationLabel.setForeground(color);
        operationLabel.setText(message);
    }

    private JPanel getContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        MatteBorder lineBorder = new MatteBorder(1, 1, 1, 1, Color.WHITE);
            EmptyBorder marginBorder = new EmptyBorder(10, 10, 10, 0);

        JPanel topPanel = new JPanel(new GridLayout(1, 1));
        topPanel.setBorder(new CompoundBorder(lineBorder, marginBorder));

        JPanel topLeftPanel = new JPanel(new GridLayout(1, 6));
        MatteBorder lineBorderTopLeftPanel = new MatteBorder(0, 1, 0, 0, Color.WHITE);
        topLeftPanel.setBorder(new CompoundBorder(lineBorderTopLeftPanel, marginBorder));

        JLabel errorLabel = new JLabel("Errors:");
        errorNumberLabel = new JLabel("0");
        JLabel warningLabel = new JLabel("Warning:");
        warningNumberLabel = new JLabel("0");
        JLabel infoLabel = new JLabel("Info:");
        infoNumberLabel = new JLabel("0");


        operationLabel = new JLabel("");

        errorLabel.setForeground(Color.RED);
        warningLabel.setForeground(Color.ORANGE);
        infoLabel.setForeground(Color.GREEN);
        operationLabel.setForeground(Color.WHITE);

        topLeftPanel.add(errorLabel, BorderLayout.WEST);
        topLeftPanel.add(errorNumberLabel, BorderLayout.WEST);
        topLeftPanel.add(warningLabel, BorderLayout.WEST);
        topLeftPanel.add(warningNumberLabel, BorderLayout.WEST);
        topLeftPanel.add(infoLabel, BorderLayout.WEST);
        topLeftPanel.add(infoNumberLabel, BorderLayout.WEST);
        topPanel.add(operationLabel, BorderLayout.WEST);

        topPanel.add(topLeftPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Area di testo per i log
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Logs"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Pannello di controllo con i bottoni (start, stop, execute)
        JPanel controlPanel = new JPanel(new GridLayout(4, 1));
        JButton updateButton = new JButton("Execute once");
        updateButton.addActionListener(e -> {
        });

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            operationClear();
        });

        controlPanel.add(updateButton);
        controlPanel.add(clearButton);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        return mainPanel;
    }

    public void operationClear() {
        errorNumberLabel.setText("0");
        warningNumberLabel.setText("0");
        infoNumberLabel.setText("0");
        logArea.setText("");
        operationLabel.setText("Clear operation completed");
        codeAnalysisService.clearIssues();
    }

}
