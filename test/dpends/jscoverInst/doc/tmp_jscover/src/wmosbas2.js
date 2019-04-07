var gpioStatesA = "00000000";
var relayLabelA = "                                                        ";
// . . . . . . . . 12345671234567123456712345671234567123456712345671234567
// . . . . . . . . x     xx     xx     xx     xx     xx     xx     xx     x
// ABOVE: variables that hold the state and labels for the 8 relays that are controlled

var g_orangeShade = "#ffbb33";
var g_bodykeystate = false;
var g_bodykeyTimerObject;
var g_bodykeyTimerVal = 300;

function onloadInit(){
	makeSvgButt();
	
	setConfigHide(); 
	setButtonNames(); 
	setButtonState();
	
	setViewFromCookie();
}

function makeSvgButt(){
	var cx = 28;
	var cy = 16;
	var rx = 26;
	var ry = 10;
	
	var i;
	for(i = 1; i <= 8; i++){
		var ellipseEle = document.getElementById("slo" + i);
		
		ellipseEle.setAttribute('cx', "50%");   // center to SVG tag
		ellipseEle.setAttribute('cy', "50%");   // center to SVG tag
		ellipseEle.setAttribute('rx', rx);
		ellipseEle.setAttribute('ry', ry);	
		
		ellipseEle.setAttribute('class' ,"svgbut svgoff");
		
		// the text will be centered to the ellipse
		var textEle = document.getElementById("svgt" + i);
		
		var navAgent = navigator.userAgent;
		textEle.setAttribute('x', "50%");   // center to SVG tag
		
		if(navAgent.indexOf("Edge") > -1){
			textEle.setAttribute('y', "63%");   // center to SVG tag
			// textEle.setAttribute('dominant-baseline', "middle");   // center to text tag
		}else{
			// not supported Edge Dec2018
			textEle.setAttribute('y', "50%");   // center to SVG tag
			textEle.setAttribute('dominant-baseline', "central");   // center to text tag
		}
		textEle.setAttribute('class' ,"svgtxtoff");
	}
}

function getById(idOfEle){
	return document.getElementById(idOfEle);
}

function setConfigHide(){
	// cfgbutt will show and used to toggle hide/show form
	getById("cfgbutt").removeAttribute('style');
	getById("cfgformtr").style.display = 'none';
}

function openConfig(ele){
	getById("cfgbutt").style.display = 'none';
	getById("cfgformtr").removeAttribute('style');
	
	// copy current text setting from the labels to the input
	// fields
	var i;
	for(i = 1; i <= 8; i++){
		var cfgNEle = getById("cfg" + i);
		var inpEle = getById("inp" + i);
		inpEle.value = cfgNEle.innerText;
	}
}

function cfgClose(){
	setConfigHide();
}

function queryRefresh(ele){
	 queryConfig(ele);
	 queryRelayState(ele);
 }

function queryRelayState(ele){
	relayAction(ele, "RLY=OQuery");
}
function relayActionResponse(srcThis){
	if (srcThis.readyState === 4 && srcThis.status === 200) {
		var tstModeEle = document.getElementById('testmode1234');
		if(tstModeEle === null){
			relayActionResponseTstMode(srcThis);
		}else{
			var getTimerTxt = tstModeEle.innerHTML.split(":");
			var timerVal = getTimerTxt[0];
			
			// running in test mode so set a timer arrangement
			setTimeout(function(){relayActionResponseTstMode(srcThis);}, timerVal);
		}
	}		
}
function relayActionResponseTstMode(srcThis){
	// gpioStatesA = this.responseText;	
		var resp = srcThis.responseText;
		
		var error = '';
		
		if(resp.endsWith('not okay')){
			error = 'I2C communication error, check cabling';
			
			var splitMsgArr = resp.split("\r");
			
			error = splitMsgArr[splitMsgArr.length - 2];
			
		}else{
		
			var equalIndex = resp.indexOf("=");
			if(equalIndex > -1){
				var actionChar = resp.substring(equalIndex + 1, equalIndex + 2);
				var xChar = resp.substring(equalIndex + 2, equalIndex + 3).toLowerCase();
				if(actionChar === "R"){
					resp = responseNrly(resp, equalIndex, xChar);
				}else if(actionChar === "A"){
					resp = responseAllRly(resp, equalIndex, xChar);
				}
			}
			// set the gpio state
			gpioStatesA = resp;
		}
		var errorMsgEle = document.getElementById('errmsg');
		errorMsgEle.innerHTML = error;
		
		resetAllRefreshButton(["rfh"]);
		setButtonState();
}

