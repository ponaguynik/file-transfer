package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.connection.ConnectionFactory;
import com.ponaguynik.filetransfer.connection.ConnectionType;
import org.junit.Test;

public class ReceiverTest {

    //Uncomment this code to test receive()

    @Test
    public void receive() throws Exception {
        Connection connection = ConnectionFactory.getConnection(ConnectionType.CLIENT, "127.0.0.1", 58000);
        assert connection != null;
        connection.connect();
        Receiver receiver = new Receiver(connection);
        receiver.receive("src/test/resources/destination");
        receiver.receive("src/test/resources/destination");
        connection.close();
    }

}