package kr.ac.tukorea.bandi.domain.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReservationController {

    @GetMapping("/reservations")
    public String management() {
        return "reservation/management";
    }

    @GetMapping("/showops")
    public String operations() {
        return "showops/operations";
    }

    @GetMapping("/reserve/{slug}")
    public String reservationForm(@PathVariable String slug) {
        return "reservation/form";
    }

    @GetMapping({"/reserve", "/reserve/lookup"})
    public String reservationLookup() {
        return "reservation/lookup";
    }
}
