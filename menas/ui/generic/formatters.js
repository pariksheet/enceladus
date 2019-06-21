/*
 * Copyright 2018-2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

jQuery.sap.require("sap.ui.core.format.DateFormat");
jQuery.sap.require("sap.ui.core.Locale");

var Formatters = new function() {
  
  this.oozieCoordinatorStatusFormatter = function(sStatus) {
    if(!sStatus) {
      return sap.ui.core.ValueState.None;
    } else if(sStatus === "RUNNING") {
      return sap.ui.core.ValueState.Success;
    } else return sap.ui.core.ValueState.Error;
  }

  this.stringDateShortFormatter = function(sDate) {
    if (!sDate)
      return "";
    var oDate = new Date(sDate);
    var oFormat = sap.ui.core.format.DateFormat.getDateTimeInstance({
      style : "short"
    }, new sap.ui.core.Locale("en_GB"))
    return oFormat.format(oDate)
  }

  this.not = function(bSth) {
    return !bSth;
  }

  this.nonEmptyObject = function(oObj) {
    return (oObj !== null) && (typeof (oObj) !== "undefined") && (Object.keys(oObj).length !== 0)
  }

  this.objToKVArray = function(oObj) {
    if(oObj === null || typeof(oObj) === "undefined") return []
    else {
      var res = [];
      for(var i in oObj) {
        res.push({
          key: i,
          value: oObj[i]
        })
      }
      return res;
    }
  }


  this.infoDatePattern = "yyyy-MM-dd";

  this.infoDateFormat = sap.ui.core.format.DateFormat.getInstance({
    pattern: this.infoDatePattern,
    calendarType: sap.ui.core.CalendarType.Gregorian
  });

  this.toStringInfoDate = function(oDate) {
    return this.infoDateFormat.format(oDate);
  }
}();
