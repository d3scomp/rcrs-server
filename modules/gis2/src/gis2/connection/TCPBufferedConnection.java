package gis2.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import rescuecore2.messages.Message;

import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

public class TCPBufferedConnection {

    private static List<Message> messageList = new ArrayList<Message>();
    private static ConnectionListener connectionListener;
    private static Connection connection;

    public TCPBufferedConnection(Socket socket) throws IOException {
        connection = new TCPConnection(socket);
        connectionListener = new ConnectionListener() {
                public void messageReceived(Connection c, Message m) {
                    messageList.add(m);
                    synchronized (connectionListener) {
                        connectionListener.notifyAll();
                    }
                }
            };
        connection.addConnectionListener(connectionListener);
        connection.startup();
    }

    public void shutdown() {
        connection.shutdown();
    }

    public void sendMessage(Message m) throws ConnectionException {
        connection.sendMessage(m);
    }

    public Message receiveMessage() {
	synchronized (connectionListener) {
	    while (true) {
		if (messageList.size() != 0) {
                    break;
                }
		try {
		    connectionListener.wait();
		} catch (InterruptedException e) {
                    e.printStackTrace();
                }
	    }
	}
	return messageList.remove(0);
    }
}