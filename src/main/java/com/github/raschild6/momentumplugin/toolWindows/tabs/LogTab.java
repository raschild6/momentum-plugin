package com.github.raschild6.momentumplugin.toolWindows.tabs;

import com.github.raschild6.momentumplugin.managers.LogManager;

import javax.swing.*;

public class LogTab {

    private LogManager logManager;

    public LogTab(LogManager logManager) {
        this.logManager = logManager;
    }

    public JPanel createLogTab() {
        return logManager.initLogTab();
    }
}
