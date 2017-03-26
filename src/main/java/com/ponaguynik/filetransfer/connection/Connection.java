package com.ponaguynik.filetransfer.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Connection by ip and port
 */
public abstract class Connection {

    protected String ip;
    protected int port;

    protected Socket socket;

    protected boolean interrupted;

    protected InputStream input;
    protected OutputStream output;

    public Connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
        interrupted = false;
    }

    /**
     * Try to make connection. It blocks execution while
     * connection is not established.
     * Can be interrupted with interrupt().
     */
    public abstract void connect() throws IOException;

    public abstract void close();

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    /**
     * Interrupt the connection attempt.
     * Connection has not to be established.
     */
    public abstract void interrupt() throws IOException;

    public InputStream getInputStream() {
        return input;
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public String getIp() {
        return ip;
    }

}