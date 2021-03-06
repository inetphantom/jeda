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
package ch.jeda.platform.java;

import ch.jeda.Log;
import ch.jeda.platform.TypefaceImp;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.imageio.ImageIO;

class ResourceManager {

    private static final String HTTP_PREFIX = "http://";
    private static final String NEW_RESOURCE_PREFIX = "res:";
    private static final ResourceFinder RESOURCE_FINDER = new ResourceFinder();

    static Class<?>[] loadClasses()
        throws Exception {
        return RESOURCE_FINDER.loadClasses();
    }

    static TypefaceImp loadTypeface(final String path) {
        final InputStream in = openInputStream(path);
        if (in == null) {
            return null;
        }

        try {
            return new JavaTypefaceImp(Font.createFont(Font.TRUETYPE_FONT, in));
        }
        catch (final IOException ex) {
            Log.e(ex, "Error while reading truetype font file '", path, "'.");
            return null;
        }
        catch (final FontFormatException ex) {
            Log.e(ex, "Invalid font format in truetype font file '", path, "'.");
            return null;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex) {
            }
        }
    }

    static BufferedImage loadImage(final String path) {
        final InputStream in = openInputStream(path);
        if (in == null) {
            return null;
        }

        try {
            return ImageIO.read(in);
        }
        catch (Exception ex) {
            Log.e(ex, "Error while reading image file '", path, "'.");
            return null;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex) {
            }
        }
    }

    static InputStream openInputStream(final String path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        else if (path.startsWith(NEW_RESOURCE_PREFIX)) {
            return openResourceInputStream(path, NEW_RESOURCE_PREFIX.length());
        }
        else if (path.startsWith(HTTP_PREFIX)) {
            return openRemoteInputStream(path);
        }
        else {
            return openFileInputStream(path);
        }
    }

    private static InputStream openFileInputStream(final String path) {
        try {
            return new FileInputStream(path);
        }
        catch (FileNotFoundException ex) {
            Log.e(ex, "File '", path, "' not found.");
        }

        return null;
    }

    private static InputStream openRemoteInputStream(final String path) {
        try {
            return new URL(path).openStream();
        }
        catch (MalformedURLException ex) {
            Log.e(ex, "Cannot open invalid path '", path, "'.");
            return null;
        }
        catch (IOException ex) {
            Log.e(ex, "Error while reading remote file '", path, "'.");
        }
        return null;
    }

    private static InputStream openResourceInputStream(final String path, final int prefixLength) {
        String resourcePath = path.substring(prefixLength);
        URL url = findResource("res/" + resourcePath);
        if (url == null) {
            url = findResource(resourcePath);
        }

        if (url == null) {
            Log.e("Resource file '", path, "' not found.");
            return null;
        }

        try {
            return url.openStream();
        }
        catch (IOException ex) {
            Log.e(ex, "Error while reading resource file '", path, "'.");
        }

        return null;
    }

    private static URL findResource(final String resourcePath) {
        URL result = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (result == null) {
            return Log.class.getClassLoader().getResource(resourcePath);
        }
        else {
            return result;
        }
    }

    private static class ResourceFinder {

        private final Set<Class<?>> classesSet;
        private Class<?>[] classes;

        ResourceFinder() {
            classesSet = new HashSet<Class<?>>();
        }

        Class<?>[] loadClasses() throws Exception {
            if (classes == null) {
                findResources();
            }

            return classes;
        }

        private void checkDirectory(final File directory, final String directoryName) {
            for (final File file : directory.listFiles()) {
                checkResource(file, directoryName);
            }
        }

        private void checkResource(final File file, final String directoryName) {
            if (!file.exists()) {
                return;
            }

            String fullName = file.getName();
            if (!directoryName.isEmpty()) {
                fullName = directoryName + '/' + fullName;
            }

            if (file.isDirectory()) {
                checkDirectory(file, fullName);
            }
            else {
                checkResource(fullName);
            }
        }

        private void checkResource(final String resourceName) {
            // Collect only global classes.
            // Inner classes have a $ in their name.
            if (resourceName.endsWith(".class") && !resourceName.contains("$")) {
                // Remove ".class" from the name
                String className = resourceName.substring(0, resourceName.length() - 6);
                // Convert path to packages
                className = className.replace("/", ".");
                try {
                    // Try to load class with system class loader
                    classesSet.add(ClassLoader.getSystemClassLoader().loadClass(className));
                }
                catch (ClassNotFoundException ex) {
                    try {
                        // Try to load class with class loader of current context
                        classesSet.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                    }
                    catch (ClassNotFoundException ex2) {
                        // Ignore
                    }
                }
            }
        }

        private void findResources() throws Exception {
            findResources(getClass().getProtectionDomain().getCodeSource().getLocation());
            final String[] classPaths = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String classPath : classPaths) {
                findResources(classPath);
            }

            classes = classesSet.toArray(new Class<?>[classesSet.size()]);
        }

        private void findResources(final String classPath) throws Exception {
            if (classPath.endsWith(".jar")) {
                try {
                    findJarResources(new URL(classPath).openStream());
                }
                catch (Exception ex) {
                    // No URL, so probably a file name.
                    findJarResources(new FileInputStream(classPath));
                }
            }
            else {
                checkDirectory(new File(classPath), "");
            }
        }

        private void findResources(final URL url) throws IOException {
            if (url.getFile().endsWith(".jar")) {
                findJarResources(url.openStream());
            }
        }

        private void findJarResources(final InputStream in) throws IOException {
            final JarInputStream jarStream = new JarInputStream(in);
            JarEntry element = jarStream.getNextJarEntry();
            while (element != null) {
                checkResource(element.getName());
                element = jarStream.getNextJarEntry();
            }
        }
    }
}
