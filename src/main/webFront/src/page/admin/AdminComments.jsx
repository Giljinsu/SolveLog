import './AdminComments.css';
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';
import {usePopup} from '../../context/PopupContext.jsx';

const PAGE_SIZE = 10;

const AdminComments = () => {
  const [contentInput, setContentInput] = useState('');
  const [authorInput, setAuthorInput] = useState('');
  const [content, setContent] = useState('');
  const [author, setAuthor] = useState('');
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState(null);
  const nav = useNavigate();
  const confirm = usePopup();

  const fetchComments = async () => {
    try {
      const res = await axios.get('/api/admin/comments', {
        params: {
          content: content || undefined,
          author: author || undefined,
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
    fetchComments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, content, author]);

  const onSubmitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setContent(contentInput.trim());
    setAuthor(authorInput.trim());
  };

  const onView = (c) => {
    nav(`/post/${c.postId}/${c.postTitle}#comment${c.commentId}`);
  };

  const onDelete = async (c) => {
    const body = c.isReply
        ? '답글을 삭제하시겠습니까? 삭제된 댓글은 복구할 수 없습니다.'
        : '댓글을 삭제하시겠습니까? 답글이 있는 경우 함께 삭제됩니다.';

    const confirmed = await confirm({
      header: '댓글 삭제',
      body,
      leftButtonText: '취소',
      rightButtonText: '삭제',
    });
    if (!confirmed) return;

    try {
      await axios.delete(`/api/admin/comments/${c.commentId}`);

      const isLastItemOnPage = pageData?.content?.length === 1 && page > 0;
      if (isLastItemOnPage) {
        setPage(page - 1);
      } else {
        fetchComments();
      }
    } catch {
      alert('댓글을 삭제하는데 문제가 발생했습니다.');
    }
  };

  const totalPages = pageData?.totalPages ?? 0;

  return (
      <AdminLayout active={"comments"}>
        <h1 className="admin-comments-title">Comments</h1>

        <form className="admin-comments-search" onSubmit={onSubmitSearch}>
          <input
              type="text"
              placeholder="내용 검색"
              value={contentInput}
              onChange={(e) => setContentInput(e.target.value)}
          />
          <input
              type="text"
              placeholder="작성자 검색"
              value={authorInput}
              onChange={(e) => setAuthorInput(e.target.value)}
          />
          <button type="submit" className="thumbnail-button">검색</button>
        </form>

        <table className="admin-comments-table">
          <thead>
          <tr>
            <th>내용</th>
            <th>작성자</th>
            <th>게시글</th>
            <th>구분</th>
            <th>작성일</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {pageData?.content?.map((c) => (
              <tr key={c.commentId}>
                <td className="admin-comments-content-cell">{c.content}</td>
                <td>
                  {c.nickname}
                  {c.authorDeleted && <span className="admin-comments-deleted-hint"> (탈퇴)</span>}
                </td>
                <td className="admin-comments-post-cell">{c.postTitle}</td>
                <td>{c.isReply ? '답글' : '댓글'}</td>
                <td>{getKorDate(c.createdAt)}</td>
                <td className="admin-comments-actions">
                  <button onClick={() => onView(c)}>보기</button>
                  <button onClick={() => onDelete(c)}>삭제</button>
                </td>
              </tr>
          ))}
          </tbody>
        </table>

        {totalPages > 1 && (
            <div className="admin-comments-pagination">
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

export default AdminComments;
