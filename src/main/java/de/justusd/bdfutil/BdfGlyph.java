package de.justusd.bdfutil;

public class BdfGlyph {

    private int encoding;
    private String charName;
    private int width;
    private int height;
    private boolean[][] pixelMatrix;

    BdfGlyph(BdfGlyphPrototype prototype) {
        this.encoding = prototype.encoding;
        this.charName = prototype.charName;
        this.width = prototype.width;
        this.height = prototype.height;
        this.pixelMatrix = prototype.pixelMatrix;
    }

    public boolean[][] getPixelMatrix() {
        boolean[][] pixelMatrix = new boolean[this.pixelMatrix.length][];
        for (int i = 0; i < pixelMatrix.length; i++) {
            pixelMatrix[i] = new boolean[this.pixelMatrix[i].length];
            System.arraycopy(this.pixelMatrix[i], 0, pixelMatrix[i], 0, this.pixelMatrix[i].length);
        }
        return pixelMatrix;
    }

}
