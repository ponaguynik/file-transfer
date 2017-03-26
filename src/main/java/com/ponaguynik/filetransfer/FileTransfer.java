package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.connection.ConnectionFactory;
import com.ponaguynik.filetransfer.connection.ConnectionType;
import com.ponaguynik.filetransfer.exception.ReportException;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * FileTransfer is a platform independent console application.
 * It is used for transferring files through local
 * network by ip and port. To transfer files you have to run
 * this app on two different devices, one of them as a sender
 * and the second as a receiver.
 */
public class FileTransfer {

    /**
     * The socket port that will be used for connection.
     * You can change it whatever you want.
     */
    private static final int SOCKET_PORT = 49200;

    private static Connection connection;

    /**
     * When you run app it checks args, runs stop thread
     * (listening for "stop" command), and executes the command
     * (send or receive).
     */
    public static void main(String... args) {
        String checkString = checkArgs(args);
        if (checkString != null) {
            System.out.println(checkString);
            System.exit(1);
        }

        StopThread stopThread = new StopThread();
        stopThread.start();

        String command = args[0];
        String ip = args[1];

        if (command.equals("send")) {
            if (!makeConnection(ip, ConnectionType.CLIENT)) {
                System.out.println("Couldn't establish connection with " + connection.getIp());
                System.exit(1);
            }
            String report = send(connection, extractFiles(args));
            System.out.println(report);
        } else {
            if (!makeConnection(ip, ConnectionType.SERVER)) {
                System.out.println("Couldn't establish connection with " + connection.getIp());
                System.exit(1);
            }
            File directory = new File(args[2]);
            String report = receive(connection, directory);
            System.out.println(report);
        }

        connection.close();

        System.exit(0);
    }

    /**
     * Check correctness of the passed arguments.
     * Check command, ip syntax, files or directory existence.
     *
     * @return error message if something wrong or
     * null if everything correct.
     */
    public static String checkArgs(String[] args) {
        if (args.length < 3)
            return usageMessage();

        String command = args[0];
        String ip = args[1];
        String[] paths = Arrays.copyOfRange(args, 2, args.length);

        if (command == null || !command.equals("send") && !command.equals("receive")) {
            return usageMessage();
        }

        if (!ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)" +
                "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
            return "Incorrect ip address";
        }

        if (command.equals("send")) {
            for (String path : paths) {
                File file = new File(path);

                if (file.isDirectory())
                    return "FileTransfer can send only files";

                if (!file.exists()) {
                    return "Couldn't find file:\n\t" + file.getAbsolutePath();
                }
            }
        } else if (command.equals("receive")) {
            File file = new File(paths[0]);

            if (file.isFile())
                return "Received files can be stored only in directory";

            if (!file.exists())
                return "Couldn't find directory:\n\t" + file.getAbsolutePath();
        }

        return null;
    }

    private static File[] extractFiles(String[] args) {
        String[] filePaths = Arrays.copyOfRange(args, 2, args.length);
        File[] files = new File[filePaths.length];

        for (int i = 0; i < filePaths.length; i++)
            files[i] = new File(filePaths[i]);

        return files;
    }

    /**
     * @return hint how to use this app.
     */
    private static String usageMessage() {
        return  "Usage is: java -jar file-transfer.jar [command] [ip] [path];\n\t[command]: 'send', 'receive';" +
                "\n\t[ip]: ip address of a receiver or a sender (0.0.0.0);" +
                "\n\t[path] (can send multiple files): absolute or relative path (../file.txt).";
    }

    private static boolean makeConnection(String ip, ConnectionType connectionType) {
        connection = ConnectionFactory.getConnection(connectionType, ip, SOCKET_PORT);
        try {
            assert connection != null;
            connection.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     *  Send files by connection.
     *
     * @return report message.
     */
    public static String send(Connection connection, File[] files) {
        Sender sender;
        try {
            sender = new Sender(connection, files.length);
        } catch (Exception e) {
            return "Failed to establish connection to " + connection.getIp();
        }

        for (File file : files) {
            String fileName = file.getName();

            try {
                sender.send(file);
                System.out.println(fileName + " has been successfully sent to " + connection.getIp());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            } catch (ReportException re) {
                System.out.println("Failed to send the file because of problem on the receiver side");
            } catch (Exception e) {
                System.out.println("Failed to send the file " + fileName + " to " + connection.getIp());
            }
        }

        return "Finished sending the files";
    }

    /**
     * Receive files by connection.
     *
     * @return report message.
     */
    public static String receive(Connection connection, File directory) {
        Receiver receiver;
        try {
            receiver = new Receiver(connection);
        } catch (IOException e) {
            return "Failed to establish connection to " + connection.getIp();
        }
        int fileCount = receiver.getFileCount();

        while (fileCount-- != 0) {
            try {
                String fileName = receiver.receive(directory);
                System.out.println(fileName + " has been stored to " + directory.getAbsolutePath());
            } catch (IOException e) {
                System.out.println(e.getMessage() + ": " + directory.getAbsolutePath());
            } catch (ReportException re) {
                System.out.println("Failed to receive the file because of problem on the sender side");
            } catch (Exception e) {
                System.out.println("Failed to receive a file");
            }
        }
        connection.close();

        return "Finished receiving the files";
    }

    /**
     * Listens console input for "stop" command.
     * Interrupts attempt of connection.
     */
    public static class StopThread extends Thread {

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.interrupted()) {
                if (scanner.nextLine().equalsIgnoreCase("stop")) {
                    try {
                        if (connection != null)
                            connection.interrupt();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

}