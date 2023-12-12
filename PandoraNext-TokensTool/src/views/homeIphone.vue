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
      <el-menu-item index="0">
        <img
          style="width: 30px"
          src="../asserts/openAi.jpg"
          @click="icoImage"
        />
      </el-menu-item>
      <div class="flex-grow" />
      <el-sub-menu index="1">
        <template #title>系统设置</template>
        <el-menu-item index="1-1" @click="onRequireSetting(pandoraNext)"
          >PandoraNext设置</el-menu-item
        >
        <el-menu-item index="1-2" @click="onRequireSetting(tokensTool)"
          >tokensTool设置</el-menu-item
        >
        <el-menu-item index="1-3" @click="onRequireSetting(validation)"
          >验证码信息设置</el-menu-item
        >
      </el-sub-menu>
      <el-sub-menu index="2">
        <template #title>系统功能</template>
        <el-menu-item index="2-1" @click="openPandora"
          >开启{{ containerName }}</el-menu-item
        >
        <el-menu-item index="2-2" @click="closePandora"
          >暂停{{ containerName }}</el-menu-item
        >
        <el-menu-item index="2-3" @click="AgainPandora"
          >重启{{ containerName }}</el-menu-item
        >
        <el-menu-item index="2-4" @click="reloadPandora"
          >重载{{ containerName }}</el-menu-item
        >
        <el-menu-item index="2-5" @click="getPoolToken"
          >PoolToken列表</el-menu-item
        >
        <el-menu-item index="2-6" @click="updateAllShareToken"
          >全部生成share_token</el-menu-item
        >
        <el-menu-item index="2-7" @click="logout">退出登录</el-menu-item>
      </el-sub-menu>
    </el-menu>
    <div style="display: block; transform: translate(5vw, 2.5vh); width=95vw;">
      <el-page-header :icon="null" title=" ">
        <template #content>
          <div class="flex items-center">
            <el-avatar size="52.6px" class="mr-3" :src="image" />
            <span class="text-large font-600 mr-3">PandoraNext</span>
            <span
              class="text-sm mr-2"
              style="color: var(--el-text-color-regular)"
            >
              TokensTool
            </span>
            <el-tag>v0.4.8.1</el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 添加token按钮 -->
    <van-floating-bubble
      v-model:offset="offset_task"
      axis="xy"
      icon="add-o"
      class="addBubble"
      @click="addToken"
    />

    <div class="under">
      <div class="search">
        <div
          style="
          display: flex;
          background= white;
          margin: 0px 0px -17px 0px;
        "
        >
          <van-search
            v-model="value"
            shape="round"
            placeholder="请输入搜索token名称关键词"
            @search="onSearch"
          />
        </div>
        <div
          style="
            display: flex;
            width: 83vw;
            height: 62vh;
            transform: translateX(0vw);
          "
        >
          <!-- 数据表 -->
          <el-table
            v-loading="loading"
            :data="tableData"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            ref="multipleTableRef"
            class="tokenTable"
          >
            <el-table-column type="selection" width="35" />
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
            <el-table-column label="Token值" width="270">
              <template #default="scope">
                <el-popover
                  effect="light"
                  trigger="hover"
                  placement="top"
                  width="auto"
                >
                  <template #default>
                    <div>token: {{ dataToken(scope.row.token) }}</div>
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
                    <el-tag>距离过期还有：{{ formatDate(scope.row) }}</el-tag>
                  </template>
                </el-popover>
              </template>
            </el-table-column>

            <!-- 操作方法表 宽300 方法handleEdit-->
            <el-table-column label="操作方法" width="335">
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

                <!-- 刷新操作按钮 方法reNew-->
                <el-button
                  size="small"
                  type="warning"
                  @click="review(scope.row)"
                  >生成</el-button
                >
              </template>
            </el-table-column>
          </el-table>
          <div style="display: flex; margin-top: 3vh"></div>
        </div>
        <div style="margin: 10px; transform: translateX(10px)">
          <el-button @click="toggleSelection()"><h1>全部取消</h1></el-button>
          <el-button class="my-button" @click="getSelectedData">
            <h1>选中合成PoolToken</h1>
          </el-button>
        </div>
      </div>
    </div>
    <br />
  </div>
  <!------------------------------------------------------------------------------------------------------>
  <!-- 修改token信息 主键 名称为show -->
  <van-dialog
    v-model:show="show"
    title="修改token信息"
    width="90vw"
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
          <br />
          <van-field name="switch" label="是否合成poolToken">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="setPoolToken" />
            </template>
          </van-field>
          <div v-if="temShared == false">
            <br />
            <van-field
              v-model="temPassword"
              type="password"
              name="进入Token的密码"
              label="进入Token的密码"
              placeholder="填了将不会分享给他人！"
            />
          </div>
          <br />
          <van-field
            v-model="temToken"
            rows="5"
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
    width="90vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="addTokenDialog"
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
          <br />
          <van-field name="switch" label="是否合成poolToken">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="addSetPoolToken" />
            </template>
          </van-field>
          <div v-if="addShared == ''">
            <br />
            <van-field
              v-model="addPassword"
              type="password"
              name="进入Token的密码"
              label="进入Token的密码"
              placeholder="填了将不会分享给他人！"
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
    width="90vw"
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
          />
          <br />
          <van-field
            v-model="temUsername"
            name="OpenAi用户名"
            label="OpenAi用户名"
            placeholder="OpenAi用户名"
          />
          <br />
          <van-field
            v-model="temUserPassword"
            type="password"
            name="OpenAi密码"
            label="OpenAi密码"
            placeholder="OpenAi密码"
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
          <van-field name="switch" label="是否合成poolToken">
            <template #right-icon>
              <van-switch
                disabled
                active-color="#0ea27e"
                v-model="setPoolToken"
              />
            </template>
          </van-field>
          <br />
          <van-field
            v-model="temPassword"
            name="进入Token的密码"
            label="进入Token的密码"
            placeholder="进入Token的密码"
          />
          <br />
          <van-field
            v-model="temToken"
            rows="5"
            label="session_token"
            type="textarea"
            maxlength="5000"
            placeholder="请填写OpenAi的Token"
            show-word-limit
          />
          <br />
          <van-field
            v-model="temAccessToken"
            rows="4"
            label="access_token"
            type="textarea"
            maxlength="5000"
            placeholder="请填写OpenAi的access_token"
            show-word-limit
          />
          <br />
          <van-field
            v-model="temShareToken"
            rows="3"
            label="share_token"
            type="textarea"
            maxlength="200"
            placeholder="请填写OpenAi的share_token"
            show-word-limit
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
    title="PandoraNext设置信息"
    width="90vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="requireSettingDialog"
  >
    <div style="display: block">
      <van-form @submit="RequireSetting(pandoraNext)">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="isolated_conv_title"
            name="对话标题"
            label="对话标题"
            placeholder="隔离对话设置标题"
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
          <van-field name="switch" label="是否配置证书">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="enabled" />
            </template>
          </van-field>
          <div v-if="enabled == true">
            <br />
            <van-field
              v-model="cert_file"
              name="证书文件"
              label="证书文件"
              placeholder="证书文件"
            />
            <br />
            <van-field
              v-model="key_file"
              name="密钥文件"
              label="密钥文件"
              placeholder="密钥文件"
            />
          </div>
          <br />
          <van-field
            v-model="proxy_url"
            name="代理服务URL"
            label="代理服务URL"
            placeholder="代理服务URL(选填)"
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

  <!-- 修改tokensTool系统设置信息 主键 名称为show_4 -->
  <van-dialog
    v-model:show="show_4"
    title="tokensTool设置信息"
    width="90vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="requireSettingDialog"
  >
    <div style="display: block">
      <van-form @submit="RequireSetting(tokensTool)">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="loginUsername"
            name="登录用户名"
            label="登录用户名"
            placeholder="tokensTool用户名"
          />
          <br />
          <van-field
            v-model="loginPassword"
            name="登录密码"
            label="登录密码"
            placeholder="不少于8位，且同时包含数字和字母"
            :rules="[{ validator: customValidator }]"
          />
          <br />
          <van-field
            v-model="proxy_api_prefix"
            name="proxy接口前缀"
            label="proxy接口前缀"
            placeholder="proxy模式接口后缀，不少于8位，且同时包含数字和字母"
            :rules="[{ validator: customValidator }]"
          />
          <br />
          <van-field
            v-model="autoToken_url"
            name="proxy模式URL"
            label="proxy模式URL"
            placeholder="http(s)://(ip:port或者域名)/后缀，同公网服务器填default"
          />
          <br />
          <van-field
            v-model="setup_password"
            name="重载服务密码"
            label="重载服务密码"
            placeholder="PandoraNext重载服务密码，不少于8位，且同时包含数字和字母"
            :rules="[{ validator: customValidator }]"
          />
          <br />
          <van-field
            v-model="site_password"
            name="访问密码"
            label="访问密码"
            placeholder="PandoraNext访问密码，建议开启访问密码"
            :rules="[{ validator: sitePasswordValidator }]"
          />
          <br />
          <van-field
            v-model="license_id"
            name="验证licenseId"
            label="验证licenseId"
            placeholder="验证licenseId(github上拿到的license_id)"
          />
          <!-- 0.4.8.1 -->
          <br />
          <van-field name="switch" label="tokensTool接口">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="isGetToken" />
            </template>
          </van-field>
          <div v-if="isGetToken == true">
            <br />
            <van-field
              v-model="getTokenPassword"
              name="接口密码"
              label="接口密码"
              placeholder="tokensTool接口密码，用于获取tokens,不少于8位，且同时包含数字和字母"
              :rules="[{ validator: customValidator }]"
            />
          </div>
          <br />
          <van-field
            v-model="containerName"
            name="监管容器名"
            label="监管容器名"
            placeholder="监管运行的容器名或文件名，默认为PandoraNext"
            :rules="[{ required: true, message: '请填写监管的容器名' }]"
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

  <!------------------------------------------------------------------------------------------------------>

  <!-- 修改tokensTool系统设置信息 主键 名称为show_5 -->
  <van-dialog
    v-model:show="show_5"
    title="PandoraNext验证信息"
    width="90vw"
    :close-on-click-overlay="true"
    :show-cancel-button="false"
    :show-confirm-button="false"
    class="requireSettingDialog"
  >
    <div style="display: block">
      <van-form @submit="RequireSetting(validation)">
        <van-cell-group inset>
          <br />
          <van-field
            v-model="provider"
            name="验证码提供商"
            label="验证码提供商"
            placeholder="验证码提供商"
          />
          <br />
          <van-field
            v-model="site_key"
            name="验证码网站参数"
            label="验证码网站参数"
            placeholder="验证码网站参数"
          />
          <br />
          <van-field
            v-model="site_secret"
            name="验证码API Key"
            label="验证码API Key"
            placeholder="验证码API Key"
          />
          <br />
          <van-field name="switch" label="是否全站密码登录页面显示">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="site_login" />
            </template>
          </van-field>
          <br />
          <van-field name="switch" label="是否在设置登录页面显示">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="setup_login" />
            </template>
          </van-field>
          <br />
          <van-field name="switch" label="是否在输入用户名页面显示">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="oai_username" />
            </template>
          </van-field>
          <br />
          <van-field name="switch" label="是否在输入密码页面显示">
            <template #right-icon>
              <van-switch active-color="#0ea27e" v-model="oai_password" />
            </template>
          </van-field>
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

  <!-- poolToken信息 主键 名称为show_6 -->
  <el-dialog
    v-model="show_6"
    title="pool_token列表"
    align-center
    width="90%"
    :close-on-click-modal="false"
    class="poolTokenDialog"
  >
    <el-table :data="poolData">
      <el-table-column property="poolName" label="pool_token名称" width="200" />
      <el-table-column property="poolToken" label="pool_token值" width="445" />
      <el-table-column
        property="shareTokens"
        label="使用token名集合"
        width="375"
      />
      <el-table-column property="poolTime" label="更新时间" width="230" />
      <el-table-column label="操作方法" width="255">
        <!-- 编辑操作按钮 -->
        <template #default="scope">
          <el-button
            size="small"
            type="danger"
            @click="deletePoolToken(scope.$index, scope.row)"
            >删除</el-button
          >
          <el-button size="small" type="success" @click="reNewPool(scope.row)"
            >刷新</el-button
          >
          <el-button size="small" type="primary" @click="reviewPool(scope.row)"
            >更换</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
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
import { ElTable } from "element-plus";

