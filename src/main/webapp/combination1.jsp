<!DOCTYPE html>
<html lang="en">
<head>
<title>Generation book system</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<script src="///cdn.jsdelivr.net/npm/vue"></script>
<script type="text/javascript" src="vis/dist/vis.js"></script>
<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
<script type="text/javascript" src="vis/dist/vis-network.min.js"></script>
<link href="vis/dist/vis-network.min.css" rel="stylesheet"
	type="text/css" />
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
<!-- <link rel="stylesheet" type="text/css" href="css/style.css">
<link rel="stylesheet" type="text/css" href="css/style-combination.css"> -->
<style>
/* Remove the navbar's default margin-bottom and rounded borders */
.navbar {
	margin-bottom: 0;
	border-radius: 0;
}

/* Set height of the grid so .sidenav can be 100% (adjust as needed) */
.row.content {
	height: 450px
}

/* Set gray background color and 100% height */
.sidenav {
	padding-top: 20px;
	/* background-color: #f1f1f1; */
	height: 100%;
}

/* Set black background color, white text and some padding */
footer {
	background-color: #555;
	color: white;
	padding: 15px;
}

/* On small screens, set height to 'auto' for sidenav and grid */
@media screen and (max-width: 767px) {
	.sidenav {
		height: auto;
		padding: 15px;
	}
	.row.content {
		height: auto;
	}
}
</style>
<style>
/* Remove the navbar's default margin-bottom and rounded borders */
.navbar {
	margin-bottom: 0;
	border-radius: 0;
}

/* Set height of the grid so .sidenav can be 100% (adjust as needed) */
.row.content {
	height: 450px
}

/* Set gray background color and 100% height */
.sidenav {
	padding-top: 20px;
	/* background-color: #f1f1f1; */
	height: 100%;
}

/* Set black background color, white text and some padding */
footer {
	background-color: #555;
	color: white;
	padding: 15px;
}

/* On small screens, set height to 'auto' for sidenav and grid */
@media screen and (max-width: 767px) {
	.sidenav {
		height: auto;
		padding: 15px;
	}
	.row.content {
		height: auto;
	}
}

#progress-wrp {
	border: 1px solid #0099CC;
	padding: 1px;
	position: relative;
	height: 30px;
	border-radius: 3px;
	margin: 10px;
	text-align: left;
	background: #fff;
	box-shadow: inset 1px 3px 6px rgba(0, 0, 0, 0.12);
}
</style>

<script type="text/javascript">
		var data = ${gr_data};
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
	
</head>
<body>

	<nav class="navbar navbar-inverse">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target="#myNavbar">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>

			</div>
			<div class="collapse navbar-collapse" id="myNavbar">
				<ul class="nav navbar-nav">
					<li><a href="./index.html">CBook Generation</a></li>
					<li><a href="./combination.jsp">Extraction Combination</a></li>
					<li><a href="./filter.jsp">Visualization</a></li>
					<li><a href="./callLink.html">Call Link</a></li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<!-- <li><a href="#"><span class="glyphicon glyphicon-log-in"></span> Login</a></li> -->
				</ul>
			</div>
		</div>
	</nav>

	<div class="container-fluid text-center">
		<div class="row content">
			<div class="col-sm-2 sidenav">
				<!-- <p><a href="#">Link</a></p>
	      <p><a href="#">Link</a></p>
	      <p><a href="#">Link</a></p> -->
			</div>
			<form class="form" action="merge" method="post" enctype="multipart/form-data">

				<div class="col-sm-8 text-left">

					<div class="row">
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<h2 class="title" style="color: red;">Text Extraction
								Combination</h2>
							<br></br>
						</div>
						<div class="col-xs-4 col-sm-4 col-lg-4"></div>
					</div>

					<div class="row">
						<div class="col-xs-2 col-sm-2 col-lg-2">
							<label>Add Cbook</label>
						</div>
						<div class="col-xs-6 col-sm-6 col-lg-6">
							<input type="file" name="upload[]" id="fileupload"
								multiple="multiple" />
						</div>
						<div class="col-xs-4 col-sm-4 col-lg-4"></div>
					</div>
					<br></br>

					<div class="row">
						<div class="col-xs-2 col-sm-2 col-lg-2">
							<button type="button" id="deleteButton">Delete</button>
						</div>
						<div class="col-xs-2 col-sm-2 col-lg-2">
							<button type="button" id="mergeButton">Concept Merge</button>
						</div>

						<div class="col-xs-2 col-sm-2 col-lg-2">
							<button type="button" id="mergeSentenceButton">Sentence
								Merge</button>
						</div>

						<div class="col-xs-6 col-sm-6 col-lg-6"></div>
					</div>
					<br></br>
					<div class="row">
						<div class="col-xs-2 col-sm-2 col-lg-2">
							<label>Equivalence degree</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<input id="threshold" name="threshold" value="0.8" />
						</div>
					</div>

					<div class="row">

						<div class="col-xs-2 col-sm-2 col-lg-2"></div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<div id="progress-wrp">
								<div class="progress-bar"></div>
								<div class="status">0%</div>
							</div>
						</div>



					</div>

					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<div id="booksUploaded">
								<fieldset id="checkArray">
									<div v-for="cbook in cbooks">
										<input type="checkbox" name="cbookselect[]" v-model="cbook"
											:id="'cbook ' + cbook.fileName" :value="cbook.storedFileName" />
										{{cbook.fileName }}
									</div>
								</fieldset>
							</div>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8"></div>
					</div>

					<br></br>

					<div class="row">
						<div class="col-xs-2 col-sm-2 col-lg-2">Output: ${message}</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<a id="downloadLink"
								href="download?fileName=${combinedConceptFile}">Combined
								concepts</a>
						</div>
					</div>

				</div>

		</form>

		<div class="col-sm-2 sidenav">
		</div>
	</div>
	</div>
	<br />
	<br />
	<br />
	<div class="container-fluid">
		<div class="row">
			<div class="col-xs-12 col-sm-12 col-lg-12">
				<div class="ConceptVisualization">
					<div id="eachGraph" class="grid-container"></div>
		
					<div id="mynetwork"></div>
					<div id="infoDiv" class="graphInfo"></div>
				</div>
			</div>

		</div>


		<div id="nu"></div>
	</div>

</body>
</html>

