/*
 * Copyright (C) 2013 - 2015 by Stefan Rothe
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

import ch.jeda.event.EventType;
import ch.jeda.event.Key;
import ch.jeda.event.KeyDownListener;
import ch.jeda.event.KeyEvent;
import ch.jeda.event.KeyUpListener;
import ch.jeda.event.PointerEvent;
import ch.jeda.event.PointerListener;

/**
 * Represents a button. A button is a {@link ch.jeda.ui.Widget} that allows the user to trigger an action by clicking on
 * it.
 *
 * @since 1.0
 * @version 2
 */
public class TextButton extends TextWidget implements KeyDownListener, KeyUpListener, PointerListener {

    private Key key;
    private boolean keyPressed;
    private Integer pointerId;

    /**
     * Constructs a button at the specified position.
     *
     * @param x the x coordinate of the button
     * @param y the y coordinate of the button
     *
     * @since 1.3
     */
    public TextButton(final double x, final double y) {
        this(x, y, Alignment.BOTTOM_LEFT, null);
    }

    /**
     * Constructs a button at the specified position with the specified alignment.
     *
     * @param x the x coordinate of the button
     * @param y the y coordinate of the button
     * @param alignment specifies how to align the button relative to (<tt>x</tt>, <tt>y</tt>)
     * @throws NullPointerException if <tt>alignment</tt> is <tt>null</tt>
     *
     * @since 1.3
     */
    public TextButton(final double x, final double y, final Alignment alignment) {
        this(x, y, alignment, null);
    }

    /**
     * Constructs a button at the specified position with the specified text.
     *
     * @param x the x coordinate of the button
     * @param y the y coordinate of the button
     * @param text the button text
     *
     * @since 1.3
     */
    public TextButton(final double x, final double y, final String text) {
        this(x, y, Alignment.BOTTOM_LEFT, text);
    }

    /**
     * Constructs a button at the specified position with the specified alignment and text.
     *
     * @param x the x coordinate of the button
     * @param y the y coordinate of the button
     * @param alignment specifies how to align the button relative to (<tt>x</tt>, <tt>y</tt>)
     * @param text the button text
     * @throws NullPointerException if <tt>alignment</tt> is <tt>null</tt>
     *
     * @since 1.3
     */
    public TextButton(final double x, final double y, final Alignment alignment, final String text) {
        super((float) x, (float) y, alignment);
        key = Key.UNDEFINED;
        setText(text);
        setTextColor(Color.WHITE);
    }

    /**
     * Returns the key associated with the button.
     *
     * @return the key associated with the button
     *
     * @see #setKey(ch.jeda.event.Key)
     * @since 1.3
     */
    public Key getKey() {
        return key;
    }

    /**
     * Checks if the widget is currently pressed.
     *
     * @return <tt>true</tt> if the widget is currently pressed, otherwise <tt>false</tt>
     *
     * @since 1.3
     */
    public final boolean isPressed() {
        return keyPressed || pointerId != null;
    }

    @Override
    public void onKeyDown(final KeyEvent event) {
        if (Key.UNDEFINED != key && event.getKey() == key && event.getSource() != this && !keyPressed) {
            keyPressed = true;
            select();
            event.consume();
        }
    }

    @Override
    public void onKeyUp(final KeyEvent event) {
        if (Key.UNDEFINED != key && event.getKey() == key && event.getSource() != this && keyPressed) {
            keyPressed = false;
            clicked();
            event.consume();
        }
    }

    @Override
    public void onPointerDown(final PointerEvent event) {
        if (pointerId == null && contains(event.getCanvasX(), event.getCanvasY())) {
            pointerId = event.getPointerId();
            select();
            sendKeyEvent(EventType.KEY_DOWN);
            event.consume();
        }
    }

    @Override
    public void onPointerMoved(final PointerEvent event) {
        if (pointerId != null && event.getPointerId() == pointerId) {
            if (contains(event.getCanvasX(), event.getCanvasY())) {
                event.consume();
            }
            else {
                pointerId = null;
                sendKeyEvent(EventType.KEY_UP);
            }
        }
    }

    @Override
    public void onPointerUp(final PointerEvent event) {
        if (pointerId != null && event.getPointerId() == pointerId) {
            pointerId = null;
            sendKeyEvent(EventType.KEY_UP);
            if (contains(event.getCanvasX(), event.getCanvasY())) {
                clicked();
                event.consume();
            }
        }
    }

    /**
     * Associates a key with the button. Associating a key with the button makes the button a virtual copy of the key:
     * The button will be pressed and released synchronously with the key. Pressing and releasing the button will
     * generate the corresponding key events.
     *
     * @param key the key to associate with the button.
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     *
     * @see #getKey()
     * @since 1.3
     */
    public void setKey(final Key key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        this.key = key;
    }

    /**
     * This method is called when the user has clicked the button. Override this method to add behaviour.
     *
     * @since 1.3
     */
    protected void clicked() {
        triggerAction(getText());
    }

    @Override
    protected void draw(final Canvas canvas) {
        canvas.setAntiAliasing(true);
        canvas.setColor(getBackground());
        canvas.setAlignment(Alignment.CENTER);
        canvas.fillRectangle(0, 0, getWidth(), getHeight());
        if (isPressed()) {
            canvas.setTextSize(getTextSize() - 1);
        }
        else {
            canvas.setTextSize(getTextSize());
            canvas.drawShadowRectangle(0, 0, getWidth(), getHeight());
        }

        canvas.setColor(getTextColor());
        canvas.drawText(0, 0, getText());
    }

    private void sendKeyEvent(final EventType eventType) {
        final View view = getView();
        if (key != Key.UNDEFINED && view != null) {
            view.postEvent(new KeyEvent(this, eventType, key));
        }
    }
}