/*
 * Copyright (C) 2011, 2012 by Stefan Rothe
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
package ch.jeda.ui;

import ch.jeda.platform.EventsImp;
import java.util.List;
import java.util.Set;

/**
 * This class represents events that are taking place on this window. These
 * are typically keys pressed or typed by the user or motion events such as
 * moving or clicking with a mouse, trackball, pen or finger.
 */
public class Events {

    private EventsImp imp;

    /**
     * Returns a set of all keys that are currently pressed.
     *
     * @return set of all keys that are pressed.
     * @since 1.0
     */
    public Set<Key> getPressedKeys() {
        return this.imp.getPressedKeys();
    }

    /**
     * Returns a String representing the recently typed characters. When a
     * character is typed, it is appended to the String. The string is cleared
     * when {@link Window#update()} is called. Returns an empty String ("") when
     * no characters have been typed since the last call to {@link Window#update()}.
     *
     * @return recently typed characters or ""
     */
    public String getTypedChars() {
        return this.imp.getTypedChars();
    }

    /**
     * Returns a set of all keys that have been typed recently.
     * 
     * @return recently typed keys
     */
    public List<Key> getTypedKeys() {
        return this.imp.getTypedKeys();
    }

    /**
     * Checks whether the specified key is currently pressed.
     *
     * @param key the key to check for
     * @return <code>true</code> if specified key is currently pressed
     * @throws NullPointerException when key is null
     * @since 1.0
     */
    public boolean isKeyPressed(Key key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        return this.imp.getPressedKeys().contains(key);
    }

    /**
     * Checks whether a key was typed recently.
     *
     * @param key key to check
     * @return <code>true</code> if specified key was typed recently
     * @throws NullPointerException when key is null
     * @since 1.0
     */
    public boolean isKeyTyped(Key key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        return this.imp.getTypedKeys().contains(key);
    }

    @Deprecated
    public Set<Key> pressedKeys() {
        return this.imp.getPressedKeys();
    }

    @Deprecated
    public String typedChars() {
        return this.imp.getTypedChars();
    }

    @Deprecated
    public List<Key> typedKeys() {
        return this.imp.getTypedKeys();
    }

    void setImp(EventsImp imp) {
        this.imp = imp;
    }
}