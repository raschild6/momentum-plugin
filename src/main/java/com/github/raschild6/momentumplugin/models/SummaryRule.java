package com.github.raschild6.momentumplugin.models;

public class SummaryRule {
    private String key;
    private String name;
    private String severity;
    private String status;
    private String type;
    private boolean template;

    public SummaryRule(String key, String name, String severity, String status, String type, boolean template) {
        this.key = key;
        this.name = name;
        this.severity = severity;
        this.status = status;
        this.type = type;
        this.template = template;
    }

    public SummaryRule(){}

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "SummaryRule{" +
            "key='" + key + '\'' +
            ", name='" + name + '\'' +
            ", severity='" + severity + '\'' +
            ", status='" + status + '\'' +
            ", type='" + type + '\'' +
            ", template=" + template +
            '}';
    }
}
