package bg.sofia.uni.project.movie.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerConfig {
    private static final Logger logger = Logger.getLogger(ServerConfig.class.getName());
    private static final String DEFAULT_CONFIG = "server.properties";
    private static final String ENV_API_KEY = "OMDB_API_KEY";

    private final Properties properties = new Properties();

    private static final int DEFAULT_PORT = 7777;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final String DEFAULT_CACHE_DIR = "cache";
    private static final String DEFAULT_LOG_FILE = "server.log";

    public ServerConfig() {
        this(DEFAULT_CONFIG);
    }

    public ServerConfig(String configFile) {
        loadConfiguration(configFile);
    }

    private void loadConfiguration(String configFile) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from " + configFile);
            } else {
                logger.info("No configuration file found, using defaults");
            }
        } catch (IOException e) {
            logger.warning("Failed to load configuration: " + e.getMessage());
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
    }

    public int getBufferSize() {
        return Integer.parseInt(properties.getProperty("server.buffer.size", String.valueOf(DEFAULT_BUFFER_SIZE)));
    }

    public Path getCacheDir() {
        return Path.of(properties.getProperty("server.cache.dir", DEFAULT_CACHE_DIR));
    }

    public String getLogFile() {
        return properties.getProperty("server.log.file", DEFAULT_LOG_FILE);
    }

    public String getApiKey() {
        String apiKey = System.getenv(ENV_API_KEY);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }

        apiKey = properties.getProperty("omdb.api.key");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OMDB_API_KEY not set in environment or properties file");
        }

        return apiKey;
    }

    public int getMaxRecommendations() {
        return Integer.parseInt(properties.getProperty("recommendations.max", "5"));
    }

    public int getConnectionTimeoutSeconds() {
        return Integer.parseInt(properties.getProperty("server.connection.timeout", "10"));
    }
}
