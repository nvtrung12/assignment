var js = {};
var message = "";

function getAdditionInfo() {
	return js;
}

function queryGraph1(dataFile) {
	$.ajax({
		type : "POST",
		url : "api/callLink",
		data : {
			fileName : dataFile,
			question: $('#question').val()
		},
		dataType : "json",

		success : function(res) {
			data = res.grData; // raw JSON instead text of JSON (not need JSON.parse here)
			buildGraph1(data, 'mynetwork');
		},
		error : function(error) {
			console.log(error);
		},
		async : true,
		timeout : 60000
	});
}

function showUploadedFile() {
	var js = getAdditionInfo();

	if (js['realName']) {
		if (js['realName']['ebookFileC'])
			$('#cbookUploaded').html(js['realName']['ebookFileC']);
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
	uploadFileAjax("#ebookFileC", fn('ebookFileC'));

	$("#f2").submit(function(e) {
		console.log($(this).serialize());
		queryGraph1('uploads/' + js['ebookFileC']);
		return false;
	});

	if (typeof meta_data !== 'undefined')
		showMetaData(meta_data);
	// showUploadedFile();

});