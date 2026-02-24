package ru.spb.miwm64.moviemanager.io;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;

public class XMLParser {

    public Map<String, String> parse(String xmlSource) throws Exception {
        Map<String, String> map = new HashMap<>();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(xmlSource.trim().startsWith("<") ?
                        new ByteArrayInputStream(xmlSource.getBytes()) :
                        new FileInputStream(xmlSource));

        traverse(doc.getDocumentElement(), map);

        return map;
    }

    private void traverse(Element elem, Map<String, String> map) {
        NodeList children = elem.getChildNodes();
        boolean hasChildElements = false;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                hasChildElements = true;
                traverse((Element) child, map);
            }
        }

        if (!hasChildElements && !elem.getTextContent().trim().isEmpty()) {
            map.put(elem.getTagName(), elem.getTextContent().trim());
        }
    }

}