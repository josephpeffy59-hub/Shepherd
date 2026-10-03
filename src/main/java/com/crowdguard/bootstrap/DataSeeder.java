package com.crowdguard.bootstrap;

import com.crowdguard.model.Quarter;
import com.crowdguard.model.SafetyRating;
import com.crowdguard.model.User;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final QuarterRepository quarterRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (quarterRepo.count() == 0) {
            seedQuarters();
        }
                if (userRepo.findByEmail("admin@shepherd.cm").isEmpty()) {
            Quarter q = quarterRepo.findAll().get(0);
            User admin = User.builder()
                    .fullName("Shepherd Admin")
                    .email("admin@shepherd.cm")
                    .password(encoder.encode("admin123"))
                    .quarter(q)
                    .admin(true)
                    .build();
            userRepo.save(admin);
        }
    }

    private void seedQuarters() {
        List<Quarter> quarters = List.of(
            q("Bastos", "Yaoundé I", 3.8840, 11.5145, SafetyRating.SAFE, "Upscale diplomatic and residential area."),
            q("Nlongkak", "Yaoundé I", 3.8760, 11.5170, SafetyRating.MODERATE, "Busy commercial and residential area."),
            q("Tsinga", "Yaoundé I", 3.8820, 11.5260, SafetyRating.SAFE, "Home to embassies and government offices."),
            q("Etoudi", "Yaoundé I", 3.9020, 11.5230, SafetyRating.MODERATE, "Location of Unity Palace."),
            q("Mvog-Ada", "Yaoundé I", 3.8730, 11.5280, SafetyRating.MODERATE, "Popular residential quarter."),
            q("Ngoa-Ekelle", "Yaoundé I", 3.8580, 11.5010, SafetyRating.MODERATE, "University of Yaoundé I area."),
            q("Mokolo", "Yaoundé I", 3.8710, 11.5080, SafetyRating.DANGEROUS, "Crowded market, watch belongings."),
            q("Damas", "Yaoundé II", 3.8700, 11.5300, SafetyRating.MODERATE, "Dense residential area."),
            q("Mvog-Mbi", "Yaoundé II", 3.8670, 11.5210, SafetyRating.MODERATE, "Mixed residential and commercial."),
            q("Mvog-Betsi", "Yaoundé II", 3.8630, 11.5250, SafetyRating.DANGEROUS, "Reported petty crime at night."),
            q("Briqueterie", "Yaoundé II", 3.8690, 11.5350, SafetyRating.MODERATE, "Market and transit area."),
            q("Elig-Essono", "Yaoundé II", 3.8580, 11.5160, SafetyRating.SAFE, "Sports complex area."),
            q("Efoulan", "Yaoundé III", 3.8400, 11.4950, SafetyRating.MODERATE, "Suburban residential quarter."),
            q("Nsam", "Yaoundé III", 3.8450, 11.4880, SafetyRating.MODERATE, "Residential, near Nsam flyover."),
            q("Mimboman", "Yaoundé III", 3.8380, 11.5050, SafetyRating.SAFE, "Calm residential quarter."),
            q("Kondengui", "Yaoundé IV", 3.8520, 11.5280, SafetyRating.MODERATE, "Central prison area."),
            q("Ekounou", "Yaoundé IV", 3.8450, 11.5320, SafetyRating.MODERATE, "Busy residential and market."),
            q("Emana", "Yaoundé IV", 3.8350, 11.5280, SafetyRating.MODERATE, "Growing residential quarter."),
            q("Odza", "Yaoundé IV", 3.8250, 11.5320, SafetyRating.MODERATE, "Near the airport."),
            q("Ekoumdoum", "Yaoundé IV", 3.8280, 11.5450, SafetyRating.MODERATE, "Eastern outskirts."),
            q("Essos", "Yaoundé V", 3.8730, 11.5400, SafetyRating.MODERATE, "Major commercial hub."),
            q("Nkolndongo", "Yaoundé V", 3.8700, 11.5480, SafetyRating.MODERATE, "Dense residential quarter."),
            q("Mvog-Atangana Mballa", "Yaoundé V", 3.8760, 11.5450, SafetyRating.MODERATE, "Residential quarter."),
            q("Madagascar", "Yaoundé V", 3.8800, 11.5500, SafetyRating.SAFE, "Quiet residential quarter."),
            q("Biyem-Assi", "Yaoundé VI", 3.8280, 11.4780, SafetyRating.SAFE, "Large university and residential area."),
            q("Mendong", "Yaoundé VI", 3.8330, 11.4690, SafetyRating.MODERATE, "Residential, near Mendong market."),
            q("Etoa-Meki", "Yaoundé VI", 3.8420, 11.4840, SafetyRating.MODERATE, "Busy junction quarter."),
            q("Nkoldongo", "Yaoundé VI", 3.8370, 11.4720, SafetyRating.MODERATE, "Residential area."),
            q("Simbock", "Yaoundé VI", 3.8450, 11.4600, SafetyRating.SAFE, "Near Simbock lake."),
            q("Etoug-Ebe", "Yaoundé VI", 3.8400, 11.4900, SafetyRating.MODERATE, "Residential, near Etoug-Ebe market."),
            q("Nkolbisson", "Yaoundé VII", 3.8830, 11.4500, SafetyRating.MODERATE, "Outskirts, university campus II."),
            q("Oyom-Abang", "Yaoundé VII", 3.8700, 11.4600, SafetyRating.MODERATE, "Suburban quarter."),
            q("Ekombitie", "Yaoundé VII", 3.8620, 11.4550, SafetyRating.MODERATE, "Suburban residential."),
            q("Mbankolo", "Yaoundé VII", 3.8900, 11.4850, SafetyRating.MODERATE, "Hilly residential quarter."),
            q("Awae", "Yaoundé VII", 3.9000, 11.4600, SafetyRating.SAFE, "Quiet suburb."),
            q("Nkolafamba", "Yaoundé VII", 3.8700, 11.4300, SafetyRating.SAFE, "Rural outskirts.")
        );
        quarterRepo.saveAll(quarters);
    }

    private Quarter q(String name, String muni, double lat, double lng, SafetyRating r, String d) {
        return Quarter.builder()
                .name(name).municipality(muni)
                .latitude(lat).longitude(lng)
                .safetyRating(r).description(d)
                .build();
    }
}