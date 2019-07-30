// credit: https://stackoverflow.com/questions/2320069/jquery-ajax-file-upload
var Upload = function(file, callback) {
	this.file = file;
	this.callback = callback
};

Upload.prototype.getType = function() {
	return this.file.type;
};
Upload.prototype.getSize = function() {
	return this.file.size;
};
Upload.prototype.getName = function() {
	return this.file.name;
};
Upload.prototype.doUpload = function() {
	var that = this;
	var formData = new FormData();

	// add assoc key values, this will be posts values
	formData.append("file", this.file, this.getName());
	formData.append("upload_file", true);

	$.ajax({
		type : "POST",
		url : "api/upload",
		xhr : function() {
			var myXhr = $.ajaxSettings.xhr();
			if (myXhr.upload) {
				myXhr.upload.addEventListener('progress',
						that.progressHandling, false);
			}
			return myXhr;
		},
		success : function(data) {
			if (that.callback)
				that.callback(data);
		},
		error : function(error) {
			console.log(error); // TODO
		},
		async : true,
		data : formData,
		cache : false,
		contentType : false,
		processData : false,
		timeout : 60000
	});
};

Upload.prototype.progressHandling = function(event) {
	var percent = 0;
	var position = event.loaded || event.position;
	var total = event.total;
	var progress_bar_id = "#progress-wrp";
	if (event.lengthComputable) {
		percent = Math.ceil(position / total * 100);
	}
	// update progressbars classes so it fits your code
	$(progress_bar_id + " .progress-bar").css("width", +percent + "%");
	$(progress_bar_id + " .status").text(percent + "%");
};