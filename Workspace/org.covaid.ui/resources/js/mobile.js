const MOBILE_DEFAULT = 'http://127.0.0.1:10080/covaid/mobile/rest/';
const MOBILE_CREATE_URL = MOBILE_DEFAULT + 'create';
const MOBILE_SET_HEALTH = MOBILE_DEFAULT + 'health';

var id ${authentication.mobile.id};
var token ${authentication.mobile.token};
var identifier ${authentication.mobile.identifier};

function registerMobile() {

	//Send the subscription details to the server using the Fetch API.
	fetch( MOBILE_CREATE_URL, {
		method: 'get',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return response.json()
			.catch(() => {
				// Couldn't parse the JSON
				throw new Error(response.status);
			})
			.then(({message}) => {
				// Got valid JSON with error response, use it
				throw new Error(message || response.status);
			});
		}
		// Successful response, parse the JSON and return the data
		var promise = response.json().then(function(parsedJson) {
			id = parsedJson.id;
			token = parsedJson.token;
			identifier = parsedJson.identifier;
			window.location.href='${link.index?id=' + id +'&token=' + token + '&identifier=' + identifier + '}';
			console.log(window.location.href);
		});
	});
}

function updateHealth() {
	// Call a method on the slider
	var value = $("#healthRange").val();
	console.log( value);
	let url = new URL(MOBILE_SET_HEALTH)
	url.search = new URLSearchParams({
	    id:this.id,
	    token:this.token,
	    identifier:this.identifier,
	    health:value
	})
	fetch( url, {
		method: 'get',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return response.json()
			.catch(() => {
				// Couldn't parse the JSON
				throw new Error(response.status);
			})
			.then(({message}) => {
				// Got valid JSON with error response, use it
				throw new Error(message || response.status);
			});
		}
		// Successful response, parse the JSON and return the data
		var promise = response.json().then(function(parsedJson) {
			id = parsedJson.id;
			token = parsedJson.token;
			identifier = parsedJson.identifier;
			window.location.href='${link.index?id=' + id +'&token=' + token + '&identifier=' + identifier + '}';
			console.log(window.location.href);
		});
	});
}

function load(){
	
	var originalVal;
	$('.healthRange').on('slideStop', function(ev){
		console.log('UPDATING');
		var newVal = $('#healthRange').data('slider').getValue();
		if(originalVal != newVal) {
			alert('Value Changed!');
		}
	});
}