const multipleTableRef = ref<InstanceType<typeof ElTable>>();
const multipleSelection = ref<User[]>([]);
const toggleSelection = (rows?: User[]) => {
  multipleTableRef.value!.clearSelection();
};
const handleSelectionChange = (val: User[]) => {
  multipleSelection.value = val;
};
const getSelectedData = async () => {
  const selectedData = multipleSelection.value;

  if (selectedData.length === 0) {
    ElMessage("未选择数据");
    return;
  }

  try {
    const { value } = await ElMessageBox.prompt(
      "请为这个pool_token取一个名字:",
      "🥰温馨提示",
      {
        confirmButtonText: "生成pool_token",
        cancelButtonText: "取消",
        inputPattern: /^[\u4e00-\u9fa5a-zA-Z0-9]{5,10}$/,
        inputErrorMessage:
          "此项不少于5个字符且不超过10个字符，可以包括汉字、字母和数字",
      }
    );

    const names = selectedData.map((userData) => userData.name);

    const addPoolToken = {
      poolName: value,
      shareTokens: names,
    };

    const response = await fetch(
      "/api/addPoolToken",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(addPoolToken),
      }
    );
    const loadingInstance = ElLoading.service({ fullscreen: true });

    const data = await response.json();

    if (data.code === 1) {
      onSearch("");
      ElMessage({
        type: "success",
        message: `生成pool_token成功，快去pool_token列表查看吧！`,
      });
    } else {
      ElMessage({
        type: "info",
        message: "生成pool_token失败，请确保配置是否正确！",
      });
    }

    loadingInstance.close();
  } catch (error) {
    ElMessage({
      type: "info",
      message: "取消生成pool_token",
    });
  }
};

//pandoraNext 为 0
const pandoraNext = 0;
//tokensTool 为 0
const tokensTool = 1;
//验证信息
const validation = 2;

/**
 * router 切换页面
 */
const router = useRouter();

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
const show_4 = ref(false);
const show_5 = ref(false);
const show_6 = ref(false);

//页头图片 image
const image = png;

/**
 * 定义User类接口
 */
interface User {
  name: string;
  token: string;
  access_token: string;
  share_token: string;
  username: string;
  userPassword: string;
  shared: boolean;
  show_user_info: boolean;
  plus: boolean;
  setPoolToken: boolean;
  password: string;
  updateTime: string;
}

/**
 * 定义Pool类接口
 */
interface Pool {
  poolName: string;
  shareTokens: string;
  poolToken: string;
  poolTime: string;
}

/**
 * 修改系统设置信息
 */
const proxy_api_prefix = ref("");
const isolated_conv_title = ref("*");
const bing = ref("");
const timeout = ref("");
const proxy_url = ref("");
const public_share = ref(false);

const enabled = ref(false);
const cert_file = ref("");
const key_file = ref("");

const site_password = ref("");
const setup_password = ref("");
const loginUsername = ref("");
const loginPassword = ref("");
const license_id = ref("");

//0.4.8.1
const isGetToken = ref(false);
const getTokenPassword = ref("");