function relaySVGkeyup(evt, ele){
	if(evt.code === "Enter" || evt.code === "Space"){
		relayToggle(ele);
	}
}

function relayToggle(eleTdParent){
	// get the ellipse sub-element
	var ele = eleTdParent.getElementsByTagName('ellipse')[0];
	
	// only for use on SVG element
	var idEle = ele.id;
	
	var idNum = idEle.substring(idEle.length - 1);
	
	// the SVG is controlled via colors as to its state and
	// is a class setting on the object
	var clzList = ele.classList;
	
	var currentStateOn = false;
	
	if(clzList.contains('svgon')){
		currentStateOn = true;
	}else if(clzList.contains('svgontooff')){
		return; // must be in a solid state to toggle
		// currentStateOn = true;
	}else if(clzList.contains('svgoff')){
		currentStateOn = false;
	}else if(clzList.contains('svgofftoon')){
		return; // must be in a solid state to toggle
		// currentStateOn = false;			
	}else {
		alert('internal error color setting state a');
		return;
	}
	if(currentStateOn){
		relayAction(ele, "RLY=OF" + idNum);	
	}else{
		relayAction(ele, "RLY=ON" + idNum);	
	}
}
 
function relayAction(ele, rlyRequest) {
	// if we are in the config mode, do not process the request
	var cfgbuttEle = document.getElementById('cfgbutt');
	if(cfgbuttEle.style.display === 'none'){
		document.getElementById('cfgclzbutt').focus();
		return;
	}
	var xhttp = new XMLHttpRequest();
  
	xhttp.open("GET", rlyRequest, true);
	xhttp.onreadystatechange = function() {relayActionResponse(this);};
	xhttp.send();
  
	//  0123456
	// 'RLY=ONn   RLY=OFn   or RLY=ONAll RLY=OFAll
	var nOrAllChar = rlyRequest.substring(rlyRequest.length - 1);
  
	// ends 'l' an All request, 'q' is query 
	// when a query need to process the response
	if(nOrAllChar === "y"){
		ele.parentNode.style.background = g_orangeShade;
		return;
	}
	if(nOrAllChar !== "l" ){  
		// the relay request will take some time to be processed and a 
		// response to come back. So set a pending state
		var idxChar = parseInt(nOrAllChar);
		idxChar--;

		var replaceChar;
		if(rlyRequest.indexOf("=ON") > -1){
			// on-pending request
			replaceCode = "3";
		}else{
			// off-pending request
			replaceCode = "4";
		}
		gpioStatesA = gpioStatesA.substring(0, idxChar) 
	                            + replaceCode 
				    + gpioStatesA.substring(idxChar+1);
	}else{
		ele.parentNode.style.background = g_orangeShade;
	}
	setButtonState();
}

function responseAllRly(resp, equalIndex, xChar){
	// ["ona", "ofa"]);
	resetAllRefreshButton(["o" + xChar.toLowerCase() + "a"]);
	resp = resp.substring(0, 8);
			
	// pending states need to remain as they are queued
	// on the server
	var j;
	var respAlter = "";
	for(j =0; j < 8; j++){
		var state = gpioStatesA.substring(j, j+1); 	
		// pending 3->oN, 4->oFf
				
		if((state === "3" && xChar === "f" ) || 
		   (state === "4" && xChar === "n") ){
			respAlter += state;
		}else{
			respAlter +=  resp.substring(j, j+1);
		}
	}
	return respAlter;
}

function responseNrly(resp, equalIndex, xChar){
	// this was a single relay action, so only affect the specific
	// relay as such
	var idxChar = parseInt(xChar);
	idxChar--;
	resp = "" + gpioStatesA.substring(0, idxChar) 
              + resp.substring(idxChar, idxChar+1)
			  + gpioStatesA.substring(idxChar+1);
	return resp;
}

