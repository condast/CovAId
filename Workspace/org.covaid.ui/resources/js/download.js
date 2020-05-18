const MOBILE_DEFAULT = '${context.covaid.mobile}';

const MOBILE_CREATE_URL = MOBILE_DEFAULT + 'create';

function registerMobile() {
	//Send the subscription details to the server using the Fetch API.
	window.location.href='${link.installing}';
}

function load(){
	console.log('LOADING DOWNLOAD PAGE');
}