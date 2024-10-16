package com.github.raschild6.momentumplugin.listeners;

import com.github.raschild6.momentumplugin.services.CodeAnalysisService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;


public class CodeChangeListener implements BulkFileListener {


    public CodeChangeListener() {}

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {}

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        for (VFileEvent event : events) {
            VirtualFile eventFile = event.getFile();

            if (eventFile != null && !eventFile.isDirectory()) {
                for (Project project : projects) {
                    CodeAnalysisService service = project.getService(CodeAnalysisService.class);
                    service.runCodeAnalysis(project);
                }
            }
        }
    }

}
