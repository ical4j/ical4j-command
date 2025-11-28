package org.ical4j.command;

import org.ical4j.command.channel.ReceiveCalendarCommand;
import org.ical4j.command.channel.ReceiveVCardCommand;
import org.ical4j.command.channel.SendCalendarCommand;
import org.ical4j.command.channel.SendVCardCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "channel", description = "Integration channel operations",
        subcommands = {ReceiveCalendarCommand.class, SendCalendarCommand.class,
                SendVCardCommand.class, ReceiveVCardCommand.class})
public class ChannelCommandGroup {

}
