/*
 * Copyright (C) 2012 by Stefan Rothe
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
package ch.jeda.platform.java;

import ch.jeda.platform.Event;
import ch.jeda.platform.WindowImp;
import ch.jeda.ui.MouseCursor;
import ch.jeda.ui.Window;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

abstract class JavaWindowImp extends JavaCanvasImp implements WindowImp {

    private static final Map<MouseCursor, Cursor> MOUSE_CURSOR_MAP = initCursorMap();
    protected final CanvasWindow canvasWindow;

    @Override
    public void close() {
        this.canvasWindow.dispose();
    }

    @Override
    public EnumSet<Window.Feature> getFeatures() {
        return this.canvasWindow.getFeatures();
    }

    @Override
    public void setFeature(Window.Feature feature, boolean enabled) {
        this.canvasWindow.setFeature(feature, enabled);
    }

    @Override
    public void setMouseCursor(MouseCursor mouseCursor) {
        assert mouseCursor != null;

        this.canvasWindow.setCursor(MOUSE_CURSOR_MAP.get(mouseCursor));
    }

    @Override
    public void setTitle(String title) {
        this.canvasWindow.setTitle(title);
    }

    @Override
    public final Iterable<Event> update() {
        this.doUpdate();
        return this.canvasWindow.fetchEvents();
    }

    static JavaWindowImp create(CanvasWindow viewWindow) {
        if (viewWindow.getFeatures().contains(Window.Feature.DoubleBuffered)) {
            return new DoubleBufferedWindowImp(viewWindow);
        }
        else {
            return new SingleBufferedWindowImp(viewWindow);
        }
    }

    JavaWindowImp(CanvasWindow viewWindow) {
        this.canvasWindow = viewWindow;
    }

    abstract void doUpdate();

    private static Cursor createInvisibleCursor() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        int[] pixels = new int[16 * 16];
        java.awt.Image image = toolkit.createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        return toolkit.createCustomCursor(image, new java.awt.Point(0, 0), "invisibleCursor");
    }

    private static Map<MouseCursor, Cursor> initCursorMap() {
        Map<MouseCursor, Cursor> result = new HashMap();
        result.put(MouseCursor.CROSSHAIR, new Cursor(Cursor.CROSSHAIR_CURSOR));
        result.put(MouseCursor.DEFAULT, new Cursor(Cursor.DEFAULT_CURSOR));
        result.put(MouseCursor.HAND, new Cursor(Cursor.HAND_CURSOR));
        result.put(MouseCursor.INVISIBLE, createInvisibleCursor());
        result.put(MouseCursor.TEXT, new Cursor(Cursor.TEXT_CURSOR));
        return result;
    }

    private static class DoubleBufferedWindowImp extends JavaWindowImp {

        private BufferedImage backBuffer;
        private BufferedImage frontBuffer;

        public DoubleBufferedWindowImp(CanvasWindow canvasWindow) {
            super(canvasWindow);
            this.backBuffer = GUI.createBufferedImage(canvasWindow.getImageSize());
            this.frontBuffer = GUI.createBufferedImage(canvasWindow.getImageSize());
            canvasWindow.setImage(this.frontBuffer);
            this.setBuffer(this.backBuffer);
        }

        @Override
        void doUpdate() {
            BufferedImage temp = this.frontBuffer;
            this.frontBuffer = this.backBuffer;
            this.backBuffer = temp;
            super.setBuffer(this.backBuffer);
            this.canvasWindow.setImage(this.frontBuffer);
            this.canvasWindow.update();
        }
    }

    private static class SingleBufferedWindowImp extends JavaWindowImp {

        private final BufferedImage buffer;

        public SingleBufferedWindowImp(CanvasWindow canvasWindow) {
            super(canvasWindow);
            this.buffer = GUI.createBufferedImage(canvasWindow.getImageSize());
            canvasWindow.setImage(this.buffer);
            this.setBuffer(this.buffer);
        }

        @Override
        void doUpdate() {
            this.canvasWindow.update();
        }

        @Override
        void modified() {
            this.canvasWindow.update();
        }
    }
}