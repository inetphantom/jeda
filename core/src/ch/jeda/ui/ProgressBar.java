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
 * Represents a progress bar. A progress bar is a {@link ch.jeda.ui.Widget} that displays a progress percentage
 * graphically.
 *
 * @since 1.3
 */
public class ProgressBar extends Widget {

    private ProgressBarStyle style;
    private double maximumValue;
    private double minimumValue;
    private double value;

    /**
     * Constructs a progress bar at the specified location.
     *
     * @param x the x coordinate of the top left corner of the progress bar
     * @param y the y coordinate of the top left corner of the progress bar
     *
     * @since 1.3
     */
    public ProgressBar(int x, int y) {
        this(x, y, Alignment.TOP_LEFT);

    }

    /**
     * Constructs a progress bar at the specified position with the specified alignment.
     *
     * @param x the x coordinate of the progress bar
     * @param y the y coordinate of the progress bar
     * @param alignment the alignment of the progress bar
     * @throws NullPointerException if <tt>alignment</tt> is <tt>null</tt>
     *
     * @since 1.3
     */
    public ProgressBar(final int x, final int y, final Alignment alignment) {
        super(x, y, alignment);
        this.style = Theme.getDefault().getDefaultProgressBarStyle();
        this.minimumValue = 0.0;
        this.maximumValue = 1.0;
    }

    @Override
    public final boolean contains(final int x, int y) {
        return this.style.contains(this, x, y);
    }

    @Override
    public final int getHeight() {
        return this.style.getHeight(this);
    }

    /**
     * Returns the current style of the progress bar.
     *
     * @return the current style of the progress bar
     *
     * @since 1.3
     */
    public final ProgressBarStyle getStyle() {
        return this.style;
    }

    /**
     * Returns the maximum value. Values greater than or equal to the maximum value are represented by a full progress
     * bar.
     *
     * @return the maximum value
     *
     * @see #getMinimumValue()
     * @see #getValue()
     * @see #setMaximumValue(double)
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public double getMaximumValue() {
        return this.maximumValue;
    }

    /**
     * Returns the minimum value. Values smaller than or equal to the minimum value are represented by an empty progress
     * bar.
     *
     * @return the minimum value
     *
     * @see #getMaximumValue()
     * @see #getValue()
     * @see #setMaximumValue(double)
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public double getMinimumValue() {
        return this.minimumValue;
    }

    /**
     * Returns the current progress as a value between <tt>0</tt> and <tt>1</tt>. Returns <tt>0</tt> if the current
     * value is smaller than or equal to the minimum value. Returns <tt>1</tt> if the current value is greater than or
     * equal to the maximum value. Otherwise, returns the fraction of the current progress (the difference between the
     * current value and the minimum value) over the progress bar range (the difference between the maxmimun and the
     * minimum value).
     *
     * @return the current progress
     *
     * @since 1.3
     */
    public double getProgress() {
        if (this.value <= this.minimumValue) {
            return 0.0;
        }
        else if (this.maximumValue <= this.value) {
            return 1.0;
        }
        else {
            return (this.value - this.minimumValue) / (this.maximumValue - this.minimumValue);
        }
    }

    /**
     * Returns the current value. Values smaller than or equal to the minimum value are represented by an empty progress
     * bar, values greater than or equal to the maximum value are represented by a full progress bar. Values between the
     * minimum and maximum value are represented by the corresponding visible fraction of the progress bar.
     *
     * @return the current value
     *
     * @see #getMaximumValue()
     * @see #getMinimumValue()
     * @see #setMaximumValue(double)
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public double getValue() {
        return this.value;
    }

    @Override
    public final int getWidth() {
        return this.style.getWidth(this);
    }

    /**
     * Sets the maximum value. Values greater than or equal to the maximum value are represented by a full progress bar.
     *
     * @param maximumValue the maximum value
     * @throws IllegalArgumentException if <tt>maximumValue<tt> is not greater than {@link  #getMinimumValue()}
     *
     * @see #getMaximumValue()
     * @see #getMinimumValue()
     * @see #getValue()
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public void setMaximumValue(final double maximumValue) {
        if (maximumValue <= this.minimumValue) {
            throw new IllegalArgumentException("maximumValue");
        }

        this.maximumValue = maximumValue;
    }

    /**
     * Sets the minimum value. Values smaller than or equal to the minimum value are represented by an empty progress
     * bar.
     *
     * @param minimumValue the minimum value
     * @throws IllegalArgumentException if <tt>minimumValue<tt> is not smaller than {@link  #getMaximumValue()}
     *
     * @see #getMaximumValue()
     * @see #getMinimumValue()
     * @see #getValue()
     * @see #setMaximumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public void setMinimumValue(final double minimumValue) {
        if (this.maximumValue <= minimumValue) {
            throw new IllegalArgumentException("minimumValue");
        }

        this.minimumValue = minimumValue;
    }

    /**
     * Sets the minimum and maximum value. Values smaller than or equal to the minimum value are represented by an empty
     * progress bar, values greater than or equal to the maximum value are represented by a full progress bar.
     *
     * @param minimumValue the minimum value
     * @param maximumValue the maximum value
     * @throws IllegalArgumentException if <tt>minimumValue<tt> is not greater than <tt>maximumValue</tt>
     *
     * @see #getMaximumValue()
     * @see #getMinimumValue()
     * @see #getValue()
     * @see #setMaximumValue(double)
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @see #setValue(double)
     * @since 1.3
     */
    public void setRange(final double minimumValue, final double maximumValue) {
        if (maximumValue <= minimumValue) {
            throw new IllegalArgumentException("maximumValue");
        }

        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    /**
     * Set the specified style for the progress bar.
     *
     * @param style the style
     * @throws NullPointerException if <tt>style</tt> is <tt>null</tt>
     *
     * @since 1.3
     */
    public final void setStyle(final ProgressBarStyle style) {
        if (style == null) {
            throw new NullPointerException("style");
        }

        this.style = style;
    }

    /**
     * Sets the current value. Values smaller than or equal to the minimum value are represented by an empty progress
     * bar, values greater than or equal to the maximum value are represented by a full progress bar. Values between the
     * minimum and maximum value are represented by the corresponding visible fraction of the progress bar.
     *
     * @param value the current value
     *
     * @see #getMaximumValue()
     * @see #getMinimumValue()
     * @see #getValue()
     * @see #setMaximumValue(double)
     * @see #setMinimumValue(double)
     * @see #setRange(double, double)
     * @since 1.3
     */
    public void setValue(final double value) {
        this.value = value;
    }

    @Override
    protected void draw(final Canvas canvas) {
        this.style.draw(this, canvas);
    }
}
