package com.github.falsepattern.chromabeam.mod.interfaces;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryo.KryoSerializable;
import com.github.falsepattern.chromabeam.util.Vector2i;
import com.github.falsepattern.chromabeam.mod.Component;

import java.util.Map;

public interface World extends KryoSerializable {

    void emitInitialBeams();
    void resolveBeams();
    void beamUpdate();
    void componentUpdate();

    void update();

    /**
     * @return The component at the specified coordinates, or null of there is no component there.
     */
    Component getComponent(int x, int y);

    /**
     * Clones a component and places it at the specified coordinates with the specified transformations.
     * @return The component instance
     */
    Component cloneComponent(int x, int y, int rotation, boolean flipped, Component original);

    /**
     * Adds a component to the world, getting the x and y coordinates from the component's internal transformation.
     * If the world already contains the component, then no changes will be made to the internal container.
     * @return The component previously at the new component's position, or null of the cell was empty.
     */
    Component setComponent(Component component);

    void pause();

    void unpause();

    /**
     * Adds a component to the world, replacing the component's internal position with the specified one and
     * updating it's transformation.
     * If the world already contains this component at the specified location, then no changes will be made to the
     * internal container, and if the world contains this component at a different location, then the component will
     * be 'moved' to the new position.
     * @return The component previously at the new component's position, or null of the cell was empty.
     */
    Component setComponent(int x, int y, Component component);

    /**
     * Removes a component from the specified location. The component after removal will not be updated/drawn by the
     * world
     * @return The component at the specified coordinates, or null of there is no component there.
     */
    Component removeComponent(int x, int y);

    /**
     * @return All components contained in the world
     */
    Component[] getAllComponents();

    /**
     * Removes ALL components from the world.
     * !!!USE WITH CAUTION!!!
     */
    void clear();

    void setLabel(int x, int y, String text);

    String getLabel(int x, int y);

    void removeLabel(int x, int y);

    Map<int[], String> getAllLabels();

    void drawLabels(SpriteBatch batch, OrthographicCamera camera, BitmapFont font);

    /**
     * Renders all components visible through the camera.
     */
    void drawComponents(SpriteBatch batch, OrthographicCamera camera);

    /**
     * Renders all beams visible through the camera.
     */
    void drawBeams(SpriteBatch batch, OrthographicCamera camera);

    /**
     * Launches a beam from the specified origin point with a specified direction and color.
     */
    void createBeam(int x, int y, int rotation, int color);


    //overrides for ease of use
    default Component getComponent(Vector2i position) {
        return getComponent(position.x, position.y);
    }

    default Component cloneComponent(Vector2i position, int rotation, boolean flipped, Component component) {
        return cloneComponent(position.x, position.y, rotation, flipped, component);
    }

    default Component cloneComponent(Component component) {
        return cloneComponent(component.getX(), component.getY(), component.getRotation(), component.getFlipped(), component);
    }

    default Component setComponent(Vector2i position, Component component) {
        return setComponent(position.x, position.y, component);
    }

    default Component removeComponent(Vector2i position) {
        return removeComponent(position.x, position.y);
    }
    default void createBeam(Vector2i position, int rotation, int color) {createBeam(position.x, position.y, rotation, color);}
}
