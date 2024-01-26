package de.justusd.filetopng.service.filetopng;

import ar.com.hjg.pngj.*;
import de.justusd.bdfutil.BdfFont;
import de.justusd.bdfutil.Font;
import de.justusd.filetopng.service.digest.Digest;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileToPNG {

    private final File file;
    private final File directory;
    private boolean saveInProgress = false;
    private boolean restoreInProgress = false;
    private final AtomicLong bytesProcessed = new AtomicLong(0L);;
    private long fileSize;
    private long fileLastModified;
    private UUID fileUUID; // used for identifying the parts of a file
    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();
    IOException occurredException = null;
    private Thread thread;

    /**
     * Either save a file to one or more PNGs or restore a file from one or more PNGs.
     * Use the save() or restore() methods to save or restore.
     * Calling one method multiple times will cause an IOException.
     * @param file File to save from or restore to (must be a file)
     * @param directory Directory for the PNGs (must be a directory)
     */
    public FileToPNG(@NotNull File file, @NotNull File directory) throws IOException {
        if(!file.exists() && !file.createNewFile()) {
            throw new IOException("New File could not be created!");
        }
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
        File inputFile = new File(System.getenv("DEBUG_FILE"));
        File outputDirectory = new File(System.getenv("DEBUG_DIRECTORY"));

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
        if (this.occurredException != null) throw this.occurredException;
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
                FileToPNG.this.occurredException = e;
            }
        };
        if (this.thread != null) return;
        Thread thread = new Thread(runnable);
        this.thread = thread;
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void restore() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        Runnable runnable = () -> {
            try {
                FileToPNG.this.restoreSync();
            } catch (IOException e) {
                FileToPNG.this.occurredException = e;
                PropertyChangeEvent event = new PropertyChangeEvent(
                        this,
                        "gotError",
                        false,
                        true
                );
                for (PropertyChangeListener listener : this.propertyChangeListeners) {
                    listener.propertyChange(event);
                }
            }
        };
        if (this.thread != null) return;
        Thread thread = new Thread(runnable);
        this.thread = thread;
        thread.setDaemon(true);
        thread.start();
    }

    @NotNull
    private Digest getMdInstance() {
        return new Digest();
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

    private int writeText(@NotNull ImageInfo imgI, @NotNull PngWriter pngW, @NotNull String text, @NotNull AtomicInteger rowIndex) throws IOException {
        int linesWritten = 0;

        BdfFont font = new BdfFont(Font.SPLEEN_8x16);

        List<List<Byte>> rows = new ArrayList<>(font.getPixelSize());
        for (int i = 0; i < font.getPixelSize(); i++) {
            rows.add(new ArrayList<>(8 * 12));
        }

        for (char c : (" " + text).toCharArray()) {
            boolean[][] pixels = font.getGlyph(c).getPixelMatrix();
            int currentRow = 0;
            for (boolean[] row : pixels) {
                for (boolean p : row) {
                    rows.get(currentRow % rows.size()).add(p ? (byte) 0x00 : (byte) 0xff);
                    rows.get(currentRow % rows.size()).add(p ? (byte) 0x00 : (byte) 0xff);
                    rows.get(currentRow % rows.size()).add(p ? (byte) 0x00 : (byte) 0xff);
                }
                currentRow++;
            }
        }

        for (List<Byte> row : rows) {
            Byte[] bytes = row.toArray(new Byte[0]);
            byte[] pBytes = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) pBytes[i] = bytes[i];
            pngW.writeRow(byteArrayLine(imgI, pBytes, (byte) 0xff), rowIndex.getAndIncrement());
            linesWritten++;
        }

        return linesWritten;
    }

    private void whiteLines(@NotNull ImageInfo imgI, @NotNull PngWriter pngW, int nLines, @NotNull AtomicInteger rowIndex) {
        for (int i = 0; i < nLines; i++) pngW.writeRow(whiteLine(imgI), rowIndex.getAndIncrement());
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public void saveSync() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        this.saveInProgress = true;
        FileInputStream inputStream = new FileInputStream(file);
        this.fileSize = file.length(); // in Bytes
        this.fileLastModified = this.file.lastModified();
        this.fileUUID = UUID.randomUUID();
        this.bytesProcessed.set(0L);
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String partPrefix = currentDate.format(formatter) + "_part";

        List<File> outputFiles = new ArrayList<>();
        List<PngWriter> writers = new ArrayList<>();
        List<FileOutputStream> outputStreams = new ArrayList<>();
        List<Digest> messageDigests = new ArrayList<>();
        Digest digestAll = this.getMdInstance();

        int paddingTop = 96;
        int paddingBottom = 64;

        long remainingBytes = this.fileSize;
        int partNo = 0;
        while (remainingBytes > 0) {

            File partFile = new File(this.directory.getAbsolutePath() + "\\" + partPrefix + partNo + ".png");
            outputFiles.add(partFile);
            Digest digestPart = this.getMdInstance();
            messageDigests.add(digestPart);
            int edge = edgeSize(remainingBytes);
            remainingBytes -= maxContentBytes(edge);
            ImageInfo imgI = new ImageInfo(edge, edge + paddingTop + paddingBottom, 8, false);
            FileOutputStream outputStream = new FileOutputStream(partFile);
            outputStreams.add(outputStream);
            PngWriter pngW = new PngWriter(outputStream, imgI);
            writers.add(pngW);

            // Header: metadata, previous checksum or digest
            AtomicInteger headerRow = new AtomicInteger(0);
            pngW.writeRow(utf8TextLine(imgI, "de.justusd.filetopng\0"), headerRow.getAndIncrement()); // header[0] // magic bytes
            pngW.writeRow(utf8TextLine(imgI,
                    "version=1;",
                    "part=" + partNo + ";",
                    "fileSize=" + fileSize + ";",
                    "contentLength=" + contentLengthForPart(fileSize, partNo) + ";",
                    "edge=" + edge + ";",
                    "paddingTop=" + paddingTop + ";",
                    "paddingBottom=" + paddingBottom + ";",
                    "digestAlgorithm=SHA-256;",
                    "digestLength=32;", // SHA-256: 256 bits = 32 bytes
                    "previousDigestIndex=2;", // optional, index always 2
                    "fileNameIndex=3;", // optional, index always 3
                    "UUID=" + this.fileUUID.toString() + ";",
                    "\0"
            ), headerRow.getAndIncrement()); // header[1] // header data

            Digest previousDigest = null;
            int prevPartIndex = partNo - 1;
            if (!(prevPartIndex < 0 || prevPartIndex >= messageDigests.size())) {
                previousDigest = messageDigests.get(prevPartIndex);
            }
            if (previousDigest != null) {
                pngW.writeRow(byteArrayLine(imgI, previousDigest.digest()), headerRow.getAndIncrement()); // header[2] // digest of previous part
            } else {
                pngW.writeRow(whiteLine(imgI), headerRow.getAndIncrement()); // header[2] // white if no digest
            }

            pngW.writeRow(utf8TextLine(imgI, file.getName() + "\0"), headerRow.getAndIncrement()); // header[3] // fileName, nullbyte terminated
            writeText(imgI, pngW, "PNG generated with: de.justusd.filetopng", headerRow);
            writeText(imgI, pngW, "Filename: " + file.getName(), headerRow);
            writeText(imgI, pngW, "part: " + partNo + "; fileSize: " + fileSize + " bytes; contentLength: " + contentLengthForPart(fileSize, partNo), headerRow);
            writeText(imgI, pngW, "", headerRow);

            for (int row = headerRow.get(); row < paddingTop; row++) { // fill up to line paddingTop
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
            AtomicInteger rowFooter = new AtomicInteger(edge + paddingTop);
            pngW.writeRow(whiteLine(imgI), rowFooter.getAndIncrement()); // footer[0]
            pngW.writeRow(byteArrayLine(imgI, digestPart.digest(), (byte) 0xff), rowFooter.getAndIncrement()); // footer[1] // digest of the part
            pngW.writeRow(byteArrayLine(imgI, digestAll.digest(), (byte) 0xff), rowFooter.getAndIncrement()); // footer[2] // overall digest
            whiteLines(imgI, pngW, 5, rowFooter);
            writeText(imgI, pngW, "SHA-256 of bytes in this part: " + byteToHexString(digestPart.digest()), rowFooter);
            writeText(imgI, pngW, "SHA-256 of all bytes written:  " + byteToHexString(digestAll.digest()), rowFooter);
            for (int row = rowFooter.get(); row < paddingTop + edge + paddingBottom; row++) {
                ImageLineByte imgL = new ImageLineByte(imgI);
                byte[] bytes = imgL.getScanline();
                Arrays.fill(bytes, (byte) 0xff);
                pngW.writeRow(imgL, row);
            }
            partNo++;
            outputStream.flush();
            pngW.close();

        }

        // cleanup
        inputStream.close();
        for (FileOutputStream stream : outputStreams) {
            try {
                stream.flush();
                stream.close();
            } catch (IOException ignored) {}
        }
        this.sendFinished();
    }

    public static String detectFileName(File inputDirectory) throws IOException {
        File[] pngFiles = inputDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) {
            return null;
        }
        Arrays.sort(pngFiles);
        Part[] parts = new Part[pngFiles.length];

        for (int i = 0; i < parts.length; i++) {
            parts[i] = new Part(pngFiles[i]);
        }

        return parts[0].getFileName();
    }

    public void restoreSync() throws IOException {
        if (this.saveInProgress || this.restoreInProgress) throw new IOException("Already running save or restore");
        this.restoreInProgress = true;

        File[] pngFiles = this.directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) {
            throw new IOException("No PNG files found.");
        }
        Arrays.sort(pngFiles);
        Part[] parts = new Part[pngFiles.length];

        for (int i = 0; i < parts.length; i++) {
            parts[i] = new Part(pngFiles[i]);
        }

        for (Part part : parts) {
            HeaderConfig c = part.getConfig();
            System.out.println("part: " + c.getPart() + ", UUID: " + c.getUUID() + ", fileName: " + part.getFileName());
        }

        Part[] filteredParts = Part.filterPartsByUUID(parts, parts[0].getConfig().getUUID());
        if (filteredParts.length == 0) {
            throw new IOException("No parts available");
        }
        this.fileSize = filteredParts[0].getConfig().getFileSize();

        // Data loop
        FileOutputStream outputStream = new FileOutputStream(this.file.getAbsoluteFile());
        Digest digest = new Digest();
        long bytesRemaining = parts[0].getConfig().getFileSize();
        for (Part part : filteredParts) {
            part.forwardToData();
            long partBytesRemaining = part.getConfig().getContentLength();
            Digest digestPart = new Digest();
            for (byte[] bytes : part.getDataIterable()) {
                int bytesToBeWrittenLength = (int) Math.min(bytesRemaining, bytes.length);
                byte[] bytesToBeWritten = new byte[bytesToBeWrittenLength];
                System.arraycopy(bytes, 0, bytesToBeWritten, 0, bytesToBeWritten.length);
                digest.update(bytesToBeWritten);
                digestPart.update(bytesToBeWritten);
                outputStream.write(bytesToBeWritten);
                bytesRemaining -= bytesToBeWritten.length;
                this.processedNBytes(bytesToBeWritten.length);
            }
            part.readRowByte();
            byte[] digestBytesPart = new byte[32];
            System.arraycopy(part.getScanline(), 0, digestBytesPart, 0, digestBytesPart.length);
            System.out.println(byteToHexString(digestBytesPart));
            byte[] digestBytesAll = new byte[32];
            System.arraycopy(part.getScanline(), 0, digestBytesAll, 0, digestBytesAll.length);
            outputStream.flush();
            if (!( Arrays.equals(digestBytesAll, digest.digest()) && Arrays.equals(digestBytesPart, digestPart.digest()) )) {
                throw new IOException("Digest mismatch in part" + part.getConfig().getPart() + "!");
            }
        }

        // cleanup
        outputStream.flush();
        outputStream.close();
        this.sendFinished();
    }

}

