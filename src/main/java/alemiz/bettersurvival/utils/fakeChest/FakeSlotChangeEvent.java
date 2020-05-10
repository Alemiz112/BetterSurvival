package alemiz.bettersurvival.utils.fakeChest;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;

public class FakeSlotChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final FakeInventory inventory;
    private final SlotChangeAction action;
    private boolean cancelled = false;

    FakeSlotChangeEvent(Player player, FakeInventory inventory, SlotChangeAction action) {
        this.player = player;
        this.inventory = inventory;
        this.action = action;
    }

    public Player getPlayer() {
        return player;
    }

    public FakeInventory getInventory() {
        return inventory;
    }

    public SlotChangeAction getAction() {
        return action;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled() {
        this.cancelled = true;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public void call(){
        BetterSurvival.getInstance().getServer().getPluginManager().callEvent(this);
    }
}
