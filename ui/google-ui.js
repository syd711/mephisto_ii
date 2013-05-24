/* Connstants */
var listId = '#google-data-list';
var dataPageId = '#google';
var playListId = '#google-play-list';
var playerPageId = '#google_player';
var playlistRefresh = 8000;
var activeTitleColor = '#fff3a5';


var refreshRemoteStatus = false;
var refreshId = '';
/*************/

function deleteGoogleCredentials() {
 initLoad(listId, 'L&ouml;sche Daten');
 $.get('/google/credentials/delete', function(data) {
     $.mobile.changePage('index.html', { transition: "slideup"}, false, false);
     $.mobile.loading("hide");
 });
}


function saveGoogleCredentials() {
 initLoad(listId, 'Speichere Daten');
 var login = $('#google-login').val();
 var pass = $('#google-password').val();
 var data = {'login':login, 'password':pass};
 $.post('/google/credentials/set', data, function(data) {
        checkGoogleCredentials();
    });       
}


function checkGoogleCredentials() {
    refreshId = '';
    initLoad(listId, 'Lade Google Music');
    $('google-control').hide();
    $.getJSON('/google/credentials/get', function(data) {
        if(!data.login || !data.password) {
            $.mobile.changePage('google_login.html', { transition: "slideup"}, false, false);
            $('#google-login').val(data.login);
            $('#google-password').val(data.password);
        }
        else {
            $.mobile.changePage('google.html', { transition: "slideup"}, false, false);
            $('#google-control-artists').attr("checked",false).checkboxradio("refresh");
            $('#google-control-albums').attr("checked",false).checkboxradio("refresh");
            $('google-control').show();    
        }        
        $.mobile.loading("hide");
	});       
}

function loadAlbums() {
	initLoad(listId, 'Lade Alben');
	$.getJSON('/google/albums/list', function(data) {
		var items = [];
		var albums = data.items;
		$.each(albums, function(key, value) {
			if(value.album && value.album.length > 0){
				var url = value.albumArtUrl;
				if(!url || url.length === 0) {
					url = 'img/info.png';
				}
				var html='<li><a href="google_player.html" data-prefetch onClick="playAlbum(\''+ value.album + '\')"><img src="' + url + '" style="height:80px;border-right:1px solid #aaa;"><h2>' + value.album + '</h2><p>'+ value.artist +'</p>';
				if(value.totalTracks !== '0') {
					html+= '<span class="ui-li-count ui-btn-up-c ui-btn-corner-all" id="track-count-label">' + value.totalTracks + '</span>';	
				}			
				html+= '</a></li>';
				items.push(html);
			}
		});
		finalizeLoading(listId, dataPageId, items);
	});       
}

function playAlbum(album) {
	initLoad(playListId, 'Lade Album');
	$.getJSON('/google/album/' + album, function(data) {
		var tracks = data.tracks;
		var items = [];
		applyPlayerInfo(data.albumArtUrl, data.album, data.artist, data.year);
		$.each(tracks, function(key,value) {
			var html='<li><a id="' + value.id + '" href="#" onClick="playSong(\'' + value.id + '\')"><h2>' + value.name + '</h2><span class="ui-li-count ui-btn-up-c ui-btn-corner-all" id="track-count-label">';
			html+=value.duration;
			html+= '</span></a></li>';
			items.push(html);			
		});  
		finalizeLoading(playListId, playerPageId, items);
	});
}

function loadArtists() {
	initLoad(listId, 'Lade Interpreten');
	$.getJSON('/google/artists/list', function(data) {
		var items = [];
		var albums = data.items;
		$.each(albums, function(key, value) {
			var html='<li><a href="google_player.html" data-prefetch onClick="playArtist(\'' + value.artist + '\')"><h2>' + value.artist + '</h2><span class="ui-li-count ui-btn-up-c ui-btn-corner-all" id="track-count-label">';
			html+= value.totalTracks;
			html+= '</span></a></li>';
			items.push(html);
		});
		finalizeLoading(listId, dataPageId, items);		
	});
}

