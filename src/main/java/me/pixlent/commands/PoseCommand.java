package me.pixlent.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public class PoseCommand extends Command {
    public PoseCommand() {
        super("pose");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Missing arguments! Usage: /pose <pose>"));

        ArgumentEnum<Entity.Pose> poseArgument = ArgumentType.Enum("pose", Entity.Pose.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        poseArgument.setCallback((sender, exception) -> {
            final String input = exception.getInput();
            Component message = Component.text("The pose ")
                    .color(TextColor.fromHexString("#e2ed4a"))
                    .append(Component.text(input))
                    .color(TextColor.fromHexString("#f53333"))
                    .append(Component.text(" is not a pose!")
                            .color(TextColor.fromHexString("#e2ed4a")));
            sender.sendMessage(message);
        });

        addSyntax((sender, context) -> {
            final Entity.Pose pose = context.get("pose");

            if (!(sender instanceof Player player)) return;

            player.setPose(pose);
            Component message = Component.text("Set pose to ")
                    .color(TextColor.fromHexString("#e2ed4a"))
                    .append(Component
                            .text(pose.name().toLowerCase())
                            .color(TextColor.fromHexString("#f53333")));
            player.sendMessage(message);
        }, poseArgument);
    }
}
