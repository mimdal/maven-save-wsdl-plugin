package com.github.mimdal;

import com.github.mimdal.xml.XmlProcess;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;

/**
 * Saves the soap WSDL file and all xsd files. Default lifecycle set to PACKAGE phase.
 *
 * @author mohamad.dehghan
 * @since 3/6/20
 */
@Mojo(name = "save", defaultPhase = LifecyclePhase.PACKAGE)
public class SaveWsdl extends AbstractMojo {

    /**
     * wsdl end-point url
     */
    @Parameter(property = "save.wsdl.url", required = true)
    private String endPoint;

    /**
     * Name of wsdl file, if not set name of url is used.
     */
    @Parameter(property = "save.wsdl.name")
    private String wsdlName;

    /**
     * if set the xsd file names = prefix-number.xsd
     * otherwise, xsd name used exist in url.
     */
    @Parameter(property = "save.wsdl.xsd.prefix", defaultValue = "")
    private String xsdPrefix;

    /**
     * if set, all xsd files save in that directory, otherwise xsd's to be save close wsdl.xml file.
     */
    @Parameter(property = "save.wsdl.xsd.directory")
    private String xsdDirectory;

    /**
     * directory to save files, it's relative to project directory. default is wsdl directory in project resources.
     */
    @Parameter(property = "save.wsdl.relative.save.directory", defaultValue = "src/main/resources/wsdl")
    private String relativeSaveDirectory;

    /**
     * Saves the soap WSDL file and XSDs (XML Schema Definition)
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String projectDirectory = project.getBasedir().getAbsolutePath() + File.separator;
        String path = projectDirectory + relativeSaveDirectory;
        XmlProcess xmlProcess = new XmlProcess(wsdlName, xsdPrefix, path, xsdDirectory, getLog());
        try {
            xmlProcess.saveWsdl(endPoint);
        } catch (DocumentException | IOException e) {
            throw new MojoFailureException("an error take place to wsdl save process. error message: " + e.getMessage()
                    , e);
        }
    }
}
