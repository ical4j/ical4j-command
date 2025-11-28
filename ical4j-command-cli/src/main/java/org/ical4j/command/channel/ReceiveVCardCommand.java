package org.ical4j.command.channel;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.integration.event.VCardListener;
import org.ical4j.integration.flow.VCardSubscriber;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "receive-card", description = "Receive a card from a channel")
public class ReceiveVCardCommand extends AbstractIngressChannelCommand<VCard, VCard> {

    private long timeout;

    private boolean autoExpunge;

    public ReceiveVCardCommand(Consumer<VCard> consumer) {
        super(consumer);
    }

    @Override
    public Integer call() {
        VCardSubscriber subscriber = new VCardSubscriber();
        subscriber.addVCardListener(new VCardListener() {
            @Override
            public void onUpdate(VCard vCard) {
                getOutputHandler().accept(vCard);
            }
        });
        getEndpoint().subscribe(subscriber);
        boolean success = getEndpoint().poll(timeout, autoExpunge);
        return success ? 0 : 1;
    }
}
