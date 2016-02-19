package rocks.reckt3r;

import me.reckter.telegram.Telegram;
import me.reckter.telegram.listener.OnCommand;
import me.reckter.telegram.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import rocks.reckt3r.model.Status;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;
import rocks.reckt3r.model.repository.WatcherRepository;
import rocks.reckt3r.model.service.UserService;
import rocks.reckt3r.model.service.WatcherService;

import java.util.Date;
import java.util.List;

/**
 * Created by hannes on 18.02.16.
 */
@Service
public class BotListener implements CommandLineRunner{


    @Autowired
    UserService userService;

    @Autowired
    WatcherRepository watcherRepository;

    @Autowired
    WatcherService watcherService;


    @Autowired
    Telegram telegram;

    @Override
    public void run(String... args) throws Exception {
        telegram.addListener(this);
        telegram.addListener(watcherService);
    }


    @OnCommand({"start", "help"})
    public void help(Message message, List<String> arguments) {
        String out = "This is a Server Mawnitr.\n" +
                "It mawnitrs your servers.\n\n" +
                "commands:\n" +
                "/status - shows the status of all your watchers \n" +
                "/watch <name> <url> [status] - adds a new watcher. I will watch the url for uptime.\n" +
                "/detail <name> - shows the details of one watcher\n" +
                "/delete <name> - deletes a watcher\n" +
                "/set_url <name> <url> - set the url of a watcher\n" +
                "/set_name <name> <newName> - set the name of a watcher\n" +
                "/set_interval <name> <interval> - sets the Interval of a watcher (in Minutes!)\n" +
                "/set_status <name> <statusCode> - set the Status code to expect of a watcher\n" +
                "/check <name> - checks the given watcher manually\n";
        message.respond(out);
    }

    @OnCommand({"list", "status"})
    public void list(Message message, List<String> arguments) {
        User user = null;
        try {
            user = userService.getOrCreate(message.chat.id);
        } catch(Exception e) {
            e.printStackTrace();
        }
        List<Watcher> watchers = watcherRepository.findAllByUser(user);
        if(watchers.size() == 0) {
            message.respond("No watchers yet.");
        } else {
            StringBuilder out = new StringBuilder();
            watchers.forEach(watcher -> {
                out.append(watcher.getName()).append(": ").append(watcher.getStatus().value());
                if(watcher.getStatus() == Status.OFFLINE) {
                    out.append(" last success ")
                            .append((new Date().getTime() - watcher.getLastSuccessAt().getTime()) / 1000)
                            .append("s ago");
                }
                out.append("\n");
            });
            message.respond(out.toString());
        }
    }


}
