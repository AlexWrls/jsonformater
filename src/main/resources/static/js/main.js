let table, json;
let nameCodeMirror = 'CodeMirror cm-s-darcula';
window.onload = function (event) {
    table = CodeMirror.fromTextArea(document.getElementById('table'), {
        lineNumbers: true, // Нумеровать каждую строчку.
        // mode: 'application/xml',
        theme: "darcula",   // Установить тему

    });

    json = CodeMirror.fromTextArea(document.getElementById('json'), {
        lineNumbers: true,
        theme: "darcula",   // Установить тему
        // theme: "idea",   // Установить тему
    });
    resizeWindow();
    let elCodeMirror = document.getElementsByClassName(nameCodeMirror);
    for (let i = 0; i < elCodeMirror.length; i++) {
        elCodeMirror[i].style.fontSize = '90%';
    }
}

window.addEventListener('resize', resizeWindow);

function resizeWindow() {
    const width = document.documentElement.clientWidth
    const height = document.documentElement.clientHeight
    table.setSize(((width / 2) - 150), (height - 200));
    json.setSize(((width / 2) - 150), (height - 200));

}

function copy_json_value() {
    let text = json.getValue();
    copyToClipboard(text, 'json-label')
}

function copy_table_value() {
    let text = table.getValue();
    copyToClipboard(text, 'table-label')
}

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
        json.setSize(1200, 600);
        table.setSize(1200, 600);
        $('#btn-table-max').hide()
        $('#btn-json-max').hide()
        $('#btn-table-min').show()
        $('#btn-json-min').show()
    } else {
        table.setSize(530, 600);
        json.setSize(530, 600);
        $('#btn-table-min').hide()
        $('#btn-json-min').hide()
        $('#btn-json-max').show()
        $('#btn-table-max').show()
    }
}

function change_text_size(elementNumber, step) {
    let el = document.getElementsByClassName(nameCodeMirror)[elementNumber];
    let style = window.getComputedStyle(el, null).getPropertyValue('font-size');
    let fontSize = parseFloat(style);
    el.style.fontSize = (fontSize + step) + 'px';
}

function tab_print() {
    let jsonObj = JSON.stringify(JSON.parse(json.getValue()), undefined, 4);
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
