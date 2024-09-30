package me.pixlent;

import me.pixlent.commands.GamemodeCommand;
import me.pixlent.item.ItemContainer;
import me.pixlent.item.ItemTransaction;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.inventory.*;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) {
        // Initialization
        System.setProperty("minestom.chunk-view-distance", "16");
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setGenerator(new TestGenerator(new ExampleTerrainBuilder(0)));
        instanceContainer.setTimeRate(0);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(-13, 93, 48));
        });

        globalEventHandler.addListener(EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            player.setGameMode(GameMode.CREATIVE);
            player.setPermissionLevel(4);
        });

        //globalEventHandler.addListener(InventoryClickEvent.class, System.out::println);
        //globalEventHandler.addListener(InventoryCloseEvent.class, System.out::println);
        //globalEventHandler.addListener(InventoryItemChangeEvent.class, System.out::println);
        //globalEventHandler.addListener(InventoryOpenEvent.class, System.out::println);
        //globalEventHandler.addListener(InventoryPreClickEvent.class, System.out::println);
        //globalEventHandler.addListener(PlayerAnvilInputEvent.class, System.out::println);

        ItemContainer container = new ItemContainer();
        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.text("Container"));

        globalEventHandler.addListener(InventoryPreClickEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            System.out.println(event.getClickType());
            System.out.println(event.getSlot());
            System.out.println(event.getClickedItem());

            if (event.getClickType().equals(ClickType.RIGHT_CLICK)) {
                ItemStack item = event.getClickedItem();
                if (event.getInventory() == null) {
                    playerInventory.setItemStack(event.getSlot(), item.withAmount(item.amount() * 2));
                } else {
                    event.getInventory().setItemStack(event.getSlot(), item.withAmount(item.amount() * 2));
                }

                event.setCancelled(true);
            }

            inventory.clear();

            container.acceptItemTransaction(new ItemTransaction(event.getClickedItem()));

            for (ItemStack itemStack : container.itemStacks) {
                inventory.addItemStack(itemStack);
            }

            if (!(player.getOpenInventory() == inventory)) {
                player.openInventory(inventory);
            }
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());

        MojangAuth.init();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
}
