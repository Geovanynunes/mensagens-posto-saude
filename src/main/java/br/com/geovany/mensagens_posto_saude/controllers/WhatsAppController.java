package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.dto.EvolutionConnectionState;
import br.com.geovany.mensagens_posto_saude.dto.EvolutionQrCode;
import br.com.geovany.mensagens_posto_saude.service.EvolutionApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/whatsapp")
public class WhatsAppController {

    private final EvolutionApiService evolutionApiService;

    public WhatsAppController(EvolutionApiService evolutionApiService) {
        this.evolutionApiService = evolutionApiService;
    }

    @GetMapping("/qrcode")
    public String exibirQrCode(Model model) {

        EvolutionConnectionState conexao =
                evolutionApiService.consultarEstadoConexao();

        String estado = conexao.instance().state();

        model.addAttribute("estadoConexao", estado);

        if (estado.equals("connecting")) {
            EvolutionQrCode qrCode = evolutionApiService.gerarQrCode();
            model.addAttribute("qrCode", qrCode);
        }

        return "whatsapp";
    }

    @GetMapping("/status")
    @ResponseBody
    public EvolutionConnectionState consultarStatus() {
        return evolutionApiService.consultarEstadoConexao();
    }

    @GetMapping("/qrcode/gerar")
    @ResponseBody
    public EvolutionQrCode gerarQrCode() {
        return evolutionApiService.gerarQrCode();
    }

    @PostMapping("/desconectar")
    @ResponseBody
    public void desconectarWhatsApp() {
        evolutionApiService.desconectarWhatsApp();
    }

}