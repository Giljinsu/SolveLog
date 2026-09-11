import axios from "axios";
import {useLoadingStore} from "../hooks/useLoadingStore.js";

let logoutCallback = null;

export const registerLogout = (fn) => {
  logoutCallback = fn;
}

// 여러 API가 거의 동시에 401을 받아도 /api/refresh 네트워크 요청은 한 번만 나가도록 하는
// single-flight promise. 진행 중인 refresh가 있으면 새로 요청을 만들지 않고 이 Promise를
// 그대로 공유해서 기다린다.
let refreshPromise = null;

const noLoadingApi = [
    "/api/me",
    "/api/refresh",
    "/api/getAlarmList",
    "/api/thumbnail-jobs" // AI 썸네일 생성 job polling 중 전역 로딩 오버레이가 깜빡이지 않도록 제외
];

const showLoading = (url) => {
  return !noLoadingApi.some((api) => url.includes(api))
}

// 1. Axios 인스턴스 생성
const instance = axios.create({
  // baseURL: "http://localhost:8080", // API 서버 주소
  // baseURL: "https://www.solvelog.site", // API 서버 주소
  baseURL: `${import.meta.env.VITE_API_BASE_URL}`,
  withCredentials: true // 쿠키 사용 시 필요
})

// 2. 요청 인터셉터 (Authorization 헤더 자동 설정)
instance.interceptors.request.use(
    (config) => {

      if (showLoading(config.url)) {
        useLoadingStore.getState().setLoading(true);
      }
      // const accessToken = localStorage.getItem("accessToken");
      // if (accessToken) {
        // config.headers.Authorization = `Bearer ${accessToken}`
      // }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
)

// 3, 응답 인터셉터 (AccessToken 만료 시 자동 재발급)
instance.interceptors.response.use(
    (response) => {
      if (showLoading(response.config?.url)) {
        useLoadingStore.getState().setLoading(false);
      }
      return response
    },
    async (error) => {
      const originalRequest = error.config;
      const {setLoading} = useLoadingStore.getState();

      //access token 만료
      if(
          error.response?.status === 401 &&
          !originalRequest._retry &&//무한루프 방지
          !originalRequest.url.includes("/api/refresh")
      ) {
        originalRequest._retry = true;

        try {
          // 이미 진행 중인 refresh가 있으면 새로 호출하지 않고 그 결과를 공유해서 기다린다
          // (여러 API가 동시에 401을 받아도 /api/refresh는 한 번만 나간다).
          if (!refreshPromise) {
            refreshPromise = instance.post("/api/refresh", {})
                .finally(() => {
                  refreshPromise = null;
                });
          }

          await refreshPromise;

          if (showLoading(error.config?.url)){
            setLoading(false);
          }
          return instance(originalRequest);
        } catch (refreshError) {
          // refresh 자체가 실패한 이유를 구분한다.
          // - 401(INVALID_TOKEN): refresh token 자체가 없거나 만료/위조 - 진짜 로그인 만료이므로 로그아웃 처리
          // - 그 외(5xx, 네트워크 오류 등 응답이 없는 경우): 일시적 오류일 수 있으므로 강제 로그아웃하지 않고
          //   호출한 쪽에 오류만 전달한다.
          const refreshStatus = refreshError.response?.status;

          if (refreshStatus === 401) {
            if (logoutCallback) {
              logoutCallback();
            }
          }

          if (showLoading(error.config?.url)){
            setLoading(false);
          }
          return Promise.reject(refreshError);
        }
      }

      if (showLoading(error.config?.url)){
        setLoading(false);
      }
      return Promise.reject(error)
    }

)

export default instance;