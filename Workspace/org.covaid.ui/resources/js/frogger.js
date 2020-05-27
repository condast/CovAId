const COVAID_DEFAULT = '${context.covaid.rest}';

const COVAID_ADVICE = COVAID_DEFAULT + 'getAdvice';

function getAdvice() {
	let url = new URL(COVAID_ADVICE)
	url.search = new URLSearchParams({
	    id:this.id,
	    token:this.token,
	    identifier:this.identifier
	})
	fetch( url, {
		method: 'get',
		headers: {
			'Content-Type': 'application/json'
		},
	}).then(response => response.text())
	  .then(( text ) => {
		$("#adviceBody").text( text );
		refreshCanvas( this.id, this.token, this.identifier);
	});
}
