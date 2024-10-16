package com.github.raschild6.momentumplugin.models;

import java.nio.file.Path;

public class Issue {
        private long id;
        private String description;
        private Severity severity;
        private Path file;
        private int line;
        private int column;

        public Issue(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Severity getSeverity() {
            return severity;
        }

        public void setSeverity(Severity severity) {
            this.severity = severity;
        }

        public Path getFile() {
            return file;
        }

        public void setFile(Path file) {
            this.file = file;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }