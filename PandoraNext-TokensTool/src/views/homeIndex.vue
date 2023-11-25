<template>
  <div class="content">
    <!-- 标头 -->
    <meta
      name="viewport"
      content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0,user-scalable=no"
    />
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
      active-text-color="#0ea27e"
      text-color="#0ea27e"
    >
      <el-menu-item>
        <img
          style="width: 45px"
          src="../asserts/openAi.jpg"
          alt="Element logo"
          @click="acc"
        />
      </el-menu-item>
      <div class="flex-grow" />
      <el-menu-item index="1">
        <a href="https://github.com/Yanyutin753/PandoraNext-TokensTool"
          >TokensTool地址</a
        >
      </el-menu-item>
      <el-menu-item index="2">
        <a
          href="https://chat.openai.com/api/auth/session"
          >OpenAI官网</a
        >
      </el-menu-item>
      <el-menu-item index="3">
        <a href="https://github.com/pandora-next/deploy">Pandora地址</a>
      </el-menu-item>
      <el-sub-menu index="4">
        <template #title>系统设置</template>
        <el-menu-item index="4-1" @click="openPandora"
          >开启PandoraNext</el-menu-item
        >
        <el-menu-item index="4-2" @click="closePandora"
          >暂停PandoraNext</el-menu-item
        >
        <el-menu-item index="4-3" @click="AgainPandora"
          >重启PandoraNext</el-menu-item
        >
        <el-menu-item index="4-3" @click="reloadPandora"
          >重载PandoraNext</el-menu-item
        >
        <el-menu-item index="4-2" @click="verifyPandora"
          >验证PandoraNext</el-menu-item
        >
        <el-menu-item index="4-4" @click="onRequireSetting"
          >系统参数设置</el-menu-item
        >
        <el-menu-item index="4-5" @click="logout">退出登录</el-menu-item>
      </el-sub-menu>
    </el-menu>
    <div style="display: block; transform: translate(5vw, 2.5vh); width=95vw;">
      <el-page-header :icon="null" title=" ">
        <template #content>
          <div class="flex items-center">
            <el-avatar :size="iconSize" class="mr-3" :src="image" />
            <span class="text-large font-600 mr-3">PandoraNext</span>
            <span
              class="text-sm mr-2"
              style="color: var(--el-text-color-regular)"
            >
              TokensTool
            </span>
            <el-tag>made by Yang</el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 添加token按钮 -->
    <van-floating-bubble
      v-model:offset="offset_task"
      axis="xy"
      icon="add-o"
      @click="addToken"
    />

    <div class="under">
      <div class="search">
        <div
          style="
          display: flex;
          background= white;
          margin: 0px 35px -17px 35px;
        "
        >
          <van-search
            v-model="value"
            shape="round"
            placeholder="请输入搜索token名称关键词"
            @search="onSearch"
          />
        </div>
        <div style="display: flex; width: 67vw; transform: translateX(0.5vw)">
          <!-- 数据表 -->
          <el-table
            v-loading="loading"
            :data="tableData"
            style="width: 72vw"
            height="610px"
          >
            <!-- Token名称表 宽150 -->
            <el-table-column label="名称" width="126">
              <template #default="scope">
                <div style="display: flex; align-items: center">
                  <el-icon><timer /></el-icon>
                  <span style="margin-left: 7px">{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>

            <!-- 账号信息表 宽260 -->
            <el-table-column label="账号信息" width="190">
              <template #default="scope">
                <el-popover
                  effect="light"
                  trigger="hover"
                  placement="top"
                  width="auto"
                >
                  <template #default>
                    <div>账号: {{ scope.row.username }}</div>
                    <div>密码: {{ scope.row.userPassword }}</div>
                  </template>
                  <template #reference>
                    <el-tag>{{ scope.row.username }}</el-tag>
                  </template>
                </el-popover>
              </template>
            </el-table-column>

            <!-- token值表 宽480 -->
            <el-table-column label="Token值" width="318">
              <template #default="scope">
                <el-popover
                  effect="light"
                  trigger="hover"
                  placement="top"
                  width="auto"
                >
                  <template #default>
                    <div>token: {{ scope.row.token }}</div>
                  </template>
                  <template #reference>
                    <!-- 做了超过50加...的操作 -->
                    <el-tag>{{ dataToken(scope.row.token) }}</el-tag>
                  </template>
                </el-popover>
              </template>
            </el-table-column>

            <!-- 有效时间表 宽210 -->
            <el-table-column label="有效时间" width="167">
              <template #default="scope">
                <el-popover
                  effect="light"
                  trigger="hover"
                  placement="top"
                  width="auto"
                >
                  <template #default>
                    <div>注册时间：{{ scope.row.updateTime }}</div>
                  </template>
                  <template #reference>
                    <el-tag
                      >距离过期还有：{{
                        formatDate(scope.row.updateTime)
                      }}</el-tag
                    >
                  </template>
                </el-popover>
              </template>
            </el-table-column>

            <!-- 操作方法表 宽300 方法handleEdit-->
            <el-table-column label="操作方法" width="268">
              <!-- 编辑操作按钮 -->
              <template #default="scope">
                <el-button
                  size="small"
                  @click="handleEdit(scope.$index, scope.row)"
                  >编辑</el-button
                >

                <!-- 查看操作按钮 方法showData-->
                <el-button
                  size="small"
                  type="primary"
                  @click="showData(scope.row)"
                  >查看</el-button
                >

                <!-- 删除操作按钮 方法deleteToken-->
                <el-button
                  size="small"
                  type="danger"
                  @click="deleteToken(scope.$index, scope.row)"
                  >删除</el-button
                >

                <!-- 刷新操作按钮 方法reNew-->
                <el-button size="small" type="success" @click="reNew(scope.row)"
                  >刷新</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div style="display: flex; margin-top: 3vh"></div>
      </div>

      <div class="bottom-component">
        <div style="text-align: center; transform: translateY(2.55vh)">
          <h2>
            欢迎大家来扩展
            <a href="https://github.com/Yanyutin753/PandoraNext-TokensTool"
              >pandoraNext-TokensTool v0.1.0
            </a>
            获取token
            <a href="https://chat.openai.com/auth/session">官网地址 </a>
            <a href="https://ai.fakeopen.com/auth">Pandora地址</a>
          </h2>
        </div>
        <br />
      </div>
    </div>
  </div>
  <!------------------------------------------------------------------------------------------------------>

  <!-- 修改token信息 主键 名称为show -->
  <van-dialog
    v-model:show="show"
    title="修改token信息"
    width="50vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="requireTokenDialog"
  >
    <div style="display: block">
      <van-form @submit="RequireToken()">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="temUsername"
            name="OpenAi用户名"
            label="OpenAi用户名"
            placeholder="OpenAi用户名"
            :rules="[{ required: true, message: '请填写OpenAi用户名' }]"
          />
          <br />
          <van-field
            v-model="temUserPassword"
            type="password"
            name="OpenAi密码"
            label="OpenAi密码"
            placeholder="OpenAi密码"
            :rules="[{ required: true, message: '请填写OpenAi密码' }]"
          />
          <br />
          <van-field name="switch" label="是否分享出来">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="temShared" />
            </template>
          </van-field>
          <div v-if="temShared == true">
            <br />
            <van-field name="switch" label="是否分享聊天记录">
              <template #right-icon>
                <van-switch
                  active-color="#0ea27e"
                  v-model="temShow_user_info"
                />
              </template>
            </van-field>
            <br />
            <van-field name="switch" label="是否显示金光">
              <template #right-icon>
                <van-switch active-color="#0ea27e" v-model="temPlus" />
              </template>
            </van-field>
          </div>
          <div v-if="temShared == false">
            <br />
            <van-field
              v-model="temPassword"
              type="temPassword"
              name="进入Token的密码"
              label="进入Token的密码"
              placeholder="填了将不会分享给他人！"
              :rules="[{ required: true, message: '如不分享token,则该项必填' }]"
            />
          </div>
          <br />
          <van-field
            v-model="temToken"
            rows="8"
            label="OpenAi的Token"
            type="textarea"
            maxlength="5000"
            placeholder="请填写OpenAi的Token(选填)"
            show-word-limit
          />
          <br />
        </van-cell-group>
        <div style="margin: 5.2px">
          <van-button round block color="#0ea27e" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
    <br />
  </van-dialog>
  <!------------------------------------------------------------------------------------------------------>

  <!-- 添加token信息 主键 名称为show_1 -->
  <van-dialog
    v-model:show="show_1"
    title="添加token信息"
    width="50vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <div style="display: block">
      <van-form @submit="onAddToken()">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="addName"
            name="Token用户名"
            label="Token用户名"
            placeholder="Token用户名"
            :rules="[{ required: true, message: '请填写Token用户名' }]"
          />
          <br />
          <van-field
            v-model="addUsername"
            name="OpenAi用户名"
            label="OpenAi用户名"
            placeholder="OpenAi用户名"
            :rules="[{ required: true, message: '请填写OpenAi用户名' }]"
          />
          <br />
          <van-field
            v-model="addUserPassword"
            type="password"
            name="OpenAi密码"
            label="OpenAi密码"
            placeholder="OpenAi密码"
            :rules="[{ required: true, message: '请填写OpenAi密码' }]"
          />
          <br />
          <van-field
            rows="3"
            type="textarea"
            maxlength="5000"
            show-word-limit
            v-model="addTokenValue"
            name="OpenAI的token"
            label="OpenAI的token"
            placeholder="选填(可不填,不填则使用账号密码)access token/session token/refresh token/share token"
          />
          <br />
          <div v-if="addPassword == ''">
            <van-field name="switch" label="是否分享出来">
              <template #right-icon>
                <van-switch active-color="#0ea27e" v-model="addShared" />
              </template>
            </van-field>
            <br />
            <div v-if="addShared == true">
              <van-field name="switch" label="是否分享聊天记录">
                <template #right-icon>
                  <van-switch
                    active-color="#0ea27e"
                    v-model="addShow_user_info"
                  />
                </template>
              </van-field>
              <br />
              <van-field name="switch" label="是否显示金光">
                <template #right-icon>
                  <van-switch active-color="#0ea27e" v-model="addPlus" />
                </template>
              </van-field>
            </div>
          </div>
          <div v-if="addShared == ''">
            <br />
            <van-field
              v-model="addPassword"
              type="temPassword"
              name="进入Token的密码"
              label="进入Token的密码"
              placeholder="填了将不会分享给他人！"
              :rules="[{ required: true, message: '如不分享token,则该项必填' }]"
            />
          </div>
          <br />
        </van-cell-group>
        <div style="margin: 5.2px">
          <van-button round block color="#0ea27e" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
    <br />
  </van-dialog>
  <!------------------------------------------------------------------------------------------------------>

  <!-- 查看token信息 主键 名称为show_2 -->
  <van-dialog
    v-model:show="show_2"
    title="token信息"
    width="50vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="showDialog"
  >
    <div style="display: block">
      <van-form>
        <van-cell-group inset>
          <br />
          <van-field
            v-model="temName"
            name="Token用户名"
            label="Token用户名"
            placeholder="Token用户名"
            :readonly="true"
            :rules="[{ required: true, message: '请填写Token用户名' }]"
          />
          <br />
          <van-field
            v-model="temUsername"
            name="OpenAi用户名"
            label="OpenAi用户名"
            placeholder="OpenAi用户名"
            :readonly="true"
            :rules="[{ required: true, message: '请填写OpenAi用户名' }]"
          />
          <br />
          <van-field
            v-model="temUserPassword"
            type="password"
            name="OpenAi密码"
            label="OpenAi密码"
            placeholder="OpenAi密码"
            :readonly="true"
            :rules="[{ required: true, message: '请填写OpenAi密码' }]"
          />
          <br />
          <van-field name="temShared" :readonly="true" label="是否分享出来">
            <template #right-icon>
              <van-switch disabled active-color="#0ea27e" v-model="temShared" />
            </template>
          </van-field>
          <br />
          <van-field
            :readonly="true"
            name="temShow_user_info"
            label="是否分享聊天记录"
          >
            <template #right-icon>
              <van-switch
                disabled
                active-color="#0ea27e"
                v-model="temShow_user_info"
              />
            </template>
          </van-field>
          <br />
          <van-field :readonly="true" name="temPlus" label="是否显示金光">
            <template #right-icon>
              <van-switch disabled active-color="#0ea27e" v-model="temPlus" />
            </template>
          </van-field>
          <br />
          <van-field
            v-model="temPassword"
            type="temPassword"
            name="进入Token的密码"
            label="进入Token的密码"
            placeholder="进入Token的密码"
            :readonly="true"
          />
          <br />
          <van-field
            v-model="temToken"
            rows="5"
            label="OpenAi的Token"
            type="textarea"
            maxlength="5000"
            placeholder="请填写OpenAi的Token"
            show-word-limit
            :readonly="true"
          />
          <br />
        </van-cell-group>
        <br />
      </van-form>
    </div>
  </van-dialog>
  <!------------------------------------------------------------------------------------------------------>
  <!-- 修改系统设置信息 主键 名称为show_3 -->
  <van-dialog
    v-model:show="show_3"
    title="修改系统设置信息"
    width="50vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="requireSettingDialog"
  >
    <div style="display: block">
      <van-form @submit="RequireSetting">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="server_mode"
            name="模式"
            label="模式"
            placeholder="web或proxy"
            :rules="[{ validator: customValidator }]"
          />
          <br />
          <van-field
            v-model="bing"
            name="绑定IP和端口"
            label="绑定IP和端口"
            placeholder="绑定IP和端口(选填)"
          />
          <br />
          <van-field
            v-model="timeout"
            name="请求超时时间"
            label="请求超时时间"
            placeholder="请求超时时间(选填)"
          />
          <br />
          <van-field name="switch" label="是否分享对话(选填)">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="public_share" />
            </template>
          </van-field>
          <br />
          <van-field
            v-model="proxy_url"
            type="temPassword"
            name="代理服务URL"
            label="代理服务URL"
            placeholder="代理服务URL(选填)"
          />
          <br />
          <van-field
            v-model="site_password"
            name="访问网站密码"
            label="访问网站密码"
            placeholder="访问网站密码(选填)"
          />
          <br />
          <van-field
            v-model="setup_password"
            name="重载服务密码密码"
            label="重载服务密码密码"
            placeholder="重载服务密码密码(选填)"
          />
          <br />
          <van-field
            v-model="pandoraNext_License"
            name="验证license"
            label="验证license"
            placeholder="验证license(复制github的命令)"
          />
          <br />
          <van-field
            v-model="loginUsername"
            name="tokensTool用户名"
            label="tokensTool用户名"
            placeholder="tokensTool用户名"
          />
          <br />
          <van-field
            v-model="loginPassword"
            name="tokensTool密码"
            label="tokensTool密码"
            placeholder="tokensTool密码"
          />
          <br />
          <van-field
            v-model="whitelist"
            name="白名单"
            label="白名单"
            placeholder="[]限制所有账号(默认为null)"
          />
          <br />
        </van-cell-group>
        <div style="margin: 5.2px">
          <van-button round block color="#0ea27e" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
    <br />
  </van-dialog>
  <!------------------------------------------------------------------------------------------------------>
</template>

<script lang="ts" setup>
// 导入类
import { Timer } from "@element-plus/icons-vue";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import png from "../asserts/chatGpt.jpg";
import { ElMessage, ElMessageBox } from "element-plus";
import { differenceInDays, parseISO } from "date-fns";
import { ElLoading } from "element-plus";

const loading = ref(true);
//加载状态

/**
 * router 切换页面
 */
const router = useRouter();
if (window.innerWidth <= 700) {
  router.replace("/iphone");
}
/**
 *   <!-- 添加token信息 主键 名称为show_1 -->
 *   <!-- 添加token信息 主键 名称为show_1 -->
 *   <!-- 查看token信息 主键 名称为show_2 -->
 *
 */
const show = ref(false);
const show_1 = ref(false);
const show_2 = ref(false);
const show_3 = ref(false);

//页头图片 image
const image = png;

/**
 * 定义User类接口
 */
interface User {
  name: string;
  token: string;
  username: string;
  userPassword: string;
  shared: boolean;
  show_user_info: boolean;
  plus: boolean;
  password: string;
  updateTime: string;
}

/**
 * 修改系统设置信息
 */
const server_mode = ref("web");
const bing = ref("");
const timeout = ref("");
const proxy_url = ref("");
const public_share = ref(false);
const site_password = ref("");
const setup_password = ref("");
const loginUsername = ref("");
const loginPassword = ref("");
const pandoraNext_License = ref("");
const whitelist = ref("");


// 自定义校验函数，直接返回错误提示
const customValidator = (value:string) => {
  if (['web', 'proxy'].includes(value)) {
    return true;
  } else {
    return `此项只能填web或proxy`; 
  }
};

/**
 * 查看或者修改token信息参数
 */
const temName = ref("");
const temToken = ref("");
const temUsername = ref("");
const temUserPassword = ref("");
const temShared = ref(false);
const temShow_user_info = ref(false);
const temPlus = ref(false);
const temPassword = ref();
const tableData = ref<User[]>([]);

/**
 * 添加用户信息参数
 */
const addName = ref("");
const addUsername = ref("");
const addUserPassword = ref("");
const addTokenValue = ref("");
const addShared = ref(false);
const addShow_user_info = ref(false);
const addPlus = ref(false);
const addPassword = ref("");

/**
 * 控制悬浮球位置
 * 单位%
 */

var y = window.innerHeight * 0.11;
var x = window.innerWidth * 0.842;

const iconSize = ref(window.innerHeight * 0.1);
console.log(window.innerHeight.toString());
const offset_task = ref({ x: x, y: y });

//搜索值
const value = ref<string>("");

/**
 * 用jwt令牌验证身份
 * 未通过者返回到/login
 */
const token = localStorage.getItem("jwtToken"); // 从localStorage获取JWT令牌
if (!token) {
  router.replace("/login");
}

const headers = {
  Authorization: `Bearer ${token}`,
};
/**
 * 用jwt令牌验证身份函数
 */
const fetchLoginToken = () => {
  axios
    .post("/api/loginToken?token=" + token)
    .then((response) => {
      if (response.data.code == 0) {
        console.error(response.data.data);
        router.replace("/login");
        return;
      }
      // 从解码后的令牌中获取特定的数据
      console.log(response.data.data);
      // 在这里处理登录令牌接口的响应
      // 如果需要执行一些特定的操作，可以在这里添加代码
    })
    .catch((error) => {
      console.error("请求loginToken接口失败", error);
      router.replace("/login");
    });
};

const onSearch = (value: string) => {
  fetchDataAndFillForm(value);
  loading.value = false;
};
/**
 * 获取数据操作，并把数据返回到tableData
 * 用于展示
 */
const fetchDataAndFillForm = async (value: string) => {
  try {
    const response = await axios.get(
      `/api/seleteToken?name=${value}`,
      {
        headers,
      }
    );
    const data = response.data.data;
    console.log(data);

    // 如果服务器返回的数据是一个数组，你可以遍历数据并将每个对象转化为User类型
    if (Array.isArray(data)) {
      const resUsers: User[] = data.map((item: User) => ({
        name: item.name,
        username: item.username,
        userPassword: item.userPassword,
        token: item.token,
        shared: item.shared,
        show_user_info: item.show_user_info,
        password: item.password,
        plus: item.plus,
        updateTime: item.updateTime,
      }));

      // 将用户数据添加到tableData
      tableData.value = resUsers;
    }
  } catch (error) {
    console.error("获取数据失败", error);
    ElMessage("获取数据失败");
  }
};

// 在组件加载完成后自动触发数据加载和填充
onMounted(() => {
  fetchLoginToken();
  onSearch(value.value);
});

/**
 * 用于用户信息设置
 */
const activeIndex = ref("-1");
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath);
};
const handleEdit = (index: number, row: User) => {
  console.log(index, row);
  temName.value = row.name;
  console.log(temName.value);
  temUsername.value = row.username;
  temUserPassword.value = row.userPassword;
  temToken.value = row.token;
  temShared.value = row.shared;
  temShow_user_info.value = row.show_user_info;
  temPlus.value = row.plus;
  temPassword.value = row.password;
  show.value = true;
};

