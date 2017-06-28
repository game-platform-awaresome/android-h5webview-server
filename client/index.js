var url = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
console.log("connectURL:", url);
var socketClient = io.connect(url);
socketClient.on("connect", function () {
    console.log("socket client connected!");
    socketClient.emit("pathCheck");
});
var logger = document.getElementsByName("console_log")[0];
socketClient.on('console.log', function (args) {
    // var args = err?errmsg:msg;
    // if(args.indexOf("BUILD SUCCESSFUL")!=-1){
    //     // require('electron').remote.require('./packager').openOutputDir();
    //     document.getElementsByTagName("form")[0].innerHTML += '<input name="download" type="button" value="下载apk" onclick="downloadAPK()"/>';
    // }
    logger.innerHTML += "<p>" + args + "</p>";
    if (document.getElementsByName("clean").length === 0) {
        document.getElementsByTagName("form")[0].innerHTML += '<input name="clean" type="button" value="清空日志" onclick="cleanLog()"/>';
        console.log(document.getElementsByTagName("form"));
    }
});
//      showAndroidHomeConfigPanel();

//      const icon_path = document.getElementsByName("icon_path")[0];

window.ondragover = function (e) {
    e.preventDefault();
    return false;
    // body...
};

window.ondrop = function (e) {
    e.preventDefault();
    return false;
    // body...
};

//      const image_holder = document.getElementsByName('image_holder')[0];
var gameicon_image_holder = document.getElementById("gameicon_image_holder");
var loading_image_holder = document.getElementById("loading_image_holder");
var ON_DRAG_OVER = function () {
    this.className = "holder hover";
    return false;
};
var ON_DRAG_LEAVE = function () {
    this.className = "holder";
    return false;
};
var ON_DROP = function (e) {
    e.preventDefault();
    console.log(e.dataTransfer);
    for (var i = 0; i < e.dataTransfer.files.length; i++) {
        var path = e.dataTransfer.files[i].path;
        document.getElementById(this.id).value = path;
    }
    return false;
};

gameicon_image_holder.ondragover = ON_DRAG_OVER;
gameicon_image_holder.ondragleave = ON_DRAG_LEAVE;
gameicon_image_holder.ondrop = ON_DROP.bind(gameicon_image_holder);


loading_image_holder.ondragover = ON_DRAG_OVER;
loading_image_holder.ondragleave = ON_DRAG_LEAVE;
loading_image_holder.ondrop = ON_DROP.bind(loading_image_holder);

////渠道配置区域///////////////////////////////////////////////////////////////////////////////////

var currentLoadingImgBuffer = { file: null, response: null };
var currentIconImgBuffer = { file: null, response: null };

var ChannelConfig = function () { };
var prototype = ChannelConfig.prototype;
prototype.channelName = "EmtyName";
prototype.packageName = "egretgame";
prototype.loadingImgBuffer = null;
prototype.iconImgBuffer = null;
prototype.gameURL = null;
prototype.appId = "";
prototype.channelId = "";
prototype.radio = null;
prototype.wx_app_id = "";
prototype.wx_partner_id = "";
prototype.wx_key = "";

var channelMap = {};
var channelCounter = 0;



var currentChannelConfig = new ChannelConfig();

function showConfig(channelConfig) {
    var game_channel = document.getElementById("game_channel");
    var ele_package_name = document.getElementsByName("package_name")[0];
    var ele_gameURL = document.getElementsByName("game_url")[0];
    var loadingImgHolder = document.getElementById("loading_image_holder");
    var iconImgHolder = document.getElementById("gameicon_image_holder");

    var ele_appId = document.getElementsByName("app_id")[0];
    var ele_channelId = document.getElementsByName("channel_id")[0];

    //微信配置////////////////////////////////
    var wx_app_id_ele = document.getElementById("wx_appid");
    var wx_partner_id_ele = document.getElementById("wx_partner_id");
    var wx_key_ele = document.getElementById("wx_key");
    /////////////////////////////////////////

    if (channelConfig) {
        game_channel.value = channelConfig.channelName;
        ele_package_name.value = channelConfig.packageName;
        ele_gameURL.value = channelConfig.gameURL;
        ele_appId.value = channelConfig.appId;
        ele_channelId.value = channelConfig.channelId;

        currentLoadingImgBuffer = channelConfig.loadingImgBuffer;
        currentIconImgBuffer = channelConfig.iconImgBuffer;
        currentChannelConfig = channelConfig;

        loadingImgHolder.value = channelConfig.loadingImgBuffer.response;
        iconImgHolder.value = channelConfig.iconImgBuffer.response;

        //微信信息/////////////////////////////////
        wx_app_id_ele.value = tempConfig.wx_app_id;
        wx_partner_id_ele.value = tempConfig.wx_partner_id;
        wx_key_ele.value = tempConfig.wx_key;
        //////////////////////////////////////////

    } else {
        console.error("showConfig channelConfig is null。");
    }
}

