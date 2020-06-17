package com.github.falsepattern.chromabeam.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.falsepattern.chromabeam.mod.Component;
import net.nicoulaj.compilecommand.annotations.Inline;

public class DrawingHelpers {
    private static final float[] COLORS = new float[]{
            0.05f, 0.05f, 0.05f,
            1, 0, 0,
            0, 1, 0,
            1, 1, 0,
            0, 0, 1,
            1, 0 ,1,
            0, 1, 1,
            1, 1, 1};
    @Inline
    public static void drawComponentColoredAlpha(SpriteBatch batch, Component component, float alpha) {
        drawComponentTextureColoredAlpha(batch, component.getTexture(), component.getX(), component.getY(), component.getRotation(), component.getDrawColor(), component.getFlipped(), alpha);
    }
    @Inline
    public static void drawComponentColored(SpriteBatch batch, Component component) {
        drawComponentTextureColored(batch, component.getTexture(), component.getX(), component.getY(), component.getRotation(), component.getDrawColor(), component.getFlipped());
    }
    @Inline
    public static void drawComponent(SpriteBatch batch, Component component) {
        drawComponentTexture(batch, component.getTexture(), component.getX(), component.getY(), component.getRotation(), component.getFlipped());
    }


    @Inline
    public static void drawComponentTexture(SpriteBatch batch, TextureRegion componentTexture, float x, float y, int rotation, boolean flipped) {
        batch.draw(componentTexture, x, y, 0.5f, 0.5f, 1, 1, 1.001f, flipped ? -1.001f : 1.001f, (4 - rotation) * 90);
    }
    @Inline
    public static void drawComponentTextureColored(SpriteBatch batch, TextureRegion componentTexture, float x, float y, int rotation, int color, boolean flipped) {
        drawComponentTextureColoredAlpha(batch, componentTexture, x, y, rotation, color, flipped, 1f);
    }
    @Inline
    public static void drawComponentTextureColoredAlpha(SpriteBatch batch, TextureRegion componentTexture, float x, float y, int rotation, int color, boolean flipped, float alpha) {
        setColor(batch, color, alpha);
        drawComponentTexture(batch, componentTexture, x, y, rotation, flipped);
    }

    public static void setColor(SpriteBatch batch, int color, float alpha) {
        batch.setColor(COLORS[color * 3], COLORS[color * 3 + 1], COLORS[color * 3 + 2], alpha);
    }
}
