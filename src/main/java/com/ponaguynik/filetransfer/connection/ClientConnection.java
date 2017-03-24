package com.ponaguynik.filetransfer.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Connection as a client.
 */
class ClientConnection extends Connection {

    ClientConnection(String ip, int port) {
        super(ip, port);
    }

    /**
     * Try to connect to the server with particular ip and port.
     * It blocks execution while connection is not established.
     * Can be interrupted with interrupt().
     */
    @Override
    public void connect() throws IOException {
        while (!interrupted) {
            try {
                socket = new Socket(ip, port);
                output = socket.getOutputStream();
                input = socket.getInputStream();
                break;
            } catch (IOException e) {
                if (!interrupted) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                } else
                    throw new IOException("Connection has been interrupted");
            }
        }
    }

    /**
     * Interrupt a connection attempt.
     * Connection has not to be established.
     */
    @Override
    public void interrupt() throws IOException {
        interrupted = true;
    }

    /**
     * Close input, output and socket.
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
            } catch (IOException ignore) {}
        }
    }
}
