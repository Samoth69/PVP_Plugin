package samoth69.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStatusChangedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Main.GameStatus gameStatus;

    public GameStatusChangedEvent(Main.GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Main.GameStatus getGameStatus() {
        return this.gameStatus;
    }
}
