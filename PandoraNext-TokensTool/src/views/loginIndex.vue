<template>
  <div class="content_login">
    <meta
      name="viewport"
      content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0,user-scalable=no"
    />
    <div class="login-container">
      <img :src="image" alt="Your Image" size:30 />
      <h1>Pandora-TokensTool</h1>
    </div>
    <div class="container">
      <div
        style="display: flex; transform: translate(0vw, 2vh); font-size: 14.6px"
      >
        <van-field
          v-model="username"
          name="用户名"
          label="用户名"
          placeholder="用户名"
          class="userName"
        />
      </div>
      <div
        style="
          display: flex;
          transform: translate(0vw, 2vh);
          margin-top: 5vh;
          font-size: 14.6px;
        "
      >
        <van-field
          v-model="password"
          type="password"
          name="密码"
          label="密码"
          placeholder="密码"
          class="userName"
        />
      </div>
      <div
        style="display: flex; transform: translate(0.5vw, 0vh); margin-top: 6vh"
      >
        <van-checkbox
          class="remember"
          v-model="checked"
          checked-color="#0ea27e"
          icon-size="13.5px"
        >
          <h9 style="font-size: 13.5px; transform: translateX(7px)"
            >记住密码</h9
          ></van-checkbox
        >
      </div>
      <div
        style="
          display: block;
          transform: translate(0vw, 0vh);
          margin-top: 3.5vh;
        "
      >
        <input type="submit" @click="submit" value="登录" class="userName" />
      </div>
    </div>
    <!-- 页面结尾文字 -->
    <div class="bottom">
      <div style="text-align: center; transform: translateY(0vh)">
        <div v-if="page == false">
          <h3>
            获取token
            <a
              href="https://chat.OpenAI.com/api/auth/session"
              >官网地址
            </a>
            <a href="https://ai.fakeopen.com/auth">Pandora地址</a>
            欢迎大家来扩展
            <a href="https://github.com/Yanyutin753/PandoraNext-TokensTool"
              >PandoraNext-TokensTool v0.4.7.1
            </a>
          </h3>
        </div>
        <div v-else>
          <h3>
            获取token
            <a
              href="https://chat.OpenAI.com/api/auth/session"
              >官网地址
            </a>
            <a href="https://ai.fakeopen.com/auth">Pandora地址</a>
            <br />
            欢迎大家来扩展
            <a href="https://github.com/Yanyutin753/PandoraNext-TokensTool"
              >PandoraNext-TokensTool v0.4.7.1
            </a>
          </h3>
        </div>
      </div>
    </div>
  </div>
</template>
  <script>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import png from "../asserts/chatGpt.jpg";
import { ElMessage } from "element-plus";
export default {
  setup() {
    const router = useRouter();
    const username = ref("");
    const password = ref("");
    const checked = ref("");
    const image = png;
    const page = ref(true);

    onMounted(() => {
      const savedUsername = localStorage.getItem("savedUsername");
      const savedPassword = localStorage.getItem("savedPassword");
      const savedRemember = localStorage.getItem("savedRemember");

      if (savedRemember === "true") {
        username.value = savedUsername || "";
        password.value = savedPassword || "";
        checked.value = true;
      }
      if (window.innerWidth > 767) {
        page.value = false;
      }
      console.log(page.value);
    });

    const submit = () => {
      // 处理登录逻辑
      if (checked.value) {
        // 如果记住密码被选中，将用户名和密码保存到本地存储
        localStorage.setItem("savedUsername", username.value);
        localStorage.setItem("savedPassword", password.value);
        localStorage.setItem("savedRemember", "true");
      } else {
        // 如果不记住密码，清除本地存储中的信息
        localStorage.removeItem("savedUsername");
        localStorage.removeItem("savedPassword");
        localStorage.removeItem("savedRemember");
      }
      let setting = {
        loginUsername: username.value,
        loginPassword: password.value,
      };
      fetch("/api/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${setting}`,
        },
        body: JSON.stringify(setting),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.code === 1) {
            // 修改此行，使用严格相等运算符
            console.log("登录成功", data.data);
            const token = data.data;
            localStorage.setItem("jwtToken", token);
            ElMessage("登录成功！");
            setTimeout(() => {
              if (window.innerWidth <= 1000) {
                router.replace("/iphone");
              } else {
                router.replace("/");
              }
            }, 1000);
          } else {
            console.error("登录失败");
            ElMessage("账号或密码错误！");
          }
        })
        .catch((error) => {
          console.error("登录时出现错误:", error);
          ElMessage("账号或密码错误！");
        });
    };

    return {
      username,
      password,
      image,
      checked,
      submit,
      page,
    };
  },
};
</script>
  
