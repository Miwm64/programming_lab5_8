package ru.spb.miwm64.moviemanager.server;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.Reader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;


public class UDPServer {
    private DatagramChannel datagramChannel;
    private CollectionManager collectionManager;
    private Selector selector;
    private boolean isRunning = true;
    private Reader reader;

    public UDPServer(int port, CollectionManager collectionManager) throws IOException {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.bind(new InetSocketAddress(port));
        this.datagramChannel.configureBlocking(false);

        this.selector = Selector.open();
        this.datagramChannel.register(selector, SelectionKey.OP_READ);
        this.reader = new NonBlockingConsoleReader();

        this.collectionManager = collectionManager;
    }

    public void run() {
        while (isRunning) {
            try {
                selector.select(100);

                // Handle UDP packets
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        System.out.println(key);
                    }
                }

                // Handle console input (non-blocking)
                String consoleInput = reader.readNextLine();
//                System.out.println(consoleInput);
                if (consoleInput != null && consoleInput.trim().equalsIgnoreCase("exit")) {
                    System.out.println("Shutting down...");
                    stop();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void stop() {
        isRunning = false;
        try {
            if (selector != null) selector.close();
            if (datagramChannel != null) datagramChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
    public void run() {}
    public void stop() {}
    private void handlePacket(){}
    class router{}
    private void sendSuccessResponse(){}
    private void sendErrorResponse(){}
 */
}