package com.github.raschild6.momentumplugin.toolWindows.tabs;

import com.github.raschild6.momentumplugin.managers.LogManager;
import com.github.raschild6.momentumplugin.managers.RuleManager;
import com.github.raschild6.momentumplugin.toolWindows.tabs.details.RuleDialog;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RuleTab extends JPanel {
    private final RuleManager ruleManager;
    private final LogManager logManager;

    private final JTable rulesTable;
    private final DefaultTableModel tableModel;

    public RuleTab(RuleManager ruleManager, LogManager logManager) {
        this.ruleManager = ruleManager;
        this.logManager = logManager;

        this.setLayout(new BorderLayout());

        // Colonne della tabella basate sulla struttura della classe Rule
        String[] columnNames = {"Key", "Name", "Severity", "Status"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.rulesTable = new JTable(tableModel);

        // Caricamento iniziale delle regole
        loadRules();

        // Listener per il doppio click su una riga
        rulesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = rulesTable.rowAtPoint(e.getPoint());
                    if (row == tableModel.getRowCount() - 1) {
                        // Riga vuota: aggiunta nuova regola
                        openRuleDialog(null);
                    } else {
                        // Riga esistente: modifica regola
                        Rule rule = getRuleFromRow(row);
                        openRuleDialog(rule);
                    }
                }
            }
        });

        // Barra di scorrimento per la tabella
        JScrollPane scrollPane = new JScrollPane(rulesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        // Pulsante di import
        JButton importButton = new JButton("Importa regole da profilo");
//        importButton.addActionListener(e -> openImportDialog());
        this.add(importButton, BorderLayout.SOUTH);
    }

    private void loadRules() {
        tableModel.setRowCount(0); // Resetta i dati
        List<Rule> rules = ruleManager.getAllRules(); // Recupera le regole dal RuleManager
        for (Rule rule : rules) {
            tableModel.addRow(new Object[]{
                rule.key(),          // Key della regola
                rule.name(),         // Nome della regola
                rule.severity(),     // Severità
                rule.status()        // Stato della regola
            });
        }
        // Aggiungi una riga vuota per l'inserimento di nuove regole
        tableModel.addRow(new Object[]{"", "", "", ""});
    }

    private Rule getRuleFromRow(int row) {
        // Crea un oggetto Rule partendo dai dati della riga
        return null;
//            new Rule(
//            (String) tableModel.getValueAt(row, 0), // Key
//            (String) tableModel.getValueAt(row, 1), // Name
//            (String) tableModel.getValueAt(row, 2), // Severity
//            (String) tableModel.getValueAt(row, 3)  // Status
//        );
    }

    private void openRuleDialog(Rule rule) {
        // Passa la regola al dialog, per modifiche o per una nuova regola (null indica nuova regola)
        RuleDialog dialog = new RuleDialog(rule, ruleManager);
        dialog.setVisible(true);

        // Ricarica le regole dopo la chiusura del dialog
        loadRules();
    }
//
//    private void openImportDialog() {
//        // ImportDialog per gestire i profili Sonar
//        ImportDialog dialog = new ImportDialog(ruleManager);
//        dialog.setVisible(true);
//
//        // Ricarica le regole dopo l'importazione
//        loadRules();
//    }
}
