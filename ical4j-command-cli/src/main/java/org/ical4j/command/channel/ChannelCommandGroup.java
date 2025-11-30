package org.ical4j.command.channel;

import picocli.CommandLine;

@CommandLine.Command(name = "channel", description = "Integration channel operations",
        subcommands = {ReceiveCalendarCommand.class, SendCalendarCommand.class,
                SendVCardCommand.class, ReceiveVCardCommand.class})
public class ChannelCommandGroup {

}
