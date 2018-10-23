package cn.guestc.nukkit.login.tasks;

import cn.guestc.nukkit.login.TopLogin;
import cn.nukkit.Player;

public class MinuteTask extends cn.nukkit.scheduler.PluginTask<TopLogin> {
    public MinuteTask(TopLogin topLogin){
        super(topLogin);
    }
    @Override
    public void onRun(int i) {
        TopLogin plugin = getOwner();
        plugin.api.ReduceMinute();
    }
}