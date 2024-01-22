package de.justusd.filetopng.service;

import ar.com.hjg.pngj.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileToPNG {

    public static void main(String[] args) {
        File inputFile = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\agpl-3.0.md");
        File outputDirectory = new File("C:\\Users\\Justus\\Documents\\aFileToPNG\\out\\");

        try {
            save(inputFile, outputDirectory);
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

    public static void save(File inputFile, File outputDirectory) throws IOException {
        if (!outputDirectory.isDirectory()) {
            throw new RuntimeException("Output directory is not a directory!");
        }
        if (!inputFile.isFile()) {
            throw new RuntimeException("Input file is not a file!");
        }
        FileInputStream inputStream = new FileInputStream(inputFile);
        long fileSize = inputFile.length(); // in Bytes

        // int edge = edgeSize(fileSize);
        long fileSizeSqrt = (long) Math.sqrt((double) fileSize / 3) + 1000;

        List<File> outputFiles = new ArrayList<>();

        int paddingTop = 96;
        int paddingBottom = 32;

        long remainingBytes = fileSize;
        int partNo = 0;
        while (remainingBytes > 0) {
            File partFile = new File(outputDirectory.getAbsolutePath() + "\\" + "part" + partNo + ".png");
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

        }

        File outputFile = new File(outputDirectory.getAbsolutePath() + "\\" + "part1.png");
        ImageInfo imgI = new ImageInfo(128, 128, 8, false);
        PngWriter pngW = new PngWriter(outputFile, imgI, true);
        ImageLineInt imgL = new ImageLineInt(imgI);
        int[] line = imgL.getScanline();
        for (int i = 0; i < 128; i++) {
            line[(i*3)] = i*2;   // R
            line[(i*3)+1] = i*2; // G
            line[(i*3)+2] = i*2; // B
        }
        for (int i = 0; i < 128; i++) {
            pngW.writeRow(imgL, i);
        }
    }

    public static void restore(File inputDirectory, File outputFile) throws IOException {
        if (!inputDirectory.isDirectory()) {
            throw new RuntimeException("Input directory is not a directory!");
        }
        if (!outputFile.isFile()) {
            throw new RuntimeException("Output file is not a file!");
        }

    }

}
