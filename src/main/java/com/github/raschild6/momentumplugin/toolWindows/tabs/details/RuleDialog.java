package com.github.raschild6.momentumplugin.toolWindows.tabs.details;

import com.github.raschild6.momentumplugin.managers.RuleManager;
import com.intellij.openapi.ui.ComboBox;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import javax.swing.*;
import java.awt.*;

public class RuleDialog extends JDialog {
    private final Rule rule;
    private final RuleManager ruleManager;

    public RuleDialog(Rule rule, RuleManager ruleManager) {
        this.rule = rule;
        boolean isNewRule = (rule == null);
        this.ruleManager = ruleManager;

        setTitle(rule == null ? "Aggiungi Regola" : "Modifica Regola");
        setModal(true);
        setSize(400, 300);

        // UI per modificare i campi della regola
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField keyField = new JTextField(isNewRule ? "" : (rule.key() != null ? rule.key() : ""));
        JTextField nameField = new JTextField(isNewRule ? "" : (rule.name() != null ? rule.name() : ""));
        ComboBox<String> severityBox = isNewRule?
            new ComboBox<>() :
            new ComboBox<>(new DefaultComboBoxModel<>(
            rule.defaultImpacts().keySet().toArray(new String[0])
        ));
//        severityBox.setSelectedItem(isNewRule ? null : rule.severity());
        JTextField statusField = new JTextField(isNewRule ? "" : (rule.status() != null ? rule.status().toString() : ""));


        panel.add(new JLabel("Key:"));
        panel.add(keyField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Severity:"));
        panel.add(severityBox);
        panel.add(new JLabel("Status:"));
        panel.add(statusField);

        this.add(panel, BorderLayout.CENTER);

        // Pulsanti
        JButton saveButton = new JButton("Salva");
        saveButton.addActionListener(e -> {
//            rule.setKey(keyField.getText());
//            rule.setName(nameField.getText());
//            rule.setSeverity((String) severityBox.getSelectedItem());
//            rule.setStatus(statusField.getText());
//
//            if (rule.key().isEmpty()) {
//                ruleManager.addRule(rule);
//            } else {
//                ruleManager.updateRule(rule);
//            }
            dispose();
        });

        JButton cancelButton = new JButton("Annulla");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
}
