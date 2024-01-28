/*
 * FileToPNG
 * Copyright (c) 2024  Justus Dietrich <git@justus-d.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
