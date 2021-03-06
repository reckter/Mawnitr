package rocks.reckt3r;

import me.reckter.telegram.Telegram;
import me.reckter.telegram.listener.OnCommand;
import me.reckter.telegram.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import rocks.reckt3r.model.Listener;
import rocks.reckt3r.model.Status;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;
import rocks.reckt3r.model.repository.ListenerRepository;
import rocks.reckt3r.model.repository.WatcherRepository;
import rocks.reckt3r.model.service.ListenerService;
import rocks.reckt3r.model.service.UserService;
import rocks.reckt3r.model.service.WatcherService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hannes on 18.02.16.
 */
@Service
public class BotListener implements CommandLineRunner {


    @Autowired
    UserService userService;

    @Autowired
    WatcherRepository watcherRepository;

    @Autowired
    WatcherService watcherService;

    @Autowired
    ListenerRepository listenerRepository;

    @Autowired
    ListenerService listenerService;


    @Autowired
    Telegram telegram;

    @Override
    public void run(String... args) throws Exception {
        telegram.addListener(this);
        telegram.addListener(watcherService);
        telegram.addListener(listenerService);
    }


    @OnCommand("detail")
    public void details(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 2) {
            message.reply("You must specify which watcher/listener you want details about.");
            return;
        }

        Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(1));
        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(1));
        if(watcher == null && listener == null) {
            message.reply("Could not find Watcher/listener " + arguments.get(1));
            return;
        }

        String out;
        if(watcher != null) {
            out = "watcher: " + watcher.getName() + "\n" +
                    "Status: " + watcher.getStatus().value() + "\n" +
                    "url: " + watcher.getUrlToWatch() + "\n" +
                    "HTTP Status to expect: " + watcher.getExpectedStatus().value() + "\n" +
                    "Checking interval: " + watcher.getSecondsBetweenChecks() / 60 + "m\n" +
                    "last success: " + ((new Date()).getTime() - watcher.getLastSuccessAt().getTime()) / 1000 + "s ago.\n" +
                    "message: " + watcher.getLastMessage();
        } else {
            out = "listener; " + listener.getName() + "\n" +
                    "Status: " + listener.getStatus().value() + "\n" +
                    "url: " + listenerService.getUrlForListener(listener) + "\n" +
                    "Checking interval: " + listener.getSecondsBetweenChecks() / 60 + "m\n" +
                    "last success: " + ((new Date()).getTime() - listener.getLastCalled().getTime()) / 1000 + "s ago.\n" +
                    "message: " + listener.getLastMessage();
        }
        message.respond(out);
    }

    @OnCommand({"start", "help"})
    public void help(Message message, List<String> arguments) {
        String out = "This is a Server Mawnitr.\n" +
                "It mawnitrs your servers.\n\n" +
                "commands:\n" +
                "/status - shows the status of all your watchers and listeners\n\n" +
                "watcher commands:\n" +
                "/watch <name> <url> - adds a new watcher. I will watch the url for uptime.\n" +
                "/detail <name> - shows the details of one watcher\n" +
                "/delete <name> - deletes a watcher\n" +
                "/set_url <name> <url> - set the url of a watcher\n" +
                "/set_name <name> <newName> - set the name of a watcher\n" +
                "/set_interval <name> <interval> - sets the Interval of a watcher (in minutes!)\n" +
                "/set_status <name> <statusCode> - set the Status code to expect of a watcher\n" +
                "/check <name> - checks the given watcher manually\n\n" +
                "listner commands:\n" +
                "/listen <name>  - adds a new listener. I will expect a GET request to the url in every interval.\n" +
                "/detail <name> - shows the details of one listener\n" +
                "/delete <name> - deletes a listener\n" +
                "/set_name <name> <newName> - set the name of a listener\n" +
                "/set_interval <name> <interval> - sets the Interval of a listener (in minutes!)\n" +
                "/token <name> - creates a new token for a listener\n";
        message.respond(out);
    }


    @OnCommand("set")
    public void set(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 4) {

        }
        Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(2));
        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(2));
        if(watcher == null && listener == null) {
            message.reply("You have no listener/watcher with the name " + arguments.get(2));
        }
        if(watcher != null) {
            watcherService.set(message, arguments);
        } else {
            listenerService.set(message, arguments);
        }
    }

    @OnCommand("delete")
    public void delete(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 2) {
            message.reply("Please supply the name of the watcher/listener you want to delete.");
            return;
        }

        Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(1));
        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(1));
        if(watcher == null && listener == null) {
            message.reply("Could not delete " + arguments.get(1) + " because there is no watcher/listener with this name");
            return;
        }
        if(watcher != null) {
            watcherRepository.delete(watcher);
            message.respond("Watcher " + watcher.getName() + " deleted");
        } else {
            listenerRepository.delete(listener);
            message.respond("listener " + listener.getName() + " deleted");
        }
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
        List<Listener> listeners = listenerRepository.findAllByUser(user);
        if(watchers.size() == 0 && listeners.size() == 0) {
            message.respond("No watchers/listeners yet.");
        } else {
            StringBuilder out = new StringBuilder();
            if(watchers.size() > 0) {
                out.append("\nwatchers:\n");
                watchers.forEach(watcher -> {
                    out.append(watcher.getName()).append(": ").append(watcher.getStatus().value());
                    if(watcher.getStatus() == Status.OFFLINE) {
                        out.append(" last success ")
                                .append((new Date().getTime() - watcher.getLastSuccessAt().getTime()) / 1000)
                                .append("s ago\n");
                    }
                    out.append("\n");
                });
            }
            if(listeners.size() > 0) {
                out.append("\nlistener:\n");
                listeners.forEach(listener -> {
                    out.append(listener.getName()).append(": ").append(listener.getStatus().value());
                    if(listener.getStatus() == Status.OFFLINE) {
                        out.append(" last called ")
                                .append((new Date().getTime() - listener.getLastCalled().getTime()) / 1000)
                                .append("s ago\n");
                    }
                });
            }
            message.respond(out.toString());
        }
    }

    @OnCommand("alert")
    public void alert(Message message, List<String> arguments) {
        if(message.chat.id == Integer.parseInt(System.getenv("ADMIN_ACC"))) {
            String text = arguments.subList(1, arguments.size()).stream().collect(Collectors.joining(" "));
            userService.findAll().forEach(user -> telegram.sendMessage(user.getTelegramId(), text));
        }
    }


}