/**
 * 添加token开启函数
 * 类user
 */
const addToken = () => {
  show_1.value = true;
};

/**
 * 添加token函数
 * 类user
 */
const onAddToken = () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const now: Date = new Date();
  const formattedTime = `${now.getFullYear()}-${(now.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${now.getDate().toString().padStart(2, "0")} ${now
    .getHours()
    .toString()
    .padStart(2, "0")}:${now.getMinutes().toString().padStart(2, "0")}:${now
    .getSeconds()
    .toString()
    .padStart(2, "0")}`;
  if (addPassword.value != "") {
    addShared.value = false;
    addShow_user_info.value = false;
    addPlus.value = false;
  }
  let api = {
    name: addName.value,
    token: addTokenValue.value,
    username: addUsername.value,
    userPassword: addUserPassword.value,
    shared: addShared.value,
    show_user_info: addShow_user_info.value,
    plus: addPlus.value,
    password: addPassword.value,
    updateTime: formattedTime,
  };
  fetch("/api/addToken", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(api),
  })
    .then((response) => response.json()) // 将 .json() 放在正确的位置
    .then((data) => {
      if (data.code == 1) {
        console.log(data.data);
        if (api.token == "") {
          api.token = data.data as string;
          ElMessage("添加成功！已为你自动装填token");
        }
        tableData.value.unshift(api);
        ElMessage(data.data);
      } else {
        ElMessage(data.msg);
      }
    });
  temName.value = "";
  temUsername.value = "";
  temUserPassword.value = "";
  temToken.value = "";
  show_1.value = false;
  loadingInstance.close();
};

