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

import ch.jeda.platform.EventsImp;
import ch.jeda.platform.ViewImp;
import ch.jeda.ui.MouseCursor;
import ch.jeda.ui.Window;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

abstract class JavaViewImp extends JavaCanvasImp implements ViewImp {

    private static final Map<MouseCursor, Cursor> MOUSE_CURSOR_MAP = initCursorMap();
    private final EnumSet<Window.Feature> features;
    protected final ViewWindow viewWindow;

    static JavaViewImp create(ViewWindow viewWindow, boolean doubleBuffered) {
        if (doubleBuffered) {
            return new DoubleBufferedViewImp(viewWindow);
        }
        else {
            return new SingleBufferedViewImp(viewWindow);
        }
    }

    JavaViewImp(ViewWindow viewWindow, Window.Feature... features) {
        this.features = EnumSet.copyOf(Arrays.asList(features));
        this.viewWindow = viewWindow;

        if (this.viewWindow.isFullscreen()) {
            this.features.add(Window.Feature.Fullscreen);
        }
    }

    @Override
    public void close() {
        this.viewWindow.dispose();
    }

    @Override
    public EventsImp getEventsImp() {
        return this.viewWindow.getEventsImp();
    }

    @Override
    public EnumSet<Window.Feature> getFeatures() {
        return this.features;
    }

    @Override
    public void setMouseCursor(MouseCursor mouseCursor) {
        assert mouseCursor != null;

        this.viewWindow.setCursor(MOUSE_CURSOR_MAP.get(mouseCursor));
    }

    @Override
    public void setTitle(String title) {
        this.viewWindow.setTitle(title);
    }

    @Override
    public final void update() {
        this.doUpdate();
    }

    abstract void doUpdate();

    private static Map<MouseCursor, Cursor> initCursorMap() {
        Map<MouseCursor, Cursor> result = new HashMap();
        result.put(MouseCursor.CROSSHAIR, new Cursor(Cursor.CROSSHAIR_CURSOR));
        result.put(MouseCursor.DEFAULT, new Cursor(Cursor.DEFAULT_CURSOR));
        result.put(MouseCursor.HAND, new Cursor(Cursor.HAND_CURSOR));
        result.put(MouseCursor.INVISIBLE, createInvisibleCursor());
        result.put(MouseCursor.TEXT, new Cursor(Cursor.TEXT_CURSOR));
        return result;
    }

    private static Cursor createInvisibleCursor() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        int[] pixels = new int[16 * 16];
        java.awt.Image image = toolkit.createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        return toolkit.createCustomCursor(image, new java.awt.Point(0, 0), "invisibleCursor");
    }

    private static class DoubleBufferedViewImp extends JavaViewImp {

        private BufferedImage backBuffer;
        private BufferedImage frontBuffer;

        public DoubleBufferedViewImp(ViewWindow viewWindow) {
            super(viewWindow, Window.Feature.DoubleBuffered);
            this.backBuffer = GUI.createBufferedImage(viewWindow.getImageSize());
            this.frontBuffer = GUI.createBufferedImage(viewWindow.getImageSize());
            viewWindow.setImage(this.frontBuffer);
            this.setBuffer(this.backBuffer);
        }

        @Override
        void doUpdate() {
            BufferedImage temp = this.frontBuffer;
            this.frontBuffer = this.backBuffer;
            this.backBuffer = temp;
            super.setBuffer(this.backBuffer);
            this.viewWindow.setImage(this.frontBuffer);
            this.viewWindow.update();
        }
    }

    private static class SingleBufferedViewImp extends JavaViewImp {

        private final BufferedImage buffer;

        public SingleBufferedViewImp(ViewWindow viewWindow) {
            super(viewWindow);
            this.buffer = GUI.createBufferedImage(viewWindow.getImageSize());
            viewWindow.setImage(this.buffer);
            this.setBuffer(this.buffer);
        }

        @Override
        void doUpdate() {
            this.viewWindow.update();
        }

        @Override
        void modified() {
            this.viewWindow.update();
        }
    }
}