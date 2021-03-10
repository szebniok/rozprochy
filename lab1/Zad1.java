package lab1;

import java.io.IOException;
import java.net.*;

class Server {
    void listen() {
        try (DatagramSocket socket = new DatagramSocket(12345)) {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData()).trim();
                System.out.println("received: " + msg + " from " + packet.getAddress().getHostAddress());
                buffer = "PONG".getBytes();

                DatagramPacket reply = new DatagramPacket(buffer, buffer.length, packet.getSocketAddress());
                socket.send(reply);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client {
    void send() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName("localhost");
            byte[] buffer = "PING".getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 12345);
            socket.send(packet);

            socket.receive(packet);
            String msg = new String(packet.getData());
            System.out.println("received: " + msg + " from " + packet.getAddress().getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Zad1 {
    public static void main(String[] args) {
        if (args.length == 0) return;
        if (args[0].equals("server")) {
            Server server = new Server();
            server.listen();
        } else {
            Client client = new Client();
            client.send();
        }
    }
}
