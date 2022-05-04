class Pair<X, Y>(var first: X, var second: Y)

operator fun <G> Pair<G, G>.get(index: Int) = if (index == 0)
    first
else second

operator fun <S> Pair<S, S>.set(index: Int, value: S) = if (index == 0)
    first = value
else
    second = value