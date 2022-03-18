package ru.taa.jsonformater.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.taa.jsonformater.dto.ObjectRs;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Сервис конвертирования Xml в Data table
 */
@Service
public class XmlToDataTableService {


    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат XML";
    private static final String ATTRIBUTE = "@";

    public ObjectRs convert(String xmlString) {
        try {
            String input = xmlString.replaceAll("\\n", "");
            input = input.replaceAll("(\\s)+", " ");
            input = input.replaceAll(">(\\s)+<", "><");
            String root = String.format("<root>%s</root>",input);
            Document doc = stringToDom(root);
            final Node node = doc.getFirstChild();
            Map<String, String> map = new LinkedHashMap<>();
            List<String> path = new ArrayList<>();
            parse(node, path, map, new LinkedList<>());
            StringBuilder sb = new StringBuilder();
            map.forEach((k, v) -> {
                sb.append("| ").append(k).append(" | ").append(v).append(" |").append("\n");
            });
            return ObjectRs.builder().txt(sb.toString()).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }

    }


    private static void parse(Node node, List<String> path, Map<String, String> map, Queue<Integer> queue) {
        final NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node item = nodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (queue.isEmpty()) {
                    String thisName = item.getNodeName();
                    int count = 0;
                    for (int j = i; j < nodes.getLength(); j++) {
                        if (thisName.equals(nodes.item(j).getNodeName())) {
                            queue.add(count);
                            count++;
                        }
                    }
                    if (queue.size() != 1) {
                        path.add(thisName + "[" + queue.poll() + "].");
                    } else {
                        queue.poll();
                        path.add(thisName + ".");
                    }
                } else {
                    path.add(item.getNodeName() + "[" + queue.poll() + "].");
                }

                if (item.getAttributes().getLength() != 0) {
                    for (int j = 0; j < item.getAttributes().getLength(); j++) {
                        final Node atr = item.getAttributes().item(j);
                        path.add(ATTRIBUTE + atr.getNodeName() + ".");
                        map.put(pathToStr(path), atr.getNodeValue());
                        path.remove(path.size() - 1);
                    }
                }
                if (item.getChildNodes().item(0).getChildNodes().getLength() == 0) {
                    map.put(pathToStr(path), item.getTextContent());
                    path.remove(path.size() - 1);
                } else {
                    parse(item, path, map, new LinkedList<>());
                    path.remove(path.size() - 1);
                }
            }
        }
    }

    private static String pathToStr(List<String> path) {
        StringBuilder sb = new StringBuilder();
        path.forEach(sb::append);
        sb.deleteCharAt(sb.lastIndexOf("."));
        return sb.toString();
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
