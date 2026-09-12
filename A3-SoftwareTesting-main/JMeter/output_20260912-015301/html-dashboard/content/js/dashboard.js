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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "[50 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[Warmup] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 06_CreateOwner_POST-0"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 06_CreateOwner_POST"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 06_CreateOwner_POST-1"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 06_CreateOwner_POST"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 06_CreateOwner_POST-1"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[Warmup] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[Warmup] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 06_CreateOwner_POST-0"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 05_VisitBillingCalculation"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 06_CreateOwner_POST"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 06_CreateOwner_POST"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 06_CreateOwner_POST-0"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 06_CreateOwner_POST-1"], "isController": false}, {"data": [1.0, 500, 1500, "[1 Users] 03_FindOwnersSearch"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 04_OwnerDetails"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 02_VeterinariansList"], "isController": false}, {"data": [1.0, 500, 1500, "[50 Users] 06_CreateOwner_POST-0"], "isController": false}, {"data": [1.0, 500, 1500, "[10 Users] 01_WelcomePage"], "isController": false}, {"data": [1.0, 500, 1500, "[25 Users] 06_CreateOwner_POST-1"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 4682, 0, 0.0, 13.731952157197782, 0, 280, 4.0, 29.0, 45.0, 198.0, 62.811913066809765, 347.656996767675, 13.769164416756103], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["[50 Users] 03_FindOwnersSearch", 321, 0, 0.0, 99.67601246105924, 21, 280, 67.0, 213.0, 237.79999999999995, 269.78, 22.75143525409313, 331.21769017116736, 3.554911758452052], "isController": false}, {"data": ["[10 Users] 02_VeterinariansList", 70, 0, 0.0, 5.171428571428573, 2, 15, 4.0, 9.0, 13.350000000000009, 15.0, 4.938271604938271, 24.37307098765432, 0.7378472222222222], "isController": false}, {"data": ["[10 Users] 03_FindOwnersSearch", 68, 0, 0.0, 22.17647058823529, 14, 48, 18.5, 38.400000000000006, 43.55, 48.0, 4.911520404478151, 45.57827735644637, 0.767425063199711], "isController": false}, {"data": ["[25 Users] 03_FindOwnersSearch", 169, 0, 0.0, 23.72189349112425, 14, 115, 21.0, 33.0, 41.5, 102.4000000000002, 12.061090493862404, 132.6495537307308, 1.8845453896660005], "isController": false}, {"data": ["[50 Users] 05_VisitBillingCalculation", 309, 0, 0.0, 9.69902912621359, 2, 53, 6.0, 21.0, 26.0, 40.499999999999886, 23.003052184917742, 112.88118870040199, 3.9761135124320703], "isController": false}, {"data": ["[Warmup] 02_VeterinariansList", 98, 0, 0.0, 11.204081632653057, 4, 34, 10.0, 19.0, 22.099999999999994, 34.0, 6.763751811719235, 33.38281411760646, 1.010599635930706], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST-0", 7, 0, 0.0, 13.142857142857142, 2, 28, 10.0, 28.0, 28.0, 28.0, 0.6021505376344086, 0.17993951612903225, 0.20052083333333334], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST", 62, 0, 0.0, 5.274193548387095, 3, 29, 4.0, 7.0, 11.549999999999983, 29.0, 4.839968774395004, 23.954064207650273, 2.54760075136612], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST-1", 7, 0, 0.0, 8.571428571428571, 3, 14, 8.0, 14.0, 14.0, 14.0, 0.6037084950409659, 2.8074803255713667, 0.11673269728331176], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST", 155, 0, 0.0, 3.929032258064515, 2, 18, 3.0, 6.0, 8.199999999999989, 14.079999999999984, 11.888326430434114, 58.83792807562509, 6.25762494726952], "isController": false}, {"data": ["[10 Users] 05_VisitBillingCalculation", 65, 0, 0.0, 5.307692307692309, 2, 16, 5.0, 8.0, 10.699999999999996, 16.0, 4.911591355599214, 24.102291564341847, 0.8489762401768173], "isController": false}, {"data": ["[1 Users] 04_OwnerDetails", 8, 0, 0.0, 22.75, 8, 39, 23.5, 39.0, 39.0, 39.0, 0.5884516366311144, 5.508688856197131, 0.08734828981243103], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST-1", 62, 0, 0.0, 2.741935483870968, 1, 9, 2.0, 4.0, 5.849999999999994, 9.0, 4.848674434973019, 22.548230136075702, 0.9375366583248611], "isController": false}, {"data": ["[25 Users] 01_WelcomePage", 177, 0, 0.0, 1.6271186440677972, 0, 26, 1.0, 2.200000000000017, 3.0, 14.299999999999983, 12.101736633392589, 27.11072640332285, 1.7018067140708328], "isController": false}, {"data": ["[Warmup] 03_FindOwnersSearch", 96, 0, 0.0, 37.05208333333333, 15, 124, 37.0, 46.3, 47.14999999999999, 124.0, 6.715634837355719, 58.414218258132216, 1.0493179433368311], "isController": false}, {"data": ["[10 Users] 04_OwnerDetails", 66, 0, 0.0, 8.606060606060606, 4, 26, 7.0, 13.300000000000004, 18.949999999999996, 26.0, 4.86761560587064, 45.56734687292573, 0.7225366914964231], "isController": false}, {"data": ["[Warmup] 01_WelcomePage", 98, 0, 0.0, 4.755102040816325, 1, 19, 4.0, 8.0, 12.0, 19.0, 6.759086833574729, 15.141938668184013, 0.9504965859714463], "isController": false}, {"data": ["[1 Users] 05_VisitBillingCalculation", 7, 0, 0.0, 12.0, 6, 27, 11.0, 27.0, 27.0, 27.0, 0.6012712592338085, 2.9505742945799693, 0.10393067664490638], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST-0", 62, 0, 0.0, 2.4193548387096775, 1, 23, 2.0, 3.0, 4.0, 23.0, 4.840724547158026, 1.446544640068707, 1.6119990923641474], "isController": false}, {"data": ["[25 Users] 05_VisitBillingCalculation", 160, 0, 0.0, 4.2250000000000005, 2, 21, 4.0, 6.900000000000006, 9.0, 17.949999999999932, 12.046378557446168, 59.114308839030265, 2.0822353561210662], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST", 299, 0, 0.0, 5.294314381270899, 1, 39, 4.0, 10.0, 14.0, 24.0, 22.875066942085535, 113.21371021727488, 12.040684650179788], "isController": false}, {"data": ["[1 Users] 02_VeterinariansList", 8, 0, 0.0, 12.125, 6, 22, 11.0, 22.0, 22.0, 22.0, 0.5903623348830345, 2.913760977049664, 0.08820843480185964], "isController": false}, {"data": ["[25 Users] 02_VeterinariansList", 174, 0, 0.0, 3.5689655172413786, 2, 15, 3.0, 5.0, 7.0, 12.75, 12.106874478151962, 59.75404649666017, 1.8089372999582523], "isController": false}, {"data": ["[50 Users] 04_OwnerDetails", 311, 0, 0.0, 15.469453376205788, 4, 69, 10.0, 33.80000000000001, 41.39999999999998, 51.879999999999995, 22.619826896501564, 211.75162170885153, 3.3576305549494507], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST", 7, 0, 0.0, 22.714285714285715, 5, 47, 22.0, 47.0, 47.0, 47.0, 0.6019951840385277, 2.979405852253182, 0.3168705119109047], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST-0", 155, 0, 0.0, 1.5677419354838702, 0, 6, 1.0, 2.0, 3.0, 6.0, 11.891062523973916, 3.553383918296893, 3.959816719409283], "isController": false}, {"data": ["[50 Users] 01_WelcomePage", 331, 0, 0.0, 2.6676737160120845, 0, 18, 2.0, 6.0, 7.0, 12.680000000000007, 22.665023281292797, 50.77496426492742, 3.1872688989317997], "isController": false}, {"data": ["[1 Users] 01_WelcomePage", 8, 0, 0.0, 5.25, 2, 12, 4.5, 12.0, 12.0, 12.0, 0.5904930617065249, 1.3228428550339533, 0.08303808680248007], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST-1", 299, 0, 0.0, 4.063545150501675, 1, 33, 3.0, 9.0, 12.0, 16.0, 22.878567602724004, 106.39427629313643, 4.423785532557961], "isController": false}, {"data": ["[1 Users] 03_FindOwnersSearch", 8, 0, 0.0, 34.0, 14, 60, 33.0, 60.0, 60.0, 60.0, 0.5891450033139406, 5.153292768245084, 0.09205390676780321], "isController": false}, {"data": ["[25 Users] 04_OwnerDetails", 165, 0, 0.0, 6.527272727272729, 4, 72, 5.0, 8.400000000000006, 13.0, 48.24000000000012, 12.066695919262834, 112.9602998848179, 1.791150175515577], "isController": false}, {"data": ["[50 Users] 02_VeterinariansList", 325, 0, 0.0, 8.843076923076922, 2, 46, 6.0, 19.0, 25.0, 30.0, 22.637041164588705, 111.72617777913213, 3.3822922833809295], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST-0", 299, 0, 0.0, 1.1471571906354523, 0, 16, 1.0, 2.0, 2.0, 6.0, 22.878567602724004, 6.836759459407759, 7.618741750516489], "isController": false}, {"data": ["[10 Users] 01_WelcomePage", 71, 0, 0.0, 2.5211267605633805, 1, 9, 2.0, 5.0, 6.0, 9.0, 4.868348875479978, 10.906242500342842, 0.6846115606143719], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST-1", 155, 0, 0.0, 2.2580645161290334, 1, 12, 2.0, 3.0, 5.0, 9.199999999999989, 11.893799877225293, 55.310815444674645, 2.299777710635359], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 4682, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
