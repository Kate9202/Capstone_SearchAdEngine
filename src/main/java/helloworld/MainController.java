package helloworld;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import helloworld.User;
import helloworld.UserRepository;

//@Controller //means this class is a controller

@RestController
@RequestMapping(path = "/demo")
public class MainController {
    @Autowired //get the bean "UserRepository"
    private UserRepository userRepository;

    @GetMapping(path="/add")
    public @ResponseBody String addNewUser (@RequestParam String name,
                                            @RequestParam String email){
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        User n = new User();
        n.setName(name);
        n.setEmail(email);
        userRepository.save(n);
        return "Saved";
    }

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<User> getAllUser(){
        //Returns a JSON or XML with user
        return userRepository.findAll();
    }

    @GetMapping(path = "/api")
    public @ResponseBody String getAmazonMessage() {
        AmazonAPI amazonAPI = new AmazonAPI();
        return amazonAPI.callAmazonAPI();
//        return null;
    }
}