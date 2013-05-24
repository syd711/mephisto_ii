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
					title = "- Sender wurde noch nicht gespielt -";
				}
				if(!name) {
					name = '<span style="color:#aaa;font-style:italic;">URL: ' + value[i].url + '</span>';
				}
                name = (i+1) + '. ' + name;
				var html = '';
				html+='<tr>';
				html+='<th>';
				if(i>0) {
					html+='	<a href="#" onClick="moveStation(' + i + ',' + (i-1) + ')" data-role="button" data-icon="arrow-u" data-iconpos="notext" data-theme="c" data-inline="true"></a>';	
				}
				if((i+1)!=value.length) {
					html+='	<a href="#" onClick="moveStation(' + i + ',' + (i+1) + ')" data-role="button" data-icon="arrow-d" data-iconpos="notext" data-theme="c" data-inline="true"></a>';	
				}
				
				html+='</th>';
				html+='<td style="vertical-align: middle;"><a href="radio_station.html" class="ui-link-inherit" style="font-weight:bold;" onClick="editStation(' + value[i].id + ')" id="station-details-edit-' + value[i].id + '" href="#station-details-page" datatheme="e">' + name + '</a></td>';
				html+='<td style="vertical-align: middle;">' + title + '</td>';
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



function editStation(id) {
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
		$.getJSON('/station/edit/' + id, function(data) {
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
	$.post('/station/save/' + stationId, data, function(data) {
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


function deleteStation() {
	$.mobile.loading( 'show', {
		text: 'Aktualisiere Liste',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	var stationId = $('#station-id').val();
	$.get('/station/delete/' + stationId, function(data) {
        loadStations();
	});
}


function moveStation(fromPos, toPos) {
	$.mobile.loading( 'show', {
		text: 'Aktualisiere Liste',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	$.get('/station/move/' + fromPos + '/' + toPos, function(data) {
		$('#stations-list').children().remove();
		loadStations();
	});
}

function applyVolumeSliderListener() {
	$('.ui-slider').live('mouseup', function() {
		$.mobile.loading('show');
		var slider_value = $("#volume-slider").val();
		$.getJSON('/radio/volume/' + slider_value, function(data) {
			$.mobile.loading('hide');
		});    
	});
	
	$.getJSON('/radio/volume', function(data) {
		$('#volume-slider').attr('value', data);
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