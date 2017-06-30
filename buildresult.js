/**
 * 管理构建结果
 * 
 * jkd2972
 */

'use strict';

var FileUtil = require("./utils/FileUtil");

var BUILD_RESULT_FOLDER = "";


function setBuildResultFolder(path){
    BUILD_RESULT_FOLDER = path;
}

/**
 * 获取构建结果列表。
 */
function getBuildResultList(){
    if(FileUtil.isDirectory(BUILD_RESULT_FOLDER)){
        return FileUtil.search(BUILD_RESULT_FOLDER,"zip");
    }
}

