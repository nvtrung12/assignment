<!DOCTYPE html>
<html lang="en">
<head>
<title>Text Extraction Combination</title>

<script
	src="///ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script src="///cdn.jsdelivr.net/npm/vue"></script>
<script type="text/javascript" src="http://visjs.org/dist/vis.js"></script>
<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>

<link href="http://visjs.org/dist/vis-network.min.css" rel="stylesheet"
	type="text/css" />

	<link rel="stylesheet" type="text/css" href="css/style.css">
<link rel="stylesheet" type="text/css" href="css/style-combination.css">

</head>

<body>
	<div class="metaData topcorner" id="metaData"></div>
	<div>
		<div class="center">
			<div id="header"><a href="./index.html" >CBook Generation</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				<a href="./merge">Extraction Combination</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				<a href="./filter.jsp">Visualization</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
			</div>

			<div class="border">
				<h2 class="textcenter">Text Extraction Combination</h2>
			</div>
			<br /> <br />
			<div class="alignleft">
				<form class="form" action="merge" method="post"
					enctype="multipart/form-data">
					<div class="buttonArea">
						<label>Add Cbook</label> <input type="file" name="upload[]"
							id="fileupload" multiple="multiple" />
						&nbsp&nbsp&nbsp
						<button type="button" id="deleteButton">Delete</button>
						&nbsp&nbsp&nbsp
						<button type="button" id="mergeButton">Concept Merge</button>
						&nbsp&nbsp&nbsp
						<button type="button" id="mergeSentenceButton">Sentence Merge</button>
						<br/>

					</div>
					<div>
						<label>Equivalence degree</label> <input id="threshold" name="threshold" value="0.8"/> <br/>
					</div>
					<div id="progress-wrp">
						<div class="progress-bar"></div>
						<div class="status">0%</div>
					</div>
					<br/>
					<div id="booksUploaded">
					<fieldset id="checkArray">
						<div v-for="cbook in cbooks">
							<input type="checkbox" name="cbookselect[]" v-model="cbook"
								:id="'cbook ' + cbook.fileName" :value="cbook.storedFileName" /> <!-- label-->{{cbook.fileName }}<!-- /label-->
						</div>
					</fieldset>
					</div>

				<div>Output: ${message}</div>
				<div>
					<a id="downloadLink"
						href="download?fileName=${combinedConceptFile}">Combined
						concepts</a>
				</div>

				</form>

			</div>
		</div>


		<div class="ConceptVisualization">
			<div id="eachGraph" class="grid-container"></div>

			<div id="mynetwork"></div>
			<div id="infoDiv" class="graphInfo"></div>
		</div>
	</div>

	<script>
		var data = ${gr_data};
	</script>
	<script>
		//var meta_data = ${meta_data};
	</script>
	<script src="js/semanticGraph.js"></script>
	<script src="js/jsupload.js"></script>
	<script>

	var lstBook = [];
	var mergeFile = null;

	var demo = new Vue({
		  el: "#booksUploaded",
		  data() {
		    return {
		      cbooks: []
		    };
		  }
		});

	// this function call merge concept only
	function postMerge(tmp, divId="mynetwork", realNames="") {
		$.ajax({
			type : "POST",
			url : "api/v1.1/merge",
			data: { test : tmp, 'books' : tmp, 'realNames': realNames},
			dataType: "json",

			success : function(res) {
				// store merge file to reuse in merge sentence
				// in this case, file always in downloads folder
				mergeFile = "downloads/" + res.file;
				$('#downloadLink').attr('href', 'download?fileName=' + res.file);
				queryGraph(mergeFile, divId);
			},
			error : function(error) {
				console.log(error); // TODO
			},
			async : true,
			timeout : 60000
		});
	}

	// merge concept done and call this function to get graph (instead return both info)
	// support for future api 1.1+
	// use default parameters of ES6/ES2015
	function queryGraph(mergeFile=null, divId="mynetwork") {
		$.ajax({
			type : "POST",
			url : "api/graph",
			data: { fileName : mergeFile, threshold: $('#threshold').val()},
			dataType: "json",

			success : function(res) {
				data = JSON.parse(res.grData);
				buildGraph1(data, divId);
			},
			error : function(error) {
				console.log(error); // TODO
			},
			async : true,
			timeout : 60000
		});
	}

	// this step must be run after merge concept, so we have merge file
	// only need new graph based on it
	function mergeSentence() {
		if (!mergeFile) {
			alert ("You do not merge concepts yet, please click Concept Merge");
			return;
		}

		$.ajax({
			type : "POST",
			url : "api/graph",
			data: { fileName : mergeFile, graphType: "mergeSentence"},
			dataType: "json",

			success : function(res) {
				data = JSON.parse(res.grData);
				buildGraph1(data, 'mynetwork');
			},
			error : function(error) {
				console.log(error); // TODO
			},
			async : true,
			timeout : 60000
		});
	}

	function doConceptMerge() {
		// only get checked
		var checkedX = $('#checkArray:checkbox:checked');
		checkedX = $('input[name="cbookselect[]"]:checked');
		checkedFiles = [];
		var realCheckedFiles = [];
		for (var i = 0; i < checkedX.length; ++i) {
			checkedFiles.push(checkedX[i].value);
			realCheckedFiles.push(checkedX[i].id);
		}

		var tmp = checkedFiles.join(':');
		var realNames = realCheckedFiles.join(':');

		postMerge(tmp, 'mynetwork', realNames);
	}

	$(document).ready(function() {

		function additionUploadDone(data) {
			lstBook.push.apply(lstBook,data.files);
			demo.cbooks = lstBook;
			// make div and show this graph in it
			for (var i = 0; i < data.files.length; ++i) {
				var sname = data.files[i].storedFileName;
				$('#eachGraph').prepend($('<div class="eachSubGraph grid-item" id="'+sname+'"></div>'));
				postMerge(sname, sname, data.files[i].fileName);
			}
		}

		// Change id to your id
		$("#fileupload").on("change", function(e) {
			// var file = $(this)[0].files[0];
			var files = $(this)[0].files;
			for (var i = 0; i < files.length; ++i) {
				file = files[i];
				var upload = new Upload(file, additionUploadDone);

				// maby check size or type here with upload.getSize() and
				// upload.getType()

				// execute upload
				upload.doUpload();
			}
			$("#fileupload").val('');
		});

		$("#mergeButton").on("click", function(e){
			doConceptMerge();
		});

		$("#mergeSentenceButton").on("click", function(e){
			mergeSentence();
		});


		$("#deleteButton").on("click", function(e) {
			var checkedX = $('#checkArray:checkbox:checked');
			checkedX = $('input[name="cbookselect[]"]:checked');
			checkedFiles = [];
			var x=new Set()

			for (var i = 0; i < checkedX.length; ++i)
				x.add(checkedX[i].value);

			// filter lstBook, remove checked
			for (var i=lstBook.length-1; i >= 0 ; --i) {
				if (x.has(lstBook[i].storedFileName))
					lstBook.splice(i, 1);
			}

			demo.cbooks = lstBook;
		});

	});


	</script>
	<script src="js/header.js"></script>

</body>
</html>
