package com.github.raschild6.momentumplugin.toolWindows.supports;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CollapsiblePanel extends JPanel {
    private JPanel contentPanel;
    private boolean isExpanded = true;

    public CollapsiblePanel(String title, JPanel content) {
        this.contentPanel = content;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Pannello del titolo (clickabile)
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(JBUI.Borders.empty(10));

        // Crea il titolo (JLabel)
        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JButton toggleButton = new JButton("[-]");
        toggleButton.setBorder(BorderFactory.createEmptyBorder());

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(toggleButton, BorderLayout.EAST);

        titlePanel.setBackground(JBColor.LIGHT_GRAY);
        
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        contentPanel.setVisible(isExpanded);

        // Gestione click per espandere o comprimere
        toggleButton.addActionListener(e -> toggleExpansion(toggleButton));

        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpansion(toggleButton);
            }
        });
    }

    // Metodo per alternare l'espansione e la compressione
    private void toggleExpansion(JButton toggleButton) {
        isExpanded = !isExpanded;
        contentPanel.setVisible(isExpanded);
        toggleButton.setText(isExpanded ? "[-]" : "[+]"); // Cambia il simbolo
        revalidate();
        repaint();
    }
}
