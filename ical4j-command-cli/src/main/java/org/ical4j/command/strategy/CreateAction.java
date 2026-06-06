package org.ical4j.command.strategy;

import net.fortuna.ical4j.extensions.strategy.action.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.InputHandler;
import org.ical4j.command.config.CommandConfig;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.function.Consumer;

@CommandLine.Command(name = "action", description = "Generate an action item")
public class CreateAction extends AbstractCommand<Calendar> implements InputHandler {

    public enum ActionType {
        Action, Agenda, Backlog, Milestone, Project, Task
    }

    @CommandLine.Parameters(index = "0", description = "Object context specification", arity = "1", split = ":")
    private String[] spec;

//    @CommandLine.ArgGroup(multiplicity = "1")
//    private OutputOptions output;

    @CommandLine.Option(names = "--summary", arity = "0..1")
    private String summary;

    @CommandLine.Option(names = "--type", arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "Action")
    private ActionType type;

    @CommandLine.Option(names = "--edit", arity = "0..1")
    private boolean edit;

    public CreateAction() {
        super(DefaultOutputHandlers.VALIDATING_CALENDAR_PRINTER(new PrintWriter(System.out, true)));
    }

    public CreateAction(Consumer<Calendar> consumer) {
        super(consumer);
    }

    @Override
    public Integer call() throws Exception {
        String description = null;
        if (summary == null && System.console() != null) {
            summary = singleInput("Summary");
        }
        if (edit) {
            File temp = File.createTempFile("cal", ".md");
            ProcessBuilder pb = new ProcessBuilder("subl", "-w", temp.getAbsolutePath());
            Process p = pb.start();
            p.waitFor();
//            Desktop.getDesktop().edit(temp);
//            while (temp.length() == 0) {
//                Thread.sleep(1000);
//            }
            description = Files.readString(temp.toPath());
//            description = multiInput("Description");
        }
        if (type == null && System.console() != null) {
            type = ActionType.valueOf(singleInput("Type"));
        }
        VToDo action = new VToDo().add(new RandomUidGenerator().generateUid())
                .add(new Summary(summary));
        if (description != null) {
            action.add(new Description(description));
        }
        switch (type) {
            case Agenda -> {
                action = new Agenda().withPrototype(action).get();
            }
            case Backlog -> {
                action = new Backlog().withPrototype(action).get();
            }
            case Milestone -> {
                action = new Milestone().withPrototype(action).get();
            }
            case Project -> {
                action = new Project().withPrototype(action).get();
            }
            case Task -> {
                action = new Task().withPrototype(action).get();
            }
            default -> {
                action = new Action().withPrototype(action).get();
            }
        }
        Calendar calendar = new Calendar().withDefaults()
                .withProperty(new ProdId(CommandConfig.INSTANCE.getUserAgent()))
                .withComponent(action).getFluentTarget();
        getOutputHandler().accept(calendar);
        return 0;
    }
}
