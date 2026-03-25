package bg.sofia.uni.project.movie.client;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.common.MovieJsonParser;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MovieClient {
    private static final int DEFAULT_PORT = 7777;
    private static final String DEFAULT_HOST = "localhost";
    private static final int POSTER_WIDTH = 400;
    private static final int POSTER_HEIGHT = 600;
    private static final int MAX_COMMAND_LENGTH = 1024;

    private final String host;
    private final int port;

    public MovieClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Allow command line arguments for host and port
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

        MovieClient client = new MovieClient(host, port);
        client.execute();
    }

    public void execute() {
        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(
                     Channels.newReader(socketChannel, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(host, port));
            System.out.println("Connected to server at " + host + ":" + port);
            System.out.println("Available commands:");
            System.out.println("  get-movie <title>     - Get movie details by title");
            System.out.println("  get-movie-id <id>    - Get movie details by IMDb ID");
            System.out.println("  recommend <title>    - Get movie recommendations");
            System.out.println("  exit                 - Exit the client");
            System.out.println();

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String command = scanner.nextLine();

                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (command.isBlank()) {
                    continue;
                }

                if (command.length() > MAX_COMMAND_LENGTH) {
                    System.out.println("Command too long. Maximum " + MAX_COMMAND_LENGTH + " characters.");
                    continue;
                }

                writer.println(command);

                try {
                    String response = reader.readLine();
                    if (response == null) {
                        System.out.println("Server disconnected.");
                        break;
                    }

                    handleResponse(response);
                } catch (Exception e) {
                    System.err.println("Error processing server response: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Cannot connect to server at " + host + ":" + port);
            System.err.println("Please make sure the server is running.");
        }
    }

    private void handleResponse(String response) {
        if (response == null || response.isBlank()) {
            System.out.println("Empty response from server.");
            return;
        }

        try {
            if (response.startsWith("[")) {
                showRecommendedMovies(response);
            } else if (response.startsWith("{")) {
                showMovieDetails(response);
            } else {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.err.println("Error parsing server response: " + e.getMessage());
            System.out.println("Raw response: " + response);
        }
    }

    private void showRecommendedMovies(String response) {
        Movie[] movies = MovieJsonParser.parseMovies(response);

        if (movies == null || movies.length == 0) {
            System.out.println("No recommendations found.");
            return;
        }

        System.out.println("\n=== Recommended Movies ===");
        for (int i = 0; i < movies.length; i++) {
            Movie m = movies[i];
            if (m.title().contains("error") || m.title().contains("Failed")) {
                System.out.println("Error: " + m.title());
            } else {
                System.out.printf("%d. %s (Rating: %s)%n", i + 1, m.title(), m.rating());
            }
        }
        System.out.println();
    }

    private void showMovieDetails(String response) {
        Movie movie = MovieJsonParser.parseMovie(response);

        if (movie.title().contains("error") || movie.title().contains("Failed")) {
            System.out.println("Error: " + movie.title());
            return;
        }

        System.out.println("\n=== Movie Details ===");
        System.out.println("Title: " + movie.title());
        System.out.println("Year: " + movie.year());
        System.out.println("Director: " + movie.director());
        System.out.println("Rating: " + movie.rating());
        System.out.println("Genre: " + movie.genre());
        System.out.println();

        if (movie.poster() != null && !movie.poster().equals("N/A") && !movie.poster().isBlank()) {
            showPoster(movie);
        }
    }

    private void showPoster(Movie movie) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(movie.title() + " - Poster");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(POSTER_WIDTH, POSTER_HEIGHT);
            frame.setLocationRelativeTo(null);

            JLabel label = new JLabel("Loading poster...", SwingConstants.CENTER);
            frame.add(label);
            frame.setVisible(true);

            new Thread(() -> {
                try {
                    ImageIcon icon = new ImageIcon(new URL(movie.poster()));
                    SwingUtilities.invokeLater(() -> {
                        label.setText("");
                        label.setIcon(icon);
                        frame.pack();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        label.setText("Failed to load poster");
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                    });
                }
            }).start();
        });
    }
}