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
package org.languages.perl.actions;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author sova
 */
public class RunAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node n = activatedNodes[0];
        FileObject fo = n.getLookup().lookup(FileObject.class);
        File file = FileUtil.toFile(fo);
        String path = file.getAbsolutePath();
        File workDir = file.getParentFile();
        StatusDisplayer.getDefault().setStatusText(file.getName() + " script started.");
        String[] cmd = new String[]{"perl", path};
        ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(true)
                .frontWindow(true).inputVisible(true).showProgress(true);
        ExecutionService exeService = ExecutionService.newService(
                new ProcessLaunch(cmd, workDir), descriptor, file.getName() + " (perl)"
        );
        Future<Integer> exitCode = exeService.run();
        try {
            if (exitCode.get() == 0) {
                StatusDisplayer.getDefault().setStatusText("Program finished successful.");
            } else {
                StatusDisplayer.getDefault().setStatusText("Program failed.");
            }
//            NotifyDescriptor.InputLine input =
//                    new NotifyDescriptor.InputLine("Arguments:", "Set Arguments");
//            if (DialogDisplayer.getDefault().notify(input) == NotifyDescriptor.OK_OPTION) {
//                text = input.getInputText();
//            } else {
//                return;
//            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Run";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    class ProcessLaunch implements Callable<Process> {

        private final String[] commandLine;
        private final File workDir;

        public ProcessLaunch(String[] commandLine, File workDir) {
            this.commandLine = commandLine;
            this.workDir = workDir;
        }

        @Override
        public Process call() throws Exception {
            ProcessBuilder pb = new ProcessBuilder(commandLine);
            pb.directory(workDir); //NOI18N
            pb.redirectErrorStream(true);
            return pb.start();
        }
    }

}
