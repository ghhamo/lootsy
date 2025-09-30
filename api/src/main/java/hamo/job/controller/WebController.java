package hamo.job.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("query", q != null ? q : "");
        return "search";
    }

    @GetMapping("/details")
    public String details(@RequestParam(required = false) String id, Model model) {
        model.addAttribute("productId", id != null ? id : "");
        return "details";
    }

    @GetMapping("/shipping")
    public String shipping() {
        return "shipping";
    }

    @GetMapping("/order")
    public String order() {
        return "order";
    }
    
    @GetMapping("/account")
    public String account() {
        return "account";
    }
}
