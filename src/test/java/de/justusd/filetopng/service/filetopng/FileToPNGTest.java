package de.justusd.filetopng.service.filetopng;

import de.justusd.filetopng.service.filetopng.FileToPNG;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

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

    @Test
    void saveAndRestoreFile() throws IOException, NoSuchAlgorithmException, InterruptedException {
        final int BYTE_COUNT = 100_000;

        byte[] fileNameBytes = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(fileNameBytes);
        File tempDir = new File("target/tempTest_" + FileToPNG.byteToHexString(fileNameBytes) + "/");
        assertTrue(tempDir.mkdir());
        assertTrue(tempDir.isDirectory());

        // preparing
        assertTrue(tempDir.isDirectory());
        File originalFile = new File(tempDir, "original");
        byte[] originalBytes = new byte[BYTE_COUNT];
        SecureRandom.getInstanceStrong().nextBytes(originalBytes);
        FileOutputStream initialOutputStream = new FileOutputStream(originalFile);
        initialOutputStream.write(originalBytes);
        initialOutputStream.flush();
        initialOutputStream.close();

        // save
        FileToPNG save = new FileToPNG(originalFile, tempDir);
        save.saveSync();

        // restore
        File restoredFile = new File(tempDir, "restored");
        FileToPNG restore = new FileToPNG(restoredFile, tempDir);
        restore.restoreSync();
        FileInputStream restoredInputStream = new FileInputStream(restoredFile);
        byte[] restoredBytes = new byte[BYTE_COUNT];
        int bytesRestored = restoredInputStream.read(restoredBytes);
        assertEquals(BYTE_COUNT, bytesRestored);
        assertArrayEquals(originalBytes, restoredBytes);

        // cleanup
        originalFile.deleteOnExit();
        restoredFile.deleteOnExit();
        File[] tempDirFiles = tempDir.listFiles();
        if (tempDirFiles != null) {
            for (File file : tempDirFiles) {
                file.deleteOnExit();
            }
        }
        tempDir.deleteOnExit();
    }

}
