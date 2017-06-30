/**
 * Created by jackyanjiaqi on 17/1/10.
 */
const packager = require('./packager');
const PATH = require("path");

const express = require('express');
const multiparty = require('multiparty');
const app = express();
const server = require('http').createServer(app);
const socketIO = require('socket.io')(server);
const port = 3456;


var generatedApkArray = [];

app.get('/download', function (req, res, next) {
    //..db get file realpath
    res.download(packager.getOutputFile(), "download.apk");
});
app.get('/downloadAllApk', function (req, res, next) {
    //..db get file 
    console.log("server downloadAllApk ==== file number : " + generatedApkArray.length);
    // var apkFileParth = null;
    // var apkLen = generatedApkArray.length;

    // for (var i = 0; i < generatedApkArray.length; i++) {
    //     apkFileParth = packager.getOutputApkParthByName(generatedApkArray[i]);
    //     console.log("server downloadAllApk , download file: " + apkFileParth);
    //     res.download(apkFileParth, function (e) {
    //         if (e) {
    //             console.log("download file: " + apkFileParth + ". error: " + e);
    //         }
    //     });
    // }
    var zipFile = packager.getOutputZipFile();
    if(zipFile && zipFile.length > 0 ){
        res.download(zipFile,function (e) {
            if (e) {
                console.log("download zip file: " + zipFile + ". error: " + e);
            }
        });
    }

});
app.use("/client", express.static(PATH.resolve(__dirname, "client")));
app.get("*", (req, res) => {
    res.redirect("/client");
});

server.listen(port, () => {
    console.log('Game start at port %d', port);
});


var channelBuffer = [];


socketIO.on("connection", (socket) => {
    console.log(`${socket.id} connect!`);
    underConnection(socket);
    socket.on('pathCheck', () => {
        console.log(`${socket.id} pathCheck!`);
        packager.pathChecked((err, message, errmsg) => {
            if (err) {
                socket.emit("console.log", errmsg);
            } else {
                socket.emit("console.log", message);
            }
            // if(err && errmsg ==="Need ANDROID_HOME!"){
            //     socket.emit("console.log",errmsg);
            // }
        });
    });
    socket.on('init-send', (arg) => {
        socket.emit('console.log', arg + ' named Jack!');
    });
    socket.on('start', function (args) {
        // jumpTo('http://123.57.70.115/beta/egretpptdemo');
        //jumpTo('file://' + __dirname + '/editor/index.html',function(){
        //	event.sender.send('path-init',args);
        //});
        console.log(`${socket.id} start pack with args:${args}`);
        packager.run(args, function (err, stdout, stderr) {
            if (err) {
                socket.emit('console.log', stderr);
            } else {
                if (stdout.indexOf("BUILD SUCCESSFUL") != -1) {
                    stdout += "<a href='/download'>点此下载</a>";
                    packager.moveReleaseApk("game_package");
                }
                socket.emit('console.log', stdout);
            }
        });
    });

    ///////////////////////////////////////////////////////////////////////////////////////////////
    socket.on('addChannelConfig', function (channelConfig, total, cb) {

        channelBuffer.push(channelConfig);
        console.log("addChannelConfig " + channelBuffer.length + "/" + total);
        if (channelBuffer.length == total) {
            cb(true);
        }
    });
    socket.on('startWithChannelBuffer', function (args) {
        console.log(`startWithChannelBuffer : ${socket.id} start pack with map`);

        var channelList = channelBuffer;
        channelBuffer = [];

        var runCounter = 0;
        var successCounter = 0;

        // var channelMap = args.channelMap;
        if (!channelList || channelList.length < 1) {
            console.warn("startWithChannelBuffer channelMap is null.")
            return;
        }

        var channelLen = channelList.length;
        var channelIndex = 0;
        console.log("startWithChannelBuffer channelLen = " + channelLen + "; channelIndex=" + channelIndex);
        var createRunArgs = function () {
            var channelConfig = channelList[channelIndex];
            return {
                game_name: args.game_name,
                package_name: channelConfig.package_name,
                game_url: channelConfig.game_url,
                app_id:channelConfig.app_id,
                channel_id:channelConfig.channel_id,
                icon_path: channelConfig.icon_path,
                version_name: args.version_name,
                version_code: args.version_code,
                game_id: args.game_id,
                loadingimage_path: channelConfig.loadingimage_path,
                gamecenter_url: args.gamecenter_url,
                android_path: args.android_path,
                wx_app_id: channelConfig.wx_app_id,
                wx_partner_id: channelConfig.wx_partner_id,
                wx_key:channelConfig.wx_key
            }
        }

        var runArgs = createRunArgs();

        var runCB = function (err, stdout, stderr) {
            var checkResult = false;
            var apkName = runArgs.game_name + "_" + runArgs.package_name + ".apk";
            if (err) {
                runCounter++;
                checkResult = true;

                socket.emit('console.log', stderr);
            } else {
                if (stdout.indexOf("BUILD SUCCESSFUL") != -1) {
                    runCounter++;
                    checkResult = true;

                    channelIndex++;

                    packager.moveReleaseApk(apkName);
                    generatedApkArray.push(apkName);
                    console.log("build sucess file:" + apkName + ",counter = " + generatedApkArray.length);
                    if (channelIndex < channelLen) {
                        setTimeout(function () {
                            runArgs = createRunArgs();
                            packager.run(runArgs, runCB);
                        }, 0)
                    } else {
                        stdout += "<br><br> 开始压缩打包当前构建的所有微端apk ……";
                        packager.zipOutputFile(apkName , function(zipFileName,err, stdout, stderr){
                            console.log("zipOutputFile ---------");
                            if(err){
                                socket.emit('console.log', "zip apks err: "+err);
                            }else{
                                 socket.emit('console.log', "<br> <a href='/downloadAllApk'>点此下载微端包："+zipFileName+".zip</a>");
                            }
                        });
                        
                    }
                }
                socket.emit('console.log', stdout);
            }

            // if (checkResult && runCounter > channelLen - 1) {
            //    socket.emit('console.log', "run result = " + channelIndex + "/" + channelLen);
            //    stdout += "<br> <a href='/downloadAllApk'>点此下载</a>";
            // }
        };

        console.log("packager run ");
        packager.run(runArgs, runCB);

    });
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    socket.on("disconnect", () => {
        console.log(`${socket.id} disconnect!`);
    });
    socket.on('reconnect', () => {
        console.log(`${socket.id} reconnect!`);
    });
});

function underConnection(client) {
    app.post('/upload', (req, res) => {
        let form = new multiparty.Form({ uploadDir: './temp' });
        form.on('error', err => {
            let errMsg = `Error parsing form:${err.stack}`;
            console.log(errMsg);
            res.send(errMsg);
            // socket.emit('console.log',errMsg);
        });
        form.parse(req, (err, fields, files) => {
            let filesTmp = JSON.stringify(files, null, 2);
            if (err) {
                console.log(`parse:error:${err}`);
                res.send("写文件操作失败\n" + err);
            } else {
                //console.log('parse files: ' + filesTmp);

                var fileNameArr = Object.keys(files);
                var firstFilename = fileNameArr[0];
                var fileDataArr = files[firstFilename];
                var fileData = fileDataArr[0];
                var uploadedPath = fileData.path;
                var dstPath = './temp' + fileData.originalFilename;
                console.log("uploadedPath " + uploadedPath);
                res.send(uploadedPath);
            }
        })
    });
}