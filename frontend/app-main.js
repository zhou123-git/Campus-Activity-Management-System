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
if(window.Vue){
  const { createApp, ref, reactive } = Vue;
  const app = createApp({
    setup(){
      const activities = ref([]);
      const pendingActivities = ref([]); // 待审核活动列表
      const state = reactive({ user: null, view: 'welcome' });
      const loginForm = reactive({ username: '', password: '' });
      const regForm = reactive({ username: '', password: '', email: '' });
      const profile = reactive({ username: '', email: '', avatar: '/default-avatar.png' });
      const pwdForm = reactive({ oldPwd: '', newPwd: '' });
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

      const load = async ()=>{
        const res = await fetch('/api/activity/list');
        activities.value = await res.json();
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
      
      // 加载用户列表
      const loadUserList = async () => {
        if (!state.user || state.user.role !== 'admin') return;
        try {
          console.log('正在加载用户列表...');
          const res = await fetch(`/api/admin/user/list?adminId=${state.user.userId}`);
          console.log('用户列表响应状态:', res.status);
          if (res.ok) {
            const result = await res.json();
            console.log('用户列表数据:', result);
            if (Array.isArray(result)) {
              userList.value = result;
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
        profile.avatar = d.avatar||'/default-avatar.png';
      };
      const doLogin = async ()=>{
        const p = new URLSearchParams({username:loginForm.username,password:loginForm.password});
        const r = await fetch('/api/login',{method:'POST',body:p});
        const d = await r.json();
        if(d.status===0){
          state.user=d; window.user=d; state.view='activities';
          await load(); await refreshUserInfo();
          await loadMyActs(); await loadMyRegs();
          notify('登录成功');
        } else notify(d.msg||'登录失败','danger');
      };
      const doRegister = async ()=>{
        const p = new URLSearchParams({username:regForm.username,password:regForm.password,email:regForm.email});
        const r = await fetch('/api/register',{method:'POST',body:p});
        const d = await r.json();
        if(d.status===0){ notify('注册成功，请登录'); state.view='login'; }
        else notify(d.msg||'注册失败','danger');
      };
      const logout = ()=>{ state.user=null; window.user=null; state.view='welcome'; myActs.value = []; myRegs.value = []; pendingActivities.value = []; notify('已退出'); };
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
        const p = new URLSearchParams({userId:state.user.userId,oldPwd:pwdForm.oldPwd,newPwd:pwdForm.newPwd});
        const r = await fetch('/api/user/changePwd',{method:'POST',body:p}); const d = await r.json();
        if(d.status===0){ notify('密码修改成功'); state.view='user'; pwdForm.oldPwd=''; pwdForm.newPwd=''; }
        else notify('原密码错误','danger');
      };
      const apply = async (aid, maxNum, count)=>{
        if(!state.user){ notify('请先登录','warning'); state.view='login'; return; }
        
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
          if (registrations && registrations.length > maxNum) {
            notify('活动报名人数已满','warning');
            return;
          }
        } catch (e) {
          // If we can't load registrations, fall back to the count parameter
          if (count > maxNum) {
            notify('活动报名人数已满','warning');
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
            if (approvedCount > maxNum) {
              notify('活动报名人数已满','warning');
              return;
            } else {
              // If not truly full, show the message from server
              notify( '报名失败','danger');
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
      const canManage = (a)=>{ return !!(state.user && a && a.publisherId===state.user.userId); };

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
        
        // 检查活动结束时间不得早于开始时间
        const startTime = new Date(pubForm.startTime);
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
          // 如果在活动管理页面，重新加载待审核活动
          if (state.view === 'activityManagement') {
            await loadPendingActivities();
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
      const getActivityStatusText = (status) => {
        switch (status) {
          case 'pending': return '待审核';
          case 'approved': return '已通过';
          case 'rejected': return '已拒绝';
          default: return '未知';
        }
      };
      
      // 获取活动状态样式类
      const getActivityStatusClass = (status) => {
        switch (status) {
          case 'pending': return 'bg-warning text-dark status-pending';
          case 'approved': return 'bg-success status-approved';
          case 'rejected': return 'bg-danger status-rejected';
          default: return 'bg-secondary';
        }
      };

      // 在设置视图时加载相应数据
      const setView = async (viewName) => {
        state.view = viewName;
        if (viewName === 'activityManagement') {
          await loadAllActivities();
        } else if (viewName === 'userManagement') {
          await loadUserList();
        }
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

      // 首次加载
      load();
      return { 
        activities, 
        pendingActivities,
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
        load, 
        apply, 
        manage, 
        canManage, 
        showActivityDetail, 
        backToPreviousView,
        viewActivityFromReg, 
        doLogin, 
        doRegister, 
        logout, 
        updateInfo, 
        uploadAvatar, 
        changePwd, 
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
        loadPendingActivities,
        setView,
        loadUserList, // 加载用户列表
        createUser, // 创建用户
        deleteUser, // 删除用户
        editUser, // 编辑用户
        updateUser // 更新用户
      };
    }
  });
  window.vueApp = app.mount('#app');
}