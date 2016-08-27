package me.totalfreedom.totalfreedommod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityWiper extends FreedomService
{

    private static final long WIPE_RATE = 5 * 20L;
    //
    private final List<Class<? extends Entity>> wipables = new ArrayList<>();
    //
    private BukkitTask wipeTask;

    public EntityWiper(TotalFreedomMod plugin)
    {
        super(plugin);
        wipables.add(EnderCrystal.class);
        wipables.add(EnderSignal.class);
        wipables.add(ExperienceOrb.class);
        wipables.add(Projectile.class);
        wipables.add(FallingBlock.class);
        wipables.add(Firework.class);
        wipables.add(Item.class);
        wipables.add(ThrownPotion.class);
        wipables.add(ThrownExpBottle.class);
        wipables.add(AreaEffectCloud.class);
        wipables.add(Minecart.class);
        wipables.add(Boat.class);
    }

    @Override
    protected void onStart()
    {
        if (!ConfigEntry.AUTO_ENTITY_WIPE.getBoolean())
        {
            return;
        }

        wipeTask = new BukkitRunnable()
        {

            @Override
            public void run()
            {
                wipeEntities();
            }
        }.runTaskTimer(plugin, WIPE_RATE, WIPE_RATE);

    }

    @Override
    protected void onStop()
    {
        FUtil.cancel(wipeTask);
        wipeTask = null;
    }

    public boolean isWipeable(Entity entity)
    {
        for (Class<? extends Entity> c : wipables)
        {
            if (c.isAssignableFrom(entity.getClass()))
            {
                return true;
            }
        }
        return false;
    }

    public int wipeEntities()
    {
        int removed = 0;
        Iterator<World> worlds = server.getWorlds().iterator();
        while (worlds.hasNext())
        {
            removed += wipeEntities(worlds.next());
        }
        return removed;
    }

    public int wipeEntities(World world)
    {
        int removed = 0;

        boolean wipeExp = ConfigEntry.ALLOW_EXPLOSIONS.getBoolean();
        Iterator<Entity> e = world.getEntities().iterator();

        Map<Chunk, List<Entity>> cem = new HashMap<>();
        while (e.hasNext())
        {
            final Entity en = e.next();

            if (wipeExp && Explosive.class.isAssignableFrom(en.getClass()))
            {
                en.remove();
                removed++;
            }

            if (!isWipeable(en))
            {
                continue;
            }

            Chunk c = en.getLocation().getChunk();
            List<Entity> cel = cem.get(c);
            if (cel == null)
            {
                cem.put(c, new ArrayList<>(Arrays.asList(en)));
            }
            else
            {
                cel.add(en);
            }
        }

        for (Chunk c : cem.keySet())
        {
            List<Entity> cel = cem.get(c);

            if (cel.size() < 30)
            {
                continue;
            }

            for (Entity en : cel)
            {
                en.remove();
            }
        }
        return removed;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        if (!ConfigEntry.AUTO_ENTITY_WIPE.getBoolean())
        {
            return;
        }

        final Item e = event.getEntity();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                e.remove();
            }
        }.runTaskLater(plugin, 20L * 20L);
    }
}
