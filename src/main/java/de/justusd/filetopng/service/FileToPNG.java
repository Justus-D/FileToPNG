package de.justusd.filetopng.service;

import ar.com.hjg.pngj.*;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FileToPNG {

    private File file;
    private File directory;
    private boolean saveInProgress = false;
    private boolean restoreInProgress = false;
    private AtomicLong bytesProcessed;
    private long fileSize;
    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();
    IOException occuredException = null;
    private Thread thread;

    /**
     * Either save a file to one or more PNGs or restore a file from one or more PNGs.
     * Use the save() or restore() methods to save or restore.
     * Calling one method multiple times will cause an IOException.
     * @param file File to save from or restore to (must be a file)
     * @param directory Directory for the PNGs (must be a directory)
     */
    public FileToPNG(@NotNull File file, @NotNull File directory) throws IOException {
        if (!file.isFile()) {
            throw new IOException("The 'File file' argument is not a file!");
        }
        if (!directory.isDirectory()) {
            throw new IOException("The 'File directory' argument is not a directory!");
        }
        this.file = file;
        this.directory = directory;
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        this.propertyChangeListeners.add(propertyChangeListener);
    }

    private synchronized void processedNBytes(int n) {
        PropertyChangeEvent event = new PropertyChangeEvent(
                this,
                "bytesProcessed",
                this.bytesProcessed.get(),
                this.bytesProcessed.addAndGet(n)
        );
        for (PropertyChangeListener listener : this.propertyChangeListeners) {
            listener.propertyChange(event);
        }
    }

    private synchronized void sendFinished() {
        PropertyChangeEvent event = new PropertyChangeEvent(
                this,
                "finished",
                false,
                true
        );
        for (PropertyChangeListener listener : this.propertyChangeListeners) {
            listener.propertyChange(event);
        }
    }

    public static void main(String[] args) {
        File inputFile = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\agpl-3.0.md");
        File outputDirectory = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\out\\");

        try {
            FileToPNG fileToPNG = new FileToPNG(inputFile, outputDirectory);
            fileToPNG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int edgeSize(long fileSize) {
        if (fileSize <= 3_000_000) return 1_000;
        if (fileSize <= 12_000_000) return 2_000;
        if (fileSize <= 27_000_000) return 3_000;
        if (fileSize <= 48_000_000) return 4_000;
        if (fileSize <= 75_000_000) return 5_000;
        if (fileSize <= 108_000_000) return 6_000;
        if (fileSize <= 147_000_000) return 7_000;
        if (fileSize <= 192_000_000) return 8_000;
        if (fileSize <= 243_000_000) return 9_000;
        if (fileSize <= 300_000_000) return 10_000;
        if (fileSize <= 363_000_000) return 11_000;
        if (fileSize <= 432_000_000) return 12_000;
        if (fileSize <= 507_000_000) return 13_000;
        if (fileSize <= 588_000_000) return 14_000;
        if (fileSize <= 675_000_000) return 15_000;
        if (fileSize <= 768_000_000) return 16_000;
        if (fileSize <= 867_000_000) return 17_000;
        if (fileSize <= 972_000_000) return 18_000;
        if (fileSize <= 1_083_000_000) return 19_000;
        return 20_000;
    }

    private static int maxContentBytes(int edgeSize) {
        return edgeSize * edgeSize * 3;
    }

    /**
     *
     * @param inputFile The file to be saved
     * @param outputDirectory The directory for the PNG files
     * @return An AtomicLong containing the bytes processed
     */
    public AtomicLong saveAsyncWithProgress(File inputFile, File outputDirectory) {
        AtomicLong bytesProcessed = new AtomicLong(0L);
        Runnable saveRunnable = new Runnable() {

            @Override
            public void run() {
//                try {
//                    save(inputFile, outputDirectory, bytesProcessed);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        };
        Thread thread = new Thread(saveRunnable);
        thread.setDaemon(true);
        thread.start();
        return bytesProcessed;
    }

    /**
     * If an IOException occurred during the execution of a thread, you can throw the occurred exception again.
     * @throws IOException The IOException that may have occurred in a thread
     */
    public void throwException() throws IOException {
        if (this.occuredException != null) throw this.occuredException;
    }

    public float progressPercentage() {
        if (this.saveInProgress || this.restoreInProgress) {
            return (float) this.getBytesProcessed() / this.getBytesTotal();
        }
        return 0.0f;
    }

    public long getBytesProcessed() {
        return this.bytesProcessed.get();
    }

    public long getBytesTotal() {
        return this.fileSize;
    }

    public synchronized void save() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        Runnable runnable = () -> {
            try {
                FileToPNG.this.saveSync();
            } catch (IOException e) {
                FileToPNG.this.occuredException = e;
            }
        };
        if (this.thread != null) return;
        Thread thread = new Thread(runnable);
        this.thread = thread;
        thread.setDaemon(true);
        thread.start();
    }

    public void saveSync() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        this.saveInProgress = true;
        FileInputStream inputStream = new FileInputStream(file);
        this.fileSize = file.length(); // in Bytes
        this.bytesProcessed = new AtomicLong(0L);

        // int edge = edgeSize(fileSize);
        long fileSizeSqrt = (long) Math.sqrt((double) fileSize / 3) + 1000;

        List<File> outputFiles = new ArrayList<>();

        int paddingTop = 96;
        int paddingBottom = 32;

        long remainingBytes = this.fileSize;
        int partNo = 0;
        while (remainingBytes > 0) {
            File partFile = new File(this.directory.getAbsolutePath() + "\\" + "part" + partNo + ".png");
            outputFiles.add(partFile);
            partNo++;
            int edge = edgeSize(remainingBytes);
            remainingBytes -= maxContentBytes(edge);
            ImageInfo imgI = new ImageInfo(edge, edge + paddingTop + paddingBottom, 8, false);
            PngWriter pngW = new PngWriter(partFile, imgI, true);

            // Metadata at the top, previous checksum or digest
            for (int row = 0; row < paddingTop; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                Arrays.fill(bytes, (byte) 0b11111111);
                pngW.writeRow(imgL, row);
            }

            // Data bytes
            for (int row = paddingTop; row < edge + paddingTop; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                int bytesRead = inputStream.read(bytes);
                this.processedNBytes(bytesRead);
                pngW.writeRow(imgL, row);
                if (bytesRead == 0) break;
            }

            // Checksum or digest
            for (int row = paddingTop + edge; row < paddingTop + edge + paddingBottom; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                Arrays.fill(bytes, (byte) 0b11111111);
                pngW.writeRow(imgL, row);
            }

            pngW.close();

        }

//        File outputFile = new File(this.directory.getAbsolutePath() + "\\" + "part1.png");
//        ImageInfo imgI = new ImageInfo(128, 128, 8, false);
//        PngWriter pngW = new PngWriter(outputFile, imgI, true);
//        ImageLineInt imgL = new ImageLineInt(imgI);
//        int[] line = imgL.getScanline();
//        for (int i = 0; i < 128; i++) {
//            line[(i*3)] = i*2;   // R
//            line[(i*3)+1] = i*2; // G
//            line[(i*3)+2] = i*2; // B
//        }
//        for (int i = 0; i < 128; i++) {
//            pngW.writeRow(imgL, i);
//        }

        inputStream.close();
        this.sendFinished();
    }

    public void restore() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        this.restoreInProgress = true;
    }

}
