package br.com.stockfarma.exception;

public class MedicamentoNaoEncontradoExcepion extends RuntimeException {
    public MedicamentoNaoEncontradoExcepion(String codigo) {
        super("Medicamento não encontrado" + codigo);
    }
}
