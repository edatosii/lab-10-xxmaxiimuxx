package co.umariana.modelo;

/**
 * Las posiciones de un árbol genealógico. Cada miembro tiene una posición (texto) que dice quién es
 * respecto al SUJETO (la persona de la que trata el árbol). Se usan dos sistemas:
 *
 * AHNENTAFEL (para los ASCENDIENTES: padres, abuelos...). Es un número entero:
 *
 *     1                  el sujeto
 *     2 = padre del 1    3 = madre del 1
 *     4 = padre del 2    5 = madre del 2    6 = padre del 3    7 = madre del 3
 *
 *   Regla: el padre de la persona n es 2n y la madre es 2n + 1. Por eso los números pares son
 *   padres y los impares (mayores que 1) son madres. Y la persona n es hijo/a de n / 2 (división entera).
 *
 * D'ABOVILLE (para los DESCENDIENTES: hijos, nietos...). Es el "1" del sujeto seguido de un punto y
 * el número de orden en cada generación:
 *
 *     1.1  primer hijo del sujeto      1.2  segundo hijo del sujeto
 *     1.1.1  primer hijo del 1.1       1.2.3  tercer hijo del 1.2
 *
 *   Regla: el progenitor de 1.2.3 es 1.2 (se quita el último número).
 *
 * La posición "1" (el sujeto) cuenta como Ahnentafel.
 *
 * LÍMITES (para que las posiciones quepan en la base de datos y en un int):
 *   - Ahnentafel: de 1 a 2047 (hasta 10 generaciones de ascendientes).
 *   - D'Aboville: cada número de orden va de 1 a 9999 y la posición completa tiene máximo 50 caracteres.
 */
public final class Posicion {

    public static final String SUJETO = "1";
    public static final int MAX_AHNENTAFEL = 2047;
    public static final int MAX_ORDEN = 9999;
    public static final int MAX_LONGITUD = 50;

    private Posicion() {
    }

    /**
     * @return true solo si la posición es exactamente "1"
     */
    public static boolean esSujeto(String posicion) {
        return SUJETO.equals(posicion);
    }

    /**
     * Ahnentafel: un entero positivo SIN ceros a la izquierda, de 1 a MAX_AHNENTAFEL, sin signo,
     * espacios ni puntos. Incluye el "1" del sujeto.
     */
    public static boolean esAhnentafel(String posicion) {
        if (posicion == null || posicion.isEmpty()) {
            return false;
        }
        if (!posicion.matches("([1-9][0-9]*)")) {
            return false;
        }
        try {
            int valor = Integer.parseInt(posicion);
            return valor >= 1 && valor <= MAX_AHNENTAFEL;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * D'Aboville: empieza por "1" y sigue con UNO o más grupos ".k", donde k es un entero positivo sin ceros
     * a la izquierda, de 1 a MAX_ORDEN. La posición completa no puede pasar de MAX_LONGITUD caracteres.
     */
    public static boolean esDAboville(String posicion) {
        if (posicion == null || posicion.length() > MAX_LONGITUD) {
            return false;
        }

        String regex = "^1(\\.(?:[1-9][0-9]*))+$";
        if (!posicion.matches(regex)) {
            return false;
        }

        String[] partes = posicion.split("\\.");
        for (String parte : partes) {
            try {
                int ordenNum = Integer.parseInt(parte);
                if (ordenNum < 1 || ordenNum > MAX_ORDEN) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    public static boolean esValida(String posicion) {
        return esAhnentafel(posicion) || esDAboville(posicion);
    }

    public static boolean esAscendiente(String posicion) {
        return esAhnentafel(posicion) && !esSujeto(posicion);
    }

    public static boolean esDescendiente(String posicion) {
        return esDAboville(posicion);
    }

    public static String posicionDelPadre(String ahnentafel) {
        if (!esAhnentafel(ahnentafel)) {
            throw new IllegalArgumentException("La posición no es un número Ahnentafel válido: " + ahnentafel);
        }
        int n = Integer.parseInt(ahnentafel);
        return String.valueOf(2 * n);
    }

    public static String posicionDeLaMadre(String ahnentafel) {
        if (!esAhnentafel(ahnentafel)) {
            throw new IllegalArgumentException("La posición no es un número Ahnentafel válido: " + ahnentafel);
        }
        int n = Integer.parseInt(ahnentafel);
        return String.valueOf(2 * n + 1);
    }

    public static String posicionDelHijoEnLinea(String ahnentafel) {
        if (!esAscendiente(ahnentafel)) {
            throw new IllegalArgumentException("La posición debe ser un ascendiente válido (distinto de '1'): " + ahnentafel);
        }
        int n = Integer.parseInt(ahnentafel);
        return String.valueOf(n / 2);
    }

    public static String posicionDelProgenitor(String daboville) {
        if (!esDescendiente(daboville)) {
            throw new IllegalArgumentException("La posición no es un d'Aboville válido: " + daboville);
        }
        int ultimoPunto = daboville.lastIndexOf('.');
        return daboville.substring(0, ultimoPunto);
    }

    public static int orden(String daboville) {
        if (!esDescendiente(daboville)) {
            throw new IllegalArgumentException("La posición no es un d'Aboville válido: " + daboville);
        }
        int ultimoPunto = daboville.lastIndexOf('.');
        String ultimoSegmento = daboville.substring(ultimoPunto + 1);
        return Integer.parseInt(ultimoSegmento);
    }

    public static int generacion(String posicion) {
        if (!esValida(posicion)) {
            throw new IllegalArgumentException("La posición no es válida: " + posicion);
        }

        if (esAhnentafel(posicion)) {
            int n = Integer.parseInt(posicion);
            if (n == 1) {
                return 0;
            }
            return (int) (Math.floor(Math.log(n) / Math.log(2)));
        } else {
            int countPuntos = 0;
            for (int i = 0; i < posicion.length(); i++) {
                if (posicion.charAt(i) == '.') {
                    countPuntos++;
                }
            }
            return countPuntos;
        }
    }
}