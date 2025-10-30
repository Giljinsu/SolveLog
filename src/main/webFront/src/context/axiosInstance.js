import axios from "axios";
import {useLoadingStore} from "../hooks/useLoadingStore.js";

let logoutCallback = null;

export const registerLogout = (fn) => {
  logoutCallback = fn;
}

const noLoadingApi = [
    "/api/me",
    "/api/refresh",
    "/api/getAlarmList"
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
          !originalRequest.url.includes("/api/refresh") && // refresh 무한루프 방지
          document.cookie.includes("refreshToken")
      ) {
        originalRequest._retry = true;

        try {
          // const refreshToken = localStorage.getItem("refreshToken");

          const res = await instance.post(
              "/api/refresh",
              {},
              // {
              //   headers: {
              //     Authorization: `Bearer ${refreshToken}`,
              //   },
              // }
          );

          // const newAccessToken = res.data.accessToken;
          // const newRefreshToken = res.data.refreshToken;

          // localStorage.setItem("accessToken", newAccessToken);
          // localStorage.setItem("refreshToken", newRefreshToken);

          // originalRequest.headers.Authorization = `bearer ${newAccessToken}`;

          if (showLoading(error.config?.url)){
            setLoading(false);
          }
          return instance(originalRequest);
        } catch (refreshError) {
          // console.error("리프레시 토큰 만료됨");
          // 강제 로그아웃 로직
          if (logoutCallback) {
            logoutCallback();
          }  else {
            // localStorage.removeItem("accessToken");
            // localStorage.removeItem("refreshToken");
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