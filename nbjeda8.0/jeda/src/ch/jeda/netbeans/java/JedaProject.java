/*
 * Copyright (C) 2012 - 2015 by Stefan Rothe
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
package ch.jeda.netbeans.java;

import java.awt.Image;
import java.io.IOException;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

class JedaProject {

    public static final String PROJECT_TYPE = "org-netbeans-modules-java-j2seproject";
    private static final Image ICON = ImageUtilities.loadImage("ch/jeda/netbeans/java/res/icon.png");
    private static final String BUILD_XML = "build.xml";
    private static final String BUILD_XML_RES = "ch/jeda/netbeans/java/res/build.xml";
    private static final String JEDA_PROPERTIES = "jeda.properties";
    private static final String JEDA_PROPERTIES_RES = "ch/jeda/netbeans/java/res/jeda.properties";
    private static final String JEDA_JAR = "libs/jeda.jar";
    private static final String JEDA_JAR_RES = "ch/jeda/netbeans/java/res/jeda.jar";
    private static final String LIBS = "libs";
    private static final String PROJECT_PROPERTIES = "nbproject/project.properties";
    private static final String PROJECT_PROPERTIES_RES = "ch/jeda/netbeans/java/res/project.properties";
    private static final String PROJECT_XML = "nbproject/project.xml";
    private static final String PROJECT_XML_RES = "ch/jeda/netbeans/java/res/project.xml";
    private static final String NBPROJECT = "nbproject";
    private static final String RES = "res";
    private static final String SRC_CH_JEDA_PROJECT = "src/ch/jeda/project";

    static Image annotateIcon(final Project project, final Image orig, final boolean openedNode) {
        if (isJedaProject(project)) {
            return ICON;
        }
        return orig;
    }

    static NodeList<?> createConfigNode(final Project project) {
        if (isJedaProject(project)) {
            try {
                return NodeFactorySupport.fixedNodeList(new Node(getJedaPropertiesFile(project), "Jeda Configuration"));
            }
            catch (final DataObjectNotFoundException ex) {
                // ignore
            }
        }

        return NodeFactorySupport.fixedNodeList();
    }

    static NodeList<?> createResourcesNode(final Project project) {
        final FileObject resources = project.getProjectDirectory().getFileObject(RES);
        if ((isJedaProject(project)) && (resources != null)) {
            try {
                return NodeFactorySupport.fixedNodeList(new Node(resources, "Resources"));
            }
            catch (final DataObjectNotFoundException ex) {
                // ignore
            }
        }

        return NodeFactorySupport.fixedNodeList();
    }

    static Lookup fixLookup(final Lookup lookup) {
        final Project project = lookup.lookup(Project.class);
        if (isJedaProject(project)) {
            return Lookups.fixed(new Object[]{new JedaProjectIconAnnotator(), new JedaProjectOpenedHook(project)});
        }
        else {
            return Lookups.fixed(new Object[0]);
        }
    }

    private static FileObject getJedaPropertiesFile(final Project project) {
        return project.getProjectDirectory().getFileObject(JEDA_PROPERTIES);
    }

    static void init(final FileObject projectDir) {
        FileHelper.addDir(projectDir, LIBS);
        FileHelper.addDir(projectDir, NBPROJECT);
        FileHelper.addDir(projectDir, RES);
        FileHelper.addDir(projectDir, SRC_CH_JEDA_PROJECT);
        FileHelper.addFile(projectDir, JEDA_PROPERTIES, JEDA_PROPERTIES_RES);
        FileHelper.addFile(projectDir, BUILD_XML, BUILD_XML_RES,
                           new FileFilter.BuildXmlFilter(projectDir.getName()));
        FileHelper.addFile(projectDir, PROJECT_PROPERTIES, PROJECT_PROPERTIES_RES,
                           new FileFilter.ProjectPropertiesFilter(projectDir.getName()));
        FileHelper.addFile(projectDir, PROJECT_XML, PROJECT_XML_RES,
                           new FileFilter.ProjectXmlFilter(projectDir.getName()));
        FileHelper.addFile(projectDir, JEDA_JAR, JEDA_JAR_RES);
    }

    private static boolean isJedaProject(final Project project) {
        return (getJedaPropertiesFile(project) != null) && (project.getProjectDirectory().getFileObject("nbproject") != null);
    }

    private static class JedaProjectOpenedHook extends ProjectOpenedHook {

        private final Project project;
        private final FileObject projectDir;

        JedaProjectOpenedHook(final Project project) {
            this.project = project;
            this.projectDir = project.getProjectDirectory();
        }

        @Override
        protected void projectClosed() {
        }

        @Override
        protected void projectOpened() {
            if (!FileHelper.exists(this.projectDir.getFileObject(JEDA_JAR))) {
                FileHelper.replaceFile(this.projectDir, PROJECT_PROPERTIES, PROJECT_PROPERTIES_RES,
                                       new FileFilter.ProjectPropertiesFilter(this.projectDir.getName()));
                FileHelper.replaceFile(this.projectDir, PROJECT_XML, PROJECT_XML_RES,
                                       new FileFilter.ProjectXmlFilter(this.projectDir.getName()));
                FileHelper.replaceFile(this.projectDir, BUILD_XML, BUILD_XML_RES,
                                       new FileFilter.BuildXmlFilter(this.projectDir.getName()));
            }

            if (this.isAutoUpdate()) {
                if (!FileHelper.replaceFile(this.projectDir, JEDA_JAR, JEDA_JAR_RES)) {
                    showError("Cannot replace jeda.jar");
                }
            }
        }

        private boolean isAutoUpdate() {
            final Properties properties = this.loadJedaProperties();
            return !"false".equals(properties.get("jeda.autoupdate"));
        }

        private Properties loadJedaProperties() {
            Properties result = new Properties();
            try {
                result.load(getJedaPropertiesFile(this.project).getInputStream());
            }
            catch (IOException ex) {
                // ignore
            }

            return result;
        }
    }

    private static class Node extends FilterNode {

        public Node(final FileObject fileObject, final String name) throws DataObjectNotFoundException {
            super(DataObject.find(fileObject).getNodeDelegate());
            this.setDisplayName(name);
        }
    }

    public static void showError(final String message) {
        final NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }
}