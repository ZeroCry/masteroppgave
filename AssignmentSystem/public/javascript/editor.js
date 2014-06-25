$(document).ready(function () {

    var editors = initEditors();
    var webSocket = openNewWebSocket();
    initCollapsibleHeaders();

    var completionCallback; // FIXME: Create a better solution

    webSocket.onopen = function() {
        console.log('ws connected ('+jsRoutes.controllers.AssignmentController.openEditorSocket(getCurrentProblemID()).webSocketURL()+')');
        sendMessage("notifyOnReady");
    };

    webSocket.onerror = function() {
        console.log('ws error');
    };

    webSocket.onclose = function() {
        console.log('ws closed');
    };

    webSocket.onmessage = function(msgevent) {
        var object = JSON.parse(msgevent.data);
        console.log(object);
        if (object.type === 'ready') {
            $('#editor-logo').addClass('ready');
        }
        else if (object.type === 'runMainResult') {
            $('.ace-editor-console').text(object.data.output);
        }
        else if (object.type === 'runTestsResult') {
            // TODO: Implement
        }
        else if (object.type === 'codeCompletionResult') {
            var proposals = object.data.proposals.map(function (proposal) {
               return {value: proposal.completion, meta: "eclipse"}
            });
            completionCallback(null, proposals);
        }
        else if (object.type === 'errorCheckingResult') {
            var files = object.data.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];

                var foundEditors = editors.filter(function (editor) {
                    var fileId = $(editor.container).attr('data-file-id');
                    return (file.fileId === fileId);
                });

                if (foundEditors.length == 1) {
                    var annotations = file.problemMarkers.map(function (problem) {
                        return new Annotation(problem.lineNumber - 1, problem.description, convertType(problem.type));
                    });

                    var editor = foundEditors[0];
                    editor.getSession().setAnnotations(annotations);
                }
            }
        }
    };

    $(editors).each(function() {
        var editor = this;

        this.on('change', function() {
            throttle(function(){
                //this code is called 300ms after the last change-event

                var fileId = $(editor.container).attr('data-file-id');
                var sourceCode = editor.getSession().getValue();

                sendMessage("updateSourceCode", {
                    fileId: fileId,
                    sourceCode: sourceCode
                });
            }, 300);
        });
    });

    function sendMessage(type, message) {
        message = message || {};
        var data = JSON.stringify({type: type, data: message});

        webSocket.send(data);
        console.log("Sent: " + data);
    }

    /*
        Click listeners go below here
        vvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    */

    $("#ae-toggle-fullscreen").click(function(){
        $(".hidden-when-editor-maximized").toggle();
        $("#ace-editor-wrapper").toggleClass("maximized");
        $("#ae-toggle-fullscreen").toggleClass("fa-expand fa-compress");
        $(editors).each(function() { this.resize(); });
    });

    $(".ae-theme-settings").click(function(){
        var theme = ("ace/theme/"+$(this).attr("id"));
        $(editors).each(function() { this.setTheme(theme); });
    });

    $(".collapsing-header").click(function(){
        $(this).find("i").toggleClass("fa-angle-down fa-angle-right"); //toggle icon on header-click
    });

    $("#run-code-button").click(function(){
        sendMessage("runMain");
    });

    $("#run-tests-button").click(function(){
        sendMessage("runTests");
    });

    $("#deliver-assignment-button").click(function(){
        sendMessage("deliverAssignment");
    });

    /*
        Helper functions go below here
        vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    */

    function initCollapsibleHeaders() {
        $(".hidden-when-editor-maximized").collapsible(); //makes ever header in this div collapsible
    }

    function initEditors() {
        var editors = [];
        var autoCompleter = ace.require("ace/ext/language_tools");
        $(".ace-editor-instance").each(function () { editors.push(createEditor($(this).attr("id"))); });
        autoCompleter.addCompleter(createCompleter());
        return editors;
    }

    function createEditor(editorID){
        var editor = ace.edit(editorID);
        editor.setTheme("ace/theme/eclipse");
        editor.getSession().setMode("ace/mode/java");
        editor.setOptions({enableBasicAutocompletion: true});
        return editor;
    }

    function createCompleter(){
        return {
            getCompletions: function(editor, session, pos, prefix, callback) {
                console.log(pos);

                editor

                sendMessage("codeCompletion", {
                    fileId: "5",
                    offset: 0
                });
                completionCallback = callback;
            }
        };
    }

    function openNewWebSocket() {
        return new WebSocket(jsRoutes.controllers.AssignmentController.openEditorSocket(getCurrentProblemID()).webSocketURL());
    }

    function getCurrentProblemID(){
        return $("#problem-id").data("problemid");
    }

    function convertType(type) {
        switch (type) {
            case 'Warning':
                return 'warning';
            case 'Information':
                return 'info';
            default:
                return 'error';
        }
    }

    function Annotation(lineNumber, message, type){
        this.row = lineNumber;
        this.text = message;
        this.type = type; // "error", "warning", "info"
    }

    var throttle = (function(){
        var timer = 0;
        return function(callback, ms){
            clearTimeout (timer);
            timer = setTimeout(callback, ms);
        };
    })();

});



