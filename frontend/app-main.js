// Modern notification system
let notificationId = 0;

function notify(msg, type='success') {
  const container = document.getElementById('notificationContainer');
  if (!container) {
    // Fallback to old notification system
    let n = document.getElementById('notifyBar');
    n.innerText = msg;
    n.className = 'alert alert-' + type;
    n.style.display = 'block';
    setTimeout(() => n.style.display = 'none', 2200);
    return;
  }

  const id = `notification-${notificationId++}`;
  const notification = document.createElement('div');
  notification.className = `notification ${type}`;
  notification.id = id;
  
  // Add icon based on type
  let iconClass = '';
  switch(type) {
    case 'success':
      iconClass = 'fas fa-check-circle';
      break;
    case 'warning':
      iconClass = 'fas fa-exclamation-circle';
      break;
    case 'danger':
      iconClass = 'fas fa-times-circle';
      break;
    case 'info':
      iconClass = 'fas fa-info-circle';
      break;
    default:
      iconClass = 'fas fa-bell';
  }
  
  notification.innerHTML = `
    <div class="notification-icon">
      <i class="${iconClass}"></i>
    </div>
    <div class="notification-content">${msg}</div>
    <div class="notification-close" onclick="closeNotification('${id}')">
      <i class="fas fa-times"></i>
    </div>
  `;
  
  container.appendChild(notification);
  
  // Trigger show animation
  setTimeout(() => {
    notification.classList.add('show');
  }, 10);
  
  // Auto close after 3 seconds
  setTimeout(() => {
    closeNotification(id);
  }, 3000);
}

function closeNotification(id) {
  const notification = document.getElementById(id);
  if (notification) {
    notification.classList.remove('show');
    setTimeout(() => {
      notification.remove();
    }, 300);
  }
}

