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
  private static final Logger LOGGER = Logger.getLogger(BinaryWebSocketServer.class.getName());

  @OnOpen
  public void onOpen(Session session) {
    LOGGER.log(Level.INFO, "client connection opened. Session ID: "+session.getId());
    sessions.add(session);
  }

  @OnClose
  public void onClose(Session session) {
    LOGGER.log(Level.INFO, "client connection closed: " +session.getId());
    sessions.remove(session);
  }

  private int ctr = 0;
  @OnMessage(maxMessageSize = 1024 * 1024 * 10)
  public void onMessage(ByteBuffer byteBuffer) {
    ctr++;
    LOGGER.log(Level.INFO, "Byte data incoming: " + byteBuffer.array().length + " - ctr: " + ctr);
    for (Session session : sessions) {
      try {
        LOGGER.log(Level.INFO, "sending byte data to a client with sess-id: " + session.getId());
        
        //as we want to send the data to all clients connected make a deep copy first
        //sendBinary will whipe the buffer on sent success
        ByteBuffer cp = deepCopy(byteBuffer);
        
        session.getBasicRemote().sendBinary(cp);
      } catch (IOException ex) {
        Logger.getLogger(BinaryWebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  private ByteBuffer deepCopy(ByteBuffer original) {
    ByteBuffer clone = ByteBuffer.allocate(original.capacity());
    original.rewind();//copy from the beginning
    clone.put(original);
    original.rewind();
    clone.flip();
    return clone;
  }
}
