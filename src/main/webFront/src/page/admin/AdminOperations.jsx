import './AdminOperations.css';
import {useEffect, useRef, useState} from 'react';
import AdminLayout from '../../components/admin/AdminLayout.jsx';
import {Button2} from '../../components/common/Button.jsx';
import axios from '../../context/axiosInstance.js';
import dayjs from 'dayjs';

const STATUS_POLL_INTERVAL_MS = 2000;
const STATUS_POLL_MAX_WAIT_MS = 2 * 60 * 1000; // 2분

const formatDateTime = (value) => value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

const statusClassName = (status) => {
  if (status === 'COMPLETED') return 'admin-operations-status-success';
  if (status === 'FAILED') return 'admin-operations-status-failed';
  return 'admin-operations-status-running';
};

const AdminOperations = () => {
  const [status, setStatus] = useState(null);
  const [isTriggering, setIsTriggering] = useState(false);
  const pollTimeoutRef = useRef(null);

  const fetchStatus = async () => {
    try {
      const res = await axios.get('/api/admin/batch/statistic');
      setStatus(res.data);
      return res.data;
    } catch (e) {
      console.log(e);
      return null;
    }
  };

  const stopPolling = () => {
    if (pollTimeoutRef.current) {
      clearTimeout(pollTimeoutRef.current);
      pollTimeoutRef.current = null;
    }
  };

  const pollUntilFinished = (startedAt) => {
    const check = async () => {
      const latest = await fetchStatus();

      if (!latest || !latest.running || Date.now() - startedAt > STATUS_POLL_MAX_WAIT_MS) {
        setIsTriggering(false);
        return;
      }

      pollTimeoutRef.current = setTimeout(check, STATUS_POLL_INTERVAL_MS);
    };

    check();
  };

  const onClickRun = async () => {
    if (isTriggering || status?.running) return;

    setIsTriggering(true);
    try {
      await axios.post('/api/admin/batch/statistic/run');
      pollUntilFinished(Date.now());
    } catch (e) {
      setIsTriggering(false);
      const message = e.response?.data?.message || '통계 Batch 실행에 실패했습니다.';
      alert(message);
    }
  };

  useEffect(() => {
    fetchStatus();

    return () => stopPolling();
  }, []);

  const running = status?.running || isTriggering;

  return (
      <AdminLayout active={"operations"}>
        <h1 className="admin-operations-title">Operations</h1>

        <div className="admin-operations-card">
          <div className="admin-operations-card-title">통계 Batch</div>

          {status && (
              <>
                <div className="admin-operations-row">
                  <span className="admin-operations-row-label">상태</span>
                  <span className={statusClassName(running ? 'RUNNING' : status.status)}>
                    {running ? 'RUNNING' : (status.status || '-')}
                  </span>
                </div>
                <div className="admin-operations-row">
                  <span className="admin-operations-row-label">최근 실행</span>
                  <span>{formatDateTime(status.startTime)}</span>
                </div>
                <div className="admin-operations-row">
                  <span className="admin-operations-row-label">종료 시간</span>
                  <span>{formatDateTime(status.endTime)}</span>
                </div>
                <div className="admin-operations-row">
                  <span className="admin-operations-row-label">소요 시간</span>
                  <span>{status.durationSeconds != null ? `${status.durationSeconds}초` : '-'}</span>
                </div>
              </>
          )}

          <div className="admin-operations-run-button">
            <Button2
                buttonText={running ? '실행 중...' : '통계 Batch 실행'}
                buttonEvent={onClickRun}
                disabled={running}
            />
          </div>
        </div>
      </AdminLayout>
  );
};

export default AdminOperations;
