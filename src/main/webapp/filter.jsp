<!DOCTYPE html>
<html lang="en">
<head>
<title>Generation book system</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
<script type="text/javascript" src="vis/dist/vis.js"></script>
<script type="text/javascript" src="vis/dist/vis-network.min.js"></script>
<link href="vis/dist/vis-network.min.css" rel="stylesheet"
	type="text/css" />
<!-- <link rel="stylesheet" type="text/css" href="css/autocomplete.css">
<link rel="stylesheet" type="text/css" href="css/style.css"> -->
<script src="js/autocomplete.js"></script>
	<script src="js/jsupload.js"></script>
	<script src="js/util.js"></script>
	<script src="js/semanticGraph.js"></script>
	<script>
		var data = ${gr_data};
	</script>
	<script>
		var id_values = ${default_id_values};
	</script>
	<script>
		var concepts = [ "computer", "os" ];
		var filter_lists = []; // contains the list of filters 
		var server_file = null; // if have is a string, a file name that uploaded to server, can be reused 
		
		function renderHTML() {
			var arrayLength = filter_lists.length;
			var str = "<br><ul style=\"list-style-type:circle\">";                       
     
			for (var i = 0; i < arrayLength; i++) {
    			// document.getElementById('list_filter').innerHTML = '{' + filter_lists + '}';
    			str += "<li>" + filter_lists[i] + "&emsp; <button class=\"btn btn-danger btn-sm\" id=\"btnRemove\" type=\"button\" value=\"Remove\" onClick=\"removeFilter(" + i + ")\">Remove</button>" + "</li>";  
    			//Do something
    		}
    		str += "</ul>";
    		return str;
		}
		
		// This function handles the the function of adding filter
		function addFilter() {			 			
			filter_lists.push(document.getElementById("filter_concept").value + ': ' + document.getElementById("numhops").value);
			document.getElementById('list_filter').innerHTML = renderHTML();	//hungchange		 
		}		
		
		function removeFilter(i) {
			filter_lists.splice(i, 1);
			document.getElementById('list_filter').innerHTML = renderHTML(); 
		}
		
		function showUploadedFile(fname) {
			$('#uploaded_file').val(server_file);
			$('#show_uploaded_file').html(server_file.substring(server_file.indexOf('.')+1));
			
			$.getJSON("api/getConceptsList?dataFileName=" + server_file,
					function(data, status) {
						concepts = data;
						// recall to update
						autocomplete(document.getElementById("filter_concept"),
								concepts);
					});
		}
		
		function uploadFileDone(data) {
			server_file = data.files[0].storedFileName;

			$.getJSON("api/getConceptsList?dataFileName=" + server_file,
					function(data, status) {
						concepts = data;
						// recall to update
						autocomplete(document.getElementById("filter_concept"),
								concepts);
					});
			
			if (server_file)
				showUploadedFile(server_file);
		}
		
		
		// call ajax /api/v1.1/filter and call /api/v1.1/graph to show
		function myDoPost() {
			$.ajax({
				type : "POST",
				url : "api/v1.1/filter",
				data: { 'uploaded_file' : $('#uploaded_file').val(), 'filters' : filter_lists.join('|')},
				dataType: "json",
				  
				success : function(res) {
					if (res.status == 0) {
						// this file always in downloads folder
						mergeFile = "downloads/" + res.filterFileName;
						queryGraph(mergeFile);
						$('#downloadLink').attr('href', 'download?fileName=' + res.filterFileName);
					} else {
						alert(res.message);
					}
				},
				error : function(error) {
					console.log(error);
				},
				async : true,
				timeout : 60000
			});
		}

		$(document).ready(function() {

			$("#filterForm").submit(function(e){
				myDoPost();
			    return false;
			});

			// for upload file when user choose (not wait to submit, use to get list of concept)
			$("#file").on("change", function(e) {
				var files = $(this)[0].files;
				for (var i = 0; i < files.length; ++i) {
					file = files[i];
					var upload = new Upload(file, uploadFileDone);

					// execute upload
					upload.doUpload();
				}
			});

			autocomplete(document.getElementById("filter_concept"), concepts);

			build_graph(data);
			
			// default value
			if (id_values && id_values['filter_concept'])
				$('#filter_concept').val(id_values['filter_concept']);
			if (id_values && id_values['numhops'])
				$('#numhops').val(id_values['numhops']);
			
			// reused uploaded file
			if (id_values && id_values['uploaded_file']) {
				$('#uploaded_file').val(id_values['uploaded_file']);
				server_file = id_values['uploaded_file'];
				showUploadedFile(server_file);
			}
			

		});
	</script>
	<script src="js/header.js"></script>

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
.form-control {
    margin-bottom: 15px;
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

				<!-- <a class="navbar-brand" href="#"><img  src="image/logo.png" alt="Phienbanmoi.com - Chia sẻ kiến thức - Download phần mềm"></a> -->
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
			<form action="filter" method="post"
					enctype="multipart/form-data" id="filterForm">
			
				<div class="col-sm-8 text-left">
				
				<div class="row text-center">
						<div class="col-xs-12 col-sm-12 col-lg-12">
							<h2 class="title" style="color:red;">Concept Space Visualization</h2>
							<br></br>
						</div>
						
					</div>


					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Cbook/VCbook</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<input  class="form-control" type="file" name="file" id="ebookFileC" value="" />
						</div>
					</div>
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Uploaded</label><label id="show_uploaded_file" style="width: 20em;"></label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<input  class="form-control" type="hidden" name="uploaded_file" id="uploaded_file" />
						</div>
					</div>

					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Filter concept:</label> 
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<input  class="form-control" type="text"
								name="filter_concept" id="filter_concept" />
						</div>
					</div>

					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Number of hops:</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<input  class="form-control" list="nhops" name="numhops" id="numhops" 
							value="1" />						
						<datalist id="nhops">
							<option value="1"></option>
							<option value="2"></option>
							<option value="3"></option>
							<option value="4"></option>
							<option value="5"></option>
							<option value="6"></option>
							<option value="7"></option>
							<option value="8">
							<option value="9">
							<option value="10">
							<option value="11">
							<option value="12">
							<option value="13">
							<option value="14">
							<option value="15">
							<option value="16">
							<option value="17">
							<option value="18">
							<option value="19">
							<option value="20">
							<option value="21">
							<option value="22">
							<option value="23">
							<option value="24">
							<option value="25">
							<option value="26">
							<option value="27">
							<option value="28">
							<option value="29">
							<option value="30">
						</datalist>
						</div>
					</div>
					
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<button class="btn btn-danger btn-md" id="btnAdd" type="button" onClick="addFilter()">Add</button>
						</div>
					</div>
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<div class="autocomplete">
							<label>List of filters</label> 
								<span id="list_filter"> </span>
						</div>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<br></br>
						</div>
					</div>
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Color scheme</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<select class="form-control">
							<option value="volvo">Automatically</option>
							<option value="saab">Manually defined by users</option>
						</select>
						</div>
					</div>
					
					
					
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Property</label> 
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<select class="form-control" name="slProperty">
								<option value="EbookName">Ebook name</option>
								<option value="ConceptType">Concept type</option>
								<option value="Frequency">Frequency</option>
								<option value="Page">Page</option>
							</select>
						</div>
					</div>
					
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Visualization</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<select class="form-control" name="slVisualization">
								<option value="LightWeight">Link weight</option>
								<option value="LinkColor">Link color</option>
								<option value="NodeShape">Node shape</option>
								<option value="NodeFillColor">Node fill color</option>
								<option value="NodeOutlineColor">Node outline color</option>
							</select>
						</div>
					</div>
					
					
					
					
					
					
					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<label>Processing location</label>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<select class="form-control">
								<option value="volvo">Local PC</option>
								<option value="saab">VWebServer</option>
								<option value="mercedes">Super Computing</option>
							</select>
						</div>
					</div>
					
					
					<div class="row text-center">
						<div class="col-xs-12 col-sm-12 col-lg-12">
							<div class="submitbtn">
								<button class="btn btn-danger btn-md" id="btnSubmit" type="submit">Submit</button>
							</div>
						</div>
							<br></br><br></br>
					</div>
					
					
					

					<div class="row">
						<div class="col-xs-4 col-sm-4 col-lg-4">
							<div>Output: ${message}</div>
						</div>
						<div class="col-xs-8 col-sm-8 col-lg-8">
							<a href="download?fileName=${conceptFile}" id="downloadLink">Concept File</a>
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
					<div id="mynetwork"></div>
					<div id="infoDiv" class="graphInfo"></div>
				</div>
			</div>

		</div>


		<div id="nu"></div>
	</div>
	<!-- <footer class="container-fluid text-center">
		<p>Footer Text</p>
	</footer> -->

</body>
</html>

<!-- <script>
	setInterval(function(){ 	document.getElementsByClassName('vis-network')[0].childNodes[0].style.height = '400px' }, 1000);

	</script> -->
