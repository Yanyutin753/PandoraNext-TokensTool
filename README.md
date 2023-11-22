# PandoraNext-TokensTool
## 不准白嫖，请给我免费的star⭐吧，十分感谢！

## 简介

[PandoraNext-TokensTool](https://github.com/Yanyutin753/PandoraNext-TokensTool) 是一个基于 [PandoraNext](https://github.com/pandora-next/deploy) 中的便捷添加管理tokens.json和config.josn的工具，旨在更加简便地使用[pandoraNext](https://github.com/pandora-next/deploy)资源，手机端电脑端在线管理PandoraNext,使得可以方便地白嫖 chatGPT，本工具是站在巨人的肩膀上，方便大家，麻烦给个不要钱的星星⭐⭐⭐！

### 请大家配合PandoraNext一起使用(一定要配合PandoraNext使用，能知道tokens.json和config.json的位置)

#### 如果不知道docker里面容器PandoraNext存储卷位置，可以参考以下代码

![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/8aacabd0-4cb1-4d44-a5e6-4bf1136b3865)

```
# 查找容器名为 "PandoraNext" 的所有挂载信息
docker inspect -f '{{range .Mounts}}{{.Destination}}: {{.Source}}{{"\n"}}{{end}}' PandoraNext
# 拿到后面的地址
```

## 功能特性

1. **保存账号信息：** 支持保存 OpenAI 账号密码和 token，方便快速访问。

2. **自动添加删除修改token：** 工具能够自动在 tokens.josn 中添加删除刷新token，简化配置过程。

3. **每五天自动通过openAI账号密码刷新token,更新渠道：** 工具会每五天自动通过openAI账号密码刷新tokens,重启PandoraNext，方便使用。

4. **通过账号密码添加token** ，避免查找繁琐的token

5. **一键重启PandoraNext** ，使得修改token效率更高

6. **支持多种PandoraNext部署方法，开箱就用**

7. **支持一键暂停，启动，重启PandoraNext**
   
8. **支持在线修改config.json文件,重启PandoraNext生效**

9. **支持热重载，需要在配置文件或者在网页上添加重载密码，开启服务**

- PandoraNext的热重载改网站密码和热重载密码还是优点bug，建议修改config.json配置文件之后按重启PandoraNext服务

- 现如今只支持账号密码登录，希望大佬能扩充！

### 初始用户名：root 初始密码值:123456 (可自行调整)

# 图片展示

### 一键重启docker里的pandoraNext容器

![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/e51d2fe1-e07d-48b8-be96-f860f65274c6)


### 管理Token,记录token更新时间，自动更新tokens.json和config.json

![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/8906380f-886c-48cd-bf42-f7931f641069)


### 在线开启热重载，避免重启PandoraNext操作

![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/e95a4a95-b85d-4539-9d8a-26c5869768a9)



## 手机端展示

### 肝了一个晚上（给我赞让我写的更有劲吧！）

![63a8b2a97b7f7b650ee0d8fc823b413](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/ee3b4306-07d4-40ed-a6b6-f62b1d61004d)


### 在线修改系统设置，启动，重启，暂停PandoraNext操作

![1f272d22383b975be2f32764ef1774a](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/bdc4bd3e-f984-4358-95ea-82cdc6e583a4)



## 使用方法

- 1.请确保部署好了PandoraNext,拿到的JWT令牌

- 2.下载[启动包](https://github.com/Yanyutin753/PandoraNext-TokensTool/tree/main/simplyDeploy)，jar包或者拉去docker镜像

- 3.上传到PandoraNext存放config.json和tokens.json的位置或者随便一个目录

- 4.然后看下方部署指令
  
#### 宝塔的pandoraNext的docker位置一般在存储卷里面
<img width="1278" alt="屏幕截图 2023-11-17 203024" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/96c1a9a8-5408-4575-a144-5ce913edc3d9">

## java部署详情
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
cd （你的jar包的位置）
```

##### 环境变量

- 登录密码：loginPassword=123456

- 登录账号：loginUsername=root

- 启动端口号：server.port=8081

- PandoraNext的部署方式：--deployWay=releases/docker
  
* 手动部署--deployWay=releases

   * docker和docker-compose部署 --deployWay=docker

- PandoraNext中存放config.json的位置 --deployPosition
  
   * 如果你的tokensTool的jar包放在了config.json --deployPosition=default

   * 如果不在的话就填你config.json的文件目录 例如：--deployPosition=/www/wwwroot/PandoraNext/PandoraNext-v0.1.3-linux-386-51a5f88

- 是否开启热重载：  --hotReload=true

- PandoraNext的部署地址  --pandoraNext_Url=http(s)://ip:port(或者你的域名)

- ⭐记住路径没有/config.json

- 记得修改你的路径，密码，账号，端口号（选填），最最重要没有括号

##### 运行程序

```
# 例如
nohup java -jar pandoraNext-0.0.1-SNAPSHOT.jar --server.port=8081 --loginUsername=root --loginPassword=123456 --deployWay=releases --deployPosition=default > myput.log 2>&1 &

# 等待一会 放行8081端口即可运行（自行调整）
```

## docker部署详情

```
# 先拉取镜像
docker pull yangclivia/tokenstool:latest
```

#### 环境变量

- 登录密码：loginPassword=123456

- 登录账号：loginUsername=root

- 启动端口号：server.port=8081

- PandoraNext的部署方式：--deployWay=releases/docker
  
   * 手动部署--deployWay=releases

   * docker和docker-compose部署 --deployWay=docker

- PandoraNext中存放config.json的位置 --deployPosition
  
   * 如果你的tokensTool的jar包放在了config.json --deployPosition=default

   * 如果不在的话就填你config.json的文件目录 例如：--deployPosition=/www/wwwroot/PandoraNext/PandoraNext-v0.1.3-linux-386-51a5f88

- 是否开启热重载：  --hotReload=true

- PandoraNext的部署地址  --pandoraNext_Url=http(s)://ip:port(或者你的域名)

- ⭐记住路径没有/config.json

- 记得修改你的路径，密码，账号，端口号（选填），最最重要没有括号

#### 手动部署PandoraNext启动命令

```
docker run -d \
  --restart=always \
  -u root \
  --name tokensTool \
  -p 8081:8081 \
  --net=host \
  --pid=host \
  --privileged=true \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v （你config.json的文件目录）:（你config.json的文件目录） \
  yangclivia/tokenstool:latest \
  --loginUsername=(你的登录账号) \
  --loginPassword=(你的登录密码)  \
  --deployWay=docker \
  --deployPosition=（你config.json的文件目录）
  --hotReload=true
  --pandoraNext_Url=http(s)://ip:port(或者你的域名)
```

#### Docker部署PandoraNext启动命令

```
docker run -d \
  --restart=always \
  -u root \
  --name tokensTool \
  -p 8081:8081 \
  --net=host \
  --pid=host \
  --privileged=true \
  -v （你config.json的文件目录）:（你config.json的文件目录） \
  -v /var/run/docker.sock:/var/run/docker.sock \
  yangclivia/tokenstool:latest \
  --loginUsername=(你的登录账号) \
  --loginPassword=(你的登录密码) \
  --deployWay=releases \
  --deployPosition=（你config.json的文件目录）
  --hotReload=true
  --pandoraNext_Url=http(s)://ip:port(或者你的域名)
```

## Docker Compose部署详情

#### 环境变量

- 登录密码：loginPassword=123456

- 登录账号：loginUsername=root

- 启动端口号：server.port=8081

- PandoraNext的部署方式：--deployWay=releases/docker
     
   * 手动部署--deployWay=releases

   * docker和docker-compose部署 --deployWay=docker

- PandoraNext中存放config.json的位置 --deployPosition
  
   * 如果你的tokensTool的jar包放在了config.json --deployPosition=default

   * 如果不在的话就填你config.json的文件目录 例如：--deployPosition=/www/wwwroot/PandoraNext/PandoraNext-v0.1.3-linux-386-51a5f88

- 是否开启热重载：  --hotReload=true

- PandoraNext的部署地址  --pandoraNext_Url=http(s)://ip:port(或者你的域名)

- ⭐记住路径没有/config.json

- 记得修改你的路径，密码，账号，端口号（选填），最最重要没有括号

## 代码模板
```
version: '3'

services:
  tokensTool:
    image: yangclivia/tokenstool:latest
    container_name: tokensTool
    restart: always
    user: root
    ports:
      - "8081:8081"
    network_mode: host
    pid: host
    privileged: true
    volumes:
      - （你config.json的文件目录）:（你config.json的文件目录）
      - /var/run/docker.sock:/var/run/docker.sock
    command: 
      - --loginUsername=(你的登录账号)
      - --loginPassword=(你的登录密码)
      - --deployWay=(部署方式看环境变量)
      - --deployPosition=（你config.json的文件目录）
      - --hotReload=true
      - --pandoraNext_Url=http(s)://ip:port(或者你的域名)
```

##### 启动tokensTool
```

cd (你的docker-compose.yml位置)

docker-compose up -d
```

##### 更新tokensTool项目代码
```
cd (你的docker-compose.yml位置)

docker-compose pull

docker-compose up -d
```

### 想要二开项目的友友们，可以自行遵循相应的开源规则更改前后端项目，本人小白，项目写的不太好，还请谅解！

### 项目遵循相应的开源规则，请自行食用

## 强调

本项目是站在巨人的肩膀上的，感谢[Pandora](https://github.com/pandora-next/deploy)超级无敌始皇!，欢迎各位来帮助修改本项目，使得本项目变得更方便，更简单！

## 新增群聊，可以进群讨论部署，无广，广子踢掉
![5323c0e5136a67e7844fdc70cc54016](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/a298d46d-5fd2-45d8-9e7b-0e4e99d8e289)


### 请给我一个免费的⭐吧！！！

![star-history-20231122](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/13003607-b7c9-4c82-a569-a9ed88c88d3d)


