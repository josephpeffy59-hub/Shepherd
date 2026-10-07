package com.crowdguard;

import com.crowdguard.model.*;
import com.crowdguard.repository.*;
import com.crowdguard.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.*;
import java.util.Base64;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:shepherdflows;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "crowdguard.upload-dir=target/test-uploads",
    "debug=false", "logging.level.root=ERROR", "spring.main.banner-mode=off"
})
@AutoConfigureMockMvc
class ApplicationFlowsTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuthorityRepository authorities;
    @Autowired QuarterRepository quarters;
    @Autowired AlertRepository alerts;
    @Autowired UserService userService;
    @Autowired AlertService alertService;
    @Autowired PasswordEncoder encoder;

    private MockMultipartFile picture(String field) {
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=");
        return new MockMultipartFile(field, "identity.png", "image/png", png);
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").param("username", email).param("password", password))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/post-login"))
            .andReturn().getRequest().getSession(false);
    }

    private void snapshot(String name, String html) throws Exception {
        Path directory = Paths.get("target/page-preview"); Files.createDirectories(directory);
        Files.writeString(directory.resolve(name), html);
    }

    @Test void completeAccountFamilyAlertAndAdminFlows() throws Exception {
        for (String[] page : new String[][]{{"/","index.html"},{"/login","login.html"},{"/register","register.html"},{"/authority-register","authority-register.html"},{"/quarters","quarters.html"}}) {
            var response = mvc.perform(get(page[0])).andExpect(status().isOk()).andReturn();
            snapshot(page[1], response.getResponse().getContentAsString());
        }
        mvc.perform(get("/js/safety.js")).andExpect(status().isOk());
        var quarter = quarters.findAll().get(0);
        String id = quarter.getId().toString();
        mvc.perform(multipart("/register").file(picture("idPicture1")).file(picture("idPicture2"))
                .param("fullName", "Flow Citizen").param("email", "FLOW@Example.com").param("password", "Secret77").param("quarterId", id))
            .andExpect(redirectedUrl("/login?registered"));
        var citizen = users.findByEmail("flow@example.com").orElseThrow();
        assertTrue(encoder.matches("Secret77", citizen.getPassword()));
        assertNotEquals("Secret77", citizen.getPassword());
        mvc.perform(get("/uploads/" + citizen.getIdPicturePath1())).andExpect(status().isOk());

        mvc.perform(post("/authority-register").param("fullName", "Flow Officer").param("email", "officer@example.com")
                .param("password", "Secret77").param("badgeNumber", "TEST123").param("type", "POLICE").param("quarterId", id))
            .andExpect(redirectedUrl("/login?registered"));
        mvc.perform(post("/authority-register").param("fullName", "Duplicate Officer").param("email", "FLOW@example.com")
                .param("password", "Secret77").param("badgeNumber", "TEST124").param("type", "POLICE").param("quarterId", id))
            .andExpect(status().isOk()).andExpect(content().string(containsString("already registered")));
        assertFalse(authorities.existsByEmailIgnoreCase("flow@example.com"));
        mvc.perform(multipart("/register").file(picture("idPicture1")).file(picture("idPicture2"))
                .param("fullName", "Invalid Citizen").param("email", "-1").param("password", "123456").param("quarterId", id))
            .andExpect(status().isOk()).andExpect(content().string(containsString("at least 7 characters")));
        assertFalse(users.existsByEmailIgnoreCase("-1"));
        mvc.perform(post("/login").param("username", "flow@example.com").param("password", "incorrect"))
            .andExpect(redirectedUrl("/login?error"));

        var session = login("FLOW@EXAMPLE.COM", "Secret77");
        mvc.perform(get("/post-login").session(session)).andExpect(redirectedUrl("/dashboard"));
        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk()).andExpect(content().string(containsString("Flow Citizen")));
        mvc.perform(get("/admin/dashboard").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/authority/dashboard").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/family").session(session)).andExpect(status().isOk()).andExpect(content().string(containsString("Create Family")));
        mvc.perform(post("/family/create").session(session).param("name", "   ")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Enter a name for your family group")));
        mvc.perform(post("/family/create").session(session).param("name", "Test Household")).andExpect(redirectedUrl("/family?created"));
        var familyPage = mvc.perform(get("/family").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Test Household"))).andExpect(content().string(containsString("Flow Citizen"))).andReturn();
        snapshot("family.html", familyPage.getResponse().getContentAsString());
        var family = users.findByEmail("flow@example.com").orElseThrow().getFamily();
        userService.register("Family Member", "family@example.com", "Secret77", quarter.getId(), picture("idPicture1"), picture("idPicture2"), family.getInviteCode());

        mvc.perform(post("/api/alert/trigger").session(session).contentType("application/json").content("{\"latitude\":999,\"longitude\":11}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/alert/trigger").session(session).contentType("application/json").content("{\"latitude\":3.848,\"longitude\":11.5021}"))
            .andExpect(status().isOk());
        assertTrue(users.findByEmail("flow@example.com").orElseThrow().isHasActiveAlert());
        var active = alerts.findByUserIdAndStatusOrderByTimestampDesc(citizen.getId(), AlertStatus.ACTIVE);
        assertEquals(1, active.size());
        var citizenPage = mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("active: true"))).andReturn();
        snapshot("user-dashboard.html", citizenPage.getResponse().getContentAsString());
        var officerSession = login("officer@example.com", "Secret77");
        mvc.perform(get("/post-login").session(officerSession)).andExpect(redirectedUrl("/authority/dashboard"));
        var officerPage = mvc.perform(get("/authority/dashboard").session(officerSession)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Flow Citizen")))
            .andExpect(content().string(not(containsString(citizen.getPassword())))).andReturn();
        snapshot("authority-dashboard.html", officerPage.getResponse().getContentAsString());
        mvc.perform(post("/api/alert/cancel").session(session)).andExpect(status().isOk());
        assertFalse(users.findByEmail("flow@example.com").orElseThrow().isHasActiveAlert());
        assertTrue(alerts.findByUserIdAndStatusOrderByTimestampDesc(citizen.getId(), AlertStatus.ACTIVE).isEmpty());
        assertEquals(AlertStatus.RESOLVED, alerts.findById(active.get(0).getId()).orElseThrow().getStatus());

        users.save(User.builder().fullName("Test Administrator").email("reviewer@example.com")
            .password(encoder.encode("Secret77")).admin(true).quarter(quarter).build());
        var adminSession = login("reviewer@example.com", "Secret77");
        mvc.perform(get("/post-login").session(adminSession)).andExpect(redirectedUrl("/admin/dashboard"));
        var adminPage = mvc.perform(get("/admin/dashboard").session(adminSession)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Flow Officer")))
            .andExpect(content().string(containsString("form=\"quarter-form-" + id + "\""))).andReturn();
        snapshot("admin-dashboard.html", adminPage.getResponse().getContentAsString());
        mvc.perform(post("/admin/quarter/" + id).session(adminSession).param("rating", "MODERATE").param("description", "Integration test guidance"))
            .andExpect(redirectedUrl("/admin/dashboard?saved"));
        mvc.perform(get("/quarters")).andExpect(status().isOk()).andExpect(content().string(containsString("Integration test guidance")));
        mvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/"));
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }
}
