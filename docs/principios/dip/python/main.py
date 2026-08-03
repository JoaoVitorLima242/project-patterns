"""DIP — o caso de uso depende do contrato, não do banco.

Rodar: python3 main.py   (Python 3.10+)

O "antes" instancia o Postgres dentro do caso de uso: não dá para testar sem
banco e não dá para trocar o adapter. O "depois" recebe o contrato, e o mesmo
caso de uso roda com qualquer implementação que o respeite.

Os "bancos" aqui são simulados — guardam em memória e imprimem o que fariam.
O que importa é para onde a dependência aponta, não o driver.

Diferença para TypeScript e Java: o contrato é um `Protocol`, satisfeito
estruturalmente. O adapter não declara que implementa nada — basta ter os
métodos, e quem confere é o type checker.
"""

from dataclasses import dataclass
from typing import Protocol


@dataclass(frozen=True)
class Usuario:
    email: str


# ============================================================================
# ANTES — o caso de uso instancia o banco dentro de si
# ============================================================================


class PostgresAntes:
    def __init__(self) -> None:
        self._tabela: dict[str, Usuario] = {}

    def buscar_por_email(self, email: str) -> Usuario | None:
        return self._tabela.get(email)

    def salvar(self, usuario: Usuario) -> None:
        print(f"[postgres] INSERT INTO usuarios (email) VALUES ('{usuario.email}')")
        self._tabela[usuario.email] = usuario


class CriarUsuarioAntes:
    def __init__(self) -> None:
        # A dependência nasce aqui dentro. Quem consome o caso de uso não tem
        # como interferir: para rodar isto, é Postgres ou nada.
        self._repositorio = PostgresAntes()

    def exec(self, email: str) -> str:
        if self._repositorio.buscar_por_email(email) is not None:
            return f"✗ e-mail já cadastrado: {email}"
        self._repositorio.salvar(Usuario(email))
        return f"Usuário criado: {email}"


# ============================================================================
# DEPOIS — o caso de uso recebe o contrato
# ============================================================================


class RepositorioDeUsuario(Protocol):
    """O contrato pertence à camada de cima.

    Ele diz o que o caso de uso precisa, não o que um banco específico sabe
    fazer. Note que não tem nada de SQL aqui.
    """

    def buscar_por_email(self, email: str) -> Usuario | None: ...
    def salvar(self, usuario: Usuario) -> None: ...


class CriarUsuario:
    # Injeção do contrato — não da implementação. É o tipo do parâmetro que
    # caracteriza o DIP, não o fato de vir pelo construtor.
    def __init__(self, repositorio: RepositorioDeUsuario) -> None:
        self._repositorio = repositorio

    def exec(self, email: str) -> str:
        if self._repositorio.buscar_por_email(email) is not None:
            return f"✗ e-mail já cadastrado: {email}"
        self._repositorio.salvar(Usuario(email))
        return f"Usuário criado: {email}"


# Adapters plugados na borda. O caso de uso não conhece nenhum dos dois.
class RepositorioPostgres:
    def __init__(self) -> None:
        self._tabela: dict[str, Usuario] = {}

    def buscar_por_email(self, email: str) -> Usuario | None:
        return self._tabela.get(email)

    def salvar(self, usuario: Usuario) -> None:
        print(f"[postgres] INSERT INTO usuarios (email) VALUES ('{usuario.email}')")
        self._tabela[usuario.email] = usuario


class RepositorioEmMemoria:
    """A implementação que torna o teste trivial: sem banco, sem rede, sem mock."""

    def __init__(self) -> None:
        self._usuarios: dict[str, Usuario] = {}

    def buscar_por_email(self, email: str) -> Usuario | None:
        return self._usuarios.get(email)

    def salvar(self, usuario: Usuario) -> None:
        print(f"[memória] guardado: {usuario.email}")
        self._usuarios[usuario.email] = usuario


# --- demonstração ---


def main() -> None:
    print("=== ANTES: o caso de uso instancia o banco dentro de si ===")
    print(CriarUsuarioAntes().exec("ana@exemplo.com"))
    print("→ não dá para rodar este caso de uso sem um Postgres do outro lado")

    print()
    print("=== DEPOIS: o caso de uso recebe o contrato ===")

    # A fiação acontece aqui, no ponto de entrada — sem container nenhum.
    print(CriarUsuario(RepositorioPostgres()).exec("bruno@exemplo.com"))

    em_memoria = RepositorioEmMemoria()
    criar_usuario = CriarUsuario(em_memoria)
    print(criar_usuario.exec("bruno@exemplo.com"))
    print(criar_usuario.exec("bruno@exemplo.com"))

    print()
    print("✓ o mesmo CriarUsuario rodou com dois adapters, sem uma linha alterada")


if __name__ == "__main__":
    main()
