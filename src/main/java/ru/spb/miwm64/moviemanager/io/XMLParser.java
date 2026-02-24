package ru.spb.miwm64.moviemanager.io;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;


import org.w3c.dom.Document;
import ru.spb.miwm64.moviemanager.entities.*;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    /**
     * Parses an XML string containing a collection of movies
     * Format: <movies><movie>...</movie><movie>...</movie></movies>
     */
    public ArrayList<Movie> parseFromXMLCollection(String xml) {
        ArrayList<Movie> movies = new ArrayList<>();

        // Extract all movie tags
        Pattern moviePattern = Pattern.compile("<movie>(.*?)</movie>", Pattern.DOTALL);
        Matcher movieMatcher = moviePattern.matcher(xml);

        while (movieMatcher.find()) {
            String movieXml = movieMatcher.group(1);
            try {
                Movie movie = parseSingleMovie(movieXml);
                movies.add(movie);
            } catch (Exception e) {
                throw new InvalidValueException("Failed to parse movie: " + e.getMessage());
            }
        }

        return movies;
    }

    /**
     * Parses a single movie XML into a Movie object
     */
    private Movie parseSingleMovie(String movieXml) {
        // Extract fields using helper method
        Long id = Long.parseLong(extractTag(movieXml, "id"));
        String name = extractTag(movieXml, "name");
        Float coordX = Float.parseFloat(extractTag(movieXml, "coordX"));
        Long coordY = Long.parseLong(extractTag(movieXml, "coordY"));
        Integer oscarsCount = Integer.parseInt(extractTag(movieXml, "oscarsCount"));
        Long goldenPalmCount = Long.parseLong(extractTag(movieXml, "goldenPalmCount"));
        java.time.ZonedDateTime zonedDateTime = ZonedDateTime.parse(extractTag(movieXml, "creationDate"));
        String genreStr = extractTagOptional(movieXml, "genre");
        String mpaaStr = extractTag(movieXml, "mpaaRating");

        // Optional operator fields
        String operatorName = extractTagOptional(movieXml, "operatorName");
        String operatorWeightStr = extractTagOptional(movieXml, "operatorWeight");
        String hairColorStr = extractTagOptional(movieXml, "hairColor");
        String nationalityStr = extractTagOptional(movieXml, "nationality");

        // Build objects
        Coordinates coords = new Coordinates(coordX, coordY);

        MovieGenre genre = null;
        if (genreStr != null && !genreStr.isEmpty()) {
            genre = MovieGenre.valueOf(genreStr);
        }

        MpaaRating mpaaRating = MpaaRating.valueOf(mpaaStr);

        Person operator = null;
        if (operatorName != null && !operatorName.isEmpty()) {
            Float weight = operatorWeightStr != null ? Float.parseFloat(operatorWeightStr) : null;
            Color hairColor = hairColorStr != null ? Color.valueOf(hairColorStr) : null;
            Country nationality = nationalityStr != null ? Country.valueOf(nationalityStr) : null;

            operator = new Person(operatorName, weight, hairColor, nationality);
        }

        // Create movie (id and creationDate will be set by collection manager)
        return new Movie(
                id,
                name,
                coords,
                zonedDateTime,
                oscarsCount,
                goldenPalmCount,
                genre,
                mpaaRating,
                operator
        );
    }

    /**
     * Converts a list of movies to XML format
     */
    public String parseCollectionIntoXML(ArrayList<Movie> movies) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<movies>\n");

        for (Movie movie : movies) {
            xml.append("  ").append(movieToXML(movie)).append("\n");
        }

        xml.append("</movies>");
        return xml.toString();
    }

    /**
     * Converts a single movie to XML string
     */
    private String movieToXML(Movie movie) {
        StringBuilder xml = new StringBuilder();
        xml.append("<movie>\n");

        // Required fields
        xml.append("    <id>").append(movie.getId()).append("</id>\n");
        xml.append("    <name>").append(escapeXML(movie.getName())).append("</name>\n");
        xml.append("    <coordX>").append(movie.getCoordinates().getX()).append("</coordX>\n");
        xml.append("    <coordY>").append(movie.getCoordinates().getY()).append("</coordY>\n");
        xml.append("    <creationDate>").append(movie.getCreationDate().format(DATE_FORMATTER)).append("</creationDate>\n");
        xml.append("    <oscarsCount>").append(movie.getOscarsCount()).append("</oscarsCount>\n");
        xml.append("    <goldenPalmCount>").append(movie.getGoldenPalmCount()).append("</goldenPalmCount>\n");

        // Optional fields
        if (movie.getGenre() != null) {
            xml.append("    <genre>").append(movie.getGenre()).append("</genre>\n");
        }

        xml.append("    <mpaaRating>").append(movie.getMpaaRating()).append("</mpaaRating>\n");

        // Operator fields
        if (movie.getOperator() != null) {
            Person op = movie.getOperator();
            xml.append("    <operatorName>").append(escapeXML(op.getName())).append("</operatorName>\n");
            xml.append("    <operatorWeight>").append(op.getWeight()).append("</operatorWeight>\n");
            xml.append("    <hairColor>").append(op.getHairColor()).append("</hairColor>\n");
            xml.append("    <nationality>").append(op.getNationality()).append("</nationality>\n");
        }

        xml.append("  </movie>");
        return xml.toString();
    }

    /**
     * Helper: Extract required tag content
     */
    private String extractTag(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        throw new InvalidValueException("Missing required tag: " + tagName);
    }

    /**
     * Helper: Extract optional tag content (returns null if not found)
     */
    private String extractTagOptional(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Helper: Escape XML special characters
     */
    private String escapeXML(String text) {
        if (text == null) return null;
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

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