// ---- Vue 3 应用（完整） ----
(function(){
  try{
    console.log('app-main: window.Vue=', !!window.Vue);
    if(!window.Vue){
      console.error('Vue not found: ensure <script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script> is loaded before app-main.js');
      return;
    }
    const { createApp, ref, reactive } = Vue;
    const app = createApp({
      setup(){
      const activities = ref([]);
      const pendingActivities = ref([]); // 待审核活动列表
      const state = reactive({ user: null, view: 'login' });
      // 顶级导航（中文标签）和侧边菜单状态
      // 顶级：'活动中心' | '活动管理' | '用户管理'
      const topNav = ref('');
      // 侧边菜单使用中文项名
      const sideMenu = ref('活动列表'); // 子菜单默认项
      const showProfileMenu = ref(false);
      const loginForm = reactive({ username: '', password: '' });
      const regForm = reactive({ username: '', password: '', email: '' });
      const profile = reactive({ username: '', email: '', avatar: '/resources/images/default-avatar.jpg' });
      const pwdForm = reactive({ oldPwd: '', newPwd: '', newUsername: '' });
      const pubForm = reactive({ activityId: '', name: '', desc: '', location: '', startTime: '', endTime: '', maxNum: 0 });
      const pubFormMsg = ref('');
      const myActs = ref([]);
      const myRegs = ref([]);
      const manageState = reactive({ activityId: '', regs: [], addUserId: '' });
      const selectedActivity = ref(null);
      const previousView = ref('activities'); // 用于返回上一个视图
      const newUser = reactive({ username: '', password: '', email: '', role: 'user', errorMsg: '' }); // 新增用户表单
      const userList = ref([]); // 用户列表
      const editingUser = reactive({ userId: '', username: '', email: '', role: 'user' }); // 编辑中的用户
      const selectedUser = ref(null); // 选中的用户
      const searchKeyword = ref(''); // 搜索关键词
      const newRole = ref('user'); // 新角色

      // Helper: switch top nav and automatically set left submenu & view
      const selectTopNav = async (name) => {
        topNav.value = name;
        // 映射到中文子菜单与视图
        if (name === '活动中心') {
          sideMenu.value = '活动列表';
          state.view = 'activities';
          await loadActivitiesWithPagination(1, 6);
        } else if (name === '活动管理') {
          // 默认切到活动管理列表而不是发布活动
          sideMenu.value = '活动管理列表';
          state.view = 'activityManagement';
          await loadReviewedActivitiesWithPagination(1, 6);
        } else if (name === '个人中心') {
          sideMenu.value = '我的信息';
          // map user's personal center to existing views
          state.view = 'user';
          await refreshUserInfo();
        } else if (name === '用户管理') {
          sideMenu.value = '用户列表';
          state.view = 'userManagement';
          await loadUserList(1, 10);
        }
      };

      const selectSideMenu = async (item) => {
        sideMenu.value = item;
        // 将中文侧边项映射为已有视图名
        if (item === '活动列表') { state.view = 'activities'; await loadActivitiesWithPagination(1, 6); }
        else if (item === '我的报名') { state.view = 'myreg'; await loadMyRegs(); }
        else if (item === '我的活动') { state.view = 'myact'; await loadMyActs(); }
        else if (item === '我的信息') { state.view = 'user'; await refreshUserInfo(); }
        else if (item === '修改信息') { state.view = 'pwd'; }
        else if (item === '发布活动') { state.view = 'publish'; openPublish(); }
        else if (item === '活动管理列表') { state.view = 'activityManagement'; await loadReviewedActivitiesWithPagination(1, 6); }
        else if (item === '活动审核') { state.view = 'activityManagement'; await loadPendingActivities(); }
        else if (item === '用户列表') { state.view = 'userManagement'; await loadUserList(1, 10); }
        else if (item === '权限管理') { state.view = 'userManagement'; await loadUserList(1,10); }
      };

      const toggleProfileMenu = () => { showProfileMenu.value = !showProfileMenu.value; };

      // make sure when setView is called elsewhere we keep topNav consistent
       // 分页相关数据
       const pagination = reactive({
         page: 1,
         pageSize: 10,
         total: 0,
         totalPages: 0
       });

       // 活动列表分页数据
       const activityPagination = reactive({
         page: 1,
         pageSize: 10,
         total: 0,
         totalPages: 0
       });

       // 活动管理列表分页数据
       const managementPagination = reactive({
         page: 1,
         pageSize: 6,
         total: 0,
         totalPages: 0
       });

       // 搜索/筛选状态
       const filters = reactive({ startTime: '', endTime: '', location: '', publisher: '', hasAvailable: false, status: '' });
       const searching = ref(false);

       const load = async ()=>{
         // 检查是否需要分页
         if (state.view === 'activities') {
           // 活动报名页面使用分页
           await loadActivitiesWithPagination(1, 6);
         } else {
           // 其他情况加载所有活动
           const res = await fetch('/api/activity/list');
           activities.value = await res.json();
         }
       };

       // 加载分页活动列表
       const loadActivitiesWithPagination = async (page = 1, pageSize = 6) => {
         try {
           // 如果处于搜索状态，请求更大的 pageSize 以便客户端过滤
           const reqPageSize = searching.value ? Math.max(pageSize, 100) : pageSize;
           const res = await fetch(`/api/activity/list?page=${page}&pageSize=${reqPageSize}`);
           const data = await res.json();

           let rawList = [];
           if (data.activities) rawList = data.activities;
           else rawList = data;

           if (!searching.value) {
             // 非搜索时使用后端分页信息（保持原有行为）
             if (data.activities) {
               activityPagination.page = data.page || page;
               activityPagination.pageSize = data.pageSize || pageSize;
               activityPagination.total = data.total || (rawList.length || 0);
               activityPagination.totalPages = data.totalPages || Math.max(1, Math.ceil((activityPagination.total || 0) / activityPagination.pageSize));
               activities.value = rawList;
             } else {
               activityPagination.page = 1;
               activityPagination.pageSize = rawList.length || pageSize;
               activityPagination.total = rawList.length || 0;
               activityPagination.totalPages = Math.max(1, Math.ceil(activityPagination.total / activityPagination.pageSize));
               activities.value = rawList;
             }
           } else {
             // 搜索时：在客户端进行过滤并做 client-side pagination（保留分页控件）
             const filtered = applyClientFilters(rawList);
             activityPagination.page = page;
             // keep the visible pageSize as the requested pageSize param to allow paging through results
             activityPagination.pageSize = pageSize || 6;
             activityPagination.total = filtered.length;
             activityPagination.totalPages = Math.max(1, Math.ceil(activityPagination.total / activityPagination.pageSize));
             // slice for the requested page
             const start = (page - 1) * activityPagination.pageSize;
             const end = start + activityPagination.pageSize;
             activities.value = filtered.slice(start, end);
           }
         } catch (error) {
           console.error('加载活动列表失败:', error);
           activities.value = [];
         }
       };

       // 计算分页页码数组
       const getPageNumbers = () => {
         const pageNumbers = [];
         // 根据当前视图选择使用哪个分页数据
         let page, totalPages;
         if (state.view === 'activities') {
           // 活动列表使用活动分页数据
           page = activityPagination.page;
           totalPages = activityPagination.totalPages;
         } else if (state.view === 'userManagement') {
           // 用户列表使用用户分页数据
           page = pagination.page;
           totalPages = pagination.totalPages;
         } else if (state.view === 'activityManagement') {
           // 活动管理列表使用管理分页数据
           page = managementPagination.page;
           totalPages = managementPagination.totalPages;
         } else {
           return [];
         }

         let start = Math.max(1, page - 2);
         let end = Math.min(totalPages, page + 2);

         // 确保显示5个页码（如果可能）
         if (end - start < 4) {
           if (start === 1) {
             end = Math.min(totalPages, start + 4);
           } else {
             start = Math.max(1, end - 4);
           }
         }

         for (let i = start; i <= end; i++) {
           pageNumbers.push(i);
         }

         return pageNumbers;
       };

       // 加载待审核活动
       const loadPendingActivities = async () => {
         if (!state.user || state.user.role !== 'admin') return;
         const res = await fetch(`/api/activity/pending?reviewerId=${state.user.userId}`);
         const result = await res.json();
         if (Array.isArray(result)) {
           pendingActivities.value = result;
         } else {
           pendingActivities.value = [];
         }
       };

       // 加载所有活动（供管理员在活动管理页面使用）
       const loadAllActivities = async () => {
         if (!state.user || state.user.role !== 'admin') return;
         const res = await fetch(`/api/activity/all?reviewerId=${state.user.userId}`);
         const result = await res.json();
         if (Array.isArray(result)) {
           pendingActivities.value = result;
         } else {
           pendingActivities.value = [];
         }
       };

       // 加载已审核通过的活动（供管理员在活动管理列表中使用）
       const loadApprovedActivities = async () => {
         if (!state.user || state.user.role !== 'admin') return;
         const res = await fetch(`/api/activity/approved?reviewerId=${state.user.userId}`);
         const result = await res.json();
         if (Array.isArray(result)) {
           pendingActivities.value = result;
         } else {
           pendingActivities.value = [];
         }
       };

       // 加载已审核（通过或拒绝）的活动（供管理员在活动管理列表中使用）
       const loadReviewedActivities = async () => {
         if (!state.user || state.user.role !== 'admin') return;
         const res = await fetch(`/api/activity/reviewed?reviewerId=${state.user.userId}`);
         const result = await res.json();
         if (Array.isArray(result)) {
           pendingActivities.value = result;
         } else {
           pendingActivities.value = [];
         }
       };

       // 加载分页的已审核活动
       const loadReviewedActivitiesWithPagination = async (page = 1, pageSize = 6) => {
         if (!state.user || state.user.role !== 'admin') return;
         
         try {
           // 如果处于搜索状态，请求更大的 pageSize 以便客户端过滤
           const reqPageSize = searching.value ? Math.max(pageSize, 100) : pageSize;
           const res = await fetch(`/api/activity/reviewed?reviewerId=${state.user.userId}&page=${page}&pageSize=${reqPageSize}`);
           const data = await res.json();
           
           let rawList = [];
           if (data.activities) rawList = data.activities;
           else rawList = data;
           
           if (!searching.value) {
             // 非搜索时使用后端分页信息
             pendingActivities.value = rawList;
             
             if (data.activities) {
               managementPagination.page = data.page || page;
               managementPagination.pageSize = data.pageSize || pageSize;
               managementPagination.total = data.total || (rawList.length || 0);
               managementPagination.totalPages = data.totalPages || Math.max(1, Math.ceil((managementPagination.total || 0) / managementPagination.pageSize));
             } else {
               managementPagination.page = 1;
               managementPagination.pageSize = rawList.length || pageSize;
               managementPagination.total = rawList.length || 0;
               managementPagination.totalPages = Math.max(1, Math.ceil(managementPagination.total / managementPagination.pageSize));
             }
           } else {
             // 搜索时：在客户端进行过滤并做 client-side pagination
             const filtered = applyClientFilters(rawList);
             managementPagination.page = page;
             // keep the visible pageSize as the requested pageSize param to allow paging through results
             managementPagination.pageSize = pageSize || 6;
             managementPagination.total = filtered.length;
             managementPagination.totalPages = Math.max(1, Math.ceil(managementPagination.total / managementPagination.pageSize));
             // slice for the requested page
             const start = (page - 1) * managementPagination.pageSize;
             const end = start + managementPagination.pageSize;
             pendingActivities.value = filtered.slice(start, end);
           }
         } catch (error) {
           console.error('加载活动管理列表失败:', error);
           pendingActivities.value = [];
           managementPagination.page = 1;
           managementPagination.pageSize = 6;
           managementPagination.total = 0;
           managementPagination.totalPages = 0;
         }
       };

       // 加载用户列表（支持分页和搜索）
       const loadUserList = async (page = 1, pageSize = 10) => {
         if (!state.user || state.user.role !== 'admin') return;
         try {
           console.log('正在加载用户列表，页码:', page, '每页数量:', pageSize, '搜索关键词:', searchKeyword.value);

           // 构建查询参数
           let queryParams = `adminId=${state.user.userId}&page=${page}&pageSize=${pageSize}`;
           if (searchKeyword.value) {
             queryParams += `&search=${encodeURIComponent(searchKeyword.value)}`;
           }

           const res = await fetch(`/api/admin/user/list?${queryParams}`);
           console.log('用户列表响应状态:', res.status);
           if (res.ok) {
             const result = await res.json();
             console.log('用户列表数据:', result);
             if (result.status === 0) {
               // 解析返回的数据
               const userData = JSON.parse(result.data.replace(/\\\\/g, '\\').replace(/\\"/g, '"'));
               if (Array.isArray(userData)) {
                 userList.value = userData;
               } else {
                 userList.value = [];
               }
               // 更新分页信息
               pagination.total = result.total;
               pagination.page = result.page;
               pagination.pageSize = result.pageSize;
               pagination.totalPages = result.totalPages;

               // 清除选中用户
               selectedUser.value = null;
             } else {
               userList.value = [];
             }
           } else {
             console.error('加载用户列表失败，状态码:', res.status);
             userList.value = [];
           }
         } catch (error) {
           console.error('加载用户列表失败:', error);
           userList.value = [];
         }
       };

       const refreshUserInfo = async ()=>{
         if(!state.user) return;
         const res = await fetch('/api/user/info',{method:'POST',body:new URLSearchParams({userId:state.user.userId})});
         const d = await res.json();
         profile.username = d.username||'';
         profile.email = d.email||'';
         // 使用 resources 下的默认头像作为兜底（与后端默认路径一致）
         profile.avatar = (d.avatar || '/resources/images/default-avatar.jpg');
       };
       const doLogin = async ()=>{
         const p = new URLSearchParams({username:loginForm.username,password:loginForm.password});
         const r = await fetch('/api/login',{method:'POST',body:p});
         const d = await r.json();
         if(d.status===0){
           state.user=d; window.user=d;
           // show left navigation now that user is logged in; default to 活动中心
           const left = document.getElementById('leftNavRow');
           if (left) left.style.display = 'block';
           // add sidebar-visible class to mainPanel to shift content
           const main = document.getElementById('mainPanel');
           if (main) main.classList.add('sidebar-visible');
           // default to 活动中心 as requested
           topNav.value = '活动中心';
           await selectTopNav('活动中心');
           notify('登录成功');
           await refreshUserInfo();
           await loadMyActs(); 
           await loadMyRegs();
           
           // 初始化之前的状态数据
           previousActivities.value = [...myActs.value];
           await loadPendingActivities();
           previousPendingActivities.value = [...pendingActivities.value];
           
           await load();
         } else notify(d.msg||'登录失败','danger');
       };
       const doRegister = async ()=>{
         const p = new URLSearchParams({username:regForm.username,password:regForm.password,email:regForm.email});
         const r = await fetch('/api/register',{method:'POST',body:p});
         const d = await r.json();
         if(d.status===0){ notify('注册成功，请登录'); state.view='login'; }
         else notify(d.msg||'注册失败','danger');
       };
       const logout = ()=>{
         state.user=null; window.user=null; state.view='login'; myActs.value = []; myRegs.value = []; pendingActivities.value = []; notify('已退出');
         // hide left navigation on logout and reset nav state
         const left = document.getElementById('leftNavRow');
         if (left) left.style.display = 'none';
         const main = document.getElementById('mainPanel');
         if (main) main.classList.remove('sidebar-visible');
         topNav.value = '';
         sideMenu.value = '';
       };
       const updateInfo = async ()=>{
         if(!state.user) return;
         const p = new URLSearchParams({userId:state.user.userId,username:profile.username,email:profile.email,avatar:profile.avatar});
         const r = await fetch('/api/user/update',{method:'POST',body:p});
         const d = await r.json();
         if(d.status===0){ state.user.username=profile.username; notify('资料已保存','success'); await load(); } // 更新资料后刷新活动列表
         else notify('修改失败，用户名可能已存在','danger');
       };
       const uploadAvatar = async (ev)=>{
         if(!state.user) return; const f = ev.target.files[0]; if(!f) return;
        const form = new FormData(); form.append('avatar', f); form.append('userId', state.user.userId);
        const r = await fetch('/api/user/avatarUpload',{method:'POST',body:form}); const d = await r.json();
        if(d.status===0){ profile.avatar=d.url; state.user.avatar=d.url; notify('头像上传成功'); await load(); } // 上传头像后刷新活动列表
        else notify('头像上传失败','danger');
       };
       const changePwd = async ()=>{
         if(!state.user) return;
         
         // Check if at least one field is filled
         if(!pwdForm.newUsername && !pwdForm.newPwd) {
           notify('请至少填写新用户名或新密码','warning');
           return;
         }
         
         const p = new URLSearchParams({
           userId: state.user.userId,
           oldPwd: pwdForm.oldPwd
         });
         
         // Add new username if provided
         if(pwdForm.newUsername) {
           p.append('newUsername', pwdForm.newUsername);
         }
         
         // Add new password if provided
         if(pwdForm.newPwd) {
           p.append('newPwd', pwdForm.newPwd);
         }
         
         const r = await fetch('/api/user/changePwd',{method:'POST',body:p}); 
         const d = await r.json();
         if(d.status===0){ 
           notify('信息修改成功'); 
           state.view='user'; 
           pwdForm.oldPwd=''; 
           pwdForm.newPwd=''; 
           pwdForm.newUsername='';
           
           // Update user info in state if username was changed
           if(d.newUsername) {
             state.user.username = d.newUsername;
             profile.username = d.newUsername;
           }
           
           await refreshUserInfo(); 
         }
         else notify(d.msg || '原密码错误','danger');
       };
       
       const confirmChangePwd = async () => {
         // Check if at least one field is filled
         if(!pwdForm.newUsername && !pwdForm.newPwd) {
           notify('请至少填写新用户名或新密码','warning');
           return;
         }
         
         // Show confirmation dialog
         if(confirm('确定要修改用户名或密码吗？')) {
           await changePwd();
         }
       };
       const apply = async (aid, maxNum, count)=>{
         if(!state.user){ notify('请先登录','warning'); state.view='login'; return; }

        // Check if activity is full
        if (count >= maxNum) {
          notify('活动报名人数已满，无法报名','warning');
          return;
        }

        // Check if user has already registered for this activity
        await loadMyRegs();
        const alreadyRegistered = myRegs.value.some(reg => reg.activityId === aid);
        if (alreadyRegistered) {
          notify('已报名','info');
          return;
        }
        
        // Load registrations for this activity to check if it's full
        try {
          const regRes = await fetch(`/api/registration/list?activityId=${aid}`);
          const registrations = await regRes.json();
          
          // Check if activity is full based on submitted applications
          if (registrations && registrations.length >= maxNum) {
            notify('活动报名人数已满，无法报名','warning');
            return;
          }
        } catch (e) {
          // If we can't load registrations, fall back to the count parameter
          if (count >= maxNum) {
            notify('活动报名人数已满，无法报名','warning');
            return;
          }
        }
        
        const r = await fetch('/api/registration/apply',{method:'POST',body:new URLSearchParams({userId:state.user.userId,activityId:aid})});
        const d = await r.json();
        if(d.status===0){ notify('已成功提交申请','success'); await load(); await loadMyRegs(); } // 报名后刷新活动列表
        else if(d.status===2){ 
          // Check approved registrations to determine if activity is truly full
          try {
            const regRes = await fetch(`/api/registration/list?activityId=${aid}`);
            const registrations = await regRes.json();
            
            // Count approved registrations
            const approvedCount = registrations.filter(reg => reg.status === '已通过').length;
            
            // If approved registrations >= maxNum, then it's truly full
            if (approvedCount >= maxNum) {
              notify('活动报名人数已满，无法报名','warning');
              return;
            } else {
              // If not truly full, show the message from server
              notify('报名失败','danger');
              return;
            }
          } catch (e) {
            // If we can't load registrations, show the message from server
            notify('报名失败','danger');
            return;
          }
        }
        else notify('报名失败','danger');
       };
       const manage = (aid)=>{ openManage(aid); };

      // 活动详情功能
      const showActivityDetail = (activity) => {
        selectedActivity.value = activity;
        previousView.value = state.view; // 保存当前视图
        state.view = 'activityDetail';
      };
      
      // 返回上一个视图
      const backToPreviousView = () => {
        state.view = previousView.value || 'activities';
      };
      
      const viewActivityFromReg = (activityId) => {
        // 根据活动ID找到活动详情并显示
        const activity = activities.value.find(a => a.activityId === activityId) || 
                        myActs.value.find(a => a.activityId === activityId);
        if (activity) {
          showActivityDetail(activity);
        } else {
          notify('未找到活动详情', 'danger');
        }
      };
      
      // 发布/编辑活动
      const openPublish = ()=>{ state.view='publish'; pubForm.activityId=''; pubForm.name=''; pubForm.desc=''; pubForm.location=''; pubForm.startTime=''; pubForm.endTime=''; pubForm.maxNum=0; pubFormMsg.value=''; };
      const editAct = async (act)=>{ 
        state.view='publish'; 
        pubForm.activityId=act.activityId; 
        pubForm.name=act.activityName; 
        pubForm.desc=act.description; 
        pubForm.location=act.location || '';
        pubFormMsg.value=''; 
        pubForm.startTime=''; 
        pubForm.endTime=''; 
        // 修复时间格式处理的bug
        if(act.startTime) {
          // 确保正确处理时间格式，修复可能的时区问题
          const startDate = new Date(act.startTime);
          pubForm.startTime = startDate.toISOString().slice(0, 16);
        }
        if(act.endTime) {
          // 确保正确处理时间格式，修复可能的时区问题
          const endDate = new Date(act.endTime);
          pubForm.endTime = endDate.toISOString().slice(0, 16);
        }
        pubForm.maxNum=act.maxNum||0;
        
        // 获取已通过审核的报名人数，以便在提交时进行验证
        try {
          const r = await fetch(`/api/registration/list?activityId=${act.activityId}`);
          const regs = await r.json();
          const approvedCount = regs.filter(reg => reg.status === '已通过').length;
          // 将已通过审核的人数保存到表单中，用于提交时验证
          pubForm.approvedCount = approvedCount;
        } catch (e) {
          console.error('获取报名信息失败', e);
          pubForm.approvedCount = 0;
        }
      };
      const submitPublish = async ()=>{
        pubFormMsg.value='';
        if(!pubForm.name||!pubForm.desc||!pubForm.startTime||!pubForm.endTime||!pubForm.maxNum){ pubFormMsg.value='请填写完整'; return; }
        
        // 检查活动开始时间不能小于当前时间
        const startTime = new Date(pubForm.startTime);
        const now = new Date();
        if (startTime < now) {
          pubFormMsg.value='活动开始时间不能小于当前时间';
          return;
        }
        
        // 检查活动结束时间不得早于开始时间
        const endTime = new Date(pubForm.endTime);
        if (endTime <= startTime) {
          pubFormMsg.value='活动结束时间必须晚于开始时间';
          return;
        }
        
        // 如果是编辑活动，检查最大人数不能少于已通过审核的人数
        if (pubForm.activityId && pubForm.approvedCount !== undefined && pubForm.maxNum < pubForm.approvedCount) {
          pubFormMsg.value=`最大人数不能少于已通过审核的人数 (${pubForm.approvedCount})`;
          return;
        }
        
        const p=new URLSearchParams({name:pubForm.name,desc:pubForm.desc,location:pubForm.location,publisherId:state.user.userId,startTime:pubForm.startTime,endTime:pubForm.endTime,maxNum:pubForm.maxNum});
        const url = pubForm.activityId?'/api/activity/update':'/api/activity/publish';
        if(pubForm.activityId) p.append('activityId',pubForm.activityId);
        const r = await fetch(url,{method:'POST',body:p}); const d=await r.json();
        if(d.status===0){ notify(pubForm.activityId?'修改成功':'已提交申请'); state.view='myact'; await loadMyActs(); await load(); } // 发布/编辑后刷新活动列表
        else pubFormMsg.value='操作失败';
      };
      const cancelPublish = ()=>{ state.view='myact'; };
      const loadMyActs = async ()=>{
        if(!state.user) return; const r=await fetch('/api/activity/my',{method:'POST',body:new URLSearchParams({publisherId:state.user.userId})}); myActs.value=await r.json();
      };
      const deleteAct = async (aid)=>{ if(!confirm('确认删除？')) return; const r=await fetch('/api/activity/delete',{method:'POST',body:new URLSearchParams({activityId:aid,publisherId:state.user.userId})}); const d=await r.json(); if(d.status===0){ notify('已删除'); await loadMyActs(); await load(); } else notify('删除失败','danger'); }; // 删除后刷新活动列表
      // 我的报名
      const loadMyRegs = async ()=>{ if(!state.user) return; const r=await fetch('/api/registration/my',{method:'POST',body:new URLSearchParams({userId:state.user.userId})}); myRegs.value=await r.json(); };
      // 报名管理
      const openManage = async (aid)=>{ state.view='manage'; manageState.activityId=aid; await loadRegsVue(aid); };
      const loadRegsVue = async (aid)=>{ const r=await fetch(`/api/registration/list?activityId=${aid}`); manageState.regs=await r.json(); };
      const reviewReg = async (rid, approve)=>{ const r=await fetch('/api/registration/review',{method:'POST',body:new URLSearchParams({registrationId:rid,approve})}); const d=await r.json(); if(d.status===0){ notify('审核成功'); await loadRegsVue(manageState.activityId); await load(); await loadMyRegs(); } else notify('操作失败','danger'); }; // 审核后刷新活动列表
      const delReg = async (rid)=>{ const r=await fetch('/api/registration/delete',{method:'POST',body:new URLSearchParams({registrationId:rid})}); const d=await r.json(); if(d.status===0){ notify('已删除'); await loadRegsVue(manageState.activityId); await load(); await loadMyRegs(); } else notify('删除失败','danger'); }; // 删除后刷新活动列表
      const addReg = async ()=>{ const uid = manageState.addUserId.trim(); if(!uid){ notify('请输入用户ID','warning'); return; } const r=await fetch('/api/registration/add',{method:'POST',body:new URLSearchParams({userId:uid,activityId:manageState.activityId})}); const d=await r.json(); if(d.status===0){ notify('已添加'); manageState.addUserId=''; await loadRegsVue(manageState.activityId); await load(); await loadMyRegs(); } else notify('添加失败','danger'); }; // 添加后刷新活动列表
      
      // 活动审核功能
      const reviewActivity = async (activityId, status) => {
        if (!state.user || state.user.role !== 'admin') {
          notify('权限不足', 'danger');
          return;
        }
        
        const r = await fetch('/api/activity/review', {
          method: 'POST',
          body: new URLSearchParams({
            activityId: activityId,
            status: status,
            reviewerId: state.user.userId
          })
        });
        
        const d = await r.json();
        if (d.status === 0) {
          notify(status === 'approved' ? '活动已通过' : '活动已拒绝', 'success');
          // 重新加载活动列表
          await load();
          // 如果在活动管理页面，重新加载活动
          if (state.view === 'activityManagement') {
            // 检查当前选中的菜单是活动管理列表还是活动审核
            if (sideMenu.value === '活动审核') {
              await loadPendingActivities();
            } else {
              await loadReviewedActivities();
            }
          }
          // 如果在活动详情页面且查看的是被审核的活动，更新详情
          if (selectedActivity.value && selectedActivity.value.activityId === activityId) {
            selectedActivity.value.status = status;
          }
          // 刷新我的活动列表
          await loadMyActs();
        } else {
          notify(d.msg || '审核失败', 'danger');
        }
      };
      
      // 活动删除功能（管理员）
      const deleteActivity = async (activityId) => {
        if (!state.user) {
          notify('权限不足', 'danger');
          return;
        }
        
        if (!confirm('确认删除该活动？')) return;
        
        try {
          const r = await fetch('/api/activity/delete', {
            method: 'POST',
            body: new URLSearchParams({
              activityId: activityId,
              publisherId: state.user.userId // 请求者ID
            })
          });
          
          const d = await r.json();
          if (d.status === 0) {
            notify('活动已删除', 'success');
            // 重新加载活动列表
            await load();
            // 如果在活动管理页面，重新加载活动
            if (state.view === 'activityManagement') {
              await loadAllActivities();
            }
            // 刷新我的活动列表
            await loadMyActs();
          } else {
            notify(d.msg || '删除失败', 'danger');
          }
        } catch (error) {
          notify('删除失败', 'danger');
        }
      };
      
      // 获取活动状态文本
      const getActivityStatusText = (status, startTime, endTime) => {
        // 如果有开始时间和结束时间，则基于时间判断状态
        if (startTime && endTime) {
          const now = new Date();
          const start = new Date(startTime);
          const end = new Date(endTime);
          
          if (now < start) {
            return '未开始';
          } else if (now >= start && now <= end) {
            return '进行中';
          } else {
            return '已结束';
          }
        }
        
        // 如果没有时间信息，则使用原来的审核状态
        switch (status) {
          case 'pending': return '待审核';
          case 'approved': return '已通过';
          case 'rejected': return '已拒绝';
          default: return '未知';
        }
      };
      
      // 获取活动状态样式类
      const getActivityStatusClass = (status, startTime, endTime) => {
        // 如果有开始时间和结束时间，则基于时间判断状态
        if (startTime && endTime) {
          const now = new Date();
          const start = new Date(startTime);
          const end = new Date(endTime);
          
          if (now < start) {
            return 'bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          } else if (now >= start && now <= end) {
            return 'bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          } else {
            return 'bg-gray-100 text-gray-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          }
        }
        
        // 如果没有时间信息，则使用原来的审核状态样式
        switch (status) {
          case 'pending': return 'bg-amber-100 text-amber-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          case 'approved': return 'bg-emerald-100 text-emerald-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          case 'rejected': return 'bg-rose-100 text-rose-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
          default: return 'bg-gray-100 text-gray-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        }
      };
      
      // 判断活动是否已结束
      const isActivityEnded = (startTime, endTime) => {
        // 如果没有开始时间和结束时间，则认为活动未结束
        if (!startTime || !endTime) {
          return false;
        }
        
        const now = new Date();
        const end = new Date(endTime);
        
        // 活动结束后返回true
        return now > end;
      };
      
      // 确认更新用户角色
      const confirmUpdateRole = () => {
        if (!selectedUser.value) {
          notify('请先选择要修改的用户', 'warning');
          return;
        }
        
        // 检查是否尝试修改自己的角色
        if (selectedUser.value.userId === state.user.userId) {
          notify('不能修改自己的角色', 'warning');
          return;
        }
        
        // 检查角色是否发生变化
        if (newRole.value === selectedUser.value.role) {
          notify('用户角色未发生变化', 'warning');
          return;
        }
        
        // 获取角色显示名称
        const currentRoleName = selectedUser.value.role === 'admin' ? '管理员' : '普通用户';
        const newRoleName = newRole.value === 'admin' ? '管理员' : '普通用户';
        
        if (confirm(`确定要将用户 "${selectedUser.value.username}" 的角色从 "${currentRoleName}" 修改为 "${newRoleName}" 吗？`)) {
          updateUserInfo();
        }
      };

      // 更新用户信息（包括角色）
      const updateUserInfo = async () => {
        if (!state.user || state.user.role !== 'admin') {
          notify('权限不足', 'danger');
          return;
        }
        
        if (!selectedUser.value) {
          notify('未选择用户', 'warning');
          return;
        }
        
        const p = new URLSearchParams({
          adminId: state.user.userId,
          userId: selectedUser.value.userId,
          username: selectedUser.value.username,
          email: selectedUser.value.email,
          role: newRole.value
        });
        
        const r = await fetch('/api/admin/user/update', {method: 'POST', body: p});
        const d = await r.json();
        
        if (d.status === 0) {
          notify('用户信息更新成功');
          // 重新加载用户列表
          await loadUserList(pagination.page, pagination.pageSize);
          // 重置新角色为默认值
          newRole.value = 'user';
        } else {
          notify(d.msg || '更新失败', 'danger');
        }
      };

      // 确认删除用户
      const confirmDeleteUser = () => {
        if (!selectedUser.value) {
          notify('请先选择要删除的用户', 'warning');
          return;
        }
        
        if (confirm(`确定要删除用户 "${selectedUser.value.username}" 吗？此操作不可恢复！`)) {
          deleteUser(selectedUser.value.userId);
        }
      };

      // 搜索用户
      const searchUsers = async () => {
        // 重置到第一页并执行搜索
        await loadUserList(1, 10);
      };

      // 重置搜索
      const resetSearch = async () => {
        searchKeyword.value = '';
        // 重置到第一页并重新加载所有用户
        await loadUserList(1, 10);
      };

      // 选中用户
      const selectUser = (user) => {
        selectedUser.value = user;
        // 同步新角色选择框的值
        newRole.value = user.role;
      };

      // 在设置视图时加载相应数据
      const setView = async (viewName) => {
        state.view = viewName;
        // keep navigation in sync
        if (['activities','myreg','myact','publish','activityDetail','manage'].includes(viewName)) {
          topNav.value = '活动中心';
          if (viewName === 'activities') sideMenu.value = '活动列表';
          if (viewName === 'myreg') sideMenu.value = '我的报名';
          if (viewName === 'myact') sideMenu.value = '我的活动';
          if (viewName === 'publish') sideMenu.value = '发布活动';
        } else if (viewName === 'activityManagement') {
          topNav.value = '活动管理';
          sideMenu.value = '活动管理列表';
        } else if (viewName === 'userManagement') {
          topNav.value = '用户管理';
          sideMenu.value = '用户列表';
        } else if (viewName === 'pwd') {
          topNav.value = '个人中心';
          sideMenu.value = '修改信息';
        }
        if (viewName === 'activityManagement') {
          await loadReviewedActivitiesWithPagination(1, 6);
        } else if (viewName === 'userManagement') {
          // 加载第一页用户列表
          await loadUserList(1, 10);
          // 重置新角色为默认值
          newRole.value = 'user';
        } else if (viewName === 'publish') {
          // 确保发布活动表单被正确初始化
          openPublish();
        }
       };

      // 刷新通知功能
      const refreshNotifications = async () => {
        if (!state.user) {
          notify('请先登录', 'warning');
          return;
        }
        
        try {
          let notifications = [];
          
          // 保存当前状态用于比较
          const oldMyActs = [...(previousMyActs.value || [])];
          const oldMyRegs = [...(previousMyRegs.value || [])];
          
          // 获取用户最新的活动申请状态
          await loadMyRegs();
          
          // 获取用户发布的活动的审核状态
          await loadMyActs();
          
          // 检查用户发布的活动是否有状态变化（仅在之前有数据时检查）
          if (oldMyActs.length > 0) {
            for (const currentAct of (myActs.value || [])) {
              const oldAct = oldMyActs.find(a => a.activityId === currentAct.activityId);
              if (oldAct && oldAct.status !== currentAct.status) {
                // 活动状态发生了变化
                if (currentAct.status === 'approved') {
                  notifications.push(`活动"${currentAct.activityName}"已通过审核`);
                } else if (currentAct.status === 'rejected') {
                  notifications.push(`活动"${currentAct.activityName}"未通过审核`);
                }
              }
            }
          }
          
          // 检查用户报名的活动是否有状态变化（仅在之前有数据时检查）
          if (oldMyRegs.length > 0) {
            for (const currentReg of (myRegs.value || [])) {
              const oldReg = oldMyRegs.find(r => r.registrationId === currentReg.registrationId);
              if (oldReg && oldReg.status !== currentReg.status) {
                // 报名状态发生了变化
                if (currentReg.status === '已通过') {
                  notifications.push(`活动"${currentReg.activityName}"的报名申请已通过`);
                } else if (currentReg.status === '已拒绝') {
                  notifications.push(`活动"${currentReg.activityName}"的报名申请被拒绝`);
                }
              }
            }
          }
          
          // 如果是管理员，检查是否有新的待审核活动
          if (state.user.role === 'admin') {
            const oldPending = [...(previousPendingActivities.value || [])];
            
            // 根据当前视图加载相应的活动数据
            if (state.view === 'activityManagement' && sideMenu.value === '活动管理列表') {
              // 在活动管理列表界面，刷新已审核活动
              await loadReviewedActivitiesWithPagination(managementPagination.page, managementPagination.pageSize);
            } else if (state.view === 'activityManagement' && sideMenu.value === '活动审核') {
              // 在活动审核界面，刷新待审核活动
              await loadPendingActivities();
            } else {
              // 其他情况加载待审核活动
              await loadPendingActivities();
            }
            
            // 检查是否有新的待审核活动（仅在之前有数据时检查）
            if (pendingActivities.value && oldPending.length > 0) {
              // 比较新旧列表，找出新增的活动
              const newActivities = (pendingActivities.value || []).filter(newAct => 
                !oldPending.some(oldAct => oldAct.activityId === newAct.activityId)
              );
              
              if (newActivities.length > 0) {
                notifications.push(`有${newActivities.length}个新活动等待审核`);
              }
            }
            
            // 更新之前的状态
            previousPendingActivities.value = [...(pendingActivities.value || [])];
          }
          
          // 更新之前的状态
          previousMyActs.value = [...(myActs.value || [])];
          previousMyRegs.value = [...(myRegs.value || [])];
          
          // 显示通知
          if (notifications.length > 0) {
            // 使用自定义确认对话框显示通知
            showNotificationDialog(notifications);
          } else {
            notify('暂无新通知', 'info');
          }
        } catch (error) {
          console.error('刷新通知失败:', error);
          notify('刷新成功', 'success'); // 即使没有通知，也表示刷新成功
        }
      };
      
      // 显示通知对话框
      const showNotificationDialog = (notifications) => {
        // 创建自定义对话框
        const dialog = document.createElement('div');
        dialog.className = 'notification-dialog';
        dialog.style.cssText = `
          position: fixed;
          top: 20px;
          right: 20px;
          z-index: 10000;
          background: white;
          border-radius: 8px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          padding: 16px;
          min-width: 300px;
          max-width: 400px;
          border-left: 4px solid #1890ff;
          animation: slideIn 0.3s ease-out;
        `;
        
        // 添加动画样式
        if (!document.querySelector('#notification-styles')) {
          const style = document.createElement('style');
          style.id = 'notification-styles';
          style.textContent = `
            @keyframes slideIn {
              from {
                transform: translateX(100%);
                opacity: 0;
              }
              to {
                transform: translateX(0);
                opacity: 1;
              }
            }
            
            @keyframes slideOut {
              from {
                transform: translateX(0);
                opacity: 1;
              }
              to {
                transform: translateX(100%);
                opacity: 0;
              }
            }
          `;
          document.head.appendChild(style);
        }
        
        let content = `
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
            <h6 style="margin: 0; color: #1890ff;">系统通知</h6>
            <button class="close-btn" style="background: none; border: none; font-size: 18px; cursor: pointer; color: #999;">×</button>
          </div>
          <ul style="margin: 0; padding-left: 20px;">`;
        
        notifications.forEach(notification => {
          content += `<li style="margin-bottom: 8px; color: #333;">${notification}</li>`;
        });
        
        content += `
          </ul>
          <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
            <button class="confirm-btn" style="
              background: #1890ff;
              color: white;
              border: none;
              padding: 6px 16px;
              border-radius: 4px;
              cursor: pointer;
              font-size: 14px;
            ">确定</button>
          </div>
        `;
        
        dialog.innerHTML = content;
        
        // 添加关闭按钮事件
        const closeBtn = dialog.querySelector('.close-btn');
        const confirmBtn = dialog.querySelector('.confirm-btn');
        
        const closeDialog = () => {
          // 添加淡出动画
          dialog.style.animation = 'slideOut 0.3s ease-out forwards';
          setTimeout(() => {
            if (document.body.contains(dialog)) {
              document.body.removeChild(dialog);
            }
          }, 300);
        };
        
        closeBtn.addEventListener('click', closeDialog);
        confirmBtn.addEventListener('click', closeDialog);
        
        // 添加到页面
        document.body.appendChild(dialog);
        
        // 5秒后自动消失
        setTimeout(closeDialog, 5000);
      };

      // 管理员功能：创建用户
      const createUser = async () => {
        if (!state.user || state.user.role !== 'admin') {
          notify('权限不足', 'danger');
          return;
        }
        
        if (!newUser.username || !newUser.password || !newUser.email) {
          newUser.errorMsg = '请填写完整信息';
          return;
        }
        
        const p = new URLSearchParams({
          adminId: state.user.userId,
          username: newUser.username,
          password: newUser.password,
          email: newUser.email,
          role: newUser.role
        });
        
        const r = await fetch('/api/admin/user/create', {method: 'POST', body: p});
        const d = await r.json();
        
        if (d.status === 0) {
          notify('用户创建成功');
          newUser.username = '';
          newUser.password = '';
          newUser.email = '';
          newUser.role = 'user';
          newUser.errorMsg = '';
          await loadUserList();
        } else {
          newUser.errorMsg = d.msg || '创建失败';
        }
      };
      
      // 管理员功能：删除用户
      const deleteUser = async (userId) => {
        if (!state.user || state.user.role !== 'admin') {
          notify('权限不足', 'danger');
          return;
        }
        
        if (!confirm('确认删除该用户？')) return;
        
        const p = new URLSearchParams({
          adminId: state.user.userId,
          userId: userId
        });
        
        const r = await fetch('/api/admin/user/delete', {method: 'POST', body: p});
        const d = await r.json();
        
        if (d.status === 0) {
          notify('用户删除成功');
          await loadUserList();
        } else {
          notify(d.msg || '删除失败', 'danger');
        }
      };
      
      // 管理员功能：编辑用户
      const editUser = (user) => {
        editingUser.userId = user.userId;
        editingUser.username = user.username;
        editingUser.email = user.email;
        editingUser.role = user.role;
        
        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
        modal.show();
      };
      
      // 管理员功能：更新用户
      const updateUser = async () => {
        if (!state.user || state.user.role !== 'admin') {
          notify('权限不足', 'danger');
          return;
        }
        
        const p = new URLSearchParams({
          adminId: state.user.userId,
          userId: editingUser.userId,
          username: editingUser.username,
          email: editingUser.email,
          role: editingUser.role
        });
        
        const r = await fetch('/api/admin/user/update', {method: 'POST', body: p});
        const d = await r.json();
        
        if (d.status === 0) {
          notify('用户信息更新成功');
          // 隐藏模态框
          const modalEl = document.getElementById('editUserModal');
          const modal = bootstrap.Modal.getInstance(modalEl);
          modal.hide();
          await loadUserList();
        } else {
          notify(d.msg || '更新失败', 'danger');
        }
      };

      // 客户端应用筛选（在获取到原始数据后应用）
      const applyClientFilters = (list) => {
        return list.filter(a => {
          // 时间过滤
          if (filters.startTime) {
            if (!a.startTime || new Date(a.startTime) < new Date(filters.startTime)) return false;
          }
          if (filters.endTime) {
            if (!a.endTime || new Date(a.endTime) > new Date(filters.endTime)) return false;
          }
          // 地点过滤（包含匹配）
          if (filters.location) {
            if (!a.location || a.location.toLowerCase().indexOf(filters.location.toLowerCase()) === -1) return false;
          }
          // 发布者过滤（匹配用户名或ID）
          if (filters.publisher) {
            const pub = (a.publisherName || a.publisherId || '').toString().toLowerCase();
            if (pub.indexOf(filters.publisher.toLowerCase()) === -1) return false;
          }
          // 空余名额过滤
          if (filters.hasAvailable) {
            const count = parseInt(a.count || 0, 10);
            const maxNum = parseInt(a.maxNum || 0, 10);
            if (isNaN(count) || isNaN(maxNum) || count >= maxNum) return false;
          }
          // 状态过滤
          if (filters.status && a.status !== filters.status) {
            return false;
          }

          return true;
        });
      };

      const applyFilters = async () => {
        searching.value = true;
        // 当搜索时，增加请求的 pageSize 以覆盖更多候选项（简单策略）
        await loadActivitiesWithPagination(1, 100);
      };

      const resetFilters = () => {
        // 清空所有筛选条件
        filters.startTime = '';
        filters.endTime = '';
        filters.location = '';
        filters.publisher = '';
        filters.hasAvailable = false;

        searching.value = false;
        
        // 重新加载第一页数据，确保以分页形式展示
        loadActivitiesWithPagination(1, 6);
      };

      // 活动管理列表的筛选功能
      const applyManagementFilters = async () => {
        // 对于活动管理列表，我们需要在客户端进行筛选
        // 先加载所有数据
        searching.value = true;
        await loadReviewedActivitiesWithPagination(1, 100);
      };

      const resetManagementFilters = () => {
        // 清空所有筛选条件
        filters.startTime = '';
        filters.endTime = '';
        filters.location = '';
        filters.publisher = '';
        filters.status = '';

        searching.value = false;
        
        // 重新加载第一页数据
        loadReviewedActivitiesWithPagination(1, 6);
      };

      // 存储上一次的活动状态，用于比较变化
      const previousActivities = ref([]);
      const previousPendingActivities = ref([]);
      const previousMyActs = ref([]); 
      const previousMyRegs = ref([]);

      // 首次加载
      load();
      return {
         activities,
         pendingActivities,
        topNav, sideMenu, selectTopNav, selectSideMenu, showProfileMenu, toggleProfileMenu,
         state,
         loginForm,
         regForm,
         profile,
         pwdForm,
         pubForm,
         pubFormMsg,
         myActs,
         myRegs,
         manageState,
         selectedActivity,
         previousView,
         newUser, // 新增用户表单
         userList, // 用户列表
         editingUser, // 编辑中的用户
         pagination, // 分页数据
         activityPagination, // 活动列表分页数据
         managementPagination, // 活动管理列表分页数据
         selectedUser, // 选中的用户
         searchKeyword, // 搜索关键词
         newRole, // 新角色
         load,
         apply,
         manage,
         showActivityDetail,
         backToPreviousView,
         viewActivityFromReg,
         doLogin,
         doRegister,
         logout,
         updateInfo,
         uploadAvatar,
         changePwd,
         confirmChangePwd,
         openPublish,
         editAct,
         submitPublish,
         cancelPublish,
         loadMyActs,
         deleteAct,
         loadMyRegs,
         openManage,
         reviewReg,
         delReg,
         addReg,
         reviewActivity,
         deleteActivity,
         getActivityStatusText,
         getActivityStatusClass,
         isActivityEnded, // 添加新函数
         loadPendingActivities,
         setView,
         loadUserList, // 加载用户列表
         loadActivitiesWithPagination, // 加载分页活动列表
         loadReviewedActivitiesWithPagination, // 加载分页的已审核活动
         getPageNumbers, // 获取分页页码
         selectUser, // 选中用户
         searchUsers, // 搜索用户
         resetSearch, // 重置搜索
         confirmDeleteUser, // 确认删除用户
         confirmUpdateRole, // 确认更新用户角色
         updateUserInfo, // 更新用户信息
         createUser, // 创建用户
         deleteUser, // 删除用户
         editUser, // 编辑用户
         updateUser, // 更新用户
         applyManagementFilters, // 应用活动管理列表筛选
         resetManagementFilters, // 重置活动管理列表筛选
         filters, searching, applyFilters, resetFilters,
         refreshNotifications, // 刷新通知功能
         showNotificationDialog // 显示通知对话框
        };
     }
   });
   window.vueApp = app.mount('#app');
   console.log('app-main: Vue mounted to #app');
  }catch(err){
    console.error('app-main error during init:', err);
    // Re-throw so it's visible in the browser console too
    throw err;
  }
})();
