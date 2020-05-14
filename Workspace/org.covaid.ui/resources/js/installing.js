const MOBILE_DEFAULT = ${context.covaid.mobile};

const MOBILE_CREATE_URL = MOBILE_DEFAULT + 'create';

var timer= setInterval( progressFunction, 5000 );

function progressFunction(){
	console.log("Installing app");	
}

function registerMobile() {

	//Send the subscription details to the server using the Fetch API.
	window.location.href='${link.installing}';
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

var loaded = false;
function load(){
	console.log('LOADING INSTALL PAGE');
	if(loaded)
	return;
	registerMobile();
	loaded = true;
}