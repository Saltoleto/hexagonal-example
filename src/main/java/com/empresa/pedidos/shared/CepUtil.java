package com.empresa.pedidos.shared;

/**
 * UTILITARIO — operacoes relacionadas a CEP.
 *
 * Classe utilitaria estatica — sem estado, sem Spring.
 * Fica em 'shared' pois pode ser usada por qualquer camada.
 *
 * Regra de negocio de formato (nao de dominio do pedido)
 * vive aqui para reuso.
 */
public final class CepUtil {

    private CepUtil() {
        // utilitario estatico — nao instanciar
    }

    /**
     * Remove caracteres nao numericos do CEP.
     */
    public static String limpar(String cep) {
        if (cep == null) return null;
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Formata o CEP no padrao 00000-000.
     */
    public static String formatar(String cep) {
        String limpo = limpar(cep);
        if (limpo == null || limpo.length() != 8) return cep;
        return limpo.substring(0, 5) + "-" + limpo.substring(5);
    }

    /**
     * Valida se o CEP tem exatamente 8 digitos numericos.
     */
    public static boolean isValido(String cep) {
        String limpo = limpar(cep);
        return limpo != null && limpo.matches("\\d{8}");
    }
}
