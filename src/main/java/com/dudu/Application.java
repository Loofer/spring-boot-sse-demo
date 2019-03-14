package com.dudu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
@Slf4j
@SpringBootApplication
@Controller
public class Application {


    private final List<SseEmitter> emitters = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RequestMapping(path = "/stream", method = RequestMethod.GET)
    public SseEmitter stream() {

        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));

        return emitter;
    }

    @ResponseBody
    @PostMapping(path = "/")
    public String sendMessage(String message) {

        String time = LocalDateTime.now().toString();
        log.info("接收到：" + message);
        log.info("时间：" + time);
        final String msg = "接收到：".concat(message).concat("--时间：").concat(time);

        emitters.forEach((SseEmitter emitter) -> {
            try {
                emitter.send(msg, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
                e.printStackTrace();
            }
        });
        return message;
    }

}