const containerName = ref("PandoraNext");
const autoToken_url = ref("default");
const whitelist = ref("");

const provider = ref("");
const site_key = ref("");
const site_secret = ref("");
const site_login = ref(false);
const setup_login = ref(false);
const oai_username = ref(false);
const oai_password = ref(false);

// 0.4.8
const poolName = ref("");
const shareTokens = ref("");
const poolToken = ref("");
const poolTime = ref("");

// 自定义校验函数，直接返回错误提示
const customValidator = (value: string) => {
  // 至少8位，包含数字和字母
  const regex = /^(?=.*\d)(?=.*[a-zA-Z]).{8,}$/;

  if (regex.test(value)) {
    return true;
  } else {
    return "此项至少要包含8位且必须包含数字和字母";
  }
};

const sitePasswordValidator = (value: string) => {
  // 至少8位，包含数字和字母
  const regex = /^(?=.*\d)(?=.*[a-zA-Z]).{8,}$/;

  if (regex.test(value) || value == "") {
    return true;
  } else {
    return "此项至少要包含8位且必须包含数字和字母";
  }
};
/**
 * 查看或者修改token信息参数
 */
const temName = ref("");
const temToken = ref("");
const temAccessToken = ref("");
const temShareToken = ref("");
const temUsername = ref("");
const temUserPassword = ref("");
const temShared = ref(false);
const temShow_user_info = ref(false);
const temPlus = ref(false);
const temPassword = ref("");
const setPoolToken = ref(false);
const tableData = ref<User[]>([]);
// 0.4.8
const poolData = ref<Pool[]>([]);

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
const addSetPoolToken = ref(false);
const addPassword = ref("");

