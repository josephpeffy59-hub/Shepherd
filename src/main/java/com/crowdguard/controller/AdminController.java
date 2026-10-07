package com.crowdguard.controller;

import com.crowdguard.model.SafetyRating;
import com.crowdguard.repository.AlertRepository;
import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final AuthorityRepository authorityRepo;
    private final AlertRepository alertRepo;
    private final QuarterRepository quarterRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model m) {
        m.addAttribute("users", userRepo.findAll());
        m.addAttribute("authorities", authorityRepo.findAll());
        m.addAttribute("alerts", alertRepo.findAll());
        m.addAttribute("quarters", quarterRepo.findAll());
        return "admin-dashboard";
    }

    @PostMapping("/quarter/{id}")
    public String updateQuarter(@PathVariable UUID id,
                                @RequestParam SafetyRating rating,
                                @RequestParam String description) {
        quarterRepo.findById(id).ifPresent(q -> {
            q.setSafetyRating(rating);
            q.setDescription(description);
            quarterRepo.save(q);
        });
        return "redirect:/admin/dashboard?saved";
    }
}
