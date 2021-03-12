package zad1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Server {
    private List<ClientContext> contexts = Collections.synchronizedList(new ArrayList<>());

    public void listen() {
        new Thread(this::listenUDP).start();

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted new connection.");
                ClientContext newContext = new ClientContext(contexts, clientSocket);
                contexts.add(newContext);
                new Thread(newContext).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenUDP() {
        byte[] buffer = new byte[1024];

        try (DatagramSocket datagramSocket = new DatagramSocket(12345)) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                System.out.println("Received UDP message.");
                contexts.stream()
                        .filter(c -> c.socket.getPort() != packet.getPort())
                        .forEach(c -> c.sendUDP(datagramSocket, packet));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientContext implements Runnable {
    public Socket socket;
    List<ClientContext> contexts;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String nickname;

    public ClientContext(List<ClientContext> contexts, Socket socket) {
        this.contexts = contexts;
        this.socket = socket;
        try {
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            nickname = inputStream.readUTF();
            while (true) {
                String msg = inputStream.readUTF();
                contexts.stream()
                        .filter(c -> !c.nickname.equals(nickname))
                        .forEach(c -> c.send(nickname, msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String nickname, String message) {
        try {
            outputStream.writeUTF(nickname + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUDP(DatagramSocket datagramSocket, DatagramPacket packet) {
        byte[] buffer = Arrays.copyOf(packet.getData(), 1024);
        DatagramPacket newPacket = new DatagramPacket(buffer, buffer.length, socket.getRemoteSocketAddress());
        try {
            datagramSocket.send(newPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}