import './AdminDashboard.css';
import {useEffect, useState} from 'react';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';

const AdminDashboard = () => {
  const [dashboard, setDashboard] = useState(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await axios.get('/api/admin/dashboard');
        setDashboard(res.data);
      } catch (e) {
        console.log(e);
      }
    };

    fetchDashboard();
  }, []);

  return (
      <AdminLayout active={"dashboard"}>
        <h1 className="admin-dashboard-title">Dashboard</h1>

        {dashboard && (
            <>
              <div className="admin-dashboard-cards">
                <div className="admin-dashboard-card">
                  <div className="admin-dashboard-card-label">전체 사용자</div>
                  <div className="admin-dashboard-card-value">{dashboard.totalUserCount}</div>
                </div>
                <div className="admin-dashboard-card">
                  <div className="admin-dashboard-card-label">USER</div>
                  <div className="admin-dashboard-card-value">{dashboard.userCount}</div>
                </div>
                <div className="admin-dashboard-card">
                  <div className="admin-dashboard-card-label">AI_USER</div>
                  <div className="admin-dashboard-card-value">{dashboard.aiUserCount}</div>
                </div>
                <div className="admin-dashboard-card">
                  <div className="admin-dashboard-card-label">ADMIN</div>
                  <div className="admin-dashboard-card-value">{dashboard.adminCount}</div>
                </div>
                <div className="admin-dashboard-card">
                  <div className="admin-dashboard-card-label">게시글</div>
                  <div className="admin-dashboard-card-value">{dashboard.totalPostCount}</div>
                </div>
              </div>

              <div className="admin-dashboard-section-title">최근 가입 사용자</div>
              <table className="admin-dashboard-table">
                <thead>
                <tr>
                  <th>사용자</th>
                  <th>이메일</th>
                  <th>Role</th>
                  <th>가입일</th>
                </tr>
                </thead>
                <tbody>
                {dashboard.recentUsers.map((u) => (
                    <tr key={u.userId}>
                      <td>{u.nickname}</td>
                      <td>{u.username}</td>
                      <td>{u.role}</td>
                      <td>{getKorDate(u.createdAt)}</td>
                    </tr>
                ))}
                </tbody>
              </table>
            </>
        )}
      </AdminLayout>
  );
};

export default AdminDashboard;
