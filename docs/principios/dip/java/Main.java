// DIP — o caso de uso depende do contrato, não do banco.
// Rodar: java Main.java   (JDK 17+, que executa um .java direto, sem compilar antes)
//
// O "antes" instancia o Postgres dentro do caso de uso: não dá para testar sem
// banco e não dá para trocar o adapter. O "depois" recebe o contrato, e o mesmo
// caso de uso roda com qualquer implementação que o respeite.
//
// Os "bancos" aqui são simulados — guardam em memória e imprimem o que fariam.
// O que importa é para onde a dependência aponta, não o driver.
//
// Main vem primeiro porque o launcher de arquivo único executa a PRIMEIRA
// classe declarada.

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ANTES: o caso de uso instancia o banco dentro de si ===");
        System.out.println(new CriarUsuarioAntes().exec("ana@exemplo.com"));
        System.out.println("→ não dá para rodar este caso de uso sem um Postgres do outro lado");

        System.out.println();
        System.out.println("=== DEPOIS: o caso de uso recebe o contrato ===");

        // A fiação acontece aqui, no ponto de entrada — sem container nenhum.
        System.out.println(new CriarUsuario(new RepositorioPostgres()).exec("bruno@exemplo.com"));

        RepositorioEmMemoria emMemoria = new RepositorioEmMemoria();
        CriarUsuario criarUsuario = new CriarUsuario(emMemoria);
        System.out.println(criarUsuario.exec("bruno@exemplo.com"));
        System.out.println(criarUsuario.exec("bruno@exemplo.com"));

        System.out.println();
        System.out.println("✓ o mesmo CriarUsuario rodou com dois adapters, sem uma linha alterada");
    }
}

record Usuario(String email) {}

// ============================================================================
// ANTES — o caso de uso instancia o banco dentro de si
// ============================================================================

class PostgresAntes {
    private final Map<String, Usuario> tabela = new HashMap<>();

    Usuario buscarPorEmail(String email) {
        return tabela.get(email);
    }

    void salvar(Usuario usuario) {
        System.out.println("[postgres] INSERT INTO usuarios (email) VALUES ('" + usuario.email() + "')");
        tabela.put(usuario.email(), usuario);
    }
}

class CriarUsuarioAntes {
    // A dependência nasce aqui dentro. Quem consome o caso de uso não tem como
    // interferir: para rodar isto, é Postgres ou nada.
    private final PostgresAntes repositorio = new PostgresAntes();

    String exec(String email) {
        if (repositorio.buscarPorEmail(email) != null) {
            return "✗ e-mail já cadastrado: " + email;
        }
        repositorio.salvar(new Usuario(email));
        return "Usuário criado: " + email;
    }
}

// ============================================================================
// DEPOIS — o caso de uso recebe o contrato
// ============================================================================

// O contrato pertence à camada de cima: ele diz o que o caso de uso precisa,
// não o que um banco específico sabe fazer. Note que não tem nada de SQL aqui.
interface RepositorioDeUsuario {
    Usuario buscarPorEmail(String email);

    void salvar(Usuario usuario);
}

class CriarUsuario {
    private final RepositorioDeUsuario repositorio;

    // Injeção do contrato — não da implementação. É o tipo do parâmetro que
    // caracteriza o DIP, não o fato de vir pelo construtor.
    CriarUsuario(RepositorioDeUsuario repositorio) {
        this.repositorio = repositorio;
    }

    String exec(String email) {
        if (repositorio.buscarPorEmail(email) != null) {
            return "✗ e-mail já cadastrado: " + email;
        }
        repositorio.salvar(new Usuario(email));
        return "Usuário criado: " + email;
    }
}

// Adapters plugados na borda. O caso de uso não conhece nenhum dos dois.
class RepositorioPostgres implements RepositorioDeUsuario {
    private final Map<String, Usuario> tabela = new HashMap<>();

    @Override
    public Usuario buscarPorEmail(String email) {
        return tabela.get(email);
    }

    @Override
    public void salvar(Usuario usuario) {
        System.out.println("[postgres] INSERT INTO usuarios (email) VALUES ('" + usuario.email() + "')");
        tabela.put(usuario.email(), usuario);
    }
}

// A implementação que torna o teste trivial: sem banco, sem rede, sem mock.
class RepositorioEmMemoria implements RepositorioDeUsuario {
    private final Map<String, Usuario> usuarios = new HashMap<>();

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarios.get(email);
    }

    @Override
    public void salvar(Usuario usuario) {
        System.out.println("[memória] guardado: " + usuario.email());
        usuarios.put(usuario.email(), usuario);
    }
}