/**
 * 展示token函数
 * 类user
 */
const showData = (row: User) => {
  temName.value = row.name;
  temUsername.value = row.username;
  temUserPassword.value = row.userPassword;
  temToken.value = row.token;
  temShared.value = row.shared;
  temShow_user_info.value = row.show_user_info;
  temPlus.value = row.plus;
  temPassword.value = row.password;
  show_2.value = true;
};

/**
 * 修改系统设置函数
 */
const onRequireSetting = async () => {
  const response = await axios.get(
    `/api/selectSetting`,
    {
      headers,
    }
  );
  const data = response.data.data;
  console.log(data);
  server_mode.value = data.server_mode;
  bing.value = data.bing;
  timeout.value = data.timeout;
  proxy_url.value = data.proxy_url;
  public_share.value = data.public_share;
  site_password.value = data.site_password;
  setup_password.value = data.setup_password;
  loginUsername.value = data.loginUsername;
  loginPassword.value = data.loginPassword;
  pandoraNext_License.value = data.pandoraNext_License;
  console.log(data.whitelist);
  if (data.whitelist == null) {
    whitelist.value = "null";
  } else whitelist.value = data.whitelist;
  show_3.value = true;
};

const RequireSetting = () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  if (whitelist.value == null || whitelist.value == "null") {
    whitelist.value = "";
  }
  const setting = {
    server_mode: server_mode.value,
    bing: bing.value,
    timeout: timeout.value,
    proxy_url: proxy_url.value,
    public_share: public_share.value,
    site_password: site_password.value,
    setup_password: setup_password.value,
    loginUsername: loginUsername.value,
    loginPassword: loginPassword.value,
    pandoraNext_License: pandoraNext_License.value,
    whitelist: whitelist.value,
  };

  fetch("/api/requireSetting", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(setting),
  })
    // 将 .json() 放在正确的位置
    .then((response) => response.json())
    .then((data) => {
      if (data.code == 1) {
        console.log(data.data);
        ElMessage(data.data);
      } else ElMessage(data.msg);
    })
    .catch((error) => {
      console.error("请求requireSetting接口失败", error);
      ElMessage("修改失败！");
    });
  show_3.value = false;
  loadingInstance.close();
};
/**
 * 修改token函数
 * 类user
 */
