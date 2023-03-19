# UinMiningBoard
- Minecraft Fabric Server Mine ScoreBoard
- 挖掘榜单


只能装在`Fabric 1.19.2`服务端,客户端无效

需要 [Fabric API](https://www.mcmod.cn/class/3124.html)

看图就明白了:
![image](https://user-images.githubusercontent.com/68675068/226177277-ec0f71b0-e637-4105-9129-688a659f4015.png)

### 命令
- `/uinminingboard help` 帮助
- `/uinminingboard score <玩家>` 获取玩家的挖掘量(不填玩家就是获取自己)
- 以下是OP才能使用的命令
  - `/uinminingboard ban <玩家>` 禁止玩家上榜
  - `/uinminingboard unban <玩家>` 允许玩家上榜
  - `/uinminingboard redirect <玩家>` 重定向玩家,用于迁移后uuid发生变化的场景,如果玩家不需要重定向会提示

### 已知bug
由于`ScoreBoard`由原版提供,其数值限制为`2147483648`,故挖掘量最大不能超过该值,不然变负数

### 联动
挖掘榜单数据可以从[FiFuMiningList](https://github.com/Core2002/MiningList)迁移

直接将`data.json` `uuid2name.json` `ignore.json`复制粘贴覆盖掉`Fabric`服务端`config`文件夹中同名文件即可

### 缘起
该模组为了[青岛科技大学-MC协会](https://skin.qustmc.cn/) 开发
