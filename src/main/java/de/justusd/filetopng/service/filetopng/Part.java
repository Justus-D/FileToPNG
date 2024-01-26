package de.justusd.filetopng.service.filetopng;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.PngReaderByte;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class Part { // Data class for PNG part

    private final HeaderConfig config;
    private final PngReaderByte pngR;
    private final AtomicInteger cursor = new AtomicInteger(0);
    private final File imageFile;
    private String fileName;
    private long contentLength;
    private final int edge;
    private final int paddingTop;
    private final int paddingBottom;
    byte[] previousDigest;

    Part(File imageFile) throws IOException {
        this.imageFile = imageFile;
        this.pngR = new PngReaderByte(imageFile);
        byte[] bytes = this.getScanline(); // header[0]
        byte[] magicBytes = "de.justusd.filetopng".getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < magicBytes.length; i++) {
            if (bytes[i] != magicBytes[i]) throw new IOException("Magic byte mismatch!");
        }
        this.config = new HeaderConfig(this.getScanline()); // header[1]
        this.contentLength = this.config.getContentLength();
        this.edge = this.config.getEdge();
        this.paddingTop = this.config.getPaddingTop();
        this.paddingBottom = this.config.getPaddingBottom();
        this.previousDigest = new byte[32];
        byte[] previousDigestRaw = this.getScanline(); // header[2]
        System.arraycopy(previousDigestRaw, 0, previousDigest, 0, previousDigest.length);
        this.fileName = HeaderConfig.getStringFromBytes(this.getScanline()); // header[3]
    }

    public static Part[] filterPartsByUUID(Part[] parts, UUID fileUUID) throws IOException {
        List<Part> result = new ArrayList<>();
        for (Part part : parts) {
            if (part.getConfig().getUUID().equals(fileUUID)) {
                result.add(part);
            }
        }
        result.sort(Comparator.comparingInt((Part a) -> a.getConfig().getPart()));
        return result.toArray(new Part[0]);
    }

    public HeaderConfig getConfig() {
        return this.config;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ImageLineByte readRowByte() throws IOException {
        if (this.pngR.hasMoreRows()) {
            IImageLine line = this.pngR.readRow(this.cursor.getAndIncrement());
            return (ImageLineByte) line;
        } else {
            throw new IOException("No more rows.");
        }
    }

    public byte[] getScanline() throws IOException {
        return this.readRowByte().getScanline();
    }

    public void forwardToData() throws IOException {
        while (this.cursor.get() < this.paddingTop) {
            this.readRowByte();
        }
    }

    public Iterable<byte[]> getDataIterable() {
        if (this.currentRow() < this.paddingTop || this.currentRow() > paddingTop + edge) return null;
        var lineIterator = new Iterator<byte[]>() {
            @Override
            public boolean hasNext() {
                return Part.this.currentRow() > paddingTop - 1 && Part.this.currentRow() < paddingTop + edge;
            }

            @Override
            public byte[] next() {
                if (this.hasNext()) {
                    try {
                        return Part.this.getScanline();
                    } catch (IOException ignored) {
                    }
                }
                return null;
            }
        };
        return new Iterable<byte[]>() {
            @NotNull
            @Override
            public Iterator<byte[]> iterator() {
                return lineIterator;
            }
        };
    }

    public AtomicInteger getCursor() {
        return this.cursor;
    }

    public int currentRow() {
        return this.cursor.get();
    }

    public File getImageFile() {
        return this.imageFile;
    }

}
