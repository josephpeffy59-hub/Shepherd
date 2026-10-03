package com.crowdguard.controller;

import com.crowdguard.model.AuthorityType;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.service.AuthorityService;
import com.crowdguard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthorityService authorityService;
    private final QuarterRepository quarterRepo;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("quarters", quarterRepo.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam UUID quarterId,
                           @RequestParam("idPicture1") MultipartFile id1,
                           @RequestParam("idPicture2") MultipartFile id2,
                           @RequestParam(required = false) String inviteCode) {
        userService.register(fullName, email, password, quarterId, id1, id2, inviteCode);
        return "redirect:/login?registered";
    }

    @GetMapping("/authority-register")
    public String authorityForm(Model m) {
        m.addAttribute("quarters", quarterRepo.findAll());
        m.addAttribute("types", AuthorityType.values());
        return "authority-register";
    }

    @PostMapping("/authority-register")
    public String authorityRegister(@RequestParam String fullName,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String badgeNumber,
                                    @RequestParam AuthorityType type,
                                    @RequestParam UUID quarterId) {
        authorityService.register(fullName, email, password, badgeNumber, type, quarterId);
        return "redirect:/login?registered";
    }
}