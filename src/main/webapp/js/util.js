/*
 * Query and show graph
 * require: js/semanticGraph.js (to show)
 */
function queryGraph(dataFile) {
	$.ajax({
		type : "POST",
		url : "api/v1.1/graph",
		data : {
			fileName : dataFile,
			keepNodes: 400
		},
		dataType : "json",

		success : function(res) {
			data = JSON.parse(res.grData);
			console.info('data', data);
			build_graph(data);
			if (res.message) {
				$('#outputMessage').html(res.message);
			}
			
		},
		error : function(error) {
			console.log(error);
		},
		async : true,
		timeout : 60000
	});
}
