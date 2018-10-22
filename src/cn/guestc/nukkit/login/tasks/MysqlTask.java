package cn.guestc.nukkit.login.tasks;

import cn.guestc.nukkit.login.Config.DataHelper;
import cn.guestc.nukkit.login.Config.MysqlConfig;
import cn.guestc.nukkit.login.TopLogin;
import cn.nukkit.scheduler.PluginTask;

public class MysqlTask extends PluginTask<TopLogin> {
    public MysqlTask(TopLogin topLogin){
        super(topLogin);
    }
    @Override
    public void onRun(int i) {
        if(getOwner().dataHelper.type == DataHelper.Type.mysql){
            MysqlConfig mc = (MysqlConfig) getOwner().dataHelper;
            mc.Check();
        }
    }
}
