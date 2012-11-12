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

import java.awt.image.BufferedImage;

public class DoubleBufferedViewImp extends AbstractViewImp {

    private BufferedImage backBuffer;
    private BufferedImage frontBuffer;

    public DoubleBufferedViewImp(ViewWindow viewWindow) {
        super(viewWindow);
        this.backBuffer = GUI.createBufferedImage(viewWindow.getWidth(), viewWindow.getHeight());
        this.frontBuffer = GUI.createBufferedImage(viewWindow.getWidth(), viewWindow.getHeight());
        viewWindow.setImage(this.frontBuffer);
        this.setBuffer(this.backBuffer);
    }

    @Override
    public boolean isDoubleBuffered() {
        return true;
    }

    @Override
    protected void doUpdate() {
        BufferedImage temp = this.frontBuffer;
        this.frontBuffer = this.backBuffer;
        this.backBuffer = temp;
        super.setBuffer(this.backBuffer);
        this.viewWindow.setImage(this.frontBuffer);
        this.viewWindow.update();
    }
}
