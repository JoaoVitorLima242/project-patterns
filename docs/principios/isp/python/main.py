"""ISP — o contrato não pode obrigar ninguém a assinar o que não faz.

Rodar: python3 main.py   (Python 3.10+)

Mesmo Animal da página do LSP, resolvido pelo outro lado: em vez de subir a
promessa para mover(), aqui a gente fatia o contrato em capacidades.

Repare que as capacidades se CRUZAM: cachorro anda e nada, pássaro anda e voa,
peixe só nada. Não existe hierarquia que dê conta disso — só fatiar dá.

Diferença para TypeScript e Java: aqui os contratos são `Protocol`, satisfeitos
estruturalmente. A classe não declara que implementa — basta ter os métodos, e
quem confere é o type checker (mypy, pyright), não o runtime.
"""

from typing import Protocol

# ============================================================================
# ANTES — um contrato só, com tudo que algum animal faz
#
# Como o contrato é único, todo animal precisa assinar as três capacidades,
# mesmo tendo só uma ou duas.
# ============================================================================


class AnimalAntes(Protocol):
    nome: str

    def andar(self) -> str: ...
    def nadar(self) -> str: ...
    def voar(self) -> str: ...


class CachorroAntes:
    def __init__(self, nome: str) -> None:
        self.nome = nome

    def andar(self) -> str:
        return "andando"

    def nadar(self) -> str:
        return "nadando"

    # Cachorro não voa, e o contrato exige. Só resta mentir.
    def voar(self) -> str:
        raise NotImplementedError("cachorro não voa")


class PeixeAntes:
    def __init__(self, nome: str) -> None:
        self.nome = nome

    # Duas das três capacidades são impossíveis para o peixe.
    def andar(self) -> str:
        raise NotImplementedError("peixe não anda")

    def nadar(self) -> str:
        return "nadando"

    def voar(self) -> str:
        raise NotImplementedError("peixe não voa")


def corrida_antes(participantes: list[AnimalAntes]) -> list[str]:
    """Quem organiza a corrida só precisa de andar().

    Mas recebe o contrato inteiro, e não tem como saber quem realmente anda
    antes de chamar e quebrar.
    """
    linhas = []
    for animal in participantes:
        try:
            linhas.append(f"{animal.nome} {animal.andar()}")
        except NotImplementedError as erro:
            linhas.append(f"{animal.nome} ✗ ERRO: {erro}")
    return linhas


# ============================================================================
# DEPOIS — cada capacidade no seu contrato
#
# Animal fica só com o que vale para todos. Cada habilidade vira um contrato
# próprio, e o animal assina apenas os que consegue cumprir.
# ============================================================================


class Animal(Protocol):
    nome: str


class Andante(Protocol):
    def andar(self) -> str: ...


class Nadante(Protocol):
    def nadar(self) -> str: ...


class Voador(Protocol):
    def voar(self) -> str: ...


# A interseção de contratos, que em TypeScript é `A & B` e em Java é um bound
# genérico, aqui vira um Protocol que herda dos dois.
class AnimalAndante(Animal, Andante, Protocol): ...


class AnimalNadante(Animal, Nadante, Protocol): ...


class Cachorro:
    def __init__(self, nome: str) -> None:
        self.nome = nome

    def andar(self) -> str:
        return "andando"

    def nadar(self) -> str:
        return "nadando"


# Satisfaz um contrato só. Não sobrou nada para mentir.
class Peixe:
    def __init__(self, nome: str) -> None:
        self.nome = nome

    def nadar(self) -> str:
        return "nadando"


class Passaro:
    def __init__(self, nome: str) -> None:
        self.nome = nome

    def andar(self) -> str:
        return "andando"

    def voar(self) -> str:
        return "voando"


def corrida(participantes: list[AnimalAndante]) -> list[str]:
    """O cliente pede exatamente o que usa.

    Passar um Peixe aqui é erro de tipo — o checker acusa antes de rodar.
    """
    return [f"{animal.nome} {animal.andar()}" for animal in participantes]


def travessia(participantes: list[AnimalNadante]) -> list[str]:
    return [f"{animal.nome} {animal.nadar()}" for animal in participantes]


# --- demonstração ---


def main() -> None:
    print("=== ANTES: um contrato só, com tudo que algum animal faz ===")
    for linha in corrida_antes([CachorroAntes("Rex"), PeixeAntes("Nemo")]):
        print(linha)

    rex = Cachorro("Rex")
    nemo = Peixe("Nemo")
    blu = Passaro("Blu")

    print()
    print("=== DEPOIS: cada capacidade no seu contrato ===")
    print(f"corrida   → {' · '.join(corrida([rex, blu]))}")
    print(f"travessia → {' · '.join(travessia([rex, nemo]))}")

    print()
    print("✓ Nemo não implementa Andante, então nem chega a ser inscrito na corrida")


if __name__ == "__main__":
    main()