/**
 * 控制悬浮球位置
 * 单位%
 */

var y = 74;
var x = window.innerWidth * 0.852;

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
};
/**
 * 获取数据操作，并把数据返回到tableData
 * 用于展示
 */
const fetchDataAndFillForm = async (value: string) => {
  try {
    const response = await axios.get(
      `/api/selectToken?name=${value}`,
      {
        headers,
      }
    );
    const data_token = response.data.data;
    console.log(data_token);

    // 如果服务器返回的数据是一个数组，你可以遍历数据并将每个对象转化为User类型
    if (Array.isArray(data_token)) {
      const resUsers: User[] = data_token.map((item: User) => ({
        name: item.name,
        username: item.username,
        userPassword: item.userPassword,
        token: item.token,
        access_token: item.access_token,
        share_token: item.share_token,
        shared: item.shared,
        show_user_info: item.show_user_info,
        password: item.password,
        plus: item.plus,
        setPoolToken: item.setPoolToken,
        updateTime: item.updateTime,
      }));

      const responsePool = await axios.get(
        `/api/selectPoolToken?name=${value}`,
        {
          headers,
        }
      );
      const pool_token = responsePool.data.data;

      // 如果服务器返回的数据是一个数组，你可以遍历数据并将每个对象转化为User类型
      if (Array.isArray(pool_token)) {
        const resPools: Pool[] = pool_token.map((item: Pool) => ({
          poolName: item.poolName,
          poolTime: item.poolTime,
          poolToken: item.poolToken,
          shareTokens: item.shareTokens,
        }));

        // 将用户数据添加到tableData
        tableData.value = resUsers;
        // 将用户数据添加到poolData
        poolData.value = resPools;

        const response = await axios.get(
          `/api/selectSetting`,
          {
            headers,
          }
        );
        const data = response.data.data;
        console.log(data);
        proxy_api_prefix.value = data.proxy_api_prefix;
        isolated_conv_title.value = data.isolated_conv_title;
        bing.value = data.bing;
        timeout.value = data.timeout;
        proxy_url.value = data.proxy_url;
        public_share.value = data.public_share;

        enabled.value = data.tls.enabled;
        cert_file.value = data.tls.cert_file;
        key_file.value = data.tls.key_file;

        site_password.value = data.site_password;
        setup_password.value = data.setup_password;
        console.log(data.whitelist);
        if (data.whitelist == null) {
          whitelist.value = "null";
        } else whitelist.value = data.whitelist;
        loginUsername.value = data.loginUsername;
        loginPassword.value = data.loginPassword;
        license_id.value = data.license_id;

        //0.4.8.1
        isGetToken.value = data.isGetToken;
        getTokenPassword.value = data.getTokenPassword;

        containerName.value = data.containerName;
        autoToken_url.value = data.autoToken_url;
        provider.value = data.validation.provider;
        site_key.value = data.validation.site_key;
        site_secret.value = data.validation.site_secret;

        site_login.value = data.validation.site_login;
        setup_login.value = data.validation.setup_login;
        oai_username.value = data.validation.oai_username;
        oai_password.value = data.validation.oai_password;
      }
    }
  } catch (error) {
    console.error("获取数据失败", error);
    ElMessage("获取数据失败");
  }
  if (loginPassword.value == "123456" && loginUsername.value == "root") {
    ElMessageBox.alert(
      "请先修改默认的初始账号和密码，并填写相应的信息，具体可参考网站文档！",
      "温馨提醒",
      {
        confirmButtonText: "OK",
        callback: () => {
          ElMessage({
            type: "info",
            message: "感谢Pandora大佬！",
          });
        },
      }
    );
    loginPassword.value = "";
    loginUsername.value = "";
    show_4.value = true;
  }
};

