package me.totalfreedom.totalfreedommod.command;

import java.util.Date;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.pravian.aero.util.Ips;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.IMPOSTOR, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Able to verify yourself or set your own password", usage = "/verify <set <password> | <password>>")
public class Command_verify extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        if (args.length == 1)
        {
            int incorrect = 0;
            String password = args[0];
            Admin admin = plugin.al.getAdmin(sender);
            if (plugin.al.isAdminImpostor(playerSender))
            {
                if (!plugin.al.getAdmin(sender).hasPassword())
                {
                    msg("You don't have any password defined in your slot", ChatColor.RED);
                    return true;
                }

                if (!password.equalsIgnoreCase(plugin.al.getAdmin(sender).getPassword()))
                {
                    FLog.info("");
                    msg("The password you insert is incorrect, please try again");
                    incorrect++;

                    if (incorrect == 3)
                    {
                        FLog.info(sender.getName() + " has failed to verify using the correct password.");
                        playerSender.kickPlayer(ChatColor.RED + "You have insert the wrong password 3 times!\n"
                                + "if you forget, please PM one of the administator to reset your password.");
                        return true;
                    }
                    return true;
                }

                FLog.info("");
                FUtil.bcastMsg(sender.getName() + " has used our verification system and has verified", ChatColor.AQUA);
                FUtil.adminAction(ConfigEntry.SERVER_NAME.getString(), "Readding " + admin.getName() + " to the admin list", true);

                admin.setName(sender.getName());
                admin.addIp(Ips.getIp(playerSender));
                admin.setActive(true);
                admin.setLastLogin(new Date());

                plugin.al.save();
                plugin.al.updateTables();

                final FPlayer fPlayer = plugin.pl.getPlayer(playerSender);
                if (fPlayer.getFreezeData().isFrozen())
                {
                    fPlayer.getFreezeData().setFrozen(false);
                    msg("You have been unfrozen");
                }
                return true;
            }
        }

        if (args.length > 1)
        {
            if (args[0].equalsIgnoreCase("set"))
            {
                if (args.length != 1)
                {
                    return false;
                }

                String password = args[1];
                if (!plugin.al.isAdmin(playerSender))
                {
                    msg("You may not set your verification password", ChatColor.RED);
                    return true;
                }

                FLog.info("");
                plugin.al.getAdmin(sender).setPassword(password);
                msg("Your new password is " + ChatColor.YELLOW + password, ChatColor.BLUE);
                return true;
            }
            return false;
        }
        return false;
    }
}
