package ru.taa.jsonformater.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ru.taa.jsonformater.dto.ObjectRq;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.service.DataTableToJsonService;
import ru.taa.jsonformater.service.DataTableToXmlService;
import ru.taa.jsonformater.service.JsonToDataTableService;
import ru.taa.jsonformater.service.XmlToDataTableService;
import ru.taa.jsonformater.utils.FormatUtils;


@RestController
@AllArgsConstructor
public class JsonFormatController {

    private final DataTableToXmlService tableToXmlService;
    private final DataTableToJsonService tableToJsonService;
    private final JsonToDataTableService jsonToDataTableService;
    private final XmlToDataTableService xmlToDataTableService;

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView view = new ModelAndView();
        view.setViewName("index");
        return view;
    }

    @PostMapping("/json_to_data_table")
    public ResponseEntity<?> jsonToDataTable(@RequestBody ObjectRq jsonObject) {
        ObjectRs format = jsonToDataTableService.convert(jsonObject.getTxt());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/data_table_to_json")
    public ResponseEntity<?> dataTableToJson(@RequestBody ObjectRq jsonObject) {
        ObjectRs format = tableToJsonService.convert(jsonObject.getTxt());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/xml_to_data_table")
    public ResponseEntity<?> xmlToDataTable(@RequestBody ObjectRq xmlObject) {
        ObjectRs format = xmlToDataTableService.convert(xmlObject.getTxt());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/data_table_to_xml")
    public ResponseEntity<?> dataTableToXml(@RequestBody ObjectRq xmlObject) {
        ObjectRs format = tableToXmlService.convert(xmlObject.getTxt());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/tab_print_xml")
    public ResponseEntity<?> tabPrintXml(@RequestBody ObjectRq xmlObject) {
        ObjectRs format = ObjectRs.builder()
                .txt(FormatUtils.prettyFormatXML(xmlObject.getTxt()))
                .build();
        return ResponseEntity.ok(format);
    }
}
