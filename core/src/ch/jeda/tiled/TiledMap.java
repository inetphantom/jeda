/*
 * Copyright (C) 2014 - 2015 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.jeda.tiled;

import ch.jeda.Data;
import ch.jeda.ui.Canvas;
import ch.jeda.ui.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Tiled map.
 *
 * @since 2.0
 */
public final class TiledMap {

    private final Color backgroundColor;
    private final int height;
    private final Layer[] layers;
    private final TiledMapOrientation orientation;
    private final Data properties;
    private final int tileHeight;
    private final TileSets tileSets;
    private final int tileWidth;
    private final int width;

    /**
     * Constructs a new Tiled map from the specified file.
     *
     * @param path the path to the Tiled map file
     *
     * @since 2.0
     */
    public TiledMap(String path) {
        if (path == null) {
            throw new NullPointerException("path");
        }

        final StringBuilder prefix = new StringBuilder();
        if (path.startsWith("res:")) {
            prefix.append("res:");
            path = path.substring(4);
        }

        final int index = path.lastIndexOf('/');
        if (index != -1) {
            prefix.append(path.substring(0, index + 1));
            path = path.substring(index + 1);
        }

        final XmlReader reader = new XmlReader(prefix.toString());
        final Element element = reader.read(path);
        backgroundColor = element.getColorAttribute(Const.BACKGROUNDCOLOR, Color.WHITE);
        height = element.getIntAttribute(Const.HEIGHT);
        tileHeight = element.getIntAttribute(Const.TILEHEIGHT);
        tileWidth = element.getIntAttribute(Const.TILEWIDTH);
        width = element.getIntAttribute(Const.WIDTH);
        orientation = parseOrientation(element.getStringAttribute(Const.ORIENTATION));
        properties = element.parsePropertiesChild();
        // Read tile sets
        tileSets = new TileSets();
        for (final Element tileSetElement : element.getChildren(Const.TILESET)) {
            if (tileSetElement.hasAttribute(Const.SOURCE)) {
                tileSets.add(new TileSet(this, reader.read(tileSetElement.getStringAttribute(Const.SOURCE)), reader));
            }
            else {
                tileSets.add(new TileSet(this, tileSetElement, reader));
            }
        }

        // Read layers
        final List<Layer> layerList = new ArrayList<Layer>();
        for (final Element layerElement : element.getChildren()) {
            if (layerElement.is("layer")) {
                layerList.add(new TileLayer(this, layerElement));
            }
            else if (layerElement.is("objectgroup")) {
                layerList.add(new ObjectGroup(this, layerElement));
            }
            else if (layerElement.is("imagelayer")) {
                layerList.add(new ImageLayer(this, layerElement, reader));
            }
        }

        layers = layerList.toArray(new Layer[layerList.size()]);
    }

    /**
     * Draws this map. The map is drawn on the specified canvas at the specified offset.
     *
     * @param canvas the canvas to draw the map on
     * @param offsetX the horizontal offset
     * @param offsetY the vertical offset
     *
     * @since 2.0
     */
    public void draw(final Canvas canvas, final double offsetX, final double offsetY) {
        canvas.setColor(backgroundColor);
        canvas.fill();
        for (int i = 0; i < layers.length; ++i) {
            layers[i].draw(canvas, offsetX, offsetY);
        }
    }

    /**
     * Returns the background color for the map.
     *
     * @return the background color for the map
     *
     * @since 2.0
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the height of the map in tiles.
     *
     * @return the height of the map in tiles
     *
     * @since 2.0
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns all layers of this map.
     *
     * @return all layers of this map
     *
     * @since 2.0
     */
    public Layer[] getLayers() {
        return Arrays.copyOf(layers, layers.length);
    }

    /**
     * Returns the orientation of this map.
     *
     * @return the orientation of this map
     *
     * @since 2.0
     */
    public TiledMapOrientation getOrientation() {
        return orientation;
    }

    /**
     * Returns the properties of this map.
     *
     * @return the properties of this map
     *
     * @since 2.0
     */
    public Data getProperties() {
        return properties;
    }

    /**
     * Returns the tile height of this map.
     *
     * @return the tile height of this map
     *
     * @since 2.0
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Returns the tile width of this map.
     *
     * @return the tile width of this map
     *
     * @since 2.0
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Returns the width of this map in tiles.
     *
     * @return the width of this map in tiles
     *
     * @since 2.0
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the tile with the specified global tile id. Returns <tt>null</tt> if no such tile exists in this map.
     *
     * @param globalId the global tile id
     * @return the tile matching the id or <tt>null</tt>
     *
     * @since 2.0
     */
    public Tile lookupTile(final int globalId) {
        return tileSets.lookupTile(globalId);
    }

    private static TiledMapOrientation parseOrientation(final String value) {
        if (value == null) {
            return TiledMapOrientation.ORTHOGONAL;
        }

        try {
            return TiledMapOrientation.valueOf(value.toUpperCase());
        }
        catch (final IllegalArgumentException ex) {
            return TiledMapOrientation.ORTHOGONAL;
        }
    }
}