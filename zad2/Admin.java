package zad2;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Admin {
    private Channel channel;

    Admin() throws IOException, TimeoutException {
        Connection connection = new ConnectionFactory().newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare("MOUNT", BuiltinExchangeType.TOPIC);
    }

    public void init() throws IOException {
        consume();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your message (prepend with {all,suppliers,tourists}: or type in quit to quit):");
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            String[] split = input.split(":");
            String type = split[0];
            String msg = split[1];
            String key;
            if (type.equalsIgnoreCase("tourists")) {
                key = "admin.tourist";
            } else if (type.equalsIgnoreCase("suppliers")) {
                key = "admin.supplier";
            } else {
                key = "admin.supplier.tourist";
            }

            channel.basicPublish("MOUNT", key, null, msg.getBytes());
        }
    }

    public void consume() throws IOException {
        channel.queueDeclare("ADMIN", false, false, false, null);
        channel.queueBind("ADMIN", "MOUNT", "#");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String key = envelope.getRoutingKey();
                String msg = new String(body);
                System.out.println("Received " + msg + " on " + key);
            }
        };
        channel.basicConsume("ADMIN", true, consumer);
    }
}
