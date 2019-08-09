var js = {};
var message = "";

function getAdditionInfo() {
	return js;
}

// get form info and post here
function myDoPost() {
	var tmp = $('#f1').serializeArray(); // only text field
	for (var i = 0; i < tmp.length; ++i)
		js[tmp[i].name] = tmp[i].value;

	$('#outputMessage').html('processing your files...');
	
	$.ajax({
		type : "POST",
		url : "api/parseFile",
		data : js,
		dataType : "json",

		success : function(res) {
			var downloadData = "";
			var jsonData = JSON.parse(res.fileDownload);
			for (var i = 0; i < jsonData.length; i++) {
				downloadData = downloadData + ' -- <a href="download?fileName=' + jsonData[i].fileName + '&fileFolder=' + jsonData[i].fileFolder + '" id="downloadLink_' + (i+1) + '">Concept File ' + (i+1) + '</a>';
			}
			$('#outputDownload').html(downloadData);
			message = res.message;
			$('#outputMessage').html(message + '<br/> loading graph...');
			var parseFile = res.conceptFile;
			queryGraph(parseFile);
		},
		error : function(error) {
			console.log(error);
		},
		async : true,
		timeout : 3600000
	});
	
	setTimeout(queryProgressParse, 2000);
}

function queryProgressParse() {
	$.ajax({
		type : "get",
		url : "api/parse_progress",
		dataType : "json",

		success : function(res) {
			console.log('res', res);
			var percentage = res.percentage;
			$('#outputMessage').html("processing ... " + percentage + "%");
			if (!percentage || percentage < 100) {
				setTimeout(queryProgressParse, 2000);
			}
		},
		error : function(error) {
			console.log(error);
			setTimeout(queryProgressParse, 2000);
		},
		async : true,
		timeout : 6000
	});
}


/*
 function queryProgressParse() {
	$.ajax({
		type : "get",
		url : "api/parse_progress",
		dataType : "json",

		success : function(res) {
			console.log('res', res);
			var percentage = res.percentage;
			if(percentage == 0) {
				$('#outputMessage').html("The file is too big. Please choose another file!");
			} else {
				$('#outputMessage').html("processing ... " + percentage + "%");
			}	
			if (!percentage || percentage < 100) {
				setTimeout(queryProgressParse, 2000);
			}
		},
		error : function(error) {
			console.log(error);
			setTimeout(queryProgressParse, 2000);
		},
		async : true,
		timeout : 6000
	});
}
*/


function showUploadedFile() {
	var js = getAdditionInfo();

	if (js['realName']) {
		if (js['realName']['ebookFile'])
			$('#ebookUploaded').html(js['realName']['ebookFile']);
		if (js['realName']['phraseFile'])
			$('#phraseListUploaded').html(js['realName']['phraseFile']);
		if (js['realName']['stopwordFile'])
			$('#ignoredConceptsUploaded').html(js['realName']['stopwordFile']);
	}
}

function fn(ss) {
	var funcs = function(data) {
		var server_file = data.files[0].storedFileName;
		js = getAdditionInfo();

		js[ss] = server_file;
		var tmp = js['realName'] || {};
		tmp[ss] = data.files[0].fileName;
		js['realName'] = tmp;

		showUploadedFile();
	}

	return funcs;
}

function uploadFileAjax(fid, callback) {
	$(fid).on("change", function(e) {
		var files = $(this)[0].files;
		for (var i = 0; i < files.length; ++i) {
			file = files[i];
			var upload = new Upload(file, callback);

			// execute upload
			upload.doUpload();
		}
	});
}

$(document).ready(function() {

	// for upload file when user choose (not wait to submit, use to get list of
	// concept)
	uploadFileAjax("#ebookFile", fn('ebookFile'));
	uploadFileAjax("#phraseFile", fn('phraseFile'));
	uploadFileAjax("#stopwordfile", fn('stopwordFile'));

	$("#f1").submit(function(e) {
		console.log($(this).serialize());
		myDoPost();
		return false;
	});

	if (typeof meta_data !== 'undefined')
		showMetaData(meta_data);
	// showUploadedFile();

});