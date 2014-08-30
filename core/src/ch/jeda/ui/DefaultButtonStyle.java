/*
 * Copyright (C) 2014 by Stefan Rothe
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

/**
 * The default Jeda button style. The graphical representation of a button is given by a background image. Optionally, a
 * second background image for the pressed state can be specified.
 *
 * @since 1.3
 */
public class DefaultButtonStyle extends DefaultTextStyle implements ButtonStyle {

    private static final String ANTIQUE = "antique";
    private static final String MODERN = "modern";
    /**
     * The Jeda antique button style in beige.
     *
     * @since 1.3
     */
    public static final ButtonStyle ANTIQUE_BEIGE = new DefaultButtonStyle(ANTIQUE, "beige");
    /**
     * The Jeda antique button style in blue.
     *
     * @since 1.3
     */
    public static final ButtonStyle ANTIQUE_BLUE = new DefaultButtonStyle(ANTIQUE, "blue");
    /**
     * The Jeda antique button style in brown.
     *
     * @since 1.3
     */
    public static final ButtonStyle ANTIQUE_BROWN = new DefaultButtonStyle(ANTIQUE, "brown");
    /**
     * The Jeda antique button style in grey.
     *
     * @since 1.3
     */
    public static final ButtonStyle ANTIQUE_GREY = new DefaultButtonStyle(ANTIQUE, "grey");
    /**
     * The Jeda modern button style in blue.
     *
     * @since 1.3
     */
    public static final ButtonStyle MODERN_BLUE = new DefaultButtonStyle(MODERN, "blue");
    /**
     * The Jeda modern button style in green.
     *
     * @since 1.3
     */
    public static final ButtonStyle MODERN_GREEN = new DefaultButtonStyle(MODERN, "green");
    /**
     * The Jeda modern button style in grey.
     *
     * @since 1.3
     */
    public static final ButtonStyle MODERN_GREY = new DefaultButtonStyle(MODERN, "grey");
    /**
     * The Jeda modern button style in red.
     *
     * @since 1.3
     */
    public static final ButtonStyle MODERN_RED = new DefaultButtonStyle(MODERN, "red");
    /**
     * The Jeda modern button style in yellow.
     *
     * @since 1.3
     */
    public static final ButtonStyle MODERN_YELLOW = new DefaultButtonStyle(MODERN, "yellow");

    private final Image background;
    private final Image backgroundPressed;
    private final int pressedOffsetX;
    private final int pressedOffsetY;

    private DefaultButtonStyle(final String type, final String color) {
        this(new Image("res:jeda/ui/" + type + "_button_" + color + ".png"),
             new Image("res:jeda/ui/" + type + "_button_" + color + "_pressed.png"), 0, 4);
        if (ANTIQUE.equals(type)) {
            this.setTypeface(Typeface.SERIF);
        }
    }

    /**
     * Constructs a new default button style with the specified background image.
     *
     * @param background the button background image
     * @throws NullPointerException if <tt>background</tt> is <tt>null</tt>
     *
     * @since 1.3
     */
    public DefaultButtonStyle(final Image background) {
        this(background, background, 0, 0);
    }

    /**
     * Constructs a new default button style with the specified background images.
     *
     * @param background the button background image for normal state
     * @param backgroundPressed the button background image for pressed state
     * @throws NullPointerException if <tt>background</tt> or <tt>backgroundPressed</tt> are <tt>null</tt>
     *
     * @since 1.3
     */
    public DefaultButtonStyle(final Image background, final Image backgroundPressed) {
        this(background, backgroundPressed, 0, 0);
    }

    /**
     * Constructs a new default button style with the specified background images and offsets.
     *
     * @param background the button background image for normal state
     * @param backgroundPressed the button background image for pressed state
     * @param pressedOffsetX the horizontal draw offset for pressed state
     * @param pressedOffsetY the vertical draw offset for pressed state
     * @throws NullPointerException if <tt>background</tt> or <tt>backgroundPressed</tt> are <tt>null</tt>
     *
     * @since 1.3
     */
    public DefaultButtonStyle(final Image background, final Image backgroundPressed, final int pressedOffsetX,
                              final int pressedOffsetY) {
        if (background == null) {
            throw new NullPointerException("background");
        }

        if (backgroundPressed == null) {
            throw new NullPointerException("backgroundPressed");
        }

        this.background = background;
        this.backgroundPressed = backgroundPressed;
        this.pressedOffsetX = pressedOffsetX;
        this.pressedOffsetY = pressedOffsetY;
    }

    @Override
    public boolean contains(final Button button, final int x, final int y) {
        final int dx = button.getCenterX() - x;
        final int dy = button.getCenterY() - y;
        return Math.abs(dx) <= this.getWidth(button) / 2 && Math.abs(dy) <= this.getHeight(button) / 2;
    }

    @Override
    public void draw(final Button button, final Canvas canvas) {
        this.applyTextStyle(canvas);
        int ox = 0;
        int oy = 0;
        Image image = this.background;
        if (button.isPressed()) {
            ox = ox + this.pressedOffsetX;
            oy = oy + this.pressedOffsetY;
            if (this.backgroundPressed != null) {
                image = this.backgroundPressed;
            }
        }

        canvas.drawImage(button.getX() + ox, button.getY() + oy, image, button.getAlignment());
        canvas.drawText(button.getCenterX() + ox, button.getCenterY() + oy, button.getText(), Alignment.CENTER);
    }

    @Override
    public int getHeight(final Button button) {
        return this.background.getHeight();
    }

    @Override
    public int getWidth(final Button button) {
        return this.background.getWidth();
    }
}
