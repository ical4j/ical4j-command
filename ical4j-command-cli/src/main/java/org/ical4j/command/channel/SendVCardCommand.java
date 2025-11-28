package org.ical4j.command.channel;

import net.fortuna.ical4j.vcard.VCard;
import picocli.CommandLine;

@CommandLine.Command(name = "send-card", description = "Send a card on a channel")
public class SendVCardCommand extends AbstractEgressChannelCommand<VCard, Void> {

    private VCard card;

    public SendVCardCommand withCard(VCard card) {
        this.card = card;
        return this;
    }

    @Override
    public Integer call() {
        boolean success = getEndpoint().send(() -> card);
        return success ? 0 : 1;
    }
}
