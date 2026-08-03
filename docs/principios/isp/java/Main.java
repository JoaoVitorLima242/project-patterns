// ISP — o contrato não pode obrigar ninguém a assinar o que não faz.
// Rodar: java Main.java   (JDK 17+, que executa um .java direto, sem compilar antes)
//
// Mesmo Animal da página do LSP, resolvido pelo outro lado: em vez de subir a
// promessa para mover(), aqui a gente fatia o contrato em capacidades.
//
// Repare que as capacidades se CRUZAM: cachorro anda e nada, pássaro anda e
// voa, peixe só nada. Não existe hierarquia que dê conta disso — só fatiar dá.
//
// Main vem primeiro porque o launcher de arquivo único executa a PRIMEIRA
// classe declarada.

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ANTES: um contrato só, com tudo que algum animal faz ===");
        for (String linha : corridaAntes(List.of(new CachorroAntes("Rex"), new PeixeAntes("Nemo")))) {
            System.out.println(linha);
        }

        Cachorro rex = new Cachorro("Rex");
        Peixe nemo = new Peixe("Nemo");
        Passaro blu = new Passaro("Blu");

        System.out.println();
        System.out.println("=== DEPOIS: cada capacidade no seu contrato ===");
        System.out.println("corrida   → " + String.join(" · ", corrida(List.of(rex, blu))));
        System.out.println("travessia → " + String.join(" · ", travessia(List.of(rex, nemo))));

        System.out.println();
        System.out.println("✓ Nemo não implementa Andante, então nem chega a ser inscrito na corrida");
    }

    // Quem organiza a corrida só precisa de andar() — mas recebe o contrato
    // inteiro, e não tem como saber quem realmente anda antes de chamar e quebrar.
    static List<String> corridaAntes(List<AnimalAntes> participantes) {
        List<String> linhas = new ArrayList<>();
        for (AnimalAntes animal : participantes) {
            try {
                linhas.add(animal.nome() + " " + animal.andar());
            } catch (UnsupportedOperationException erro) {
                linhas.add(animal.nome() + " ✗ ERRO: " + erro.getMessage());
            }
        }
        return linhas;
    }

    // O cliente pede exatamente o que usa. O bound `Animal & Andante` é a
    // interseção de contratos: precisa ser as duas coisas ao mesmo tempo.
    // Passar um Peixe aqui não compila — o erro saiu do runtime e virou erro de tipo.
    static <T extends Animal & Andante> List<String> corrida(List<T> participantes) {
        List<String> linhas = new ArrayList<>();
        for (T animal : participantes) {
            linhas.add(animal.nome() + " " + animal.andar());
        }
        return linhas;
    }

    static <T extends Animal & Nadante> List<String> travessia(List<T> participantes) {
        List<String> linhas = new ArrayList<>();
        for (T animal : participantes) {
            linhas.add(animal.nome() + " " + animal.nadar());
        }
        return linhas;
    }
}

// ============================================================================
// ANTES — um contrato só, com tudo que algum animal faz
//
// Como o contrato é único, todo animal precisa assinar as três capacidades,
// mesmo tendo só uma ou duas.
// ============================================================================

interface AnimalAntes {
    String nome();

    String andar();

    String nadar();

    String voar();
}

class CachorroAntes implements AnimalAntes {
    private final String nome;

    CachorroAntes(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String andar() {
        return "andando";
    }

    @Override
    public String nadar() {
        return "nadando";
    }

    // Cachorro não voa, e o contrato exige. Só resta mentir.
    @Override
    public String voar() {
        throw new UnsupportedOperationException("cachorro não voa");
    }
}

class PeixeAntes implements AnimalAntes {
    private final String nome;

    PeixeAntes(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    // Duas das três capacidades são impossíveis para o peixe.
    @Override
    public String andar() {
        throw new UnsupportedOperationException("peixe não anda");
    }

    @Override
    public String nadar() {
        return "nadando";
    }

    @Override
    public String voar() {
        throw new UnsupportedOperationException("peixe não voa");
    }
}

// ============================================================================
// DEPOIS — cada capacidade no seu contrato
//
// Animal fica só com o que vale para todos. Cada habilidade vira um contrato
// próprio, e o animal assina apenas os que consegue cumprir.
// ============================================================================

interface Animal {
    String nome();
}

interface Andante {
    String andar();
}

interface Nadante {
    String nadar();
}

interface Voador {
    String voar();
}

class Cachorro implements Animal, Andante, Nadante {
    private final String nome;

    Cachorro(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String andar() {
        return "andando";
    }

    @Override
    public String nadar() {
        return "nadando";
    }
}

// Assina um contrato só além de Animal. Não sobrou nada para mentir.
class Peixe implements Animal, Nadante {
    private final String nome;

    Peixe(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String nadar() {
        return "nadando";
    }
}

class Passaro implements Animal, Andante, Voador {
    private final String nome;

    Passaro(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String andar() {
        return "andando";
    }

    @Override
    public String voar() {
        return "voando";
    }
}