function queryConfig(ele) {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "CONFIGQUERY", true);
   
	xhttp.onreadystatechange = function() {
		if (this.readyState === 4 && this.status === 200) {
			relayLabelA = this.responseText;
			setButtonNames();
		}	
	};
	xhttp.send();
}

function resetAllRefreshButton(buttArr){
	var i;
	for(i = 0; i < buttArr.length; i++){
		getById(buttArr[i]).parentNode.style.background = 'white';
	}
}
function override(evt, thisEle){
	
	if(evt.key === "Tab"){
		return;
	}
	evt.preventDefault();
	
	// flow thru the radio buttons
	var viewButtArr = document.getElementsByName('buttview');
	
	var i;
	var iInArrOfThisEle = -1;
	for(i =0; i < viewButtArr.length; i++){
		if(viewButtArr[i] === thisEle){
			iInArrOfThisEle = i;
		}
	}
	if(iInArrOfThisEle === -1){
		return;
	}
	// left arrow 
	if(evt.key === 'ArrowRight'){
		iInArrOfThisEle++;
		if(iInArrOfThisEle >= viewButtArr.length){
			iInArrOfThisEle = 0;
		}
		
	}else if(evt.key === 'ArrowLeft'){
		iInArrOfThisEle--;
		if(iInArrOfThisEle === -1){
			iInArrOfThisEle = viewButtArr.length -1;
		}
	}
	viewButtArr[iInArrOfThisEle].focus();
	
}



/**
Body key up action which supports 1 thru 8 for relay toggle on-off and
n, f, r for on-all, off-all, refresh respectively
*/
function bodykey(evt){
	// if the config button is displayed as none, the page is in edit mode for
	// the labels
	// if(document.getElementById('cfgbutt').style.display === 'none'){
		// // document.getElementById('cfgclzbutt').focus();
		// return;
	// }
	
	if (evt !== undefined) {
		var cRaw = evt.which;
		// '1' <= 49, '8' <= 56
		var c = String.fromCharCode(cRaw);
		
		if(!g_bodykeystate){
			g_bodykeystate = c === "A";
			
			// need to type in another character within 100ms for the 
			// 2nd key to be accepted
			g_bodykeyTimerObject = setTimeout(function(){bodykeyTimerOff();}, g_bodykeyTimerVal);
			return;
		}
		bodykeyTimerOff();
		clearTimeout(g_bodykeyTimerObject);
		
		if (c >= "1" && c <= "8") {
			// if the config button is not being displayed
			// then config has been opened for entering data
			// so 
			if(getById("cfgbutt").style.display === 'none'){
				document.getElementById('cfgclzbutt').focus();
				return;
			}
			var keyEle = "slo" + c;
			var ele = document.getElementById(keyEle);
			
			var controlEle = ele.parentNode.parentNode;
			relayToggle(controlEle);
			
			// if the svg toggle switches are visible then set the
			// focus to the toggle switch (if double buttons only then)
			var cookieVal = document.cookie;
	
			if( cookieVal.indexOf("double-buttons") > -1){
				return;
			}
			// single or both means the svg toggle buttons are visible			
			controlEle.parentNode.focus();
			return;
		}
		var clickOn = "";
		// 'r' <= 
		if(c === "R"){
			clickOn = "rfh";
		} else if(c === "N"){
			clickOn = "ona";
		} else if(c === "F"){
			clickOn = "ofa";
		}
		if(clickOn !== ""){
			// cause the appropriate button to be clicked
			var butt2Clk = document.getElementById(clickOn);
			butt2Clk.click();
		}
	}
}

function bodykeyTimerOff(){
	g_bodykeystate = false;
}

function setButtonState(){	
	svgButtonSetter();
	buttonStateSetter();
}