<style scoped>
.content_login {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  background: rgb(184 255 225 / 22%);
  zoom: 1;
  /* 禁止页面内容缩放 */
  width: 100vw;
  /* 设置容器宽度 */
  height: 100vh;
  /* 设置容器高度，使其占满整个视口 */
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
  /* 隐藏水平滚动条 */
  flex-direction: column;
}
body {
  font-family: Arial, sans-serif;
  background-color: #f2f2f2;
}
.van-cell {
  /* background: #fbfbfb; */
  box-shadow: 2px 2px 2px rgba(113, 55, 55, 0.08);
  border-radius: 14px;
}
.userName {
  font-size: 14.6px;
}

.container {
  max-width: 30vw;
  height: auto;
  margin: 0 auto;
  background-color: #fff;
  padding: 40px;
  transform: translateY(10vh);
  box-shadow: 0px 0px 3.5px rgba(0, 0, 0, 0.2);
  border-radius: 14px;
}

input[type="submit"] {
  width: 100%;
  padding: 14px;
  background-color: #0ea27e;
  color: #fff;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  font-weight: bold;
}

input[type="submit"]:hover {
  background-color: #4caf50;
}

#background {
  width: 100%;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background-size: cover;
  background-position: 0px -560px;
  z-index: -1;
  opacity: 0.99;
}
.login-container {
  color: #fff;
  margin-top: 3vh;
  margin-bottom: 7vh;
  background-color: #0ea27e;
  padding: 17.5px;
  border-radius: 25px;
  backdrop-filter: blur(7px);
  height: 5vh;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: row; /* 指定Flex容器的主轴方向，这里设置为水平方向 */
}
.login-container h1 {
  margin-right: 20px; /* 在文本和图像之间添加一些间距 */
  font-size: 35px;
}

.login-container img {
  width: 40px;
  height: 40px;
}

/* 字体 a 超链接 h3 字体大小 */
a {
  color: #0ea27e;
}

h3 {
  font-size: 20.5px;
  color: #606266;
}

.van-field__label {
  width: 120px;
  font-size: 14.6px;
}

.bottom {
  /* 占据剩余空间 */
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 让底部组件紧贴容器底部 */
  justify-content: flex-end;
  margin-top: 10px;
}

.van-field__label :deep() .label {
  font-size: 18.6px;
}
@media only screen and (max-width: 767px) {
  .content_login {
    flex: 1; /* 占据剩余空间 */
    display: flex;
    background: rgb(184 255 225 / 22%);
    zoom: 1;
    /* 禁止页面内容缩放 */
    width: 100vw;
    /* 设置容器宽度 */
    height: 100vh;
    /* 设置容器高度，使其占满整个视口 */
    overflow-y: auto;
    /* 显示垂直滚动条 */
    overflow-x: hidden;
    /* 隐藏水平滚动条 */
    flex-direction: column;
  }
  .login-container h1 {
    margin-right: 20px; /* 在文本和图像之间添加一些间距 */
    font-size: 30px;
  }
  .login-container {
    color: #fff;
    margin-top: 0vh;
    margin-bottom: 5vh;
    background-color: #0ea27e;
    padding: 17.5px;
    border-radius: 10px;
    border-top-left-radius: 0; /* 将上左角设置为直角 */
    border-top-right-radius: 0; /* 将上右角设置为直角 */
    backdrop-filter: blur(7px);
    height: 5vh;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: row; /* 指定Flex容器的主轴方向，这里设置为水平方向 */
  }

  .container {
    max-width: 65vw;
    height: auto;
    margin: 0 auto;
    background-color: #fff;
    padding: 30px;
    transform: translateY(10vh);
    box-shadow: 0px 0px 3px rgba(0, 0, 0, 0.2);
    border-radius: 14px;
  }
  .userName {
    font-size: 14.6px;
  }
  .login-container h1 {
    margin-right: 7px;
    font-size: 28px;
    justify-content: center;
    align-items: center;
  }
  .van-field__label {
    font-size: 14.6px;
  }
  h3 {
    font-size: 14.5px;
    color: #606266;
  }
  input[type="submit"] {
    width: 100%;
    background-color: #0ea27e;
    border-radius: 14px;
    height: 40px;
  }
  .el-message--info .el-message__content {
    color: var(--el-message-text-color);
    overflow-wrap: anywhere;
    width: 41vw;
  }
  .van-field__label {
    width: 84px;
    font-size: 13px;
  }
}
</style>