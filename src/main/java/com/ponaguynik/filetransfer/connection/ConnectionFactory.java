package com.ponaguynik.filetransfer.connection;

/**
 * Used Factory Pattern for connections
 */
public class ConnectionFactory {

    private ConnectionFactory() {

    }

    public static synchronized Connection getConnection(ConnectionType type, String ip, int port) {
        switch (type) {
            case SERVER:
                return new ServerConnection(ip, port);
            case CLIENT:
                return new ClientConnection(ip, port);
            default:
                return null;
        }
    }
}
