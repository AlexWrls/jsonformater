// Переменные CodeMirror
let table, json, tableXml, xml, query, step;
let fullWind;
let appName = ''
const textArea = []
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
    query = CodeMirror.fromTextArea(document.getElementById('query'), {
        lineNumbers: true,
    });
    step = CodeMirror.fromTextArea(document.getElementById('step'), {
        lineNumbers: true,
    });
    textArea.push(table, json, tableXml, xml, query, step);
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
    if (fullWind === false) {
        textArea.forEach(i => {
            i.setSize(((width / 2) - 150), (height - 200));
        })
    } else {
        textArea.forEach(i => {
            i.setSize(width - 100, 600);
        })
    }
}


// ============================================================================
// JSON converter script
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
    let btn_max = document.getElementsByClassName('btn-sm btn-secondary btn-max')
    let btn_min = document.getElementsByClassName('btn-sm btn-secondary btn-min')
    if (event === '+') {
        fullWind = true
        const width = document.documentElement.clientWidth
        textArea.forEach(i => {
            i.setSize(width - 100, 600);
        })
        for (let i = 0; i < btn_max.length; i++) {
            btn_max[i].style.display = 'none';
        }
        for (let i = 0; i < btn_min.length; i++) {
            btn_min[i].style.display = '';
        }
    } else {
        fullWind = false
        resize_window();
        for (let i = 0; i < btn_max.length; i++) {
            btn_max[i].style.display = '';
        }
        for (let i = 0; i < btn_min.length; i++) {
            btn_min[i].style.display = 'none';
        }
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
        url: appName + "/json_to_data_table",
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
        url: appName + "/data_table_to_json",
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
// XML converter script
// ============================================================================
function copy_xml_value() {
    let text = xml.getValue();
    copyToClipboard(text, 'xml-label')
}

function copy_table_xml_value() {
    let text = tableXml.getValue();
    copyToClipboard(text, 'table-xml-label')
}

function xml_to_table(e) {
    let data = {};
    let endPoint = '/xml_to_data_table'
    if (e === 'cut') {
        endPoint = '/xml_to_data_table_cut_name_space'
    }
    data["txt"] = xml.getValue();
    $('#btn-xml').prop('disabled', true)
    $('#btn-xml-table').prop('disabled', true)
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: appName + endPoint,
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
        url: appName + "/data_table_to_xml",
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
        url: appName + "/tab_print_xml",
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

// ============================================================================
// SQL query converter script
// ============================================================================
function copy_query_value() {
    let text = query.getValue();
    copyToClipboard(text, 'query-label')
}

function copy_step_value() {
    let text = step.getValue();
    copyToClipboard(text, 'step-label')
}


function query_to_table(e, prop) {
    let endpoint = '/query_to_table_only_not_null'
    if (e === 'all') {
        endpoint = '/query_to_table'
    }
    let data = {};
    data["txt"] = query.getValue() + '\n';
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: appName + endpoint,
        data: JSON.stringify(data),
        dataType: 'json',
        timeout: 600000,
        success: function (data) {
            console.log("SUCCESS : ", data);
            let head = getHeadStep(data.tableName, prop)
            let table = getTableStep(data.columns, data.values)
            $('#step').val(head + table)
            step.getDoc().setValue(head + table);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        }
    });
}

function getTableStep(columns, values) {
    let table = '';
    columns.forEach(i => {
        table = table + '| ' + i
    })
    table = table + ' |\n'
    values.forEach(i => {
        table = table + '| ' + i
    })
    table = table + ' |'
    return table;
}

function getHeadStep(name, prop) {
    if (prop === 'create') {
        return 'Допустим таблица ' + name + ' имеет записи:\n';
    } else {
        return 'Тогда таблица ' + name + ' состоит из записей:\n'
    }
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
