package rocks.reckt3r.model.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rocks.reckt3r.model.Listener;
import rocks.reckt3r.model.repository.ListenerRepository;

import java.util.Date;

/**
 * Created by hannes on 19.02.16.
 */
@RestController("/listen")
public class ListenerController {

    @Autowired
    ListenerRepository listenerRepository;

    @RequestMapping(value = "/{token}")
    public ResponseEntity<String> get(@PathVariable("token") String token) {
        Listener listener = listenerRepository.findByToken(token);
        if(listener == null) {
            return new ResponseEntity<String>("token invalid", HttpStatus.NOT_FOUND);
        }

        listener.setLastCalled(new Date());
        listenerRepository.save(listener);

        return new ResponseEntity<String>("ok", HttpStatus.OK);
    }


    @RequestMapping(value = "/{token}", method = RequestMethod.POST)
    public ResponseEntity<String> post(@PathVariable("token") String token) {
        return get(token);
    }


}
