package com.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // Quando o usuario acessar http://localhost:8080/eventos
    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        // 1. Busca todos os eventos do banco de dados
        List<Evento> lista = eventoRepository.findAll();
        
        // 2. Envia a lista de eventos para a pagina HTML com o nome "meusEventos"
        model.addAttribute("eventos", lista);
        
        // 3. Abre o arquivo chamado "lista-eventos.html"
        return "lista-eventos";
    }
    
 // Rota que recebe o ID do evento pela URL (Ex: /eventos/comprar/1)
    @GetMapping("/eventos/comprar/{id}")
    public String abrirCheckout(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento != null) {
            model.addAttribute("evento", evento);
            return "checkout";
        }
        return "redirect:/eventos";
    }

    // 2. Nova rota POST que processa a quantidade e calcula o valor total
    @org.springframework.web.bind.annotation.PostMapping("/eventos/confirmar-compra")
    public String confirmarCompra(
            @org.springframework.web.bind.annotation.RequestParam("idEvento") Long idEvento,
            @org.springframework.web.bind.annotation.RequestParam("quantidade") Integer quantidade,
            Model model) {
        
        Evento evento = eventoRepository.findById(idEvento).orElse(null);
        
        if (evento != null) {
            // Validacao de estoque
            if (quantidade > evento.getQuantidadeDisponivel()) {
                model.addAttribute("evento", evento);
                model.addAttribute("erro", "Desculpe! Estoque insuficiente. Temos apenas " + evento.getQuantidadeDisponivel() + " disponíveis.");
                return "checkout"; // Devolve para a tela de checkout mostrando o erro
            }
            
            // LOGICA DA COMPRA:
            // 1. Subtrai a quantidade do estoque do banco de dados
            evento.setQuantidadeDisponivel(evento.getQuantidadeDisponivel() - quantidade);
            eventoRepository.save(evento); // Atualiza o MySQL automaticamente
            
            // 2. Calcula o valor total multiplicando a quantidade pelo preco base
            Double valorTotal = evento.getPrecoBase() * quantidade;
            
            // 3. Passa as informacoes para a tela de sucesso
            model.addAttribute("evento", evento);
            model.addAttribute("quantidadeComprada", quantidade);
            model.addAttribute("valorTotal", valorTotal);
            
            return "sucesso-compra";
        }
        
        return "redirect:/eventos";
    	}
    
    @GetMapping("/eventos/novo")
    public String exibirFormularioCadastro() {
        return "novo-evento";
    }

    // 2. Rota que recebe os dados digitados e salva no MySQL
    @org.springframework.web.bind.annotation.PostMapping("/eventos/salvar")
    public String salvarNovoEvento(
            @org.springframework.web.bind.annotation.RequestParam("nome") String nome,
            @org.springframework.web.bind.annotation.RequestParam("local") String local,
            @org.springframework.web.bind.annotation.RequestParam("dataEvento") String dataTexto,
            @org.springframework.web.bind.annotation.RequestParam("precoBase") Double precoBase,
            @org.springframework.web.bind.annotation.RequestParam("quantidadeDisponivel") Integer quantidadeDisponivel,
            @org.springframework.web.bind.annotation.RequestParam("nomeImagem") String nomeImagem,
            @org.springframework.web.bind.annotation.RequestParam("artistas") String artistas,
            @org.springframework.web.bind.annotation.RequestParam("maioresSucessos") String maioresSucessos,
            @org.springframework.web.bind.annotation.RequestParam("linkMapa") String linkMapa,
            @org.springframework.web.bind.annotation.RequestParam("vezesEmSp") Integer vezesEmSp,
            @org.springframework.web.bind.annotation.RequestParam("tiposIngresso") String tiposIngresso) {
        
        // O HTML envia a data como texto, precisamos converter para LocalDateTime do Java
        java.time.LocalDateTime dataConvertida = java.time.LocalDateTime.parse(dataTexto);
        
        // Cria o objeto Evento com o que voce digitou na tela
        Evento novoEvento = new Evento(nome, dataConvertida, local, precoBase, quantidadeDisponivel, nomeImagem, artistas, maioresSucessos, linkMapa, vezesEmSp, tiposIngresso);
        
        // Salva no banco de dados!
        eventoRepository.save(novoEvento);
        
        // Redireciona de volta para a lista de eventos, que ja vai aparecer atualizada
        return "redirect:/eventos";
    }
    
    @GetMapping("/eventos/detalhes/{id}")
    public String exibirDetalhes(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento != null) {
            model.addAttribute("evento", evento);
            return "detalhes-evento"; // Nome do novo HTML que vamos criar
        }
        return "redirect:/eventos";
    }
    
}
