"""LSP — o contrato do pai só pode prometer o que vale para TODOS os filhos.

Rodar: python3 main.py   (Python 3.10+)

O "antes" põe andar() em Animal. Cachorro cumpre, peixe não tem como — e o
conserto não está no peixe, está em Animal, que prometeu demais.
"""

from abc import ABC, abstractmethod

# ============================================================================
# ANTES — Animal promete andar()
#
# "Animal anda" parece uma verdade universal, e não é. Peixe pertence ao
# conjunto Animal, mas não cabe no contrato: para herdar, é obrigado a
# quebrá-lo.
# ============================================================================


class AnimalAntes(ABC):
    def __init__(self, nome: str) -> None:
        self.nome = nome

    @abstractmethod
    def andar(self) -> str: ...


class CachorroAntes(AnimalAntes):
    def andar(self) -> str:
        return "andando"


class PeixeAntes(AnimalAntes):
    # Não existe implementação honesta aqui. Peixe é animal, mas não anda —
    # a única saída é violar o que o pai prometeu por ele.
    def andar(self) -> str:
        raise NotImplementedError("peixe não anda")


def passear_antes(animais: list[AnimalAntes]) -> list[str]:
    """Quem consome só conhece AnimalAntes e confia na promessa do pai.

    É esse código que a violação quebra, não a subclasse.
    """
    linhas = []
    for animal in animais:
        try:
            linhas.append(f"{animal.nome} se locomove {animal.andar()}")
        except NotImplementedError as erro:
            linhas.append(f"{animal.nome} ✗ ERRO: {erro}")
    return linhas


# ============================================================================
# DEPOIS — Animal promete mover()
#
# O que vale para todo animal não é "anda", é "se move". Subindo o contrato
# para esse nível, cada subclasse cumpre do seu jeito e nenhuma precisa mentir.
# ============================================================================


class Animal(ABC):
    def __init__(self, nome: str) -> None:
        self.nome = nome

    # A promessa agora é verdadeira para TODO elemento do conjunto Animal.
    @abstractmethod
    def mover(self) -> str: ...


class Cachorro(Animal):
    def mover(self) -> str:
        return "andando"


class Peixe(Animal):
    def mover(self) -> str:
        return "nadando"


class Passaro(Animal):
    # Entrou por último e não exigiu mudança em nada acima dele.
    def mover(self) -> str:
        return "voando"


def passear(animais: list[Animal]) -> list[str]:
    """Sem try/except: o contrato do pai virou verdade, não há o que dar errado."""
    return [f"{animal.nome} se locomove {animal.mover()}" for animal in animais]


# --- demonstração ---


def main() -> None:
    print("=== ANTES: andar() mora em Animal ===")
    for linha in passear_antes([CachorroAntes("Rex"), PeixeAntes("Nemo")]):
        print(linha)

    print()
    print("=== DEPOIS: Animal promete mover(), cada um cumpre do seu jeito ===")
    for linha in passear([Cachorro("Rex"), Peixe("Nemo"), Passaro("Blu")]):
        print(linha)

    print()
    print("✓ nenhum subtipo precisou quebrar o contrato do pai")


if __name__ == "__main__":
    main()
