"""Composição vs. Herança — capacidade se resolve compondo, identidade com contrato.

Rodar: python3 main.py   (Python 3.10+)

O "antes" herda o serviço de e-mail só para ganhar enviar(). Junto com o método
vêm a identidade (CriarConta vira um ServicoDeEmail) e o vazamento do público do
pai para a fachada.

Diferença para TypeScript e Java: Python tem herança múltipla, então a "vaga de
herança" não é uma só. Os outros dois problemas continuam iguais — e o contrato
aqui é `Protocol`, satisfeito estruturalmente, sem declarar nada.
"""

from typing import Protocol, TypeVar

# ============================================================================
# ANTES — herda o serviço de e-mail para ganhar enviar()
# ============================================================================


class ServicoDeEmail:
    def enviar(self, para: str, assunto: str) -> None:
        print(f"[email] para={para} assunto={assunto}")


class CriarContaAntes(ServicoDeEmail):
    """Herdou só para reusar enviar().

    Mas levou a identidade junto: agora CriarConta É um ServicoDeEmail, o que
    não é verdade em lugar nenhum.
    """

    def exec(self, email: str) -> str:
        self.enviar(email, "Bem-vinda")
        return f"Conta criada: {email}"


# ============================================================================
# DEPOIS — implementa o contrato, injeta a capacidade
# ============================================================================

I = TypeVar("I")
O = TypeVar("O")


class CasoDeUso(Protocol[I, O]):
    """Identidade: o que faz esta classe SER um caso de uso."""

    def exec(self, entrada: I) -> O: ...


class EnviadorDeEmail(Protocol):
    """Capacidade: o que a classe precisa TER para fazer o trabalho."""

    def enviar(self, para: str, assunto: str) -> None: ...


class CriarConta:
    # O e-mail entra como campo: o caso de uso TEM um enviador, não É um.
    def __init__(self, email: EnviadorDeEmail) -> None:
        self._email = email

    def exec(self, email: str) -> str:
        self._email.enviar(email, "Bem-vindo")
        return f"Conta criada: {email}"


# Dois adapters. Trocar de um para o outro não encosta no CriarConta.
class EmailSmtp:
    def enviar(self, para: str, assunto: str) -> None:
        print(f"[email] para={para} assunto={assunto}")


class EmailSilencioso:
    def enviar(self, para: str, assunto: str) -> None:
        print(f"[teste] e-mail suprimido: {para}")


# --- demonstração ---


def main() -> None:
    print("=== ANTES: herda o serviço de e-mail para ganhar enviar() ===")
    antes = CriarContaAntes()
    print(antes.exec("ana@exemplo.com"))

    # O público do pai vazou para a fachada do caso de uso.
    print("✗ vazou: dá para chamar criarConta.enviar() de fora do caso de uso")
    antes.enviar("qualquer@exemplo.com", "Isto não devia ser possível")

    print()
    print("=== DEPOIS: implementa o contrato, injeta a capacidade ===")
    print(CriarConta(EmailSmtp()).exec("bruno@exemplo.com"))
    print(CriarConta(EmailSilencioso()).exec("carla@exemplo.com"))

    print()
    print("✓ a fachada do caso de uso tem um método só: exec")


if __name__ == "__main__":
    main()
