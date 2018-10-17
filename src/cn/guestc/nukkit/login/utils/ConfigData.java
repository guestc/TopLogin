package cn.guestc.nukkit.login.utils;

public class ConfigData {
    public int UserMaxLen = 16;
    public int UserMinLen = 3;
    public int PasswdMaxLen = 16;
    public int PasswdMinLen = 8;
    public boolean UnloginMove = false;
    public boolean UnloginMessage = false;
    public boolean UnloginBreak = false;
    public boolean UnloginPlace = false;
    public boolean UnloginCraft = false;
    public boolean UnloginInteract = false;
    public boolean UnloginPickItem = false;
    public boolean UnloginDropItem = false;
    public boolean AutoLogin = true;
    public int AutoLoginValidHours = 2;
    public byte MessageType = 1;
    public String LoginType = "text";

}
