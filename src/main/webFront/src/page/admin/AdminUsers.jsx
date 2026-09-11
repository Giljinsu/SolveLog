import './AdminUsers.css';
import {useEffect, useState} from 'react';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import AdminPagination from '../../components/admin/AdminPagination.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';
import {useAuth} from '../../context/AuthContext.jsx';
import {usePopup} from '../../context/PopupContext.jsx';

const ROLE_OPTIONS = ['USER', 'AI_USER', 'ADMIN'];
const PAGE_SIZE = 10;

const AdminUsers = () => {
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState(null);
  const {user} = useAuth();
  const confirm = usePopup();

  const fetchUsers = async () => {
    try {
      const res = await axios.get('/api/admin/users', {
        params: {
          keyword: searchKeyword || undefined,
          page,
          size: PAGE_SIZE,
        },
      });
      setPageData(res.data);
    } catch (e) {
      console.log(e);
    }
  };

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, searchKeyword]);

  const onSubmitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setSearchKeyword(keyword.trim());
  };

  const onChangeRole = async (targetUser, newRole) => {
    if (newRole === targetUser.role) return;

    const confirmed = await confirm({
      header: 'Role 변경',
      body: `${targetUser.username}의 Role을 ${targetUser.role} → ${newRole}로 변경하시겠습니까?`,
      leftButtonText: '취소',
      rightButtonText: '변경',
    });
    if (!confirmed) return;

    try {
      await axios.patch(`/api/admin/users/${targetUser.userId}/role`, {role: newRole});
      fetchUsers();
    } catch (e) {
      const message = e.response?.data?.message || 'Role 변경에 실패했습니다.';
      alert(message);
    }
  };

  const totalPages = pageData?.totalPages ?? 0;

  return (
      <AdminLayout active={"users"}>
        <h1 className="admin-users-title">Users</h1>

        <form className="admin-users-search" onSubmit={onSubmitSearch}>
          <input
              type="text"
              placeholder="username 또는 nickname 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit" className="thumbnail-button">검색</button>
        </form>

        <table className="admin-users-table">
          <thead>
          <tr>
            <th>사용자</th>
            <th>이메일</th>
            <th>Role</th>
            <th>가입일</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {pageData?.content?.map((u) => {
            const isSelf = user?.username === u.username;

            return (
                <tr key={u.userId}>
                  <td>{u.nickname}</td>
                  <td>{u.username}</td>
                  <td>{u.role}</td>
                  <td>{getKorDate(u.createdAt)}</td>
                  <td>
                    {isSelf ? (
                        <span className="admin-users-self-hint">본인 계정</span>
                    ) : (
                        <select
                            value={u.role}
                            onChange={(e) => onChangeRole(u, e.target.value)}
                        >
                          {ROLE_OPTIONS.map((role) => (
                              <option key={role} value={role}>{role}</option>
                          ))}
                        </select>
                    )}
                  </td>
                </tr>
            );
          })}
          </tbody>
        </table>

        <AdminPagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </AdminLayout>
  );
};

export default AdminUsers;
