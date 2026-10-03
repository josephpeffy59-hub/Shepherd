package com.crowdguard.controller;

import com.crowdguard.repository.QuarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class QuarterController {

    private final QuarterRepository repo;

    @GetMapping("/quarters")
    public String list(Model m) {
        m.addAttribute("quarters", repo.findAll());
        return "quarters";
    }
}