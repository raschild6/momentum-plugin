package com.github.raschild6.momentumplugin.managers;

import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;

import java.util.List;

@ServerSide
public class RuleManager {
    private static final Logger logger = LoggerFactory.getLogger(RuleManager.class);

    private final LogManager logManager;

    public RuleManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public void addCustomRule(String ruleKey, String description) {
        // Logica per aggiungere una regola personalizzata
        this.logManager.log("Aggiunta regola personalizzata: " + ruleKey);
        // Implementazione reale per aggiungere la regola al sistema
    }

    public List<RulesDefinition.Rule> getAllRules() {
        // Logica per recuperare tutte le regole
        this.logManager.log("Recuperate tutte le regole");
        // Implementazione reale per recuperare tutte le regole
        return List.of();
    }
}
