// Переменные CodeMirror
let table, json, tableXml, xml;
let fullWind;
// Событие при загрузке страницы
window.onload = function (event) {
    fullWind = false;
    // инициализация code mirror
    table = CodeMirror.fromTextArea(document.getElementById('table'), {
        lineNumbers: true,
        // theme: "idea",   // Установить тему
    });

    json = CodeMirror.fromTextArea(document.getElementById('json'), {
        lineNumbers: true,
    });

    tableXml = CodeMirror.fromTextArea(document.getElementById('table-xml'), {
        lineNumbers: true,
    });
    xml = CodeMirror.fromTextArea(document.getElementById('xml'), {
        lineNumbers: true,
        mode: 'application/xml',
    });
// установка размера контента
    resize_window();
// установка параметров размер шрифта
    let codeMirror = document.getElementsByTagName('CodeMirror cm-s-default')
    for (let i = 0; i < codeMirror.length; i++) {
        codeMirror[i].style.fontSize = '90%';
    }

}

// событие при изменениии размера окна браузера
window.addEventListener('resize', resize_window);

// привязка контента к размеру окна браузера
function resize_window() {
    const width = document.documentElement.clientWidth
    const height = document.documentElement.clientHeight
    if (fullWind===false) {
        table.setSize(((width / 2) - 150), (height - 200));
        json.setSize(((width / 2) - 150), (height - 200));
        tableXml.setSize(((width / 2) - 150), (height - 200));
        xml.setSize(((width / 2) - 150), (height - 200));
    } else {
        table.setSize(width-100, 600);
        json.setSize(width-100, 600);
        tableXml.setSize(width-100, 600);
        xml.setSize(width-100, 600);
    }
}


// ============================================================================
// JSON Script
// ============================================================================
function copy_json_value() {
    let text = json.getValue();
    copyToClipboard(text, 'json-label')
}

function copy_table_value() {
    let text = table.getValue();
    copyToClipboard(text, 'table-label')
}

// универсальная функция копирование в буфер обмена строки 'str' отображение в лейбле 'infoLabel'
function copyToClipboard(str, infoLabel) {
    let area = document.createElement('textarea');
    document.body.appendChild(area);
    area.value = str;
    area.select();
    document.execCommand("copy");
    document.body.removeChild(area);
    $('<span>', {
        id: 'info'
    }).appendTo('#' + infoLabel)
    let info = document.getElementById('info');
    info.style.color = 'gray'
    info.textContent = ' Скопировано в буфер обмена'
    setTimeout(function () {
        $('#info')[0].remove()
    }, 1500);
}

function change_size(event) {
    if (event === '+') {
        fullWind = true
        const width = document.documentElement.clientWidth
        table.setSize(((width) - 100), 600);
        json.setSize(((width) - 100), 600);
        table.setSize(((width) - 100), 600);
        xml.setSize(((width) - 100), 600);
        tableXml.setSize(((width) - 100), 600);
        $('#btn-table-max').hide()
        $('#btn-table-min').show()

        $('#btn-json-max').hide()
        $('#btn-json-min').show()

        $('#btn-table-xml-max').hide()
        $('#btn-table-xml-min').show()

        $('#btn-xml-max').hide()
        $('#btn-xml-min').show()
    } else {
        fullWind = false
        resize_window();
        $('#btn-table-max').show()
        $('#btn-table-min').hide()

        $('#btn-json-max').show()
        $('#btn-json-min').hide()

        $('#btn-table-xml-max').show()
        $('#btn-table-xml-min').hide()

        $('#btn-xml-max').show()
        $('#btn-xml-min').hide()
    }
}

// универсальная функция изменения размера шрифта
// * elementNumber - номер элемента codeMirror
// * step - шаг изменения
function change_text_size(elementNumber, step) {
    let el = document.getElementsByClassName('CodeMirror cm-s-default')[elementNumber];
    let style = window.getComputedStyle(el, null).getPropertyValue('font-size');
    let fontSize = parseFloat(style);
    el.style.fontSize = (fontSize + step) + 'px';
}

// Печать с отступом JSON
function tab_print() {
    let jsonObj = JSON.stringify(JSON.parse(json.getValue()), undefined, 2);
    $('#json').val(jsonObj);
    json.getDoc().setValue(jsonObj);
}

function json_to_table() {
    let data = {};
    data["txt"] = json.getValue();
    $('#btn-json').prop('disabled', true)
    $('#btn-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/json_to_data_table",
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            $('#table').val(data.txt)
            $('#btn-json').prop('disabled', false)
            $('#btn-table').prop('disabled', false)
            table.getDoc().setValue(data.txt);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            $('#btn-json').prop('disabled', false)
            $('#btn-table').prop('disabled', false)
        }
    });
}

function table_to_json() {
    let data = {};
    data["txt"] = table.getValue() + '\n';
    $('#btn-json').prop('disabled', true)
    $('#btn-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/data_table_to_json",
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            $('#json').val(data.txt)
            $('#btn-json').prop('disabled', false)
            $('#btn-table').prop('disabled', false)
            json.getDoc().setValue(data.txt);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            $('#btn-json').prop('disabled', false)
            $('#btn-table').prop('disabled', false)
        }
    });
}

// ============================================================================
// XML Script
// ============================================================================
function copy_xml_value() {
    let text = xml.getValue();
    copyToClipboard(text, 'xml-label')
}

function copy_table_xml_value() {
    let text = tableXml.getValue();
    copyToClipboard(text, 'table-xml-label')
}


function xml_to_table() {
    let data = {};
    data["txt"] = xml.getValue();
    $('#btn-xml').prop('disabled', true)
    $('#btn-xml-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/xml_to_data_table",
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            $('#table-xml').val(data.txt)
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
            tableXml.getDoc().setValue(data.txt);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
        }
    });
}

function table_to_xml() {
    let data = {};
    data["txt"] = tableXml.getValue() + '\n';
    $('#btn-xml').prop('disabled', true)
    $('#btn-xml-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/data_table_to_xml",
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            $('#xml').val(data.txt)
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
            xml.getDoc().setValue(data.txt);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
        }
    });
}

// печать с отступом Xml
function tab_print_xml() {
    let data = {};
    data["txt"] = xml.getValue() + '\n';
    $('#btn-xml').prop('disabled', true)
    $('#btn-xml-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/tab_print_xml",
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            $('#xml').val(data.txt)
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
            xml.getDoc().setValue(data.txt);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            $('#btn-xml').prop('disabled', false)
            $('#btn-xml-table').prop('disabled', false)
        }
    });
}

// скрытие контента при нажатии на кнопки меню (Xml | Json)
function openPage(pageName, elmnt) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablink");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].style.backgroundColor = "";
    }
    document.getElementById(pageName).style.display = "inline-block";
    elmnt.style.backgroundColor = '#198754';
}
