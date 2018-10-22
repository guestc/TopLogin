package cn.guestc.nukkit.login.tasks;

import cn.guestc.nukkit.login.TopLogin;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

public class MyTask extends cn.nukkit.scheduler.PluginTask<TopLogin> {
    public MyTask(TopLogin topLogin){
        super(topLogin);
    }
    @Override
    public void onRun(int i) {
        TopLogin plugin = getOwner();
        for(Player p : plugin.getServer().getOnlinePlayers().values()){
            if(!plugin.api.isLogin(p.getName())){
                if(plugin.dataHelper.IsRegister(p.getName())){
                    plugin.api.Message(p,plugin.api.getMessage("login-in-message"));
                }
            }
        }
    }
}
