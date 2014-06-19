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

class MathUtil {

    static final double TWO_PI = 2 * Math.PI;

    static boolean isZero(final double value, final double threshold) {
        return Math.abs(value) < threshold;
    }

    static double normalizeAngle(double angle) {
        angle = angle % TWO_PI;
        if (angle < 0) {
            angle = angle + TWO_PI;
        }

        return angle;
    }
}
