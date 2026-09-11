import './AdminMonitoringDetail.css';
import {Fragment, useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import AdminPagination from '../../components/admin/AdminPagination.jsx';
import axios from '../../context/axiosInstance.js';
import {getKorDate} from '../../utils/DateUtils.js';

const PAGE_SIZE = 10;

const AI_STATUS_LABEL = {
  PENDING: '분석 대기 중',
  DONE: '분석 완료',
  FAILED: '분석 실패',
  SKIPPED: '분석 건너뜀',
};

const AdminMonitoringDetail = () => {
  const {issueId} = useParams();
  const nav = useNavigate();
  const [issue, setIssue] = useState(null);
  const [eventPage, setEventPage] = useState(0);
  const [eventPageData, setEventPageData] = useState(null);
  const [expandedEventId, setExpandedEventId] = useState(null);

  const fetchIssue = async () => {
    try {
      const res = await axios.get(`/api/admin/monitoring/issues/${issueId}`);
      setIssue(res.data);
    } catch (e) {
      console.log(e);
    }
  };

  const fetchEvents = async () => {
    try {
      const res = await axios.get(`/api/admin/monitoring/issues/${issueId}/events`, {
        params: {page: eventPage, size: PAGE_SIZE},
      });
      setEventPageData(res.data);
    } catch (e) {
      console.log(e);
    }
  };

  useEffect(() => {
    fetchIssue();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [issueId]);

  useEffect(() => {
    fetchEvents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [issueId, eventPage]);

  const totalPages = eventPageData?.totalPages ?? 0;

  if (!issue) {
    return (
        <AdminLayout active={"monitoring"}>
          <p>불러오는 중...</p>
        </AdminLayout>
    );
  }

  return (
      <AdminLayout active={"monitoring"}>
        <button className="admin-monitoring-back" onClick={() => nav('/admin/monitoring')}>
          ← 목록으로
        </button>

        <h1 className="admin-monitoring-detail-title">{issue.exceptionClass}</h1>
        <p className="admin-monitoring-detail-message">{issue.representativeMessage}</p>

        <div className="admin-monitoring-detail-meta">
          <div><span>상태</span>{issue.status}</div>
          <div><span>Severity</span>{issue.severity}</div>
          <div><span>발생 횟수</span>{issue.occurrenceCount}</div>
          <div><span>최초 발생</span>{getKorDate(issue.firstOccurredAt)}</div>
          <div><span>최근 발생</span>{getKorDate(issue.lastOccurredAt)}</div>
          <div><span>AI 상태</span>{AI_STATUS_LABEL[issue.aiAnalysisStatus] ?? issue.aiAnalysisStatus}</div>
        </div>

        <section className="admin-monitoring-ai-section">
          <h2>AI 분석 결과</h2>
          {issue.aiAnalysisStatus === 'DONE' ? (
              <div className="admin-monitoring-ai-content">
                <div><h3>Summary</h3><p>{issue.aiSummary}</p></div>
                <div><h3>Possible Cause</h3><p>{issue.aiPossibleCause}</p></div>
                <div><h3>Impact</h3><p>{issue.aiImpact}</p></div>
                <div><h3>Solution</h3><p>{issue.aiSolution}</p></div>
                <div>
                  <h3>Check Points</h3>
                  <ul>
                    {issue.aiCheckPoints?.map((point, idx) => <li key={idx}>{point}</li>)}
                  </ul>
                </div>
                <p className="admin-monitoring-analyzed-at">분석 시각: {getKorDate(issue.analyzedAt)}</p>
              </div>
          ) : (
              <p className="admin-monitoring-ai-empty">
                {AI_STATUS_LABEL[issue.aiAnalysisStatus] ?? issue.aiAnalysisStatus}
              </p>
          )}
        </section>

        <section>
          <h2>최근 ErrorEvent</h2>
          <table className="admin-monitoring-event-table">
            <thead>
            <tr>
              <th>발생 시각</th>
              <th>Method</th>
              <th>URI</th>
              <th>Status</th>
              <th>traceId</th>
              <th>사용자</th>
              <th>Stack Trace</th>
            </tr>
            </thead>
            <tbody>
            {eventPageData?.content?.map((event) => (
                <Fragment key={event.eventId}>
                  <tr>
                    <td>{getKorDate(event.occurredAt)}</td>
                    <td>{event.httpMethod ?? '-'}</td>
                    <td className="admin-monitoring-event-uri">{event.requestUri ?? '-'}</td>
                    <td>{event.httpStatus ?? '-'}</td>
                    <td className="admin-monitoring-event-trace">{event.traceId ?? '-'}</td>
                    <td>{event.username ?? '-'}</td>
                    <td>
                      <button
                          onClick={() => setExpandedEventId(
                              expandedEventId === event.eventId ? null : event.eventId)}
                      >
                        {expandedEventId === event.eventId ? '접기' : '보기'}
                      </button>
                    </td>
                  </tr>
                  {expandedEventId === event.eventId && (
                      <tr>
                        <td colSpan={7}>
                          <pre className="admin-monitoring-stack-trace">{event.stackTraceExcerpt}</pre>
                        </td>
                      </tr>
                  )}
                </Fragment>
            ))}
            </tbody>
          </table>

          <AdminPagination page={eventPage} totalPages={totalPages} onPageChange={setEventPage} />
        </section>
      </AdminLayout>
  );
};

export default AdminMonitoringDetail;
