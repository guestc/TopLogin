package cn.guestc.nukkit.login.Config;

import java.util.Date;

public abstract class DataHelper {

    public String connectStr;

    public Object obj;

    abstract public void init();

    abstract public boolean AddUser(String user,String passwd,String mail);

    abstract public boolean VerifyPasswd(String user,String passwd);

    abstract public boolean SetPasswd(String user,String passwd);

    abstract public boolean SetMail(String user,String mail);

    abstract public void LoginOut(String user);

    abstract public boolean IsRegister(String user);

    abstract public Date getLastTime(String user);

}
