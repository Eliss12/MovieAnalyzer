package bg.sofia.uni.project.movie.client;

import bg.sofia.uni.project.movie.common.ClientMovie;
import bg.sofia.uni.project.movie.common.ClientMovieJsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

public class ClientHandler {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Scanner scanner;

    private static final int POSTER_WIDTH = 400;
    private static final int POSTER_HEIGHT = 600;

    public ClientHandler(BufferedReader reader, PrintWriter writer, Scanner scanner) {
        this.reader = reader;
        this.writer = writer;
        this.scanner = scanner;
    }

    public void run() {
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

            if (command.length() > 1024) {
                System.out.println("Command too long. Maximum 1024 characters.");
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
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies(response);

        if (movies == null || movies.length == 0) {
            System.out.println("No recommendations found.");
            return;
        }

        System.out.println("\n=== Recommended Movies ===");
        for (int i = 0; i < movies.length; i++) {
            ClientMovie m = movies[i];
            if (m.title().contains("error") || m.title().contains("Failed")) {
                System.out.println("Error: " + m.title());
            } else {
                System.out.printf("%d. %s (Rating: %s)%n", i + 1, m.title(), m.rating());
            }
        }
        System.out.println();
    }

    private void showMovieDetails(String response) {
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie(response);

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

        if (movie.posterUrl() != null && !movie.posterUrl().equals("N/A") && !movie.posterUrl().isBlank()) {
            showPoster(movie);
        }
    }

    private void showPoster(ClientMovie movie) {
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
                    ImageIcon icon = new ImageIcon(new URL(movie.posterUrl()));
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