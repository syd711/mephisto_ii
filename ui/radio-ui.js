$.ajaxSetup ({  
    cache: false  
}); 

var refreshRemoteStatus = false;

function loadStations() {
	try {
		$.mobile.loading('show', {
			text: 'Aktualisiere Liste',
			textVisible: true,
			theme: 'a',
			html: ""
		});
	}
	catch(e) {
		//ignore
	}
	$.getJSON('/stations/list', function(data) {
		$.mobile.loading( 'show', {
			text: 'Lade Stationen',
			textVisible: true,
			theme: 'a',
			html: ""
		});
		var items = [];
		var ids = [];
	
		$.each(data, function(key, value) {
		if(key === 'stations') {
			for(var i=0; i<value.length; i++) {
				var name = value[i].name;
				var title = value[i].title;
				if(!title) {
					title = "- keine Informationen verf&uuml;gbar -";
				}
				if(!name) {
					var url = value[i].url;
					if(url.length > 35 ) {
						url = url.substr(0, 34) + "...";
					}
					name = '<span style="color:#aaa;font-style:italic;">' + url + '</span>';
				}
				var html = '';
				html+='<tr>';
				html+='<td style="vertical-align: middle;">' + (i+1) + '</td>';
				html+='<td>';
				if(i>0) {
					html+='	<a href="#" id="' + i + '-up" onClick="moveStation(' + i + ',' + (i-1) + ')" data-role="button" data-icon="arrow-u" data-iconpos="notext" data-theme="c" data-inline="true"></a>';
				}
				if((i+1)!=value.length) {
					html+='	<a href="#" id="' + i + '-down" onClick="moveStation(' + i + ',' + (i+1) + ')" data-role="button" data-icon="arrow-d" data-iconpos="notext" data-theme="c" data-inline="true"></a>';
				}
				
				html+='</td>';
				html+='<td style="vertical-align: middle;"><a class="ui-link-inherit" style="font-weight:bold;" onClick="editStation(' + value[i].id + ')" id="station-details-edit-' + value[i].id + '" href="#station-details-page" datatheme="e">+' + name + '</a></td>';
				html+='<td style="vertical-align: middle;">' + title + '</td>';
				html+='<td style="vertical-align: middle;">';
				html+='	 <a id="play-' + i + '" href="#" onClick="playStation(' + i + ')" data-role="button" data-icon="gear" data-iconpos="notext" data-theme="c" data-inline="true"></a>';
				html+='</td>';
				html+='</tr>';
				items.push(html);
				ids.push(value[i].id);
			}	
		}
	});		
	
	$('#stations-list').children().remove();
	$('#stations-list').append(items); 
	$('#station-count-label').html(items.length);
	$('#stations-page').trigger('create');//trigger page refresh
    
    /*
     * Disable the add button if the station count is reached
     */
    var stationCount = $('#stations-list').children().length;
    if(stationCount >= MAX_RADIO_STATIONS) {
        $('#add-station-button').hide();
    }
    else {
        $('#add-station-button').show();
    }
    
    
	//refresh may throw exception the first time
	try {
		$('#stations-list').listview('refresh');
	}
	catch(err) {
	}
	$.mobile.loading("hide");
	});  
}

function playStation(id) {
	$.getJSON('/stations/play/' + id, function(data) {
		loadStations();
	});
}

function editStation(id) {

	$.mobile.changePage('radio_station.html', { dataUrl : "radio_station.html", data : {}, reloadPage : true, changeHash : true });
    $('#station-url').val("");
	$('#station-name').val("");
	if(id<0) {
		$('#station-id').val(-1);
		$('#station-url').attr("placeholder", "");
		$('#delete-station-link').hide();
	}
	else {
		$('#delete-station-link').addClass("ui-disabled");		
		$('#save-station-link').addClass("ui-disabled");		
		$('#test-station-link').addClass("ui-disabled");		
		$('#station-url').attr("placeholder", "Lade Stations-Daten...");
		$('#delete-station-link').show();
	}
	
	if(id>=0) {
		$.mobile.loading( 'show', {
			text: 'Lade Daten',
			textVisible: true,
			theme: 'a',
			html: ""
		});
		$.getJSON('/stations/edit/' + id, function(data) {
			$('#station-url').val(data.url);
			$('#station-name').val(data.name);
			$('#station-id').val(data.id);
			$.mobile.loading("hide");
			
			$('#delete-station-link').removeClass("ui-disabled");		
			$('#save-station-link').removeClass("ui-disabled");		
			$('#test-station-link').removeClass("ui-disabled");		
		});	
	}
}

function reloadStations() {
	//remove all existing station and reload the stations in this callback
	$('#stations-list').children().remove();
	$.mobile.loading( 'show', {
		text: 'Lade Stationen',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	$('#stations-reload-btn').addClass("ui-disabled");
	$.getJSON('/stations/reload', function(data) {
		loadStations();
		$('#stations-reload-btn').removeClass("ui-disabled");		
	});       
}


function saveStation() {
	$.mobile.loading( 'show', {
		text: 'Aktualisiere Liste',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	var stationUrl = $('#station-url').val();
	var stationId = $('#station-id').val();
	var data = {url:stationUrl};
	//remove all existing station and reload the stations in this callback
	$('#stations-list').children().remove();
	$.post('/stations/save/' + stationId, data, function(data) {
		loadStations();
    });
}

function testStation() {
    var stationUrl = $('#station-url').val();
    $('#station-test-url').val(stationUrl);
    $('#radio-testframe').attr('src', stationUrl);
}

function restoreUrlAfterTest() {
    var stationUrl = $('#station-test-url').val();
    $('#station-url').val(stationUrl);
}


function moveStation(fromPos, toPos) {
	$.mobile.loading( 'show', {
		text: 'Aktualisiere Liste',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	$.get('/stations/move/' + fromPos + '/' + toPos, function(data) {
		$('#stations-list').children().remove();
		loadStations();
	});
}

function remoteControl(action) {
    if(action === 'play' && $('#play-button .ui-btn-text').text() === 'Stop') {
        action = 'stop';
    }
	$.mobile.loading('show');
	$.get('/radio/control/' + action, function(data) {
		$('#current-textarea').val(data);
        if(data.length === 0) {
            refreshRemoteStatus = false;
            $('#play-button .ui-btn-text').text('Start');    
            $('#play-button').buttonMarkup({ icon: "arrow-r" });
        }
        else {
            refreshRemoteStatus = true;
            refreshDisplay();
            $('#play-button .ui-btn-text').text('Stop');    
            $('#play-button').buttonMarkup({ icon: "delete" });
        }
        
		$.mobile.loading("hide");
	});	
}

function refreshDisplay() {
    if(refreshRemoteStatus) {
        $.get('/radio/control/current', function(data) {
            $('#current-textarea').val(data);
            window.setTimeout(function() {
                refreshDisplay();
            }, 10000);
        });
    }
}