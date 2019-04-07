function BranchData() {
    this.position = -1;
    this.nodeLength = -1;
    this.evalFalse = 0;
    this.evalTrue = 0;

    this.init = function(position, nodeLength) {
        this.position = position;
        this.nodeLength = nodeLength;
        return this;
    };

    this.ranCondition = function(result) {
        if (result)
            this.evalTrue++;
        else
            this.evalFalse++;
    };

    this.pathsCovered = function() {
        var paths = 0;
        if (this.evalTrue > 0)
          paths++;
        if (this.evalFalse > 0)
          paths++;
        return paths;
    };

    this.covered = function() {
        return this.evalTrue > 0 && this.evalFalse > 0;
    };

    this.toJSON = function() {
        return '{"position":' + this.position
            + ',"nodeLength":' + this.nodeLength
            + ',"evalFalse":' + this.evalFalse
            + ',"evalTrue":' + this.evalTrue + '}';
    };

    this.message = function(src) {
        if (this.evalTrue === 0 && this.evalFalse === 0)
            return 'Condition never evaluated         :\t' + src + '\n';
        else if (this.evalTrue === 0)
            return 'Condition never evaluated to true :\t' + src + '\n';
        else if (this.evalFalse === 0)
            return 'Condition never evaluated to false:\t' + src + '\n';
        else
            return 'Condition covered';
    };
}

BranchData.fromJson = function(jsonString) {
    var json = eval('(' + jsonString + ')');
    var branchData = new BranchData();
    branchData.init(json.position, json.nodeLength);
    branchData.evalFalse = json.evalFalse;
    branchData.evalTrue = json.evalTrue;
    return branchData;
};

BranchData.fromJsonObject = function(json) {
    var branchData = new BranchData();
    branchData.init(json.position, json.nodeLength);
    branchData.evalFalse = json.evalFalse;
    branchData.evalTrue = json.evalTrue;
    return branchData;
};

function buildBranchMessage(conditions) {
    var message = 'The following was not covered:';
    var i;
    for (i = 0; i < conditions.length; i++) {
        if (conditions[i] !== undefined && conditions[i] !== null && !conditions[i].covered())
            message += '\n- '+ conditions[i].message(conditions[i].src);
    }
    return message;
}

function convertBranchDataConditionArrayToJSON(branchDataConditionArray) {
    var condition, branchDataObject, value;
    var array = [];
    var length = branchDataConditionArray.length;
    for (condition = 0; condition < length; condition++) {
        branchDataObject = branchDataConditionArray[condition];
        if (branchDataObject === undefined || branchDataObject === null) {
            value = 'null';
        } else {
            value = branchDataObject.toJSON();
        }
        array.push(value);
    }
    return '[' + array.join(',') + ']';
}

function convertBranchDataLinesToJSON(branchData) {
    if (branchData === undefined) {
        return '{}'
    }
    var line;
    var json = '';
    for (line in branchData) {
        if (isNaN(line))
            continue;
        if (json !== '')
            json += ',';
        json += '"' + line + '":' + convertBranchDataConditionArrayToJSON(branchData[line]);
    }
    return '{' + json + '}';
}

function convertBranchDataLinesFromJSON(jsonObject) {
    if (jsonObject === undefined) {
        return {};
    }
    var line, branchDataJSON, conditionIndex, condition;
    for (line in jsonObject) {
        branchDataJSON = jsonObject[line];
        if (branchDataJSON !== null) {
            for (conditionIndex = 0; conditionIndex < branchDataJSON.length; conditionIndex ++) {
                condition = branchDataJSON[conditionIndex];
                if (condition !== null) {
                    branchDataJSON[conditionIndex] = BranchData.fromJsonObject(condition);
                }
            }
        }
    }
    return jsonObject;
}
function jscoverage_quote(s) {
    return '"' + s.replace(/[\u0000-\u001f"\\\u007f-\uffff]/g, function (c) {
        switch (c) {
            case '\b':
                return '\\b';
            case '\f':
                return '\\f';
            case '\n':
                return '\\n';
            case '\r':
                return '\\r';
            case '\t':
                return '\\t';
            // IE doesn't support this
            /*
             case '\v':
             return '\\v';
             */
            case '"':
                return '\\"';
            case '\\':
                return '\\\\';
            default:
                return '\\u' + jscoverage_pad(c.charCodeAt(0).toString(16));
        }
    }) + '"';
}

