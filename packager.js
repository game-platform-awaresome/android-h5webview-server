'use strict';
/**
 * Created by yanjiaqi on 16/4/21.
 */
const zlib = require('zlib')
const exec = require('child_process').exec;
const fs = require("fs");
const FileUtil = require('./utils/FileUtil');
const XMLUtil = require('./utils/XMLUtil');
// const async = require('async');

let TEMPLATE_PATH = null;
let GRADLE_PATH = null;
let ICON_PATH = null;
let LOADINGIMAGE_PATH = null;
let STRINGS_PATH = null;
let OUTPUT_PATH = null;
let ANDROID_PATH = null;
var OUT_APK_FOLDER = null;
var OUT_BUILD_RESULT_FOLDER = null;

function setPackagerTemplatePath(path) {
    TEMPLATE_PATH = path;
    GRADLE_PATH = FileUtil.joinPath(TEMPLATE_PATH, './app/build.gradle');
    ICON_PATH = FileUtil.joinPath(TEMPLATE_PATH, './app/src/main/res/drawable/icon.png');
    LOADINGIMAGE_PATH = FileUtil.joinPath(TEMPLATE_PATH, './app/src/main/res/drawable/');
    STRINGS_PATH = FileUtil.joinPath(TEMPLATE_PATH, './app/src/main/res/values/strings.xml');
    ANDROID_PATH = FileUtil.joinPath(TEMPLATE_PATH, './local.properties');
    OUTPUT_PATH = FileUtil.joinPath(TEMPLATE_PATH, './app/build/outputs/apk');


}
exports.setPackagerTemplatePath = setPackagerTemplatePath;

function setDefaultPath() {
    OUT_APK_FOLDER = FileUtil.joinPath(__dirname, "./outApk");
    if (!FileUtil.exists(OUT_APK_FOLDER)) {
        FileUtil.createDirectory(OUT_APK_FOLDER);
    }

    OUT_BUILD_RESULT_FOLDER = FileUtil.joinPath(__dirname, "./buildapks");
    if (!FileUtil.exists(OUT_BUILD_RESULT_FOLDER)) {
        FileUtil.createDirectory(OUT_BUILD_RESULT_FOLDER);
    }

    setPackagerTemplatePath(FileUtil.joinPath(__dirname, './android_template'));
}

function setAndroidHome(path, cb) {
    if (FileUtil.exists(path)) {
        let content = 'sdk.dir=' + FileUtil.joinPath(path);
        FileUtil.save(ANDROID_PATH, content);
        cb(false, 'ANDROID SDK PATH:' + path + ' SET!');
        return true;
    } else {
        cb(true, '', 'ANDROID_HOME:' + path + " not found!");
        return false;
    }
}

exports.setAndroidHome = setAndroidHome;

function pathChecked(cb) {
    if (TEMPLATE_PATH === null) {
        setDefaultPath();
    }
    cb(false, "TEMPLATE_PATH:" + TEMPLATE_PATH);
    cb(false, "GRADLE_PATH:" + GRADLE_PATH);
    cb(false, "ICON_PATH:" + ICON_PATH);
    cb(false, "STRINGS_PATH:" + STRINGS_PATH);
    cb(false, "ANDROID_PATH:" + ANDROID_PATH);
    if (FileUtil.exists(TEMPLATE_PATH)) {
        if (FileUtil.exists(ANDROID_PATH)) {
            cb(false, 'Path Checked!');
            return true;
        } else if (process.env.ANDROID_HOME) {
            cb(false, process.env);
            return setAndroidHome(process.env.ANDROID_HOME, cb);
        } else {
            cb(true, '', 'Need ANDROID_HOME!');
            return false;
        }
    } else {
        cb(true, '', TEMPLATE_PATH + " DON'T EXIST!");
        return false
    }
}
exports.pathChecked = pathChecked;

var currentResultName = null;
var currentResultFolder = null;
function createNewResult(name){
    currentResultName = name;
    currentResultFolder = FileUtil.joinPath(BUILD_RESULT_FOLDER , "/"+name);
    if(!FileUtil.exists(currentResultFolder)){
        FileUtil.createDirectory(currentResultFolder);
    }
}

function writeStringResource(appName, gameUrl, gameCenterUrl, gameId, channelId, cb) {
    var xmlContent =
        '<resources>' +
        '<string name="app_name">' +
        appName +
        '</string>' +
        '<string name="game_url">' +
        XMLUtil.escapeHtml2(gameUrl) +
        '</string>';
    if (gameCenterUrl != null && gameCenterUrl != "") {
        xmlContent = xmlContent +
            '<string name="gamecenter_url">' +
            XMLUtil.escapeHtml2(gameCenterUrl) +
            '</string>';
    }
    if (gameId != null && gameId != "") {
        xmlContent = xmlContent +
            '<string name="game_id">' +
            gameId +
            '</string>';
    }

    if (channelId != null && channelId.length > 0) {
        xmlContent = xmlContent +
            '<string name="channel_id">' +
            channelId +
            '</string>';
    }
    xmlContent = xmlContent +
        '</resources>';
    FileUtil.save(STRINGS_PATH, xmlContent);
    cb(false, "strings.xml saved!");
    return true;
    //}else{
    //    cb(true,'','strings.xml can not be saved!');
    //    return false;
    //}
}

