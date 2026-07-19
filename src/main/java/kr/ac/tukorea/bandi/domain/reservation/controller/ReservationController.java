package kr.ac.tukorea.bandi.domain.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/reserve")
    public String reservationForm() {
        return "reservation/form";
    }
}
