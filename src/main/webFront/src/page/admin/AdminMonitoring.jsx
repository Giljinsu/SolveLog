import './AdminMonitoring.css';
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import AdminPagination from '../../components/admin/AdminPagination.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';

const PAGE_SIZE = 10;

const AdminMonitoring = () => {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState(null);
  const nav = useNavigate();

  const fetchIssues = async () => {
    try {
      const res = await axios.get('/api/admin/monitoring/issues', {
        params: {page, size: PAGE_SIZE},
      });
      setPageData(res.data);
    } catch (e) {
      console.log(e);
    }
  };

  useEffect(() => {
    fetchIssues();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const totalPages = pageData?.totalPages ?? 0;

  return (
      <AdminLayout active={"monitoring"}>
        <h1 className="admin-monitoring-title">Monitoring</h1>

        <table className="admin-monitoring-table">
          <thead>
          <tr>
            <th>상태</th>
            <th>Severity</th>
            <th>Exception</th>
            <th>Message</th>
            <th>발생 횟수</th>
            <th>최초 발생</th>
            <th>최근 발생</th>
            <th>AI 상태</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {pageData?.content?.map((issue) => (
              <tr key={issue.issueId}>
                <td>
                  <span className={`admin-monitoring-badge status-${issue.status?.toLowerCase()}`}>
                    {issue.status}
                  </span>
                </td>
                <td>
                  <span className={`admin-monitoring-badge severity-${issue.severity?.toLowerCase()}`}>
                    {issue.severity}
                  </span>
                </td>
                <td className="admin-monitoring-exception-cell">{issue.exceptionClass}</td>
                <td className="admin-monitoring-message-cell">{issue.representativeMessage}</td>
                <td>{issue.occurrenceCount}</td>
                <td>{getKorDate(issue.firstOccurredAt)}</td>
                <td>{getKorDate(issue.lastOccurredAt)}</td>
                <td>
                  <span className={`admin-monitoring-badge ai-${issue.aiAnalysisStatus?.toLowerCase()}`}>
                    {issue.aiAnalysisStatus}
                  </span>
                </td>
                <td>
                  <button onClick={() => nav(`/admin/monitoring/${issue.issueId}`)}>상세</button>
                </td>
              </tr>
          ))}
          {pageData?.content?.length === 0 && (
              <tr>
                <td colSpan={9} className="admin-monitoring-empty">기록된 오류가 없습니다.</td>
              </tr>
          )}
          </tbody>
        </table>

        <AdminPagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </AdminLayout>
  );
};

export default AdminMonitoring;
