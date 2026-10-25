package org.springframework.samples.petclinic.rest.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.samples.petclinic.model.Vaccine;
import org.springframework.samples.petclinic.model.VaccineValidationException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Registers the vaccine applied to a pet (SPEC-001, POST /api/pets/{petId}/vaccines).
 * With the security on, only VET_ADMIN registers (RN-002, RN-004).
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class VaccineRestControllerV1 {

    public record VaccineRequest(String name, LocalDate applicationDate, LocalDate expirationDate) {
    }

    public record VaccineResponse(Integer id, Integer petId, String name,
                                  LocalDate applicationDate, LocalDate expirationDate) {
    }

    private final ClinicService clinicService;

    private final Clock clock;

    public VaccineRestControllerV1(ClinicService clinicService, Clock clock) {
        this.clinicService = clinicService;
        this.clock = clock;
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @PostMapping("/pets/{petId}/vaccines")
    public ResponseEntity<VaccineResponse> registerVaccine(@PathVariable("petId") Integer petId,
                                                           @RequestBody VaccineRequest request) {
        if (this.clinicService.findPetById(petId) == null) {
            return ResponseEntity.notFound().build();
        }
        Vaccine vaccine = Vaccine.register(petId, request.name(), request.applicationDate(),
            request.expirationDate(), LocalDate.now(this.clock));
        this.clinicService.saveVaccine(vaccine);
        VaccineResponse response = new VaccineResponse(vaccine.getId(), vaccine.getPetId(), vaccine.getName(),
            vaccine.getApplicationDate(), vaccine.getExpirationDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(VaccineValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(VaccineValidationException e, HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, e, request,
            "The request contains invalid or missing parameters");
        List<Map<String, String>> errors = e.getViolations().stream()
            .map(v -> Map.of(
                "field", v.field(),
                "message", "Field '" + v.field() + "' " + v.message()))
            .toList();
        detail.setProperty("schemaValidationErrors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException e,
                                                              HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, e, request,
            "The request body is missing or malformed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.FORBIDDEN, e, request, "Access is denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
    }

    private ProblemDetail problem(HttpStatus status, Exception e, HttpServletRequest request, String text) {
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setType(URI.create(request.getRequestURL().toString()));
        detail.setTitle(e.getClass().getSimpleName());
        detail.setDetail(text);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }
}
