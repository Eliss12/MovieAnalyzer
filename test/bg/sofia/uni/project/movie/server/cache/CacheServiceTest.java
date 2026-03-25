package bg.sofia.uni.project.movie.server.cache;

import bg.sofia.uni.project.movie.exceptions.CacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CacheServiceTest {

    @TempDir
    Path tempDir;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(tempDir);
    }

    @Test
    void testSaveAndRead() throws CacheException {
        cacheService.save("test.json", "data");
        assertTrue(cacheService.exists("test.json"));
        String content = cacheService.read("test.json");
        assertEquals("data", content);
    }

    @Test
    void testReadWithNonExistent() {

        assertThrows(CacheException.class, () -> cacheService.read("missing.json"));
    }

    @Test
    void testReadWithFileNameNull() {

        assertThrows(CacheException.class, () -> cacheService.read(null));
    }

    @Test
    void testSaveWithNullFileName() {

        assertThrows(CacheException.class, () -> cacheService.save(null, "data"));
    }

    @Test
    void testSaveWithNullData() {
        assertThrows(CacheException.class, () -> cacheService.save("file.json", null));
    }

    @Test
    void testSizeWithOneFile() throws CacheException {
        cacheService.save("test.json", "data");
        assertTrue(cacheService.exists("test.json"));
        long size = cacheService.size();
        assertEquals(1, size);
    }

    @Test
    void testSizeWithZeroFiles() throws CacheException {
        long size = cacheService.size();
        assertEquals(0, size);
    }
}