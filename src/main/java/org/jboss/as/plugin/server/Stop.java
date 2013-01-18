/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.plugin.server;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.plugin.common.AbstractServerConnection;
import org.jboss.as.plugin.common.Operations;
import org.jboss.dmr.ModelNode;

/**
 * Stops the running standalone instance JBoss Application Server.
 * 
 * @author Morgany V. Mendes
 */
@Mojo(name = "stop")
public class Stop extends AbstractServerConnection {

    private static final String SHUTDOWN_CMD = ":shutdown";

    @Override
    public String goal() {
	return "stop";
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
	getLog().debug("Executing shutdown");
	synchronized (CLIENT_LOCK) {

	    final ModelControllerClient client = getClient();
	    try {
		doExecute(client);
	    } catch (IOException e) {
		throw new MojoFailureException(
		        "Could not execute shutdown command.", e);
	    } finally {
		close();
	    }
	}
    }

    private void doExecute(ModelControllerClient client) throws IOException {

	final CommandContext ctx = create();
	try {

	    final ModelNode result;
	    try {
		result = client.execute(ctx.buildRequest(SHUTDOWN_CMD));
	    } catch (CommandFormatException e) {
		throw new IllegalArgumentException(String.format(
		        "Command '%s' is invalid", SHUTDOWN_CMD), e);
	    }
	    if (!Operations.successful(result)) {
		throw new IllegalArgumentException(String.format(
		        "Command '%s' was unsuccessful. Reason: %s",
		        SHUTDOWN_CMD, Operations.getFailureDescription(result)));
	    }
	} finally {
	    ctx.terminateSession();
	}
    }

    /**
     * Creates the command context and binds the client to the context.
     * <p/>
     * If the client is {@code null}, no client is bound to the context.
     * 
     * @return the command line context
     * 
     * @throws IllegalStateException
     *             if the context fails to initialize
     */
    public static CommandContext create() {
	final CommandContext commandContext;
	try {
	    commandContext = CommandContextFactory.getInstance()
		    .newCommandContext();
	} catch (CliInitializationException e) {
	    throw new IllegalStateException("Failed to initialize CLI context",
		    e);
	}
	return commandContext;
    }
}
