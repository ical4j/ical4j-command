package org.ical4j.command.workspace;

import org.ical4j.command.collection.ListObjectUids;
import picocli.CommandLine;

@CommandLine.Command(name = "workspace", description = "Calendar and card workspace operations",
        subcommands = {GetCollectionDetails.class, ListCollections.class,
                CreateCollection.class, UpdateCollection.class,
                DeleteCollection.class, ListObjectUids.class,})
public class WorkspaceCommandGroup {

}
