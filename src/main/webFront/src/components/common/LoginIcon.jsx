import "./LoginIcon.css"
import {useEffect, useRef, useState} from "react";
import {useAuth} from "../../context/AuthContext.jsx";
import {useNavigate} from "react-router-dom";
import UserImg from "./UserImg.jsx";

const LoginIcon = ({menu}) => {
  const [open, setOpen] = useState(false)
  const loginMenuRef = useRef();
  const {logout, isLoading, user} = useAuth();

  const username = user?.username;
  const nickname = user?.nickname;
  const userImgId = user?.userImgId;
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

  const nav = useNavigate();

  const onButtonClick = () => {
    setOpen(!open);
  }

  useEffect(() => {
    const handler = (e) => {
      // const loginMenu = loginMenuRef.current;
      if (loginMenuRef.current && !loginMenuRef.current.contains(e.target)) {
        setOpen(false);
      }
    }

    document.addEventListener('mousedown', handler);

    return () => document.removeEventListener('mousedown', handler);
  }, []);



  return (
      <>
        <div className={"login_icon_box"} ref={loginMenuRef}>
          <div onClick={onButtonClick} className={"login_Icon"}>
            <UserImg
                radius={45}
                nickname={nickname}
                userImg={userImgId ? `${backendBaseUrl}/api/inlineFile/${userImgId}` : ""}
            />
            <div className="arrow">▾</div>
          </div>
          {open && (
            <ul className={"login_icon_dropdown"}>
              <li onClick={()=> nav(`/myPage/${username}`, {
                state:{
                  nickname:nickname
                }
              })}>마이페이지</li>
              <li onClick={() => nav("/tempPost")}>임시작성글</li>
              {!menu && (
                  <li onClick={()=>nav("/postEdit")}>글 작성하기</li>
              )}
              <li onClick={() => {
                logout();
                setOpen(false);
              }}>로그아웃</li>
            </ul>
          )}
        </div>

      </>
  )
}

export default LoginIcon;