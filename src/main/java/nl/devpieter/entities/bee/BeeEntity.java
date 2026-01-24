package nl.devpieter.entities.bee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.followers.FlyingNodeFollower;
import net.minestom.server.entity.pathfinding.generators.FlyingNodeGenerator;

import java.util.List;

public class BeeEntity extends EntityCreature {

    private final Point hiveLocation;

    private int nectarLevel = 0;

    public BeeEntity(Point hiveLocation) {
        super(EntityType.BEE);
        this.hiveLocation = hiveLocation;

        setNoGravity(true);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        Navigator navigator = getNavigator();
        navigator.setNodeGenerator(FlyingNodeGenerator::new);
        navigator.setNodeFollower(() -> new FlyingNodeFollower(this));

        addAIGroup(
                List.of(
                        new BeeReturnToHiveGoal(this),
                        new BeePollinateGoal(this, _ -> {
                            nectarLevel++;
                            updateNectarMeta();
                        }),
                        new RandomStrollGoal(this, 15)
                ),
                List.of()
        );
    }

    public boolean hasNectar() {
        return nectarLevel >= 3;
    }

    public void resetNectar() {
        nectarLevel = 0;
        updateNectarMeta();
    }

    public Point getHiveLocation() {
        return hiveLocation;
    }

    private void updateNectarMeta() {
        BeeMeta meta = (BeeMeta) getEntityMeta();
        meta.setHasNectar(hasNectar());
    }
}
