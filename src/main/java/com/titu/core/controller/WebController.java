package com.titu.core.controller;

import com.titu.core.model.Cliente;
import com.titu.core.model.Despesa;
import com.titu.core.model.Evento;
import com.titu.core.repository.EventoRepository;
import com.titu.core.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ClienteService clienteService;
    private final DespesaService despesaService;
    private final CategoriaService categoriaService;
    private final EventoService eventoService;
    private final EventoRepository eventoRepository;
    private final TipoEventoService tipoEventoService;
    private final FechamentoService fechamentoService;
    private final DashboardService dashboardService;
    private final ColunaDiarioService colunaDiarioService;

    // =========================================================================
    // ROTAS PRINCIPAIS & DASHBOARD
    // =========================================================================

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
        return "cliente-detalhes";
    }

    // =========================================================================
    // ROTAS DE DESPESAS (ABA 3 DA PLANILHA)
    // =========================================================================

    // ROTA 1: A ABA HISTÓRICO DE SAÍDAS (Status = PAGO)
    @GetMapping("/despesas")
    public String paginaDespesasHistorico(@RequestParam(required = false) Integer mes,
                                          @RequestParam(required = false) Integer ano,
                                          Model model) {
        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        // Busca TUDO do mês para a matemática do Dashboard
        List<Despesa> todasDespesasDoMes = despesaService.listarPorMesEAno(ano, mes);

        // Separa APENAS o que foi pago para mostrar na tabela desta tela
        List<Despesa> despesasPagas = todasDespesasDoMes.stream()
                .filter(d -> d.getStatus().name().equals("PAGO"))
                .toList();

        BigDecimal totalPago = despesasPagas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("despesas", despesasPagas);
        model.addAttribute("totalPago", totalPago);
        model.addAttribute("totalLancamentos", despesasPagas.size());

        // Dados para o modal de lançamento
        model.addAttribute("fornecedores", clienteService.listarSomenteFornecedores());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("formasPagamento", com.titu.core.model.FormaPagamento.values());
        model.addAttribute("tiposCategoria", com.titu.core.model.TipoCategoria.values());

        model.addAttribute("mesTrancado", fechamentoService.isMesTrancado(mes, ano));
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "despesas"; // Aponta para o arquivo original que limparemos
    }

    // ROTA 2: A NOVA ABA DE CONTAS A PAGAR (Status = PENDENTE)
    @GetMapping("/contas-a-pagar")
    public String paginaContasAPagar(@RequestParam(required = false) Integer mes,
                                     @RequestParam(required = false) Integer ano,
                                     Model model) {
        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        List<Despesa> todasDespesasDoMes = despesaService.listarPorMesEAno(ano, mes);

        List<Despesa> despesasPendentes = todasDespesasDoMes.stream()
                .filter(d -> d.getStatus().name().equals("PENDENTE"))
                .toList();

        BigDecimal totalPendente = despesasPendentes.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("despesas", despesasPendentes);
        model.addAttribute("totalPendente", totalPendente);
        model.addAttribute("totalLancamentos", despesasPendentes.size());

        // Dados para o modal (mesmo da aba de saídas)
        model.addAttribute("fornecedores", clienteService.listarSomenteFornecedores());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("formasPagamento", com.titu.core.model.FormaPagamento.values());
        model.addAttribute("tiposCategoria", com.titu.core.model.TipoCategoria.values());

        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        return "contas-a-pagar"; // Aponta para o NOVO arquivo
    }



    @PostMapping("/categorias/salvar")
    public String salvarCategoria(com.titu.core.model.Categoria categoria, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.salvar(categoria);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Nova categoria adicionada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao criar categoria: " + e.getMessage());
        }

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
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Despesa marcada como PAGA! Movida para o Histórico de Saídas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao processar pagamento.");
        }
        return "redirect:/contas-a-pagar"; // Volta direto pra aba pendente!
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

        if (mes == null || ano == null) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            mes = hoje.getMonthValue();
            ano = hoje.getYear();
        }

        // Puxa as colunas do Cérebro
        List<com.titu.core.model.ColunaDiario> colunasDinamicas = colunaDiarioService.listarTodas();

        model.addAttribute("eventos", eventoService.listarPorMesEAno(ano, mes));
        model.addAttribute("colunasMapeadas", colunasDinamicas); // Manda a lista real pro HTML
        model.addAttribute("quantidadeColunas", colunasDinamicas.size());

        model.addAttribute("mesTrancado", fechamentoService.isMesTrancado(mes, ano));
        model.addAttribute("tiposEvento", tipoEventoService.listarTodos());
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
    public String salvarEvento(Evento evento, RedirectAttributes redirectAttributes) {
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
    public Map<String, Object> atualizarCampoAjax(@RequestParam Long id, @RequestParam String campo, @RequestParam String valor) {
        Evento evento = eventoRepository.findById(id).orElseThrow();

        if (campo.equals("tipoEvento")) {
            com.titu.core.model.TipoEvento tipo = tipoEventoService.listarTodos().stream()
                    .filter(t -> t.getId().toString().equals(valor))
                    .findFirst().orElseThrow();
            evento.setTipoEvento(tipo);
        } else {
            BigDecimal num = new BigDecimal(valor.isEmpty() ? "0" : valor.replace(",", "."));

            if (campo.equals("receitaBar")) {
                evento.setReceitaBar(num);
            } else {
                // MÁGICA DO JSONB: Se não for a receita, cai automaticamente como custo dinâmico!
                if (evento.getCustosDetalhados() == null) {
                    evento.setCustosDetalhados(new HashMap<>());
                }
                evento.getCustosDetalhados().put(campo, num);
            }
        }

        eventoService.salvar(evento);

        // Devolve os totais calculados dinamicamente na hora para o JavaScript atualizar a tela
        Map<String, Object> response = new HashMap<>();
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

    @GetMapping("/dashboard")
    public String paginaDashboard(@RequestParam(required = false) Integer mes,
                                  @RequestParam(required = false) Integer ano,
                                  Model model) {

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

        // Puxa o Molde do Diário para a tela
        model.addAttribute("colunasDiario", colunaDiarioService.listarTodas());

        return "configuracoes";
    }

    @PostMapping("/colunas-diario/salvar-config")
    public String salvarColunaConfig(com.titu.core.model.ColunaDiario colunaInput, RedirectAttributes redirectAttributes) {
        try {
            if (colunaInput.getId() != null) {
                // EDIÇÃO: Protege a chave do banco e o tipo, muda só o nome visual!
                com.titu.core.model.ColunaDiario existente = colunaDiarioService.buscarPorId(colunaInput.getId());
                existente.setNomeVisual(colunaInput.getNomeVisual());
                colunaDiarioService.salvar(existente);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Nome da coluna atualizado!");
            } else {
                // CRIAÇÃO NOVA PELO PAINEL
                String baseKey = java.text.Normalizer.normalize(colunaInput.getNomeVisual(), java.text.Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                        .replaceAll("[^a-zA-Z0-9 ]", "");
                String[] words = baseKey.split(" ");
                StringBuilder keyBuilder = new StringBuilder(words[0].toLowerCase());
                for (int i = 1; i < words.length; i++) {
                    if (words[i].length() > 0) {
                        keyBuilder.append(words[i].substring(0, 1).toUpperCase()).append(words[i].substring(1).toLowerCase());
                    }
                }
                colunaInput.setChaveBanco(keyBuilder.toString() + colunaInput.getTipo());
                colunaDiarioService.salvar(colunaInput);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Nova coluna adicionada ao Molde!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro: " + e.getMessage());
        }
        return "redirect:/configuracoes";
    }

    @GetMapping("/colunas-diario/excluir/{id}")
    public String excluirColunaConfig(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            colunaDiarioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Coluna removida do Molde!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir coluna.");
        }
        return "redirect:/configuracoes";
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

    @PostMapping("/eventos/limpar-linha")
    @ResponseBody
    public ResponseEntity<?> limparLinha(@RequestParam Long id) {
        Evento evento = eventoService.buscarPorId(id);

        // Zera o valor fixo
        evento.setReceitaBar(BigDecimal.ZERO);

        // Zera a gaveta de custos inteira com um único comando elegante!
        if (evento.getCustosDetalhados() != null) {
            evento.getCustosDetalhados().clear();
        } else {
            evento.setCustosDetalhados(new HashMap<>());
        }

        eventoService.salvar(evento);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/colunas-diario/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarNovaColuna(@RequestParam String nomeVisual, @RequestParam String tipo) {
        // Mágica para criar a chave do banco (ex: "Custo Bolo" vira "custoBolo")
        String baseKey = java.text.Normalizer.normalize(nomeVisual, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-zA-Z0-9 ]", "");
        String[] words = baseKey.split(" ");
        StringBuilder keyBuilder = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            if (words[i].length() > 0) {
                keyBuilder.append(words[i].substring(0, 1).toUpperCase()).append(words[i].substring(1).toLowerCase());
            }
        }

        com.titu.core.model.ColunaDiario nova = com.titu.core.model.ColunaDiario.builder()
                .nomeVisual(nomeVisual)
                .chaveBanco(keyBuilder.toString() + tipo)
                .tipo(tipo)
                .build();

        colunaDiarioService.salvar(nova);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/colunas-diario/editar")
    @ResponseBody
    public ResponseEntity<?> editarNomeColuna(@RequestParam Long id, @RequestParam String novoNome) {
        com.titu.core.model.ColunaDiario col = colunaDiarioService.buscarPorId(id);
        col.setNomeVisual(novoNome); // Edita SÓ O NOME visual
        colunaDiarioService.salvar(col);
        return ResponseEntity.ok().build();
    }


}