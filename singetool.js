
var md5 = require("./md5");

 function GetQueryString(queryStr ,name) {
     var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
     var r = queryStr.match(reg);
     if (r != null) return unescape(r[2]); return null;
 }



function checkSinge( queryStr ) {
    var query_name = GetQueryString(queryStr,"name");
    var query_timestamp = GetQueryString(queryStr,"timestamp");
    var query_sign = GetQueryString(queryStr,"sign");
    console.error("query_name =" + query_name + "; query_timestamp=" + query_timestamp + "; query_sign=" + query_sign);
    if (null == query_timestamp || null == query_name || null == query_sign) {
        return false;
    }

    function checkTime() {
        if (null == query_timestamp) {
            console.error("query_timestamp is null");
            return false;
        }

        var date =new Date();

        var signDate = new Date();
        signDate.setTime(query_timestamp * 1000);
        console.log("checktime ======== " + date.toDateString() + ";" + signDate.toDateString());
        return date.toDateString() == signDate.toDateString();
    }
    if (!checkTime()) {
        return false;
    }


    function checkSign() {
        secret = 'sdas$sd@#d';
        var signe = md5.hex_md5(query_name + query_timestamp + secret);
        console.log("checkSign =========== signe = " + signe);
        return query_sign == signe;
    }
    if (!checkSign()) {
        return false;
    }
    return true;
};
exports.checkSinge = checkSinge;