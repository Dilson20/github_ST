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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 5544, 0, 0.0, 16.731060606060606, 1, 214, 9.0, 36.0, 58.0, 132.0, 58.527315914489314, 305.8961591119029, 12.889748696885723], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["[50 Users] 03_FindOwnersSearch", 379, 0, 0.0, 65.97625329815305, 13, 214, 51.0, 137.0, 153.0, 199.5999999999999, 20.058216459380787, 259.38647376951576, 3.1340963217782485], "isController": false}, {"data": ["[10 Users] 02_VeterinariansList", 84, 0, 0.0, 9.023809523809526, 3, 23, 7.0, 17.0, 19.75, 23.0, 4.341982838829733, 21.430059831489714, 0.648753295254833], "isController": false}, {"data": ["[10 Users] 03_FindOwnersSearch", 81, 0, 0.0, 16.555555555555557, 7, 47, 14.0, 27.799999999999997, 34.599999999999966, 47.0, 4.256884591128863, 27.997545968441248, 0.6651382173638849], "isController": false}, {"data": ["[25 Users] 03_FindOwnersSearch", 198, 0, 0.0, 33.06060606060607, 8, 91, 23.0, 71.0, 77.04999999999998, 90.00999999999999, 10.374096196164729, 89.75912072330505, 1.6209525306507389], "isController": false}, {"data": ["[50 Users] 05_VisitBillingCalculation", 366, 0, 0.0, 18.20218579234973, 3, 69, 12.0, 51.900000000000034, 60.0, 66.31999999999994, 19.87186448039961, 97.0694005185145, 3.434882825225323], "isController": false}, {"data": ["[Warmup] 02_VeterinariansList", 98, 0, 0.0, 14.612244897959185, 4, 161, 11.0, 27.0, 28.099999999999994, 161.0, 6.7642186637217, 33.385118287548316, 1.0106693901849808], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST-0", 10, 0, 0.0, 20.1, 4, 100, 12.0, 91.50000000000003, 100.0, 100.0, 0.5681495369581274, 0.16922422731662976, 0.18919823447531392], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST", 76, 0, 0.0, 13.697368421052632, 7, 37, 11.0, 24.0, 27.0, 37.0, 4.193797594084539, 20.739639664496192, 2.2033819390795717], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST-1", 10, 0, 0.0, 9.2, 6, 22, 8.5, 20.700000000000003, 22.0, 22.0, 0.5706785367802317, 2.6522062075557837, 0.10978874193916567], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST", 183, 0, 0.0, 15.442622950819674, 6, 40, 11.0, 30.0, 34.0, 40.0, 10.110497237569062, 50.03841505524861, 5.3216721339779], "isController": false}, {"data": ["[10 Users] 05_VisitBillingCalculation", 78, 0, 0.0, 12.33333333333334, 5, 32, 10.0, 23.0, 26.0, 32.0, 4.232000434051327, 20.672330245239, 0.73150788752645], "isController": false}, {"data": ["[1 Users] 04_OwnerDetails", 10, 0, 0.0, 19.9, 9, 61, 13.5, 57.90000000000001, 61.0, 61.0, 0.5660270560932813, 5.29821223892002, 0.08401964113884643], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST-1", 76, 0, 0.0, 6.881578947368421, 3, 25, 5.0, 13.299999999999997, 14.149999999999991, 25.0, 4.195186575402959, 19.49696573470965, 0.8070817923382645], "isController": false}, {"data": ["[25 Users] 01_WelcomePage", 205, 0, 0.0, 5.346341463414635, 1, 22, 4.0, 10.400000000000006, 11.699999999999989, 19.879999999999995, 10.473076530090937, 23.462146054715436, 1.4727763870440378], "isController": false}, {"data": ["[Warmup] 03_FindOwnersSearch", 95, 0, 0.0, 21.789473684210527, 8, 143, 17.0, 40.0, 53.0, 143.0, 6.714255424411619, 39.131519895398974, 1.0491024100643156], "isController": false}, {"data": ["[10 Users] 04_OwnerDetails", 80, 0, 0.0, 14.1375, 6, 30, 11.0, 25.900000000000006, 27.950000000000003, 30.0, 4.27578834847675, 40.02288214858364, 0.6346873329770176], "isController": false}, {"data": ["[Warmup] 01_WelcomePage", 98, 0, 0.0, 6.714285714285715, 2, 34, 5.0, 12.0, 13.049999999999997, 34.0, 6.758154610026895, 15.13985026894697, 0.9503654920350321], "isController": false}, {"data": ["[1 Users] 05_VisitBillingCalculation", 10, 0, 0.0, 15.9, 7, 42, 14.5, 39.80000000000001, 42.0, 42.0, 0.5673436968115284, 2.771340987745376, 0.09806624446839896], "isController": false}, {"data": ["[10 Users] 06_CreateOwner_POST-0", 76, 0, 0.0, 6.644736842105262, 3, 13, 5.5, 12.0, 12.149999999999991, 13.0, 4.194723479412739, 1.249404942598521, 1.396875689921625], "isController": false}, {"data": ["[25 Users] 05_VisitBillingCalculation", 189, 0, 0.0, 15.830687830687832, 4, 47, 13.0, 33.0, 38.5, 42.49999999999997, 10.255018990775909, 50.09336424986435, 1.772596056022789], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST", 360, 0, 0.0, 14.986111111111105, 4, 63, 11.0, 31.900000000000034, 41.0, 52.389999999999986, 19.881813663224168, 98.39944496603523, 10.465134340310378], "isController": false}, {"data": ["[1 Users] 02_VeterinariansList", 10, 0, 0.0, 12.799999999999999, 5, 24, 12.5, 23.400000000000002, 24.0, 24.0, 0.5669255626736209, 2.798087689211407, 0.08470665145416408], "isController": false}, {"data": ["[25 Users] 02_VeterinariansList", 203, 0, 0.0, 12.492610837438418, 3, 49, 11.0, 24.0, 35.0, 48.920000000000016, 10.47471620227038, 51.69845281862745, 1.5650699013157896], "isController": false}, {"data": ["[50 Users] 04_OwnerDetails", 373, 0, 0.0, 20.683646112600552, 4, 81, 16.0, 43.0, 58.0, 75.51999999999998, 19.857325383304943, 185.87154667868933, 2.9475717365843273], "isController": false}, {"data": ["[1 Users] 06_CreateOwner_POST", 10, 0, 0.0, 29.7, 10, 110, 20.5, 102.80000000000003, 110.0, 110.0, 0.5674080798910577, 2.8060102700862464, 0.29811088572401273], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST-0", 183, 0, 0.0, 6.114754098360652, 2, 18, 5.0, 11.0, 11.799999999999983, 16.319999999999993, 10.112173288390341, 3.0216398954246557, 3.367432706387799], "isController": false}, {"data": ["[50 Users] 01_WelcomePage", 405, 0, 0.0, 5.995061728395062, 1, 30, 4.0, 11.0, 14.0, 24.879999999999995, 20.623281393217233, 46.20098390238313, 2.900148945921173], "isController": false}, {"data": ["[1 Users] 01_WelcomePage", 11, 0, 0.0, 7.545454545454545, 2, 13, 7.0, 12.600000000000001, 13.0, 13.0, 0.5607381352908192, 1.2561848460518936, 0.07885380027527145], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST-1", 360, 0, 0.0, 10.336111111111109, 2, 54, 7.0, 24.0, 31.94999999999999, 43.389999999999986, 19.88510826336721, 92.47352104507291, 3.8449721056120194], "isController": false}, {"data": ["[1 Users] 03_FindOwnersSearch", 10, 0, 0.0, 19.599999999999998, 12, 36, 19.0, 35.0, 36.0, 36.0, 0.5657068507099622, 3.3403221700514796, 0.08839169542343157], "isController": false}, {"data": ["[25 Users] 04_OwnerDetails", 194, 0, 0.0, 18.30412371134021, 5, 60, 13.0, 41.5, 48.25, 57.150000000000034, 10.315308129951614, 96.55491057186156, 1.5311785505396929], "isController": false}, {"data": ["[50 Users] 02_VeterinariansList", 395, 0, 0.0, 14.164556962025326, 2, 59, 9.0, 35.400000000000034, 45.0, 53.08000000000004, 20.454663145357568, 100.95494876624721, 3.0562143176169023], "isController": false}, {"data": ["[50 Users] 06_CreateOwner_POST-0", 360, 0, 0.0, 4.56944444444444, 2, 17, 4.0, 8.0, 9.0, 13.389999999999986, 19.893899204244033, 5.944856598143236, 6.624823856100796], "isController": false}, {"data": ["[10 Users] 01_WelcomePage", 85, 0, 0.0, 5.564705882352938, 2, 12, 5.0, 10.0, 10.700000000000003, 12.0, 4.365466591340969, 9.779668320836116, 0.6138937394073237], "isController": false}, {"data": ["[25 Users] 06_CreateOwner_POST-1", 183, 0, 0.0, 9.158469945355195, 3, 29, 6.0, 20.0, 22.0, 27.319999999999993, 10.116645475150642, 47.04586739524021, 1.9559862865553652], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 5544, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
