package bg.sofia.uni.project.movie.server.cache;

import bg.sofia.uni.project.movie.exceptions.CacheException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class CacheService {
    private final Path cacheDir;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
        lock.readLock().lock();
        if (file == null || file.isBlank()) {
            return false;
        }

        try {
            return Files.exists(cacheDir.resolve(file));
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public String read(String file) throws CacheException {
        lock.readLock().lock();

        if (file == null || file.isBlank()) {
            throw new CacheException("Invalid cache file name");
        }

        try {
            Path path = cacheDir.resolve(file);
            if (!Files.exists(path)) {
                throw new CacheException("Cache file not found: " + file);
            }
            String content = Files.readString(path);

            // If the first line is a timestamp (all digits), remove it
            int newlineIdx = content.indexOf('\n');
            if (newlineIdx > 0 && content.substring(0, newlineIdx).matches("\\d+")) {
                return content.substring(newlineIdx + 1);
            }
            return content;
        } catch (IOException e) {
            throw new CacheException("Failed to read cache file: " + file, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void save(String file, String data) throws CacheException {
        lock.writeLock().lock();
        if (file == null || file.isBlank()) {
            throw new CacheException("Invalid cache file name");
        }

        if (data == null) {
            throw new CacheException("Cannot save null data to cache");
        }

        try {
            Path path = cacheDir.resolve(file);
            Files.createDirectories(path.getParent());
            String meta = System.currentTimeMillis() + "\n" + data;
            Files.writeString(path, meta);
        } catch (IOException e) {
            throw new CacheException("Failed to save cache file: " + file, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void cleanExpiredEntries() throws CacheException {
        lock.writeLock().lock();
        try {
            try (Stream<Path> paths = Files.list(cacheDir)) {
                paths.filter(p -> p.toString().endsWith(".json"))
                        .filter(p -> {
                            try {
                                return isExpired(p);
                            } catch (IOException e) {
                                return false;
                            }
                        })
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to clean expired cache file: " + p, e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new CacheException("Failed to clean expired cache entries", e);
        } finally {
            lock.writeLock().unlock();
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

    private boolean isExpired(Path path) throws IOException {
        long lastModified = Files.getLastModifiedTime(path).toMillis();
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        return System.currentTimeMillis() - lastModified > thirtyDaysInMillis;
    }
}