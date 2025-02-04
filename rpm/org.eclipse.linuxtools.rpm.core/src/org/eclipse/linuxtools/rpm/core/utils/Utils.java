/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.FrameworkUtil;
/**
 * Utilities for calling system executables.
 */
public class Utils {

    /**
     * Runs the given command and parameters.
     *
     * @param command
     *            The command with all parameters.
     * @return Stream containing the combined content of stderr and stdout.
     * @throws IOException
     *             If IOException occurs.
     */
	public static BufferedProcessInputStream runCommandToInputStream(String... command) throws IOException {
        return runCommandToInputStream(null, command);
    }

    /**
     * Runs the given command and parameters.
     * @param project rpm project
     *
     * @param command
     *            The command with all parameters.
     * @return Stream containing the combined content of stderr and stdout.
     * @throws IOException
     *             If IOException occurs.
     * @since 2.1
     */
	private static BufferedProcessInputStream runCommandToInputStream(IProject project, String... command)
			throws IOException {
        Process p = RuntimeProcessFactory.getFactory().exec(command, project);
        return new BufferedProcessInputStream(p);
    }

    /**
     * Runs the given command and parameters. Note that the command's process can
     * be terminated by interrupting the thread this method runs in, but only after
     * the process' execution has begun.
     *
     * @param outStream
     *            The stream to write the output to.
     * @param project
     *               The project which is executing this command.
     * @param command
     *            The command with all parameters.
     * @return An IStatus indicating the result of the command.
     * @throws IOException If an IOException occurs.
     * @since 1.1
     */
	public static IStatus runCommand(final OutputStream outStream, IProject project, String... command)
			throws IOException {
        return watchProcess(outStream, RuntimeProcessFactory.getFactory().exec(command, project));
    }

    /**
     * Watches the streams of the given running process. Note that the process can
     * be terminated at any time by interrupting the thread this method runs in.
     *
     * @param outStream
     *            The stream to write the output to.
     * @param child
     *            The process to watch the output of.
     * @return An IStatus indicating the result of the command.
     * @since 2.2
     */
    public static IStatus watchProcess(final OutputStream outStream, Process child) {
		final BufferedInputStream in = new BufferedInputStream(
				new SequenceInputStream(child.getInputStream(), child.getErrorStream()));

        Thread readinJob = new Thread(() -> {
		    try {
		        int i;
		        while ((i = in.read()) != -1) {
		            outStream.write(i);
		        }
		        outStream.flush();
		        outStream.close();
		        in.close();
		    } catch (IOException e) {}
		});
        readinJob.start();

        boolean canceled = false;
        try {
            // Catch interrupt attempts made before the wait
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            child.waitFor();
        } catch (InterruptedException e) {
            child.destroy();
            canceled = true;
        }

        while (readinJob.isAlive()) {
            try {
                readinJob.join();
            } catch (InterruptedException e) {}
        }

        if (canceled) {
            return Status.CANCEL_STATUS;
        }
		if (child.exitValue() != 0) {
			return new Status(IStatus.ERROR, FrameworkUtil.getBundle(Utils.class).getSymbolicName(),
					NLS.bind(Messages.Utils_NON_ZERO_RETURN_CODE, child.exitValue()), null);
		}
        return Status.OK_STATUS;
    }

    /**
     * Run a command and return its output.
     * @param command The command to execute.
     * @return The output of the executed command.
     * @throws IOException If an I/O exception occurred.
     */
	public static String runCommandToString(String... command) throws IOException {
        return runCommandToString(null, command);
    }

    /**
     * Run a command and return its output.
     * @param project rpm Project
     * @param command The command to execute.
     * @return The output of the executed command.
     * @throws IOException If an I/O exception occurred.
     * @since 2.1
     */
	public static String runCommandToString(IProject project, String... command) throws IOException {
        BufferedInputStream in = runCommandToInputStream(project, command);
        return inputStreamToString(in);
    }
    /**
     * Reads the content of the given InputStream and returns its textual
     * representation.
     *
     * @param stream
     *            The InputStream to read.
     * @return Textual content of the stream.
     * @throws IOException If an IOException occurs.
     */
	private static String inputStreamToString(InputStream stream) throws IOException {
        StringBuilder retStr = new StringBuilder();
        int c;
        while ((c = stream.read()) != -1) {
            retStr.append((char) c);
        }
        stream.close();
        return retStr.toString();
    }
}
