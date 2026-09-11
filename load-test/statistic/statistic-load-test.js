// SolveLog 통계 API (GET /api/statistic/getUserStatistic) Before/After 부하테스트
//
// 실행 예:
//   BASE_URL=http://localhost:8080 STAGE=10vu STAGE_VUS=10 STAGE_DURATION=1m MODE=before \
//     k6 run statistic-load-test.js
//
// 환경변수:
//   BASE_URL        대상 서버 (기본 http://localhost:8080)
//   MODE            before | after | cache-miss (결과 라벨링용)
//   STAGE           결과 파일명에 쓰일 단계 이름 (warmup, 10vu, 50vu, 100vu, 200vu)
//   STAGE_VUS       본 단계 목표 VU 수
//   STAGE_DURATION  본 단계 유지 시간 (예: 1m, 2m)
//   RAMP            목표 VU까지 올리고 내리는 시간 (기본 5s)
//   OUT_DIR         결과 json 저장 디렉터리 (기본 ./results)

import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Trend, Rate, Counter } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MODE = __ENV.MODE || 'unspecified';
const STAGE = __ENV.STAGE || 'unspecified';
const STAGE_VUS = parseInt(__ENV.STAGE_VUS || '10', 10);
const STAGE_DURATION = __ENV.STAGE_DURATION || '30s';
const RAMP = __ENV.RAMP || '5s';
const OUT_DIR = __ENV.OUT_DIR || './results';

const LOGIN_USERNAME = 'loadtest_runner';
const LOGIN_PASSWORD = 'loadtest1234!';

// 프론트(MyPageStatistic.jsx)와 동일하게 '전체 글'일 때만 categoryType 생략
const CATEGORY_TYPES = [undefined, '문제풀이', '자유게시판', '학습기록'];

const users = new SharedArray('loadtest users', function () {
  return JSON.parse(open('./users.json'));
});

// 응답 스키마 검증 실패, 통계 API 고유 실패율을 별도로도 집계
const statSchemaFailRate = new Rate('statistic_schema_fail_rate');
const statDuration = new Trend('statistic_req_duration', true);

export const options = {
  scenarios: {
    main: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { target: STAGE_VUS, duration: RAMP },
        { target: STAGE_VUS, duration: STAGE_DURATION },
        { target: 0, duration: RAMP },
      ],
      gracefulRampDown: '5s',
    },
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/login`,
    JSON.stringify({ username: LOGIN_USERNAME, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status !== 200) {
    throw new Error(
      `setup 로그인 실패 (status=${loginRes.status}, body=${loginRes.body}). ` +
      `loadtest_runner 계정이 시드되어 있는지 확인하세요.`
    );
  }

  const setCookieHeaders = loginRes.headers['Set-Cookie'];
  const rawCookies = Array.isArray(setCookieHeaders) ? setCookieHeaders : [setCookieHeaders];
  const cookiePairs = rawCookies
    .filter(Boolean)
    .map((c) => c.split(';')[0]);

  const cookieHeader = cookiePairs.join('; ');

  if (!cookieHeader.includes('accessToken=')) {
    throw new Error('setup: accessToken 쿠키를 로그인 응답에서 찾지 못했습니다.');
  }

  return { cookieHeader };
}

export default function (data) {
  const userIdx = (__VU + __ITER) % users.length;
  const user = users[userIdx];
  const categoryType = CATEGORY_TYPES[(__VU + __ITER) % CATEGORY_TYPES.length];

  const today = new Date().toISOString().slice(0, 10);
  const params = {
    username: user.username,
    currentDate: today,
    year: new Date().getFullYear(),
  };
  if (categoryType) params.categoryType = categoryType;

  const query = Object.entries(params)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');

  const res = http.get(`${BASE_URL}/api/statistic/getUserStatistic?${query}`, {
    headers: { Cookie: data.cookieHeader },
    tags: { name: 'getUserStatistic' },
  });

  statDuration.add(res.timings.duration);

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'has valid statistic shape': (r) => {
      if (r.status !== 200) return false;
      try {
        const body = JSON.parse(r.body);
        return (
          typeof body.totalCount !== 'undefined' &&
          Array.isArray(body.dailyStatistic) &&
          Array.isArray(body.categoryStatistic) &&
          Array.isArray(body.tagStatistic)
        );
      } catch (e) {
        return false;
      }
    },
  });

  statSchemaFailRate.add(!ok);

  sleep(0.2);
}

export function handleSummary(data) {
  const fileName = `${OUT_DIR}/${MODE}-${STAGE}.json`;
  const stdoutName = `${OUT_DIR}/${MODE}-${STAGE}.summary.txt`;

  const metrics = data.metrics;
  const compact = {
    mode: MODE,
    stage: STAGE,
    target_vus: STAGE_VUS,
    stage_duration: STAGE_DURATION,
    generated_at: new Date().toISOString(),
    http_req_duration_avg: metrics.http_req_duration ? metrics.http_req_duration.values.avg : null,
    http_req_duration_p95: metrics.http_req_duration ? metrics.http_req_duration.values['p(95)'] : null,
    http_req_duration_p99: metrics.http_req_duration ? metrics.http_req_duration.values['p(99)'] : null,
    http_req_failed_rate: metrics.http_req_failed ? metrics.http_req_failed.values.rate : null,
    http_reqs_count: metrics.http_reqs ? metrics.http_reqs.values.count : null,
    http_reqs_rate: metrics.http_reqs ? metrics.http_reqs.values.rate : null,
    iterations_count: metrics.iterations ? metrics.iterations.values.count : null,
    vus_max: metrics.vus_max ? metrics.vus_max.values.max : null,
    statistic_schema_fail_rate: metrics.statistic_schema_fail_rate
      ? metrics.statistic_schema_fail_rate.values.rate
      : null,
    test_run_duration_ms: data.state.testRunDurationMs,
    raw_metrics: metrics,
  };

  const result = {};
  result[fileName] = JSON.stringify(compact, null, 2);
  result[stdoutName] = textSummary(data, { indent: ' ', enableColors: false });
  result['stdout'] = textSummary(data, { indent: ' ', enableColors: true });
  return result;
}
