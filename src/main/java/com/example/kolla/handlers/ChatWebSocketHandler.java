package com.example.kolla.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    // Danh sách các sessions (users đang kết nối)
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Thêm session mới vào danh sách
        sessions.add(session);
        log.info("Người dùng mới kết nối: " + session.getId());
        log.info("Session details: " + session.getUri() + ", Protocol: " + session.getAcceptedProtocol());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String receivedMessage = message.getPayload();
        log.info("Nhận tin nhắn từ " + session.getId() + ": " + receivedMessage);
//        var x = 1;
        // Gửi tin nhắn đến tất cả users khác
        TextMessage broadcastMessage = new TextMessage(session.getId() + ": " + receivedMessage);
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !session.equals(webSocketSession)) {
                webSocketSession.sendMessage(broadcastMessage);
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        // Xóa session khi user ngắt kết nối
        sessions.remove(session);
        log.info("Người dùng đã ngắt kết nối: " + session.getId());
    }
}