package com.github.raschild6.momentumplugin;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.PsiErrorElementUtil;
import com.github.raschild6.momentumplugin.services.MyProjectService;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/src/test/testData")
public class MyPluginTest extends BasePlatformTestCase {

    public void testXMLFile() {
        var psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>");
        XmlFile xmlFile = assertInstanceOf(psiFile, XmlFile.class);

        assertFalse(PsiErrorElementUtil.hasErrors(getProject(), xmlFile.getVirtualFile()));

        assertNotNull(xmlFile.getRootTag());

        if (xmlFile.getRootTag() != null) {
            assertEquals("foo", xmlFile.getRootTag().getName());
            assertEquals("bar", xmlFile.getRootTag().getValue().getText());
        }
    }

    public void testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2");
    }

    public void testProjectService() {
        MyProjectService projectService = ServiceManager.getService(getProject(), MyProjectService.class);

        assertNotNull(projectService.runCodeAnalysis());
    }

    @NotNull
    @Override
    public String getTestDataPath() {
        return "src/test/testData/rename";
    }
}
