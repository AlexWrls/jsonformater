package ru.taa.jsonformater.utils;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Document;
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

@UtilityClass
public class XmlUtils {

    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат XML";

    /**
     * Красивое форматирование XML
     *
     * @param message xml в строковом представлении
     * @return форматрованный xml в строковом представлении без кодировки
     */
    public static String prettyFormatXML(String message) {
        try {
            String xmlStr = prepareXml(message);
            StringWriter sw = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(stringToDom(xmlStr)), new StreamResult(sw));
            return normalizeXmlStr(sw.toString());
        } catch (Exception e) {
            return EXCEPT;
        }
    }

    /**
     * Удаляет строку кодировки XML
     *
     * @param message xml в текстовом представлении
     * @return body soap ответа в строковом представлении без преамбулы и начального корня
     */
    private static String normalizeXmlStr(String message) {
        return message.replaceAll("<\\?[^<](.*)\\?>", "")
                .trim();


    }

    /**
     * Подготовка XML к форматированию
     *
     * @param message xml в текстовом представлении
     * @return body soap ответа в строковом представлении без кодировки
     */
    private static String prepareXml(String message) {
        return message.replaceAll("(<|</)hgfs>", "")
                .replaceAll("\\n", "")
                .replaceAll("\\s+", " ")
                .replaceAll("> <", "><");

    }

    /**
     * Конвертирование текста в DOM элемент
     *
     * @param xmlSource строка DOM
     * @return DOM представление строки xmlSource, корневой элемент
     */
    public static Document stringToDom(String xmlSource) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            StringReader reader = new StringReader(xmlSource);
            InputSource source = new InputSource(reader);
            return builder.parse(source);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException("Ошибка конвертирования строки в Document");
        }
    }

    /**
     * Конвертирование DOM элемента в строку
     *
     * @param node элемент DOM
     * @return строковое представление DOM элемента
     */
    public static String domToString(Node node) {
        try {
            StringWriter sw = new StringWriter();
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty(OutputKeys.VERSION, "1.0");
            tf.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
            tf.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка конвертирования Document в объект строка");
        }
    }
}

