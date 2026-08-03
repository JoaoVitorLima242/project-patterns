// DIP — o caso de uso depende do contrato, não do banco.
// Rodar: node main.ts   (Node 22.18+ ou 23.6+, que executa .ts direto)
//
// O "antes" instancia o Postgres dentro do caso de uso: não dá para testar sem
// banco e não dá para trocar o adapter. O "depois" recebe o contrato, e o mesmo
// caso de uso roda com qualquer implementação que o respeite.
//
// Os "bancos" aqui são simulados — guardam em memória e imprimem o que fariam.
// O que importa é para onde a dependência aponta, não o driver.

type Usuario = { email: string };

// ============================================================================
// ANTES — o caso de uso instancia o banco dentro de si
// ============================================================================

class PostgresAntes {
  private tabela = new Map<string, Usuario>();

  buscarPorEmail(email: string): Usuario | null {
    return this.tabela.get(email) ?? null;
  }
  salvar(usuario: Usuario): void {
    console.log(`[postgres] INSERT INTO usuarios (email) VALUES ('${usuario.email}')`);
    this.tabela.set(usuario.email, usuario);
  }
}

class CriarUsuarioAntes {
  // A dependência nasce aqui dentro. Quem consome o caso de uso não tem como
  // interferir: para rodar isto, é Postgres ou nada.
  private repositorio = new PostgresAntes();

  exec(email: string): string {
    if (this.repositorio.buscarPorEmail(email) !== null) {
      return `✗ e-mail já cadastrado: ${email}`;
    }
    this.repositorio.salvar({ email });
    return `Usuário criado: ${email}`;
  }
}

// ============================================================================
// DEPOIS — o caso de uso recebe o contrato
// ============================================================================

// O contrato pertence à camada de cima: ele diz o que o caso de uso precisa,
// não o que um banco específico sabe fazer. Note que não tem nada de SQL aqui.
interface RepositorioDeUsuario {
  buscarPorEmail(email: string): Usuario | null;
  salvar(usuario: Usuario): void;
}

class CriarUsuario {
  private repositorio: RepositorioDeUsuario;

  // Injeção do contrato — não da implementação. É o tipo do parâmetro que
  // caracteriza o DIP, não o fato de vir pelo construtor.
  constructor(repositorio: RepositorioDeUsuario) {
    this.repositorio = repositorio;
  }

  exec(email: string): string {
    if (this.repositorio.buscarPorEmail(email) !== null) {
      return `✗ e-mail já cadastrado: ${email}`;
    }
    this.repositorio.salvar({ email });
    return `Usuário criado: ${email}`;
  }
}

// Adapters plugados na borda. O caso de uso não conhece nenhum dos dois.
class RepositorioPostgres implements RepositorioDeUsuario {
  private tabela = new Map<string, Usuario>();

  buscarPorEmail(email: string): Usuario | null {
    return this.tabela.get(email) ?? null;
  }
  salvar(usuario: Usuario): void {
    console.log(`[postgres] INSERT INTO usuarios (email) VALUES ('${usuario.email}')`);
    this.tabela.set(usuario.email, usuario);
  }
}

// A implementação que torna o teste trivial: sem banco, sem rede, sem mock.
class RepositorioEmMemoria implements RepositorioDeUsuario {
  private usuarios = new Map<string, Usuario>();

  buscarPorEmail(email: string): Usuario | null {
    return this.usuarios.get(email) ?? null;
  }
  salvar(usuario: Usuario): void {
    console.log(`[memória] guardado: ${usuario.email}`);
    this.usuarios.set(usuario.email, usuario);
  }
}

// --- demonstração ---

console.log("=== ANTES: o caso de uso instancia o banco dentro de si ===");
console.log(new CriarUsuarioAntes().exec("ana@exemplo.com"));
console.log("→ não dá para rodar este caso de uso sem um Postgres do outro lado");

console.log();
console.log("=== DEPOIS: o caso de uso recebe o contrato ===");

// A fiação acontece aqui, no ponto de entrada — sem container nenhum.
console.log(new CriarUsuario(new RepositorioPostgres()).exec("bruno@exemplo.com"));

const emMemoria = new RepositorioEmMemoria();
const criarUsuario = new CriarUsuario(emMemoria);
console.log(criarUsuario.exec("bruno@exemplo.com"));
console.log(criarUsuario.exec("bruno@exemplo.com"));

console.log();
console.log("✓ o mesmo CriarUsuario rodou com dois adapters, sem uma linha alterada");