// 在组件加载完成后自动触发数据加载和填充
onMounted(() => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  if (window.innerWidth <= 700) {
    router.replace("/iphone");
  }
  fetchLoginToken();
  onSearch(value.value);
  loadingInstance.close();
});

/**
 * 用于用户信息设置
 */
const activeIndex = ref("-1");

let temRequireToken = "";
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath);
};
const handleEdit = (index: number, row: User) => {
  console.log(index, row);
  temName.value = row.name;
  console.log(temName.value);
  temUsername.value = row.username;
  temUserPassword.value = row.userPassword;
  //用来判断token是否更改
  temToken.value = row.token;
  temRequireToken = row.token;
  temShared.value = row.shared;
  temShow_user_info.value = row.show_user_info;
  temPlus.value = row.plus;
  temPassword.value = row.password;
  setPoolToken.value = row.setPoolToken;
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
    setPoolToken: addSetPoolToken.value,
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
          onSearch("");
          addName.value = "";
          addTokenValue.value = "";
          addUsername.value = "";
          addUserPassword.value = "";
          addShared.value = false;
          addShow_user_info.value = false;
          addPlus.value = false;
          addSetPoolToken.value = false;
          addPassword.value = "";
          ElMessage("添加成功！已为你自动装填token");
        }
      } else {
        ElMessage(data.msg);
      }
      loadingInstance.close();
    })
    .catch((error) => {
      ElMessage("获取账号出现问题，请检查刷新网址是否正确！");
      loadingInstance.close();
    });
  show_1.value = false;
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
  temAccessToken.value = row.access_token;
  temShareToken.value = row.share_token;
  temShared.value = row.shared;
  temShow_user_info.value = row.show_user_info;
  temPlus.value = row.plus;
  temPassword.value = row.password;
  setPoolToken.value = row.setPoolToken;
  show_2.value = true;
};

/**
 * 修改系统设置函数
 */
const onRequireSetting = async (value: any) => {
  if (value == 0) {
    show_3.value = true;
  } else if (value == 1) {
    show_4.value = true;
  } else if (value == 2) {
    show_5.value = true;
  }
};

const RequireSetting = (value: any) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  if (whitelist.value == null || whitelist.value == "null") {
    whitelist.value = "";
  }
  if (enabled.value == false) {
    cert_file.value = "";
    key_file.value = "";
  }
  const tls = {
    enabled: enabled.value,
    cert_file: cert_file.value,
    key_file: key_file.value,
  };
  const validation = {
    provider: provider.value,
    site_key: site_key.value,
    site_secret: site_secret.value,
    site_login: site_login.value,
    setup_login: setup_login.value,
    oai_username: oai_username.value,
    oai_password: oai_password.value,
  };
  const setting = {
    proxy_api_prefix: proxy_api_prefix.value,
    isolated_conv_title: isolated_conv_title.value,
    bing: bing.value,
    timeout: timeout.value,
    proxy_url: proxy_url.value,
    public_share: public_share.value,
    site_password: site_password.value,
    setup_password: setup_password.value,
    loginUsername: loginUsername.value,
    loginPassword: loginPassword.value,
    license_id: license_id.value,

    //0.4.8.1
    isGetToken: isGetToken.value,
    getTokenPassword: getTokenPassword.value,

    containerName: containerName.value,
    autoToken_url: autoToken_url.value,
    whitelist: whitelist.value,
    validation: validation,
    tls: tls,
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
      } else {
        ElMessage(data.msg);
      }
      fetchLoginToken();
      loadingInstance.close();
    })
    .catch((error) => {
      loadingInstance.close();
      console.error("请求requireSetting接口失败", error);
      ElMessage("修改失败！");
    });
  console.log(value);
  if (value == 0) {
    show_3.value = false;
  } else if (value == 1) {
    show_4.value = false;
  } else if (value == 2) {
    show_5.value = false;
  }
};
/**
 * 修改token函数
 * 类user
 */
