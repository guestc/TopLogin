package cn.guestc.nukkit.login.events;

import cn.guestc.nukkit.login.TopLogin;
import cn.guestc.nukkit.login.TopLoginAPI;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerCreationEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.TextPacket;

public class LoginEvent implements Listener {

    private TopLogin plugin;

    private TopLoginAPI API;

    public LoginEvent(TopLogin toplogin){
        plugin = toplogin;
        API = plugin.api;
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginMove){
            if(!API.isLogin(name)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onTouch(PlayerInteractEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginInteract){
            if(!API.isLogin(name)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onCraft(CraftItemEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginCraft){
            if(!API.isLogin(name)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onPickItem(InventoryPickupItemEvent event){
        InventoryHolder holder = event.getInventory().getHolder();
        if(holder instanceof Player){
            String name = ((Player) holder).getName();
            if(!API.cdata.UnloginPickItem){
                if(!API.isLogin(name)){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
            String name = player.getName();
            if(!API.cdata.UnloginDropItem){
                if(!API.isLogin(name)){
                    event.setCancelled(true);
                }
            }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginPlace){
            if(!API.isLogin(name)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginBreak){
            if(!API.isLogin(name)){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority= EventPriority.HIGH,ignoreCancelled =false)
    public void onDataPacket(DataPacketSendEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.cdata.UnloginMessage){
            if(!API.isLogin(name)){
                DataPacket pk = event.getPacket();
                if(pk instanceof TextPacket){
                    if(pk.offset != 600){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
