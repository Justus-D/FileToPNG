package de.justusd.filetopng.service.filetopng;

import de.justusd.filetopng.service.filetopng.FileToPNG;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class FileToPNGTest {

    @Test
    void edgeSize() {
        assertEquals(20_000, FileToPNG.edgeSize(1_083_000_001));
    }

    @Test
    void contentLengthForPart() {
        assertEquals(1_200_000_000L, FileToPNG.contentLengthForPart(1_200_001_000L, 0));
        assertEquals(1_000L, FileToPNG.contentLengthForPart(1_200_001_000L, 1));
        assertEquals(0L, FileToPNG.contentLengthForPart(1_200_001_000L, 2));
        assertEquals(1_200_000_000L, FileToPNG.contentLengthForPart(1_200_001_000L * 2L + 1000L, 1));
        assertEquals(1_000L, FileToPNG.contentLengthForPart(1_200_000_000L * 2L + 1_000L, 2));
    }

}
