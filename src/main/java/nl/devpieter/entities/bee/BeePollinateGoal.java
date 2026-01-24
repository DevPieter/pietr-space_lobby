package nl.devpieter.entities.bee;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.instance.block.Block;
import nl.devpieter.utils.minestom.WorldUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BeePollinateGoal extends GoalSelector {

    private static final long MAX_PATHFINDING_TIME = TimeUnit.SECONDS.toNanos(20);
    private static final long FAILURE_DELAY = TimeUnit.SECONDS.toNanos(5);

    private static final long POLLINATION_COOLDOWN = TimeUnit.SECONDS.toNanos(10);
    private static final long POLLINATION_ANIMATION_DURATION = TimeUnit.SECONDS.toNanos(3);

    private static final int SEARCH_RADIUS = 15;
    private static final double ARRIVAL_THRESHOLD = 1;

    private static final double POLLINATION_HEIGHT = 0.3;
    private static final double POLLINATION_SPEED = 4.0;
    private static final double POLLINATION_APPROACH_FACTOR = 3.0;

    private static final List<Block> FLOWER_BLOCKS = List.of(
            Block.DANDELION,
            Block.POPPY,
            Block.BLUE_ORCHID,
            Block.ALLIUM,
            Block.AZURE_BLUET,
            Block.RED_TULIP,
            Block.ORANGE_TULIP,
            Block.WHITE_TULIP,
            Block.PINK_TULIP,
            Block.OXEYE_DAISY,
            Block.CORNFLOWER,
            Block.LILY_OF_THE_VALLEY,
            Block.SUNFLOWER,
//            Block.LILAC,
//            Block.ROSE_BUSH,
            Block.PEONY
    );

    private final Random random = new Random();

    private final BeeEntity bee;
    private final Consumer<Void> onPollinate;

    private long lastFailedSearch = -1;
    private long lastFailedPathfinding = -1;

    private long startTime = -1;
    private Point targetFlower;

    private boolean isPollinating;
    private long pollinationStartTime = -1;
    private long lastPollinationTime = -1;

    private Entity debugEntity = null;

    public BeePollinateGoal(BeeEntity bee, Consumer<Void> onPollinate) {
        super(bee);

        this.bee = bee;
        this.onPollinate = onPollinate;
    }

    @Override
    public boolean shouldStart() {
        if (bee.hasNectar()) {
            return false;
        }

        if (lastFailedSearch != -1 && System.nanoTime() - lastFailedSearch < FAILURE_DELAY) {
            return false;
        }

        if (lastFailedPathfinding != -1 && System.nanoTime() - lastFailedPathfinding < FAILURE_DELAY) {
            return false;
        }

        return lastPollinationTime == -1 || System.nanoTime() - lastPollinationTime >= POLLINATION_COOLDOWN;
    }

    @Override
    public void start() {
        reset();

        List<Point> locations = WorldUtils.findBlocks(
                entityCreature.getInstance(),
                entityCreature.getPosition(),
                SEARCH_RADIUS,
                FLOWER_BLOCKS
        );

        if (locations.isEmpty()) {
            lastFailedSearch = System.nanoTime();
            return;
        }

        targetFlower = locations.get(random.nextInt(locations.size()));
        Point target = targetFlower.add(0.5, 1.5, 0.5);

        entityCreature.getNavigator().setPathTo(target);
        startTime = System.nanoTime();

        if (debugEntity != null) debugEntity.remove();
        debugEntity = WorldUtils.placeMarker(
                entityCreature.getInstance(),
                new Vec(target.x(), target.y(), target.z()),
                Component.text("Flower Target")
        );
    }

    @Override
    public void tick(long time) {
        if (targetFlower == null) return;
        if (bee.hasNectar()) return;

        if (startTime != -1 && System.nanoTime() - startTime > MAX_PATHFINDING_TIME) {
            lastFailedPathfinding = System.nanoTime();
            reset();

            return;
        }


        if (isPollinating) tickPollinating();
        else tickFlyingToFlower();
    }

    private void tickPollinating() {
        long pollinationTime = System.nanoTime() - pollinationStartTime;
        double timeInSeconds = pollinationTime / 1_000_000_000.0;

        double yOffset = Math.sin(timeInSeconds * POLLINATION_SPEED) * POLLINATION_HEIGHT;

        Point target = targetFlower.add(0.5, .7, 0.5);

        Point targetPos = new Pos(
                target.x(),
                target.y() + yOffset,
                target.z()
        );

        Point currentPos = entityCreature.getPosition();
        Vec direction = new Vec(
                targetPos.x() - currentPos.x(),
                targetPos.y() - currentPos.y(),
                targetPos.z() - currentPos.z()
        );

        entityCreature.setVelocity(direction.mul(POLLINATION_APPROACH_FACTOR));
    }

    private void tickFlyingToFlower() {
        Point target = targetFlower.add(0.5, 1.0, 0.5);

        double distance = entityCreature.getPosition().distance(target);
        if (distance > ARRIVAL_THRESHOLD) return;

        isPollinating = true;
        pollinationStartTime = System.nanoTime();

        entityCreature.getNavigator().reset();
    }

    @Override
    public boolean shouldEnd() {
        if (bee.hasNectar()) return true;
        if (!isPollinating) return targetFlower == null;

        long hoverTime = System.nanoTime() - pollinationStartTime;
        return hoverTime >= POLLINATION_ANIMATION_DURATION;
    }

    @Override
    public void end() {
        entityCreature.getNavigator().reset();

        if (debugEntity != null) {
            debugEntity.remove();
            debugEntity = null;
        }

        if (isPollinating) {
            lastPollinationTime = System.nanoTime();
            onPollinate.accept(null);
        }

        reset();
    }

    private void reset() {
        targetFlower = null;

        isPollinating = false;
        pollinationStartTime = -1;

        entityCreature.getNavigator().reset();
    }
}
