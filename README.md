# TopLogin


**A simple login for Nukkit.**
**Support **`Mysql` **And** `Yaml`


##Commands


- **/login [passwd]** : **Login in server**
- **/passwd [new passwd]** : **Change player passwd**
- **/setpasswd [player] [new passswd]** : **Set player passwd in Console**
- **/setmail [mail]** : **Set player mail**


##RegisterSteps


- **Type your ** `Name`
- **Type your ** `Passwd`
- **Comfirm your ** `Passwd`
- **Type your ** `Mail`
- **Register success!**


##Config


| Key  | Type | Default | Description |
| :------ | :--: | :----: | :-------------- |
|storage-type | string | yaml | Choose `mysql` or `yaml`|
|mysql-src| string | 127.0.0.1 | Mysql Source |
|mysql-user| string | root | Mysql User |
|mysql-passwd | string | 123456789 | Mysql Passwd|
|mysql-database | string | toplogin | Mysql DataBase |
|mysql-table-prefix | string | mc_login | Mysql table `preifx` name|
|language | string | language_chs.yml | Choose language file in `PlugDataFolder`|
|passwd-min | int | 8 | passwd min lenght |
|passwd-max | int | 16 | passwd max lenght |
|username-min | int | 3 | player name min lenght |
|username-max | int | 16 | player name max lenght |
|login-type | string | command | - `command` : `/login [passwd]`  **`OR`** `text` : `[passwd]` enter in chat|
|autologin | bool | true | Enable autologin |
|autologin-valid-hours | int | 2 | Set autologin in 2 hours |
|unlogin-message | bool | false | can receive message except TopLogin Plugin Message without login |
|unlogin-break | bool | false | can break block without login |
|unlogin-place | bool | false | can place block without login |
|unlogin-interact | bool | false | can interact without login |
|unlogin-pickitem | bool | false | can pick up item without login |
|unlogin-drop | bool | false | can drop item without login |
|unlogin-move | bool | true | can move without login |
|message-type | int | 1 | PluginMessageType `1=chat` `3=popup` `4=tip` |
|ban-username | array | ["steve","steve*"] | can't register username `*`=`\w` |
