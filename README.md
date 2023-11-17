# PandoraNext-TokensTool
##### 请给我点个免费的star⭐吧，十分感谢！
## 简介

[PandoraNext-TokensTool](https://github.com/Yanyutin753/PandoraNext-TokensTool) 是一个基于 [PandoraNext](https://github.com/pandora-next/deploy) 中的便捷添加管理token的工具，旨在更加简便地使用[pandoraNext](https://github.com/pandora-next/deploy)资源，使得可以方便地白嫖 chatGPT，本工具是站在巨人的肩膀上，方便大家，麻烦给个不要钱的星星⭐⭐⭐！

### 请大家配合PandoraNext一起使用
##### docker PandoraNext启动命令
```
# 记得在下面代码的指定位置填写好你的One-API数据表的密码
$ docker pull pengzhile/pandora-next
$ docker run -d --restart always --name PandoraNext --net=bridge -p 8181:8181 \
             -e PANDORA_NEXT_LICENSE="<JWT Token>" pengzhile/pandora-next

```

## 功能特性

1. **保存账号信息：** 支持保存 OpenAI 账号密码和 token，方便快速访问。

2. **自动添加删除修改token：** 工具能够自动在 One-API 中添加删除修改渠道，简化配置过程。

3. **每五天自动通过openAI账号密码刷新token,更新渠道：** 工具会每五天自动通过openAI账号密码刷新token,更新渠道，方便使用。

4. **通过账号密码添加token** ，避免查找繁琐的token

5. **一键重启PandoraNext** ，使得修改token效率更高

- 现如今只支持账号密码登录，希望大佬能扩充！

### 初始用户名：root 初始密码值:123456 (可自行调整)

# 图片展示
### 一键重启docket里的pandoraNext容器
![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/e51d2fe1-e07d-48b8-be96-f860f65274c6)


## 管理Token,记录token更新时间，自动更新One-API的渠道
![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/8906380f-886c-48cd-bf42-f7931f641069)


## 手机端展示
### 肝了一个晚上（给我赞让我写的更有劲吧！）
![63a8b2a97b7f7b650ee0d8fc823b413](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/ee3b4306-07d4-40ed-a6b6-f62b1d61004d)

## 使用方法
- 1.请确保部署好了PandoraNext,拿到的JWT令牌
- 2.下载[启动包](https://github.com/Yanyutin753/PandoraNext-TokensTool/tree/main/simplyDeploy)，jar包
- 3.上传到PandoraNext存放config.json和tokens.json的位置
- 4.到达安装好包的路径下
  
#### 宝塔的pandoraNext的docker位置一般在
<img width="1278" alt="屏幕截图 2023-11-17 203024" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/96c1a9a8-5408-4575-a144-5ce913edc3d9">

## 部署详情
```
# 先拿到管理员权限
sudo su -
# 提示你输入密码进行确认。输入密码并按照提示完成验证。
```

```
# 安装 OpenJDK 11：
sudo apt install openjdk-11-jdk
# 安装完成后，可以通过运行以下命令来验证 JDK 安装：
java -version
```

```
# 填写下面路径
cd （你的PandoraNext存放config.json和tokens.json的位置）
# 修改并输入下面代码，填写你的初始密码，初始账号，想要启动的端口号
# 例如
nohup java -jar pandoraNext-0.0.1-SNAPSHOT.jar --server.port=8081 --loginUsername=root --loginPassword=123456 > output.log 2>&1 &
# 等待一会 放行8001端口即可运行（自行调整）
```

### 想要二开项目的友友们，可以自行更改前后端项目，本人小白，项目写的不太好，还请谅解！

## 强调
本项目是站在巨人的肩膀上的，感谢[Pandora](https://github.com/pandora-next/deploy)超级无敌始皇!，欢迎各位来帮助修改本项目，使得本项目变得更方便，更简单！

### 请给我一个免费的⭐吧！！！
