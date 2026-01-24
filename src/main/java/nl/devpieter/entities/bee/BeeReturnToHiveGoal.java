package nl.devpieter.entities.bee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ai.GoalSelector;

public class BeeReturnToHiveGoal extends GoalSelector {

    private static final double ARRIVAL_THRESHOLD = 2.0;

    private final BeeEntity bee;
    private boolean returning = false;

    public BeeReturnToHiveGoal(BeeEntity bee) {
        super(bee);
        this.bee = bee;
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