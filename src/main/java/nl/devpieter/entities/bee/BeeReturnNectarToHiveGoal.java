package nl.devpieter.entities.bee;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ai.GoalSelector;

import java.util.function.Consumer;

public class BeeReturnNectarToHiveGoal extends GoalSelector {

    private static final double ARRIVAL_THRESHOLD = 1;

    private final BeeEntity bee;
    private boolean returning = false;

    private final Consumer<Void> onEnterHive;

    public BeeReturnNectarToHiveGoal(BeeEntity bee, Consumer<Void> onEnterHive) {
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

        BoundingBox bb = entityCreature.getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2;

        entityCreature.getNavigator().setPathTo(location, centerToCorner, 80, 50, null);
    }

    @Override
    public void tick(long time) {
        Point location = bee.getHiveLocation();
        if (location == null) return;

        double distance = entityCreature.getPosition().distance(location);
        if (distance > ARRIVAL_THRESHOLD) return;

        onEnterHive.accept(null);
        returning = false;
    }

    @Override
    public boolean shouldEnd() {
        return !returning || !bee.hasNectar();
    }

    @Override
    public void end() {
        returning = false;
        entityCreature.getNavigator().reset();
    }
}