function writeLoadingImage(loadingPath, cb) {
    tickOff('loading');
    if (FileUtil.exists(loadingPath)) {
        FileUtil.getFileName()
        let fileType = FileUtil.getExtension(loadingPath);
        let drawableName = FileUtil.joinPath(LOADINGIMAGE_PATH, "loading." + fileType);
        FileUtil.copy(loadingPath, drawableName);
        cb(false, drawableName + " saved!");
        return true;
    } else {
        cb(true, loadingPath + "don't exist!");
        return false;
    }
}

function tickOff(fileName) {
    let fileList = FileUtil.getDirectoryAllListing(LOADINGIMAGE_PATH);
    fileList.filter(function (path) {
        return FileUtil.getFileName(path).indexOf(fileName) != -1;
    }).forEach(function (path) {
        FileUtil.remove(fileName);
    })
}

function writeIconImage(iconPath, cb) {
    FileUtil.copy(iconPath, ICON_PATH);
    cb(false, "icon.png saved!");
    return true;
}

function writeGradlePackageName(packageName, versionName, versionCode, cb) {
    var gradle_content_raw = FileUtil.read(GRADLE_PATH);
    //var gradle_content_raw = fs.read(GRADLE_PATH);
    if (gradle_content_raw === '') {
        cb(true, '', "读取配置文件失败!");
        return false;
    }
    cb(false, gradle_content_raw);
    console.log("我是文件内容", gradle_content_raw);

    var gradle_content_new = '';
    var _start = gradle_content_raw.indexOf('\/\/{packageName');
    var _end = gradle_content_raw.indexOf('\/\/packageName}');
    if (_start == -1 || _end == -1) {
        cb(true, '', "配置文件损坏!");
        return false;
    }
    cb(false, "_start:" + _start + " _end:" + _end);

    var gradleAppIdReplaceStr =
        'applicationId "' + packageName + '"\n' +
        'versionName "' + versionName + '"\n' +
        'versionCode ' + versionCode + "\n";

    gradle_content_new = gradle_content_new.concat(gradle_content_raw.substring(0, _start));
    gradle_content_new = gradle_content_new.concat('\/\/{packageName\n' + gradleAppIdReplaceStr);
    gradle_content_new = gradle_content_new.concat(gradle_content_raw.substring(_end));

    cb(false, "gradle_content_new:" + gradle_content_new);
    FileUtil.save(GRADLE_PATH, gradle_content_new);
    return true;
}

const WX_CONFIG_FILENAME = "Constants.java";
function writeWXConfig(wx_app_id, wx_partner_id, wx_key) {
    var fileList = FileUtil.searchByFunction(FileUtil.joinPath(TEMPLATE_PATH, "/app/src/main"), function (filePath) {

        if (filePath.indexOf(WX_CONFIG_FILENAME) == filePath.length - WX_CONFIG_FILENAME.length) {
            return true;
        }
        return false;
    }, false);

    if (!fileList || fileList.length != 1) {
        console.error("can't find config file: Constants.java. " + fileList + "/" + fileList.length);
        return false;
    }

    var configFilePath = fileList[0];
    // console.log("find wx config file : "+configFilePath);
    var configfile_content_raw = FileUtil.read(configFilePath);
    if (configfile_content_raw) {

        function replaceConfigStr(keyName, value) {
            //  console.log("replaceConfigStr key : "+keyName);
            var start = configfile_content_raw.indexOf(keyName);
            //  console.log(" -- start : "+start);
            var end = configfile_content_raw.indexOf(";", start);
            //  console.log(" -- end : "+end);
            configfile_content_raw = configfile_content_raw.replace(configfile_content_raw.substring(start + keyName.length, end), " = \"" + value + "\"");
        }

        replaceConfigStr("APP_ID", wx_app_id);
        replaceConfigStr("PARTNER_ID", wx_partner_id);
        replaceConfigStr("KEY", wx_key)

        //console.log("wx config file : "+configfile_content_raw);

        FileUtil.save(configFilePath, configfile_content_raw);
        return true;
    } else {
        console.error("read Constants.java error.");
        return false;
    }
}

