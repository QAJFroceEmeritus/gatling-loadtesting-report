var stats = {
    type: "GROUP",
name: "All Requests",
path: "",
pathFormatted: "group_missing-name--1146707516",
stats: {
    "name": "All Requests",
    "numberOfRequests": {
        "total": "310",
        "ok": "310",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "1643",
        "ok": "1643",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "4029",
        "ok": "4029",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "1960",
        "ok": "1960",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "296",
        "ok": "296",
        "ko": "-"
    },
    "percentiles1": {
        "total": "1895",
        "ok": "1894",
        "ko": "-"
    },
    "percentiles2": {
        "total": "1967",
        "ok": "1967",
        "ko": "-"
    },
    "percentiles3": {
        "total": "2303",
        "ok": "2303",
        "ko": "-"
    },
    "percentiles4": {
        "total": "3156",
        "ok": "3849",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0.0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 310,
    "percentage": 100.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 0,
    "percentage": 0.0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "3.37",
        "ok": "3.37",
        "ko": "-"
    }
},
contents: {
"req_student-dashboa-1256759983": {
        type: "REQUEST",
        name: "Student Dashboard",
path: "Student Dashboard",
pathFormatted: "req_student-dashboa-1256759983",
stats: {
    "name": "Student Dashboard",
    "numberOfRequests": {
        "total": "310",
        "ok": "310",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "1643",
        "ok": "1643",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "4029",
        "ok": "4029",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "1960",
        "ok": "1960",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "296",
        "ok": "296",
        "ko": "-"
    },
    "percentiles1": {
        "total": "1894",
        "ok": "1894",
        "ko": "-"
    },
    "percentiles2": {
        "total": "1967",
        "ok": "1967",
        "ko": "-"
    },
    "percentiles3": {
        "total": "2303",
        "ok": "2303",
        "ko": "-"
    },
    "percentiles4": {
        "total": "3849",
        "ok": "3156",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0.0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 310,
    "percentage": 100.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 0,
    "percentage": 0.0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "3.37",
        "ok": "3.37",
        "ko": "-"
    }
}
    }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
