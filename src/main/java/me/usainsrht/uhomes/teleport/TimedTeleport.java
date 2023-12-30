package me.usainsrht.uhomes.teleport;

import me.usainsrht.uhomes.UHomes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public class TimedTeleport {

    private Entity entity;
    private Location startLocation;
    private Location targetLocation;
    private int taskID;
    private int ticksTotal;
    private int ticksPassed;
    private int ticksToRunOnTick;
    private Consumer<TimedTeleport> onTick;
    private Consumer<TimedTeleport> onCancel;
    private Consumer<TimedTeleport> onFinish;

    public TimedTeleport() {};

    public void start(boolean loadChunk) {
        startLocation = entity.getLocation().clone();
        taskID = Bukkit.getScheduler().runTaskTimer(UHomes.getInstance(), () -> {
            ticksPassed++;
            if (ticksPassed % ticksToRunOnTick == 0) onTick.accept(this);
            if (ticksPassed >= ticksTotal) onFinish.accept(this);
        }, 1L, 1L).getTaskId();
        if (loadChunk) targetLocation.getWorld().getChunkAtAsyncUrgently(targetLocation);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskID);
        onCancel.accept(this);
    }

    //builder methods

    public TimedTeleport location(Location location) {
        this.targetLocation = location;
        return this;
    }

    public TimedTeleport entity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public TimedTeleport ticks(int ticks) {
        ticksTotal = ticks;
        return this;
    }

    public TimedTeleport ticksToRunOnTick(int ticksToRunOnTick) {
        this.ticksToRunOnTick = ticksToRunOnTick;
        return this;
    }

    public TimedTeleport onTick(Consumer<TimedTeleport> consumer) {
        onTick = consumer;
        return this;
    }

    public TimedTeleport onCancel(Consumer<TimedTeleport> consumer) {
        onCancel = consumer;
        return this;
    }

    public TimedTeleport onFinish(Consumer<TimedTeleport> consumer) {
        onFinish = consumer;
        return this;
    }


    //getter methods


    public int getTaskID() {
        return taskID;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public Entity getEntity() {
        return entity;
    }

    public Consumer<TimedTeleport> getOnCancel() {
        return onCancel;
    }

    public Consumer<TimedTeleport> getOnFinish() {
        return onFinish;
    }

    public Consumer<TimedTeleport> getOnTick() {
        return onTick;
    }

    public int getTicksPassed() {
        return ticksPassed;
    }

    public int getTicksToRunOnTick() {
        return ticksToRunOnTick;
    }

    public int getTicksTotal() {
        return ticksTotal;
    }
}
