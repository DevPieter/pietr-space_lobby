package nl.devpieter.entities.bee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ai.GoalSelector;

import java.util.function.Consumer;

public class BeeReturnToHiveGoal extends GoalSelector {

    private static final double ARRIVAL_THRESHOLD = 1;

    private final BeeEntity bee;
    private boolean returning = false;

    private final Consumer<Void> onEnterHive;

    public BeeReturnToHiveGoal(BeeEntity bee, Consumer<Void> onEnterHive) {
        super(bee);

        this.bee = bee;
        this.onEnterHive = onEnterHive;
    }

    @Override
    public boolean shouldStart() {
        return bee.hasNectar() && !returning;
    }

    @Override
    public void start() {
        returning = true;

        Point location = bee.getHiveLocation();
        if (location == null) return;

        entityCreature.getNavigator().setPathTo(location);
    }

    @Override
    public void tick(long time) {
        Point location = bee.getHiveLocation();
        if (location == null) return;

        double distance = entityCreature.getPosition().distance(location);
        if (distance > ARRIVAL_THRESHOLD) return;

        bee.resetNectar();
        onEnterHive.accept(null);
    }

    @Override
    public boolean shouldEnd() {
        return !bee.hasNectar();
    }

    @Override
    public void end() {
        returning = false;
        entityCreature.getNavigator().reset();
    }
}