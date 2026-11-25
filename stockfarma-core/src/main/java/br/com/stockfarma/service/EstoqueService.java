package br.com.stockfarma.service;

import br.com.stockfarma.exception.EstoqueInsuficienteException;
import br.com.stockfarma.exception.MedicamentoNaoEncontradoExcepion;
import br.com.stockfarma.model.Categoria;
import br.com.stockfarma.model.Medicamento;
import br.com.stockfarma.model.Movimentacao;
import br.com.stockfarma.model.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EstoqueService {
    private final Map<String, Medicamento> medicamentos = new LinkedHashMap<>();
    private final Deque<Movimentacao> movimentacoes = new ArrayDeque<>();

    public void carregar(List<Medicamento> lista) {
        medicamentos.clear();
        movimentacoes.clear();
        for (Medicamento medicamento : lista) {
            medicamentos.put(medicamento.codigo(), medicamento);
        }
    }

    public List<Medicamento> listar() {
        return new ArrayList<>(medicamentos.values());
    }

    public Medicamento buscar(String codigo) {
        Medicamento medicamento = medicamentos.get(codigo);
        if (medicamento == null) {
            throw new MedicamentoNaoEncontradoExcepion(codigo);
        }
        return medicamento;
    }

    public void registrarEntrada(String codigo, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        Medicamento atual = buscar(codigo);
        Medicamento atualizado = atual.comQuantidade(atual.quantidade() + quantidade);
        medicamentos.put(codigo, atualizado);
        movimentacoes.push(new Movimentacao(TipoMovimentacao.ENTRADA, codigo,
                quantidade, LocalDateTime.now()));
    }

    public void registrarSaida(String codigo, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        Medicamento atual = buscar(codigo);
        if (atual.quantidade() < quantidade) {
            throw new EstoqueInsuficienteException(codigo, quantidade, atual.quantidade());
        }
        Medicamento atualizado = atual.comQuantidade(atual.quantidade() - quantidade);
        medicamentos.put(codigo, atualizado);
        movimentacoes.push(new Movimentacao(TipoMovimentacao.SAIDA, codigo,
                quantidade, LocalDateTime.now()));
    }

    public void desfazerUltima() {
        if (movimentacoes.isEmpty()) {
            return;
        }
        Movimentacao ultima = movimentacoes.pop();
        Medicamento atual = buscar(ultima.codigo());
        int quantidadeRevertida = ultima.tipo() == TipoMovimentacao.ENTRADA
                ? -ultima.quantidade()
                : ultima.quantidade();
        medicamentos.put(ultima.codigo(),
                atual.comQuantidade(atual.quantidade() + quantidadeRevertida));
    }

    public List<Categoria> categoriasComEstoqueBaixo(int limite) {
        return medicamentos.values().stream()
                .filter(medicamento -> medicamento.quantidade() < limite)
                .map(Medicamento::categoria)
                .distinct()
                .toList();
    }
}