const RequireToken = () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const now: Date = new Date();
  const formattedTime = `${now.getFullYear()}-${(now.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${now.getDate().toString().padStart(2, "0")} ${now
    .getHours()
    .toString()
    .padStart(2, "0")}:${now.getMinutes().toString().padStart(2, "0")}:${now
    .getSeconds()
    .toString()
    .padStart(2, "0")}`;
  if (temPassword.value != "" && temShared.value === false) {
    temPlus.value = false;
    temShow_user_info.value = false;
  } else if (temShared.value === true || temPassword.value == "") {
    temPassword.value = "";
    temShared.value = true;
  }
  const api = {
    name: temName.value,
    token: temToken.value,
    username: temUsername.value,
    userPassword: temUserPassword.value,
    shared: temShared.value,
    show_user_info: temShow_user_info.value,
    plus: temPlus.value,
    password: temPassword.value,
  };
  fetch("/api/requiredToken", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(api),
  })
    // 将 .json() 放在正确的位置
    .then((response) => response.json())
    .then((data) => {
      if (data.code == 1) {
        console.log(data.data);
        ElMessage(data.data);
        console.log(tableData.value);
        for (let i = 0; i < tableData.value.length; i++) {
          console.log(tableData.value[i].name);
          if (tableData.value[i].name === temName.value) {
            tableData.value[i].token = api.token;
            tableData.value[i].username = api.username;
            tableData.value[i].userPassword = api.userPassword;
            tableData.value[i].shared = api.shared;
            tableData.value[i].show_user_info = api.show_user_info;
            tableData.value[i].plus = api.plus;
            tableData.value[i].password = api.password;
            tableData.value[i].updateTime = formattedTime;
            break; // 找到匹配的元素后跳出循环
          }
        }
      } else ElMessage(data.msg);
    })
    .catch((error) => {
      console.error("请求requireToken接口失败", error);
      ElMessage("修改失败！");
    });
  show.value = false;
  loadingInstance.close();
};

