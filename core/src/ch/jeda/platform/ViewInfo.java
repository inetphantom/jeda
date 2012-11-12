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
package ch.jeda.platform;

import ch.jeda.Size;
import ch.jeda.ui.Window;
import java.util.EnumSet;

public class ViewInfo {

    private final Size size;
    private final EnumSet<Window.Feature> features;

    public ViewInfo(Size size, EnumSet<Window.Feature> features) {
        this.size = size;
        this.features = features;
    }

    public boolean hasFeature(Window.Feature mode) {
        return this.features.contains(mode);
    }

    public Size getSize() {
        return this.size;
    }
}
