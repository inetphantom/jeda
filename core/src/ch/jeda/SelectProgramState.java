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

import ch.jeda.platform.SelectionRequest;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class SelectProgramState extends EngineState {

    private static final String DEFAULT_PROGRAM_PROPERTY = "jeda.default.program";
    private static final Class<?>[] NO_PARAMS = new Class<?>[0];

    SelectProgramState(final Context context) {
        super(context);
    }

    @Override
    void onPause() {
        Engine.enterShutdownState();
    }

    @Override
    void onResume() {
    }

    @Override
    void onStop() {
    }

    @Override
    void run() {
        try {
            // Load all program classes
            final List<ProgramInfo> programInfos = new ArrayList<ProgramInfo>();
            ProgramInfo defaultProgram = null;
            final String defaultProgramName = this.context.getProperties().
                    getString(DEFAULT_PROGRAM_PROPERTY);
            for (String className : this.context.listClassNames()) {
                Class cls = loadClass(className);
                if (isProgramClass(cls)) {
                    ProgramInfo pi = this.createProgramInfo(cls);
                    programInfos.add(pi);
                    if (cls.getName().equals(defaultProgramName)) {
                        defaultProgram = pi;
                    }
                }
            }

            if (defaultProgram != null) {
                Engine.enterCreateProgramState(defaultProgram);
            }
            else if (programInfos.size() == 1) {
                Engine.enterCreateProgramState(programInfos.get(0));
            }
            else if (programInfos.isEmpty()) {
                this.logError(Message.NO_PROGRAM_ERROR);
                Engine.enterShutdownState();
            }
            else {
                final SelectionRequest<ProgramInfo> request = new SelectionRequest<ProgramInfo>();
                for (ProgramInfo programInfo : programInfos) {
                    request.addItem(programInfo.getName(), programInfo);
                }

                request.sortItemsByName();
                request.setTitle(Message.translate(Message.CHOOSE_PROGRAM_TITLE));
                this.context.showSelectionRequest(request);
                request.waitForResult();
                if (request.isCancelled()) {
                    Engine.enterShutdownState();
                }
                else {
                    Engine.enterCreateProgramState(request.getResult());
                }
            }
        }
        catch (final Exception ex) {
            this.logError(ex, Message.CHOOSE_PROGRAM_ERROR);
            Engine.enterShutdownState();
        }
    }

    private ProgramInfo createProgramInfo(final Class<Program> programClass) {
        String title = this.context.getProperties().getString(programClass.getName());
        if (title == null) {
            title = programClass.getSimpleName();
        }

        return new ProgramInfo(programClass, title);
    }

    private static boolean isProgramClass(final Class<?> cls) {
        if (!Program.class.isAssignableFrom(cls)) {
            return false;
        }

        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }

        try {
            int ctorModifiers = cls.getConstructor(NO_PARAMS).getModifiers();
            return Modifier.isPublic(ctorModifiers);
        }
        catch (final NoSuchMethodException ex) {
            return false;
        }
    }

    private static Class<?> loadClass(final String className) throws Exception {
        try {
            return Class.forName(className);
        }
        catch (final NoClassDefFoundError ex) {
            return Class.forName(className, false,
                                 Thread.currentThread().getContextClassLoader());
        }
        catch (final ExceptionInInitializerError ex) {
            return Class.forName(className, false,
                                 Thread.currentThread().getContextClassLoader());
        }
    }
}