/**
 * 开启pandora函数
 */
const openPandora = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(`/api/open`, {
    headers,
  });
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    ElMessageBox.alert(data, "温馨提醒", {
      confirmButtonText: "OK",
      callback: () => {
        ElMessage({
          type: "info",
          message: "感谢Pandora大佬！",
        });
      },
    });
  } else {
    ElMessage(response.data.msg);
  }
  loadingInstance.close();
};

/**
 * 暂停pandora函数
 */
const closePandora = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(`/api/close`, {
    headers,
  });
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    ElMessageBox.alert(data, "温馨提醒", {
      confirmButtonText: "OK",
      callback: () => {
        ElMessage({
          type: "info",
          message: "感谢Pandora大佬！",
        });
      },
    });
  } else {
    ElMessage(response.data.msg);
  }
  loadingInstance.close();
};

/**
 * 重启pandora函数
 */
const AgainPandora = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(`/api/restart`, {
    headers,
  });
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    ElMessageBox.alert(data, "温馨提醒", {
      confirmButtonText: "OK",
      callback: () => {
        ElMessage({
          type: "info",
          message: "感谢Pandora大佬！",
        });
      },
    });
  } else {
    ElMessage(response.data.msg);
  }
  loadingInstance.close();
};

/**
 * 重载pandora函数
 */
