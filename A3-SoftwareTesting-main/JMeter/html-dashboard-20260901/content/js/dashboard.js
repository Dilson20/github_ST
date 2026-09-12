/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "[10 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 01_WelcomePage"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 4173, 0, 0.0, 6.964533908459137, 0, 332, 5.0, 13.0, 20.0, 40.0, 52.59310605583212, 284.46868698405694, 8.058791413290063], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["[10 Users] 04_OwnerDetails", 95, 0, 0.0, 14.810526315789474, 6, 86, 13.0, 20.400000000000006, 25.599999999999966, 86.0, 5.1221221760931686, 47.94486431430959, 0.7603150105138297], "isController": false}, {"data": ["[1 Users] 05_VisitBillingCalculation", 11, 0, 0.0, 22.09090909090909, 9, 43, 22.0, 40.000000000000014, 43.0, 43.0, 0.6724538452133513, 3.284779427497249, 0.11623469785426091], "isController": false}, {"data": ["[25 Users] 05_VisitBillingCalculation", 228, 0, 0.0, 8.76754385964912, 3, 64, 6.0, 16.0, 28.0, 55.680000000000064, 12.502056259253166, 61.06961465701596, 2.160999958874815], "isController": false}, {"data": ["[50 Users] 03_FindOwnersSearch", 486, 0, 0.0, 7.524691358024695, 4, 46, 6.0, 11.0, 15.0, 27.25999999999999, 25.47036318851213, 148.44446045804727, 3.9797442482050207], "isController": false}, {"data": ["[1 Users] 02_VeterinariansList", 12, 0, 0.0, 24.416666666666668, 6, 123, 16.0, 93.00000000000011, 123.0, 123.0, 0.6607565662683773, 3.26119500578162, 0.09872632288970871], "isController": false}, {"data": ["[10 Users] 02_VeterinariansList", 100, 0, 0.0, 9.879999999999999, 4, 74, 7.0, 15.900000000000006, 20.849999999999966, 73.91999999999996, 5.1663566852655505, 25.498795593097746, 0.7719263406695598], "isController": false}, {"data": ["[25 Users] 02_VeterinariansList", 247, 0, 0.0, 6.5910931174089065, 2, 48, 4.0, 14.200000000000017, 23.19999999999999, 42.68000000000009, 12.847854356306893, 63.41118741872561, 1.919650113784135], "isController": false}, {"data": ["[10 Users] 03_FindOwnersSearch", 98, 0, 0.0, 15.285714285714294, 6, 89, 14.0, 22.0, 25.14999999999999, 89.0, 5.1459777357697964, 29.991401491283344, 0.8040590212140306], "isController": false}, {"data": ["[25 Users] 03_FindOwnersSearch", 241, 0, 0.0, 13.058091286307057, 5, 81, 8.0, 29.0, 38.0, 70.57999999999998, 12.755372075791255, 74.33990287922093, 1.993026886842384], "isController": false}, {"data": ["[50 Users] 04_OwnerDetails", 475, 0, 0.0, 6.595789473684213, 4, 32, 6.0, 9.0, 12.0, 23.24000000000001, 25.303643724696357, 236.85100107540487, 3.756009615384616], "isController": false}, {"data": ["[50 Users] 05_VisitBillingCalculation", 464, 0, 0.0, 4.450431034482759, 2, 17, 4.0, 6.0, 8.0, 14.700000000000045, 25.14768847216953, 122.84056419706249, 4.346817245677741], "isController": false}, {"data": ["[10 Users] 05_VisitBillingCalculation", 92, 0, 0.0, 11.021739130434781, 4, 67, 9.0, 16.0, 19.349999999999994, 67.0, 5.092438835381379, 24.875370170485997, 0.8802360096313517], "isController": false}, {"data": ["[50 Users] 01_WelcomePage", 505, 0, 0.0, 1.5584158415841585, 0, 31, 1.0, 2.0, 4.0, 8.0, 25.65144511606644, 57.4652491174379, 3.607234469446843], "isController": false}, {"data": ["[1 Users] 01_WelcomePage", 12, 0, 0.0, 36.58333333333333, 7, 332, 10.0, 236.60000000000034, 332.0, 332.0, 0.649456080532554, 1.4549338366617957, 0.0913297613248904], "isController": false}, {"data": ["[1 Users] 04_OwnerDetails", 12, 0, 0.0, 26.833333333333336, 13, 34, 28.5, 34.0, 34.0, 34.0, 0.6666296316871284, 6.239887714571412, 0.09895283595355812], "isController": false}, {"data": ["[25 Users] 01_WelcomePage", 250, 0, 0.0, 2.6, 0, 26, 2.0, 5.0, 8.0, 23.0, 12.806064952361439, 28.688586914762833, 1.8008528839258273], "isController": false}, {"data": ["[1 Users] 03_FindOwnersSearch", 12, 0, 0.0, 36.333333333333336, 19, 95, 32.5, 77.90000000000006, 95.0, 95.0, 0.6639738836939081, 3.8697227909035576, 0.10374591932717313], "isController": false}, {"data": ["[25 Users] 04_OwnerDetails", 234, 0, 0.0, 12.551282051282051, 4, 75, 8.0, 28.0, 38.0, 70.45000000000007, 12.600290775940984, 117.94315145253351, 1.8703556620537396], "isController": false}, {"data": ["[50 Users] 02_VeterinariansList", 497, 0, 0.0, 3.9778672032193154, 2, 26, 3.0, 6.0, 8.0, 17.019999999999982, 25.637057670483856, 126.53289986975138, 3.830536937093779], "isController": false}, {"data": ["[10 Users] 01_WelcomePage", 102, 0, 0.0, 4.343137254901962, 1, 65, 3.0, 6.0, 10.0, 63.529999999999944, 5.181610363220726, 11.608021653543307, 0.7286639573279147], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 4173, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
