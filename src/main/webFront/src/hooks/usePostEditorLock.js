import {useCallback, useEffect, useRef, useState} from 'react';

// 동일 브라우저에서 게시글 작성 화면을 동시에 하나의 탭에서만 사용하도록 하는 lock.
// localStorage 기반이라 완벽한 원자적 락은 아니지만(TOCTOU 가능성 있음),
// temp file(username + postId=null) 충돌을 줄이는 목적의 MVP 수준 구현이다.

const LOCK_KEY = 'postEditorLock';
const TAB_ID_KEY = 'postEditorTabId';

const HEARTBEAT_INTERVAL_MS = 10000; // 10초
const STALE_TIMEOUT_MS = 60000; // 60초 - 백그라운드 탭 timer throttling을 감안해 여유 있게 설정

// 같은 탭 새로고침에는 동일 tabId가 유지되고, 새 탭은 sessionStorage가 비어있어 새 tabId가 발급된다.
const getTabId = () => {
  let tabId = sessionStorage.getItem(TAB_ID_KEY);
  if (!tabId) {
    tabId = crypto.randomUUID();
    sessionStorage.setItem(TAB_ID_KEY, tabId);
  }
  return tabId;
};

const readLock = () => {
  const raw = localStorage.getItem(LOCK_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

const writeLock = (tabId) => {
  localStorage.setItem(LOCK_KEY, JSON.stringify({tabId, timestamp: Date.now()}));
};

const isStale = (lock) => !lock || (Date.now() - lock.timestamp > STALE_TIMEOUT_MS);

// lock이 없거나, 이미 내 소유이거나, stale이면 (재)획득하고 true를 반환한다.
// 다른 탭이 유효하게 소유 중이면 false.
const tryAcquireLock = (tabId) => {
  const lock = readLock();

  if (!lock || lock.tabId === tabId || isStale(lock)) {
    writeLock(tabId);
    return true;
  }

  return false;
};

// 현재 저장된 lock의 tabId가 자신과 같을 때만 제거한다.
// (다른 탭이 이미 새로 획득한 lock을 늦은 cleanup이 지우는 race 방지)
const releaseLockIfOwned = (tabId) => {
  const lock = readLock();
  if (lock && lock.tabId === tabId) {
    localStorage.removeItem(LOCK_KEY);
  }
};

const usePostEditorLock = () => {
  const tabIdRef = useRef(null);
  if (tabIdRef.current === null) {
    tabIdRef.current = getTabId();
  }

  const [isLockOwner, setIsLockOwner] = useState(false);
  const heartbeatIntervalRef = useRef(null);

  const stopHeartbeat = useCallback(() => {
    if (heartbeatIntervalRef.current) {
      clearInterval(heartbeatIntervalRef.current);
      heartbeatIntervalRef.current = null;
    }
  }, []);

  const startHeartbeat = useCallback(() => {
    if (heartbeatIntervalRef.current) return;
    heartbeatIntervalRef.current = setInterval(() => {
      writeLock(tabIdRef.current);
    }, HEARTBEAT_INTERVAL_MS);
  }, []);

  // mount 시 최초 획득 시도 + "다시 확인" 버튼에서도 재사용
  const acquire = useCallback(() => {
    const acquired = tryAcquireLock(tabIdRef.current);
    setIsLockOwner(acquired);

    if (acquired) {
      startHeartbeat();
    } else {
      stopHeartbeat();
    }

    return acquired;
  }, [startHeartbeat, stopHeartbeat]);

  useEffect(() => {
    acquire();

    const tabId = tabIdRef.current;

    // preventDefault/returnValue를 설정하지 않는다 - 브라우저 native 이탈 확인창을 띄우지 않고
    // lock 정리만 조용히 수행한다.
    const handleBeforeUnload = () => {
      releaseLockIfOwned(tabId);
    };
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
      stopHeartbeat();
      releaseLockIfOwned(tabId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return {isLockOwner, retryLock: acquire};
};

export default usePostEditorLock;
