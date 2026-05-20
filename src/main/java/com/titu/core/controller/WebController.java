package com.titu.core.controller;

import com.titu.core.model.Cliente;
import com.titu.core.model.Despesa;
import com.titu.core.repository.EventoRepository;
import com.titu.core.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ClienteService clienteService;
    private final DespesaService despesaService;
    private final CategoriaService categoriaService;
    private final EventoService eventoService; // <-- ADICIONE AQUI
    private final EventoRepository eventoRepository;
    private final TipoEventoService tipoEventoService;
    private final FechamentoService fechamentoService;

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // =========================================================================
    // ROTAS PRINCIPAIS & DASHBOARD
    // =========================================================================

    // Guarda de Trânsito! Se entrar no site puro, joga pro Dashboard
    @GetMapping("/")
    public String redirecionarRaiz() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/suporte")
    public String suporte() {
        return "suporte";
    }

    // =========================================================================
    // ROTAS DE PARCEIROS (Fornecedores / Promoters / etc)
    // =========================================================================

    @GetMapping("/clientes")
    public String paginaClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("cliente", new Cliente());
        return "clientes";
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Parceiro salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Atenção: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao salvar parceiro.");
        }
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/excluir/{id}")
    public String excluirCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Parceiro excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Não é possível excluir: Este parceiro possui lançamentos vinculados!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir parceiro: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/{id}/detalhes")
    public String detalhesCliente(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        // No futuro, podemos listar os Eventos que esse parceiro trabalhou aqui
        return "cliente-detalhes";
    }

    // =========================================================================
    // ROTAS DE DESPESAS (ABA 3 DA PLANILHA)
    // =========================================================================

    @GetMapping("/despesas")
    public String paginaDespesas(Model model) {
        model.addAttribute("despesas", despesaService.listarTodas());
        model.addAttribute("fornecedores", clienteService.listarSomenteFornecedores());

        // AGORA BUSCA DO BANCO DE DADOS!
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("formasPagamento", com.titu.core.model.FormaPagamento.values());

        // Mandando os Tipos (Provisão/Variável) para o modal de NOVA Categoria
        model.addAttribute("tiposCategoria", com.titu.core.model.TipoCategoria.values());

        return "despesas";
    }

    @PostMapping("/categorias/salvar")
    public String salvarCategoria(com.titu.core.model.Categoria categoria, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.salvar(categoria);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Nova categoria adicionada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao criar categoria: " + e.getMessage());
        }

        // Volta pra tela de despesas (ou de onde ele estiver chamando)
        String urlAnterior = request.getHeader("Referer");
        return (urlAnterior != null) ? "redirect:" + urlAnterior : "redirect:/despesas";
    }

    @PostMapping("/despesas/salvar")
    public String salvarDespesa(Despesa despesa,
                                @RequestParam(required = false) Long fornecedorId,
                                RedirectAttributes redirectAttributes) {
        try {
            despesaService.salvar(despesa, fornecedorId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Despesa registrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar despesa: " + e.getMessage());
        }
        return "redirect:/despesas";
    }

    @GetMapping("/despesas/pagar/{id}")
    public String darBaixaDespesa(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            despesaService.darBaixa(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Despesa marcada como PAGA! Dinheiro saiu do caixa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao processar pagamento.");
        }
        String urlAnterior = request.getHeader("Referer");
        return (urlAnterior != null) ? "redirect:" + urlAnterior : "redirect:/despesas";
    }

    @GetMapping("/despesas/excluir/{id}")
    public String excluirDespesa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            despesaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Despesa apagada do sistema!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao apagar despesa.");
        }
        return "redirect:/despesas";
    }

    // =========================================================================
    // ROTAS DE EVENTOS (ABA 4 - DIÁRIO)
    // =========================================================================

    @GetMapping("/eventos")
    public String paginaDiario(@RequestParam(required = false) Integer mes,
                               @RequestParam(required = false) Integer ano,
                               Model model) {

        // Se vier vazio, pega a data de hoje
        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        // Busca só os eventos do mês filtrado
        model.addAttribute("eventos", eventoService.listarPorMesEAno(ano, mes));
        // Dentro do metodo que abre a página do Diário (ex: /diario)
        model.addAttribute("mesTrancado", fechamentoService.isMesTrancado(mes, ano));
        model.addAttribute("tiposEvento", tipoEventoService.listarTodos());
        // Devolve pro HTML saber qual mês/ano deixar selecionado no select
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "eventos";
    }

    @PostMapping("/eventos/fechar")
    public String fecharCompetenciaDoDiario(@RequestParam Integer mes,
                                            @RequestParam Integer ano,
                                            RedirectAttributes redirectAttributes) {
        try {
            fechamentoService.fecharCompetencia(ano, mes, "Admin");
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Mês finalizado com sucesso! Lançamentos trancados.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao finalizar o mês.");
        }
        return "redirect:/eventos?mes=" + mes + "&ano=" + ano;
    }

    @PostMapping("/eventos/salvar")
    public String salvarEvento(com.titu.core.model.Evento evento, RedirectAttributes redirectAttributes) {
        try {
            eventoService.salvar(evento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Evento registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar evento: " + e.getMessage());
        }
        return "redirect:/eventos";
    }

    @GetMapping("/eventos/excluir/{id}")
    public String excluirEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Evento apagado!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao apagar evento.");
        }
        return "redirect:/eventos";
    }

    @PostMapping("/eventos/gerar-mes")
    public String gerarMesCompleto(@RequestParam int ano, @RequestParam int mes, RedirectAttributes redirectAttributes) {
        try {
            eventoService.gerarMesCompleto(ano, mes);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agenda do mês gerada com sucesso! Agora é só preencher os valores.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao gerar mês: " + e.getMessage());
        }
        return "redirect:/eventos";
    }

    @PostMapping("/eventos/atualizar-campo")
    @ResponseBody
    public java.util.Map<String, Object> atualizarCampoAjax(@RequestParam Long id, @RequestParam String campo, @RequestParam String valor) {
        com.titu.core.model.Evento evento = eventoRepository.findById(id).orElseThrow();

        if (campo.equals("tipoEvento")) {
            // Busca o objeto da tabela nova pelo ID que veio do JavaScript
            com.titu.core.model.TipoEvento tipo = tipoEventoService.listarTodos().stream()
                    .filter(t -> t.getId().toString().equals(valor))
                    .findFirst().orElseThrow();
            evento.setTipoEvento(tipo);
        } else {
            // Converte o texto do JS para dinheiro no Java
            java.math.BigDecimal num = new java.math.BigDecimal(valor.isEmpty() ? "0" : valor.replace(",", "."));
            switch(campo) {
                case "receitaBar": evento.setReceitaBar(num); break;
                case "custoProblemas": evento.setCustoProblemas(num); break;
                case "custoDiarias": evento.setCustoDiarias(num); break;
                case "custoPromoters": evento.setCustoPromoters(num); break;
                case "custoDjPagode": evento.setCustoDjPagode(num); break;
                case "custoSeguranca": evento.setCustoSeguranca(num); break;
                case "custoSom": evento.setCustoSom(num); break;
                case "provisaoCustoBar": evento.setProvisaoCustoBar(num); break;
                case "provisaoSocios": evento.setProvisaoSocios(num); break;
                case "provisaoDecoracao": evento.setProvisaoDecoracao(num); break;
                case "provisaoTaxa": evento.setProvisaoTaxa(num); break;
            }
        }

        eventoService.salvar(evento); // Salva as mudanças no banco

        // Devolve os totais calculados na hora para o navegador!
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalCustos", evento.getTotalCustos());
        response.put("lucroDoDia", evento.getLucroDoDia());
        response.put("margem", evento.getMargem());
        return response;
    }

    @PostMapping("/eventos/apagar-mes")
    public String apagarMesCompleto(@RequestParam int ano, @RequestParam int mes, RedirectAttributes redirectAttributes) {
        try {
            eventoService.apagarMesCompleto(ano, mes);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Todos os eventos do mês " + mes + "/" + ano + " foram apagados com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao apagar o mês.");
        }
        return "redirect:/eventos";
    }

    @PostMapping("/tipos-evento/salvar")
    public String salvarTipoEvento(com.titu.core.model.TipoEvento tipoEvento, RedirectAttributes redirectAttributes) {
        try {
            tipoEventoService.salvar(tipoEvento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Novo tipo de evento cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/eventos";
    }

    // Injete o serviço lá no topo da classe
    private final com.titu.core.service.DashboardService dashboardService;

    // --- ROTA DO DASHBOARD ---
    @GetMapping("/dashboard")
    public String paginaDashboard(@RequestParam(required = false) Integer mes,
                                  @RequestParam(required = false) Integer ano,
                                  Model model) {

        // Se ele clicar no menu lateral sem filtro, joga pro mês/ano atual
        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        model.addAttribute("dadosDashboard", dashboardService.processarDashboardDoMes(ano, mes));
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "dashboard";
    }

    // =========================================================================
    // ROTAS DE CONFIGURAÇÕES & PAINEL DE CONTROLE
    // =========================================================================

    @GetMapping("/configuracoes")
    public String paginaConfiguracoes(Model model) {
        model.addAttribute("tiposEvento", tipoEventoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("tiposCategoria", com.titu.core.model.TipoCategoria.values());
        return "configuracoes";
    }

    @GetMapping("/tipos-evento/excluir/{id}")
    public String excluirTipoEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tipoEventoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Tipo de evento removido do sistema!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Não é possível excluir: Este tipo de festa já possui eventos lançados no Diário.");
        }
        return "redirect:/configuracoes";
    }

    @GetMapping("/categorias/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria de despesa excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Não é possível excluir: Esta categoria possui contas vinculadas na aba de Despesas.");
        }
        return "redirect:/configuracoes";
    }


}