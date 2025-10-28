import axios,{registerLogout} from "./axiosInstance.js"
import {createContext, useContext, useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useSearchContext} from "./SearchContext.jsx";
// import axios from "axios";

const AuthContext = createContext();

// 타입스크립트 -> 대규모 사이트는 사용한다 -> 타입을 지정해므로써 오류를 사전에 컴파일 에러로 발견가능하여 사용한다

export const AuthProvider = ({children}) => {
  const [user, setUser] = useState(null)
  // username roles nickname userImgId
  const [isLoading, setIsLoading] = useState(true)
  const nav = useNavigate();
  const {resetSearchCondition, reloadCategories} = useSearchContext();

  const fetchUser = async () => {
    try {
      const axiosResponse = await axios.get("/api/me");
      setUser(axiosResponse.data)
    } catch (e) {
      setUser(null)
      // localStorage.removeItem("accessToken");
      // localStorage.removeItem("refreshToken");

      nav("/");

      // if (e.response) {
      //   const {status, data} = e.response;
      //   if (status === 401) { // unAuthorization
      //     nav("/");
      //   }
      // }
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    try {
      await axios.post("/api/logout");

      setUser(null)
      resetSearchCondition();
      reloadCategories();
      nav("/")

    } catch (e) {
      console.error("로그아웃 실패", e);
    }
  }

  const reFetchUser = async () => {
    setIsLoading(true);
    await fetchUser();
  }

  useEffect(() => {
    fetchUser();
    registerLogout(logout);
  }, []);

  return (
      <AuthContext.Provider
          value={{
            user,
            isAuthentication: !!user, // boolean true false 로 반환
            isLoading,
            logout,
            reFetchUser,
          }}
      >
        {children}
      </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth AuthProvider 내부에서 사용되어야 합니다.")
  }

  return context;
}