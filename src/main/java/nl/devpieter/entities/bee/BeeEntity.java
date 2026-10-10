package nl.devpieter.entities.bee;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.followers.FlyingNodeFollower;
import net.minestom.server.entity.pathfinding.generators.FlyingNodeGenerator;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.thread.Acquirable;
import nl.devpieter.entities.PhysicsItem;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.utils.common.RandomUtils;
import space.pietr.lobby.common.utils.minestom.ParticleUtils;
import space.pietr.lobby.common.utils.minestom.SoundUtils;
import space.pietr.lobby.common.utils.minestom.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BeeEntity extends EntityCreature {

    private final Point hiveLocation;

    private final HashMap<UUID, Integer> collectCounts = new HashMap<>();
    private long lastCollectTime = 0;

    private boolean isFrozen = false;

    private final int maxNectarLevel = 8;
    private int nectarLevel = 0;

    public BeeEntity(Point hiveLocation, Consumer<Acquirable<@NotNull ? extends BeeEntity>> onEnterHive) {
        super(EntityType.BEE);
        this.hiveLocation = hiveLocation;

        setNoGravity(true);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        Navigator navigator = getNavigator();
        navigator.setNodeGenerator(FlyingNodeGenerator::new);
        navigator.setNodeFollower(() -> new FlyingNodeFollower(this));

        addAIGroup(
                List.of(
                        new BeeReturnNectarToHiveGoal(this, _ -> onEnterHive.accept((Acquirable<? extends BeeEntity>) this.acquirable())),
                        new BeePollinateGoal(this, _ -> incrementNectarLevel()),
                        new RandomStrollGoal(this, 15)
                ),
                List.of()
        );
    }

    @Override
    public void update(long time) {
        if (isFrozen) return;
        super.update(time);
    }

    public Point getHiveLocation() {
        return hiveLocation;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    public boolean hasNectar() {
        return nectarLevel >= maxNectarLevel;
    }

    public void resetNectar() {
        collectCounts.clear();

        nectarLevel = 0;
        updateNectarMeta();
    }

    private void incrementNectarLevel() {
        if (nectarLevel >= maxNectarLevel) return;

        int increment = RandomUtils.randomIntInclusive(1, 3);
        nectarLevel = Math.min(nectarLevel + increment, maxNectarLevel);

        updateNectarMeta();
    }

    public void collectNectar(Player player) {
        if (!hasNectar()) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCollectTime < 500) return;
        if (currentTime - lastCollectTime > 5_000) collectCounts.clear();

        lastCollectTime = currentTime;

        int count = collectCounts.getOrDefault(player.getUuid(), 0) + 1;
        collectCounts.put(player.getUuid(), count);

        if (count >= 4) {
            resetNectar();
            handleCollectComplete(player);
        } else handleCollectFeedback();
    }

    private void handleCollectFeedback() {
        ParticleUtils.spawnParticleToViewers(
                this,
                Particle.FALLING_NECTAR,
                getPosition(),
                new Vec(0.2, 0.2, 0.2),
                0.05f,
                4
        );

        SoundUtils.playSoundInWorld(
                getInstance(),
                getPosition(),
                Sound.sound(
                        SoundEvent.ITEM_HONEYCOMB_WAX_ON,
                        Sound.Source.NEUTRAL,
                        1.0f,
                        RandomUtils.randomFloatInclusive(1.1f, 1.6f, 0.01f)
                )
        );
    }

    private void handleCollectComplete(Player player) {
        ItemStack nectar = ItemStack.of(Material.HONEYCOMB).with(builder -> {
            builder.customName(TextUtils.miniText("<gold>Raw Nectar"));
            builder.lore(TextUtils.miniText("<gray>A bottle of freshly collected nectar."));
        });

        PhysicsItem itemEntity = new PhysicsItem(nectar);
        itemEntity.setInstance(player.getInstance(), getPosition());

        Vec velocity = new Vec(
                (RandomUtils.randomDoubleInclusive(0.0, 1.0, 0.01) - 0.5) * 0.1,
                -0.05,
                (RandomUtils.randomDoubleInclusive(0.0, 1.0, 0.01) - 0.5) * 0.1
        );

        itemEntity.setVelocity(velocity);

        ParticleUtils.spawnParticleToViewers(
                this,
                Particle.FALLING_HONEY,
                getPosition(),
                new Vec(0.2, 0.2, 0.2),
                0.05f,
                4
        );

        SoundUtils.playSoundInWorld(
                getInstance(),
                getPosition(),
                Sound.sound(
                        SoundEvent.ENTITY_BEE_STING,
                        Sound.Source.NEUTRAL,
                        0.8f,
                        RandomUtils.randomFloatInclusive(1.2f, 1.4f, 0.01f)
                )
        );
    }

    private void updateNectarMeta() {
        BeeMeta meta = (BeeMeta) getEntityMeta();
        meta.setHasNectar(hasNectar());
    }
}
