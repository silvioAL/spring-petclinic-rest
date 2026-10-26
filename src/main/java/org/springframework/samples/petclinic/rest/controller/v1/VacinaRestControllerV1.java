package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Vacina;
import org.springframework.samples.petclinic.model.VacinaAVencerItem;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.VacinaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Registro da vacina aplicada (SPEC-001) e lista de vacinas a vencer para a recepcao (SPEC-002).
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class VacinaRestControllerV1 {

    static final int TAMANHO_MAXIMO_DA_PAGINA = 100;
    static final int TAMANHO_PADRAO_DA_PAGINA = 20;

    private final ClinicService clinicService;
    private final VacinaService vacinaService;

    public VacinaRestControllerV1(ClinicService clinicService, VacinaService vacinaService) {
        this.clinicService = clinicService;
        this.vacinaService = vacinaService;
    }

    public record VacinaEntrada(String nome, String validade) {
    }

    public record VacinaSaida(Integer id, Integer petId, String nome, LocalDate validade) {
    }

    public record PaginaDeVacinas(List<VacinaAVencerItem> itens, long total, int page, int size) {
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @PostMapping(value = "/pets/{petId}/vacinas", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VacinaSaida> registrarVacina(@PathVariable("petId") Integer petId,
                                                       @RequestBody VacinaEntrada entrada) {
        String nome = entrada == null || entrada.nome() == null ? "" : entrada.nome().trim();
        if (nome.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDate validade;
        try {
            if (entrada.validade() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            validade = LocalDate.parse(entrada.validade().trim());
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (this.clinicService.findPetById(petId) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Vacina vacina = this.vacinaService.registrar(petId, nome, validade);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/pets/{petId}/vacinas/{id}").buildAndExpand(petId, vacina.getId()).toUri());
        VacinaSaida saida = new VacinaSaida(vacina.getId(), vacina.getPetId(), vacina.getNome(), vacina.getValidade());
        return new ResponseEntity<>(saida, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @GetMapping(value = "/vacinas/a-vencer", produces = "application/json")
    public ResponseEntity<PaginaDeVacinas> listarAVencer(@RequestParam(name = "page", required = false) String page,
                                                         @RequestParam(name = "size", required = false) String size) {
        int pagina;
        int tamanho;
        try {
            pagina = page == null ? 0 : Integer.parseInt(page.trim());
            tamanho = size == null ? TAMANHO_PADRAO_DA_PAGINA : Integer.parseInt(size.trim());
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO_DA_PAGINA) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Page<VacinaAVencerItem> resultado = this.vacinaService.listarAVencer(LocalDate.now(), pagina, tamanho);
        return new ResponseEntity<>(
            new PaginaDeVacinas(resultado.getContent(), resultado.getTotalElements(), pagina, tamanho),
            HttpStatus.OK);
    }
}
