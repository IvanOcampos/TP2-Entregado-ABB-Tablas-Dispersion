# TP2 — ABB aumentado e índice doble

> Completar antes de entregar: grupo, integrantes, CIC y Declaración de Honor requerida por la cátedra.

## Diseño

`ABBAumentado` conserva `n.tamano = 1 + tamano(n.izq) + tamano(n.der)`. Eliminar un nodo con dos hijos mueve físicamente el nodo sucesor a la posición eliminada; nunca copia su clave o valor. `IndiceDoble` posee el árbol y una `TablaEncadenada` cuyas entradas apuntan a esos mismos nodos. Por eso el acceso exacto por índice no navega el árbol.

## Análisis formal

Para `kEsimo`, cada llamada desciende a un único hijo: `T(h)=T(h-1)+Theta(1)`, con base constante, luego `T(h)=Theta(h)`.

En un ABB perfectamente balanceado, `T(n)=T(n/2)+Theta(1)`. Por Maestro: `a=1`, `b=2`, `f(n)=Theta(1)` y `n^(log_b a)=1`; caso 2, por lo que `T(n)=Theta(log n)`.

En un ABB degenerado, `T(n)=T(n-1)+Theta(1)`. Al desenrollar, `T(n)=T(1)+(n-1)Theta(1)=Theta(n)`. Maestro no aplica porque el subproblema es `n-1`, no `n/b`.

`consultarRango` hace dos descensos y cuesta `Theta(h)`; ocupa `Theta(1)` adicional al ser iterativo. `consultarRangoIngenuo` visita todos los nodos: `Theta(n)`. El árbol usa `Theta(n)` espacio; el entero extra de tamaño no cambia ese orden.

La tabla encadenada tiene búsqueda esperada `Theta(1+alpha)`, peor caso `Theta(n)` y espacio `Theta(n+m)`. Insertar en el índice cuesta, en promedio, `Theta(h)+Theta(1+alpha)`. Un rehash aislado cuesta `Theta(n)`, pero al duplicar la capacidad el total de rehashes es menor que `2n`, amortizado `O(1)` por inserción para la parte hash.

## Resultados a completar al ejecutar

### Tabla 1 — visitas ABB

| N | h aleatorio | h ordenado | k aleatorio | k ordenado | rango aumentado | rango ingenuo |
|---:|---:|---:|---:|---:|---:|---:|
| 2000 | 27 | 1999 | 14 | 1000 | 34 | 2000 |
| 4000 | 26 | 3999 | 18 | 2000 | 42 | 4000 |
| 6000 | 27 | 5999 | 21 | 3000 | 31 | 6000 |
| 8000 | 29 | 7999 | 11 | 4000 | 42 | 8000 |
| 10000 | 27 | 9999 | 16 | 5000 | 36 | 10000 |

### Tabla 2 — índice doble

| N | visitas ABB get | sondas hash get | alfa | m |
|---:|---:|---:|---:|---:|
| 2000 | 28165 | 2000 | 0.7102 | 2816 |
| 4000 | 62114 | 4000 | 0.7102 | 5632 |
| 6000 | 92072 | 6000 | 0.5327 | 11264 |
| 8000 | 127687 | 8000 | 0.7102 | 11264 |
| 10000 | 155535 | 10000 | 0.8878 | 11264 |

### Tabla 3 — tabla fija m=97

| N | alfa | sondas/N |
|---:|---:|---:|
| 2000 | 20.6186 | 10.8150 |
| 4000 | 41.2371 | 21.1208 |
| 6000 | 61.8557 | 31.4288 |
| 8000 | 82.4742 | 41.7386 |
| 10000 | 103.0928 | 52.0468 |

## Conclusión

En la Tabla 1, el árbol ordenado alcanza altura `N-1`, mientras el aleatorio mantiene altura cercana a logarítmica. Por eso `kEsimo` ordenado visita `N/2`; el rango aumentado recorre solo dos caminos y el ingenuo visita `N`. En el árbol pequeño de la traza, dos caminos pueden sumar más visitas que los nueve nodos del ingenuo; no contradice la tendencia asintótica.

En la Tabla 2, con `m >= N` y claves 1..N, el módulo reparte casi perfectamente y hay una sonda por búsqueda, mientras las visitas del ABB son mayores. En la Tabla 3, `alpha=N/97` aumenta linealmente y también las sondas promedio, como predice `Theta(1+alpha)`.