var currentGameName = "game";
function run(args, cb) {
    if (args.android_path) {
        setAndroidHome(args.android_path, cb);
    }
    const game_name_val = args.game_name;
    currentGameName = game_name_val;
    const package_name_val = args.package_name;
    const game_url_val = args.game_url;
    const icon_path_val = args.icon_path;
    const version_name_val = args.version_name;
    const version_code_val = args.version_code;
    const game_id_val = args.app_id;
    const channel_id_val = args.channel_id;
    const gamecenter_url_val = args.gamecenter_url;
    const loadingimage_path_val = args.loadingimage_path;

    //微信配置///////////////////////////////////////
    const wx_app_id = args.wx_app_id;
    const wx_partner_id = args.wx_partner_id;
    const wx_key = args.wx_key;
    ////////////////////////////////////////////////

    if (!game_name_val) {
        cb(true, '', 'game_name should not be empty');
        return;
    }
    if (!package_name_val) {
        cb(true, '', 'package_name should not be empty');
        return;
    }
    if (!game_url_val) {
        cb(true, '', 'game_url should not be empty');
        return;
    }
    if (!icon_path_val) {
        cb(true, '', 'icon_path should not be empty');
        return;
    }
    if (!version_name_val) {
        cb(true, '', 'version_name should not be empty');
        return;
    }
    if (!version_code_val) {
        cb(true, '', 'version_code should not be empty');
        return;
    }
    //以下参数可以为空
    //if(!game_id_val){
    //    cb(true,'','game_id should not be empty');
    //    return;
    //}
    //if(!gamecenter_url_val){
    //    cb(true,'','gamecenter_url should not be empty');
    //    return;
    //}

    if (pathChecked(cb) &&
        (loadingimage_path_val === '' || (loadingimage_path_val != '' && writeLoadingImage(loadingimage_path_val, cb))) &&
        writeStringResource(game_name_val, game_url_val, gamecenter_url_val, game_id_val, channel_id_val, cb) &&
        writeIconImage(icon_path_val, cb) &&
        writeGradlePackageName(package_name_val, version_name_val, version_code_val, cb) &&
        writeWXConfig(wx_app_id, wx_partner_id, wx_key)) {
        //执行语句
        if (
            process.platform === "darwin" ||
            process.platform === "linux"
        ) {
            const statement = "cd " + TEMPLATE_PATH + " && sudo ./gradlew assembleRelease";
            console.log("start exec package 1");
            exec(statement, cb);
        } else
            if (process.platform === "win32") {
                const statement = "cd " + TEMPLATE_PATH + " && gradlew assembleRelease";
                console.log("start exec package 2");
                exec(statement, cb);
            }
    }
}



exports.run = run;
function openOutputDir() {
    if (OUTPUT_PATH) {
        if (process.platform === "darwin") {
            exec('open ' + OUTPUT_PATH);
        } else
            if (process.platform === "win32") {
                exec('start ' + OUTPUT_PATH);
            }

    }
}
exports.openOutputDir = openOutputDir;

function getOutputFile() {
    return FileUtil.joinPath(OUTPUT_PATH, "app-release.apk");
}
exports.getOutputFile = getOutputFile;

function getOutputApkParthByName(apkName) {
    var file = FileUtil.joinPath(OUT_APK_FOLDER, apkName);
    if (FileUtil.exists(file)) {
        return file;
    } else {
        console.warn("getOutputApkByName file : \"" + file + "\" is not exists.");
    }
}
exports.getOutputApkParthByName = getOutputApkParthByName;

function clearOutApk(){
    FileUtil.remove(OUT_APK_FOLDER,true);
}

var currentZipFileName = "buildresult";

function zipOutputFile(fileName ,cb ) {
    var date = new Date();
    currentZipFileName = currentGameName+"-"+ date.getUTCFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+"-"+date.getTime();
    console.log("zipOutputFile filename : "+currentZipFileName);
    var zipCommand = "winrar a -k -m1 -ep1 -afzip -r -o+ " + OUT_BUILD_RESULT_FOLDER + "/"+currentZipFileName+".zip " +OUT_APK_FOLDER 
    exec(zipCommand, function (err, stdout, stderr) {
        clearOutApk();
        console.log("zip cb err:" + err + "; stdout:" + stdout + "; stderr:" + stderr);
        cb(currentZipFileName,err,stdout,stderr);
    })
}
exports.zipOutputFile = zipOutputFile;

function getOutputZipFile() {
    var zipFile = FileUtil.joinPath(OUT_BUILD_RESULT_FOLDER + "/"+currentZipFileName+".zip");
    if (FileUtil.exists(zipFile)) {
        return zipFile;
    }
    return null;
}
exports.getOutputZipFile = getOutputZipFile;

function moveReleaseApk(apkName) {
    var apkFile = getOutputFile();
    if (!FileUtil.isFile(apkFile)) {
        console.error("moveReleaseApk apkFile is null.");
        return;
    }
    var outApk = FileUtil.joinPath(OUT_APK_FOLDER, apkName);
    console.error("moveReleaseApk outApk : " + outApk);
    FileUtil.copy(apkFile, outApk);
}
exports.moveReleaseApk = moveReleaseApk;