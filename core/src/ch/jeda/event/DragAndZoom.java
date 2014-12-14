/*
 * Copyright (C) 2013 - 2014 by Stefan Rothe
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
package ch.jeda.event;

/**
 * @deprecated
 */
public class DragAndZoom implements PointerListener, TickListener, TurnListener {

    private PointerEvent lastPos;
    private double dx;
    private double dy;
    private double nextDx;
    private double nextDy;
    private double nextZoom;
    private double zoom;

    /**
     * @deprecated
     */
    public double getDx() {
        return this.dx;
    }

    /**
     * @deprecated
     */
    public double getDy() {
        return this.dy;
    }

    /**
     * @deprecated
     */
    public double getZoom() {
        return this.zoom;
    }

    /**
     * @deprecated
     */
    public boolean isDragging() {
        return this.lastPos != null;
    }

    @Override
    public void onPointerDown(final PointerEvent event) {
        if (this.lastPos == null) {
            this.lastPos = event;
        }
    }

    @Override
    public void onPointerMoved(final PointerEvent event) {
        if ((this.lastPos != null) && (event.getPointerId() == this.lastPos.getPointerId())) {
            this.nextDx += event.getX() - this.lastPos.getX();
            this.nextDy += event.getY() - this.lastPos.getY();
            this.lastPos = event;
        }
    }

    @Override
    public void onPointerUp(final PointerEvent event) {
        if ((this.lastPos != null) && (event.getPointerId() == this.lastPos.getPointerId())) {
            this.lastPos = null;
        }
    }

    @Override
    public void onTick(final TickEvent event) {
        this.dx = this.nextDx;
        this.dy = this.nextDy;
        this.zoom = this.nextZoom;
        this.nextDx = 0f;
        this.nextDy = 0f;
        this.nextZoom = 0f;
    }

    @Override
    public void onTurn(final TurnEvent event) {
        if (event.getAxis() == TurnAxis.MOUSE_WHEEL) {
            this.nextZoom += event.getAmount();
        }
    }
}
