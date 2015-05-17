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
package ch.jeda.tiled;

import ch.jeda.Data;
import ch.jeda.ui.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;

final class Element {

    private final Data attributes;
    private final List<Element> children;
    private final Map<String, List<Element>> childrenByName;
    private final String localName;
    private String content;

    Element(final String localName, final Attributes attributes) {
        this.attributes = new Data();
        for (int i = 0; i < attributes.getLength(); ++i) {
            this.attributes.writeString(attributes.getLocalName(i), attributes.getValue(i));
        }

        children = new ArrayList<Element>();
        childrenByName = new HashMap<String, List<Element>>();
        this.localName = localName;
    }

    void addChild(final Element element) {
        final String name = element.getLocalName();
        if (!childrenByName.containsKey(name)) {
            childrenByName.put(name, new ArrayList<Element>());
        }

        children.add(element);
        childrenByName.get(name).add(element);
    }

    boolean getBooleanAttribute(final String name, final boolean defaultValue) {
        final String value = attributes.readString(name);
        if (value == null) {
            return defaultValue;
        }
        else {
            return "1".equals(value);
        }
    }

    Color getColorAttribute(final String name, final Color defaultValue) {
        final String value = attributes.readString(name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return new Color(value);
        }
        catch (final IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    double getDoubleAttribute(final String name) {
        return attributes.readDouble(name);
    }

    double getDoubleAttribute(final String name, final double defaultValue) {
        return attributes.readDouble(name, defaultValue);
    }

    int getIntAttribute(final String name) {
        return attributes.readInt(name);
    }

    String getStringAttribute(final String name) {
        return attributes.readString(name);
    }

    Element getChild(final String name) {
        if (childrenByName.containsKey(name)) {
            return Collections.unmodifiableList(childrenByName.get(name)).get(0);
        }
        else {
            return null;
        }
    }

    List<Element> getChildren() {
        return Collections.unmodifiableList(children);
    }

    List<Element> getChildren(final String name) {
        if (childrenByName.containsKey(name)) {
            return Collections.unmodifiableList(childrenByName.get(name));
        }
        else {
            // Unchecked conversion
            return Collections.EMPTY_LIST;
        }
    }

    public String getContent() {
        return content;
    }

    String getLocalName() {
        return localName;
    }

    boolean hasAttribute(final String name) {
        return attributes.hasValue(name);
    }

    boolean hasChild(final String name) {
        return childrenByName.containsKey(name);
    }

    boolean is(final String name) {
        return localName.equals(name);
    }

    /**
     * Parses a properties child of this element.
     *
     * @return the parsed properties
     */
    Data parsePropertiesChild() {
        final Data result = new Data();
        final Element propertiesElement = getChild(Const.PROPERTIES);
        if (propertiesElement != null) {
            for (final Element child : propertiesElement.getChildren(Const.PROPERTY)) {
                result.writeString(child.getStringAttribute(Const.NAME), child.getStringAttribute(Const.VALUE));
            }
        }

        return result;
    }

    void setContent(final String content) {
        this.content = content;
    }
}