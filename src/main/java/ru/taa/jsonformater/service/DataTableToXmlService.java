package ru.taa.jsonformater.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DataTableToXmlService  {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");


    public String format(String content, Map<String, String> params) {
        try {
            Document doc = stringToDom(content);
            AtomicReference<Node> node = new AtomicReference<>(doc.getFirstChild());
            params.forEach((k, v) -> {
                String[] paths = k.split("\\.");
                for (int i = 0; i < paths.length; i++) {
                    Matcher matcher = ARRAY_PATTERN.matcher(paths[i]);
                    if (!matcher.find()) {
                        paths[i] += "[0]";
                        matcher = ARRAY_PATTERN.matcher(paths[i]);
                        matcher.find();
                    }
                    // Получить название и индекс нужного элемента
                    String elementName = matcher.group(1);
                    int indexElement = Integer.parseInt(matcher.group(3));

                    Element element = (Element) node.get();
                    // Получаем элемент по названию и индексу
                    Node item = element.getElementsByTagName(elementName).item(indexElement);
                    // Если не найден то создадим
                    if (Objects.isNull(item)) {
                        item = element.getOwnerDocument().createElement(elementName);
                        element.appendChild(item);
                    }
                    node.set(item);
                    // Если последний элемент в массиве paths то установим значение или удалим
                    if (i == paths.length - 1) {
                        if (Objects.isNull(v)) {
                            element.removeChild(item);
                        } else {
                            item.setTextContent(v);
                        }
                        node.set(doc.getFirstChild());
                    }
                }
            });
            return domToString(doc);
        } catch (Exception e) {
            log.error("Error parsing xml", e);
        }
        return content;
    }

    //Конвертирование текста в DOM элемент
    private static Document stringToDom(String xmlSource) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(xmlSource);
            InputSource source = new InputSource(reader);
            return builder.parse(source);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error parsing String to Document", e);
            throw new RuntimeException("Error parsing String to Document", e);
        }
    }

    //Конвертирование DOM элемента в строку
    private static String domToString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            log.error("Error converting Document to String", e);
            throw new RuntimeException("Error converting Document to String", e);
        }
    }
}

