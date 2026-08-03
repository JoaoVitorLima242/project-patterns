// LSP — o contrato do pai só pode prometer o que vale para TODOS os filhos.
// Rodar: node main.ts   (Node 22.18+ ou 23.6+, que executa .ts direto)
//
// O "antes" põe andar() em Animal. Cachorro cumpre, peixe não tem como — e o
// conserto não está no peixe, está em Animal, que prometeu demais.

// ============================================================================
// ANTES — Animal promete andar()
//
// "Animal anda" parece uma verdade universal, e não é. Peixe pertence ao
// conjunto Animal, mas não cabe no contrato: para herdar, é obrigado a quebrá-lo.
// ============================================================================

abstract class AnimalAntes {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  abstract andar(): string;
}

class CachorroAntes extends AnimalAntes {
  andar(): string {
    return "andando";
  }
}

class PeixeAntes extends AnimalAntes {
  // Não existe implementação honesta aqui. Peixe é animal, mas não anda —
  // a única saída é violar o que o pai prometeu por ele.
  andar(): string {
    throw new Error("peixe não anda");
  }
}

// Quem consome só conhece AnimalAntes e confia na promessa do pai.
// É esse código que a violação quebra, não a subclasse.
function passearAntes(animais: AnimalAntes[]): string[] {
  return animais.map((animal) => {
    try {
      return `${animal.nome} se locomove ${animal.andar()}`;
    } catch (erro) {
      return `${animal.nome} ✗ ERRO: ${(erro as Error).message}`;
    }
  });
}

// ============================================================================
// DEPOIS — Animal promete mover()
//
// O que vale para todo animal não é "anda", é "se move". Subindo o contrato
// para esse nível, cada subclasse cumpre do seu jeito e nenhuma precisa mentir.
// ============================================================================

abstract class Animal {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  // A promessa agora é verdadeira para TODO elemento do conjunto Animal.
  abstract mover(): string;
}

class Cachorro extends Animal {
  mover(): string {
    return "andando";
  }
}

class Peixe extends Animal {
  mover(): string {
    return "nadando";
  }
}

// Entrou por último e não exigiu mudança em nada acima dele.
class Passaro extends Animal {
  mover(): string {
    return "voando";
  }
}

// Sem try/catch: o contrato do pai virou verdade, então não há o que dar errado.
function passear(animais: Animal[]): string[] {
  return animais.map((animal) => `${animal.nome} se locomove ${animal.mover()}`);
}

// --- demonstração ---

console.log("=== ANTES: andar() mora em Animal ===");
for (const linha of passearAntes([new CachorroAntes("Rex"), new PeixeAntes("Nemo")])) {
  console.log(linha);
}

console.log();
console.log("=== DEPOIS: Animal promete mover(), cada um cumpre do seu jeito ===");
for (const linha of passear([new Cachorro("Rex"), new Peixe("Nemo"), new Passaro("Blu")])) {
  console.log(linha);
}

console.log();
console.log("✓ nenhum subtipo precisou quebrar o contrato do pai");
