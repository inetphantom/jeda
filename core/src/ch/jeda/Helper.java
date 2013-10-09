/*
 * Copyright (C) 2013 by Stefan Rothe
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
package ch.jeda;

import java.util.Locale;
import java.util.ResourceBundle;

class Helper {

    static String getMessage(final String key) {
        try {
            final ResourceBundle rb = ResourceBundle.getBundle("translations", Locale.getDefault());
            if (rb.containsKey(key)) {
                return rb.getString(key);
            }
        }
        catch (final Exception ex) {
            // ignore
        }

        return "<" + key + ">";
    }

    static boolean hasInterface(final Class<?> candidateClass, final Class<?> targetInterface) {
        final Class[] interfaces = candidateClass.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            if (interfaces[i].equals(targetInterface)) {
                return true;
            }
        }

        return false;
    }

    static void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException ex) {
        }
    }
}