function playArtist(artist) {
	initLoad(playListId, 'Lade Titel');
	$.getJSON('/google/artist/' + artist, function(data) {
		var items = [];
		var tracks = data.tracks;
		var trackCount = data.totalTracks;
		applyPlayerInfo(data.albumArtUrl, data.artist, trackCount + ' Titel', data.year);
		$.each(tracks, function(key, value) {
			var html='<li><a id="' + value.id + '" href="#" onClick="playSong(\'' + value.id + '\')"><h2>' + value.name + '</h2><span class="ui-li-count ui-btn-up-c ui-btn-corner-all" id="track-count-label">';
			html+= value.duration;
			html+= '</span></a></li>';
			items.push(html);			
		});
		finalizeLoading(playListId, playerPageId, items);		
	});
}

function playSong(id) {
    $.mobile.loading( 'show', {
        text: 'Lade Titel',
        textVisible: true,
    	theme: 'a',
    	html: ""
	});
    $.get('/google/player/song/' + id, function(data) {
        $('#google-play-list').find('a').attr('style','background-color:transparent;')
        $('#' + id).attr('style', 'background-color:' + activeTitleColor);
        //$('#' + id).addClass('playing');
		$.mobile.loading("hide");
        if(!refreshRemoteStatus) {
            refreshRemoteStatus = true;
            refreshPlaylist();
        }
	});    
}

function playerAction(action) {
    $.mobile.loading( 'show', {
        text: 'Aktualisiere Player',
        textVisible: true,
        theme: 'a',
    	html: ""
	});
    $.get('/google/player/' + action, function(data) {
    	$.mobile.loading("hide");
	});  
}

function startPlayer() {
    $.mobile.loading( 'show', {
        text: 'Aktualisiere Player',
        textVisible: true,
        theme: 'a',
        html: ""
	});
    $.get('/google/player/all', function(data) {
        $('#google-play-list').find('a').attr('style','background-color:transparent;')
        $('#' + data).attr('style', 'background-color:' + activeTitleColor);
    	$.mobile.loading("hide");
        if(!refreshRemoteStatus) {
            refreshRemoteStatus = true;
            refreshPlaylist();
        }
    });
}

function stopPlayer() {
    refreshRemoteStatus = false;
    $.get('/google/player/stop', function(data) {
        $('#google-play-list').find('a').attr('style','background-color:transparent;')
    });
}

// Helper
/*******************************************/
function initLoad(lId, title) {
	//remove all existing station and reload the stations in this callback
	$.mobile.loading( 'show', {
		text: title,
		textVisible: true,
		theme: 'a',
		html: ""
	});
	$(lId).children().remove();	
}

function finalizeLoading(lId, pageId, items) {
    $(lId).append(items); 
    $(lId).trigger('create');
    try {
		$(lId).listview('refresh');
    }
    catch(err) {
    }
    $.mobile.loading("hide");	
}

function applyPlayerInfo(url, album, artist, year) {
	if(url && url.length > 0) {
		$('#player-cover').attr('src', 'http:' + url);	
		$('#player-cover').attr('style', 'height:80px;border:1px solid #021a40;');
	}
	else {
		$('#player-cover').attr('src', 'img/info.png');
		$('#player-cover').removeAttr('style');
	}
	
	$('#player-album').html('');
	$('#player-album').html(album);
	$('#player-artist').html('');
	$('#player-artist').html(artist);
	$('#player-year').html('');	
	if(year && year !== '0') {
		$('#player-year').html(year);	
	}
}


function refreshPlaylist() {
    if(refreshRemoteStatus) {
        $.get('/google/player/current', function(data) {
            if(refreshId !== data) {
                refreshId = data;
                $('#google-play-list').find('a').attr('style','background-color:transparent;')
                if(data && data.length > 0) {
                    $('#' + data).attr('style', 'background-color:' + activeTitleColor);
                }
            }
            window.setTimeout(function() {
                refreshPlaylist();  
            }, playlistRefresh);
        });    
    }
}