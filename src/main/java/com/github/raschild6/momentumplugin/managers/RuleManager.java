package com.github.raschild6.momentumplugin.managers;

import com.github.raschild6.momentumplugin.models.SummaryRule;
import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@ServerSide
public class RuleManager {
    private static final Logger logger = LoggerFactory.getLogger(RuleManager.class);

    private final LogManager logManager;

    public RuleManager(LogManager logManager) {
        this.logManager = logManager;
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
        logger.info("Creating new rule");
    }

}
