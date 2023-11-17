import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
    { path: '/login', component: () => import('@/views/loginIndex.vue'), meta: { title: 'Welcome to PandoraNext-TokensTool',keepAlive: true }},
    { path: '/', component: () => import('@/views/homeIndex.vue'), meta: { title: 'PandoraNext-TokensTool',keepAlive: true }},
    { path: '/iphone', component: () => import('@/views/homeIphone.vue'), meta: { title: 'PandoraNext-TokensTool',keepAlive: true }},
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    // 检查是否存在to.meta.title，如果存在则设置页面标题为to.meta.title，否则使用默认标题
    if (to.meta.title) {
        document.title = `${to.meta.title}`; // 使用字符串模板将其转换为字符串
    } else {
        document.title = 'PandoraNext-TokensTool';
    }
    next();
});

export default router;
