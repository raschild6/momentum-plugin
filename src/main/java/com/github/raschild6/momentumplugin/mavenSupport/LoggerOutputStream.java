package com.github.raschild6.momentumplugin.mavenSupport;

import com.github.raschild6.momentumplugin.managers.LogManager;

import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {
    private LogManager logManager;

    // Constructor that takes the loggerManager
    public LoggerOutputStream(LogManager logManager) {
        this.logManager = logManager;
    }

    // Write a single byte to the logger
    @Override
    public void write(int b) {
        // Convert the byte to a character and log it
        logManager.log(String.valueOf((char) b));
    }

    // Write a byte array to the logger
    @Override
    public void write(byte[] b, int off, int len) {
        // Convert the byte array to a string and log it
        logManager.log(new String(b, off, len));
    }
}