const RequireToken = () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  let formattedTime = "";
  if (temRequireToken != temToken.value) {
    const now: Date = new Date();
    formattedTime = `${now.getFullYear()}-${(now.getMonth() + 1)
      .toString()
      .padStart(2, "0")}-${now.getDate().toString().padStart(2, "0")} ${now
      .getHours()
      .toString()
      .padStart(2, "0")}:${now.getMinutes().toString().padStart(2, "0")}:${now
      .getSeconds()
      .toString()
      .padStart(2, "0")}`;
  }
  if (temShared.value === true) {
    temPassword.value = "";
  }
  const api = {
    name: temName.value,
    token: temToken.value,
    username: temUsername.value,
    userPassword: temUserPassword.value,
    shared: temShared.value,
    show_user_info: temShow_user_info.value,
    plus: temPlus.value,
    setPoolToken: setPoolToken.value,
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
            tableData.value[i].setPoolToken = api.setPoolToken;
            tableData.value[i].password = api.password;
            if (temRequireToken != temToken.value) {
              tableData.value[i].updateTime = formattedTime;
            }
            break; // 找到匹配的元素后跳出循环
          }
        }
      } else {
        ElMessage(data.msg);
      }
      loadingInstance.close();
    })
    .catch((error) => {
      loadingInstance.close();
      console.error("请求requireToken接口失败", error);
      ElMessage("修改失败！");
    });
  show.value = false;
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
 * 一键全生成
 */
const updateAllShareToken = async () => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  const response = await axios.get(
    `/api/updateAllToken`,
    {
      headers,
    }
  );
  const data = response.data.data;
  console.log(data);
  if (data != null && data != "") {
    onSearch("");
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

  fetch("/api/updateSessionToken", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      // 确保 token 变量已定义
      Authorization: `Bearer ${token}`,
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
          onSearch("");
          ElMessageBox.alert("刷新成功!", "温馨提醒", {
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
      loadingInstance.close();
    })
    .catch((error) => {
      loadingInstance.close();
      console.error("Error:", error);
    });
};

/**
 * 刷新Token函数
 */
const reNewPool = (row: Pool) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  fetch("/api/refreshSimplyPoolToken", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      // 确保 token 变量已定义
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(row),
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
          onSearch("");
          ElMessageBox.alert("刷新成功!", "温馨提醒", {
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
      loadingInstance.close();
    })
    .catch((error) => {
      loadingInstance.close();
      console.error("Error:", error);
    });
};

/**
 * 更改token操作
 */
const review = (row: User) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
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
      // 确保 token 变量已定义
      Authorization: `Bearer ${token}`,
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
          row.share_token = data.data.share_token;
          row.access_token = data.data.access_token;
          onSearch("");
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
      loadingInstance.close();
    })
    .catch((error) => {
      loadingInstance.close();
      console.error("Error:", error);
    });
};

/**
 * 更新token操作
 */
const reviewPool = (row: Pool) => {
  ElMessageBox.confirm(
    "您确定要删除这个Pool_Token吗，删除之后就找不到咯，请您要仔细认真考虑哦！",
    "温馨提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      fetch("/api/changePoolToken", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          // 确保 token 变量已定义
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(row),
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
              onSearch("");
              ElMessageBox.alert("更换pool_token成功!", "温馨提醒", {
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
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "更换pool_token取消！",
      });
    });
};

/**
 * 删除PoolToken函数
 * 参数 Pool
 */
