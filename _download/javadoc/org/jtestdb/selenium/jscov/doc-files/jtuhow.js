/* Copyright (c) 2019 dbradley. */
var imageIdArr = new Array();
var imageSetSizeArr = new Array();

var g_tocIdCount = 1;
var g_tocSortedArr = new Array();


function dispTempButt(buttEle, eleName) {
  var templatEle = document.getElementById(eleName);
  var dispOfEle = templatEle.style.display;
  //
  var chg;
  var from;
  var to;
  if (dispOfEle === 'none') {
    chg = 'block';
    from = "Show ";
    to = "Hide ";
  } else {
    chg = 'none';
    from = "Hide ";
    to = "Show ";
  }
  buttEle.scrollIntoView();

  templatEle.style.display = chg;

  var buttText = buttEle.innerText;
  buttText = buttText.replace(from, to);
  buttEle.innerText = buttText;
}
function imgScale(thisButt, imgEleId) {
  // extract the percent value from the button text
  var text = thisButt.innerText;
  var idxPercent = text.lastIndexOf("%");
  var idxPercentSrt = text.lastIndexOf(" ", idxPercent);

  var percentStr = text.substring(idxPercentSrt + 1, idxPercent + 1);

  imgScaleSet(imgEleId, percentStr);
}
function imgScaleSet(imgEleId, percentStr) {
  var imgEle = document.getElementById(imgEleId);

  if (imgEle.aaWidth === undefined) {
    imgEle.aaWidth = imgEle.clientWidth;
    imgEle.aaHeight = imgEle.clientHeight;
  }
  var wClt = imgEle.aaWidth;
  var percentNumber = parseInt(percentStr.substring(0, percentStr.length - 1));

  var wPx = wClt * percentNumber / 100 + "px";
  imgEle.style.width = wPx;
}
function onloadImg() {
  var i;
  for (i = 0; i < imageIdArr.length; i++) {
    imgScaleSet(imageIdArr[i], imageSetSizeArr[i]);
  }
}
function imageRegister(imgEleId, setPercent) {
  imageIdArr.push(imgEleId);
  imageSetSizeArr.push(setPercent);
}
function buttFocus(buttEle) {
  buttEle.scrollIntoView();
}

function buildTOC() {
  // <a href="org/jtestdb/selenium/jscov/JscovTestUtil.html" 
  // title="class in org.jtestdb.selenium.jscov" 
  // target="classFrame">JscovTestUtil</a>
  var appendToTocEle = document.getElementById('tocele');

  var hdrOfStrEle = document.createElement('p');

  var hdrOfStrEleAnchor = document.createElement('a');
  hdrOfStrEleAnchor.setAttribute('href', "../JscovTestUtil.html");
  hdrOfStrEleAnchor.setAttribute('title', "class in org.jtestdb.selenium.jscov");
  hdrOfStrEleAnchor.setAttribute('target', "classFrame");

  hdrOfStrEleAnchor.innerHTML = "<i>HOME: JscovTestUtil </i>";

  hdrOfStrEle.appendChild(hdrOfStrEleAnchor);
  appendToTocEle.appendChild(hdrOfStrEle);

  getHeadersInOrderBySequenceProcessing(document.body, appendToTocEle, 1);

  // sort the headers of the document in alphabetic order

  if (false) {
    g_tocSortedArr.sort(function (aEle, bEle) {
      // return b - a
      //   -1 <
      //    0 =
      //   +1 >
      //
      // get the
      var aText = aEle.getElementsByTagName('a')[0].innerHTML;
      var bText = bEle.getElementsByTagName('a')[0].innerHTML;

      if (aText === bText) {
        return 0;
      }
      if (aText > bText) {
        return +1;
      }
      return -1;
    });
  }

  // put the sorted index in place
  var i;
  for (i = 0; i < g_tocSortedArr.length; i++) {
    appendToTocEle.appendChild(g_tocSortedArr[i]);
  }
}



function getHeadersInOrderBySequenceProcessing(tagEle, appendToEle) {

  var childArr = tagEle.childNodes;
  var tagNam = tagEle.nodeName.toLowerCase();

  if (tagNam === 'h1' || tagNam === 'h2' || tagNam === 'h3' ||
          tagNam === 'h4' || tagNam === 'h5' || tagNam === 'h6') {
    //
    if (tagEle.id === undefined || tagEle.id === "") {
      tagEle.id = "tocid" + g_tocIdCount;
      g_tocIdCount++;
    }
    var hdrOfStrEle = document.createElement('p');
    hdrOfStrEle.setAttribute('class', "martpbt");
    hdrOfStrEle.innerHTML = "&bull; ";

    var hdrOfStrEleAnchor = document.createElement('a');
    hdrOfStrEleAnchor.setAttribute('href', "#" + tagEle.id);
    hdrOfStrEleAnchor.innerHTML = tagEle.innerHTML;

    hdrOfStrEle.appendChild(hdrOfStrEleAnchor);
    //    appendToEle.appendChild(hdrOfStrEle); // add in order found

    g_tocSortedArr.push(hdrOfStrEle);
  }
  //
  var i;
  for (i = 0; i < childArr.length; i++) {
    getHeadersInOrderBySequenceProcessing(childArr[i], appendToEle);
  }
}
