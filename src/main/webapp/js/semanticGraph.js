function flog(params) {
	// document.getElementById('infoDiv').innerHTML = params;
}
function process_doubleClick(nodeId) {
	var content = '';
	content += 'Concept: ' + nodeId + '<br/>';
	// content += 'Node Id: ' + nodeId + '<br/>';
	content += 'Sentence: ' + JSON.stringify(data['metaInfo'][nodeId]);

	document.getElementById('infoDiv').innerHTML = content;
	console.log(data['metaInfo'][nodeId]);
}

function drawHistogram(xdata, divKey) {
	var trace = {
		x : xdata,
		type : 'histogram',
	};
	var data = [ trace ];
	Plotly.newPlot(divKey, data);
}

function showMetaData(meta_data) {
	var out = "";

	out += "run time (s): " + meta_data['runtime']/1000 + "<br/>";
	out += "number of page: " + meta_data['numberOfPage'] + "<br/>";
	out += "number of word: " + meta_data['numberOfWord'] + "<br/>";
	out += "number of sentence: " + meta_data['numberOfSentence'] + "<br/>";
	out += "unique word: " + meta_data['uniqueWord'] + "<br/>";

	out += "concept degree distribution average: "
			+ meta_data['conceptDegreeDistributionAverage'].toFixed(4) + "<br/>";
	out += "sentence degree distribution average: "
			+ meta_data['sentenceDegreeDistributionAverage'].toFixed(4) + "<br/>";

	out += "number of sentence nodes: " + meta_data['numberOfSentenceNodes']
			+ "<br/>";
	out += "unique domain concept: " + meta_data['uniqueDomainConcept']
			+ "<br/>";
	out += "number of sentences to concept links: "
			+ meta_data['numberOfSentencesToConceptLinks'] + "<br/>";

	out += '<div id="histogram_sent" class="histogram_div"></div>';
	out += '<div id="histogram_concept" class="histogram_div"></div>';

	document.getElementById('metaData').innerHTML = out;

	// drawHistogram(meta_data['sentenceDegreeDistributionHis'],'histogram_sent')
	// drawHistogram(meta_data['conceptDegreeDistributionHis'],'histogram_concept')
	// JSON.stringify(meta_data);
}

// create a network
function build_graph(data) {
	buildGraph1(data, 'mynetwork');
}

function buildGraph1(data, divId) {
	var container = document.getElementById(divId);
	var visjs_data = {
		nodes : new vis.DataSet(data.nodes),
		edges : new vis.DataSet(data.edges)
	};
	var options = {
		interaction : {
			hover : true,
			tooltipDelay : 3600000
		},
		layout: {improvedLayout:false},
		physics:{
			enabled: true,
	        stabilizations:true,
	        adaptiveTimestep: false,
	        stabilization: {
	            iterations: 1
	          }
	   }
	};

	var network = new vis.Network(container, visjs_data, options);
	network.stabilize(1);

	network.on("click", function(params) {
		// params.event = "[original event]";
		// flog('<h2>Click event:</h2>' + JSON.stringify(params, null, 4));
		// console.log('click event, getNodeAt returns: '
		// + this.getNodeAt(params.pointer.DOM));
		// process_doubleClick(params['nodes']);
	});
	network.on("doubleClick", function(params) {
		// params.event = "[original event]";
		// flog('<h2>doubleClick event:</h2>' + JSON.stringify(params, null,
		// 4));
		// process_doubleClick(params['nodes']);
		network.interactionHandler._checkShowPopup(params.pointer.DOM);
	});
	network.on("oncontext", function(params) {
		params.event = "[original event]";
		flog('<h2>oncontext (right click) event:</h2>'
				+ JSON.stringify(params, null, 4));
	});

	network.on("dragStart", function(params) {
		// There's no point in displaying this event on screen, it gets
		// immediately overwritten
		params.event = "[original event]";
		console.log('dragStart Event:', params);
		console.log('dragStart event, getNodeAt returns: '
				+ this.getNodeAt(params.pointer.DOM));
	});
	network.on("dragging", function(params) {
		params.event = "[original event]";
		flog('<h2>dragging event:</h2>' + JSON.stringify(params, null, 4));
	});
	network.on("dragEnd", function(params) {
		params.event = "[original event]";
		flog('<h2>dragEnd event:</h2>' + JSON.stringify(params, null, 4));
		console.log('dragEnd Event:', params);
		console.log('dragEnd event, getNodeAt returns: '
				+ this.getNodeAt(params.pointer.DOM));
	});
	network.on("zoom", function(params) {
		flog('<h2>zoom event:</h2>' + JSON.stringify(params, null, 4));
	});
	network.on("showPopup", function(params) {
		// flog ('<h2>showPopup event: </h2>' + JSON.stringify(params, null,
		// 4));
	});
	network.on("hidePopup", function() {
		console.log('hidePopup Event');
	});
	network.on("select", function(params) {
		console.log('select Event:', params);
	});
	network.on("selectNode", function(params) {
		console.log('selectNode Event:', params);
	});
	network.on("selectEdge", function(params) {
		console.log('selectEdge Event:', params);
	});
	network.on("deselectNode", function(params) {
		console.log('deselectNode Event:', params);
	});
	network.on("deselectEdge", function(params) {
		console.log('deselectEdge Event:', params);
	});
	network.on("hoverNode", function(params) {
		console.log('hoverNode Event:', params);
	});
	network.on("hoverEdge", function(params) {
		console.log('hoverEdge Event:', params);
	});
	network.on("blurNode", function(params) {
		console.log('blurNode Event:', params);
	});
	network.on("blurEdge", function(params) {
		console.log('blurEdge Event:', params);
	});
}
