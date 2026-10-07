package com.crowdguard.controller;

import com.crowdguard.model.AuthorityType;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.service.AuthorityService;
import com.crowdguard.service.UserService;
import com.crowdguard.service.RegistrationValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.Map;

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
                           @RequestParam(defaultValue = "") String email,
                           @RequestParam(defaultValue = "") String password,
                           @RequestParam UUID quarterId,
                           @RequestParam("idPicture1") MultipartFile id1,
                           @RequestParam("idPicture2") MultipartFile id2,
                           @RequestParam(required = false) String inviteCode,
                           Model model) {
        try {
            userService.register(fullName, email, password, quarterId, id1, id2, inviteCode);
        } catch (RegistrationValidationException e) {
            model.addAttribute("validationErrors", e.getErrors());
            return citizenErrors(model, fullName, email, quarterId, inviteCode);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("validationErrors", Map.of("registration", "We could not create your account. Check your details and try again."));
            return citizenErrors(model, fullName, email, quarterId, inviteCode);
        }
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
                                    @RequestParam(defaultValue = "") String email,
                                    @RequestParam(defaultValue = "") String password,
                                    @RequestParam String badgeNumber,
                                    @RequestParam AuthorityType type,
                                    @RequestParam UUID quarterId,
                                    Model model) {
        try {
            authorityService.register(fullName, email, password, badgeNumber, type, quarterId);
        } catch (RegistrationValidationException e) {
            model.addAttribute("validationErrors", e.getErrors());
            return authorityErrors(model, fullName, email, quarterId, badgeNumber, type);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("validationErrors", Map.of("registration", "We could not create your account. Check your details and try again."));
            return authorityErrors(model, fullName, email, quarterId, badgeNumber, type);
        }
        return "redirect:/login?registered";
    }

    private String citizenErrors(Model model, String name, String email, UUID quarterId, String inviteCode) {
        model.addAttribute("fullName", name);
        model.addAttribute("email", email);
        model.addAttribute("quarterId", quarterId);
        model.addAttribute("inviteCode", inviteCode);
        // Passwords and uploaded identity files must not be echoed into the response.
        return registerForm(model);
    }

    private String authorityErrors(Model model, String name, String email, UUID quarterId, String badge, AuthorityType type) {
        model.addAttribute("fullName", name);
        model.addAttribute("email", email);
        model.addAttribute("quarterId", quarterId);
        model.addAttribute("badgeNumber", badge);
        model.addAttribute("selectedType", type);
        return authorityForm(model);
    }
}
