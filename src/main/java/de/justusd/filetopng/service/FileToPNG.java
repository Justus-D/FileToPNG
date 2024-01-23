package de.justusd.filetopng.service;

import ar.com.hjg.pngj.*;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public void joinThread() throws InterruptedException {
        if (this.thread != null) this.thread.join();
    }

    public static void main(String[] args) {
        File inputFile = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\agpl-3.0.md");
        File outputDirectory = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\out\\");

        try {
            FileToPNG fileToPNG = new FileToPNG(inputFile, outputDirectory);
            fileToPNG.save();
            fileToPNG.joinThread();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int edgeSize(long fileSize) {
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

    public static long contentLengthForPart(long fileSize, int partIndex) {
        long remainingSize = fileSize;
        int part = 0;
        while (remainingSize > 0) {
            int edgeSize = edgeSize(remainingSize);
            long partLength = Math.min(remainingSize, (long) edgeSize * edgeSize * 3L);
            remainingSize -= partLength;
            if (part == partIndex) return partLength;
            part++;
        }
        return 0L;
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

    @NotNull
    private MessageDigest getMdInstance() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest;
        } catch (NoSuchAlgorithmException ignored) {} // this will never happen
        return this.getMdInstance(); // yeah
    }

    private ImageLineByte whiteLine(ImageInfo imgI) {
        ImageLineByte imgL = new ImageLineByte(imgI);
        byte[] bytes = imgL.getScanline();
        Arrays.fill(bytes, (byte) 0b11111111);
        return imgL;
    }

    private ImageLineByte utf8TextLine(@NotNull ImageInfo imgI, @NotNull String... text) {
        int totalLength = 0;
        for (String s : text) totalLength += s.length();
        StringBuilder sb = new StringBuilder(totalLength);
        for (String s : text) {
            sb.append(s);
        }
        return utf8TextLine(imgI, sb.toString());
    }

    private ImageLineByte utf8TextLine(@NotNull ImageInfo imgI, @NotNull String text) {
        return this.byteArrayLine(imgI, text.getBytes(StandardCharsets.UTF_8));
    }

    private ImageLineByte byteArrayLine(@NotNull ImageInfo imgI, @NotNull byte[] dataBytes) {
        return this.byteArrayLine(imgI, dataBytes, (byte) 0xff);
    }

    private ImageLineByte byteArrayLine(@NotNull ImageInfo imgI, @NotNull byte[] dataBytes, byte fillWith) {
        ImageLineByte imgL = new ImageLineByte(imgI);
        byte[] bytes = imgL.getScanline();
        System.arraycopy(dataBytes, 0, bytes, 0, dataBytes.length);
        Arrays.fill(bytes, dataBytes.length, bytes.length - 1, (byte) 0xff);
        return imgL;
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
        List<MessageDigest> messageDigests = new ArrayList<>();
        MessageDigest digestAll = this.getMdInstance();

        int paddingTop = 96;
        int paddingBottom = 32;

        long remainingBytes = this.fileSize;
        int partNo = 0;
        while (remainingBytes > 0) {
            File partFile = new File(this.directory.getAbsolutePath() + "\\" + "part" + partNo + ".png");
            outputFiles.add(partFile);
            MessageDigest digestPart = this.getMdInstance();
            messageDigests.add(digestPart);
            int edge = edgeSize(remainingBytes);
            remainingBytes -= maxContentBytes(edge);
            ImageInfo imgI = new ImageInfo(edge, edge + paddingTop + paddingBottom, 8, false);
            PngWriter pngW = new PngWriter(partFile, imgI, true);

            // Header: metadata, previous checksum or digest
            int headerRow = 0;
            pngW.writeRow(utf8TextLine(imgI, "de.justusd.filetopng\0"), headerRow++); // header[0] // magic bytes
            pngW.writeRow(utf8TextLine(imgI,
                    "version=1;",
                    "part=" + partNo + ";",
                    "contentLength=" + ";",
                    "edge=" + edge + ";",
                    "paddingTop=" + paddingTop + ";",
                    "paddingBottom=" + paddingBottom + ";",
                    "digestAlgorithm=SHA-256;",
                    "digestLength=32;", // SHA-256: 256 bits = 32 bytes
                    "previousDigestIndex=2;",
                    "fileNameIndex=3;",
                    "\0"
            ), headerRow++); // header[1] // header data

            MessageDigest previousDigest = null;
            int prevPartIndex = partNo - 1;
            if (!(prevPartIndex < 0 || prevPartIndex >= messageDigests.size())) {
                previousDigest = messageDigests.get(prevPartIndex);
            }
            if (previousDigest != null) {
                pngW.writeRow(byteArrayLine(imgI, previousDigest.digest()), headerRow++); // header[2] // digest of previous part
            } else {
                pngW.writeRow(whiteLine(imgI), headerRow++); // header[2] // white if no digest
            }

            pngW.writeRow(utf8TextLine(imgI, file.getName() + "\0"), headerRow++); // header[3] // fileName, nullbyte terminated

            for (int row = headerRow; row < paddingTop; row++) { // fill up to line paddingTop
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                Arrays.fill(bytes, (byte) 0xff);
                pngW.writeRow(imgL, row);
            }

            // Body: data bytes
            for (int row = paddingTop; row < edge + paddingTop; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                int bytesRead = inputStream.read(bytes);
                pngW.writeRow(imgL, row);
                if (bytesRead > 0) {
                    digestPart.update(bytes, 0, bytesRead);
                    digestAll.update(bytes, 0, bytesRead);
                    this.processedNBytes(bytesRead);
                }
            }

            // Footer: checksum or digest
            int rowFooter = edge + paddingTop;
            pngW.writeRow(whiteLine(imgI), rowFooter++); // footer[0]
            for (int row = rowFooter; row < paddingTop + edge + paddingBottom; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                Arrays.fill(bytes, (byte) 0xff);
                pngW.writeRow(imgL, row);
            }

            partNo++;
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
