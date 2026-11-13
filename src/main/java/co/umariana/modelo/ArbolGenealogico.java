package co.umariana.modelo;

import java.util.ArrayList;
import java.util.List;

public class ArbolGenealogico {

    private static class Nodo {
        Persona persona;
        Nodo padre; // Ascendiente 2n
        Nodo madre; // Ascendiente 2n + 1
        List<Nodo> hijos = new ArrayList<>(); // Descendientes d'Aboville

        Nodo(Persona persona) {
            this.persona = persona;
        }
    }

    private final Nodo raiz;
    private int tamano;

    public ArbolGenealogico(Persona sujeto) {
        if (sujeto == null) {
            throw new IllegalArgumentException("El sujeto no puede ser null");
        }
        sujeto.validar();
        if (!Posicion.esSujeto(sujeto.getPosicion())) {
            throw new ArbolInvalidoException("La posición del sujeto debe ser '1'");
        }
        this.raiz = new Nodo(sujeto);
        this.tamano = 1;
    }

    public Persona sujeto() {
        return raiz.persona;
    }

    public int tamano() {
        return tamano;
    }

    private Nodo buscarNodo(String posicion) {
        if (!Posicion.esValida(posicion)) {
            return null;
        }
        if (Posicion.esSujeto(posicion)) {
            return raiz;
        }

        if (Posicion.esAhnentafel(posicion)) {
            int n = Integer.parseInt(posicion);
            String bits = Integer.toBinaryString(n);
            Nodo actual = raiz;

            // Recorrer siguiendo los bits de n (0 = padre, 1 = madre)
            for (int i = 1; i < bits.length(); i++) {
                if (actual == null) return null;
                if (bits.charAt(i) == '0') {
                    actual = actual.padre;
                } else {
                    actual = actual.madre;
                }
            }
            return actual;
        } else if (Posicion.esDAboville(posicion)) {
            String[] partes = posicion.split("\\.");
            Nodo actual = raiz;

            // Recorrer bajando según el número de hijo de cada nivel
            for (int i = 1; i < partes.length; i++) {
                if (actual == null) return null;
                int numHijo = Integer.parseInt(partes[i]);
                
                Nodo hijoEncontrado = null;
                for (Nodo h : actual.hijos) {
                    if (Posicion.orden(h.persona.getPosicion()) == numHijo) {
                        hijoEncontrado = h;
                        break;
                    }
                }
                actual = hijoEncontrado;
            }
            return actual;
        }

        return null;
    }

    public Persona buscar(String posicion) {
        Nodo n = buscarNodo(posicion);
        return n != null ? n.persona : null;
    }

    public boolean contiene(String posicion) {
        return buscar(posicion) != null;
    }

    public Persona agregar(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser null");
        }
        persona.validar();

        String pos = persona.getPosicion();
        if (contiene(pos)) {
            throw new ArbolInvalidoException("La posición ya existe en el árbol: " + pos);
        }

