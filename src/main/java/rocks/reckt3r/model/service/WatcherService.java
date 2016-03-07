package rocks.reckt3r.model.service;

import me.reckter.telegram.Telegram;
import me.reckter.telegram.listener.OnCommand;
import me.reckter.telegram.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import rocks.reckt3r.model.Listener;
import rocks.reckt3r.model.Status;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;
import rocks.reckt3r.model.repository.ListenerRepository;
import rocks.reckt3r.model.repository.WatcherRepository;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hannes on 18.02.16.
 */
@Service
public class WatcherService {


    @Autowired
    WatcherRepository watcherRepository;

    @Autowired
    ListenerRepository listenerRepository;

    @Autowired
    Telegram telegram;

    @Autowired
    UserService userService;


    public void set(Message message, List<String> arguments) {
        if(arguments.size() < 4) {
            message.reply("Not enough parameters. Please use /help for help.");
            return;
        }

        User user = userService.getOrCreate(message.chat.id);

        Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(2));
        if(watcher == null) {
            message.reply("Could not find Watcher " + arguments.get(2));
            return;
        }
        switch(arguments.get(1)) {
            case "url":
                try {
                    watcher.setUrlToWatch(new URL(arguments.get(3)));
                } catch(MalformedURLException e) {
                    message.reply("please provide a valid url");
                    return;
                }
                break;
            case "name":
                if(watcherRepository.findOneByUserAndName(user, arguments.get(3)) != null ||
                    listenerRepository.findOneByUserAndName(user, arguments.get(3)) != null     ) {
                    message.reply("You allready have a listener/watcher with that name.");
                    return;
                }
                watcher.setName(arguments.get(3));
                break;
            case "status":
                try {
                    HttpStatus status = HttpStatus.valueOf(Integer.parseInt(arguments.get(3)));
                    watcher.setExpectedStatus(status);
                } catch(IllegalArgumentException e) {
                    message.respond("please supply a valid HTTP Status code.");
                    return;
                }
                break;
            case "interval":
                try {
                    watcher.setSecondsBetweenChecks(Integer.parseInt(arguments.get(3)) * 60);
                } catch(IllegalArgumentException e) {
                    message.respond("You must specify time in a number in minutes.");
                    return;
                }
                break;
            default:
                message.reply("could not set the option " + arguments.get(1) + " because this option does not exist.");
                return;
        }
        watcher =  watcherRepository.save(watcher);
        message.respond("Set " + arguments.get(1) + " to " + arguments.get(3) + " of watcher " + watcher.getName());
        checkWatcher(watcher);
    }


    @OnCommand("check")
    public void check(Message message, List<String> arguments) {
        System.out.println("creating bot: " + message.getText() + "/n" + arguments.stream().collect(Collectors.joining(", ")));
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() > 1) {
            for(int i = 1; i < arguments.size(); i++) {
                Watcher watcher = watcherRepository.findOneByUserAndName(user, arguments.get(i));
                if(watcher != null) {
                    checkWatcher(watcher);
                } else {
                    message.respond("Could not find watcher " + arguments.get(i));
                }
            }
        } else {
            watcherRepository.findAllByUser(user).forEach(this::checkWatcher);
        }
        message.respond("Checking in Progress.");

    }

    @OnCommand("watch")
    public void addWatcher(Message message, List<String> arguments) {
        User user = userService.getOrCreate(message.chat.id);

        if(arguments.size() < 3) {
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

        try {
            watcher = new Watcher();
            watcher.setUser(user);

            watcher.setName(arguments.get(1));
            watcher.setUrlToWatch(new URL(arguments.get(2)));
            watcher.setStatus(Status.ONLINE);

            if(arguments.size() > 3) {
                String statusCodeString = arguments.get(3);
                try {
                    HttpStatus status = HttpStatus.valueOf(Integer.parseInt(statusCodeString));
                    watcher.setExpectedStatus(status);
                } catch(IllegalArgumentException e) {
                    message.respond("please supply a valid HTTP Status code.");
                    return;
                }
            }

            watcher.setSecondsBetweenChecks(60);
            watcher.setExpectedStatus(HttpStatus.OK);
            watcher.setLastChecked(new Date());
            watcher.setLastSuccessAt(new Date());

            message.respond("watcher " + watcher.getName() + " created.");

            checkWatcher(watcherRepository.save(watcher));

        } catch(MalformedURLException e) {
            message.respond("You must input a valid url.");
        }
    }


    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 10 * 1000)
    public void watchForDowntimes() {

        watcherRepository.findAll().stream().filter(watcher -> new Date().getTime() - watcher.getLastChecked().getTime() >= (watcher.getSecondsBetweenChecks()) * 1000)
        .forEach(this::checkWatcher);

    }

    public void checkWatcher(Watcher watcher) {
        AsyncRestTemplate restTemplate = new AsyncRestTemplate();

        watcher.setLastChecked(new Date());
        watcher = watcherRepository.save(watcher);

        HttpEntity<String> entity = new HttpEntity<>("");

        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(watcher.getUrlToWatch().toString(), HttpMethod.GET, entity, String.class);

        final Watcher finalWatcher = watcher;
        future.addCallback(result -> {
            receivedCode(result.getStatusCode(), finalWatcher, result.getBody());
        }, ex -> {
            if(ex instanceof HttpStatusCodeException) {
                HttpStatusCodeException clientErrorException = (HttpStatusCodeException) ex;
                receivedCode(clientErrorException.getStatusCode(), finalWatcher, clientErrorException.getResponseBodyAsString());
            } else if(ex instanceof ConnectException) {

                finalWatcher.setLastChecked(new Date());
                finalWatcher.setLastMessage("Timeout");
                finalWatcher.setStatus(Status.OFFLINE);
                sendErrorMessage(finalWatcher);
                watcherRepository.save(finalWatcher);

            } else {
                telegram.sendMessage(finalWatcher.getUser().getTelegramId(), "Got an error trying to check " + finalWatcher.getName() + ": \n "
                        + ex.getClass().getName() + ": " + ex.getMessage());
            }
        });

    }


    private void sendErrorMessage(Watcher watcher) {
        if(watcher.getLastWarned() == null) {
            watcher.setLastWarned(new Date(0));
        }
        if(new Date().getTime() - watcher.getLastWarned().getTime() > 20 * 60 * 1000 || watcher.getStatus() == Status.ONLINE) {
            watcher.setLastWarned(new Date());
            telegram.sendMessage(watcher.getUser().getTelegramId(), "==== Server down ==== \n \nGot an error while checking " + watcher.getName() + "\n" +
                    "last successfull message: " + ((new Date().getTime() - watcher.getLastSuccessAt().getTime()) / 1000) + "s ago \n" +
                    watcher.getLastMessage());
        }
    }

    private void sendIsOkAgainMessage(Watcher watcher) {
        watcher.setLastWarned(new Date(0));
        telegram.sendMessage(watcher.getUser().getTelegramId(), "==== Server ok again ====\n \n " + watcher.getName() +
                " is ok again was \n down for " + ((new Date().getTime() - watcher.getLastSuccessAt().getTime()) / 1000) + "s\n");
    }

    private void receivedCode(HttpStatus code, Watcher watcher, String body) {

        watcher.setLastChecked(new Date());
        watcher.setLastMessage(code + "\n body: " + body);

        if(code == watcher.getExpectedStatus()) {
            if(watcher.getStatus() == Status.OFFLINE) {
                sendIsOkAgainMessage(watcher);
            }
            watcher.setLastSuccessAt(watcher.getLastChecked());
            watcher.setStatus(Status.ONLINE);
        } else {
            sendErrorMessage(watcher);
            watcher.setStatus(Status.OFFLINE);
        }

        watcherRepository.save(watcher);
    }

}
