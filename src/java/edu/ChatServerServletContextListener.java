package edu;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class ChatServerServletContextListener implements ServletContextListener {
    
    /**
     * Хранилище сервера Jetty
     */
    private Server server = null;
    
    /**
     * Метод вызывается когда контейнер сервлетов запускается
     * @param event 
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        try {
            // Создание сервера Jetty на 8081 порту
            this.server = new Server(8081);
            
            // Регистрируем ChatWebSocketHandler в сервере Jetty
            ChatWebSocketHandler chatWebSocketHandler = new ChatWebSocketHandler();
            // Это вариант хэндлера для WebSocketHandlerContainer
            chatWebSocketHandler.setHandler(new DefaultHandler());
            
            // Вставляем наш хэндлер слушаться jetty
            server.setHandler(chatWebSocketHandler);
            
            // Запускаем Jetty
            server.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод вызывается когда контейнер сервлетов останавливается
     * @param event 
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {

        // Если сервер jetty когда-нибудь запустился
        if (server != null) {
            try {
                // останавливаем Jetty
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}