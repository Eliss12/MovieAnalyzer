package bg.sofia.uni.project.movie.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MovieClient {
    private static final int DEFAULT_PORT = 7777;
    private static final String DEFAULT_HOST = "localhost";

    private final String host;
    private final int port;

    public MovieClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + DEFAULT_PORT);
            }
        }
        new MovieClient(host, port).execute();
    }

    public void execute() {
        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(host, port));
            System.out.println("Connected to server at " + host + ":" + port);

            ClientHandler handler = new ClientHandler(reader, writer, scanner);
            handler.run();

        } catch (IOException e) {
            System.err.println("Cannot connect to server at " + host + ":" + port);
            System.err.println("Please make sure the server is running.");
        }
    }
}