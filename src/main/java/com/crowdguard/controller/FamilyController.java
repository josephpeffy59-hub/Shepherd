package com.crowdguard.controller;

import com.crowdguard.model.User;
import com.crowdguard.repository.UserRepository;
import com.crowdguard.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final UserRepository userRepo;

    @GetMapping
    public String view(Authentication auth, Model m) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        m.addAttribute("family", u.getFamily());
        return "family";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name, Authentication auth) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        familyService.createFamily(u, name);
        return "redirect:/family";
    }
}