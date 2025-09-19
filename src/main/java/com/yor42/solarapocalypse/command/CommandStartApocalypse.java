package com.yor42.solarapocalypse.command;

import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class CommandStartApocalypse extends CommandBase {

    private static final ArrayList<String> Aliases = new ArrayList<String>() {{
        add("apocalypse");
        add("solapc");
        add("soap");
    }};

    private static final HashMap<String, BiConsumer<MinecraftServer, ICommandSender>> COMMAND = new HashMap<>();

    public CommandStartApocalypse(){
        COMMAND.put("now", ((minecraftServer, iCommandSender) -> {

            World world = minecraftServer.getWorld(0);
            SolarApocalypseData data = SolarApocalypseData.get(world);
            if(data.isApocalypseStarted()){
                if(iCommandSender instanceof EntityPlayerMP player) {
                    player.sendMessage(new TextComponentTranslation("text.solarapocalypse.apocalypse_already_triggered"));
                }
                return;
            }
            SolarApocalypseData.get(world).setApocalypseStage(world,0);

            for(EntityPlayerMP playerMP : minecraftServer.getPlayerList().getPlayers()){
                playerMP.sendMessage(new TextComponentTranslation("text.solarapocalypse.started"));
            }
        }));
        COMMAND.put("current", ((minecraftServer, iCommandSender) -> {

            World world = minecraftServer.getWorld(0);
            int stage = SolarApocalypseData.get(world).getStage();

            if(iCommandSender instanceof EntityPlayerMP){
                iCommandSender.sendMessage(new TextComponentTranslation("text.solarapocalypse.stage", stage));
            }
        }));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return new ArrayList<>(COMMAND.keySet());
    }

    public String getName() {
        return Aliases.get(0);
    }

    @Override
    public List<String> getAliases() {
        return Aliases;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/apocalypse [subcommand]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        BiConsumer<MinecraftServer, ICommandSender> function = COMMAND.get(args[0]);
        if(function != null){
            function.accept(server,sender);
        }
    }
}
