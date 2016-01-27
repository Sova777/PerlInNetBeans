/*
* CDDL HEADER START
*
* The contents of this file are subject to the terms of the
* Common Development and Distribution License, Version 1.0 only
* (the "License"). You may not use this file except in compliance
* with the License.
*
* You can obtain a copy of the license at LICENSE.txt
* or http://netbeans.mojgorod.ru/perl_licensing.html.
* See the License for the specific language governing permissions
* and limitations under the License.
*
* When distributing Covered Code, include this CDDL HEADER in each
* file and include the License file at LICENSE.txt.
* If applicable, add the following below this CDDL HEADER, with the
* fields enclosed by brackets "[]" replaced with your own identifying
* information: Portions Copyright [yyyy] [name of copyright owner]
*
* CDDL HEADER END
*/

/*
* Copyright 2008-2012 Valeriy Soldatov. All rights reserved.
* Use is subject to license terms.
*/
package org.languages.perl.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author valera
 */
public class PerlProject implements Project {

    public static final String SOURCE_DIR = "";

    private final FileObject projectDir;
    LogicalViewProvider logicalView = new PerlLogicalView(this);
    private final ProjectState state;

    private Lookup lkp;

    public PerlProject(FileObject projectDir, ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
    }
    
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    FileObject getSourceFolder(boolean create) {
        FileObject result =
            projectDir.getFileObject(SOURCE_DIR);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(SOURCE_DIR);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[] {
                this,  //project spec requires a project be in its own lookup
                state, //allow outside code to mark the project as needing saving
                new ActionProviderImpl(), //Provides standard actions like Build and Clean
                loadProperties(), //The project properties
                new Info(), //Project information implementation
                logicalView, //Logical view of project implementation
            });
        }
        return lkp;
    }

    private Properties loadProperties() {
        FileObject fob = projectDir.getFileObject(PerlProjectFactory.PROJECT_DIR
                + "/" + PerlProjectFactory.PROJECT_PROPFILE);
        Properties properties = new NotifyProperties(state);
        if (fob != null) {
            try {
                properties.load(fob.getInputStream());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        return properties;
    }

    private static class NotifyProperties extends Properties {

        private final ProjectState state;

        NotifyProperties(ProjectState state) {
            this.state = state;
        }

        @Override
        public Object put(Object key, Object val) {
            Object result = super.put(key, val);
            if (((result == null) != (val == null)) || (result != null
                    && val != null && !val.equals(result))) {
                state.markModified();
            }
            return result;
        }
    }

    private final class ActionProviderImpl implements ActionProvider {
        public String[] getSupportedActions() {
            return new String[0];
        }

        public void invokeAction(String action, Lookup lookup) throws IllegalArgumentException {
            //do nothing
        }

        public boolean isActionEnabled(String action, Lookup lookup) throws IllegalArgumentException {
            return false;
        }
    }

    /** Implementation of project system's ProjectInformation class */
    private final class Info implements ProjectInformation {
        public Icon getIcon() {
            return new ImageIcon (ImageUtilities.loadImage(
                    "org/languages/perl/project.png"));
        }

        public String getName() {
            return getProjectDirectory().getName();
        }

        public String getDisplayName() {
            return getName();
        }

        public void addPropertyChangeListener (PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public void removePropertyChangeListener (PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        public Project getProject() {
            return PerlProject.this;
        }
    }

}
