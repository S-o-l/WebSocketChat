/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

public class ChatWebSocketHandler extends WebSocketHandler {

    /**
     * Набор открытых сокетов
     */
    private final Set<ChatWebSocket> webSockets = new CopyOnWriteArraySet<ChatWebSocket>();

    /**
     * Выполняется когда пытается открыться новое соединение
     * @param request
     * @param protocol протокол (бывает двух видов ws и wss)
     * @return 
     */
    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request,
            String protocol) {

        // У нас есть два варианта
        // Либо мы не пускаем клиента и вызываем исключение
        //    throw new Exception();
        // Либо возвращаем объект, который будет соединять сервер с клиентом
        //   и обрабатывать запросы от клиента 
        return new ChatWebSocket();
    }

    private class ChatWebSocket implements WebSocket.OnTextMessage {

        /**
         * Хранилище соединения
         */
        private Connection connection;
        
        /**
         * Ник пользователя
         */
        private String userName = null;
        
        /**
         * Шаблон входящей команды авторизации
         */
        private final Pattern authCmdPattern = Pattern.compile("^\\/auth ([\\S]+).*");
        
        /**
         * Шаблон входящей команды получения списка пользователей
         */
        private final Pattern getUsersCmdPattern = Pattern.compile("^\\/getUsers.*");
        
        /**
         * Шаблон входящей команды получения помощи
         */
        private final Pattern helpCmdPattern = Pattern.compile("^\\/help.*");

        /**
         * Выполняется когда открыто новое соединение
         * @param connection 
         */
        @Override
        public void onOpen(Connection connection) {

            // Сохраняем соединение в свойство ChatWebSocket::connection
            this.connection = connection;

            // Добавляем себя в глобальный набор ChatWebSocketHandler::webSockets
            webSockets.add(this);
        }

        /**
         * Выполняется когда пришло новое сообщение
         * @param data 
         */
        @Override
        public void onMessage(String data) {
            
            // На всякий случай удаляем теговые дескрипторы
            data = data.replaceAll("<", "<").replaceAll(">", ">");
            
            // Если пришла команда авторизации
            if (authCmdPattern.matcher(data).matches()) {
                Matcher matcher = authCmdPattern.matcher(data);
                matcher.find();
                
                // Устанавливаем новый ник пользователю
                userName = matcher.group(1);
                System.out.println(data);
                try {
                    // Цикл шарит по набору сокетов ChatWebSocketHandler::webSockets
                    for (ChatWebSocket webSocket : webSockets) {

                        // и отправляет сообщение, что подключился новый пользователь
                        webSocket.connection.sendMessage("ati|"
                                + (webSocket.equals(this)
                                ? "Вы успешно авторизировались"
                                : ("В чат подключился " + userName)));
                    }
                } catch (IOException x) {

                    // Все ошибки будут приводить к разъединению клиента от сервера
                    connection.disconnect();
                }

            // Если пришла команда получения списка пользователей
            } else if (getUsersCmdPattern.matcher(data).matches()) {

                String userList = "";

                // Цикл шарит по набору сокетов ChatWebSocketHandler::webSockets
                for (ChatWebSocket webSocket : webSockets) {
                    userList += webSocket.userName + ", ";
                }
                userList = userList.substring(0, userList.length() - 2);

                try {

                    // Отсылаем список активных пользователей
                    connection.sendMessage("inf|Список активных пользователей: " + userList);

                } catch (IOException x) {

                    // Все ошибки будут приводить к разъединению клиента от сервера
                    connection.disconnect();
                }

            // Если пришла команда получения помощи
            } else if (helpCmdPattern.matcher(data).matches()) {
                
                String helpMessage = "Отправлять сообщения можно просто написав "
                        + "их в поле для ввода и нажать Enter.<br />"
                        + "Чат поддерживает три команды:<br />"
                        + "<ul><li><b>/help</b> - для распечатки этого сообщения</li>"
                        + "<li><b>/getUsers</b> - для получения списка пользователей</li>"
                        + "<li><b>/auth <i>ник</i></b> - для авторизации</li></ul>";

                try {
                    // Отсылаем инструкцию
                    connection.sendMessage("inf|" + helpMessage);

                } catch (IOException x) {

                    // Все ошибки будут приводить к разъединению клиента от сервера
                    connection.disconnect();
                }
                
            // Если пришла не команда а сообщение
            } else {


                try {

                    // Если пользователь не авторизирован
                    if (userName == null) {
                        connection.sendMessage("err|Вы не авторизированны<br />"
                                + "Используйте команду <b>/help</b> для помощи");
                        return;
                    }
                    // Цикл шарит по набору сокетов ChatWebSocketHandler::webSockets
                    for (ChatWebSocket webSocket : webSockets) {

                        // и каждому рассылает сообщение с флагом in для всех
                        // кроме автора, автору - флаг out
                        webSocket.connection.sendMessage((webSocket.equals(this) ? "out|" : ("in |" + userName + "|")) + data);
                    }
                } catch (IOException x) {

                    // Все ошибки будут приводить к разъединению клиента от сервера
                    connection.disconnect();
                }
            }

        }

        /**
         * Выполняется когда клиент разъединяется от сервера
         * @param closeCode
         * @param message 
         */
        @Override
        public void onClose(int closeCode, String message) {

                try {
                    // Цикл шарит по набору сокетов ChatWebSocketHandler::webSockets
                    for (ChatWebSocket webSocket : webSockets) {
                        // и отправляет сообщение, что пользователь отключился
                        webSocket.connection.sendMessage("ato|"
                                + (webSocket.equals(this)
                                ? "Вы покинули чат"
                                : ("Чат покинул " + userName)));
                    }
                } catch (IOException x) {
                    // Все ошибки будут приводить к разъединению клиента от сервера
                    connection.disconnect();
                }
            // Удаляем себя из глобального набора ChatWebSocketHandler::webSockets
            webSockets.remove(this);
        }
    }
}