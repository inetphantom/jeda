/*
 * Copyright (C) 2011 by Stefan Rothe
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

import ch.jeda.platform.java.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class KeyboardEventQueue extends EventQueue<KeyEvent> implements KeyListener {

    @Override
    public void keyPressed(KeyEvent event) {
        this.add(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        this.add(event);
    }

    @Override
    public void keyTyped(KeyEvent event) {
        this.add(event);
    }
}