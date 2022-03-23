package ru.taa.jsonformater.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ru.taa.jsonformater.dto.ObjectRq;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.dto.QueryRs;
import ru.taa.jsonformater.service.DataTableToJsonService;
import ru.taa.jsonformater.service.DataTableToXmlService;
import ru.taa.jsonformater.service.JsonToDataTableService;
import ru.taa.jsonformater.service.SqlToDataTableService;
import ru.taa.jsonformater.service.XmlToDataTableService;
import ru.taa.jsonformater.utils.XmlUtils;


@RestController
@AllArgsConstructor
public class JsonFormatController {
    private final DataTableToXmlService tableToXmlService;
    private final DataTableToJsonService tableToJsonService;
    private final JsonToDataTableService jsonToDataTableService;
    private final XmlToDataTableService xmlToDataTableService;
    private final SqlToDataTableService sqlToDataTableService;

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView view = new ModelAndView();
        view.setViewName("index");
        return view;
    }

    @PostMapping("/json_to_data_table")
    public ResponseEntity<?> jsonToDataTable(@RequestBody ObjectRq jsonObject) {
        ObjectRs convert = jsonToDataTableService.convert(jsonObject.getTxt());
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/data_table_to_json")
    public ResponseEntity<?> dataTableToJson(@RequestBody ObjectRq jsonObject) {
        ObjectRs convert = tableToJsonService.convert(jsonObject.getTxt());
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/xml_to_data_table")
    public ResponseEntity<?> xmlToDataTable(@RequestBody ObjectRq xmlObject) {
        ObjectRs convert = xmlToDataTableService.convert(xmlObject.getTxt(), false);
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/xml_to_data_table_cut_name_space")
    public ResponseEntity<?> xmlToDataTableCutNameSpace(@RequestBody ObjectRq xmlObject) {
        ObjectRs convert = xmlToDataTableService.convert(xmlObject.getTxt(), true);
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/data_table_to_xml")
    public ResponseEntity<?> dataTableToXml(@RequestBody ObjectRq xmlObject) {
        ObjectRs convert = tableToXmlService.convert(xmlObject.getTxt());
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/tab_print_xml")
    public ResponseEntity<?> tabPrintXml(@RequestBody ObjectRq xmlObject) {
        String xml = XmlUtils.prettyFormatXML(xmlObject.getTxt());
        ObjectRs format = ObjectRs.builder().txt(xml).build();
        return ResponseEntity.ok(format);
    }

    @PostMapping("/query_to_table")
    public ResponseEntity<?> queryToTable(@RequestBody ObjectRq query) {
        QueryRs convert = sqlToDataTableService.convert(query.getTxt(), false);
        return ResponseEntity.ok(convert);
    }

    @PostMapping("/query_to_table_only_not_null")
    public ResponseEntity<?> queryNotNullToTable(@RequestBody ObjectRq query) {
        QueryRs convert = sqlToDataTableService.convert(query.getTxt(), true);
        return ResponseEntity.ok(convert);
    }
}
