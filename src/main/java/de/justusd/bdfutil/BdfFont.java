package de.justusd.bdfutil;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class BdfFont {

    private Map<Integer, BdfGlyph> glyphs = new HashMap<>();
    private Map<String, String> properties = new HashMap<>();
    private String familyName;
    private String copyright;
    private int pixelSize;
    private static final String FONTS_RESOURCE_PATH = "/de/justusd/bdfutil/fonts/";

    /**
     * Load a .bdf file
     * @param file The .bdf file
     */
    public BdfFont(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        parse(reader);
        reader.close();
    }

    public BdfFont(Font font) throws IOException {
        InputStream resourceStream = this.getClass().getResourceAsStream(FONTS_RESOURCE_PATH + font.filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream));
        parse(reader);
        reader.close();
        resourceStream.close();
    }

    private void parse(@NotNull BufferedReader reader) throws IOException {
        boolean inFont = false, inProperties = false, inChar = false, inBitmap = false;
        BdfGlyphPrototype proto = new BdfGlyphPrototype();
        int charLine = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("STARTFONT")) inFont = true;
            else if (line.startsWith("ENDFONT")) inFont = false;
            else if (line.startsWith("STARTPROPERTIES")) inProperties = true;
            else if (line.startsWith("ENDPROPERTIES")) inProperties = false;
            else if (line.startsWith("STARTCHAR")) {
                inChar = true;
                charLine = 0;
                String[] charName = line.split(" ", 2);
                if (charName.length == 2) {
                    proto.charName = charName[1];
                }
            }
            else if (line.startsWith("BITMAP")) inBitmap = true;
            else if (line.startsWith("ENDCHAR")) {
                inChar = false;
                charLine = 0;
                inBitmap = false;
                this.glyphs.put(proto.encoding, new BdfGlyph(proto));
                proto = new BdfGlyphPrototype();
            }
            else if (inFont && inProperties) {
                String[] prop = line.split(" ", 2);
                if (prop.length == 2) {
                    this.properties.put(prop[0], prop[1]);
                }
            }
            else if (inFont && inChar && !inBitmap) {
                if (line.startsWith("BBX")) {
                    String[] bbx = line.split(" ");
                    if (bbx.length == 5) {
                        proto.width = Integer.parseInt(bbx[1]);
                        proto.height = Integer.parseInt(bbx[2]);
                        proto.pixelMatrix = new boolean[proto.height][proto.width];
                    }
                } else if (line.startsWith("ENCODING")) {
                    String[] encoding = line.split(" ");
                    proto.encoding = Integer.parseInt(encoding[1]);
                }
            }
            else if (inFont && inChar) {
                BigInteger bigInteger;
                try {
                    bigInteger = new BigInteger(line.replaceAll(" ", ""), 16);
                } catch (NumberFormatException e) {
                    System.out.println("NumberFormatException: " + line);
                    bigInteger = new BigInteger("0");
                }
                StringBuilder binaryString = new StringBuilder(bigInteger.toString(2));
                while (binaryString.length() < proto.width) {
                    binaryString.insert(0, "0");
                }
                boolean[] bits = new boolean[binaryString.length()];
                for (int i = 0; i < bits.length; i++) {
                    bits[i] = binaryString.charAt(i) == '1';
                }
                proto.pixelMatrix[charLine++] = bits;
            }

        } // all lines read
        this.familyName = this.getPropertyNotNull("FAMILY_NAME").replaceAll("\"", "");
        this.copyright = this.getPropertyNotNull("COPYRIGHT").replaceAll("\"", "");
        String pixelSize = this.getProperty("PIXEL_SIZE");
        this.pixelSize = Integer.parseInt(pixelSize == null ? "0" : pixelSize);
    }

    /**
     * Gets a property of the font. If the value is a string, it may be enclosed with quotation marks.
     * @param propertyName Name of the property
     * @return Value
     */
    public String getProperty(String propertyName) {
        return this.properties.get(propertyName);
    }

    @NotNull
    public String getPropertyNotNull(String propertyName) {
        String property = this.properties.get(propertyName);
        if (property != null) return property;
        return "";
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public String getCopyright() {
        return this.copyright;
    }

    public BdfGlyph getGlyph(int encoding) {
        return this.glyphs.get(encoding);
    }

    public BdfGlyph getGlyph(char c) {
        return this.glyphs.get((int) c);
    }

    public int getPixelSize() {
        return this.pixelSize;
    }

    public static void main(String[] args) throws IOException {
        BdfFont font = new BdfFont(Font.SPLEEN_8x16);

        List<List<Boolean>> rows = new ArrayList<>(font.getPixelSize());
        for (int i = 0; i < font.getPixelSize(); i++) {
            rows.add(new ArrayList<>(8 * 12));
        }

        for (char c : "Hello World! äöüß".toCharArray()) {
            boolean[][] pixels = font.getGlyph(c).getPixelMatrix();
            int currentRow = 0;
            for (boolean[] row : pixels) {
                for (boolean p : row) {
                    rows.get(currentRow % rows.size()).add(p);
                }
                currentRow++;
            }
        }

        for (List<Boolean> row : rows) {
            for (Boolean p : row) {
                System.out.print(p ? "█" : " ");
            }
            System.out.print("\n");
        }
    }

}
