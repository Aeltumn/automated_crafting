package nl.dgoossens.autocraft;

import org.bukkit.event.Listener;

@Deprecated
public class ReloadCommandListener implements Listener {
    private final AutomatedCrafting instance;
    public ReloadCommandListener(final AutomatedCrafting inst) {
        instance = inst;
    }

    /*
            At the moment this is unnecessary because /reload also
            reloads the plugins which causes the recipes to get reloaded anyways.
     */

    /*@EventHandler(ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent e) { parseCommand(e.getCommand(), null); }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) { parseCommand(e.getMessage(), e.getPlayer()); }

    private void parseCommand(String command, final CommandSender listener) {
        if(command==null) return;
        if(command.startsWith("/")) command = command.substring(1);
        String[] t = command.split(" ");
        if(t.length>0) command = t[0];
        if(command.contains(":")) {
            String[] s = command.split(":", 2);
            command = s[1];
        }
        if(command.toLowerCase().equals("reload")) {
            new BukkitRunnable() {
                public void run() {
                    instance.getRecipeLoader().reload(listener);
                }
            }.runTaskLaterAsynchronously(instance, 30*20); //Run in 30 seconds to make sure reloading is probably done.
        }
    }*/
}
