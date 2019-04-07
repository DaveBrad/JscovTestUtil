
function enPassword(rawPasswordString){
	var b64Encode = window.btoa(rawPasswordString);
	return b64Encode;
	
	// var pwdValue = document.getElementById("pwdcfg").value;
	// var encrypted = pwdValue;
	
	// // pwdValue = "dddddddddddddddd";
	// //          1234567890123456
	
	// // pwdValue = padString("dddddddddddddddd");
	// console.log(pwdValue);
	
	// var doEncrypt = true;
	
	// if(doEncrypt){
		// var key = "1234567890123456"; 
		// key = CryptoJS.enc.Hex.parse("1234567890123456");
		// // key = CryptoJS.enc.Hex.parse(pwdValue);
		// console.log(key.toString());
	
		// pwdValue = "dddddddddddddddd";
	
		// var iv = 0; // CryptoJS.enc.Hex.parse("1234567890123456");
		// console.log(iv.toString());
	
		// encrypted = CryptoJS.AES.encrypt(pwdValue, key, 
			// {iv: iv, 
			// padding: CryptoJS.pad.Pkcs7,
			// mode: CryptoJS.mode.CBC}
		// ); 
	
		// var decrypt = CryptoJS.AES.decrypt(encrypted, key, 
		// {iv: iv, 
			// padding: CryptoJS.pad.Pkcs7,
			// mode: CryptoJS.mode.CBC}); 
		// console.log(hex2a(decrypt.toString()));

		// // {iv: iv, padding: CryptoJS.pad.NoPadding, mode: CryptoJS.mode.CBC}	
	// }
	// console.log(encrypted.toString());
	
	// var b64Encrypted = window.btoa(encrypted);
	// console.log(b64Encrypted);
	
	// var xhttp = new XMLHttpRequest();
	// var parms = "";
	// parms += 'pwdacc=' + b64Encrypted + '&';
	// parms += 'type=' + type;
	
	// if(!doEncrypt){
		// parms += '&notencrypt=dummy';
	// }
	
	// xhttp.open("POST", "actssid", true);
	// xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  
	// xhttp.onreadystatechange = function() {
		// if (this.readyState === 4){
			// if(this.status === 200) {
				// // if wish to do something
				
				// var labelactEle = document.getElementById('labelact');
				// labelactEle.innerHTML = "Reboot requested: need to re-post page after";
			// }
		// }
	// };
	// xhttp.send(parms);
}

// function hex2a(hex) {
    // var str = '';
    // for (var i = 0; i < hex.length; i += 2)
        // str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    // return str;
// }



