/* STOMP, SockJS, WebSockets */
var webSocketEndpoint = '/twitterSearch';
var subscriptionEndpointPrefix = '/queue/search/';

var stompClient = null;
var subscription = null;
var qTextInput = null;
var resultsBlock = null;

$(document).ready(function() {
	qTextInput = $('input#q');
	resultsBlock = $("#resultsBlock");
    // Create websocket
    connectWebSocket();
	registerSearch();
});

function registerSearch() {
	$("#search").submit(function(event){
		// Clean tweets
		resultsBlock.empty();
		subscribeTweetQuery(qTextInput.val());

		event.preventDefault();
	});
}

function connectWebSocket() {
	stompClient = Stomp.over(new SockJS(webSocketEndpoint));
	stompClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);
	});
}

function subscribeTweetQuery(tweetQuery) {
    console.log("TweetQuery: " + tweetQuery);
	// Unsubscribe previous query
	if (subscription != null) subscription.unsubscribe();
	// Subscribe new query
	subscription = stompClient.subscribe(subscriptionEndpointPrefix+tweetQuery, function(tweet){
		console.log('Received: ' + tweet);

        tweet = JSON.parse(tweet.body);
		var content = '';
		content +='<div class="row panel panel-default">'
				+ '<div class="panel-heading">'
				+ '		<a href="https://twitter.com/'+ tweet.fromUser +'" target="_blank"><b>@'+ tweet.fromUser +'</b></a>'
				+ '		<div class="pull-right">'
				+ '			<a href="https://twitter.com/'+ tweet.fromUser +'/status/'+ tweet.idStr +'" target="_blank"><span class="glyphicon glyphicon-link"></span></a>'
				+ '		</div>'
				+ '</div>'
				+ '<div class="panel-body">'+ tweet.unmodifiedText +'</div>'
				+ '</div>';
		$("#resultsBlock").append(content);
	}, function(error){
		// Error connecting to the endpoint
		console.log('Error: ' + error);
	});

	console.log("sending tweet query to /app/search");
	// Request search stream over the query
	stompClient.send("/app/search", {}, JSON.stringify({'query' : tweetQuery}));
}