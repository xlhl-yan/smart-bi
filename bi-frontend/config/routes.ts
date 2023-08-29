export default [
  {
    path: '/user',
    layout: false,
    routes: [
      { name: '登录', path: '/user/login', component: './User/Login' },
      { name: '注册', path: '/user/register', component: './User/Register' },
    ],
  },
  { path: '/welcome', name: '欢迎', icon: 'smile', component: './Welcome' },
  {
    path: '/',
    redirect: '/add/chart',
  },
  {
    path: '/add/chart',
    name: '分析',
    icon:'AreaChart',
    component: './User/AddChart',
  },
  {
    path: '/add/chart/async',
    name: '异步分析',
    icon:'AreaChart',
    component: './User/AddChartAsync',
  },
  {
    path: '/my/chart',
    name: '我的图表',
    icon:'PieChart',
    component: './User/MyChart',
  },
  {
    path: '/admin',
    name: '管理页',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      { path: '/admin', redirect: '/admin/sub-page' },
      { path: '/admin/sub-page', name: '二级管理页', component: './Admin' },
    ],
  },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
