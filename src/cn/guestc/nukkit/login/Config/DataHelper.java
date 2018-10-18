package cn.guestc.nukkit.login.Config;

import cn.guestc.nukkit.login.TopLogin;
import cn.nukkit.Player;

import java.util.Date;

public abstract class DataHelper {

    public Object obj;
    public String DataDir;
    public String DB_url;
    public String DB_user;
    public String DB_passwd;
    public String DB_prefix;

    public TopLogin plugin;
    abstract public void init();

    abstract public boolean AddUser(String user,String passwd,String mail);

    abstract public boolean VerifyPasswd(String user,String passwd);

    abstract public boolean SetPasswd(String user,String passwd);

    abstract public boolean SetMail(String user,String mail);

    abstract public void LoginOut(Player player);

    abstract public boolean IsRegister(String user);

    abstract public Date getLastTime(String user);

}
