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
import nl.devpieter.utils.minestom.ParticleUtils;
import nl.devpieter.utils.minestom.SoundUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class BeeEntity extends EntityCreature {

    private final Point hiveLocation;

    private final HashMap<UUID, Integer> collectCounts = new HashMap<>();
    private long lastCollectTime = 0;

    private boolean isFrozen = false;
    private int nectarLevel = 2;

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
                        new BeeReturnToHiveGoal(this, _ -> {
                            onEnterHive.accept((Acquirable<? extends BeeEntity>) this.acquirable());
                        }),
                        new BeePollinateGoal(this, _ -> {
                            nectarLevel++;
                            updateNectarMeta();
                        }),
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

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    public boolean hasNectar() {
        return nectarLevel >= 3;
    }

    public void resetNectar() {
        collectCounts.clear();

        nectarLevel = 0;
        updateNectarMeta();
    }

    public Point getHiveLocation() {
        return hiveLocation;
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
                        SoundEvent.UI_HUD_BUBBLE_POP,
                        Sound.Source.NEUTRAL,
                        2.0f,
                        1.0f
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

        Random random = new Random();
        Vec velocity = new Vec(
                (random.nextDouble() - 0.5) * 0.1,
                -0.05,
                (random.nextDouble() - 0.5) * 0.1
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
                        SoundEvent.ITEM_HONEYCOMB_WAX_ON,
                        Sound.Source.NEUTRAL,
                        1.0f,
                        1.0f
                )
        );
    }

    private void updateNectarMeta() {
        BeeMeta meta = (BeeMeta) getEntityMeta();
        meta.setHasNectar(hasNectar());
    }
}
