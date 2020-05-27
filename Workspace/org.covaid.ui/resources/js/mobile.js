const MOBILE_DEFAULT = '${context.covaid.mobile}';


const MOBILE_CREATE_URL = MOBILE_DEFAULT + 'create';
const MOBILE_SET_HEALTH = MOBILE_DEFAULT + 'health';
const MOBILE_SET_SAFETY = MOBILE_DEFAULT + 'safety';
const MOBILE_SET_EMAIL = MOBILE_DEFAULT + 'email';
const MOBILE_HEALTH_ADVICE = MOBILE_DEFAULT + 'health';

var id ${authentication.mobile.id};
var token ${authentication.mobile.token};
var identifier ${authentication.mobile.identifier};

function updateHealth() {
	// Call a method on the slider
	var value = $("#healthRange").val();
	var safety = $("#safetyRange").val();
	
	if( value < safety )
		$('#safetyRange').val( value );
	//console.log( value);
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
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return;
		}
		refreshCanvas( this.id, this.token, this.identifier);
	});
}

function setHealth( val){
	("#healthRange").value = val; 
}

function updateSafety() {
	// Call a method on the slider
	var value = $("#safetyRange").val();
	//console.log( value);
	let url = new URL(MOBILE_SET_SAFETY)
	url.search = new URLSearchParams({
	    id:this.id,
	    token:this.token,
	    identifier:this.identifier,
	    safety:value
	})
	fetch( url, {
		method: 'get',
		headers: {
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return;
		}
		refreshCanvas( this.id, this.token, this.identifier);
	});
}

function setSafety( val){
	("#safetyRange").value = val; 
}

function healthAdvice() {
	console.log('health advice');
	let url = new URL(MOBILE_HEALTH_ADVICE)
	url.search = new URLSearchParams({
	    id:this.id,
	    token:this.token,
	    identifier:this.identifier,
    	cough:$("#adviceCough").is(':checked'),
		fever:$("#adviceFever").is(':checked'),
		lot:$("#adviceLackOfTaste").is(':checked'),
		sorethroat:$("#adviceSoreThroat").is(':checked'),
		nasal:$("#adviceNasalCold").is(':checked'),
		temp:$("#adviceTemp").val()
	})
	fetch( url, {
		method: 'get',
		headers: {
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return;
		}
		refreshCanvas( this.id, this.token, this.identifier);
	});
}

function setEmail() {
	// Call a method on the slider
	var value = $("#email").val();
	//console.log( value);
	let url = new URL(MOBILE_SET_EMAIL)
	url.search = new URLSearchParams({
	    id:this.id,
	    token:this.token,
	    identifier:this.identifier,
	    email:value
	})
	fetch( url, {
		method: 'get',
		headers: {
			'Content-Type': 'application/json'
		},
	}).then(response => {
		if (!response.ok) {
			return response.json();
		}
		// Successful response, parse the JSON and return the data
		refreshCanvas( this.id, this.token, this.identifier);
	});
}

function setEmail( val){
	("#email").value = val; 
}

function load(){	
	updateHealth();
	updateSafety();
}