package com.titu.core.controller;

import com.titu.core.service.CaixinhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/caixinhas")
@RequiredArgsConstructor
public class CaixinhaController {

    private final CaixinhaService caixinhaService;

    @GetMapping
    public String paginaCaixinhas(@RequestParam(required = false) Integer mes,
                                  @RequestParam(required = false) Integer ano,
                                  Model model) {

        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        model.addAttribute("caixinhas", caixinhaService.processarCaixinhasDoMes(ano, mes));
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "caixinhas";
    }


}