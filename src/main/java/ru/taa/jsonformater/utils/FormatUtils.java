package ru.taa.jsonformater.utils;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@UtilityClass
public class FormatUtils {

    @SneakyThrows
    public static String normalizeJson(String message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(message);
        parseObj(jsonObject);
        return jsonObject.toString();
    }

    public static void parseObj(JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            final Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                parseObj((JSONObject) value);
            } else if (value instanceof JSONArray) {
                parseArr((JSONArray) value);
            } else {
                if (!(value instanceof String)) {
                    jsonObject.put(key, Objects.isNull(value) ? "null" : value.toString());
                }
            }
        }
    }

    public static void parseArr(JSONArray json) {
        for (int i = 0; i < json.size(); i++) {
            final Object val = json.get(i);
            if (val instanceof JSONObject) {
                parseObj((JSONObject) val);
            } else if (val instanceof JSONArray) {
                parseArr((JSONArray) val);
            } else {
                if (!(val instanceof String)) {
                    json.set(i, Objects.isNull(val) ? "null" : val.toString());
                }
            }
        }
    }

    public static Map<String, String> prepareData(String data) {
        Map<String, String> tables = new LinkedHashMap<>();
        if (data.contains("\n")) {
            String[] lines = data.split("\n");
            for (String line : lines) {
                String[] item = line.split("\\|");
                tables.put(item[1].trim(), item[2].trim());
            }
        }
        return tables;
    }

    public static String prettyFormatXML(String message) {
        try {
            for (Map.Entry<String, String> entry : STRING_MAP.entrySet()) {
                message = message.replaceAll(entry.getKey(), entry.getValue());
            }
            if (message.contains("<?")) {
               message = StringUtils.substringAfter(message,"?>");
            }
            if (message.startsWith("<root>")) {
                message = message.substring(6);
                message = message.substring(0, message.length() - 7);
            }
            Source xmlInput = new StreamSource(new StringReader(message));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            String res = xmlOutput.getWriter().toString();
            int end = res.indexOf("?>");
            return res.substring(end + 2);
        } catch (Exception e) {
            return message;
        }
    }

    private static final Map<String, String> STRING_MAP = ImmutableMap.<String, String>builder()
            .put("\\n", "")
            .put("(\\s)+", " ")
            .put("> <", "><")
            .build();
}
























