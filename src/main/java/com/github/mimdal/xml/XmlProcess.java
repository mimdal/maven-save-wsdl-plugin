package com.github.mimdal.xml;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author mohamad.dehghan
 * @since 3/6/20
 */
public class XmlProcess {
    private static final String ATTRIBUTE_NAME = "schemaLocation";
    private Document document;
    private static int increment;

    private Map<String, String> map;
    private Queue<String> queue;

    private String wsdlName;
    private String xsdPrefix;
    private String path;
    private String xsdDirectory;
    private Log logger;

    public XmlProcess(String wsdlName, String xsdPrefix, String path, String xsdDirectory, Log logger) {
        this.wsdlName = wsdlName;
        this.xsdPrefix = xsdPrefix;
        this.path = path;
        this.xsdDirectory = xsdDirectory;
        this.logger = logger;

        map = new HashMap<>();
        queue = new LinkedList<>();
    }

    public void saveWsdl(String wsdlEndPoint) throws IOException, DocumentException {
        logger.debug("wsdl End Point = " + wsdlEndPoint);
        logger.debug("wsdl Name = " + wsdlName);
        logger.debug("xsd Prefix = " + xsdPrefix);
        logger.debug("path = " + path);
        logger.debug("xsd Directory = " + xsdDirectory);
        URL url = new URL(wsdlEndPoint);
        InputStream inputStream = url.openStream();
        if (wsdlName == null) {
            wsdlName = getEndPointFileName(wsdlEndPoint);
        }
        saveEndPoint(inputStream, path, wsdlName, false);
        InputStream xsdInputStream;
        String currentXsdUrl;
        String currentXsdFileName;
        while (queue.peek() != null) {
            currentXsdUrl = queue.poll();
            xsdInputStream = getStream(currentXsdUrl);
            currentXsdFileName = map.get(currentXsdUrl);
            String xsdPath = xsdDirectory == null ? path : path + File.separator + xsdDirectory;
            saveEndPoint(xsdInputStream, xsdPath, currentXsdFileName, true);
        }
    }

    private void saveEndPoint(InputStream inputStream, String path, String fileName, boolean isXsd)
            throws IOException, DocumentException {
        document = new SAXReader().read(inputStream);
        List<Node> xsdNodes = getNodesByAttribute();
        processNodes(xsdNodes, isXsd);
        saveAsFile(path, fileName);
    }

    private List<Node> getNodesByAttribute() {
        String XPATH_ATTRIBUTE_PREFIX = "//*[@";
        String XPATH_ATTRIBUTE_POSTFIX = "]";
        final String attributeXpath = XPATH_ATTRIBUTE_PREFIX + ATTRIBUTE_NAME + XPATH_ATTRIBUTE_POSTFIX;
        return document.selectNodes(attributeXpath);
    }

    private InputStream getStream(String endPoint) throws IOException {
        URL url = new URL(endPoint);
        return url.openStream();
    }

    private void saveAsFile(String directory, String fileName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        final String filePathAndName = directory + File.separator + fileName;
        final XMLWriter writer = new XMLWriter(new FileWriter(filePathAndName), OutputFormat.createPrettyPrint());
        writer.write(document);
        writer.close();
        logger.info(filePathAndName + " was saved.");
    }

    private void processNodes(List<Node> xsdNodes, boolean isXsd) throws MalformedURLException {
        final String DASH_SIGN = "-";
        final String XSD_FORMAT = ".xsd";
        for (Node node : xsdNodes) {
            Element element = (Element) node;
            String xsdUrl = getXsdUrl(element);
            String xsdFileName;
            int number;
            if (!map.containsKey(xsdUrl)) {
                number = getNumber();
                if (xsdPrefix == null) xsdFileName = getEndPointFileName(xsdUrl);
                else xsdFileName = xsdPrefix + DASH_SIGN + number + XSD_FORMAT;
                logger.debug("xsd file (" + number + ") in url (" + xsdUrl + ") is processing to save.");
                addToMapAndQueue(xsdUrl, xsdFileName);
            }
            changeSchemaLocationToLocal((Element) node, isXsd);
        }
    }

    private int getNumber() {
        return ++increment;
    }

    private void addToMapAndQueue(String xsdUrl, String xsdFileName) {
        queue.add(xsdUrl);
        map.put(xsdUrl, xsdFileName);
        logger.debug("xsd file (" + xsdFileName + ") in url (" + xsdUrl + ") was add to map and queue.");
    }

    private String getEndPointFileName(String xsdUrl) throws MalformedURLException {
        URL url = new URL(xsdUrl);
        return FilenameUtils.getName(url.getPath());
    }

    private void changeSchemaLocationToLocal(Element node, boolean isXsd) throws MalformedURLException {
        final String SLASH_SIGN = "/";
        String schemaLocation = getXsdUrl(node);
        URL url = new URL(schemaLocation);
        String localName = xsdPrefix == null ? FilenameUtils.getName(url.getPath()) : map.get(schemaLocation);
        if (xsdDirectory != null && !isXsd) {
            localName = xsdDirectory + SLASH_SIGN + localName;
        }
        node.addAttribute(ATTRIBUTE_NAME, localName);
        logger.debug("the process of change schemaLocation value was done. "
                + schemaLocation + " was changed to " + localName);
    }

    private String getXsdUrl(Element node) {
        return node.attributeValue(ATTRIBUTE_NAME);
    }
}