const deletePoolToken = (index: number, row: Pool) => {
  const loadingInstance = ElLoading.service({ fullscreen: true });
  let msg = "";
  ElMessageBox.confirm(
    "您确定要删除这个Pool_Token吗，删除之后就找不到咯，请您要仔细认真考虑哦！",
    "温馨提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      fetch("/api/deletePoolToken", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          // 确保 token 变量已定义
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(row),
      })
        .then((response) => {
          msg = "删除成功！";
          // 从数组中移除商品项
          poolData.value.splice(index, 1);
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
const formatDate = (value: User) => {
  if (!value) return "";
  var nowDay = new Date();
  const timeDay = parseISO(value.updateTime);
  const daysDiff = differenceInDays(nowDay, timeDay);
  if (value.token.length <= 2000) {
    return daysDiff >= 10
      ? "已经过去了至少10天"
      : Math.ceil(10 - daysDiff) + "天";
  } else {
    return daysDiff >= 80
      ? "已经过去了至少80天"
      : Math.ceil(80 - daysDiff) + "天";
  }
};

/**
 * 更改Token显示操作
 */
const dataToken = (value: string) => {
  return value.substring(0, 30) + "...";
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

const getPoolToken = () => {
  show_6.value = true;
};
</script>

<style>
.van-floating-bubble {
  position: fixed;
  width: 40px;
  height: 40px;
  background: #0ea27e;
}
.van-floating-bubble__icon {
  font-size: 30px;
  position: fixed;
}
.addBubble {
  position: fixed;
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
  width: 84px;
  font-size: 13px;
}
.el-table .cell {
  font-size: 14px;
  line-height: 40px;
}
.el-tag {
  font-size: 12.6px;
}
.el-button--small {
  font-size: 12.6px;
}
.el-page-header__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  line-height: 24px;
}
.el-page-header__title {
  transform: translate(-42.7px, -7px);
  font-size: 27.3px;
  font-weight: 500;
}
.el-page-header__content {
  transform: translate(-4vw, -2.4vh);
  font-size: 15px;
  color: var(--el-text-color-primary);
}
.el-avatar--circle {
  transform: translate(-14px, 1.8vh);
  border-radius: 50%;
}
/* 集合列字体大小 */
.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  font-size: 14px;
}

/* 集合内容字体大小 */
.el-menu-item {
  font-size: 12.6px;
  width: auto;
}

.van-dialog__header {
  font-size: 14px;
}

.requireDialog {
  height: 60vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}

.requireTokenDialog {
  height: 60vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.addTokenDialog {
  height: 60vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.showDialog {
  height: 60vh;
  overflow-y: auto;
  /* 显示垂直滚动条 */
  overflow-x: hidden;
}
.requireSettingDialog {
  height: 65vh;
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
  margin-left: 6.15vw;
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
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  transform: translate(7.5vw, 1.75vh);
  width: 85vw;
  background: #fff;
  border-radius: 10px;
  height: auto;
  margin-top: 5px;
  margin-bottom: 10px;
}

.bottom-component {
  flex: 1;
  height: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.under {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
}

.el-scrollbar {
  height: 100%;
}
.el-table {
  width: 95%;
  max-width: 95%;
  max-height: 66vh;
  overflow-y: auto;
}

.el-menu--horizontal.el-menu {
  border-bottom: 1px solid #fff;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  border-radius: 0%;
}

.el-message--info .el-message__content {
  color: var(--el-message-text-color);
  overflow-wrap: anywhere;
  width: 41vw;
}

.el-menu--popup {
  z-index: 100;
  min-width: auto;
  border: none;
  padding: 5px 0;
  border-radius: var(--el-border-radius-small);
  box-shadow: var(--el-box-shadow-light);
}

.my-button {
  margin-right: 4.3vw;
  float: right;
}

h1 {
  color: #0ea27e;
  font-size: 14px;
}

.poolTokenDialog {
  max-height: 75.3vh;
  overflow: auto;
}

.el-message-box__btns button:nth-child(2) {
  --el-button-bg-color: #0ea27e;
  --el-button-border-color: #0ea27e;
  --el-button-outline-color: #0ea27e;
  --el-button-active-color: #0ea27e;
  --el-button-hover-bg-color: #0ea27e;
  --el-button-hover-border-color: #0ea27e;
  --el-button-active-bg-color: #0ea27e;
  --el-button-active-border-color: #0ea27e;
  --el-button-disabled-text-color: #0ea27e;
  --el-button-disabled-bg-color: #0ea27e;
  --el-button-disabled-border-color: var(--el-color-primary-light-5);
  margin-left: 10px;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #0ea27e inset;
}

.tokenTable {
  height: 100%;
}

/* 选择框 */
.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #0ea27e;
  border-color: #0ea27e;
}
.el-checkbox__input.is-indeterminate .el-checkbox__inner {
  background-color: #0ea27e;
  border-color: #0ea27e;
}

.el-checkbox__inner:hover {
  border-color: #0ea27e;
}

</style>