function clearConfigShow() {
    document.getElementById("game_channel").value = null;
    document.getElementsByName("package_name")[0].value = null;
    document.getElementsByName("game_url")[0].value = null;
    document.getElementById("loading_image_holder").value = null;
    document.getElementById("gameicon_image_holder").value = null;

    //微信配置////////////////////////////////
    document.getElementById("wx_appid").value = null;
    document.getElementById("wx_partner_id").value = null;
    document.getElementById("wx_key").value = null;
    /////////////////////////////////////////
}

function addChannel() {
    var game_channel = document.getElementById("game_channel");
    var ele_package_name = document.getElementsByName("package_name")[0];
    var ele_gameURL = document.getElementsByName("game_url")[0];
    var ele_appId = document.getElementsByName("app_id")[0];
    var ele_channelId = document.getElementsByName("channel_id")[0];
    /////////////

    var tempConfig = currentChannelConfig;

    var isNewConfig = tempConfig.radio ? false : true;
    if (isNewConfig) {
        channelCounter++;
    }

    currentChannelConfig = new ChannelConfig();

    var channelName = game_channel.value || "Channel_" + channelCounter;
    var packageName = ele_package_name.value;
    var gameURL = ele_gameURL.value;
    var appId = ele_appId.value;
    var channelId = ele_channelId.value;

    if (!gameURL || gameURL.length < 1) {
        console.error("请输入游戏地址");
        return;
    }
    if(!appId || appId.length < 1){
        console.error("请输入游戏ID(appId)");
        return;
    }
    if(!channelId || channelId.length < 1){
        console.error("请输入渠道ID");
        return;
    }
    if (!packageName || packageName.length < 1) {
        console.error("请输入游戏报名");
        return;
    }
    if (!currentLoadingImgBuffer.file) {
        console.error("请上传加载图片");
        return;
    }
    if (!currentIconImgBuffer.file) {
        console.error("请上传ICON图片");
        return;
    }

    tempConfig.channelName = channelName;
    tempConfig.packageName = packageName;
    tempConfig.loadingImgBuffer = currentLoadingImgBuffer;
    tempConfig.iconImgBuffer = currentIconImgBuffer;
    tempConfig.gameURL = gameURL;
    tempConfig.appId = appId;
    tempConfig.channelId = channelId;

    //微信配置////////////////////////////////
    tempConfig.wx_app_id = document.getElementById("wx_appid").value || "";
    tempConfig.wx_partner_id = document.getElementById("wx_partner_id").value || "";
    tempConfig.wx_key = document.getElementById("wx_key").value || "";
    /////////////////////////////////////////

    var radio = null;
    if (isNewConfig) {
        var channelNameList = document.getElementById("channelnamelist");

        var li = channelNameList;

        radio = document.createElement("input");
        radio.type = "radio";
        radio.id = channelName + channelCounter;

        tempConfig.radio = radio;


        var label = document.createElement("label");
        label.setAttribute("for", radio.id);
        label.textContent = channelName;

        li.appendChild(radio);
        li.appendChild(label);
        li.appendChild(document.createElement("br"));

        function changeRadio(radio, changeConfigShow) {
            var radioObj;
            var channelObj;
            var channel;
            var i;
            var channelNames = Object.getOwnPropertyNames(channelMap);
            for (i = 0; i < channelNames.length; i++) {
                channel = channelMap[channelNames[i]];
                radioObj = channel.radio;
                if (channel.radio != radio) {
                    radioObj.checked = false;
                } else {
                    channelObj = channel;
                }
            }
            if (radio.checked && changeConfigShow) {
                console.log("channel list item mousedown id = " + radio.id);
                showConfig(channelObj);
            }
        }

        radio.onclick = function (museEvent) {
            var radio = museEvent.target;
            changeRadio(radio, true);
        }

        changeRadio(radio, false);
        radio.checked = true;
    } else {
        radio = tempConfig.radio;
    }

    if (!channelMap.hasOwnProperty(radio.id)) {
        channelMap[radio.id] = (tempConfig);
    }

    clearConfigShow();
}

function onAddChannel() {
    addChannel();
}

function onLoadingImgDrop(event) {
    let files = event.dataTransfer.files;
    file = files[0];
    uploadLoadingImage(file, currentLoadingImgBuffer);
    currentLoadingImgBuffer.file = file;
    console.log("----------- onLoadingImgDrop = " + event);
}

function onIconImgDrop(event) {
    let files = event.dataTransfer.files;
    file = files[0];
    uploadIconImage(file, currentIconImgBuffer);
    currentIconImgBuffer.file = file;
    console.log("----------- onIconImgDrop = " + event);
}


//////////////////////////////////////////////////////////////////////////////////////////////////

function showAndroidHomeConfigPanel() {
    document.getElementsByName("sdk_div")[0].innerHTML =
        '请拖入AndroidSDK目录:' +
        '<textarea class="holder" rows="5" maxrows="5" id="sdk_path_holder" placeholder="输入地址.拖动文件"></textarea>';
    var sdk_path_holder = document.getElementById('sdk_path_holder');
    sdk_path_holder.ondragover = ON_DRAG_OVER;
    sdk_path_holder.ondragleave = ON_DRAG_LEAVE;
    sdk_path_holder.ondrop = ON_DROP.bind(sdk_path_holder);
}

