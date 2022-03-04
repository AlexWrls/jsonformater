package ru.taa.jsonformater.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class XmlToDataTableService {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("(.*)(\\[([0-9]+)])");

    private static final String RES = "<phonebook>\n" +
            "    <person>\n" +
            "        <name>Остап Бендер</name>\n" +
            "        <email>ostap@12.com</email>\n" +
            "        <phone>999-987-6543</phone>\n" +
            "    </person>\n" +
            "    <person>\n" +
            "        <name>Киса Воробьянинов</name>\n" +
            "        <email>kisa@12.com</email>\n" +
            "        <phone>999-986-5432</phone>\n" +
            "    </person>\n" +
            "    <person>\n" +
            "        <name>Мадам Грицацуева</name>\n" +
            "        <email>madam@12.com</email>\n" +
            "        <phone>999-985-4321</phone>\n" +
            "    </person>\n" +
            "</phonebook>";

    public static void main(String[] args) {
        new XmlToDataTableService().format(RES);
    }

    public void format(String message) {
        Document doc = stringToDom(message.replaceAll("\\s|\\n", ""));
        Map<String, String> map = new LinkedHashMap<>();
        List<String> path = new ArrayList<>();
        Node node = doc.getFirstChild();
        parse(node, map, path, 0);
        map.forEach((k, v) -> System.out.println("| " + k + " | " + v + " |"));
    }

    private void parse(Node node, Map<String, String> map, List<String> path, int idx) {
        if (node.getNodeType() == Node.ELEMENT_NODE && node.getChildNodes().getLength() == 1) {
            StringBuilder sb = new StringBuilder();
            if (!path.isEmpty()) {
                for (int i = 0; i < path.size(); i++) {
                    sb.append(path.get(i)).append(".");
                }
            }
            sb.append(node.getNodeName()).append("[").append(idx).append("]");
            map.put(sb.toString(), node.getTextContent());
        } else {
            path.add(node.getNodeName() + "[" + idx + "]");
            correctPath(path);
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node item = node.getChildNodes().item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    parse(item, map, path, i);
                }
            }
        }
    }

    private void correctPath(List<String> path) {
        if (path.size() > 1) {
            for (int i = 0; i < path.size(); i++) {
                for (int j = 0; j < path.size(); j++) {
                    if (i != j) {
                        String path1 = path.get(i);
                        String path2 = path.get(j);
                        Matcher matcher1 = ARRAY_PATTERN.matcher(path1);
                        if (matcher1.find()) {
                            path1 = matcher1.group(1);
                            int index1 = Integer.parseInt(matcher1.group(3));
                            Matcher matcher2 = ARRAY_PATTERN.matcher(path2);
                            if (matcher2.find()) {
                                path2 = matcher2.group(1);
                                if (path1.equals(path2)) {
                                    path.set(i, path1 + "[" + (index1 + 1) + "]");
                                    path.subList(i + 1, path.size()).clear();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Document stringToDom(String xmlSource) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(xmlSource);
            InputSource source = new InputSource(reader);
            return builder.parse(source);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException("Error parsing String to Document", e);
        }
    }

}
