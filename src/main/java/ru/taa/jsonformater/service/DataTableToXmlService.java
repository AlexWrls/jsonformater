package ru.taa.jsonformater.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.utils.FormatUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис конвертирования Data table в Json
 */
@Service
@Slf4j
public class DataTableToXmlService {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");
    private static final String ATTRIBUTE = "@";
    private static final String ROOT = "<root></root>";
    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат DATA_TABLE";

    public ObjectRs convert(String table) {
        try {
            String resultXml = format(FormatUtils.prepareData(table));
            return ObjectRs.builder().txt(FormatUtils.prettyFormatXML(resultXml)).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }
    }

    private String format(Map<String, String> params) {
        try {
            Document doc = stringToDom();
            params.forEach((k, v) -> {
                Node node = doc.getFirstChild();
                String[] paths = k.split("\\.");
                for (int i = 0; i < paths.length; i++) {
                    // Получить название и индекс нужного элемента
                    String elementName = paths[i];
                    int indexElement = 0;
                    Matcher matcher = ARRAY_PATTERN.matcher(elementName);
                    if (matcher.find()) {
                        elementName = matcher.group(1);
                        indexElement = Integer.parseInt(matcher.group(2));
                    }
                    Element element = (Element) node;
                    if (elementName.startsWith(ATTRIBUTE)) {
                        element.setAttribute(elementName.substring(1), v);
                        continue;
                    }
                    // Получаем элемент по названию и индексу
                    Node item = element.getElementsByTagName(elementName).item(indexElement);
                    // Если не найден то создадим
                    if (Objects.isNull(item)) {
                        item = element.getOwnerDocument().createElement(elementName);
                        element.appendChild(item);
                    }
                    node = item;
                    // Если последний элемент в массиве paths то установим значение или удалим
                    if (i == paths.length - 1) {
                        if (Objects.isNull(v)) {
                            element.removeChild(item);
                        } else {
                            item.setTextContent(v);
                        }
                    }
                }
            });
            return domToString(doc);
        } catch (Exception e) {
            log.error("Ошибка разбора параметров:\n" + params.toString());
        }
        return DataTableToXmlService.ROOT;
    }

    //Конвертирование текста в DOM элемент
    private Document stringToDom() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(DataTableToXmlService.ROOT);
            InputSource source = new InputSource(reader);
            return builder.parse(source);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Ошибка конвертации строки в Document");
            throw new RuntimeException("Error parsing String to Document");
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
            log.error("Ошибка конвертицм Document в строку");
            throw new RuntimeException("Error converting Document to String");
        }
    }
}
