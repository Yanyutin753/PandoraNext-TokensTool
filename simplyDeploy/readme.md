# tokensTool使用教程

##### ### *请先确保安装好tokensTool和pandoraNext之后食用本教程*

## 初始配置介绍
###*1.首先打开tokensTool，点击右上角的系统设置，找到tokensTool设置*

<img width="400" alt="56bfc0cf0b70582cf8dd2c80d498253" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/e6e024d4-adf6-4bce-9e4f-1922595f5a78">

> **登录用户名**： 用于设置tokensTool的登录账号

> **登录密码**：  用于设置tokensTool的登录密码

> **Proxy接口前缀**： 用于设置PandoraNext关于API的接口前缀

> **Proxy模式Url**：填写（你的PandoraNext的API的网站）
*  默认为**default**的话，就会获取本机填写的PandoraNext的API接口
* 如果需要自定义，则按照这个格式**http(s)://(ip:port或者域名)/你填的Proxy接口前缀**

> **重载服务密码**：填写重载服务密码之后，将会开启PandoraNext的**热重载**这个功能

> **访问密码**： 填写访问密码之后，开启PandoraNext的访问限制，web端填写访问密码才能访问

> **验证LicenseId**：填写从始皇的[接口](https://dash.pandoranext.com/)拿到的license_id

> **Tokenstool接口**: 打开将会开启tokensTool的接口，你可以通过访问tokensTool的接口来获取到你想要拿到tokensTool里的pool_token或者share_token，方便第三方应用使用PandoraNext的API。

> **监管容器**: 具体可以看你的docker里的PandoraNext服务的容器名称，默认为PandoraNext，填写完之后可以控制容器的开启，暂停，重载与重启。（release部署使用默认即可）

#### *填写完毕点击提交之后，会退出界面，重新登录之后，重新启动PandoraNext，等待PandoraNext启动完成！*



#### 注意事项
* 所有密码皆要按照网址提示进行填写，尽量使用强密码，不尽量避免重复密码。
* 如遇重启失败问题，请检查PandoraNext的绑定IP和端口，是否为0.0.0.0:8181，并查看报错，如果是拒绝IP的错误，请删除data文件夹里面的license_id，静待片刻，重启PandoraNext。
* 如遇初始登录不上的问题，请检查运行tokensTool时是否配置正确，正常来说一般都是docker填写的路径不对。

------------

##2. 修改PandoraNext里面的其他配置介绍

<img width="400"  alt="d3e5ac6e9e9da3da99163de9872b953" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/a42216aa-d945-44bd-9cce-8e7fa107e45b">

> **对话标题**： 隔离会话可以设置标题了，而不再是千篇一律的*号。

> **绑定IP和端口**：  指定绑定IP和端口，在docker内，IP只能用0.0.0.0，否则映射不出来。

> **请求超时时间**： timeout是请求的超时时间，单位为秒。

> **是否分享对话**：对于GPT中创建的对话分享，是否需要登录才能查看。为true则无需登录即可查看。
> **是否配置证书**：配置PandoraNext直接以https启动。
* enabled 是否启用，true或false。启用时必须配置证书和密钥文件路径。
* cert_file 证书文件路径。
* key_file 密钥文件路径。

> **代理服务URL**： 指定部署服务流量走代理，如：http://127.0.0.1:8888、socks5://127.0.0.1:7980

> **白名单**：邮箱数组指定哪些用户可以登录使用，用户名/密码登录受限制，各种Token登录受限。内置tokens不受限。
* whitelist为null则不限制，为空数组[]则限制所有账号，内置tokens不受限。
* 一个whitelist的例子："whitelist": ["mail2@test.com", "mail2@test.com"]

------------

## 3.添加Token介绍

<img width="400" alt="764d2f1534d7a2a3261c6cd833d9470" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/872590e1-fae8-4f75-a42f-706891526ae1">

> **Token用户名**： token的独一无二的名称，用于PandoraNext直接输入该名称登录（不允许重名，重名则覆盖！）

> **OpenAI用户名**：（必填） OpenAI的邮箱，乱填则无法刷新session_token

> **OpenAI密码**：（必填） OpenAI的密码，乱填则无法刷新session_token

> **OpenAI的token**：（选填）只能填session_token，如填其他，可能会出现无法生成，无法合成pool_token的情况。

> **是否分享出来**：开启这个账号会出现在/shared.html中，登录页面会出现它的链接，且之后不能用Token用户名进行登录。

> **是否合成pool_token**： 对于该token是否参与合成pool_token

> **进入token的密码**：输入完Token用户名进入输入密码页面输入匹配


------------


## 4.Token功能介绍

<img width="400" height="400" alt="906eac572ef8bc4d9d23f91ff882383" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/39e62e3a-d14c-4001-94a3-3da7fafedcb0">

> **编辑**：字面意思，编辑token

> **查看**：字面意思，查看token

> **删除**：字面意思，删除token

> **刷新**：耗费102/108 通过邮箱密码刷新**session_token**,再通过**session_token**转化成**access_token**,**access_token**再转换成**share_token**,这些**tokens**都会在查看中出现，请点击查看。由于使用接口较多，刷新时间较长，请耐心等待！

> **生成**：耗费 2/8 通过原有的**session_token**转化成**access_token**,**access_token**再转换成**share_token**,这些tokens都会在查看中出现，请点击查看。

### 注意事项
* *刷新获得session_token的正常有效期是：**3个月**，所以请不要盲目刷新*

* *如遇刷新和生成功能失败的情况，请检查自己的**Proxy模式Url**是否填写正确，本工具将通过这个url加上接口进行请求，如发现失败，请按照上诉针对于**Proxy模式Url**的说明重新填写！

------------


## 5.poolToken列表介绍

<img width="700" height="200"alt="764d2f1534d7a2a3261c6cd833d9470" src="https://github.com/Yanyutin753/PandoraNext-TokensTool/assets/132346501/dd59e346-265b-4153-8b0a-cf3750da240a">

* 通过点击选择token,点击选择合成pool_token，填写独一无二的名字之后，pool_token将会在pool_token列表显示（请确保不要填写重复名称，否则会覆盖）

> **删除**：字面意思，删除并注销该**pool_token**

> **刷新**：是不会更改**pool_token**,只是重新刷新该**pool_token**里的**share_tokens**。

> **更换**：是会注销原先的**pool_token**，重新把相应的**share_tokens**填充到新的pool_token里面

## 6.tokensTool实现自动刷新access_token、share_token和pool_token的原理介绍

* *1.首先系统会在每天的3点先自动检查token距离现在是否超过了9或10或11的倍数，如果是的话则进行自动生成，将**session_token**重新生成**access_token**和**share_token***
* *2.其次系统会在每天的凌晨4点左右会重新刷新**pool_token**,将更新好了的和没更新好的一起放入**pool_token**中，完成更新。*

### 注意事项
* **由于始皇的接口限制，没到刷新时间，是不会更改access_token的，所以，这样做能尽可能的降低没有及时更新的风险，但是不怕一万就怕万一，如果是在时遇到问题了，还请大家手动按一下按钮进行刷新，谢谢大家的不断支持！**

------------

## ⭐最后如遇到实在解决不了的问题，欢迎加我微信，加进充满大佬的群吧，群佬有时间就会帮忙答疑的！

#### 让我们一起感谢始皇能让我们白嫖openAI吧，一起让项目更好！