function getArrayJSON(coverage) {
    var array = [];
    if (coverage === undefined)
        return array;

    var length = coverage.length;
    for (var line = 0; line < length; line++) {
        var value = coverage[line];
        if (value === undefined || value === null) {
            value = 'null';
        }
        array.push(value);
    }
    return array;
}

function jscoverage_serializeCoverageToJSON() {
    var json = [];
    for (var file in _$jscoverage) {
        var lineArray = getArrayJSON(_$jscoverage[file].lineData);
        var fnArray = getArrayJSON(_$jscoverage[file].functionData);

        json.push(jscoverage_quote(file) + ':{"lineData":[' + lineArray.join(',') + '],"functionData":[' + fnArray.join(',') + '],"branchData":' + convertBranchDataLinesToJSON(_$jscoverage[file].branchData) + '}');
    }
    return '{' + json.join(',') + '}';
}

function jscoverage_parseCoverageJSON(data) {
    var result = {};
    var json = eval('(' + data + ')');
    var file;
    for (file in json) {
        var fileCoverage = json[file];
        result[file] = {};
        result[file].lineData = fileCoverage.lineData;
        result[file].functionData = fileCoverage.functionData;
        result[file].branchData = convertBranchDataLinesFromJSON(fileCoverage.branchData);
    }
    return result;
}

function jscoverage_pad(s) {
    return '0000'.substr(s.length) + s;
}

function jscoverage_html_escape(s) {
    return s.replace(/[<>\&\"\']/g, function (c) {
        return '&#' + c.charCodeAt(0) + ';';
    });
}
if (typeof(_$jscoverage) === "undefined" && (typeof(Storage) !== "undefined") && typeof(localStorage["jscover"]) !== "undefined")
    _$jscoverage = jscoverage_parseCoverageJSON(localStorage["jscover"]);
