import './AdminPosts.css';
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';
import {usePopup} from '../../context/PopupContext.jsx';

const PAGE_SIZE = 10;

const AdminPosts = () => {
  const [titleInput, setTitleInput] = useState('');
  const [authorInput, setAuthorInput] = useState('');
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState(null);
  const nav = useNavigate();
  const confirm = usePopup();

  const fetchPosts = async (targetPage = page) => {
    try {
      const res = await axios.get('/api/admin/posts', {
        params: {
          title: title || undefined,
          author: author || undefined,
          page: targetPage,
          size: PAGE_SIZE,
        },
      });
      setPageData(res.data);
    } catch (e) {
      console.log(e);
    }
  };

  useEffect(() => {
    fetchPosts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, title, author]);

  const onSubmitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setTitle(titleInput.trim());
    setAuthor(authorInput.trim());
  };

  const onView = (post) => {
    nav(`/post/${post.postId}/${post.title}`);
  };

  const onDelete = async (post) => {
    const confirmed = await confirm({
      header: '게시글 삭제',
      body: `"${post.title}" 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.`,
      leftButtonText: '취소',
      rightButtonText: '삭제',
    });
    if (!confirmed) return;

    try {
      await axios.delete(`/api/admin/posts/${post.postId}`);

      const isLastItemOnPage = pageData?.content?.length === 1 && page > 0;
      if (isLastItemOnPage) {
        setPage(page - 1);
      } else {
        fetchPosts();
      }
    } catch {
      alert('게시글을 삭제하는데 문제가 발생했습니다.');
    }
  };

  const totalPages = pageData?.totalPages ?? 0;

  return (
      <AdminLayout active={"posts"}>
        <h1 className="admin-posts-title">Posts</h1>

        <form className="admin-posts-search" onSubmit={onSubmitSearch}>
          <input
              type="text"
              placeholder="제목 검색"
              value={titleInput}
              onChange={(e) => setTitleInput(e.target.value)}
          />
          <input
              type="text"
              placeholder="작성자 검색"
              value={authorInput}
              onChange={(e) => setAuthorInput(e.target.value)}
          />
          <button type="submit" className="thumbnail-button">검색</button>
        </form>

        <table className="admin-posts-table">
          <thead>
          <tr>
            <th>제목</th>
            <th>작성자</th>
            <th>상태</th>
            <th>조회수</th>
            <th>작성일</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {pageData?.content?.map((p) => (
              <tr key={p.postId}>
                <td className="admin-posts-title-cell">{p.title}</td>
                <td>
                  {p.nickname}
                  {p.authorDeleted && <span className="admin-posts-deleted-hint"> (탈퇴)</span>}
                </td>
                <td>{p.isTemp ? '임시글' : '게시됨'}</td>
                <td>{p.viewCount}</td>
                <td>{getKorDate(p.createdAt)}</td>
                <td className="admin-posts-actions">
                  <button onClick={() => onView(p)}>보기</button>
                  <button onClick={() => onDelete(p)}>삭제</button>
                </td>
              </tr>
          ))}
          </tbody>
        </table>

        {totalPages > 1 && (
            <div className="admin-posts-pagination">
              <button disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
              {Array.from({length: totalPages}, (_, i) => i).map((p) => (
                  <button
                      key={p}
                      className={p === page ? 'active' : ''}
                      onClick={() => setPage(p)}
                  >
                    {p + 1}
                  </button>
              ))}
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
            </div>
        )}
      </AdminLayout>
  );
};

export default AdminPosts;