        if (Posicion.esAscendiente(pos)) {
            String posHijo = Posicion.posicionDelHijoEnLinea(pos);
            Nodo nodoHijo = buscarNodo(posHijo);
            if (nodoHijo == null) {
                throw new ArbolInvalidoException("Falta el hijo en línea del cual depende: " + posHijo);
            }
            if (persona.getNacimiento() >= nodoHijo.persona.getNacimiento()) {
                throw new ArbolInvalidoException("El ascendiente debe nacer antes que el hijo");
            }

            Nodo nuevo = new Nodo(persona);
            int n = Integer.parseInt(pos);
            if (n % 2 == 0) {
                nodoHijo.padre = nuevo;
            } else {
                nodoHijo.madre = nuevo;
            }
            tamano++;
            return persona;

        } else if (Posicion.esDescendiente(pos)) {
            String posProgenitor = Posicion.posicionDelProgenitor(pos);
            Nodo nodoProg = buscarNodo(posProgenitor);
            if (nodoProg == null) {
                throw new ArbolInvalidoException("Falta el progenitor del cual depende: " + posProgenitor);
            }
            if (persona.getNacimiento() <= nodoProg.persona.getNacimiento()) {
                throw new ArbolInvalidoException("El descendiente debe nacer después que su progenitor");
            }

            Nodo nuevo = new Nodo(persona);
            nodoProg.hijos.add(nuevo);
            tamano++;
            return persona;

        } else {
            throw new ArbolInvalidoException("No se puede agregar una segunda persona en la posición '1'");
        }
    }

    public Persona agregarPadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo n = buscarNodo(posicion);
        if (n == null) {
            throw new ArbolInvalidoException("La posición origen no existe: " + posicion);
        }
        if (!Posicion.esAhnentafel(posicion)) {
            throw new ArbolInvalidoException("Solo se pueden agregar padres a posiciones Ahnentafel");
        }
        String posPadre = Posicion.posicionDelPadre(posicion);
        if (!Posicion.esAhnentafel(posPadre)) {
            throw new ArbolInvalidoException("La posición del padre excede el límite permitido");
        }
        Persona persona = new Persona(posPadre, nombre, nacimiento, fallecimiento);
        return agregar(persona);
    }

    public Persona agregarMadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo n = buscarNodo(posicion);
        if (n == null) {
            throw new ArbolInvalidoException("La posición origen no existe: " + posicion);
        }
        if (!Posicion.esAhnentafel(posicion)) {
            throw new ArbolInvalidoException("Solo se pueden agregar madres a posiciones Ahnentafel");
        }
        String posMadre = Posicion.posicionDeLaMadre(posicion);
        if (!Posicion.esAhnentafel(posMadre)) {
            throw new ArbolInvalidoException("La posición de la madre excede el límite permitido");
        }
        Persona persona = new Persona(posMadre, nombre, nacimiento, fallecimiento);
        return agregar(persona);
    }

    public Persona agregarHijo(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo n = buscarNodo(posicion);
        if (n == null) {
            throw new ArbolInvalidoException("La posición origen no existe: " + posicion);
        }
        if (Posicion.esAscendiente(posicion)) {
            throw new ArbolInvalidoException("No se pueden agregar hijos a un ascendiente");
        }

        int maxOrden = 0;
        for (Nodo h : n.hijos) {
            int ordenHijo = Posicion.orden(h.persona.getPosicion());
            if (ordenHijo > maxOrden) {
                maxOrden = ordenHijo;
            }
        }
        String nuevaPos = posicion + "." + (maxOrden + 1);
        Persona persona = new Persona(nuevaPos, nombre, nacimiento, fallecimiento);
        return agregar(persona);
    }

    public int eliminar(String posicion) {
        if (Posicion.esSujeto(posicion)) {
            throw new ArbolInvalidoException("El sujeto no se puede eliminar");
        }
        Nodo n = buscarNodo(posicion);
        if (n == null) {
            throw new ArbolInvalidoException("La posición no existe: " + posicion);
        }

        int eliminados = contarSubarbol(n);

        if (Posicion.esAscendiente(posicion)) {
            String posHijo = Posicion.posicionDelHijoEnLinea(posicion);
            Nodo hijo = buscarNodo(posHijo);
            if (hijo.padre == n) {
                hijo.padre = null;
            }
            if (hijo.madre == n) {
                hijo.madre = null;
            }
        } else if (Posicion.esDescendiente(posicion)) {
            String posProg = Posicion.posicionDelProgenitor(posicion);
            Nodo prog = buscarNodo(posProg);
            prog.hijos.remove(n);
        }

        tamano -= eliminados;
        return eliminados;
    }

    private int contarSubarbol(Nodo nodo) {
        if (nodo == null) {
            return 0;
        }
        int total = 1;
        total += contarSubarbol(nodo.padre);
        total += contarSubarbol(nodo.madre);
        for (Nodo h : nodo.hijos) {
            total += contarSubarbol(h);
        }
        return total;
    }

    public List<Persona> miembros() {
        List<Persona> resultado = new ArrayList<>();
        resultado.add(raiz.persona);
        resultado.addAll(ascendientes());
        resultado.addAll(descendientes());
        return resultado;
    }

    public List<Persona> ascendientes() {
        List<Persona> lista = new ArrayList<>();
        recolectarAscendientes(raiz, lista);
        lista.sort((p1, p2) -> Integer.compare(Integer.parseInt(p1.getPosicion()), Integer.parseInt(p2.getPosicion())));
        return lista;
    }

    private void recolectarAscendientes(Nodo actual, List<Persona> lista) {
        if (actual == null) {
            return;
        }
        if (Posicion.esAscendiente(actual.persona.getPosicion())) {
            lista.add(actual.persona);
        }
        recolectarAscendientes(actual.padre, lista);
        recolectarAscendientes(actual.madre, lista);
    }

    public List<Persona> descendientes() {
        List<Persona> lista = new ArrayList<>();
        recolectarDescendientes(raiz, lista);
        return lista;
    }

    private void recolectarDescendientes(Nodo actual, List<Persona> lista) {
        if (actual == null) {
            return;
        }
        if (Posicion.esDescendiente(actual.persona.getPosicion())) {
            lista.add(actual.persona);
        }
        List<Nodo> hijosOrdenados = new ArrayList<>(actual.hijos);
        hijosOrdenados.sort((h1, h2) -> Integer.compare(Posicion.orden(h1.persona.getPosicion()), Posicion.orden(h2.persona.getPosicion())));
        for (Nodo h : hijosOrdenados) {
            recolectarDescendientes(h, lista);
        }
    }
}