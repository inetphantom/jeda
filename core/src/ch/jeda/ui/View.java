/*
 * Copyright (C) 2011 - 2015 by Stefan Rothe
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

import ch.jeda.Jeda;
import ch.jeda.JedaInternal;
import ch.jeda.event.Event;
import ch.jeda.event.EventQueue;
import ch.jeda.event.TickEvent;
import ch.jeda.event.TickListener;
import ch.jeda.platform.ViewImp;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a graphical view that can contains {@link ch.jeda.ui.Element}s.
 *
 * @since 2.0
 */
public class View {

    private static final int DEFAULT_HEIGHT = 600;
    private static final int DEFAULT_WIDTH = 800;
    private static final EnumSet<ViewFeature> IMP_CHANGING_FEATURES = initImpChangingFeatures();
    private final Object elementLock;
    private final Map<String, Set<Element>> elementsByName;
    private final Set<Element> elementSet;
    private final EventQueue eventQueue;
    private final Set<Element> pendingInsertions;
    private final Set<Element> pendingRemovals;
    private Canvas background;
    private Canvas canvas;
    private Element[] elements;
    private ViewImp imp;
    private String title;

    /**
     * Constructs a view. The view is shown on the screen.
     * <p>
     * The size of the view's drawing area depends on the platform:
     * </p><p>
     * <img src="../../../windows.png"> <img src="../../../linux.png"> The drawing area of the view has a width of 800
     * pixels and a height of 600 pixels.
     * </p><p>
     * <img src="../../../android.png"> The size drawing area depends on the screen size of the device.
     *
     * @since 2.0
     */
    public View() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructs a view. The view is shown on the screen. The specified features will be enabled for the view.
     * <p>
     * The size of the view's drawing area depends on the platform:
     * </p><p>
     * <img src="../../../windows.png"> <img src="../../../linux.png"> The drawing area of the view has a width of 800
     * pixels and a height of 600 pixels.
     * </p><p>
     * <img src="../../../android.png"> The size drawing area depends on the screen size of the device.
     *
     * @param features the features of the view
     *
     * @since 2.0
     */
    public View(final ViewFeature... features) {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, features);
    }

    /**
     * Constructs a view. The view is shown on the screen. The specified features will be enabled for the view.
     * <p>
     * The size of the view's drawing area depends on the platform:
     * </p><p>
     * <img src="../../../windows.png"> <img src="../../../linux.png"> The drawing area of the view has the specified
     * <tt>width</tt> and <tt>height</tt>.
     * </p><p>
     * <img src="../../../android.png"> The size drawing area depends on the screen size of the device.</p>
     *
     * @param width the width of the drawing area in pixels
     * @param height the height of the drawing area in pixels
     * @param features the features of the view
     * @throws IllegalArgumentException if width or height are smaller than 1
     *
     * @since 2.0
     */
    public View(final int width, final int height, final ViewFeature... features) {
        elementLock = new Object();
        elementsByName = new HashMap<String, Set<Element>>();
        elementSet = new HashSet<Element>();
        eventQueue = new EventQueue();
        pendingInsertions = new HashSet<Element>();
        pendingRemovals = new HashSet<Element>();
        elements = new Element[0];
        title = Jeda.getProgramName();
        resetImp(width, height, toSet(features));
        Jeda.addEventListener(eventQueue);
        Jeda.addEventListener(new EventLoop(this));
    }

    /**
     * Adds a {@link ch.jeda.ui.Element} to the currnt page of the view. Has no effect if <tt>element</tt>
     * is <tt>null</tt>. The {@link ch.jeda.ui.Element} becomes inactive (it no longer receives events) and insivible if
     * the current page changes.
     *
     * @param element the element to be added to the view
     *
     * @see #remove(ch.jeda.ui.Element)
     * @see #getElements()
     * @see #getElements(java.lang.Class)
     * @since 2.0
     */
    public final void add(final Element element) {
        if (element != null) {
            synchronized (elementLock) {
                if (elementSet.contains(element)) {
                    pendingRemovals.remove(element);
                }
                else {
                    pendingInsertions.add(element);
                }
            }
        }
    }

    public final void add(final Element... elements) {
        for (int i = 0; i < elements.length; ++i) {
            add(elements[i]);
        }
    }

    /**
     * Adds an event listener to the view. The specified object will receive events for all events listener interfaces
     * it implements. Has no effect if <tt>listener</tt> is <tt>null</tt> or an element of this view.
     *
     * @param listener the event listener
     *
     * @since 2.0
     */
    public final void addEventListener(final Object listener) {
        eventQueue.addListener(listener);
    }

    /**
     * Closes the view. The view becomes invalid, all subsequent method calls to the view will cause an error.
     *
     * @since 2.0
     */
    public final void close() {
        imp.close();
    }

    /**
     * Returns the background canvas of this view. The content of the background canvas is displayed behind all elements
     * of the view.
     *
     * @return the background canvas of this view
     *
     * @since 2.0
     */
    public final Canvas getBackground() {
        return background;
    }

    /**
     * Returns an element with the specified name. If more than one element with the specified name are managed by the
     * view, an arbitrary element with the name is returned.
     *
     * @param name the name to look for
     * @return an element with the specified name
     *
     * @since 2.0
     */
    public final Element getElement(final String name) {
        final Set<Element> result = elementsByName.get(name);
        if (result == null) {
            return null;
        }
        else {
            for (final Element element : result) {
                return element;
            }

            return null;
        }
    }

    /**
     * Returns an elements of the specified class with the specified name.
     *
     * @param <T> the type of the element to return
     * @param clazz the class of the element to return
     * @param name the name of the element to return
     * @return an element of the specified class with the specified name
     * @throws NullPointerException if <tt>clazz</tt> is <tt>null</tt>
     *
     * @see #add(ch.jeda.ui.Element)
     * @see #getElements()
     * @see #remove(ch.jeda.ui.Element)
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public final <T extends Element> T getElement(final Class<T> clazz, final String name) {
        final Set<Element> result = elementsByName.get(name);
        if (result == null) {
            return null;
        }
        else {
            for (final Element element : result) {
                if (clazz.isInstance(element)) {
                    // Unchecked cast
                    return (T) element;
                }
            }

            return null;
        }
    }

    /**
     * Returns all elements currently managed by the view.
     *
     * @return all elements currently managed by the view.
     *
     * @see #add(ch.jeda.ui.Element)
     * @see #getElements(java.lang.Class)
     * @see #remove(ch.jeda.ui.Element)
     * @since 2.0
     */
    public final Element[] getElements() {
        return Arrays.copyOf(elements, elements.length);
    }

    /**
     * Returns all elements of the specified class currently managed by the view.
     *
     * @param <T> the type of elements to return
     * @param clazz the class of elements to return
     * @return all elements currently managed by the view.
     * @throws NullPointerException if <tt>clazz</tt> is <tt>null</tt>
     *
     * @see #add(ch.jeda.ui.Element)
     * @see #getElements()
     * @see #remove(ch.jeda.ui.Element)
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public final <T extends Element> T[] getElements(final Class<T> clazz) {
        final List<T> result = new ArrayList<T>();
        for (int i = 0; i < elements.length; ++i) {
            if (clazz.isInstance(elements[i])) {
                // Unchecked cast
                result.add((T) elements[i]);
            }
        }

        // Unchecked cast
        return result.toArray((T[]) Array.newInstance(clazz, result.size()));
    }

    /**
     * Returns all elements with the specified name.
     *
     * @param name the name to look for
     * @return all elements with the specified name
     *
     * @since 2.0
     */
    public final Element[] getElements(final String name) {
        final Set<Element> result = elementsByName.get(name);
        if (result == null) {
            return new Element[0];
        }
        else {
            return result.toArray(new Element[result.size()]);
        }
    }

    /**
     * Returns the height of this view in pixels.
     *
     * @return the height of this view in pixels
     *
     * @since 2.0
     */
    public final int getHeight() {
        return imp.getCanvas().getHeight();
    }

    /**
     * Returns the current view title.
     *
     * @return current view title
     *
     * @see #setTitle(java.lang.String)
     * @since 2.0
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Returns the width of this view in pixels.
     *
     * @return the width of this view in pixels
     *
     * @since 2.0
     */
    public final int getWidth() {
        return imp.getCanvas().getWidth();
    }

    /**
     * Checks for a view feature. Returns <tt>true</tt> if the specified feature is currently enabled for the view.
     *
     * @param feature the feature to check for
     * @return <tt>true</tt> if the feature is enabled, otherwise returns
     * <tt>false</tt>
     * @throws NullPointerException if <tt>feature</tt> is <tt>null</tt>
     *
     * @see #setFeature(ch.jeda.ui.ViewFeature, boolean)
     * @since 2.0
     */
    public final boolean hasFeature(final ViewFeature feature) {
        if (feature == null) {
            throw new NullPointerException("feature");
        }

        return imp.getFeatures().contains(feature);
    }

    /**
     * Removes a {@link ch.jeda.ui.Element} from the view. Has no effect if <tt>element</tt> is
     * <tt>null</tt>.
     *
     * @param element the element to be removed from the view
     *
     * @see ch.jeda.ui.Element
     * @since 2.0
     */
    public final void remove(final Element element) {
        if (element != null) {
            synchronized (elementLock) {
                if (!elementSet.contains(element)) {
                    pendingInsertions.remove(element);
                }
                else {
                    pendingRemovals.add(element);
                }
            }
        }
    }

    public final void remove(final Element... elements) {
        for (int i = 0; i < elements.length; ++i) {
            remove(elements[i]);
        }
    }

    /**
     * Removes an event listener from the view. The specified object will not receive events anymore. Has no effect if
     * <tt>listener</tt> is <tt>null</tt> or an element of this view.
     *
     * @param listener the event listener
     * @since 2.0
     */
    public final void removeEventListener(final Object listener) {
        eventQueue.removeListener(listener);
    }

    /**
     * Enables or disables a view feature.
     *
     * @param feature the feature to be enabled or disabled
     * @param enabled <tt>true</tt> to enable the feature,
     * <tt>false</tt> to disable it
     * @throws NullPointerException if <tt>feature</tt> is <tt>null</tt>
     *
     * @see #hasFeature(ch.jeda.ui.ViewFeature)
     * @since 2.0
     */
    public final void setFeature(final ViewFeature feature, final boolean enabled) {
        if (feature == null) {
            throw new NullPointerException("feature");
        }

        if (IMP_CHANGING_FEATURES.contains(feature)) {
            final EnumSet<ViewFeature> featureSet = EnumSet.copyOf(imp.getFeatures());
            if (enabled) {
                featureSet.add(feature);
            }
            else {
                featureSet.remove(feature);
            }

            resetImp(getWidth(), getHeight(), featureSet);
        }
        else if (feature == ViewFeature.SCROLLABLE) {
            eventQueue.setDragEnabled(enabled);
        }
        else {
            imp.setFeature(feature, enabled);
        }
    }

    /**
     * Sets the shape of the mouse cursor.
     *
     * @param mouseCursor new shape of mouse cursor
     * @throws NullPointerException if <tt>mouseCursor</tt> is
     * <tt>null</tt>
     *
     * @see MouseCursor
     * @since 2.0
     */
    public final void setMouseCursor(final MouseCursor mouseCursor) {
        if (mouseCursor == null) {
            throw new NullPointerException("mouseCursor");
        }

        imp.setMouseCursor(mouseCursor);
    }

    /**
     * Sets the view title.
     *
     * @param title new title of the view
     * @throws NullPointerException if <tt>title</tt> is <tt>null</tt>
     *
     * @see #getTitle()
     * @since 2.0
     */
    public final void setTitle(final String title) {
        if (title == null) {
            throw new NullPointerException("title");
        }

        this.title = title;
        imp.setTitle(title);
    }

    /**
     * This method is invoked after an element has been added to the view.
     *
     * @param element the element that has been added
     *
     * @since 2.0
     */
    protected void elementAdded(final Element element) {
    }

    /**
     * This method is invoked after an element has been removed from the view.
     *
     * @param element the element that has been removed
     *
     * @since 2.0
     */
    protected void elementRemoved(final Element element) {
    }

    void addName(final Element element, final String name) {
        if (!elementsByName.containsKey(name)) {
            elementsByName.put(name, new HashSet<Element>());
        }

        elementsByName.get(name).add(element);
    }

    void drawOrderChanged(final Element element) {
        elements = null;
    }

    void removeName(final Element element, final String name) {
        if (elementsByName.containsKey(name)) {
            elementsByName.get(name).remove(element);
        }
    }

    void postEvent(final Event event) {
        eventQueue.addEvent(event);
    }

    private void tick(final TickEvent event) {
        if (imp.isVisible()) {
            updateElements();
            eventQueue.processEvents();
            canvas.drawCanvas(0, 0, background);
            for (int i = 0; i < elements.length; ++i) {
                elements[i].draw(canvas);
            }

            imp.update();
        }
    }

    private void resetImp(final int width, final int height, final EnumSet<ViewFeature> features) {
        if (imp != null) {
            imp.close();
        }

        imp = JedaInternal.createViewImp(width, height, features);
        imp.setEventQueue(eventQueue);
        imp.setTitle(title);
        eventQueue.setDragEnabled(features.contains(ViewFeature.SCROLLABLE));

        canvas = new Canvas(getWidth(), getHeight());
        canvas.setImp(imp.getCanvas());

        background = new Canvas(getWidth(), getHeight());
        background.setColor(Color.WHITE);
        background.fill();
        background.setColor(Color.BLACK);

    }

    private void updateElements() {
        synchronized (elementLock) {
            if (pendingInsertions.isEmpty() && pendingRemovals.isEmpty()) {
                return;
            }

            for (final Element element : pendingRemovals) {
                elementSet.remove(element);
                removeEventListener(element);
                element.removeFromView(this);
                removeName(element, element.getName());
                elementRemoved(element);
            }

            for (final Element element : pendingInsertions) {
                elementSet.add(element);
                addEventListener(element);
                element.addToView(this);
                addName(element, element.getName());
                elementAdded(element);
            }

            pendingInsertions.clear();
            pendingRemovals.clear();
            elements = elementSet.toArray(new Element[elementSet.size()]);
            Arrays.sort(elements, Element.DRAW_ORDER);
        }
    }

    private static EnumSet<ViewFeature> initImpChangingFeatures() {
        final EnumSet<ViewFeature> result = EnumSet.noneOf(ViewFeature.class
        );
        result.add(ViewFeature.DOUBLE_BUFFERED);

        result.add(ViewFeature.FULLSCREEN);
        return result;
    }

    private static EnumSet<ViewFeature> toSet(final ViewFeature... features) {
        final EnumSet<ViewFeature> result = EnumSet.noneOf(ViewFeature.class
        );
        for (final ViewFeature feature : features) {
            result.add(feature);
        }

        return result;
    }

    private static class EventLoop implements TickListener {

        private final View view;

        public EventLoop(final View view) {
            this.view = view;
        }

        @Override
        public void onTick(final TickEvent event) {
            view.tick(event);
        }
    }
}
