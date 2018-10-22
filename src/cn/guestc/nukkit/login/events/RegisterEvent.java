package cn.guestc.nukkit.login.events;

import cn.guestc.nukkit.login.TopLogin;
import cn.guestc.nukkit.login.TopLoginAPI;
import cn.guestc.nukkit.login.utils.UserData;
import cn.nukkit.Player;
import cn.nukkit.api.API;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.Date;
import java.util.HashMap;

public class RegisterEvent implements Listener {
    private TopLogin plugin;

    private TopLoginAPI API;

    private FormWindow Funreg;


    public enum RegisterState{
        confirmName,
        Passwd,
        confirmPasswd,
        Mail
    }

    private HashMap<String,RegisterState> registers = new HashMap<>();

    private HashMap<String, UserData> reging = new HashMap<>();

    public RegisterEvent(TopLogin toplogin){
        plugin = toplogin;
        API = plugin.api;
        Funreg = new FormWindowSimple(API.getMessage("unreg-usage-ui-title"),API.getMessage("unreg-usage-ui-text"));
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!plugin.dataHelper.IsRegister(name)){
            if(API.cdata.EnableFormUI){
                player.showFormWindow(Funreg);
            }
            player.sendMessage(API.getMessage("reg-comfirm-name"));
        }else{
            API.AutoLogin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = false)
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        API.LoginOut(player);
        if(registers.containsKey(name)){
            registers.remove(name);
        }
        if(reging.containsKey(name)){
            reging.remove(name);
        }
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onCommandPre(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.isLogin(name)){
            if(API.cdata.LoginType.equals("command")){
                if(!registers.containsKey(name) && plugin.dataHelper.IsRegister(name)){
                    String msg = event.getMessage();
                    if(msg.substring(0,"/login ".length()).equals("/login ")){
                        String passwd = TopLoginAPI.getPasswdFormStr(msg.replace("/login ",""));
                        if(plugin.dataHelper.VerifyPasswd(player.getName(),passwd)){
                            API.Message(player,API.getMessage("login-in-success"));
                            API.LoginIn(player);
                        }else{
                            API.Message(player,API.getMessage("login-in-wrong-passwd"));
                            API.Message(player,API.getMessage("login-in-message"));
                        }
                    }
                }
            }
            event.setCancelled(true);
        }

    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onChat(PlayerChatEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        String msg = event.getMessage();
        if(registers.containsKey(name)){
            switch(registers.get(name)){
                case confirmName:
                    if(msg.toLowerCase().equals(name.toLowerCase())){
                        registers.put(name,RegisterState.Passwd);
                        player.sendMessage(API.getMessage("reg-passwd"));
                        reging.put(name,new UserData());
                        event.setCancelled(true);
                        return;
                    }
                    player.sendMessage(API.getMessage("reg-comfirm-name-wrong"));
                    break;
                case Mail:
                    if(!TopLoginAPI.isMail(msg)){
                        player.sendMessage(API.getMessage("reg-mail-wrong"));
                        player.sendMessage(API.getMessage("reg-mail"));
                        event.setCancelled(true);
                        return;
                    }
                    UserData ud1 = reging.get(name);
                    String passwd = TopLoginAPI.getPasswdFormStr(ud1.passwd);
                    plugin.dataHelper.AddUser(name,passwd,msg);
                    player.sendMessage(API.getMessage("reg-success"));
                    player.sendMessage(String.format(API.getMessage("reg-success-return-msg"),name,ud1.passwd,msg));
                    API.LoginIn(player);
                    reging.remove(name);
                    registers.remove(name);
                    break;
                case Passwd:
                    String remsg = API.CheckPasswd(msg);
                    if(remsg != null){
                        player.sendMessage(remsg);
                        player.sendMessage(API.getMessage("reg-comfirm-name-wrong"));
                        event.setCancelled(true);
                        return;
                    }
                    player.sendMessage(API.getMessage("reg-passwd-comfirm"));
                    registers.put(name,RegisterState.confirmPasswd);
                    UserData ud = reging.get(name);
                    ud.name = name;
                    ud.passwd = msg;
                    reging.put(name,ud);
                    break;
                case confirmPasswd:
                    if(!reging.get(name).passwd.equals(msg)){
                        player.sendMessage(API.getMessage("reg-passwd-comfirm-not"));
                        player.sendMessage(API.getMessage("reg-passwd-comfirm"));
                        event.setCancelled(true);
                        return;
                    }
                    player.sendMessage(API.getMessage("reg-mail"));
                    registers.put(name,RegisterState.Mail);
                    break;
            }
            event.setCancelled(true);
        }else{
            if(!API.isLogin(name)){
                if(API.cdata.LoginType.equals("text")){
                    if(plugin.dataHelper.VerifyPasswd(name,TopLoginAPI.getPasswdFormStr(msg))){
                        API.LoginIn(player);
                        API.Message(player,API.getMessage("login-in-success"));
                    }else{
                        API.Message(player,API.getMessage("login-in-wrong-passwd"));
                        API.Message(player,API.getMessage("login-in-message"));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=false)
    public void PreLogin(PlayerPreLoginEvent event){
        if(!event.isCancelled()){
            String name = event.getPlayer().getName();
            if(!plugin.dataHelper.IsRegister(name)){
                String msg = null;
                if(!plugin.dataHelper.canRegister()){
                    msg = API.getMessage("cant-reg");
                }
                if(API.isBanReg(name)){
                    msg = API.getMessage("reg-comfirm-name-banned");
                }
                if(name.length() > API.cdata.UserMaxLen){
                    msg = API.getMessage("reg-comfirm-name-max-lenght");
                }
                if(name.length() < API.cdata.UserMinLen){
                    msg = API.getMessage("reg-comfirm-name-min-lenght");
                }
                if(msg != null){
                    event.setKickMessage(msg);
                    event.setCancelled(true);
                    return;
                }
                registers.put(name,RegisterState.confirmName);
            }
        }
    }
}
