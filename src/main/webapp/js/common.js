window.App = (function () {
  const http = axios.create({ timeout: 15000 });

  http.interceptors.response.use(function (res) {
    return res;
  }, function (err) {
    return Promise.reject(err);
  });

  function unwrap(res) {
    const data = res.data;
    if (data && data.code === 0) {
      return data.data;
    }
    const msg = (data && data.msg) || '请求失败';
    if (data && data.code === 401) {
      location.href = 'login.html';
    }
    throw new Error(msg);
  }

  function get(url, params) {
    return http.get(url, { params: params }).then(unwrap);
  }

  function post(url, body) {
    return http.post(url, body).then(unwrap);
  }

  function fmt(v) {
    if (!v) return '-';
    return String(v).replace('T', ' ');
  }

  function roleCards(role) {
    const all = {
      userQuery: { key: 'userQuery', icon: '👤', title: '用户信息查询', desc: '按关键字模糊查询用户' },
      loanQuery: { key: 'loanQuery', icon: '📋', title: '借阅记录查询', desc: '关键字 / 超期 / 日期范围' },
      loanStat: { key: 'loanStat', icon: '📊', title: '借阅数据统计', desc: '按类型、按专业统计' },
      borrow: { key: 'borrow', icon: '📚', title: '借书', desc: '选择图书借阅' },
      return: { key: 'return', icon: '↩️', title: '还书', desc: '按副本归还' },
      reviewRecommend: { key: 'reviewRecommend', icon: '✅', title: '审阅推荐图书', desc: '通过入库或驳回' },
      addBook: { key: 'addBook', icon: '➕', title: '添加书籍', desc: '新增图书并自动生成副本' },
      listBook: { key: 'listBook', icon: '🔍', title: '查看所有书籍', desc: '浏览与模糊查询' },
      discardBook: { key: 'discardBook', icon: '🗑️', title: '废弃图书', desc: '无外借副本才可废弃' },
      editBook: { key: 'editBook', icon: '✏️', title: '编辑图书', desc: '更新书名作者等字段' },
      editCopy: { key: 'editCopy', icon: '📦', title: '图书副本编辑', desc: '新增或删除副本' },
      myLoan: { key: 'myLoan', icon: '🧾', title: '个人借阅查询', desc: '全部 / 未还 / 日期范围' },
      recommend: { key: 'recommend', icon: '💡', title: '推荐图书', desc: '馆内已有则提示，否则提交审核' }
    };
    const map = {
      '管理员': ['userQuery', 'loanQuery', 'loanStat', 'borrow', 'return', 'reviewRecommend'],
      '学生': ['borrow', 'return', 'myLoan'],
      '操作员': ['addBook', 'listBook', 'discardBook', 'editBook', 'editCopy', 'borrow', 'return', 'myLoan'],
      '教师': ['borrow', 'return', 'myLoan', 'recommend']
    };
    return (map[role] || []).map(function (k) { return all[k]; });
  }

  return { get: get, post: post, fmt: fmt, roleCards: roleCards };
})();
