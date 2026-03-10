package ru.spb.miwm64.moviemanager.io;

import org.w3c.dom.*;
import ru.spb.miwm64.moviemanager.entities.*;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public ArrayList<Movie> parseFromXMLCollection(String xml) {
        ArrayList<Movie> movies = new ArrayList<>();
        try {
            Pattern moviePattern = Pattern.compile("<movie>(.*?)</movie>", Pattern.DOTALL);
            Matcher movieMatcher = moviePattern.matcher(xml);
            while (movieMatcher.find()) {
                String movieXml = movieMatcher.group(1);
                try {
                    Movie movie = parseSingleMovie(movieXml);
                    movies.add(movie);
                } catch (Exception e) {
                    //throw new InvalidValueException("Failed to parse movie: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new InvalidValueException("Failed to parse XML collection: " + e.getMessage());
        }
        return movies;
    }

    private Movie parseSingleMovie(String movieXml) {
        try {
            Long id = Long.parseLong(extractTag(movieXml, "id"));
            String name = extractTag(movieXml, "name");
            Float coordX = Float.parseFloat(extractTag(movieXml, "coordX"));
            Long coordY = Long.parseLong(extractTag(movieXml, "coordY"));
            Integer oscarsCount = Integer.parseInt(extractTag(movieXml, "oscarsCount"));
            Long goldenPalmCount = Long.parseLong(extractTag(movieXml, "goldenPalmCount"));
            ZonedDateTime creationDate = ZonedDateTime.parse(extractTag(movieXml, "creationDate"), DATE_FORMATTER);
            String genreStr = extractTagOptional(movieXml, "genre");
            String mpaaStr = extractTag(movieXml, "mpaaRating");

            String operatorName = extractTagOptional(movieXml, "operatorName");
            String operatorWeightStr = extractTagOptional(movieXml, "operatorWeight");
            String hairColorStr = extractTagOptional(movieXml, "hairColor");
            String nationalityStr = extractTagOptional(movieXml, "nationality");

            Coordinates coords = new Coordinates(coordX, coordY);
            MovieGenre genre = null;
            if (genreStr != null && !genreStr.isEmpty()) {
                genre = MovieGenre.fromString(genreStr.toUpperCase());
            }
            MpaaRating mpaaRating = MpaaRating.fromString(mpaaStr.toUpperCase());
            Person operator = null;
            if (operatorName != null && !operatorName.isEmpty()) {
                Float weight = operatorWeightStr != null ? Float.parseFloat(operatorWeightStr) : null;
                Color hairColor = hairColorStr != null ? Color.fromString(hairColorStr.toUpperCase()) : null;
                Country nationality = nationalityStr != null ? Country.fromString(nationalityStr.toUpperCase()) : null;
                operator = new Person(operatorName, weight, hairColor, nationality);
            }
            return new Movie(id, name, coords, creationDate, oscarsCount, goldenPalmCount, genre, mpaaRating, operator);
        } catch (Exception e) {
            throw new InvalidValueException("Error parsing movie XML: " + e.getMessage());
        }
    }

    public String parseCollectionIntoXML(ArrayList<Movie> movies) {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<movies>\n");
            for (Movie movie : movies) {
                xml.append("  ").append(movieToXML(movie)).append("\n");
            }
            xml.append("</movies>");
            return xml.toString();
        } catch (Exception e) {
            throw new InvalidValueException("Failed to convert collection to XML: " + e.getMessage());
        }
    }

    private String movieToXML(Movie movie) {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<movie>\n");
            xml.append("    <id>").append(movie.getId()).append("</id>\n");
            xml.append("    <name>").append(escapeXML(movie.getName())).append("</name>\n");
            xml.append("    <coordX>").append(movie.getCoordinates().getX()).append("</coordX>\n");
            xml.append("    <coordY>").append(movie.getCoordinates().getY()).append("</coordY>\n");
            xml.append("    <creationDate>").append(movie.getCreationDate().format(DATE_FORMATTER)).append("</creationDate>\n");
            xml.append("    <oscarsCount>").append(movie.getOscarsCount()).append("</oscarsCount>\n");
            xml.append("    <goldenPalmCount>").append(movie.getGoldenPalmCount()).append("</goldenPalmCount>\n");
            if (movie.getGenre() != null) {
                xml.append("    <genre>").append(movie.getGenre()).append("</genre>\n");
            }
            xml.append("    <mpaaRating>").append(movie.getMpaaRating()).append("</mpaaRating>\n");
            if (movie.getOperator() != null) {
                Person op = movie.getOperator();
                xml.append("    <operatorName>").append(escapeXML(op.getName())).append("</operatorName>\n");
                xml.append("    <operatorWeight>").append(op.getWeight()).append("</operatorWeight>\n");
                xml.append("    <hairColor>").append(op.getHairColor()).append("</hairColor>\n");
                xml.append("    <nationality>").append(op.getNationality()).append("</nationality>\n");
            }
            xml.append("  </movie>");
            return xml.toString();
        } catch (Exception e) {
            throw new InvalidValueException("Error converting movie to XML: " + e.getMessage());
        }
    }

    private String extractTag(String xml, String tagName) {
        try {
            Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            throw new InvalidValueException("Missing required tag: " + tagName);
        } catch (Exception e) {
            throw new InvalidValueException("Error extracting tag " + tagName + ": " + e.getMessage());
        }
    }

    private String extractTagOptional(String xml, String tagName) {
        try {
            Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String escapeXML(String text) {
        if (text == null) return null;
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public Map<String, String> parse(String xmlSource) {
        Map<String, String> map = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                public void warning(org.xml.sax.SAXParseException e) {}
                public void error(org.xml.sax.SAXParseException e) {}
                public void fatalError(org.xml.sax.SAXParseException e) {}
            });
            Document doc = builder.parse(xmlSource.trim().startsWith("<") ?
                    new ByteArrayInputStream(xmlSource.getBytes()) :
                    new FileInputStream(xmlSource));
            traverse(doc.getDocumentElement(), map);
        } catch (Exception e) {
            throw new InvalidValueException("Failed to parse XML: " + e.getMessage());
        }
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