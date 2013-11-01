/*
 * Copyright (C) 2013 by Stefan Rothe
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
package ch.jeda;

import ch.jeda.platform.CanvasImp;
import ch.jeda.platform.ImageImp;
import ch.jeda.platform.WindowImp;
import ch.jeda.ui.WindowFeature;
import java.util.EnumSet;

/**
 * <b>Internal</b>. Do not use this class.
 */
public class JedaInternal {

    /**
     * <b>Internal</b>. Do not use this method.
     */
    public static CanvasImp createCanvasImp(final int width, final int height) {
        return Jeda.createCanvasImp(width, height);
    }

    /**
     * <b>Internal</b>. Do not use this method.
     */
    public static ImageImp createImageImp(final String path) {
        return Jeda.createImageImp(path);
    }

    /**
     * <b>Internal</b>. Do not use this method.
     */
    public static WindowImp createWindowImp(final int width, final int height, final EnumSet<WindowFeature> features) {
        return Jeda.createWindowImp(width, height, features);
    }
}
