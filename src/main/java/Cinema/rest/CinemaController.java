package Cinema.rest;

import Cinema.model.Cinema;
import Cinema.model.CinemaHall;
import Cinema.model.User;
import Cinema.service.CinemaHallService;
import Cinema.service.CinemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller
public class CinemaController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CinemaService cinemaService;

    @GetMapping("/")
    public String greeting(Map<String, Object> model) {
        return "greeting";
    }

    @GetMapping(value = "/cinemas")
    public String getCinemas(Model model, @RequestParam(required = false, defaultValue = "") String filter) {
        List<Cinema> cinemas;

        if (filter != null && !filter.isEmpty()) {
            cinemas = cinemaService.findByName(filter);
        } else {
            cinemas = cinemaService.findAll();
        }

        model.addAttribute("cinemas", cinemas);
        model.addAttribute("filter", filter);
        return "cinema-list";
    }

    @GetMapping(value = "/cinemas/{cinema_id}")
    public String getCinemaById(Model model, @PathVariable long cinema_id) {
        Cinema cinema = null;
        try {
            cinema = cinemaService.findById(cinema_id);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        model.addAttribute("cinemaHalls", cinema.getCinemaHalls());
        model.addAttribute("cinema", cinema);
        return "cinema";
    }

    @GetMapping(value = {"/cinemas/add"})
    public String showAddCinema(Model model) {
        Cinema cinema = new Cinema();
        model.addAttribute("add", true);
        model.addAttribute("cinema", cinema);
        return "cinema-add";
    }

    @PostMapping(value = "/cinemas/add")
    public String addCinema(Model model,
                            @AuthenticationPrincipal User user,
                            @RequestParam String name) {
        try {
            Cinema cinema = new Cinema(name, user);
            Cinema newCinema = cinemaService.save(cinema);
            return "redirect:/cinemas/" + newCinema.getCinema_id();
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            logger.error(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "cinema-add";
        }
    }

    @GetMapping(value = {"/cinemas/{cinema_id}/edit"})
    public String showEditCinema(Model model,
                                 @PathVariable long cinema_id) {
        Cinema cinema = null;
        try {
            cinema = cinemaService.findById(cinema_id);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        model.addAttribute("cinema", cinema);
        assert cinema != null;
        model.addAttribute("cinemaHalls", cinema.getCinemaHalls());
        return "cinema-edit";
    }

    @PostMapping(value = {"/cinemas/{cinema_id}/edit"})
    public String updateCinema(Model model,
                               @RequestParam String name,
                               @PathVariable("cinema_id") Cinema cinema) {
        try {
            cinema.setName(name);
            cinemaService.update(cinema);
            return "redirect:/cinemas/" + cinema.getCinema_id();
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            logger.error(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("cinemaHalls", cinema.getCinemaHalls());
            return "cinema-edit";
        }
    }

    @GetMapping(value = {"/cinemas/{cinema_id}/delete"})
    public String showDeleteCinema(
            Model model, @PathVariable long cinema_id) {
        Cinema cinema = null;
        try {
            cinema = cinemaService.findById(cinema_id);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        model.addAttribute("allowDelete", true);
        model.addAttribute("cinema", cinema);
        return "cinema-list";
    }

    @PostMapping(value = {"/cinemas/{cinema_id}/delete"})
    public String deleteCinemaById(
            Model model, @PathVariable("cinema_id") Cinema cinema) {
        try {
            cinemaHallService.deleteByCinema(cinema);
            cinemaService.deleteById(cinema.getCinema_id());
            return "redirect:/cinemas";
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            logger.error(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "cinema-edit";
        }
    }

    @Autowired
    private CinemaHallService cinemaHallService;

    @PostMapping("/cinemas/{cinema_id}/add_hall")
    public String addHall(@RequestParam(value = "rowCount") Integer row,
                          @RequestParam(value = "placeCount") Integer place,
                          @PathVariable("cinema_id") Cinema cinema,
                          Model model) throws Exception {
        CinemaHall cinemaHall = new CinemaHall(row, place, cinema.getUser());
        cinemaHall.setCinema(cinema);
        cinema.getCinemaHalls().add(cinemaHall);
        cinemaHallService.save(cinemaHall);
        cinemaService.update(cinema);
        model.addAttribute("cinemaHalls", cinema.getCinemaHalls());
        return "redirect:/cinemas/"+cinema.getCinema_id()+"/edit";
    }

    @GetMapping("/cinemas/{cinema_id}/show_hall")
    public String showHall(Model model, @PathVariable("cinema_id") Cinema cinema) {
        model.addAttribute("cinemaHalls", cinema.getCinemaHalls());
        return "cinema-edit";
    }
}
