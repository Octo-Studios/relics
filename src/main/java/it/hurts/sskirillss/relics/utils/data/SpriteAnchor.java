package it.hurts.sskirillss.relics.utils.data;

public enum SpriteAnchor {
    TOP_LEFT(0F, 0F),
    TOP_CENTER(0.5F, 0F),
    TOP_RIGHT(1F, 0F),
    CENTER_LEFT(0F, 0.5F),
    CENTER(0.5F, 0.5F),
    CENTER_RIGHT(1F, 0.5F),
    BOTTOM_LEFT(0F, 1F),
    BOTTOM_CENTER(0.5F, 1F),
    BOTTOM_RIGHT(1F, 1F);

    private final float xFactor;
    private final float yFactor;

    SpriteAnchor(float xFactor, float yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    public float getXOffset(float patternWidth, float scale) {
        return xFactor * patternWidth * scale;
    }

    public float getYOffset(float patternHeight, float scale) {
        return yFactor * patternHeight * scale;
    }
}