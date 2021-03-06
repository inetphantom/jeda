/*
 * Copyright (C) 2014 - 2015 by Stefan Rothe
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
 * The default Jeda check box style. The graphical representation of a button is given by an image for unchecked and an
 * image for checked state.
 *
 * @since 1.3
 * @version 2
 */
public class DefaultCheckBoxStyle implements CheckBoxStyle {

    /**
     * The Jeda antique check box style in beige.
     *
     * @since 1.3
     */
    public static final CheckBoxStyle ANTIQUE_BEIGE = new DefaultCheckBoxStyle("antique", "beige", "blue");
    /**
     * The Jeda antique check box style in blue.
     *
     * @since 1.4
     */
    public static final CheckBoxStyle ANTIQUE_BLUE = new DefaultCheckBoxStyle("antique", "blue", "grey");
    /**
     * The Jeda antique check box style in bronze.
     *
     * @since 1.4
     */
    public static final CheckBoxStyle ANTIQUE_BRONZE = new DefaultCheckBoxStyle("antique", "bronze", "grey");
    /**
     * The Jeda antique check box style in grey.
     *
     * @since 1.4
     */
    public static final CheckBoxStyle ANTIQUE_GREY = new DefaultCheckBoxStyle("antique", "grey", "blue");
    /**
     * The Jeda modern check box style in blue.
     *
     * @since 1.3
     */
    public static final CheckBoxStyle MODERN_BLUE = new DefaultCheckBoxStyle("modern", "blue");
    /**
     * The Jeda modern check box style in green.
     *
     * @since 1.3
     */
    public static final CheckBoxStyle MODERN_GREEN = new DefaultCheckBoxStyle("modern", "green");
    /**
     * The Jeda modern check box style in red.
     *
     * @since 1.3
     */
    public static final CheckBoxStyle MODERN_RED = new DefaultCheckBoxStyle("modern", "red");
    /**
     * The Jeda modern check box style in yellow.
     *
     * @since 1.3
     */
    public static final CheckBoxStyle MODERN_YELLOW = new DefaultCheckBoxStyle("modern", "yellow");

    private final Image checked;
    private final Image unchecked;

    private DefaultCheckBoxStyle(final String type, final String color) {
        this(new Image("res:jeda/ui/" + type + "_check_box_unchecked.png"),
             new Image("res:jeda/ui/" + type + "_check_box_checked_" + color + ".png"));
    }

    private DefaultCheckBoxStyle(final String type, final String checkedColor, final String uncheckedColor) {
        this(new Image("res:jeda/ui/" + type + "_check_box_unchecked_" + uncheckedColor + ".png"),
             new Image("res:jeda/ui/" + type + "_check_box_checked_" + checkedColor + ".png"));
    }

    /**
     * Constructs a new default check box style with the specified images for unchecked and checked state.
     *
     * @param unchecked the image for unchecked state
     * @param checked the image for checked state
     * @throws NullPointerException if <tt>unchecked</tt> or <tt>checked</tt> are <tt>null</tt>
     *
     * @since 1.3
     */
    public DefaultCheckBoxStyle(final Image unchecked, final Image checked) {
        if (checked == null) {
            throw new NullPointerException("checked");
        }

        if (unchecked == null) {
            throw new NullPointerException("unchecked");
        }

        this.checked = checked;
        this.unchecked = unchecked;
    }

    @Override
    public boolean contains(final CheckBox checkBox, final float x, final float y) {
        final float dx = checkBox.getCenterX() - x;
        final float dy = checkBox.getCenterY() - y;
        return Math.abs(dx) <= getWidth(checkBox) / 2 && Math.abs(dy) <= getHeight(checkBox) / 2;
    }

    @Override
    public void draw(final CheckBox checkBox, final Canvas canvas) {
        if (checkBox.isChecked()) {
            canvas.drawImage(checkBox.getLeft(), checkBox.getTop(), checked);
        }
        else {
            canvas.drawImage(checkBox.getLeft(), checkBox.getTop(), unchecked);
        }
    }

    @Override
    public int getHeight(final CheckBox checkBox) {
        return unchecked.getHeight();
    }

    @Override
    public int getWidth(final CheckBox checkBox) {
        return checked.getWidth();
    }
}
