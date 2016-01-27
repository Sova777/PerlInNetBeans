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

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author sova
 */
public class RunAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        try {
//            String text = null;
//            NotifyDescriptor.InputLine input =
//                    new NotifyDescriptor.InputLine("Arguments:", "Set Arguments");
//            if (DialogDisplayer.getDefault().notify(input) == NotifyDescriptor.OK_OPTION) {
//                text = input.getInputText();
//            } else {
//                return;
//            }
//            String[] cmd = new String[3];
//            Node n = activatedNodes[0];
//            FileObject fo = n.getLookup().lookup(FileObject.class);
//            File file = FileUtil.toFile(fo);
//            String path = file.getAbsolutePath();
//            File workDir = file.getParentFile();
//            cmd[0] = "/bin/sh";
//            cmd[1] = "-c";
//            cmd[2] = "gnome-terminal -x perl" + " " + path + " " + text;
            Node n = activatedNodes[0];
            FileObject fo = n.getLookup().lookup(FileObject.class);
            File file = FileUtil.toFile(fo);
            String path = file.getAbsolutePath();
            File workDir = file.getParentFile();
            String[] cmd = new String[]{"perl", path};
            launchProcess(cmd, workDir);
        } catch (IOException ex) {
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
/*
    public void launchProcess(String[] cmdline, File workDir) throws IOException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Cannot be called from EDT");
        }
        final Process process = Runtime.getRuntime().exec(cmdline, null, workDir);

        InputOutput io = IOProvider.getDefault().getIO("Perl", false);

        io.select();
        io.getErr().println("Perl script started.");
        String command = cmdline[0];
        for (int i = 1; i < cmdline.length; i++) {
            command = command + " " + cmdline[i];
        }
        io.getOut().println(command);
        io.getOut().println();

        try {
            //Hang this thread until the process exits
            process.waitFor();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        io.getErr().println("\nPerl script finished.");
        if (process.exitValue() == 0) {
            StatusDisplayer.getDefault().setStatusText("Program finished successful.");
        } else {
            //Get a localized string
            StatusDisplayer.getDefault().setStatusText("Program failed.");
        }
        io.getOut().println();        
    }
 */
    public void launchProcess(String[] cmdline, File workDir) throws IOException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Cannot be called from EDT");
        }
        final Process process = Runtime.getRuntime().exec(cmdline, null, workDir);

        //Get the standard out
        InputStream out = new BufferedInputStream(process.getInputStream(), 8192);
        //Get the standard in
        InputStream err = new BufferedInputStream(process.getErrorStream(), 8192);

        OutputStream out_stream = new BufferedOutputStream(process.getOutputStream(), 8192);

        //Create readers for each
        final Reader outReader = new BufferedReader(new InputStreamReader(out));
        final Reader errReader = new BufferedReader(new InputStreamReader(err));

        InputOutput io = IOProvider.getDefault().getIO("Perl", false);
        Reader inReader = io.getIn();

        io.select();
        io.getErr().println("Perl script started.");
        String command = cmdline[0];
        for (int i = 1; i < cmdline.length; i++) {
            command = command + " " + cmdline[i];
        }
        io.getOut().println(command);
        io.getOut().println();

        OutHandler processSystemOut = new OutHandler(outReader, io.getOut());
        OutHandler processSystemErr = new OutHandler(errReader, io.getErr());
//        InputHandler processSystemIn = new InputHandler(io, out_stream);

        //Get two different threads listening on the output & err
        RequestProcessor.getDefault().post(processSystemOut);
        RequestProcessor.getDefault().post(processSystemErr);
//        RequestProcessor.getDefault().post(processSystemIn);

        try {
            //Hang this thread until the process exits
            process.waitFor();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
//        processSystemIn.stopInput();
        inReader.close();
        processSystemOut.close();
        processSystemErr.close();
        io.getErr().println("\nPerl script finished.");
        if (process.exitValue() == 0) {
            StatusDisplayer.getDefault().setStatusText("Program finished successful.");
        } else {
            //Get a localized string
            StatusDisplayer.getDefault().setStatusText("Program failed.");
        }
        io.getOut().println();
    }

    
    //The runnable that will handle an output stream, piping it to the output window:
    static class OutHandler implements Runnable {

        private Reader out;
        private OutputWriter writer;

        public OutHandler(Reader out, OutputWriter writer) {
            this.out = out;
            this.writer = writer;
        }

        public void run() {
            while (true) {
                try {
                    while (!out.ready()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            close();
                            return;
                        }
                    }
                    if (!readOneBuffer() || Thread.currentThread().isInterrupted()) {
                        close();
                        return;
                    }
                } catch (IOException ioe) {
                    //Stream already closed, this is fine
                    return;
                }
            }
        }

        private boolean readOneBuffer() throws IOException {
            char[] cbuf = new char[255];
            int read;
            while ((read = out.read(cbuf)) != -1) {
                writer.write(cbuf, 0, read);
            }
            return read != -1;
        }

        private void close() {
            try {
                out.close();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            } finally {
                writer.close();
            }
        }
    }
/*
    static class InputHandler implements Runnable {

        private InputOutput inputOutput;
        private OutputStream str;
        private boolean stopIn = false;

        public InputHandler(InputOutput inputOutput, OutputStream out) {
            str = out;
            this.inputOutput = inputOutput;
        }

        public void stopInput() {
            stopIn = true;
        }

        public void run() {
            Reader in = inputOutput.getIn();
            try {
                while (true) {
                    int read = in.read();
//                    if (read == 13) {
//                        this.inputOutput.getOut().flush();
//                        this.inputOutput.getErr().flush();
//                        str.write(13);
//                    } else
                      if (read != -1) {
                        //this.inputOutput.getOut().flush();
                        //this.inputOutput.getErr().flush();
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        str.write(read);
                        //str.flush();
                    } else {
                        str.close();
                        return;
                    }
                    if (stopIn) {
                        return;
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
*/
}
