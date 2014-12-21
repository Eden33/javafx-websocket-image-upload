/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eden.javafx.test.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author edi
 */
@ServerEndpoint("/images")
public class BinaryWebSocketServer {

    
  private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

  @OnOpen
  public void onOpen(Session session) {
    sessions.add(session);
  }

  @OnClose
  public void onClose(Session session) {
    sessions.remove(session);
  }

  @OnMessage
  public void onMessage(ByteBuffer byteBuffer) {
    for (Session session : sessions) {
      try {
        session.getBasicRemote().sendBinary(byteBuffer);
      } catch (IOException ex) {
        Logger.getLogger(BinaryWebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