function svgButtonSetter(){
	// svg state may have the following values
	// 0 -> on
	// 1 -> off
	// 2 -> off to on
	// 3 -> on to off
	//
	var i;
	for(i = 1; i <= 8; i++){	
		var svgEle = getById("slo" + i);
		if(svgEle === null){
			// not configured
			return;
		}
		var svgTextEle = getById("svgt" + i);
		
		// the SVG is controlled via colors as to its state and
		// is a class setting on the object
		var clzList = svgEle.classList;
		
		var currentState;
		if(clzList.contains('svgon')){
			currentState = 0;
			clzList.remove('svgon');
			
		}else if(clzList.contains('svgontooff')){
			currentState = 3;
			clzList.remove('svgontooff');
			
		}else if(clzList.contains('svgoff')){
			currentState = 1;
			clzList.remove('svgoff');
			
		}else if(clzList.contains('svgofftoon')){
			currentState = 2;
			clzList.remove('svgofftoon');
			
		}else {
			alert('internal error color setting state');
			return;
		}
		// the new state as per the returned value from the board (WeMos D1)
		// 0 -> on
		// 1 -> off
		// 3 -> pending on
		// 4 -> pending off
		var valueState = parseInt(gpioStatesA.substring(i-1, i));
		
		var stateOnBool = valueState === 1;
		var pendingStateBool = valueState > 2;
		var pendingOnBool = valueState === 3;
		
		svgTextEle.classList.remove('svgtxtoff');
		svgTextEle.classList.remove('svgtxton');
		
		if(!pendingStateBool){
			// is on or off
			if(stateOnBool){
				clzList.add('svgon');
				svgTextEle.innerHTML = "on-" + i;
				svgTextEle.classList.add('svgtxton');
				
				// text = text.replace("RLY=ON", "RLY=OF");
				// svgEle.setAttribute("onclick", "relayAction(this,'RLY=OF1');");
			}else{
				clzList.add('svgoff');
				svgTextEle.innerHTML = "off-" + i;
				
				svgTextEle.classList.add('svgtxtoff');
				
				// text = text.replace("RLY=OF", "RLY=ON");
				// svgEle.setAttribute("onclick", "relayAction(this,'RLY=ON1');");
				// svgEle.onclick = "relayAction(this,'RLY=ON1');";
			}
			// svgEle.outerHTML = text;
			continue;
		}
		// in a pending state
		if(pendingOnBool){
			clzList.add('svgofftoon');
			svgTextEle.classList.add('svgtxtoff');
		}else{
			clzList.add('svgontooff');
			svgTextEle.classList.add('svgtxton');
		}
	}
}

function buttonStateSetter(){
	// there are two rows of buttons
	
	// button state may have the following values
	// 0 -> on
	// 1 -> off
	// 3 -> pending on
	// 4 -> pending off
	//
	var i;
	for(i = 1; i <= 8; i++){
		var onEle = getById("lo" + i);
		if(onEle === null){
			// not configured
			return;
		}
		var onEleParent = onEle.parentNode;
		
		var offEle = getById("lf" + i);
		var offEleParent = offEle.parentNode;
		
		var valueState = parseInt(gpioStatesA.substring(i-1, i));
		
		var stateOnBool = valueState === 1;
		var pendingStateBool = valueState > 2;
		var pendingOnBool = valueState === 3;

		// color state is controlled by a class setting
		// using the CSS file
		var onclzList = onEleParent.classList;
		var offclzList = offEleParent.classList;
		
		if(pendingStateBool){
			if(pendingOnBool){
				// onEleParent.style.background = g_orangeShade;
				onclzList.remove('onclr');
				onclzList.add('transclr');
			}else{
				// offEleParent.style.background = g_orangeShade;
				offclzList.remove('offclr');
				offclzList.add('transclr');
			}
			return;
		}
		// remove all color state representations from the 'off' or
		// 'on' button and reset them to the current state
		onclzList.remove('transclr');
		onclzList.remove('onclr');

		offclzList.remove('transclr');
		offclzList.remove('offclr');
		
		// when a button is disabled, it is the active state that
		//needs to be represented by a color
		onEle.disabled = stateOnBool;
		if(onEle.disabled){
			onclzList.add('onclr');
		}
		offEle.disabled = !stateOnBool;
		if(offEle.disabled){
			offclzList.add('offclr');
		}
	}	
}

function setButtonNames(){
	var i;
	for(i = 1; i <= 8; i++){
		var cfgEle = getById("cfg" + i);
		var idx = (i -1) * 7;
		var newName = relayLabelA.substring(idx, idx + 7);
		
		cfgEle.innerHTML = newName;
	}	
}

