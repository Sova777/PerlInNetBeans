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

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author valera
 */
public class PerlLogicalView implements LogicalViewProvider {

    private final PerlProject project;

    PerlLogicalView(PerlProject project) {
        this.project = project;
    }

    public Node createLogicalView() {
        try {

            //Get the sources directory, creating it if deleted:
            FileObject sources = project.getSourceFolder(true);

            //Get the DataObject that represents it:
            DataFolder sourcesDataObject =
                    DataFolder.findFolder (sources);

            //Get its default node: we'll wrap our node around it to change the
            //display name, icon, etc:
            Node sourcesFolderNode = sourcesDataObject.getNodeDelegate();

            //This FilterNode will be our project node:
            return new SourcesNode (sourcesFolderNode, project);

        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            //Fallback: the directory couldn't be created -
            //read-only filesystem or something evil happened:
            return new AbstractNode (Children.LEAF);
        }
    }

    public Node findPath(Node node, Object o) {
        //leave unimplemented for now:
        return null;
    }

    /** This is the node you actually see in the Projects window for the project */
    private static final class SourcesNode extends FilterNode {

        final PerlProject project;

        public SourcesNode (Node node, PerlProject project) throws DataObjectNotFoundException {
            super (node, new FilterNode.Children (node),
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both:
                    new ProxyLookup (new Lookup[] { Lookups.singleton(project),
                    node.getLookup() }));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean arg0) {
            Action[] nodeActions = new Action[7];
            nodeActions[0] = CommonProjectActions.newFileAction();
            nodeActions[1] = CommonProjectActions.copyProjectAction();
            nodeActions[2] = CommonProjectActions.deleteProjectAction();
            nodeActions[5] = CommonProjectActions.setAsMainProjectAction();
            nodeActions[6] = CommonProjectActions.closeProjectAction();
            return nodeActions;
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage (
                "org/languages/perl/project.png");
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }
    }

}
