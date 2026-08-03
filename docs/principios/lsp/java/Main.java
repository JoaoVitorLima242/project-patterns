// LSP — o contrato do pai só pode prometer o que vale para TODOS os filhos.
// Rodar: java Main.java   (JDK 17+, que executa um .java direto, sem compilar antes)
//
// O "antes" põe andar() em Animal. Cachorro cumpre, peixe não tem como — e o
// conserto não está no peixe, está em Animal, que prometeu demais.
//
// Main vem primeiro porque o launcher de arquivo único executa a PRIMEIRA
// classe declarada.

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ANTES: andar() mora em Animal ===");
        for (String linha : passearAntes(List.of(new CachorroAntes("Rex"), new PeixeAntes("Nemo")))) {
            System.out.println(linha);
        }

        System.out.println();
        System.out.println("=== DEPOIS: Animal promete mover(), cada um cumpre do seu jeito ===");
        for (String linha : passear(List.of(new Cachorro("Rex"), new Peixe("Nemo"), new Passaro("Blu")))) {
            System.out.println(linha);
        }

        System.out.println();
        System.out.println("✓ nenhum subtipo precisou quebrar o contrato do pai");
    }

    // Quem consome só conhece AnimalAntes e confia na promessa do pai.
    // É esse código que a violação quebra, não a subclasse.
    static List<String> passearAntes(List<AnimalAntes> animais) {
        List<String> linhas = new ArrayList<>();
        for (AnimalAntes animal : animais) {
            try {
                linhas.add(animal.nome + " se locomove " + animal.andar());
            } catch (UnsupportedOperationException erro) {
                linhas.add(animal.nome + " ✗ ERRO: " + erro.getMessage());
            }
        }
        return linhas;
    }

    // Sem try/catch: o contrato do pai virou verdade, não há o que dar errado.
    static List<String> passear(List<Animal> animais) {
        List<String> linhas = new ArrayList<>();
        for (Animal animal : animais) {
            linhas.add(animal.nome + " se locomove " + animal.mover());
        }
        return linhas;
    }
}

// ============================================================================
// ANTES — Animal promete andar()
//
// "Animal anda" parece uma verdade universal, e não é. Peixe pertence ao
// conjunto Animal, mas não cabe no contrato: para herdar, é obrigado a quebrá-lo.
// ============================================================================

abstract class AnimalAntes {
    final String nome;

    AnimalAntes(String nome) {
        this.nome = nome;
    }

    abstract String andar();
}

class CachorroAntes extends AnimalAntes {
    CachorroAntes(String nome) {
        super(nome);
    }

    @Override
    String andar() {
        return "andando";
    }
}

class PeixeAntes extends AnimalAntes {
    PeixeAntes(String nome) {
        super(nome);
    }

    // Não existe implementação honesta aqui. Peixe é animal, mas não anda —
    // a única saída é violar o que o pai prometeu por ele.
    @Override
    String andar() {
        throw new UnsupportedOperationException("peixe não anda");
    }
}

// ============================================================================
// DEPOIS — Animal promete mover()
//
// O que vale para todo animal não é "anda", é "se move". Subindo o contrato
// para esse nível, cada subclasse cumpre do seu jeito e nenhuma precisa mentir.
// ============================================================================

abstract class Animal {
    final String nome;

    Animal(String nome) {
        this.nome = nome;
    }

    // A promessa agora é verdadeira para TODO elemento do conjunto Animal.
    abstract String mover();
}

class Cachorro extends Animal {
    Cachorro(String nome) {
        super(nome);
    }

    @Override
    String mover() {
        return "andando";
    }
}

class Peixe extends Animal {
    Peixe(String nome) {
        super(nome);
    }

    @Override
    String mover() {
        return "nadando";
    }
}

class Passaro extends Animal {
    Passaro(String nome) {
        super(nome);
    }

    // Entrou por último e não exigiu mudança em nada acima dele.
    @Override
    String mover() {
        return "voando";
    }
}
