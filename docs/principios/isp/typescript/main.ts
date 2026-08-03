// ISP — o contrato não pode obrigar ninguém a assinar o que não faz.
// Rodar: node main.ts   (Node 22.18+ ou 23.6+, que executa .ts direto)
//
// Mesmo Animal da página do LSP, resolvido pelo outro lado: em vez de subir a
// promessa para mover(), aqui a gente fatia o contrato em capacidades.
//
// Repare que as capacidades se CRUZAM: cachorro anda e nada, pássaro anda e
// voa, peixe só nada. Não existe hierarquia que dê conta disso — só fatiar dá.

// ============================================================================
// ANTES — um contrato só, com tudo que algum animal faz
//
// Como o contrato é único, todo animal precisa assinar as três capacidades,
// mesmo tendo só uma ou duas.
// ============================================================================

interface AnimalAntes {
  nome: string;
  andar(): string;
  nadar(): string;
  voar(): string;
}

class CachorroAntes implements AnimalAntes {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  andar(): string {
    return "andando";
  }
  nadar(): string {
    return "nadando";
  }
  // Cachorro não voa, e o contrato exige. Só resta mentir.
  voar(): string {
    throw new Error("cachorro não voa");
  }
}

class PeixeAntes implements AnimalAntes {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  // Duas das três capacidades são impossíveis para o peixe.
  andar(): string {
    throw new Error("peixe não anda");
  }
  nadar(): string {
    return "nadando";
  }
  voar(): string {
    throw new Error("peixe não voa");
  }
}

// Quem organiza a corrida só precisa de andar() — mas recebe o contrato inteiro,
// e não tem como saber quem realmente anda antes de chamar e quebrar.
function corridaAntes(participantes: AnimalAntes[]): string[] {
  return participantes.map((animal) => {
    try {
      return `${animal.nome} ${animal.andar()}`;
    } catch (erro) {
      return `${animal.nome} ✗ ERRO: ${(erro as Error).message}`;
    }
  });
}

// ============================================================================
// DEPOIS — cada capacidade no seu contrato
//
// Animal fica só com o que vale para todos. Cada habilidade vira um contrato
// próprio, e o animal assina apenas os que consegue cumprir.
// ============================================================================

interface Animal {
  nome: string;
}

interface Andante {
  andar(): string;
}

interface Nadante {
  nadar(): string;
}

interface Voador {
  voar(): string;
}

class Cachorro implements Animal, Andante, Nadante {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  andar(): string {
    return "andando";
  }
  nadar(): string {
    return "nadando";
  }
}

// Assina um contrato só. Não sobrou nada para mentir.
class Peixe implements Animal, Nadante {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  nadar(): string {
    return "nadando";
  }
}

class Passaro implements Animal, Andante, Voador {
  nome: string;

  constructor(nome: string) {
    this.nome = nome;
  }

  andar(): string {
    return "andando";
  }
  voar(): string {
    return "voando";
  }
}

// O cliente pede exatamente o que usa. `&` é interseção de contratos: precisa
// ser as duas coisas ao mesmo tempo. Passar um Peixe aqui não compila —
// o erro saiu do runtime e virou erro de tipo.
function corrida(participantes: (Animal & Andante)[]): string[] {
  return participantes.map((animal) => `${animal.nome} ${animal.andar()}`);
}

function travessia(participantes: (Animal & Nadante)[]): string[] {
  return participantes.map((animal) => `${animal.nome} ${animal.nadar()}`);
}

// --- demonstração ---

const rexAntes = new CachorroAntes("Rex");
const nemoAntes = new PeixeAntes("Nemo");

console.log("=== ANTES: um contrato só, com tudo que algum animal faz ===");
for (const linha of corridaAntes([rexAntes, nemoAntes])) {
  console.log(linha);
}

const rex = new Cachorro("Rex");
const nemo = new Peixe("Nemo");
const blu = new Passaro("Blu");

console.log();
console.log("=== DEPOIS: cada capacidade no seu contrato ===");
console.log(`corrida   → ${corrida([rex, blu]).join(" · ")}`);
console.log(`travessia → ${travessia([rex, nemo]).join(" · ")}`);

console.log();
console.log("✓ Nemo não implementa Andante, então nem chega a ser inscrito na corrida");
