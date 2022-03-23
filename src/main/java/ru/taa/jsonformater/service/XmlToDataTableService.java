package ru.taa.jsonformater.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.utils.XmlUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис конвертирования Xml в Data table
 */
@Service
public class XmlToDataTableService {
    private static final String ATTRIBUTE = "@";
    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат XML";
    private static final Pattern NAME_SPACE_PATTERN = Pattern.compile("^(.*)[:](.*)$");
    private static final String ROOT = "<hgfs>%s</hgfs>";

    /**
     * Обрезает неймспейс у XML
     *
     * @param xmlString    строковое представление xml
     * @param cutNameSpace обрезать неймспейс у утегов xml
     * @return объект обвета с форматированныи xml в Data table
     */
    public ObjectRs convert(String xmlString, boolean cutNameSpace) {
        try {
            String input = xmlString.replaceAll("\\n", "");
            input = input.replaceAll("(\\s)+", " ");
            input = input.replaceAll(">(\\s)+<", "><");
            String root = String.format(ROOT, input);
            Document doc = XmlUtils.stringToDom(root);
            final Node node = doc.getFirstChild();
            Map<String, String> map = new LinkedHashMap<>();
            List<String> path = new ArrayList<>();
            parse(node, path, map, new LinkedList<>());
            StringBuilder sb = new StringBuilder();
            if (cutNameSpace) {
                map = cutNameSpace(map);
            }
            map.forEach((k, v) -> {
                sb.append("| ").append(k).append(" | ").append(v).append(" |").append("\n");
            });
            return ObjectRs.builder().txt(sb.toString()).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }
    }

    /**
     * Обрезает неймспейс у XML
     *
     * @param params параметры xml(путь и значение)
     * @return рпраметры xml(путь без неймспейс и значение)
     */
    private static Map<String, String> cutNameSpace(Map<String, String> params) {
        Map<String, String> res = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            String[] split = k.split("\\.");
            List<String> paths = new ArrayList<>();
            for (String path : split) {
                Matcher matcher = NAME_SPACE_PATTERN.matcher(path);
                if (matcher.find()) {
                    if (path.contains("@")) {
                        paths.add("." + matcher.group(1));
                    } else {
                        paths.add(matcher.group(2) + ".");
                    }
                } else {
                    paths.add(path + ".");
                }
            }
            res.put(pathToStr(paths), v);
        });
        return res;
    }

    /**
     * Разбор xml элементов с корневой Node для получения параметров params (путь и значение))
     * используется рекурсивный обход дерева элементов
     *
     * @param node   элемент xml для разбора
     * @param path   список для формирования пути
     * @param params список парметров разобранных элемнтов xml (путь и значение)
     * @param queue  очередь для формирования нумерации в случае совпадения названия тэгов
     */
    private static void parse(Node node, List<String> path, Map<String, String> params, Queue<Integer> queue) {
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
                        params.put(pathToStr(path), atr.getNodeValue());
                        path.remove(path.size() - 1);
                    }
                }
                if (item.getChildNodes().item(0).getChildNodes().getLength() == 0) {
                    params.put(pathToStr(path), item.getTextContent());
                } else {
                    parse(item, path, params, new LinkedList<>());
                }
                path.remove(path.size() - 1);
            }
        }
    }

    /**
     * Объединяет список пути в строку с разделением '.'
     *
     * @param path список с элементами названия пути
     * @return строка объединненого списка
     */
    private static String pathToStr(List<String> path) {
        StringBuilder sb = new StringBuilder();
        path.forEach(sb::append);
        sb.deleteCharAt(sb.lastIndexOf("."));
        return sb.toString();
    }
}