if (typeof(jscoverbeforeunload) === "undefined") {
    jscoverbeforeunload = (window.onbeforeunload) ? window.onbeforeunload : function () {};
    window.onbeforeunload = function () {
        jscoverbeforeunload();
        if ((typeof(_$jscoverage) !== "undefined") && (typeof(Storage) !== "undefined"))
            localStorage["jscover"] = jscoverage_serializeCoverageToJSON();
    };
}
var jsCover_isolateBrowser = false;
if (!jsCover_isolateBrowser) {
    try {
        if (typeof top === 'object' && top !== null && typeof top.opener === 'object' && top.opener !== null) {
            // this is a browser window that was opened from another window

            if (!top.opener._$jscoverage) {
                top.opener._$jscoverage = {};
            }
        }
    } catch (e) {
    }

    try {
        if (typeof top === 'object' && top !== null) {
            // this is a browser window

            try {
                if (typeof top.opener === 'object' && top.opener !== null && top.opener._$jscoverage) {
                    top._$jscoverage = top.opener._$jscoverage;
                }
            } catch (e) {
            }

            if (!top._$jscoverage) {
                top._$jscoverage = {};
            }
        }
    } catch (e) {
    }

    try {
        if (typeof top === 'object' && top !== null && top._$jscoverage) {
            this._$jscoverage = top._$jscoverage;
        }
    } catch (e) {
    }
}
if (!this._$jscoverage) {
    this._$jscoverage = {};
}
if (! _$jscoverage['/script.js']) {
  _$jscoverage['/script.js'] = {};
  _$jscoverage['/script.js'].lineData = [];
  _$jscoverage['/script.js'].lineData[1] = 0;
  _$jscoverage['/script.js'].lineData[2] = 0;
  _$jscoverage['/script.js'].lineData[4] = 0;
  _$jscoverage['/script.js'].lineData[5] = 0;
  _$jscoverage['/script.js'].lineData[7] = 0;
  _$jscoverage['/script.js'].lineData[8] = 0;
  _$jscoverage['/script.js'].lineData[9] = 0;
  _$jscoverage['/script.js'].lineData[10] = 0;
  _$jscoverage['/script.js'].lineData[12] = 0;
  _$jscoverage['/script.js'].lineData[13] = 0;
  _$jscoverage['/script.js'].lineData[15] = 0;
  _$jscoverage['/script.js'].lineData[16] = 0;
  _$jscoverage['/script.js'].lineData[18] = 0;
  _$jscoverage['/script.js'].lineData[19] = 0;
  _$jscoverage['/script.js'].lineData[21] = 0;
  _$jscoverage['/script.js'].lineData[22] = 0;
  _$jscoverage['/script.js'].lineData[23] = 0;
  _$jscoverage['/script.js'].lineData[24] = 0;
  _$jscoverage['/script.js'].lineData[25] = 0;
}
if (! _$jscoverage['/script.js'].functionData) {
  _$jscoverage['/script.js'].functionData = [];
  _$jscoverage['/script.js'].functionData[0] = 0;
  _$jscoverage['/script.js'].functionData[1] = 0;
  _$jscoverage['/script.js'].functionData[2] = 0;
}
if (! _$jscoverage['/script.js'].branchData) {
  _$jscoverage['/script.js'].branchData = {};
  _$jscoverage['/script.js'].branchData['9'] = [];
  _$jscoverage['/script.js'].branchData['9'][1] = new BranchData();
  _$jscoverage['/script.js'].branchData['12'] = [];
  _$jscoverage['/script.js'].branchData['12'][1] = new BranchData();
  _$jscoverage['/script.js'].branchData['15'] = [];
  _$jscoverage['/script.js'].branchData['15'][1] = new BranchData();
  _$jscoverage['/script.js'].branchData['18'] = [];
  _$jscoverage['/script.js'].branchData['18'][1] = new BranchData();
}
_$jscoverage['/script.js'].branchData['18'][1].init(11, 23);
function visit4_18_1(result) {
  _$jscoverage['/script.js'].branchData['18'][1].ranCondition(result);
  return result;
}
_$jscoverage['/script.js'].branchData['15'][1].init(11, 23);
function visit3_15_1(result) {
  _$jscoverage['/script.js'].branchData['15'][1].ranCondition(result);
  return result;
}
_$jscoverage['/script.js'].branchData['12'][1].init(11, 23);
function visit2_12_1(result) {
  _$jscoverage['/script.js'].branchData['12'][1].ranCondition(result);
  return result;
}
_$jscoverage['/script.js'].branchData['9'][1].init(6, 23);
function visit1_9_1(result) {
  _$jscoverage['/script.js'].branchData['9'][1].ranCondition(result);
  return result;
}
_$jscoverage['/script.js'].lineData[1]++;
function getMessage(number) {
  _$jscoverage['/script.js'].functionData[0]++;
  _$jscoverage['/script.js'].lineData[2]++;
  return 'You selected the number ' + number + '.';
}
_$jscoverage['/script.js'].lineData[4]++;
function getMessage4() {
  _$jscoverage['/script.js'].functionData[1]++;
  _$jscoverage['/script.js'].lineData[5]++;
  return 'You selected the number 4.';
}
_$jscoverage['/script.js'].lineData[7]++;
function go(element) {
  _$jscoverage['/script.js'].functionData[2]++;
  _$jscoverage['/script.js'].lineData[8]++;
  var message;
  _$jscoverage['/script.js'].lineData[9]++;
  if (visit1_9_1(element.id === 'radio1')) {
    _$jscoverage['/script.js'].lineData[10]++;
    message = 'You selected the number 1.';
  } else {
    _$jscoverage['/script.js'].lineData[12]++;
    if (visit2_12_1(element.id === 'radio2')) {
      _$jscoverage['/script.js'].lineData[13]++;
      message = getMessage(2);
    } else {
      _$jscoverage['/script.js'].lineData[15]++;
      if (visit3_15_1(element.id === 'radio3')) {
        _$jscoverage['/script.js'].lineData[16]++;
        message = getMessage(3);
      } else {
        _$jscoverage['/script.js'].lineData[18]++;
        if (visit4_18_1(element.id === 'radio4')) {
          _$jscoverage['/script.js'].lineData[19]++;
          message = getMessage4();
        }
      }
    }
  }
  _$jscoverage['/script.js'].lineData[21]++;
  var div = document.getElementById('request');
  _$jscoverage['/script.js'].lineData[22]++;
  div.className = 'black';
  _$jscoverage['/script.js'].lineData[23]++;
  div = document.getElementById('result');
  _$jscoverage['/script.js'].lineData[24]++;
  div.innerHTML = '\x3cp\x3e' + message + '\x3c/p\x3e';
  _$jscoverage['/script.js'].lineData[25]++;
  div.innerHTML += '\x3cp\x3eIf you are running the instrumented version of this program, you can click the "Summary" tab to view a coverage report.\x3c/p\x3e';
}
