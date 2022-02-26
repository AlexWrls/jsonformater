package ru.taa.jsonformater.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ru.taa.jsonformater.dto.JsonRq;
import ru.taa.jsonformater.dto.JsonRs;
import ru.taa.jsonformater.service.DataTableToJsonService;
import ru.taa.jsonformater.service.JsonToDataTableService;

@RestController
public class JsonFormatController {
    @Autowired
    private DataTableToJsonService tableToJsonService;
    @Autowired
    private JsonToDataTableService jsonToDataTableService;

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView view = new ModelAndView();
        view.setViewName("index");
        return view;
    }

    @PostMapping("/json_to_data_table")
    public ResponseEntity<?> jsonToDataTable(@RequestBody JsonRq jsonObject) {
        JsonRs format = jsonToDataTableService.format(jsonObject.getJsonData());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/data_table_to_json")
    public ResponseEntity<?> dataTableToJson(@RequestBody JsonRq jsonObject) {
        JsonRs format = tableToJsonService.format(jsonObject.getJsonData());
        return ResponseEntity.ok(format);
    }
}
