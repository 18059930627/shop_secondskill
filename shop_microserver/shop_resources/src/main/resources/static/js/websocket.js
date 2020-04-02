/**
 * 初始化变量
 */
var ws;
/**
 * 心跳时间
 * @type {number}
 */
var heartTime = 2000;
/**
 * 关闭连接时间
 * @type {number}
 */
var closeTime = 5000;

/**
 * 重连时间
 * @type {number}
 */
var reConnectTime = 5000;

//初始化WebSocket
function initWebSocket(callBack) {

    if (callBack.heartTime) {
        heartTime = callBack.heartTime;
    }

    if (callBack.closeTime) {
        closeTime = callBack.closeTime;
    }

    if (callBack.reConnectTime) {
        reConnectTime = callBack.reConnectTime;
    }

    //判断浏览器是否支持WebSocket
    if (window.WebSocket) {
        ws = new WebSocket(callBack.url)

        //设置回调函数
        ws.onopen = function () {
            console.log("服务器连接成功")

            //发送心跳
            sendHeart();

            //与心跳进行对比，主动关闭连接
            closeConnetion();

            //调用自定义open方法
            if (callBack.myopen) {
                callBack.myopen();
            }
        }

        ws.onclose = function () {

            console.log("连接关闭")

            //关闭心跳
            if (heartTimeOut) {
                clearTimeout(heartTimeOut);
            }

            //重新连接服务器
            setTimeout(function () {
                reconnection();
            }, reConnectTime)

            //调用自定义close方法
            if (callBack.myclose) {
                callBack.myclose();
            }

        }

        ws.onmessage = function (msg) {
            console.log("接收到从服务器发送的消息:" + msg.data)
            //服务器返回一个封装对象->json字符串
            var msgObj = JSON.parse(msg.data)

            if (msgObj.type == "2") {
                //服务器返回心跳信息
                if (closeTimeOut) {
                    //清除连接定时
                    clearTimeout(closeTimeOut)
                }
                //再重新关闭
                closeConnetion();
            } else {
                //处理其它信息
                if (callBack.mymessage) {
                    callBack.mymessage(msgObj);
                }
            }
        }

        ws.onerror = function () {
            console.log("连接出现异常")

            if (callBack.myerror) {
                callBack.myerror();
            }
        }

    } else {
        alert('对不起，您的浏览器太低级，请换个浏览器')
        return;
    }
}

/**
 * 客户端主动与服务器进行断开连接
 */
var closeTimeOut = null;

function closeConnetion() {
    closeTimeOut = setTimeout(function () {
        if (ws) {
            console.log("客户端主动关闭连接")
            ws.close();
        }
    }, closeTime)
}

/**
 * 重新连接服务器
 */
function reconnection() {
    //初始化连接服务器
    initWebSocket();
}

/**
 * 客户端发送心跳到服务器
 */
var heartTimeOut = null;

function sendHeart() {
    console.log("发送一次心跳")
    //轮询发送
    heartTimeOut = setInterval(function () {
        //构造心跳信息
        var heartMsg = {"type": 2};
        //发送消息
        sendObject(heartMsg);
    }, heartTime)
}

/**
 * 发送对象到服务器  对象->Json
 */
function sendObject(obj) {
    var msg = JSON.stringify(obj);
    sendMsg(msg);
}

/**
 * 发送json字符串到服务器
 */
function sendMsg(msg) {
    if (ws) {
        ws.send(msg);
    } else {
        alert('与服务器连接失败')
    }
}
