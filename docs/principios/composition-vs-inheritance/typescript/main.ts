// Composição vs. Herança — capacidade se resolve compondo, identidade com contrato.
// Rodar: node main.ts   (Node 22.18+ ou 23.6+, que executa .ts direto)
//
// O "antes" herda o serviço de e-mail só para ganhar enviar(). Junto com o
// método vêm a identidade (CriarConta vira um ServicoDeEmail), o vazamento do
// público do pai para a fachada, e o gasto da única vaga de herança.

// ============================================================================
// ANTES — herda o serviço de e-mail para ganhar enviar()
// ============================================================================

class ServicoDeEmail {
  enviar(para: string, assunto: string): void {
    console.log(`[email] para=${para} assunto=${assunto}`);
  }
}

// Herdou só para reusar enviar(). Mas levou a identidade junto: agora
// CriarConta É um ServicoDeEmail, o que não é verdade em lugar nenhum.
class CriarContaAntes extends ServicoDeEmail {
  exec(email: string): string {
    this.enviar(email, "Bem-vinda");
    return `Conta criada: ${email}`;
  }
}

// ============================================================================
// DEPOIS — implementa o contrato, injeta a capacidade
// ============================================================================

// Identidade: o que faz esta classe SER um caso de uso.
// Como é interface, ela dá o tipo sem trazer implementação nenhuma junto.
interface CasoDeUso<I, O> {
  exec(input: I): O;
}

// Capacidade: o que a classe precisa TER para fazer o trabalho.
interface EnviadorDeEmail {
  enviar(para: string, assunto: string): void;
}

class CriarConta implements CasoDeUso<string, string> {
  private email: EnviadorDeEmail;

  // O e-mail entra como campo: o caso de uso TEM um enviador, não É um.
  constructor(email: EnviadorDeEmail) {
    this.email = email;
  }

  exec(email: string): string {
    this.email.enviar(email, "Bem-vindo");
    return `Conta criada: ${email}`;
  }
}

// Dois adapters. Trocar de um para o outro não encosta no CriarConta.
class EmailSmtp implements EnviadorDeEmail {
  enviar(para: string, assunto: string): void {
    console.log(`[email] para=${para} assunto=${assunto}`);
  }
}

class EmailSilencioso implements EnviadorDeEmail {
  enviar(para: string): void {
    console.log(`[teste] e-mail suprimido: ${para}`);
  }
}

// --- demonstração ---

console.log("=== ANTES: herda o serviço de e-mail para ganhar enviar() ===");
const antes = new CriarContaAntes();
console.log(antes.exec("ana@exemplo.com"));

// O público do pai vazou para a fachada do caso de uso.
console.log("✗ vazou: dá para chamar criarConta.enviar() de fora do caso de uso");
antes.enviar("qualquer@exemplo.com", "Isto não devia ser possível");

console.log();
console.log("=== DEPOIS: implementa o contrato, injeta a capacidade ===");
console.log(new CriarConta(new EmailSmtp()).exec("bruno@exemplo.com"));
console.log(new CriarConta(new EmailSilencioso()).exec("carla@exemplo.com"));

console.log();
console.log("✓ a fachada do caso de uso tem um método só: exec");
