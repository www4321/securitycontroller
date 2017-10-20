//require jquery

var sc_host = ""; 
var url = sc_host+"/sc/directory/commands";

var commands = null;

function fetchCommands(){
    $.get(url,function(data,status){
        if(data.OPT_STATUS != 200){
            alert("cannot fetch command, error code: "+data.HEAD.OPT_STATUS );
            return;
        }
        commands = data.DATA.commands;
        if(commands == null){
            alert("command list is null" );
            return;
        }
        buildDataArray(0);
    });
}

function submitCommand(input){
    var s = input.split(" ");
    var i=0, j=0;
    var command = null, subcmd = null, object = null;
    var options = [];
    var option_key = null;
    for(i=0;i<s.length;i++){
        if(s[i] == "") //skip "   "
            continue;
        if(j == 0)
            command = s[j];
        else if(j == 1)
            subcmd = s[j];
        else if(j == 2)
            object = s[j];
        else if(option_key == null)//option key
            option_key = s[j];
        else if(option_key != null)
            options[option_key] = s[j];
        j = j + 1;
    }
    if(command == null || subcmd == null){
        alert("command or subcommand empty!");
        return;
    }

	console.log("submitting command..");
    var req = {"command": command, "subcmd": subcmd, "options":options};
    var jreq = JSON.stringify(req);
    //req = JSON.stringify(cmd); 
    $.ajax({
        type: 'POST', url: url+"/"+command, 
        data: jreq, 
        contentType: "application/json",
        dataType: 'json',
        success: function(data) {
            if(data.OPT_STATUS == 200)
                $("#result").html( $("#result").html()+"<br/>"+data.DATA.result);
            else
                $("#result").html( $("#result").html()+"<br/>error");
        }
    });
}


function buildDataArray(type, value){
    var i=0;
    if(type == 0){//command
        data = [];
        for(i = 0; i < commands.length; i++) {
            var command = commands[i];
            data.push(command.command);
        }
    }
    else if (type == 1){ //subcommand
        for(i = 0; i < commands.length; i++) {
            var command = commands[i];
            if(command.command != value)
                continue;
            data = [];
            var j=0;
            for(j=0; j<command.subcommand.length; j++){
                var fcmd = command.command + " " + command.subcommand[j];
			    console.log("push .."+ fcmd);
                data.push(fcmd);
            }
            break;
        }
    }
    refreshAutoComplete();
}

function checkInput(input){
}

$(document).ready(function() {

	$('#command').bind('keydown', function(e) {
		var key = e.keyCode || e.charCode;
	
		if(key == 9){ //tab
			console.log("break..");
			
			var commandListbox = $("#command_listbox");
			var commands = commandListbox.children();
			var command = commands[0];
			
			$('#command').val($(command).text());
			return false;
		}
		else if(key == 32){ //space
			console.log("new word..");
            var input = $('#command').val();
            input = input.trim();
            var blankPos = input.indexOf(" ");
            if(blankPos >=0) //more than two words
                return;
            buildDataArray(1, input);
            $('#command').val(input+" ");
            return false;
		}		
		else if(key == 10 ||key == 13){ //newline
            var input = $('#command').val();
            $("#result").html( $("#result").html()+"<br/><br/>#"+input);
            submitCommand(input);
            $('#command').val("");
            buildDataArray(0);
            refreshAutoComplete();
            return;
		}
		else if( (key>=65 && key<=90 ) || (key>=97 && key<=122 )){
			console.log(key);
            refreshAutoComplete();
		}
		else{
			console.log("other key:"+key);
		}
	});
	$('#command').bind('keyup', function(e) {
		var key = e.keyCode || e.charCode;
		if(key == 8){
			console.log("backspace..");
            var input = $('#command').val();
            //input = input.substring(0, input.length-2);
            //$('#command').val(input);
            var finput = input.trim();
            if(finput.length == 0){
                buildDataArray(0);
            }
            else{
                var blankPos = finput.indexOf(" ");
                if(blankPos >=0) //more than two words
                    return;
                if(input[input.length-1] == ' ')
                    buildDataArray(1, input);
                else
                    buildDataArray(0);
            }
            return false;
        }
    });
	
});
