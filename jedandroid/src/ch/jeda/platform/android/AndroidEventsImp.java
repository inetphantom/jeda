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
package ch.jeda.platform.android;

import android.view.MotionEvent;
import ch.jeda.Location;
import ch.jeda.platform.EventsImp;
import ch.jeda.ui.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidEventsImp implements EventsImp {

    private final Set<Key> pressedKeys;
    private final List<Key> typedKeys;
    private boolean clicked;
    private Location downLocation;
    private String typedChars;
    private EventQueue<MotionEvent> motionEventQueue;
    private Location pointerLocation;
    private boolean pointerAvailable;

    AndroidEventsImp() {
        this.motionEventQueue = new EventQueue();
        this.pressedKeys = new HashSet();
        this.typedKeys = new ArrayList();
        this.typedChars = "";
        this.pointerLocation = Location.ORIGIN;
    }

    @Override
    public Location getPointerLocation() {
        return this.pointerLocation;
    }

    @Override
    public Set<Key> getPressedKeys() {
        return Collections.unmodifiableSet(this.pressedKeys);
    }

    @Override
    public String getTypedChars() {
        return this.typedChars;
    }

    @Override
    public List<Key> getTypedKeys() {
        return Collections.unmodifiableList(this.typedKeys);
    }

    @Override
    public boolean isClicked() {
        return this.clicked;
    }

    @Override
    public boolean isPointerAvailable() {
        return this.pointerAvailable;
    }

    void addMotionEvent(MotionEvent event) {
        this.motionEventQueue.add(event);
    }

    void update() {
        this.clicked = false;
        this.typedChars = "";
        this.typedKeys.clear();
        this.motionEventQueue.swap();
        for (MotionEvent event : this.motionEventQueue) {
            this.handleMotionEvent(event);
        }
    }

    private void handleMotionEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.updatePointerLocation(event);
                if (this.downLocation == null) {
                    this.downLocation = this.pointerLocation;
                }

                this.pointerAvailable = true;
                break;
            case MotionEvent.ACTION_MOVE:
                this.updatePointerLocation(event);
                this.pointerAvailable = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                this.updatePointerLocation(event);
                if (this.downLocation != null) {
                    if (this.downLocation.manhattanDistanceTo(this.pointerLocation) < 50) {
                        this.clicked = true;
                    }
                }

                this.pointerAvailable = false;
                break;
        }
    }

    private void updatePointerLocation(MotionEvent event) {
        this.pointerLocation = new Location((int) event.getX(), (int) event.getY());
    }
}