// function downloadAPK(){
//     window.open(window.location.host+"/download","_blank");
// }

function cleanLog() {
    logger.innerHTML = '';
    var formEle = document.getElementsByTagName("form")[0];
    formEle.removeChild(formEle.childNodes[formEle.childNodes.length - 1]);
}

function uploadLoadingImage(imgFile, buffer) {
    uploadFile('loading_image', imgFile, buffer);
}

function uploadIconImage(imgFile, buffer) {
    uploadFile('gameicon_image', imgFile, buffer);
}

function uploadFile(name, inFile, resultBuffer) {
    console.log("uploadFile ===================================");
    var file = null;
    var files = null;
    let fileinput = document.getElementById(name + "_fileinput");
    let holder = document.getElementById(name + "_holder");

    var formData = new FormData();

    if (inFile) {
        file = inFile;
    } else {
        files = fileinput.files;
        file = files[0];
    }
    formData.append('myfile', file);
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/upload', true);
    xhr.onload = function (e) {
        if (this.status == 200) {
            holder.value = this.response;
            if (resultBuffer) {
                resultBuffer.response = this.response;
            }
            // document.getElementById('result').innerHTML = this.response;
        }
    };
    xhr.send(formData);
}

function onStart() {
    var game_name_val = document.getElementsByName("game_name")[0].value;
    console.log('game_name_val:', game_name_val);
    var package_name_val = document.getElementsByName("package_name")[0].value;
    console.log('package_name_val:', package_name_val);
    var game_url_val = document.getElementsByName("game_url")[0].value;
    console.log('game_url_val:', game_url_val);
    var version_name_val = document.getElementsByName("version_name")[0].value;
    console.log('version_name_val:', version_name_val);
    var version_code_val = document.getElementsByName("version_code")[0].value;
    console.log('version_code_val:', version_code_val);
    var gamecenter_url_nodes = document.getElementsByName("gamecenter_url");
    var gamecenter_url_val = null;
    if (gamecenter_url_nodes && gamecenter_url_nodes.length > 0) {
        gamecenter_url_val = gamecenter_url_nodes[0].value;
    }
    console.log('gamecenter_url_val:', gamecenter_url_val);
    //var game_id_val = document.getElementsByName("game_id")[0].value;
    var game_id_val = "org.egret.com";
    console.log('game_id_val:', game_id_val);
    var icon_path_val = document.getElementById("gameicon_image_holder").value;
    console.log('icon_path_val:', icon_path_val);
    var loadingimage_path_val = document.getElementById("loading_image_holder").value;
    console.log('loadingimage_path_val:', loadingimage_path_val);
    var android_path_val = document.getElementById("sdk_path_holder") != null ?
        document.getElementById("sdk_path_holder").value : null;
    console.log('android_path_val', android_path_val);

    // var channelNames = Object.getOwnPropertyNames(channelMap);
    // var channelConfig = channelMap[channelNames[0]];
    // socketClient.emit('start', {
    //     game_name: game_name_val,
    //     package_name: channelConfig.packageName,
    //     game_url: channelConfig.gameURL,
    //     icon_path: channelConfig.iconImgBuffer.response,
    //     version_name: version_name_val,
    //     version_code: version_code_val,
    //     game_id: game_id_val,
    //     loadingimage_path: channelConfig.loadingImgBuffer.response,
    //     gamecenter_url: gamecenter_url_val,
    //     android_path: android_path_val
    // });

    function addConfigCB(isOver) {
        if (isOver) {
            socketClient.emit('startWithChannelBuffer', {
                game_name: game_name_val,
                version_name: version_name_val,
                version_code: version_code_val,
                game_id: game_id_val,
                gamecenter_url: gamecenter_url_val,
                android_path: android_path_val
            });
        }
    }

    var i = 0;
    var channelNames = Object.getOwnPropertyNames(channelMap);
    var len = channelNames.length;
    var channelConfig;
    for (i = 0; i < channelNames.length; i++) {
        channelConfig = channelMap[channelNames[i]];
        socketClient.emit('addChannelConfig', {
            channel_name: channelConfig.channelName,
            package_name: channelConfig.packageName,
            game_url: channelConfig.gameURL,
            app_id :channelConfig.appId,
            channel_id : channelConfig.channelId,
            icon_path: channelConfig.iconImgBuffer.response,
            loadingimage_path: channelConfig.loadingImgBuffer.response,
            wx_app_id: channelConfig.wx_app_id,
            wx_partner_id: channelConfig.wx_partner_id,
            wx_key: channelConfig.wx_key
        }, len, addConfigCB);
    }


}
//复选框
var launcherSwitch = document.getElementsByName("quicklaunch")[0];
function onCheckBoxClick(e) {
    var checkBox = e.target;
    var quicklaunch = document.getElementsByName("gamecenter_url")[0];
    if (checkBox.checked) {
        quicklaunch.disabled = false;
    } else {
        quicklaunch.disabled = true;
    }
}

