package ru.taa.jsonformater.controller;

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
    public ResponseEntity<?> jsonToDataTable(@RequestBody ObjectRq jsonObject) {
        ObjectRs format = jsonToDataTableService.bind(jsonObject.getTxt());
        return ResponseEntity.ok(format);
    }

    @PostMapping("/data_table_to_json")
    public ResponseEntity<?> dataTableToJson(@RequestBody ObjectRq jsonObject) {
        ObjectRs format = tableToJsonService.format(jsonObject.getTxt());
        return ResponseEntity.ok(format);
    }
}
