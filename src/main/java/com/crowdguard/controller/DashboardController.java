package com.crowdguard.controller;

import com.crowdguard.model.AlertStatus;
import com.crowdguard.model.Authority;
import com.crowdguard.model.User;
import com.crowdguard.repository.AlertRepository;
import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AuthorityRepository authorityRepo;
    private final QuarterRepository quarterRepo;
    private final AlertRepository alertRepo;
    private final UserRepository userRepo;

    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_AUTHORITY")))
            return "redirect:/authority/dashboard";
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            return "redirect:/admin/dashboard";
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String userDash(Authentication auth, Model m) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        m.addAttribute("email", u.getEmail());
        m.addAttribute("userName", u.getFullName());
        m.addAttribute("userId", u.getId().toString());
        m.addAttribute("familyId", u.getFamily() != null ? u.getFamily().getId().toString() : "");
        return "user-dashboard";
    }

    @GetMapping("/authority/dashboard")
    public String authorityDash(Authentication auth, Model m) {
        Authority a = authorityRepo.findByEmail(auth.getName()).orElseThrow();
        m.addAttribute("authority", a);
        m.addAttribute("quarters", quarterRepo.findAll());
        m.addAttribute("activeAlerts", alertRepo.findByStatusOrderByTimestampDesc(AlertStatus.ACTIVE));
        return "authority-dashboard";
    }
}