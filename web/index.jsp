<%-- 
    Document   : index
    Created on : Jan 23, 2013, 10:09:07 PM
    Author     : TempRDP
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <link href="CSS/bootstrap.css" rel="stylesheet">
        <link href="CSS/bootstrap-responsive.css" rel="stylesheet">
        <script src="JS/jquery.js"></script>
        <script src="JS/bootstrap.js"></script>
        <script src="JS/javascript.js"></script>        
        <script>
            var socket;
            var chatUsers = {};
            var colorsNamesArray = new Array("DarkBlue","Blue","Green","DeepSkyBlue","Lime","DodgerBlue","ForestGreen","DarkSlateGrey","MediumSeaGreen","RoyalBlue","DarkSlateBlue","DarkOliveGreen","MediumAquaMarine","DimGrey","OliveDrab","LightSlateGrey","LawnGreen","Maroon","Purple","Olive","BlueViolet","DarkMagenta","YellowGreen","Sienna","Brown","GreenYellow","FireBrick","MediumOrchid","RosyBrown","DarkKhaki","Silver","MediumVioletRed","IndianRed ","Peru","Chocolate","Tan","Orchid","GoldenRod","Crimson","DarkSalmon","Khaki","SandyBrown","Salmon","Red","DeepPink","OrangeRed","Darkorange","Orange","Gold","Yellow");            
            function loadInnerContent() {
                $("#headerofpage").load("header.html", function() {
                    $("#homemenu").addClass("active");
                    chat();
                });
            }
            function chat() {
                socket = new WebSocket("ws://212.83.63.236:8081/chatdemo");
                socket.onopen = function () {
                    $("#serverStatus").html("Connected");
                    $("#serverStatus").removeClass("label-warning").removeClass("label-error").addClass("label-success");
                    loadChatWindow();
                };
                socket.onclose = function () {
                    $("#serverStatus").html("Closed");
                    $("#serverStatus").removeClass("label-warning").removeClass("label-success").addClass("label-important");
                    removeChatWindow();
                };
                socket.onmessage = function (event) {
                    var messageHeader = event.data.substring(0,4);
                    if (messageHeader == "inf|") $("#messagesPane").append("<em>" + event.data.substring(4) + "</em><br>");
                    else if (messageHeader == "ati|") {
                        chatUsers[event.data.substring(22)] = colorsNamesArray[parseInt((Math.random() * (49)), 10)];
                        console.log("sample color is: " + colorsNamesArray[10]);
                        console.log("chatUsers name is: " + event.data.substring(22) + " and the color is: " + chatUsers[event.data.substring(22)]);
                        $("#messagesPane").append(event.data.substring(4) + "<br>");
                    }
                    else if (messageHeader == "ato|") {
                        delete chatUsers[event.data.substring(16)];
                        $("#messagesPane").append(event.data.substring(4) + "<br>");
                    }
                    else if (messageHeader == "out|") $("#messagesPane").append(event.data.substring(4) + "<br>");
                    else if (messageHeader == "err|") $("#messagesPane").append(event.data.substring(4) + "<br>");
                    else if (messageHeader == "in |") {
                        var senderName = event.data.substring(4).substring(0, event.data.indexOf("|", 5) - 4);
                        if (chatUsers[senderName] == undefined) {
                            console.log("is undefined");
                            chatUsers[senderName] = colorsNamesArray[parseInt((Math.random() * (49)), 10)];
                        }
                        var color = chatUsers[senderName];
                        console.log("sender: " + senderName + " color: " + color);
                        $("#messagesPane").append("<span style=\"color: " + color + ";\">" + event.data.substring(4) + "</span><br>");
                    }
                    document.getElementById("messagesPane").scrollTop = document.getElementById("messagesPane").scrollHeight;
                };
            }
            function authorize() {
                socket.send("/auth " + $("#authorizeNick").val());
            }
            function sendMessage() {
                socket.send($("#messageInput").val());
                $("#messageInput").val("");
            }
            function loadChatWindow() {
                $("#centerColumn").load("chatwindow.html");
            }
            function removeChatWindow() {
                $("#centerColumn").html("");
            }
            function enterKeyCapture(e) {
                if (e.keyCode == 13) {
                    sendMessage();
                }
            }
        </script>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Websocket chat demo</title>
    </head>
    <body onload="loadInnerContent()">
    <div id="headerofpage">
    </div>

    <div class="row-fluid">
            <div class="span3" id="leftColumn">
                <label>Chat status:</label>
                <label class="label label-warning" id ="serverStatus">Connecting to server</label>
                <div id="authorizeDiv">
                    <label>Input nick to join:</label>
                    <input id="authorizeNick" placeholder="Nick">
                    <button class="btn btn-primary" onclick="authorize()">Join chat</button>
                </div>
            </div>
            <div class="span6" id="centerColumn">
            </div>
            <div class="span3" id="rightColumn">
                <div id="abouttext">
                    <p> About project: </p>
                    <p> This is a demo of simple Websocket chat made on basis of CSS framework Twitter Bootstrap, Javascript and JQuery for front-end and WebSockets with Jetty server for back-end</p>
                    <p> This project was done basing on course by Nikolay Tkachenko and his training center <a href="http://javanec.kiev.ua">Javanec.kiev.ua</a> that took place on November - December, 2012.
                </div>
            </div>
        </div>
    </body>
</html>
