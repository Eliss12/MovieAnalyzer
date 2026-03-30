package bg.sofia.uni.project.movie.server;

import bg.sofia.uni.project.movie.server.cache.CacheService;
import bg.sofia.uni.project.movie.server.command.*;
import bg.sofia.uni.project.movie.server.command.service.GenreBasedRecommendationService;
import bg.sofia.uni.project.movie.server.command.service.MovieService;
import bg.sofia.uni.project.movie.server.command.service.OMDbMovieService;
import bg.sofia.uni.project.movie.server.command.service.RecommendationService;
import bg.sofia.uni.project.movie.server.config.ServerConfig;
import bg.sofia.uni.project.movie.server.logger.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.logging.Level;

public class MovieServer {
    private final ServerConfig config;
    private final CommandProcessor processor;
    private final CacheService cacheService;
    private final ExecutorService workers;
    private final ConcurrentLinkedQueue<SelectionKey> readyToWrite;
    private volatile boolean running;
    private final ScheduledExecutorService cacheCleaner;

    public MovieServer(ServerConfig config, CommandProcessor processor, CacheService cacheService) {
        this.config = config;
        this.processor = processor;
        this.cacheService = cacheService;
        this.workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.readyToWrite = new ConcurrentLinkedQueue<>();
        this.running = true;
        this.cacheCleaner = Executors.newSingleThreadScheduledExecutor();
    }

    static void main() {
        try {
            ServerConfig config = new ServerConfig();
            LoggerUtil.setLogFile(config.getLogFile());

            CacheService cache = new CacheService(config.getCacheDir());
            RecommendationService recommendationService = new GenreBasedRecommendationService();
            MovieService movieService = new OMDbMovieService(
                    cache,
                    config.getApiKey(),
                    recommendationService,
                    config.getMaxRecommendations()
            );

            CommandProcessor processor = new CommandProcessor(movieService);
            MovieServer server = new MovieServer(config, processor, cache);

            server.start();

        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Failed to start server", e);
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            setupShutdownHook(selector);

            cacheCleaner.scheduleAtFixedRate(() -> {
                try {
                    // You need access to the CacheService instance.
                    // You can pass it to MovieServer constructor, or get it from somewhere else.
                    // For simplicity, modify constructor to accept CacheService.
                    cacheService.cleanExpiredEntries();
                } catch (Exception e) {
                    LoggerUtil.log(Level.WARNING, "Failed to clean expired cache entries", e);
                }
            }, 1, 1, TimeUnit.DAYS);

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(config.getPort()));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LoggerUtil.info("Movie Server started on port " + config.getPort());
            System.out.println("Movie Analyzer Server running on port " + config.getPort());

            while (running) {
                try {
                    selector.select();
                    processWriteQueue(selector);
                    processSelectedKeys(selector);
                } catch (IOException e) {
                    LoggerUtil.log(Level.SEVERE, "Selector error", e);
                }
            }

        } catch (IOException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to start server", e);
            System.err.println("Cannot start server: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void processWriteQueue(Selector selector) {
        SelectionKey writeKey;
        while ((writeKey = readyToWrite.poll()) != null) {
            if (writeKey.isValid()) {
                writeKey.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    private void processSelectedKeys(Selector selector) {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (!key.isValid()) {
                continue;
            }

            try {
                if (key.isAcceptable()) {
                    accept(selector, key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            } catch (IOException e) {
                LoggerUtil.log(Level.WARNING, "Error processing key", e);
                closeConnection(key);
            }
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();

        if (client == null) {
            return;
        }

        client.configureBlocking(false);
        ClientSession session = new ClientSession();
        client.register(selector, SelectionKey.OP_READ, session);

        LoggerUtil.info("Accepted connection from " + client.getRemoteAddress());
        System.out.println("Accepted connection from " + client.getRemoteAddress());
    }

    private void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        try {
            ByteBuffer buffer = session.getReadBuffer();
            buffer.clear();
            int bytes = channel.read(buffer);

            if (bytes == -1) {
                closeConnection(key);
                return;
            }

            buffer.flip();

            while (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                if (c == '\n') {
                    String command = session.getAndResetCommand();
                    if (!command.isEmpty()) {
                        workers.submit(() -> processCommand(key, command));
                    }
                } else {
                    session.appendToCommand(c);
                }
            }

        } catch (IOException e) {
            LoggerUtil.log(Level.WARNING, "Error reading from channel", e);
            closeConnection(key);
        }
    }

    private void processCommand(SelectionKey key, String command) {
        try {
            String response = processor.process(command);
            ClientSession session = (ClientSession) key.attachment();
            session.setResponse(response);
            readyToWrite.add(key);
            key.selector().wakeup();
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Error processing command: " + command, e);
        }
    }

    private void write(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        try {
            ByteBuffer buffer = session.getWriteBuffer();
            if (buffer == null || !buffer.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ);
                return;
            }

            channel.write(buffer);

            if (!buffer.hasRemaining()) {
                session.clearWriteBuffer();
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            LoggerUtil.log(Level.WARNING, "Error writing to channel", e);
            closeConnection(key);
        }
    }

    private void closeConnection(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel != null && channel.isOpen()) {
                LoggerUtil.info("Closing connection from " + channel.getRemoteAddress());
                channel.close();
            }
            key.cancel();
        } catch (IOException e) {
            LoggerUtil.log(Level.WARNING, "Error closing connection", e);
        }
    }

    private void setupShutdownHook(Selector selector) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            running = false;
            selector.wakeup();
        }));
    }

    private void shutdown() {
        System.out.println("Server stopping...");
        cacheCleaner.shutdown();
        try {
            if (!cacheCleaner.awaitTermination(5, TimeUnit.SECONDS)) {
                cacheCleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cacheCleaner.shutdownNow();
            Thread.currentThread().interrupt();
        }

        workers.shutdown();
        try {
            if (!workers.awaitTermination(10, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Server stopped.");
    }
}