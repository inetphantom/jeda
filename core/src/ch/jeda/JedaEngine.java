/*
 * Copyright (C) 2012 - 2014 by Stefan Rothe
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

import ch.jeda.event.Event;
import ch.jeda.event.EventQueue;
import ch.jeda.event.SensorType;
import ch.jeda.event.TickEvent;
import ch.jeda.platform.CanvasImp;
import ch.jeda.platform.TypefaceImp;
import ch.jeda.platform.ImageImp;
import ch.jeda.platform.InputRequest;
import ch.jeda.platform.Platform;
import ch.jeda.platform.SelectionRequest;
import ch.jeda.platform.WindowImp;
import ch.jeda.platform.WindowRequest;
import ch.jeda.ui.WindowFeature;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.xml.sax.XMLReader;

class JedaEngine implements Platform.Callback, Runnable {

    private static final TypefaceImp EMPTY_TYPEFACE_IMP = new EmptyTypefaceImp();
    private static final String DEFAULT_IMAGE_PATH = "res:jeda/logo-64x64.png";
    private static final double DEFAULT_TICK_FREQUENCY = 60.0;
    private static final String JEDA_APPLICATION_PROPERTIES_FILE = "res/jeda.properties";
    private static final String JEDA_PLATFORM_PROPERTIES_FILE = "res/jeda/platform.properties";
    private static final String JEDA_SYSTEM_PROPERTIES_FILE = "res/jeda/system.properties";
    private final AudioManager audioManager;
    private final Object currentProgramLock;
    private final ImageImp defaultImageImp;
    private final EventQueue eventQueue;
    private final FrequencyMeter frequencyMeter;
    private final Object pauseLock;
    private final Platform platform;
    private final ProgramClassWrapper[] programClasses;
    private final Properties properties;
    private final Timer timer;
    private JedaProgramExecutor currentProgram;
    private boolean paused;

    static JedaEngine create() {
        final JedaEngine result = new JedaEngine();
        final Thread eventThread = new Thread(result);
        eventThread.setName(Message.get(Message.ENGINE_EVENT_THREAD_NAME));
        eventThread.setDaemon(true);
        eventThread.start();
        return result;
    }

    JedaEngine() {
        this.currentProgramLock = new Object();
        this.eventQueue = new EventQueue();
        this.frequencyMeter = new FrequencyMeter();
        this.pauseLock = new Object();
        this.timer = new Timer(DEFAULT_TICK_FREQUENCY);
        // Load properties
        this.properties = initProperties();
        // Init platform
        this.platform = initPlatform(this.properties.getString("jeda.platform.class"), this);
        // Init audio manager
        this.audioManager = new AudioManager(this.platform.getAudioManagerImp());
        // Load default image
        this.defaultImageImp = this.platform.createImageImp(DEFAULT_IMAGE_PATH);
        // Find Jeda programs and plugins
        final List<ProgramClassWrapper> programClassList = new ArrayList<ProgramClassWrapper>();
        try {
            final Class[] classes = this.platform.loadClasses();
            // Load jeda plugins and jeda programs
            for (int i = 0; i < classes.length; ++i) {
                final ProgramClassWrapper pcw = ProgramClassWrapper.tryCreate(classes[i], this.properties);
                if (pcw != null) {
                    programClassList.add(pcw);
                }
            }
        }
        catch (final Exception ex) {
            Log.err(ex, Message.ENGINE_ERROR_INIT_CLASSES);
        }

        this.programClasses = programClassList.toArray(new ProgramClassWrapper[programClassList.size()]);
        this.paused = false;
    }

    @Override
    public void pause() {
        synchronized (this.pauseLock) {
            this.paused = true;
        }
    }

    @Override
    public void postEvent(final Event event) {
        this.eventQueue.addEvent(event);
    }

    @Override
    public void resume() {
        synchronized (this.pauseLock) {
            if (this.paused) {
                this.paused = false;
                this.timer.start();
            }
        }
    }

    @Override
    public void run() {
        this.timer.start();
        while (true) {
            // Application is paused
            if (this.isPaused()) {
                try {
                    Thread.sleep(100);
                }
                catch (final InterruptedException ex) {
                    // ignore
                }
            }
            // Application is running
            else {
                this.frequencyMeter.count();
                final TickEvent event = new TickEvent(this, this.timer.getLastStepDuration(),
                                                      this.frequencyMeter.getFrequency());
                this.eventQueue.addEvent(event);
                this.eventQueue.processEvents();
                this.timer.tick();
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this.currentProgramLock) {
            if (this.currentProgram != null) {
                this.currentProgram.stop();
            }
            else {
                this.platform.shutdown();
            }
        }
    }

    void addEventListener(final Object listener) {
        this.eventQueue.addListener(listener);
    }

    CanvasImp createCanvasImp(final int width, final int height) {
        return this.platform.createCanvasImp(width, height);
    }

    TypefaceImp createTypefaceImp(final String path) {
        if (path == null) {
            return EMPTY_TYPEFACE_IMP;
        }

        final TypefaceImp result = this.platform.createTypefaceImp(path);
        if (result == null) {
            return EMPTY_TYPEFACE_IMP;
        }
        else {
            return result;
        }
    }

    ImageImp createImageImp(final String path) {
        if (path == null) {
            return this.defaultImageImp;
        }

        final ImageImp result = this.platform.createImageImp(path);
        if (result == null) {
            return this.defaultImageImp;
        }
        else {
            return result;
        }
    }

    WindowImp createWindowImp(final int width, final int height, final EnumSet<WindowFeature> features) {
        if (features == null) {
            throw new NullPointerException("features");
        }

        final WindowRequest request = new WindowRequest(width, height, features);
        this.platform.showWindow(request);
        request.waitForResult();
        return request.getResult();
    }

    XMLReader createXmlReader() {
        return this.platform.createXmlReader();
    }

    AudioManager getAudioManager() {
        return this.audioManager;
    }

    String getProgramName() {
        synchronized (this.currentProgramLock) {
            if (this.currentProgram != null) {
                return this.currentProgram.getProgramName();
            }
            else {
                return null;
            }
        }
    }

    ProgramClassWrapper[] getProgramClasses() {
        return this.programClasses;
    }

    Properties getProperties() {
        return this.properties;
    }

    TypefaceImp getStandardTypefaceImp(final Platform.StandardTypeface standardTypeface) {
        final TypefaceImp result = this.platform.getStandardTypefaceImp(standardTypeface);
        if (result == null) {
            return EMPTY_TYPEFACE_IMP;
        }
        else {
            return result;
        }
    }

    double getTickFrequency() {
        return this.timer.getTargetFrequency();
    }

    boolean isSensorAvailable(final SensorType sensorType) {
        return this.platform.isSensorAvailable(sensorType);
    }

    boolean isSensorEnabled(final SensorType sensorType) {
        return this.platform.isSensorEnabled(sensorType);
    }

    boolean isVirtualKeyboardVisible() {
        return this.platform.isVirtualKeyboardVisible();
    }

    void log(final LogLevel logLevel, final String message) {
        if (this.platform == null) {
            System.err.print(message);
        }
        else {
            this.platform.log(logLevel, message);
        }
    }

    InputStream openResource(final String path) {
        return this.platform.openResource(path);
    }

    void programTerminated() {
        synchronized (this.currentProgramLock) {
            this.currentProgram = null;
            this.platform.shutdown();
        }
    }

    void removeEventListener(final Object listener) {
        this.eventQueue.removeListener(listener);
    }

    void showInputRequest(final InputRequest request) {
        this.platform.showInputRequest(request);
    }

    void showSelectionRequest(final SelectionRequest request) {
        this.platform.showSelectionRequest(request);
    }

    void setSensorEnabled(final SensorType sensorType, final boolean enabled) {
        this.platform.setSensorEnabled(sensorType, enabled);
    }

    void setTickFrequency(final double hertz) {
        this.timer.setTargetFrequency(hertz);
    }

    void setVirtualKeyboardVisible(final boolean visible) {
        this.platform.setVirtualKeyboardVisible(visible);
    }

    void startProgram(final String programClassName) {
        synchronized (this.currentProgramLock) {
            if (this.currentProgram != null) {
                Log.err(Message.PROGRAM_ERROR_ALREADY_RUNNING);
            }
            else {
                this.currentProgram = new JedaProgramExecutor(this, programClassName);
                final Thread programThread = new Thread(this.currentProgram);
                programThread.setName(Message.get(Message.ENGINE_PROGRAM_THREAD_NAME));
                programThread.start();
            }
        }
    }

    private boolean isPaused() {
        synchronized (this.pauseLock) {
            return this.paused;
        }
    }

    private static Platform initPlatform(final String platformClassName, final Platform.Callback callback) {
        if (platformClassName == null || platformClassName.isEmpty()) {
            initErr(Message.ENGINE_ERROR_PLATFORM_MISSING_CLASS_NAME);
            return null;
        }

        try {
            final Class<?> clazz = JedaEngine.class.getClassLoader().loadClass(platformClassName);
            final Constructor<?> ctor = clazz.getConstructor(Platform.Callback.class);
            ctor.setAccessible(true);
            final Object result = ctor.newInstance(callback);
            if (result instanceof Platform) {
                return (Platform) ctor.newInstance(callback);
            }
            else {
                initErr(Message.ENGINE_ERROR_PLATFORM_MISSING_INTERFACE, platformClassName, Platform.class);
                return null;
            }
        }
        catch (final ClassNotFoundException ex) {
            initErr(ex, Message.ENGINE_ERROR_PLATFORM_CLASS_NOT_FOUND, platformClassName);
            return null;
        }
        catch (final NoSuchMethodException ex) {
            initErr(ex, Message.ENGINE_ERROR_PLATFORM_CONSTRUCTOR_NOT_FOUND, platformClassName);
            return null;
        }
        catch (final InstantiationException ex) {
            initErr(ex.getCause(), Message.ENGINE_ERROR_PLATFORM_INSTANTIATION, platformClassName);
            return null;
        }
        catch (final IllegalAccessException ex) {
            initErr(ex.getCause(), Message.ENGINE_ERROR_PLATFORM_ACCESS, platformClassName);
            return null;
        }
        catch (final InvocationTargetException ex) {
            initErr(ex.getCause(), Message.ENGINE_ERROR_PLATFORM_CONSTRUCTOR, platformClassName);
            return null;
        }
    }

    private static Properties initProperties() {
        java.util.Properties result = new java.util.Properties();
        loadProperties(result, JEDA_SYSTEM_PROPERTIES_FILE);
        loadProperties(result, JEDA_PLATFORM_PROPERTIES_FILE);
        loadProperties(result, JEDA_APPLICATION_PROPERTIES_FILE);
        result.putAll(System.getProperties());
        return new Properties(result);
    }

    private static void loadProperties(final java.util.Properties properties, final String path) {
        final URL url = JedaEngine.class.getClassLoader().getResource(path);
        if (url == null) {
            Log.err(Message.ENGINE_ERROR_PROPERTIES_NOT_FOUND, path);
            return;
        }

        InputStream in = null;
        try {
            in = url.openStream();
            properties.load(in);
        }
        catch (final Exception ex) {
            Log.err(Message.ENGINE_ERROR_PROPERTIES_READ, path);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (final IOException ex) {
                    // ignore
                }
            }
        }
    }

    private static void initErr(final String messageKey, Object... args) {
        System.err.format(Message.get(messageKey), args);
        System.err.println();
    }

    private static void initErr(final Throwable throwable, final String messageKey, Object... args) {
        System.err.format(Message.get(messageKey), args);
        System.err.println();
        if (throwable != null) {
            System.err.println("  " + throwable);
            final StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (int i = 0; i < stackTrace.length; ++i) {
                System.err.println("   " + stackTrace[i].toString());
            }
        }
    }

    private static class EmptyTypefaceImp implements TypefaceImp {

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public String getName() {
            return "";
        }
    }
}
