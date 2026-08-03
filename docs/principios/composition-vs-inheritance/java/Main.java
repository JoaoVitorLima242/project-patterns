// Composição vs. Herança — capacidade se resolve compondo, identidade com contrato.
// Rodar: java Main.java   (JDK 17+, que executa um .java direto, sem compilar antes)
//
// O "antes" herda o serviço de e-mail só para ganhar enviar(). Junto com o
// método vêm a identidade (CriarConta vira um ServicoDeEmail), o vazamento do
// público do pai para a fachada, e o gasto da única vaga de herança — que em
// Java é literal: CriarContaAntes não pode estender mais nada.
//
// Main vem primeiro porque o launcher de arquivo único executa a PRIMEIRA
// classe declarada.

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ANTES: herda o serviço de e-mail para ganhar enviar() ===");
        CriarContaAntes antes = new CriarContaAntes();
        System.out.println(antes.exec("ana@exemplo.com"));

        // O público do pai vazou para a fachada do caso de uso.
        System.out.println("✗ vazou: dá para chamar criarConta.enviar() de fora do caso de uso");
        antes.enviar("qualquer@exemplo.com", "Isto não devia ser possível");

        System.out.println();
        System.out.println("=== DEPOIS: implementa o contrato, injeta a capacidade ===");
        System.out.println(new CriarConta(new EmailSmtp()).exec("bruno@exemplo.com"));
        System.out.println(new CriarConta(new EmailSilencioso()).exec("carla@exemplo.com"));

        System.out.println();
        System.out.println("✓ a fachada do caso de uso tem um método só: exec");
    }
}

// ============================================================================
// ANTES — herda o serviço de e-mail para ganhar enviar()
// ============================================================================

class ServicoDeEmail {
    void enviar(String para, String assunto) {
        System.out.println("[email] para=" + para + " assunto=" + assunto);
    }
}

// Herdou só para reusar enviar(). Mas levou a identidade junto: agora
// CriarConta É um ServicoDeEmail, o que não é verdade em lugar nenhum.
class CriarContaAntes extends ServicoDeEmail {
    String exec(String email) {
        enviar(email, "Bem-vinda");
        return "Conta criada: " + email;
    }
}

// ============================================================================
// DEPOIS — implementa o contrato, injeta a capacidade
// ============================================================================

// Identidade: o que faz esta classe SER um caso de uso.
// Como é interface, ela dá o tipo sem trazer implementação nenhuma junto.
interface CasoDeUso<I, O> {
    O exec(I input);
}

// Capacidade: o que a classe precisa TER para fazer o trabalho.
interface EnviadorDeEmail {
    void enviar(String para, String assunto);
}

class CriarConta implements CasoDeUso<String, String> {
    private final EnviadorDeEmail email;

    // O e-mail entra como campo: o caso de uso TEM um enviador, não É um.
    CriarConta(EnviadorDeEmail email) {
        this.email = email;
    }

    @Override
    public String exec(String destinatario) {
        email.enviar(destinatario, "Bem-vindo");
        return "Conta criada: " + destinatario;
    }
}

// Dois adapters. Trocar de um para o outro não encosta no CriarConta.
class EmailSmtp implements EnviadorDeEmail {
    @Override
    public void enviar(String para, String assunto) {
        System.out.println("[email] para=" + para + " assunto=" + assunto);
    }
}

class EmailSilencioso implements EnviadorDeEmail {
    @Override
    public void enviar(String para, String assunto) {
        System.out.println("[teste] e-mail suprimido: " + para);
    }
}