const reloadPandora = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(`/api/reload`, {
    headers,
  });
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    ElMessageBox.alert(data, "温馨提醒", {
      confirmButtonText: "OK",
      callback: () => {
        ElMessage({
          type: "info",
          message: "感谢Pandora大佬！",
        });
      },
    });
  } else {
    ElMessage(response.data.msg);
  }
  loadingInstance.close();
};

/**
 * 验证pandora函数
 */
const verifyPandora = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(`/api/verify`, {
    headers,
  });
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    ElMessageBox.alert(data, "温馨提醒", {
      confirmButtonText: "OK",
      callback: () => {
        ElMessage({
          type: "info",
          message: "感谢Pandora大佬！",
        });
      },
    });
  } else {
    ElMessage(response.data.msg);
  }
  loadingInstance.close();
};

/**
 * 刷新Token函数
 */
const reNew = (row: User) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  console.log(row);
  console.log(row.token);
  const api = {
    name: row.name,
    token: row.token,
    username: row.username,
    userPassword: row.userPassword,
    shared: row.shared,
    show_user_info: row.show_user_info,
    plus: row.plus,
    password: row.password,
  };

  fetch("/api/updateToken", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`, // 确保 token 变量已定义
    },
    body: JSON.stringify(api),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      if (data != null && data != "") {
        if (data.data != null) {
          row.token = data.data;
          const now: Date = new Date();
          const formattedTime = `${now.getFullYear()}-${(now.getMonth() + 1)
            .toString()
            .padStart(2, "0")}-${now
            .getDate()
            .toString()
            .padStart(2, "0")} ${now
            .getHours()
            .toString()
            .padStart(2, "0")}:${now
            .getMinutes()
            .toString()
            .padStart(2, "0")}:${now.getSeconds().toString().padStart(2, "0")}`;

          row.updateTime = formattedTime;
          ElMessageBox.alert("更新成功!", "温馨提醒", {
            confirmButtonText: "OK",
            callback: () => {
              ElMessage({
                type: "info",
                message: "感谢Pandora大佬！",
              });
            },
          });
        } else {
          ElMessage(data.msg);
        }
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
  loadingInstance.close();
};

/**
 * 删除Token函数
 * 参数 user
 */
const deleteToken = (index: number, row: User) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  let msg = "";
  ElMessageBox.confirm(
    "您确定要删除这个Token吗，删除之后就找不到咯，请您要仔细认真考虑哦！",
    "温馨提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      axios
        .put(
          `/api/deleteToken?name=${row.name}`,
          null,
          {
            headers,
          }
        )
        .then((response) => {
          msg = "删除成功！";
          // 从数组中移除商品项
          console.log(response.data.data);
          tableData.value.splice(index, 1);
          ElMessage({
            type: "success",
            message: msg,
          });
        })
        .catch((error) => {
          // 处理完成失败的逻辑
          console.error("删除失败", error);
        });
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "删除取消！",
      });
    });
  loadingInstance.close();
};

/**
 * 获取token的过期时间
 */
const formatDate = (value: string) => {
  if (!value) return "";
  var nowDay = new Date();
  const timeDay = parseISO(value);
  const daysDiff = differenceInDays(nowDay, timeDay);
  return daysDiff >= 10
    ? "已经过去了至少10天"
    : Math.ceil(10 - daysDiff) + "天";
};
/**
 * 更改Token显示操作
 */
const dataToken = (value: string) => {
  return value.substring(0, 40) + "...";
};

/**
 * 退出登录操作
 */
const logout = () => {
  // 删除本地存储的 token
  localStorage.removeItem("jwtToken");
  // 使用 Vue Router 进行页面跳转到登录页面
  router.replace("/login");
};
</script>

<style>
.van-floating-bubble {
  width: 6vh;
  height: 6vh;
  background: #0ea27e;
}
.van-floating-bubble__icon {
  font-size: 50px;
}
.content {
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
.van-dialog {
  top: 50vh;
  height: auto;
}
.van-field__label {
  width: 120px;
  font-size: 14.6px;
}
.el-table .cell {
  font-size: 14px;
  line-height: 45px;
}
.el-tag {
  font-size: 12.6px;
}
.el-button--small {
  font-size: 12.6px;
}
.el-page-header__title {
  transform: translate(-42.7px, -7px);
  font-size: 27.3px;
  font-weight: 500;
}
.el-page-header__content {
  transform: translate(3vw, -3.5vh);
  font-size: 37px;
  color: var(--el-text-color-primary);
}

.el-avatar--circle {
  transform: translate(-14px, 2.8vh);
  border-radius: 50%;
}

/* 集合列字体大小 */
.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  font-size: 14px;
}

/* 集合内容字体大小 */
.el-menu-item {
  font-size: 12.6px;
}

.van-dialog__header {
  font-size: 14px;
}

.requireTokenDialog {
  height: 77.8vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.addTokenDialog {
  height: 64.5vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.showDialog {
  height: 77.8vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.requireSettingDialog {
  height: 82vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
/* 集合内框内字体颜色 */
.el-tag {
  --el-tag-text-color: #0ea27e;
  background-color: #f4fffd;
}

/* 集合内各数据位置 */
.el-table__inner-wrapper {
  margin-left: 50px;
  margin-bottom: 7px;
  overflow: auto;
}

/* 字体 a 超链接 h2 字体大小 */
a {
  color: #0ea27e;
}

h2 {
  font-size: 1.75vh;
  color: #606266;
  margin: 0px;
}

/* 表格上下间距 */
.el-table--large .el-table__cell {
  padding: 17.5px 0;
}

.el-menu--horizontal.el-menu {
  border-bottom: 1px solid #fff;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  border-radius: 10%;
}
/* 菜单左右方法 */
.flex-grow {
  flex-grow: 1;
}
/* 搜索框 */
.van-search {
  width: 100%;
  margin: 14px;
}

.search {
  box-shadow: 0px 0px 5px rgba(0, 0, 0, 0.2);
  transform: translate(14vw, 3.5vh);
  width: 72vw;
  background: white;
  border-radius: 10px;
}

.bottom-component {
  /* 样式设置，具体根据你的需求进行调整 */
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: auto;
  margin-top: 1.3vh;
}

.under {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
}

.el-table {
  width: 95%;
  max-width: 100%;
}
</style>