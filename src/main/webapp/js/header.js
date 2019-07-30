// build a header dynamic
headers = [
	{name: 'CBook Generation', 'link': 'index.html'},
	{name: 'Extraction Combination', 'link': 'merge'},
	{name: 'Visualization', 'link': 'filter.jsp'},
//	{name: 'Question Visualization', 'link': 'question.html'},
	];

$(document).ready(function() {
	var s = "";
	for (var i = 0; i < headers.length; ++i) {
		s += '<a href="' + headers[i]['link'] + '">'+ headers[i]['name'] + '</a>';
		s += '&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp';
	}
	
	$('#header').html(s);

});
