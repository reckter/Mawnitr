package rocks.reckt3r.model.service;

import me.reckter.telegram.Telegram;
import me.reckter.telegram.listener.OnCommand;
import me.reckter.telegram.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rocks.reckt3r.model.Listener;
import rocks.reckt3r.model.Status;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;
import rocks.reckt3r.model.repository.ListenerRepository;
import rocks.reckt3r.model.repository.UserRepository;
import rocks.reckt3r.model.repository.WatcherRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by hannes on 19.02.16.
 */
@Service
public class ListenerService {


    @Autowired
    ListenerRepository listenerRepository;

    @Autowired
    WatcherRepository watcherRepository;

    @Autowired
    UserService userService;

    @Autowired
    Telegram telegram;


    @OnCommand("token")
    public void token(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 2) {
            message.reply("You need to suppl a listener name to create a new token for");
        }

        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(1));
        listener.setToken(UUID.randomUUID().toString().replace("-", ""));
        listener = listenerRepository.save(listener);

        message.respond("new url: " + getUrlForListener(listener));
    }


    @OnCommand("listen")
    public void listener(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 2) {
            message.respond("You need to supply at least to arguments! See /help for help");
            return;
        }

        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(1));
        Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(1));
        if(listener != null) {
            message.respond("You already have a listener with that name. Please choose another name.");
            return;
        }
        if(watcher != null) {
            message.respond("You already have a watcher with that name. Please choose another name.");
            return;
        }

        listener = new Listener();
        listener.setUser(user);

        listener.setName(arguments.get(1));
        listener.setStatus(Status.ONLINE);
        listener.setToken(UUID.randomUUID().toString().replace("-", ""));


        listener.setSecondsBetweenChecks(60);
        listener.setLastWarned(new Date(0));
        listener.setLastCalled(new Date(0));

        listener = listenerRepository.save(listener);

        message.respond("listener " + listener.getName() + " created.\n" + "url for this listener: " + getUrlForListener(listener));

    }


    public String getUrlForListener(Listener listener) {
        return System.getenv("URL_BASE") + ":" + System.getenv("server.port") + "/listen/" + listener.getToken();
    }

    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void checkForListener() {
        listenerRepository.findAll().forEach(listener -> {
            if((new Date()).getTime() - listener.getLastCalled().getTime() > listener.getSecondsBetweenChecks() * 1000) {
                if(listener.getStatus() == Status.OFFLINE) {
                    if((new Date()).getTime() - listener.getLastWarned().getTime() > 20 * 60 * 1000) {
                        sendErrorMessage(listener);
                        listener.setStatus(Status.OFFLINE);
                        listener.setLastWarned(new Date());
                        listenerRepository.save(listener);
                    }
                } else {
                    sendErrorMessage(listener);
                    listener.setStatus(Status.OFFLINE);
                    listener.setLastWarned(new Date());
                    listenerRepository.save(listener);
                }
            } else {
                if(listener.getStatus() == Status.OFFLINE) {
                    sendIsOkAgainMessage(listener);
                    listener.setStatus(Status.OFFLINE);
                    listener.setLastWarned(new Date(0));
                    listenerRepository.save(listener);
                }
            }
        });


    }


    private void sendErrorMessage(Listener listener) {
        telegram.sendMessage(listener.getUser().getTelegramId(), "==== Server down ==== \n \nGot an error while checking " + listener.getName() + "\n" +
                "last successfull message: " + ((new Date().getTime() - listener.getLastCalled().getTime()) / 1000) + "s ago \n" +
                listener.getLastMessage());
    }

    private void sendIsOkAgainMessage(Listener listener) {
        telegram.sendMessage(listener.getUser().getTelegramId(), "==== Server ok again ====\n \n " + listener.getName() +
                " is ok again was \n down for " + ((new Date().getTime() - listener.getLastCalled().getTime()) / 1000) + "s\n");
    }

    public void set(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(2));
        if(listener == null) {
            message.reply("Could not find listener " + arguments.get(2));
            return;
        }
        switch(arguments.get(1)) {
            case "name":
                if(watcherRepository.findOneByUserAndName(user, arguments.get(3)) != null ||
                        listenerRepository.findOneByUserAndName(user, arguments.get(3)) != null     ) {
                    message.reply("You already have a listener/watcher with that name.");
                    return;
                }
                listener.setName(arguments.get(3));
                break;
            case "interval":
                try {
                    listener.setSecondsBetweenChecks(Integer.parseInt(arguments.get(3)) * 60);
                } catch(IllegalArgumentException e) {
                    message.respond("You must specify time in a number in minutes.");
                    return;
                }
                break;
            default:
                message.reply("could not set the option " + arguments.get(1) + " because this option does not exist.");
                return;
        }
        listener = listenerRepository.save(listener);
        message.respond("Set " + arguments.get(1) + " to " + arguments.get(3) + " of listener " + listener.getName());
    }
}
