package bg.sofia.uni.project.movie.server.cache;

import bg.sofia.uni.project.movie.exceptions.CacheException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheService {
    private final Path cacheDir;

    public CacheService(Path cacheDir) {
        this.cacheDir = cacheDir;
        initializeCacheDirectory();
    }

    private void initializeCacheDirectory() {
        try {
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDir, e);
        }
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public boolean exists(String file) {
        if (file == null || file.isBlank()) {
            return false;
        }
        return Files.exists(cacheDir.resolve(file));
    }

    public String read(String file) throws CacheException {
        if (file == null || file.isBlank()) {
            throw new CacheException("Invalid cache file name");
        }

        try {
            Path path = cacheDir.resolve(file);
            if (!Files.exists(path)) {
                throw new CacheException("Cache file not found: " + file);
            }
            return Files.readString(path);
        } catch (IOException e) {
            throw new CacheException("Failed to read cache file: " + file, e);
        }
    }

    public void save(String file, String data) throws CacheException {
        if (file == null || file.isBlank()) {
            throw new CacheException("Invalid cache file name");
        }

        if (data == null) {
            throw new CacheException("Cannot save null data to cache");
        }

        try {
            Path path = cacheDir.resolve(file);
            Files.createDirectories(path.getParent());
            Files.writeString(path, data);
        } catch (IOException e) {
            throw new CacheException("Failed to save cache file: " + file, e);
        }
    }

    public void clear() throws CacheException {
        try {
            if (Files.exists(cacheDir)) {
                try (var stream = Files.list(cacheDir)) {
                    stream.forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new CacheException("Failed to clear cache directory", e);
        }
    }

    public long size() throws CacheException {
        try {
            if (!Files.exists(cacheDir)) {
                return 0;
            }

            try (var stream = Files.list(cacheDir)) {
                return stream.count();
            }
        } catch (IOException e) {
            throw new CacheException("Failed to get cache size", e);
        }
    }
}