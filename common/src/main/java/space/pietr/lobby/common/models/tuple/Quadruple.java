package space.pietr.lobby.common.models.tuple;

public record Quadruple<T, U, V, W>(T first, U second, V third, W fourth) {

    public static <T, U, V, W> Quadruple<T, U, V, W> of(T first, U second, V third, W fourth) {
        return new Quadruple<>(first, second, third, fourth);
    }
}
