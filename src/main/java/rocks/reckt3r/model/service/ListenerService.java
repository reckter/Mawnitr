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
    UserService userService;

    @Autowired
    Telegram telegram;


    @OnCommand("listen")
    public void listener(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 3) {
            message.respond("You need to supply at least to arguments! See /help for help");
            return;
        }

        Listener listener = listenerRepository.findOneByUserAndName(user, arguments.get(1));
        if(listener != null) {
            message.respond("You already have a listener with that name. Please choose another name.");
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


    private String getUrlForListener(Listener listener) {
        return "http://reckt3r.rocks/listen/" + listener.getToken();
    }

    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void checkForListener() {
        listenerRepository.findAll().forEach(listener -> {
            if((new Date()).getTime() - listener.getLastCalled().getTime() > listener.getSecondsBetweenChecks() * 1000) {
                if(listener.getStatus() == Status.OFFLINE) {
                    if((new Date()).getTime() - listener.getLastWarned().getTime() > 20 * 60 * 1000) {
                        sendErrorMessage(listener);
                        listener.setLastWarned(new Date());
                    }
                } else {
                    sendErrorMessage(listener);
                    listener.setLastWarned(new Date());
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
}
