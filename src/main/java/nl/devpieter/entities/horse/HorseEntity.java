package nl.devpieter.entities.horse;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.models.PenaltyValue;
import nl.devpieter.utils.minestom.TextUtils;

public class HorseEntity extends EntityCreature {

    private final SchedulerManager scheduler = MinecraftServer.getSchedulerManager();

    private Task stageOneCrashTask;
    private Task stageTwoCrashTask;
    private Task boostTask;

    private final AttributeInstance movementSpeedAttr = getAttribute(Attribute.MOVEMENT_SPEED);
    private final AttributeInstance jumpStrengthAttr = getAttribute(Attribute.JUMP_STRENGTH);

    private final PenaltyValue movementSpeed = new PenaltyValue(0.4);
    private final PenaltyValue jumpStrength = new PenaltyValue(0.8);

    private final BossBar boostBarBoosts;

    private final int maxBoosts = 6;
    private final double boostCost = 10;
    private double currentBoostPower = 20;

    public HorseEntity() {
        super(EntityType.HORSE);

        HorseMeta meta = (HorseMeta) getEntityMeta();
        meta.setVariantAndMarking(HorseMeta.Variant.BLACK, HorseMeta.Marking.WHITE);

        meta.setTamed(true);

        setEquipment(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE));
        movementSpeedAttr.setBaseValue(movementSpeed.getValue());
        jumpStrengthAttr.setBaseValue(jumpStrength.getValue());

        boostBarBoosts = BossBar.bossBar(
                TextUtils.miniText("Boost"),
                (float) 0,
                BossBar.Color.GREEN,
                BossBar.Overlay.NOTCHED_6
        );

        updateBoostBar();
        scheduler.buildTask(() -> grandBoostPower(1)).repeat(TaskSchedule.seconds(4)).schedule();
    }

    private void updateBoostBar() {
        float progress = (float) (currentBoostPower / (maxBoosts * boostCost));
        boostBarBoosts.progress(progress);
    }

    public void grandBoostPower(int amount) {
        currentBoostPower += amount;
        if (currentBoostPower > maxBoosts * boostCost) currentBoostPower = maxBoosts * boostCost;

        updateBoostBar();
    }

    public void crash() {
        HorseMeta meta = (HorseMeta) getEntityMeta();
        meta.setRearing(true);

        movementSpeed.addPenalty("crash", PenaltyValue.Type.SUBTRACTIVE_PERCENTAGE, 70);
        jumpStrength.addPenalty("crash", PenaltyValue.Type.SUBTRACTIVE_PERCENTAGE, 100);

        if (stageOneCrashTask != null) stageOneCrashTask.cancel();
        if (stageTwoCrashTask != null) stageTwoCrashTask.cancel();

        stageOneCrashTask = scheduler.buildTask(() -> {
            meta.setRearing(false);
            jumpStrength.addPenalty("crash", PenaltyValue.Type.SUBTRACTIVE_PERCENTAGE, 50);
        }).delay(TaskSchedule.seconds(1)).schedule();

        stageTwoCrashTask = scheduler.buildTask(() -> {
            movementSpeed.removePenalty("crash");
            jumpStrength.removePenalty("crash");
        }).delay(TaskSchedule.seconds(3)).schedule();
    }

    public void boost(Player player) {
        if (currentBoostPower < boostCost) return;

        currentBoostPower -= boostCost;
        updateBoostBar();

        Potion potion = new Potion(PotionEffect.SPEED, 6, Potion.INFINITE_DURATION, Potion.ICON_FLAG | Potion.AMBIENT_FLAG);
        player.sendPackets(new EntityEffectPacket(player.getEntityId(), potion));

        movementSpeed.addPenalty("boost", PenaltyValue.Type.ADDITIVE_PERCENTAGE, 40);

        if (boostTask != null) boostTask.cancel();

        boostTask = scheduler.buildTask(() -> {
            movementSpeed.removePenalty("boost");
            player.sendPacket(new RemoveEntityEffectPacket(player.getEntityId(), PotionEffect.SPEED));
        }).delay(TaskSchedule.seconds(4)).schedule();
    }

    @Override
    public void addPassenger(Entity entity) {
        super.addPassenger(entity);

        if (!(entity instanceof Player player)) return;
        boostBarBoosts.addViewer(player);
    }

    @Override
    public void removePassenger(Entity entity) {
        super.removePassenger(entity);

        if (!(entity instanceof Player player)) return;
        boostBarBoosts.removeViewer(player);
    }

    @Override
    public void sendPacketToViewers(SendablePacket packet) {
        Player rider = getControllingPlayer();

        if (rider != null && packet instanceof EntityVelocityPacket) {
            for (Player viewer : getViewers()) if (viewer != rider) viewer.sendPacket(packet);
            return;
        }

        super.sendPacketToViewers(packet);
    }

    @Override
    public void movementTick() {
        if (getControllingPlayer() == null) {
            super.movementTick();
            return;
        }

        double speed = movementSpeed.getValue();
        if (movementSpeedAttr.getBaseValue() != speed) movementSpeedAttr.setBaseValue(speed);

        double jump = jumpStrength.getValue();
        if (jumpStrengthAttr.getBaseValue() != jump) jumpStrengthAttr.setBaseValue(jump);

        velocity = Vec.ZERO;
        gravityTickCount = 0;
    }

    private Player getControllingPlayer() {
        var passengers = getPassengers();
        return !passengers.isEmpty() && passengers.getFirst() instanceof Player player ? player : null;
    }
}
