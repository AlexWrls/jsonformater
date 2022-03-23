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
import ru.taa.jsonformater.utils.XmlUtils;

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
    private static final String ROOT = "<hgfs></hgfs>";
    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат DATA_TABLE";

    public ObjectRs convert(String table) {
        try {
            String resultXml = format(FormatUtils.prepareData(table));
            return ObjectRs.builder().txt(XmlUtils.prettyFormatXML(resultXml)).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }
    }

    /**
     * Собрать xml по параметрам params
     *
     * @param params параметры xml(путь и значение)
     * @return строковое представление xml по входным парметрам
     */
    private String format(Map<String, String> params) {
        try {
            Document doc = XmlUtils.stringToDom(ROOT);
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
            return XmlUtils.domToString(doc);
        } catch (Exception e) {
            log.error("Ошибка разбора параметров:\n" + params.toString());
        }
        return DataTableToXmlService.ROOT;
    }

}