function keyAction(ele, evt){
  var fieldName = ele.id; 
  // length may not be greater than 7 chars
  var currText = ele.value;
  
  if (evt !== undefined) {
    var c = evt.which;
	
	if (c === 13 || c === 10) {
		// disallow cr lf
		return;
	}
	if(currText.length >= 7){
	  // may not input more character, cut to 7 chars
	  ele.value = currText.substring(0, 7);
	}
  } 
}
function setView(ele){
	var val = ele.value;

	// set the setting as a cookie for this user
	// document.cookie = "username=" + val; 
	buildCookie("viewsetting", val, 3600); // 4.8 years

	setViewFromCookie();;
	clearCfgInputs();
	
	// cfgClose();
}
function buildCookie(cname, cvalue, exdays) {
  var d = new Date();
  d.setTime(d.getTime() + (exdays*24*60*60*1000));
  var expires = "expires="+ d.toUTCString();
  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}
function setViewFromCookie(){
	// set the rows for single and/or double views to an appropriate display
	// visibility
	var cookieVal = document.cookie;
	
	var singleEleArr = document.getElementsByClassName("svgbuttclz");
	var doubleEleArr = document.getElementsByClassName("dblbuttclz");
	
	var setViewEleIdStr = '';
	
	if( cookieVal.indexOf("single-button") > -1){
		setViewDisplay(doubleEleArr, 'none'); 
		setViewDisplay(singleEleArr, '');
		setViewEleIdStr = 'single-button';
	}
	if( cookieVal.indexOf("double-buttons") > -1){
		setViewDisplay(doubleEleArr, ''); 
		setViewDisplay(singleEleArr, 'none');
		setViewEleIdStr = 'double-buttons';		
	}
	if( cookieVal.indexOf("both") > -1){
		setViewDisplay(doubleEleArr, ''); 
		setViewDisplay(singleEleArr, ''); 
		setViewEleIdStr = 'both';
	}
	if(setViewEleIdStr === ''){
		return;
	}
	var viewEle = document.getElementById(setViewEleIdStr);
	viewEle.checked = true;
}
function setViewDisplay(eleArr, setting){
	if(eleArr === null){
		return;
	}
	var i;
	for(i = 0; i < eleArr.length; i++){
		var eleI = eleArr[i];
		
		eleI.style.display = setting;
	}
}
function cfgsubmit(){
	// a multi-step process of actions
	// 1) set the names as per the input fields
	// 2) get the names from the display fields and concat
	// 3) build the GET request and its data
	// 4) send the data to the server, and wait for response
	// 5) set the names as per the response (a double take but reasonable)
	var i;
	var fullNameStr = "";
	
	for(i = 1; i <= 8; i++){
		var inpEle = getById("inp" + i);
		var inpValOrCfg = inpEle.value;
		
		if(inpValOrCfg === ""){
			var cfgEle = getById("cfg" + i);
			inpValOrCfg = cfgEle.innerHTML;
		}
		// need to make the size 7 characters, right-fill spaces
		var lenValCfg =  7 - inpValOrCfg.length;
		
		if(lenValCfg < 7){
			// fill spaces
			var j;
			for(j = 0; j < lenValCfg; j++){
				inpValOrCfg += " ";
			}
		}
		fullNameStr += inpValOrCfg;
	}
	setButtonNames();
	clearCfgInputs();
	
	var xhttp = new XMLHttpRequest();
	var params = 'inp=' + fullNameStr;
	
	xhttp.open("POST", "CONFIG", true);
	xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  
	xhttp.onreadystatechange = function() {
		if (this.readyState === 4 && this.status === 200) {
			// document.getElementById("relaystateid").innerHTML =
			// this.responseText;
			relayLabelA = this.responseText;
			setButtonNames();
		}	
	};
	xhttp.send(params);
  
}
function clearCfgInputs(){
	var i;
	for(i = 1; i <= 8; i++){
		getById("inp" + i).value = "";
	}
}

function pwbuttact(){
	var xhttp = new XMLHttpRequest();
	var parms = 'pwdacc=' + enPassword(document.getElementById("pwdcfg").value);
	
	xhttp.open("POST", "access", true);
	xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  
	xhttp.onreadystatechange = function() {
		if (this.readyState === 4){
			if(this.status === 200) {
				if(this.responseText === "okay"){
					//relative to domain
					window.location.href = 'cfgacc'; 
				}	
			}
			if(this.status === 401) {
				window.location.href = 'a401'; 	
			}
		}
	};
	xhttp.send(parms);
}
