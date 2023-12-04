# PandoraNext-TokensTool

## 不准白嫖，请给我免费的star⭐吧，十分感谢！

## 简介

[PandoraNext-TokensTool](https://github.com/Yanyutin753/PandoraNext-TokensTool) 是一个基于 [PandoraNext](https://github.com/pandora-next/deploy) 中的便捷添加管理tokens.json和config.josn的工具，旨在更加简便地使用[pandoraNext](https://github.com/pandora-next/deploy)资源，手机端电脑端在线管理PandoraNext,使得可以方便地白嫖 chatGPT，本工具是站在巨人的肩膀上，方便大家，麻烦给个不要钱的星星⭐⭐⭐！


## 功能特性

1. **保存账号信息：** 支持保存 OpenAI 账号密码和 token，方便快速访问。

2. **自动添加删除修改token：** 工具能够自动在 tokens.josn 中添加删除刷新token，简化配置过程。

3. **每五天自动通过openAI账号密码刷新share_token,access_token,pool_token**， tokensTool工具会每五天自动通过openAI账号密码刷新tokens,重启PandoraNext，方便使用。

4. **通过账号密码添加token**该功能如今恢复正常 ，避免查找繁琐的token

5. **一键暂停，启动,重启PandoraNext** ，使得修改token效率更高

6. **支持多种PandoraNext部署方法，开箱就用**
   
7. **支持在线修改config.json文件,重启PandoraNext生效**

8. **支持热重载，需要在配置文件或者在网页上添加重载密码，开启服务**

9. **新增脚本文件,真一键部署并更新PandoraNext和tokensTool双服务**

- PandoraNext的热重载改网站密码和热重载密码还是优点bug，建议修改config.json配置文件之后按重启PandoraNext服务

- 现如今只支持账号密码登录，希望大佬能扩充！

#### 初始用户名：root 初始密码值:123456 
#### **(请务必在第一次登录之后修改)**

------------

## 使用方法

### 一键部署PandoraNext和tokensTool(最强推荐)

#### 将直接拉取最新版本的PandoraNext和tokensTool

```
# 安装git
sudo apt update
sudo apt install git

# 国内
cd / && git clone https://gitee.com/yangyangEN/tokenTools-sh.git

# 国外
cd / && git clone https://github.com/Yanyutin753/tokenTools-sh.git

# 运行脚本
cd /tokenTools-sh && sudo sh install.sh

# 一键更新pandoraNext和tokensTool服务
cd /tokenTools-sh && sudo sh update.sh

```

#### 1. 开放8081和8181端口，先访问8081,然后填写系统变量，把127.0.0.1:8181改成0.0.0.0:8181

#### 2. 在8081页面的系统变量里的tokentool设置更改账号密码，并填写[license_ip](https://dash.pandoranext.com)
***注意是下面类似括号里的内容*****
```
curl -fLO "https://dash.pandoranext.com/data/ (uVlk_4ilqs23dfsdfdsfsdfOlgaPdNkgGDwesNmVHGoI_23) /license.jwt"
```

#### 3.设置相应的信息，点击重启PandoraNext，期间如遇打不开PandoraNext,请耐心等待一会，再重启PandoraNext即可（热重载需要在容器启动之后才能进行）


### 具体想要修改一键部署的端口可以在/pandora/docker-compose.yml里修改

------------

## 非一键部署方式
### 环境变量

- 启动端口号：server.port=8081

- PandoraNext的部署方式：--deployWay=releases/docker
  
* 手动部署--deployWay=releases

   * docker和docker-compose部署 --deployWay=docker

- PandoraNext中存放config.json的位置（docker部署在上面代码查到位置） --deployPosition
  
   * 如果你的tokensTool的jar包放在了config.json --deployPosition=default

   * 如果不在的话就填你config.json的文件目录 例如：--deployPosition=/www/wwwroot/PandoraNext/PandoraNext-v0.1.3-linux-386-51a5f88

- 是否开启热重载：  --hotReload=true

- PandoraNext的部署ip  --pandoara_Ip=

   * 如果你是静态IP,这填上你的PandoraNext部署的外网IP(格式：20.123.43.52）

   * 如果你是动态IP，填上default，则将通过[淘宝链接](https://www.taobao.com/help/getip.php)自动获取你的外网ip

- ⭐记住路径没有/config.json

- 记得修改你的路径，密码，账号，端口号（选填），最最重要没有括号


#### 如果不知道docker里面容器config.json位置，可以参考以下代码

![image](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/8aacabd0-4cb1-4d44-a5e6-4bf1136b3865)
```
# 查找容器名为 "PandoraNext" 的所有挂载信息
docker inspect -f '{{range .Mounts}}{{.Destination}}: {{.Source}}{{"\n"}}{{end}}' PandoraNext
# 拿到:后面的地址
```

### java部署详情

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

##### 运行程序
```
# 例如
nohup java -jar pandoraNext-0.0.1-SNAPSHOT.jar --server.port=8081 --deployWay=releases --deployPosition=default --pandoara_Ip=（你的Pandoara部署的服务器外网Ip) Or default > myput.log 2>&1 &

# 等待一会 放行8081端口即可运行（自行调整）
```

## docker部署详情
```
# 先拉取镜像
docker pull yangclivia/tokenstool:latest
```
#### 手动部署PandoraNext启动命令
```
docker run -d \
  --restart=always \
  -u root \
  --name tokensTool \
  --net=host \
  --pid=host \
  --privileged=true \
  -v （你config.json的文件目录）:（你config.json的文件目录） \
  yangclivia/tokenstool:latest \
  --deployWay=releases \
  --deployPosition=（你config.json的文件目录） \
  --hotReload=true \
  --server.port=8081 \
  --pandoara_Ip=你的Pandoara部署的服务器外网Ip Or default
```
#### Docker部署PandoraNext启动命令
```
docker run -d \
  --restart=always \
  -u root \
  --name tokensTool \
  --net=host \
  --pid=host \
  --privileged=true \
  -v （你config.json的文件目录）:（你config.json的文件目录） \
  -v /var/run/docker.sock:/var/run/docker.sock \
  yangclivia/tokenstool:latest \
  --deployWay=docker \
  --deployPosition=（你config.json的文件目录） \
  --hotReload=true \
  --server.port=8081 \
  --pandoara_Ip=你的Pandoara部署的服务器外网Ip Or default
```

### Docker Compose部署详情
## 代码模板
```
version: '3'
services:
  tokensTool:
    image: yangclivia/tokenstool:latest
    container_name: tokensTool
    restart: always
    user: root
    network_mode: host
    pid: host
    privileged: true
    volumes:
      - （你config.json的文件目录）:（你config.json的文件目录）
      - /var/run/docker.sock:/var/run/docker.sock
    command: 
      - --deployWay=(部署方式看环境变量)
      - --deployPosition=（你config.json的文件目录）
      - --hotReload=true
      - --server.port=8081
      - --pandoara_Ip=你的Pandoara部署的服务器外网Ip Or default
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

## 接口

1. /shared_token
   * 请求方式为get
   * 示例网址：http://ip:8081/shared_token?password=123456
   * 返回
    ```
     {
    "code": 1,
    "msg": "success",
    "data": [
              "fk-Yasdasdasdasdasdasd",
              "fk-ssadasdd asdasdasdasM"
          ]
      }
     ```
    
2 /pool_token
   * 请求方式为get
   * 示例网址：http://ip:8081/pool_token?password=123456
   * 返回
     ```
      {
          "code": 1,
          "msg": "success",
          "data": "pk-L25JirYw2mWiyRqasdasdSCYrnovbHkmXIA7jDUs-Zpug"
      }
     ```
    
3./token/shared_token
   * 请求方式为get
   * 示例网址：http://ip:8081/token/shared_token?password=123456?tokenName=tokenstool
   * 返回
     ```
     {
       "code": 1,
       "msg": "success",
       "data": "fk-I2hsq9weY_NnBm0Fgcsadsasdasdasg9_OFwn7A"
      }
     ```
	 
# 图片展示

# 手机端展示

### 肝了一个晚上（给我赞让我写的更有劲吧！）

![63a8b2a97b7f7b650ee0d8fc823b413](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/ee3b4306-07d4-40ed-a6b6-f62b1d61004d)


### 在线修改系统设置，启动，重启，暂停PandoraNext操作

![9f04e44841c765f8d066fbdda395e5f](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/c3d3a252-c936-4156-886e-2a17ccd540db)

### 项目遵循相应的开源规则，请自行食用


## 强调

本项目是站在巨人的肩膀上的，感谢[Pandora](https://github.com/pandora-next/deploy)超级无敌始皇!，欢迎各位来帮助修改本项目，使得本项目变得更方便，更简单！

## 新增群聊，点了⭐️可以进群讨论部署，无广，广子踢掉

![b5c8df8d7121836cc7cb4b957acd3bd](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/fe714766-eed5-4016-a143-518d2ef95cc6)

### 请给我一个免费的⭐吧！！！

![star-history-2023123](https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/71108198-87e4-4c05-af1b-c03a3818084e)



