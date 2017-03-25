package com.ponaguynik.filetransfer.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Connection as a server.
 */
class ServerConnection extends Connection {

    private ServerSocket serverSocket;

    ServerConnection(String ip, int port) {
        super(ip, port);
    }

    /**
     * Establish server on a particular port.
     * Wait for client (with particular ip) connection.
     * It blocks execution while connection is not established.
     * Can be interrupted with interrupt().
     */
    @Override
    public void connect() throws IOException {
        serverSocket = new ServerSocket(port);

        while (!interrupted) {
            socket = serverSocket.accept();
            if (socket.getInetAddress().toString().endsWith(ip))
                break;
            else
                socket.close();
        }

        if (interrupted) {
            close();
            throw new IOException("Connection has been interrupted");
        }

        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    /**
     * Interrupt the connection attempt.
     * Connection has not to be established.
     */
    @Override
    public void interrupt() throws IOException {
        if (socket == null || socket.isClosed()) {
            interrupted = true;
            new Socket("127.0.0.1", port);
        } else
            throw new IOException("Connection already established");
    }

    /**
     * Close input, output, socket and
     * server socket.
     */
    @Override
    public void close() {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ignore) {
            }
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException ignore) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
        }
    }
}
