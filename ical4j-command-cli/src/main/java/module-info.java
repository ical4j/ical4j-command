module ical4j.command {
    requires java.base;
    requires ical4j.command.core;
    requires ical4j.core;
    requires ical4j.vcard;
    requires ical4j.connector.api;

    requires info.picocli;
    requires ical4j.integration.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires ical4j.serializer;
    requires org.slf4j;
    requires org.apache.logging.log4j.core;
    requires ical4j.extensions;
    requires java.desktop;
    requires com.electronwill.nightconfig.core;

    exports org.ical4j.command;
    exports org.ical4j.command.calendar;
    exports org.ical4j.command.channel;
    exports org.ical4j.command.collection;
    exports org.ical4j.command.card;

    opens org.ical4j.command to info.picocli;
    opens org.ical4j.command.calendar to info.picocli;
    opens org.ical4j.command.channel to info.picocli;
    opens org.ical4j.command.collection to info.picocli;
    opens org.ical4j.command.card to info.picocli;
    exports org.ical4j.command.workspace;
    opens org.ical4j.command.workspace to info.picocli;
    exports org.ical4j.command.config;
    opens org.ical4j.command.config to info.picocli;
    exports org.ical4j.command.strategy;
    opens org.ical4j.command.strategy to info.picocli;
}