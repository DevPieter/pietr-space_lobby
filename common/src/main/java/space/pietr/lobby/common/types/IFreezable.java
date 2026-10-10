package space.pietr.lobby.common.types;

import java.util.List;

public interface IFreezable {

    default List<IFreezable> getChildren() {
        return List.of();
    }

    default void freeze() {
        for (IFreezable child : getChildren()) {
            if (child.canBeFrozen()) child.freeze();
        }
    }

    default void melt() {
        for (IFreezable child : getChildren()) {
            if (child.canBeMolted()) child.melt();
        }
    }

    boolean isFrozen();

    default boolean canBeFrozen() {
        return !isFrozen();
    }

    default boolean canBeMolted() {
        return isFrozen();
    }
}
