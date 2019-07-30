<!DOCTYPE html>
<html lang="en">
<head>
<title>Concept filter</title>

<script
	src="///ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript" src="vis/dist/vis.js"></script>

<link href="vis/dist/vis-network.min.css" rel="stylesheet"
	type="text/css" />
<link rel="stylesheet" type="text/css" href="css/autocomplete.css">
<link rel="stylesheet" type="text/css" href="css/style.css">
</head>

<body>
	<div class="mainpart">
		<div class="center">
			<div id="header"><a href="./index.html" >CBook Generation</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				<a href="./merge">Extraction Combination</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				<a href="./filter.jsp">Visualization</a> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
			</div>
		
			<div class="border">
				<h2 class="textcenter">Concept Space Visualization</h2>
			</div>
			<br /> <br />
			<div class="alignleft">
				<form class="form" action="filter" method="post"
					enctype="multipart/form-data" id="filterForm">
					<div>
						<label>Cbook/VCbook</label><input type="file" name="file"
							id="file" />
					</div>
					<div><label>Uploaded</label><label id="show_uploaded_file" style="width: 20em;"></label>
					<input type="hidden" name="uploaded_file" id="uploaded_file" />
					</div>

					<div>
						<div class="autocomplete" style="width: 400px;">
							<label>Filter concept:</label> <input type="text"
								name="filter_concept" id="filter_concept" />
						</div>
					</div>
					<!-- div id="ajaxGetUserServletResponse"></div>
					<div id="filterConceptResult"></div-->
					<div>
						<label>Number of hops:</label> <input list="nhops" name="numhops" id="numhops" 
							value="1" />						
						<datalist id="nhops">
							<option value="1">
							<option value="2">
							<option value="3">
							<option value="4">
							<option value="5">
							<option value="6">
							<option value="7">
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
					<div class="addbtn">
						<input id="btnAdd" type="button" style="margin-left: 60px;" value="Add" onClick="addFilter()"/>						
					    </div>
					    <div class="autocomplete" style="width: 400px;">
							<label>List of filters:</label> 
								<span id='list_filter'> </span>
						</div>
					
					<div>
						<label>Color scheme</label> <select>
							<option value="volvo">Automatically</option>
							<option value="saab">Manually defined by users</option>
						</select>
					</div>
					<div>
						<label>Property</label> <select name="slProperty">
							<option value="EbookName">Ebook name</option>
							<option value="ConceptType">Concept type</option>
							<option value="Frequency">Frequency</option>
							<option value="Page">Page</option>
						</select>
					</div>
					<!-- div id="divProperty" ></div-->
					<div>
						<label>Visualization</label> <select name="slVisualization">
							<option value="LightWeight">Link weight</option>
							<option value="LinkColor">Link color</option>
							<option value="NodeShape">Node shape</option>
							<option value="NodeFillColor">Node fill color</option>
							<option value="NodeOutlineColor">Node outline color</option>
						</select>
					</div>
					<div>
						<label>Processing location</label> <select>
							<option value="volvo">Local PC</option>
							<option value="saab">VWebServer</option>
							<option value="mercedes">Super Computing</option>
						</select>
					</div>
					<br />
					<div class="submitbtn">
						<input id="btnSubmit" type="submit" style="margin-left: 60px;" />
					</div>
				</form>


				<br />
				<div>Output: ${message}</div>
				<div>
					<a href="download?fileName=${conceptFile}" id="downloadLink">Concept File</a>
				</div>

			</div>
		</div>


		<div class="ConceptVisualization">
			<div id="mynetwork"></div>
			<div id="infoDiv" class="graphInfo"></div>
		</div>
	</div>

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
    			str += "<li>" + filter_lists[i] + "&emsp; <input id=\"btnRemove\" type=\"button\" value=\"Remove\" onClick=\"removeFilter(" + i + ")\"/>" + "</li>";  
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
</body>
</html>
