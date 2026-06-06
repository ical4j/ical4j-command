package org.ical4j.command.workspace;

import picocli.CommandLine;

@CommandLine.Command(name = "workspace", description = "Manage collection workspaces",
        subcommands = {ListWorkspaces.class, ActivateWorkspace.class, PrintWorkspace.class})
public class WorkspaceCommandGroup {

}
